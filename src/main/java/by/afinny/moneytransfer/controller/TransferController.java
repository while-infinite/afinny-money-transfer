package by.afinny.moneytransfer.controller;

import by.afinny.moneytransfer.dto.ChangeStatusResponseDto;
import by.afinny.moneytransfer.dto.CreatePaymentDto;
import by.afinny.moneytransfer.dto.CreatePaymentResponseDto;
import by.afinny.moneytransfer.dto.IsFavoriteTransferDto;
import by.afinny.moneytransfer.dto.RequestRefillBrokerageAccountDto;
import by.afinny.moneytransfer.dto.ResponseBrokerageAccountDto;
import by.afinny.moneytransfer.dto.TransferDto;
import by.afinny.moneytransfer.dto.TransferOrderIdDto;
import by.afinny.moneytransfer.entity.constant.TransferTypeName;
import by.afinny.moneytransfer.service.TransferService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("auth/payments")
@RequiredArgsConstructor
public class TransferController {

    public static final String URL_TRANSFER = "/auth/payments";
    public static final String URL_DELETE_TRANSFER = "/{transferId}/draft";
    public static final String URL_FAVORITES = "/favorites";
    public static final String URL_FAVORITES_TRANSFER = "/favorites/{transferOrderId}";
    public static final String URL_TRANSFER_TYPE = "/transferType";
    public static final String URL_PAYMENT_TYPE = "/paymentType";
    public static final String URL_NEW = "/new";
    public static final String PARAM_CLIENT_ID = "clientId";
    public static final String URL_CHANGE_STATUS = "/{transferId}/status";

    private final TransferService transferService;

    @DeleteMapping("/{transferId}/draft")
    public ResponseEntity<Void> deleteIdDraftTransfer(@RequestParam UUID clientId,
                                                      @PathVariable UUID transferId) {
        transferService.deleteIdDraftTransferOrder(clientId, transferId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/favorites")
    public ResponseEntity<IsFavoriteTransferDto> getFavoriteTransfers(@RequestParam UUID clientId,
                                                                      @RequestBody TransferOrderIdDto transferOrderIdDto) {
        IsFavoriteTransferDto isFavoriteTransferDto = transferService
                .getFavoriteTransferOrder(clientId, transferOrderIdDto.getTransferOrderId());
        return ResponseEntity.ok(isFavoriteTransferDto);
    }

    @GetMapping("favorites/{transferOrderId}")
    public ResponseEntity<TransferDto> getFavoriteTransfers(@RequestParam UUID clientId,
                                                            @PathVariable UUID transferOrderId) {
        TransferDto transferOrder = transferService.getFavoriteTransfer(clientId, transferOrderId);
        return ResponseEntity.ok(transferOrder);
    }

    @GetMapping("/transferType")
    public ResponseEntity<List<TransferTypeName>> getTransferType() {
        List<TransferTypeName> transferType = transferService.getTransferType();
        return ResponseEntity.ok(transferType);
    }

    @GetMapping("/paymentType")
    public ResponseEntity<List<TransferTypeName>> getPaymentType() {
        List<TransferTypeName> paymentType = transferService.getPaymentType();
        return ResponseEntity.ok(paymentType);
    }

    @PostMapping("/new")
    public ResponseEntity<ResponseBrokerageAccountDto> refillBrokerageAccount(@RequestParam UUID clientId,
                                                                              @RequestBody RequestRefillBrokerageAccountDto requestRefillBrokerageAccountDto) {
        ResponseBrokerageAccountDto responseBrokerageAccountDto = transferService.refillBrokerageAccount(clientId, requestRefillBrokerageAccountDto);
        return ResponseEntity.ok(responseBrokerageAccountDto);
    }


    @PostMapping("/new-payment")
    public ResponseEntity<CreatePaymentResponseDto> createPaymentOrTransfer(@RequestParam UUID clientId,
                                                                            @RequestBody CreatePaymentDto createPaymentDto) {
        return ResponseEntity.ok(transferService.createPaymentOrTransfer(clientId, createPaymentDto));
    }

    @PatchMapping("/{transferId}/status")
    public ResponseEntity<ChangeStatusResponseDto> changeStatus(@PathVariable UUID transferId) {
        return ResponseEntity.ok(transferService.changeStatus(transferId));
    }

}