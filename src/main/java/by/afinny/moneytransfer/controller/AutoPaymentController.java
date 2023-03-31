package by.afinny.moneytransfer.controller;

import by.afinny.moneytransfer.dto.AutoPaymentDto;
import by.afinny.moneytransfer.dto.AutoPaymentsDto;
import by.afinny.moneytransfer.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("auth/autopayments")
@RequiredArgsConstructor
public class AutoPaymentController {

    public static final String URL_AUTO_PAYMENT = "/auth/autopayments";
    public static final String PARAM_CLIENT_ID = "clientId";
    public static final String PARAM_TRANSFER_ID = "transferId";

    private final PaymentService paymentService;

    @GetMapping
    public ResponseEntity<List<AutoPaymentsDto>> viewAutoPayments(@RequestParam UUID clientId) {
        List<AutoPaymentsDto> autoPaymentsDtoList = paymentService.viewAutoPayments(clientId);
        return ResponseEntity.ok(autoPaymentsDtoList);
    }

    @PatchMapping
    public ResponseEntity<AutoPaymentDto> updateAutoPayment(@RequestParam UUID transferId,
                                                            @RequestBody AutoPaymentDto autoPaymentDto) {
        AutoPaymentDto result = paymentService.addAutoPayment(autoPaymentDto, transferId);
        return ResponseEntity.ok(result);
    }
}
