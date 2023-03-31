package by.afinny.moneytransfer.dto.kafka;

import by.afinny.moneytransfer.entity.TransferType;
import by.afinny.moneytransfer.entity.constant.TransferStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ProducerTransferOrderEvent {

    private UUID id;
    private LocalDateTime createdAt;
    private TransferType transferType;
    private String purpose;
    private String remitterCardNumber;
    private BigDecimal sum;
    private BigDecimal sumCommission;
    private TransferStatus transferStatus;
    private UUID brokerageId;
}