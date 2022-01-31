package tech.qvanphong.firotipbot;

import discord4j.rest.RestClient;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import tech.qvanphong.firotipbot.properties.BotProperties;

@Configuration
public class BeanConfiguration {


    @Bean
    public RestClient discordRestClient(BotProperties botProperties) {
        return RestClient.create(botProperties.getToken());
    }

    @Bean(name = "messageSource")
    public MessageSource getMessageResource() {
        ReloadableResourceBundleMessageSource messageResource = new ReloadableResourceBundleMessageSource();

        // Đọc vào file i18n/messages_xxx.properties
        // Ví dụ: i18n/messages_en.properties
        messageResource.setBasename("classpath:i18n/messages");
        messageResource.setDefaultEncoding("UTF-8");
        return messageResource;
    }

}
