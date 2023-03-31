package by.afinny.moneytransfer.dto.kafka;

import by.afinny.moneytransfer.entity.constant.TransferStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter(AccessLevel.PUBLIC)
@ToString
public class ConsumerСreatePaymentEvent {
    private UUID clientId;
    private UUID transferOrderId;
    private TransferStatus transferStatus;
    private LocalDateTime completedAt;
    private String authorizationCode;
    private String remitterCardNumber;
    private BigDecimal sum;
}
