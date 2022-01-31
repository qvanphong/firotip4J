package tech.qvanphong.firotipbot.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;
import tech.qvanphong.firotipbot.model.User;

public interface UserRepository extends ReactiveCrudRepository<User, String> {
    @Override
    <S extends User> Mono<S> save(S entity);

    Mono<User> getUserByAddress(String address);

    Mono<User> getUserByUserId(String userId);

    Mono<Boolean> existsUserByUserId(String userId);

}
