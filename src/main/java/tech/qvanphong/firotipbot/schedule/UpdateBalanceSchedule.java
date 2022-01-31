package tech.qvanphong.firotipbot.schedule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tech.qvanphong.firotipbot.api.FiroAPI;
import tech.qvanphong.firotipbot.model.Transaction;
import tech.qvanphong.firotipbot.model.User;
import tech.qvanphong.firotipbot.service.TransactionService;
import tech.qvanphong.firotipbot.service.UserService;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;

@Component
public class UpdateBalanceSchedule {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final FiroAPI firoAPI;
    private final TransactionService txService;
    private final UserService userService;

    public UpdateBalanceSchedule(FiroAPI firoAPI, TransactionService txService, UserService userService) {
        this.firoAPI = firoAPI;
        this.txService = txService;
        this.userService = userService;
    }

    @Scheduled(fixedDelay = 1000 * 60)
    public void updateBalance() {
        logger.info("[ SCHEDULE ] Updating Balance");
        List<LinkedHashMap<String, Object>> txs = firoAPI.listTransactions();
        for (LinkedHashMap<String, Object> tx : txs) {
            String address = (String) tx.get("address");

            if (address == null) continue;

            String txId = (String) tx.get("txid");
            User receiveUser = userService.getUser(address).block();
            Transaction transaction = txService.getTransaction(txId).block();
            Integer confirmations = (Integer) tx.get("confirmations");

            // handle deposit
            /*
             * In deposit case, user is the person that sending a transaction to generated address from bot.
             * So there is no way to check if there is deposit a transaction in DB.
             * Main condition to check if this is a transaction is:
             * - tx.category  =  receiver
             * - tx.address must exist in user DB
             * - transaction doesn't exist in DB.
             * */
            if (receiveUser != null &&
                    transaction == null &&
                    confirmations >= 1 &&
                    tx.get("category").equals("receive")) {
                Double amount = (Double) tx.get("amount");
                // Create deposit transaction
                txService.createTransaction(txId, receiveUser.getUserId(), amount, Transaction.DEPOSIT_TYPE, Transaction.COMPLETE_STATUS).subscribe();

                // update user's balance
                receiveUser.setBalance(receiveUser.getBalance().add(BigDecimal.valueOf(amount)));
                userService.saveUser(receiveUser).subscribe();

                logger.info("[DEPOSIT] Updated balance of user id: " + receiveUser.getUserId());

                continue;
            }

            /*
             * Handle withdraw, the withdraw tx is created by bot. Which mean the transaction will exist in DB when user
             * request withdraw. The status of transaction is "pending" and type must be "withdraw".
             * things to do with withdraw tx is:
             * - check if tx category is "spend"
             * - remove the locked balance if confirmation > 2 and update transaction status to completed
             *
             * */

            if (transaction != null &&
                    transaction.getType().equals(Transaction.WITHDRAW_TYPE) &&
                    transaction.getStatus().equals(Transaction.PENDING_STATUS) &&
                    confirmations >= 1 &&
                    tx.get("category").equals("spend")) {
                Double amount = (Double) tx.get("amount");
                User userRequested = userService.getUserById(transaction.getUserId()).block();

                if (userRequested == null) {

                    logger.warn("[WITHDRAW] Found an withdraw tx but can't find which user");
                    continue;
                }

                userRequested.setLockedBalance(userRequested.getLockedBalance().add(BigDecimal.valueOf(amount)));
                transaction.setStatus(Transaction.COMPLETE_STATUS);
                userService.saveUser(userRequested)
                        .then(txService.saveTransaction(transaction))
                        .subscribe();
                logger.info("[WITHDRAW] Updated locked balance of user id: " + transaction.getUserId());
            }
        }
    }
}
