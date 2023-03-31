package by.afinny.moneytransfer.dto.kafka;

import by.afinny.moneytransfer.entity.constant.TransferStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ConsumerTransferOrderEvent {
    private String authorizationCode;
    private LocalDateTime createdAt;
    private UUID id;
    private TransferStatus status;
}
