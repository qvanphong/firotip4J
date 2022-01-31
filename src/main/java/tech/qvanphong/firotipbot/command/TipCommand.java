package tech.qvanphong.firotipbot.command;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.InteractionReplyEditSpec;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import tech.qvanphong.firotipbot.model.TipLog;
import tech.qvanphong.firotipbot.model.User;
import tech.qvanphong.firotipbot.repository.TipLogRepository;
import tech.qvanphong.firotipbot.service.UserService;
import tech.qvanphong.firotipbot.utility.MessageSourceSender;

import java.math.BigDecimal;
import java.util.Date;

@Component
public class TipCommand implements SlashCommand {
    private final UserService userService;
    private final MessageSourceSender messageSourceSender;
    private final MessageSource messageSource;
    private final TipLogRepository tipLogRepository;

    public TipCommand(UserService userService, MessageSourceSender messageSourceSender, MessageSource messageSource, TipLogRepository tipLogRepository) {
        this.userService = userService;
        this.messageSourceSender = messageSourceSender;
        this.messageSource = messageSource;
        this.tipLogRepository = tipLogRepository;
    }


    @Override
    public String getName() {
        return "tip";
    }

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event) {
        String senderId = event.getInteraction().getUser().getId().asString();
        Mono<String> receiverIdMono = event.getOption("user").get().getValue().get().asUser().map(user -> user.getId().asString());
        double amount = event.getOption("amount").get().getValue().get().asDouble();
        boolean hasMessage = event.getOption("message").isPresent();

        return event.deferReply()
                .then(userService.getOrCreateUser(senderId))
                .zipWith(receiverIdMono.flatMap(userService::getOrCreateUser))
                .flatMap((tuples) -> {
                    User sender = tuples.getT1();
                    User receiver = tuples.getT2();
                    if (sender.getBalance().compareTo(BigDecimal.valueOf(amount)) >= 0) {
                        String senderUsername = event.getInteraction().getUser().getUsername();
                        String senderAvatarUrl = event.getInteraction().getUser().getAvatarUrl();


                        EmbedCreateSpec.Builder embedBuilder = EmbedCreateSpec.builder()
                                .author(senderUsername, null, senderAvatarUrl)
                                .thumbnail(senderAvatarUrl)
                                .title("Tipping")
                                .description(messageSource.getMessage("tipping.description", new Object[]{sender.getUserId(), receiver.getUserId()}, messageSourceSender.locale));

                        if (hasMessage) {
                            embedBuilder.addField("Message", event.getOption("message").get().getValue().get().asString(), false);
                        }
                        embedBuilder.addField("Amount", String.format("**%.3f FIRO**", amount), false);

                        InteractionReplyEditSpec replyEditSpec = InteractionReplyEditSpec.builder().addEmbed(embedBuilder.build()).build();
                        return userService.subtractBalance(sender, amount)
                                .then(userService.increaseBalance(receiver, amount))
                                .then(tipLogRepository.save(new TipLog(null, senderId, receiver.getId(), BigDecimal.valueOf(amount), new Date())))
                                .then(event.editReply(replyEditSpec));
                    } else {
                        return messageSourceSender.updateReply(event, "error.insufficient_balance", null);
                    }
                })
                .then();
    }
}
