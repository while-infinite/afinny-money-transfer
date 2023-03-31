package by.afinny.moneytransfer.dto;

import by.afinny.moneytransfer.entity.constant.CurrencyCode;
import by.afinny.moneytransfer.entity.constant.TransferStatus;
import by.afinny.moneytransfer.entity.constant.TransferTypeName;
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
public class TransferOrderHistoryDto {

    private UUID transferOrderId;
    private LocalDateTime createdAt;
    private String purpose;
    private UUID payeeId;
    private BigDecimal sum;
    private LocalDateTime completedAt;
    private TransferStatus transferStatus;
    private TransferTypeName transferTypeName;
    private CurrencyCode currencyCode;
    private String remitterCardNumber;
    private String name;
}
