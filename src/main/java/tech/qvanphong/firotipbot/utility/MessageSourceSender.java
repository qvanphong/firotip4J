package tech.qvanphong.firotipbot.utility;

import com.mongodb.lang.Nullable;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.entity.Message;
import discord4j.rest.http.client.ClientException;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Locale;

@Component
public class MessageSourceSender {
    public final Locale locale = Locale.ENGLISH;

    private final MessageSource messageSource;

    public MessageSourceSender(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public Mono<Message> sendPrivateAndUpdateReply(ChatInputInteractionEvent event, String code, @Nullable Object[] args) {
        boolean isUseFromGuild = event.getInteraction().getGuildId().isPresent();

        if (isUseFromGuild) {
            return sendPrivateMessage(event, code, args)
                    .then(event.editReply(messageSource.getMessage("sent_dm", null, locale))
                            .onErrorResume(throwable -> {
                                if (throwable instanceof ClientException) {
                                    return event.editReply(messageSource.getMessage("error.private_message", null, locale));
                                }
                                return Mono.empty();
                            }));
        } else {
            return event.editReply(messageSource.getMessage(code, args, locale));
        }
    }

    public Mono<Message> sendPrivateMessage(ChatInputInteractionEvent event, String code, @Nullable Object[] args) {
        String userId = event.getInteraction().getUser().getUserData().id().asString();
        return event.getClient().getUserById(Snowflake.of(userId))
                .flatMap(discord4j.core.object.entity.User::getPrivateChannel)
                .flatMap(privateChannel -> privateChannel.createMessage(messageSource.getMessage(code, args, locale)));
    }

    public Mono<Message> updateReply(ChatInputInteractionEvent event, String code, @Nullable Object[] args) {
        return event.editReply(messageSource.getMessage(code, args, locale));
    }

}
