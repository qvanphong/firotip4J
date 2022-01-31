package tech.qvanphong.firotipbot.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import tech.qvanphong.firotipbot.api.FiroAPI;
import tech.qvanphong.firotipbot.model.User;
import tech.qvanphong.firotipbot.properties.FiroProperties;
import tech.qvanphong.firotipbot.repository.UserRepository;

import java.math.BigDecimal;
import java.util.Date;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final FiroAPI firoAPI;
    private final FiroProperties firoProperties;

    public UserService(UserRepository userRepository, FiroAPI firoAPI, FiroProperties firoProperties) {
        this.userRepository = userRepository;
        this.firoAPI = firoAPI;
        this.firoProperties = firoProperties;
    }

    public Mono<User> createNewUser(String userId) {
        String newAddress = firoAPI.getNewAddress();
        if (newAddress != null && !newAddress.isEmpty()) {
            User user = new User();
            user.setUserId(userId);
            user.setAddress(newAddress);
            user.setBalance(BigDecimal.valueOf(0));
            user.setLockedBalance(BigDecimal.valueOf(0));
            user.setCreatedTime(new Date());

            return userRepository.save(user);
        }
        return Mono.error(new Throwable("Can't get new address"));
    }

    public Mono<User> saveUser(User user) {
        return userRepository.save(user);
    }

    public Mono<User> getUser(String address) {
        return userRepository.getUserByAddress(address);
    }

    public Mono<User> getUserById(String id) {
        return userRepository.getUserByUserId(id);
    }

    public Mono<User> getOrCreateUser(String userId) {
        return userRepository.getUserByUserId(userId)
                .switchIfEmpty(createNewUser(userId));
    }

    public Mono<User> increaseBalance(User user, double amount) {
        user.setBalance(user.getBalance().add(BigDecimal.valueOf(amount)));
        return userRepository.save(user);
    }

    public Mono<User> subtractBalance(User user, double amount) {
        user.setBalance(user.getBalance().subtract(BigDecimal.valueOf(amount)));
        return userRepository.save(user);
    }

    public Mono<User> lockWithdrawBalance(User user, double amount) {
        user.setLockedBalance(user.getLockedBalance().add(BigDecimal.valueOf(amount)));
        user.setBalance(user.getBalance().subtract(BigDecimal.valueOf(amount)));

        return userRepository.save(user);
    }
}
