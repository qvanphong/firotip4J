package tech.qvanphong.firotipbot.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import tech.qvanphong.firotipbot.model.Transaction;
import tech.qvanphong.firotipbot.repository.TransactionRepository;

import java.util.Date;

@Service
public class TransactionService {
    private final TransactionRepository repository;

    public TransactionService(TransactionRepository repository) {
        this.repository = repository;
    }

    public Mono<Transaction> createTransaction(String txId, String userId, double amount, String type) {
        return createTransaction(txId, userId, amount, type, Transaction.PENDING_STATUS);
    }

    public Mono<Transaction> createTransaction(String txId, String userId, double amount, String type, String status) {
        Transaction transaction = new Transaction();
        transaction.setUserId(userId);
        transaction.setAmount(amount);
        transaction.setTxId(txId);
        transaction.setType(type);
        transaction.setStatus(status);
        transaction.setTimestamp(new Date());

        return repository.save(transaction);
    }

    public Mono<Transaction> saveTransaction(Transaction transaction) {
        return repository.save(transaction);
    }

    public Mono<Transaction> getTransaction(String txId) {
        return repository.getTransactionByTxId(txId);
    }
}
