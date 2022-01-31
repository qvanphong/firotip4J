package tech.qvanphong.firotipbot.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tech.qvanphong.firotipbot.properties.FiroProperties;
import tech.qvanphong.firotipbot.properties.RPCProperties;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class FiroAPI {
    private final String rpcUrl;
    private final OkHttpClient httpClient;
    private final String basicAuthorization;
    private final ObjectMapper objectMapper;

    @Autowired
    public FiroAPI(FiroProperties firoProperties) {
        RPCProperties rpc = firoProperties.getRpc();

        this.basicAuthorization = Credentials.basic(rpc.getUser(), rpc.getPwd());
        this.rpcUrl = "http://127.0.0.1:" + rpc.getPort();
        this.httpClient = new OkHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    public List<LinkedHashMap<String, Object>> listTransactions() {
        JsonArray arrayBody = new JsonArray();
        arrayBody.add("*");
        arrayBody.add(100);

        return execute("listtransactions", List.class, arrayBody);
    }

    public String getNewAddress() {
        return execute("getnewaddress", String.class);
    }

    public boolean validateAddress(String address) {
        JsonArray jsonArray = new JsonArray();
        jsonArray.add(address);
        Map result = execute("validateaddress", Map.class, jsonArray);
        if (result != null) {
            return (boolean) result.get("isvalid");
        }
        return false;
    }

    public String joinSplit(String address, double amount) {
        JsonObject paramObject = new JsonObject();
        paramObject.addProperty(address, amount);

        JsonArray subtractFeeFromAddresses = new JsonArray();
        subtractFeeFromAddresses.add(address);

        JsonArray jsonArray = new JsonArray();
        jsonArray.add(paramObject);
        jsonArray.add(subtractFeeFromAddresses);

        return execute("joinsplit", String.class, jsonArray);
    }

    public Map<String, Object> getTransaction(String txId) {
        JsonArray params = new JsonArray();
        params.add(txId);

        return execute("gettransaction", Map.class, params);
    }

    public Double getTxFee(String txId) {
        Map<String, Object> transaction = this.getTransaction(txId);
        if (transaction != null)
             return (Double) transaction.get("fee");
        return null;
    }

    public Double getEstimateFee() {
        JsonArray params = new JsonArray();
        params.add(2);
        return execute("estimatefee", Double.class, params);
    }

    public List<Object> autoMintLelantus() {
        return execute("autoMintlelantus", List.class);
    }

    public boolean unlockWallet(String passphrase) {
        JsonArray params = new JsonArray();
        params.add(passphrase);
        params.add(100000000);

        JsonObject body = new JsonObject();
        body.addProperty("jsonrpc", "1.0");
        body.addProperty("id", "tip_bot");
        body.addProperty("method", "walletpassphrase");
        body.add("params", params);

        RequestBody requestBody = RequestBody.create(body.toString(), MediaType.get("application/json; charset=utf-8"));

        Request request = new Request.Builder()
                .addHeader("Authorization", basicAuthorization)
                .url(this.rpcUrl)
                .method("POST", requestBody)
                .build();

        Response response = null;
        try {
            response = httpClient.newCall(request).execute();
            return response.isSuccessful();

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (response != null) response.close();
        }

    }

    private <T> T execute(String method, Class<T> clazz, JsonArray params) {
        JsonObject body = new JsonObject();
        body.addProperty("jsonrpc", "1.0");
        body.addProperty("id", "tip_bot");
        body.addProperty("method", method);

        if (params != null) {
            body.add("params", params);
        }

        RequestBody requestBody = RequestBody.create(body.toString(), MediaType.get("application/json; charset=utf-8"));

        Request request = new Request.Builder()
                .addHeader("Authorization", basicAuthorization)
                .url(this.rpcUrl)
                .method("POST", requestBody)
                .build();

        Response response = null;
        try {
            response = httpClient.newCall(request).execute();

            if (response.isSuccessful()) {
                JsonNode jsonNode = objectMapper.readTree(Objects.requireNonNull(response.body()).string());
                JsonNode resultNode = jsonNode.get("result");
                if (!resultNode.isNull()) {
                    return objectMapper.treeToValue(resultNode, clazz);
                }
            }

            return null;
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (response != null) response.close();
        }
    }
    private <T extends Object> T execute(String method, Class<T> clazz) {
        return execute(method, clazz, null);
    }

}
