package tech.qvanphong.firotipbot.command;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import tech.qvanphong.firotipbot.service.UserService;
import tech.qvanphong.firotipbot.utility.MessageSourceSender;

@Component
public class DepositCommand implements SlashCommand{
    private final UserService userService;
    private final MessageSourceSender messageSourceSender;

    public DepositCommand(UserService userService, MessageSourceSender messageSourceSender) {
        this.userService = userService;
        this.messageSourceSender = messageSourceSender;
    }

    @Override
    public String getName() {
        return "deposit";
    }

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event) {
        String userId = event.getInteraction().getUser().getUserData().id().asString();
        return event.deferReply()
                .then(userService.getOrCreateUser(userId))
                .flatMap(user -> messageSourceSender.sendPrivateAndUpdateReply(event, "deposit", new Object[]{user.getAddress()}))
                .then();
    }
}
