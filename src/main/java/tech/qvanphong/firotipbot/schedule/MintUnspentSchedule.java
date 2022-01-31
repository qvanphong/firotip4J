package tech.qvanphong.firotipbot.schedule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tech.qvanphong.firotipbot.api.FiroAPI;

@Component
public class MintUnspentSchedule {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final FiroAPI firoAPI;

    public MintUnspentSchedule(FiroAPI firoAPI) {
        this.firoAPI = firoAPI;
    }

    @Scheduled(fixedDelay = 1000 * 60 * 5)
    public void mintingUnspent() {
        logger.info("[ SCHEDULE ] Auto mint Lelantus");
        firoAPI.autoMintLelantus();
    }
}
