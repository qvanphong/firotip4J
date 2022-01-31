package tech.qvanphong.firotipbot.model;

import java.math.BigDecimal;

public class Winner {
    private String userId;
    private BigDecimal amount;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Winner() {
    }

    public Winner(String userId, BigDecimal amount) {
        this.userId = userId;
        this.amount = amount;
    }
}