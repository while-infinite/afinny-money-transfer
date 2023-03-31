package by.afinny.moneytransfer.dto;

import by.afinny.moneytransfer.entity.constant.TransferTypeName;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter(AccessLevel.PUBLIC)
@ToString
public class AutoPaymentsDto {

    private UUID transferOrderId;
    private TransferTypeName typeName;
}
