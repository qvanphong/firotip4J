package tech.qvanphong.firotipbot.command;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import tech.qvanphong.firotipbot.api.FiroAPI;
import tech.qvanphong.firotipbot.model.Transaction;
import tech.qvanphong.firotipbot.properties.FiroProperties;
import tech.qvanphong.firotipbot.service.TransactionService;
import tech.qvanphong.firotipbot.service.UserService;
import tech.qvanphong.firotipbot.utility.MessageSourceSender;

import java.math.BigDecimal;

@Component
public class WithdrawCommand implements SlashCommand {
    private final UserService userService;
    private final TransactionService transactionService;
    private final FiroProperties firoProperties;
    private final FiroAPI firoAPI;
    private final MessageSourceSender messageSourceSender;

    public WithdrawCommand(UserService userService, TransactionService transactionService, FiroProperties firoProperties, FiroAPI firoAPI, MessageSourceSender messageSourceSender) {
        this.userService = userService;
        this.transactionService = transactionService;
        this.firoProperties = firoProperties;
        this.firoAPI = firoAPI;
        this.messageSourceSender = messageSourceSender;
    }

    @Override
    public String getName() {
        return "withdraw";
    }

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event) {
        String address = event.getOption("address").get().getValue().get().asString();
        double amount = event.getOption("amount").get().getValue().get().asDouble();

        String userId = event.getInteraction().getUser().getId().asString();
        return event.deferReply()
                .then(userService.getOrCreateUser(userId))
                .flatMap(user -> {
                    if (user.getBalance().compareTo(BigDecimal.valueOf(amount)) < 1) {
                        return messageSourceSender.sendPrivateAndUpdateReply(event, "error.excess_amount", null);
                    }

                    if (!firoAPI.validateAddress(address)) {
                        return messageSourceSender.sendPrivateAndUpdateReply(event, "error.invalid_address", null);
                    }

                    Double txFee = firoProperties.getTxFee();
                    String txId = firoAPI.joinSplit(address, amount - txFee);

                    if (txId == null)
                        return messageSourceSender.sendPrivateAndUpdateReply(event, "error.failed_broadcast", null);

                    return transactionService.createTransaction(txId, userId, amount, Transaction.WITHDRAW_TYPE)
                            .then(userService.lockWithdrawBalance(user, amount - txFee))
                            .then(messageSourceSender.sendPrivateAndUpdateReply(event, "broadcast_tx", new Object[]{address, txId}));
                })
                .then();
    }
}
