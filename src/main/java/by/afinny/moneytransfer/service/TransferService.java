package by.afinny.moneytransfer.service;

import by.afinny.moneytransfer.dto.ChangeStatusResponseDto;
import by.afinny.moneytransfer.dto.CreatePaymentDto;
import by.afinny.moneytransfer.dto.CreatePaymentResponseDto;
import by.afinny.moneytransfer.dto.CreditCardStatementDto;
import by.afinny.moneytransfer.dto.DebitCardStatementDto;
import by.afinny.moneytransfer.dto.DetailsHistoryDto;
import by.afinny.moneytransfer.dto.FilterOptionsDto;
import by.afinny.moneytransfer.dto.IsFavoriteTransferDto;
import by.afinny.moneytransfer.dto.RequestRefillBrokerageAccountDto;
import by.afinny.moneytransfer.dto.ResponseBrokerageAccountDto;
import by.afinny.moneytransfer.dto.TransferDto;
import by.afinny.moneytransfer.dto.TransferOrderHistoryDto;
import by.afinny.moneytransfer.dto.kafka.ConsumerTransferOrderEvent;
import by.afinny.moneytransfer.dto.kafka.ConsumerСreatePaymentEvent;
import by.afinny.moneytransfer.entity.TransferOrder;
import by.afinny.moneytransfer.entity.constant.TransferTypeName;

import java.util.List;
import java.util.UUID;

public interface TransferService {

    void deleteIdDraftTransferOrder(UUID clientId,UUID transferId);

    TransferDto getFavoriteTransfer(UUID clientId, UUID transferOrderId);

    IsFavoriteTransferDto getFavoriteTransferOrder(UUID clientId,UUID transferOrderId);

    List<TransferOrderHistoryDto> getTransferOrderHistory(FilterOptionsDto filterOptions);

    DetailsHistoryDto getDetailsHistory(UUID clientId, UUID transferOrderId);

    List<CreditCardStatementDto> getCreditCardStatement(UUID cardId, UUID clientId, String form, String to, Integer pageNumber, Integer pageSize);

    List<DebitCardStatementDto> getViewDebitCardStatement(UUID cardId, UUID clientId, String from, String to, Integer pageNumber, Integer pageSize);

    List<TransferTypeName> getTransferType();

    List<TransferTypeName> getPaymentType();

    CreatePaymentResponseDto createPaymentOrTransfer(UUID clientId, CreatePaymentDto createPaymentDto);
    ResponseBrokerageAccountDto refillBrokerageAccount(UUID clientId, RequestRefillBrokerageAccountDto requestRefillBrokerageAccountDto);

    void modifyTransferOrder(ConsumerTransferOrderEvent ConsumerTransferOrderEvent);
    void updatePaymentOrTransfer(ConsumerСreatePaymentEvent event);

    void sendToKafka(TransferOrder transferOrder);

    ChangeStatusResponseDto changeStatus(UUID transferId);
}
