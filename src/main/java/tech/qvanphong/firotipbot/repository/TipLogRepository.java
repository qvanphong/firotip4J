package tech.qvanphong.firotipbot.repository;

import org.bson.types.ObjectId;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import tech.qvanphong.firotipbot.model.TipLog;

@Repository
public interface TipLogRepository extends ReactiveCrudRepository<TipLog, ObjectId> {
    @Override
    <S extends TipLog> Mono<S> save(S entity);
}
