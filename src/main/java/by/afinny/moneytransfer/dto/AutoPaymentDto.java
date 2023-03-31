package by.afinny.moneytransfer.dto;

import by.afinny.moneytransfer.entity.constant.TransferPeriodicity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter(AccessLevel.PUBLIC)
@ToString
public class AutoPaymentDto {

    private LocalDateTime startDate;
    private TransferPeriodicity periodicity;
}
