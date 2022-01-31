package tech.qvanphong.firotipbot.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.Date;

@Document("tip_logs")
@AllArgsConstructor
public @Data class TipLog {
    @Id
    private ObjectId id;

    private String sender;

    private String receiver;

    private BigDecimal amount;

    private Date date;
}
