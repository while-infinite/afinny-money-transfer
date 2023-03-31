package by.afinny.moneytransfer.service.impl;

import by.afinny.moneytransfer.dto.AutoPaymentDto;
import by.afinny.moneytransfer.dto.AutoPaymentsDto;
import by.afinny.moneytransfer.entity.TransferOrder;
import by.afinny.moneytransfer.entity.constant.TransferPeriodicity;
import by.afinny.moneytransfer.mapper.TransferMapper;
import by.afinny.moneytransfer.repository.TransferOrderRepository;
import by.afinny.moneytransfer.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final TransferOrderRepository transferOrderRepository;
    private final TransferMapper transferOrderMapper;

    @Override
    @Transactional
    public AutoPaymentDto addAutoPayment(AutoPaymentDto autoPaymentDto, UUID transferId) {
        log.info("addAutoPayment() method invoked");

        TransferOrder transferOrder = getTransferOrder(transferId);
        LocalDateTime startDate = getStartDate(autoPaymentDto, transferOrder);
        TransferPeriodicity periodicity = getPeriodicity(autoPaymentDto, transferOrder);
        transferOrder.setStartDate(startDate);
        transferOrder.setTransferPeriodicity(periodicity);
        transferOrderRepository.save(transferOrder);
        return buildAutoPayment(startDate, periodicity);
    }

    @Override
    public List<AutoPaymentsDto> viewAutoPayments(UUID clientId) {
        log.info("viewAutoPayments() method invoked");
        List<TransferOrder> transferOrders = getTransferOrders(clientId);
        return transferOrderMapper.toAutoPaymentDtoList(transferOrders);
    }

    private List<TransferOrder> getTransferOrders(UUID clientId) {
        List<TransferOrder> transferOrders =
                transferOrderRepository.findByClientIdAndStartDateNotNullAndTransferPeriodicityNotNull(clientId);
        if (transferOrders.isEmpty()) {
            throw new EntityNotFoundException("there are no auto payments for this clientId: " + clientId);
        }
        return transferOrders;
    }

    private AutoPaymentDto buildAutoPayment(LocalDateTime startDate, TransferPeriodicity periodicity) {
        return AutoPaymentDto.builder()
                .startDate(startDate)
                .periodicity(periodicity).build();
    }

    private TransferPeriodicity getPeriodicity(AutoPaymentDto autoPaymentDto, TransferOrder transferOrder) {
        return transferOrder.getTransferPeriodicity() == null ? autoPaymentDto.getPeriodicity() : null;
    }

    private LocalDateTime getStartDate(AutoPaymentDto autoPaymentDto, TransferOrder transferOrder) {
        return transferOrder.getStartDate() == null ? autoPaymentDto.getStartDate() : null;
    }

    private TransferOrder getTransferOrder(UUID transferId) {
        return transferOrderRepository.findById(transferId)
                .orElseThrow(() -> new EntityNotFoundException("transfer order with id: " + transferId + " not found"));
    }
}
