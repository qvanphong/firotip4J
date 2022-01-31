package tech.qvanphong.firotipbot.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;
import tech.qvanphong.firotipbot.model.Rain;

public interface RainRepository extends ReactiveCrudRepository<Rain, String> {

    @Override
    <S extends Rain> Mono<S> save(S entity);
}
