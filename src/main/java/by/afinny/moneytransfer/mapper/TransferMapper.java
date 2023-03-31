package by.afinny.moneytransfer.mapper;

import by.afinny.moneytransfer.dto.AutoPaymentsDto;
import by.afinny.moneytransfer.dto.ChangeStatusResponseDto;
import by.afinny.moneytransfer.dto.CreatePaymentDepositDto;
import by.afinny.moneytransfer.dto.CreatePaymentResponseDto;
import by.afinny.moneytransfer.dto.CreditCardStatementDto;
import by.afinny.moneytransfer.dto.DetailsHistoryDto;
import by.afinny.moneytransfer.dto.IsFavoriteTransferDto;
import by.afinny.moneytransfer.dto.ResponseBrokerageAccountDto;
import by.afinny.moneytransfer.dto.ResponseClientDataDto;
import by.afinny.moneytransfer.dto.TransferDto;
import by.afinny.moneytransfer.dto.kafka.ConsumerСreatePaymentEvent;
import by.afinny.moneytransfer.dto.kafka.ProducerTransferOrderEvent;
import by.afinny.moneytransfer.dto.kafka.ProducerСreatePaymentEvent;
import by.afinny.moneytransfer.entity.TransferOrder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.UUID;

@Mapper
public interface TransferMapper {

    List<AutoPaymentsDto> toAutoPaymentDtoList(List<TransferOrder> transferOrderList);

    @Mapping(source = "transferOrder.id", target = "transferOrderId")
    @Mapping(source = "transferOrder.transferType.transferTypeName", target = "typeName")
    AutoPaymentsDto toAutoPaymentDto(TransferOrder transferOrder);

    IsFavoriteTransferDto toIsFavoriteTransferDto(TransferOrder transferOrder);

    @Mapping(target = "transferTypeId", source = "transferType.id")
    @Mapping(target = "payeeId", source = "payee.id")
    TransferDto transferOrderToTransferDto(TransferOrder transferOrder);

    @Mapping(source = "payee.payeeType", target = "payeeType")
    @Mapping(source = "payee.name", target = "name")
    @Mapping(source = "payee.inn", target = "inn")
    @Mapping(source = "payee.bic", target = "bic")
    @Mapping(source = "payee.payeeAccountNumber", target = "payeeAccountNumber")
    @Mapping(source = "payee.payeeCardNumber", target = "payeeCardNumber")
    DetailsHistoryDto toDetailsHistoryDto(TransferOrder transferOrder);

    @Mapping(target = "transferOrderId", source = "transferOrder.id")
    @Mapping(target = "payeeId", source = "payee.id")
    @Mapping(target = "typeName", source = "transferType.transferTypeName")
    @Mapping(target = "currencyCode", source = "transferType.currencyCode")
    CreditCardStatementDto toCreditCardStatementDto(TransferOrder transferOrder);

    List<CreditCardStatementDto> toCreditCardStatementDto(List<TransferOrder> transferOrders);

    @Mapping(target = "status", source = "transferOrder.transferStatus")
    @Mapping(target = "transferTypeId", source = "transferOrder.transferType.id")
    @Mapping(target = "brokerageId", source = "transferOrder.brokerage.id")
    ResponseBrokerageAccountDto toResponseBrokerageAccountDto(TransferOrder transferOrder, ResponseClientDataDto responseClientDataDto);

    @Mapping(target = "brokerageId", source = "transferOrder.brokerage.id")
    ProducerTransferOrderEvent toProducerTransferOrderEvent(TransferOrder transferOrder);

    @Mapping(target = "id", qualifiedByName = "uuidToString")
    @Mapping(target = "created_at", source = "createdAt")
    @Mapping(target = "status", source = "transferStatus")
    @Mapping(target = "card_number", source = "remitterCardNumber")
    @Mapping(target = "transfer_type_id", source = "transferType.id")
    @Mapping(target = "remitter_card_number", source = "remitterCardNumber")
    @Mapping(target = "name", source = "payee.name")
    @Mapping(target = "payee_account_number", source = "payee.payeeAccountNumber")
    @Mapping(target = "payee_card_number", source = "payee.payeeCardNumber")
    @Mapping(target = "sum_commission", source = "sumCommission")
    @Mapping(target = "inn", source = "payee.inn")
    @Mapping(target = "bic", source = "payee.bic")
    CreatePaymentResponseDto toCreatePaymentResponseDto(TransferOrder payment);

    @Mapping(target = "transferOrderId", source = "id")
    ProducerСreatePaymentEvent toСreatePaymentEvent(TransferOrder payment);

    CreatePaymentDepositDto toCreatePaymentDepositDto(ConsumerСreatePaymentEvent event);

    @Mapping(target = "transferId", source = "id")
    @Mapping(target = "status", source = "transferStatus")
    ChangeStatusResponseDto toChangeStatusResponseDto(TransferOrder transferOrder);

    @Named("uuidToString")
    default String uuidToString(UUID id) {
        return id.toString();
    };


}