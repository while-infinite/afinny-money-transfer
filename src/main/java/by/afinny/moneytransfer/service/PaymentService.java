package by.afinny.moneytransfer.service;

import by.afinny.moneytransfer.dto.AutoPaymentDto;
import by.afinny.moneytransfer.dto.AutoPaymentsDto;

import java.util.List;
import java.util.UUID;

public interface PaymentService {

    AutoPaymentDto addAutoPayment(AutoPaymentDto autoPaymentDto, UUID transferId);

    List<AutoPaymentsDto> viewAutoPayments(UUID clientId);
}
