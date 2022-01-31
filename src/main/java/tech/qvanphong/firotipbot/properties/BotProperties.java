package tech.qvanphong.firotipbot.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("bot")
public @Data class BotProperties {
    private String token;

    public void setToken(String token) {
        this.token = token;
    }
}
