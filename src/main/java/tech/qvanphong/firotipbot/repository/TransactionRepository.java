package tech.qvanphong.firotipbot.repository;

import org.bson.types.ObjectId;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;
import tech.qvanphong.firotipbot.model.Transaction;

public interface TransactionRepository extends ReactiveCrudRepository<Transaction, ObjectId> {
    @Override
    <S extends Transaction> Mono<S> save(S entity);

    Mono<Transaction> getTransactionByTxId(String txId);
}
