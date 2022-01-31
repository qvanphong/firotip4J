package tech.qvanphong.firotipbot;

import discord4j.core.DiscordClientBuilder;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ApplicationContext;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import reactor.core.publisher.Mono;
import tech.qvanphong.firotipbot.api.FiroAPI;
import tech.qvanphong.firotipbot.listener.SlashCommandListener;
import tech.qvanphong.firotipbot.properties.BotProperties;
import tech.qvanphong.firotipbot.properties.FiroProperties;

@SpringBootApplication
@ConfigurationPropertiesScan(basePackages = "tech.qvanphong.firotipbot.properties")
@EnableReactiveMongoRepositories
@EnableScheduling
public class SpringBootMain {
    public static void main(String[] args) throws Exception {
        //Start spring application
        ApplicationContext springContext = new SpringApplicationBuilder(SpringBootMain.class)
                .build()
                .run(args);

        BotProperties botProperties = springContext.getBean(BotProperties.class);
        FiroProperties firoProperties = springContext.getBean(FiroProperties.class);
        FiroAPI firoAPI = springContext.getBean(FiroAPI.class);

        if (!firoAPI.unlockWallet(firoProperties.getRpc().getPassphrase())) {
            throw new Exception("Fail to unlock wallet. Please check your passphrase, node status and RPC server again");
        }

        //Login
        DiscordClientBuilder.create(botProperties.getToken()).build()
                .withGateway(gatewayClient -> {
                    SlashCommandListener slashCommandListener = new SlashCommandListener(springContext);

                    Mono<Void> onSlashCommandMono = gatewayClient
                            .on(ChatInputInteractionEvent.class, slashCommandListener::handle)
                            .then();

                    return Mono.when(onSlashCommandMono);
                }).block();
    }
}
