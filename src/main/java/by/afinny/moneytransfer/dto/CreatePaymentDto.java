package by.afinny.moneytransfer.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter(AccessLevel.PUBLIC)
@ToString
public class CreatePaymentDto {
    private Integer transferTypeId;
    private String sum;
    private String remitterCardNumber;
    private String name;
    private String payeeAccountNumber;
    private String payeeCardNumber;
    private String inn;
    private String bic;
    private String sumCommission;
    private String purpose;
}
