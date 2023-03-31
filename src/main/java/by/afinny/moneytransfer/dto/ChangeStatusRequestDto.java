package by.afinny.moneytransfer.dto;

import by.afinny.moneytransfer.entity.constant.TransferStatus;
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
@Setter
@ToString
public class ChangeStatusRequestDto {
    private UUID transferId;
    private TransferStatus status;
}
