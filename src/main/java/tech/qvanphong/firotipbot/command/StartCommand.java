package tech.qvanphong.firotipbot.command;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import tech.qvanphong.firotipbot.api.FiroAPI;
import tech.qvanphong.firotipbot.model.User;
import tech.qvanphong.firotipbot.repository.UserRepository;
import tech.qvanphong.firotipbot.utility.MessageSourceSender;

import java.math.BigDecimal;
import java.util.Date;

@Component
public class StartCommand implements SlashCommand {
    private final UserRepository userRepository;
    private final FiroAPI firoAPI;
    private final MessageSource messageSource;
    private final MessageSourceSender messageSourceSender;

    public StartCommand(UserRepository userRepository, FiroAPI firoAPI, MessageSource messageSource, MessageSourceSender messageSourceSender) {
        this.userRepository = userRepository;
        this.firoAPI = firoAPI;
        this.messageSource = messageSource;
        this.messageSourceSender = messageSourceSender;
    }

    @Override
    public String getName() {
        return "start";
    }

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event) {
        String userId = event.getInteraction().getUser().getUserData().id().asString();

        return event.deferReply()
                .then(userRepository.existsUserByUserId(userId))
                .flatMap(isExisted -> {
                    if (!isExisted) {
                        String newAddress = firoAPI.getNewAddress();
                        if (newAddress != null && !newAddress.isEmpty()) {
                            User user = new User();
                            user.setUserId(userId);
                            user.setAddress(newAddress);
                            user.setBalance(BigDecimal.ZERO);
                            user.setLockedBalance(BigDecimal.ZERO);
                            user.setCreatedTime(new Date());

                            return userRepository.save(user)
                                    .flatMap(newlySavedUser -> messageSourceSender.sendPrivateAndUpdateReply(event, "wallet.new", new Object[]{newAddress}));
                        } else {
                            return messageSourceSender.sendPrivateAndUpdateReply(event, "error.cant_create_address", null);
                        }
                    }
                    return messageSourceSender.updateReply(event, "wallet.existed", null);
                })
                .then();
    }
}
