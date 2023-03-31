package by.afinny.moneytransfer.dto;

import lombok.AccessLevel;
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
@Setter(AccessLevel.PUBLIC)
@ToString
public class TransferDto {
    private Integer transferTypeId;
    private String purpose;
    private UUID payeeId;
    private BigDecimal sum;
    private Boolean isFavorite;
}
