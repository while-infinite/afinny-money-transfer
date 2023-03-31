package by.afinny.moneytransfer.dto;

import by.afinny.moneytransfer.entity.constant.OperationType;
import by.afinny.moneytransfer.entity.constant.TransferTypeName;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter(AccessLevel.PUBLIC)
@ToString
public class FilterOptionsDto {

    @NotNull
    private UUID clientId;
    @NotNull
    private Integer pageNumber;
    @NotNull
    private Integer pageSize;
    private String purpose;
    private BigDecimal sum;
    private BigDecimal min_sum;
    private BigDecimal max_sum;
    private @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from;
    private @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to;
    private String remitterCardNumber;
    private TransferTypeName type_name;
    private OperationType operationType;
}
