package tech.qvanphong.firotipbot.command;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.entity.User;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.InteractionReplyEditSpec;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.qvanphong.firotipbot.model.Winner;
import tech.qvanphong.firotipbot.service.RainService;
import tech.qvanphong.firotipbot.service.UserService;
import tech.qvanphong.firotipbot.utility.MessageSourceSender;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Component
public class RainCommand implements SlashCommand {
    private final MessageSource messageSource;
    private final MessageSourceSender messageSourceSender;
    private final UserService userService;
    private final RainService rainService;

    public RainCommand(MessageSource messageSource, MessageSourceSender messageSourceSender, UserService userService, RainService rainService) {
        this.messageSource = messageSource;
        this.messageSourceSender = messageSourceSender;
        this.userService = userService;
        this.rainService = rainService;
    }

    @Override
    public String getName() {
        return "rain";
    }

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event) {
        double amount = event.getOption("amount").get().getValue().get().asDouble();
        long usersToRainOn = event.getOption("users_to_rain_on").get().getValue().get().asLong();

        String username = event.getInteraction().getUser().getUsername();
        String avatarUrl = event.getInteraction().getUser().getAvatarUrl();
        String userId = event.getInteraction().getUser().getId().asString();

        AtomicReference<BigDecimal> eachUserReceive = new AtomicReference<>(new BigDecimal(0));
        StringBuilder winnersStringBuilder = new StringBuilder();
        List<Winner> winners = new ArrayList<>();

        return event.deferReply()
                .then(userService.getOrCreateUser(userId))
                .flatMap(userRaining -> {
                    // Check if user have enough balance to rain
                    if (userRaining.getBalance().compareTo(BigDecimal.valueOf(amount)) < 0) {
                        return event.editReply(messageSource.getMessage("error.not_enough_to_rain", null, messageSourceSender.locale));
                    }

                    // Update balance of user that using rain.
                    userRaining.setBalance(userRaining.getBalance().subtract(BigDecimal.valueOf(amount)));
                    return userService.saveUser(userRaining)
                            .then(event.getInteraction().getChannel())

                            // Getting 100 latest message
                            .flatMap(messageChannel -> messageChannel.getMessagesBefore(Snowflake.of(Instant.now()))
                                    .take(100)
                                    .mapNotNull(message -> message.getAuthor().orElse(null))
                                    .collectList())

                            // Take user from message to select the lucky users
                            .flatMapMany(users -> {
                                Set<User> setOfUsers = users.stream()
                                        .filter(user -> !user.isBot() && !user.getId().equals(event.getInteraction().getUser().getId()))
                                        .collect(Collectors.toSet());
                                List<User> selectedUsers = new ArrayList<>(setOfUsers);

                                if (selectedUsers.size() > usersToRainOn) {
                                    Collections.shuffle(selectedUsers);
                                    selectedUsers = selectedUsers.subList(0, (int) usersToRainOn);
                                }

                                eachUserReceive.set(BigDecimal.valueOf(amount).divide(BigDecimal.valueOf(selectedUsers.size()),2, RoundingMode.FLOOR));

                                return Flux.just(selectedUsers.toArray(new User[0]));
                            })

                            // append string for message and update to Db
                            .flatMap(user -> {
                                winnersStringBuilder.append("<@!").append(user.getId().asString()).append("> ");

                                Winner winner = new Winner(user.getId().asString(), eachUserReceive.get());
                                winners.add(winner);

                                return userService.getOrCreateUser(user.getId().asString())
                                        .flatMap(winnerFromDb -> {
                                            winnerFromDb.setBalance(winnerFromDb.getBalance().add(eachUserReceive.get()));
                                            return userService.saveUser(winnerFromDb);
                                        });
                            })
                            .then(rainService.saveRainLog(userId, amount, winners))
                            // Build up and embed message and create editReply mono
                            .then(Mono.fromCallable(() -> EmbedCreateSpec.builder()
                                    .author(username, null, avatarUrl)
                                    .thumbnail(avatarUrl)
                                    .title(messageSource.getMessage("rain.title", new Object[]{username}, messageSourceSender.locale))
                                    .description(messageSource.getMessage("rain.description", new Object[]{usersToRainOn, eachUserReceive.get()}, messageSourceSender.locale))
                                    .addField("Lucky Users", winnersStringBuilder.toString(), false).build()))
                            .flatMap(winnerEmbed -> event.editReply(InteractionReplyEditSpec.builder()
                                    .addEmbed(winnerEmbed)
                                    .build()));
                })
                .then();
    }
}
