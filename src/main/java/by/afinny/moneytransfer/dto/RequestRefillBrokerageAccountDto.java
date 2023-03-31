package by.afinny.moneytransfer.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class RequestRefillBrokerageAccountDto {
    @JsonProperty("transfer_type_id")
    private Integer transferTypeId;
    @JsonProperty("sum")
    private BigDecimal sum;
    @JsonProperty("remitter_card_number")
    private String remitterCardNumber;
    @JsonProperty("brokerage_id")
    private UUID brokerageId;
}