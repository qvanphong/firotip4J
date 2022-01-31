package tech.qvanphong.firotipbot.service;

import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import tech.qvanphong.firotipbot.model.Rain;
import tech.qvanphong.firotipbot.model.Winner;
import tech.qvanphong.firotipbot.repository.RainRepository;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Component
public class RainService {
    private final RainRepository rainRepository;

    public RainService(RainRepository rainRepository) {
        this.rainRepository = rainRepository;
    }

    public Mono<Rain> saveRainLog(String userId, double amount, List<Winner> winners) {
        Rain rain = new Rain();
        rain.setUserId(userId);
        rain.setAmount(BigDecimal.valueOf(amount));
        rain.setWinners(winners);
        rain.setDate(new Date());

        return rainRepository.save(rain);
    }
}
