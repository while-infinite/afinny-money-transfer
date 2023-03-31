package by.afinny.moneytransfer.dto;

import by.afinny.moneytransfer.entity.constant.PayeeType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter(AccessLevel.PUBLIC)
@ToString
public class DetailsHistoryDto {

    private LocalDateTime createdAt;
    private String remitterCardNumber;
    private BigDecimal sumCommission;
    private BigDecimal currencyExchange;
    private PayeeType payeeType;
    private String name;
    private String inn;
    private String bic;
    private String payeeAccountNumber;
    private String payeeCardNumber;
}