package by.afinny.moneytransfer.dto;

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
public class ResponseBrokerageAccountDto {

    private UUID id;
    private String firstName;
    private String lastName;
    private String middleName;
    private LocalDateTime createdAt;
    private TransferStatus status;
    private Integer transferTypeId;
    private BigDecimal sum;
    private String remitterCardNumber;
    private BigDecimal sumCommission;
    private UUID brokerageId;
    private Boolean isFavorite;
    private String purpose;
    private String authorizationCode;
}