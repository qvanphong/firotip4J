package tech.qvanphong.firotipbot.model;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.Map;

@Document("txs")
public @Data
class Transaction {
    @Id
    private ObjectId id;

    private String txId;

    private String userId;

    private String type;

    private String status;

    private Double amount;

    private Date timestamp;

    private Map<String, Object> detail;

    public static final String WITHDRAW_TYPE = "withdraw";
    public static final String DEPOSIT_TYPE = "deposit";

    public static final String PENDING_STATUS = "pending";
    public static final String COMPLETE_STATUS = "complete";
}
