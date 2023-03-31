package by.afinny.moneytransfer.mapper;

import by.afinny.moneytransfer.dto.DebitCardStatementDto;
import by.afinny.moneytransfer.dto.TransferOrderHistoryDto;
import by.afinny.moneytransfer.entity.TransferOrder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;


@Mapper
public interface TransferHistoryMapper {

    List<TransferOrderHistoryDto> toTransferOrderHistoryDto(List<TransferOrder> transferOrderList);

    @Mapping(source = "transferOrder.id", target = "transferOrderId")
    @Mapping(source = "payee.id", target = "payeeId")
    @Mapping(source = "payee.name", target = "name")
    @Mapping(source = "transferType.currencyCode", target = "currencyCode")
    @Mapping(source = "transferType.transferTypeName", target = "transferTypeName")
    TransferOrderHistoryDto toTransfer(TransferOrder transferOrder);

    List<DebitCardStatementDto> toDebitCardStatementDto(List<TransferOrder> transferOrders);

    @Mapping(target = "transferOrderId", source = "transferOrder.id")
    @Mapping(target = "payeeId", source = "payee.id")
    @Mapping(target = "typeName", source = "transferType.transferTypeName")
    @Mapping(target = "currencyCode", source = "transferType.currencyCode")
    DebitCardStatementDto toDebitCardStatementDto(TransferOrder transferOrder);
}
