package by.afinny.moneytransfer.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter(AccessLevel.PUBLIC)
@ToString
public class CommissionDto {

    private Integer id;
    private BigDecimal minCommission;
    private BigDecimal maxCommission;
    private BigDecimal percentCommission;
    private BigDecimal fixCommission;
    private BigDecimal minSum;
    private BigDecimal maxSum;
}


