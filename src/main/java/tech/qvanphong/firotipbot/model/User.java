package tech.qvanphong.firotipbot.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.Date;

@Document("users")
public @Data
class User {
    @Id
    private String id;

    private String userId;

    private String address;

    private BigDecimal balance;

    private BigDecimal lockedBalance;

    private Date createdTime;
}
