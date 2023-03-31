package by.afinny.moneytransfer.dto.kafka;

import by.afinny.moneytransfer.entity.Payee;
import by.afinny.moneytransfer.entity.TransferType;
import by.afinny.moneytransfer.entity.constant.OperationType;
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
public class ProducerСreatePaymentEvent {
    private UUID transferOrderId;
    private UUID clientId;
    private LocalDateTime createdAt;
    private TransferType transferType;
    private String purpose;
    private String remitterCardNumber;
    private Payee payee;
    private BigDecimal sum;
    private BigDecimal sumCommission;
    private String authorizationCode;
    private BigDecimal currencyExchange;
    private OperationType operationType;
}
