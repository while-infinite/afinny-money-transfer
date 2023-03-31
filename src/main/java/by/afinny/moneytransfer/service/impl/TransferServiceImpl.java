package by.afinny.moneytransfer.service.impl;

import by.afinny.moneytransfer.dao.TransferOrderDao;
import by.afinny.moneytransfer.dto.CardNumberDto;
import by.afinny.moneytransfer.dto.ChangeStatusResponseDto;
import by.afinny.moneytransfer.dto.CreatePaymentDepositDto;
import by.afinny.moneytransfer.dto.CreatePaymentDto;
import by.afinny.moneytransfer.dto.CreatePaymentResponseDto;
import by.afinny.moneytransfer.dto.CreditCardStatementDto;
import by.afinny.moneytransfer.dto.DebitCardStatementDto;
import by.afinny.moneytransfer.dto.DetailsHistoryDto;
import by.afinny.moneytransfer.dto.FilterOptionsDto;
import by.afinny.moneytransfer.dto.IsFavoriteTransferDto;
import by.afinny.moneytransfer.dto.RequestRefillBrokerageAccountDto;
import by.afinny.moneytransfer.dto.ResponseBrokerageAccountDto;
import by.afinny.moneytransfer.dto.ResponseClientDataDto;
import by.afinny.moneytransfer.dto.TransferDto;
import by.afinny.moneytransfer.dto.TransferOrderHistoryDto;
import by.afinny.moneytransfer.dto.kafka.ConsumerTransferOrderEvent;
import by.afinny.moneytransfer.dto.kafka.ConsumerСreatePaymentEvent;
import by.afinny.moneytransfer.dto.kafka.ProducerTransferOrderEvent;
import by.afinny.moneytransfer.dto.kafka.ProducerСreatePaymentEvent;
import by.afinny.moneytransfer.entity.Brokerage;
import by.afinny.moneytransfer.entity.Payee;
import by.afinny.moneytransfer.entity.TransferOrder;
import by.afinny.moneytransfer.entity.TransferType;
import by.afinny.moneytransfer.entity.constant.OperationType;
import by.afinny.moneytransfer.entity.constant.PayeeType;
import by.afinny.moneytransfer.entity.constant.TransferStatus;
import by.afinny.moneytransfer.entity.constant.TransferTypeName;
import by.afinny.moneytransfer.exception.TransferOrderStatusException;
import by.afinny.moneytransfer.mapper.TransferHistoryMapper;
import by.afinny.moneytransfer.mapper.TransferMapper;
import by.afinny.moneytransfer.openfeign.credit.CreditCardClient;
import by.afinny.moneytransfer.openfeign.deposit.DepositDebitCardStatementClient;
import by.afinny.moneytransfer.openfeign.investments.InvestmentsInvestmentClient;
import by.afinny.moneytransfer.openfeign.user.UserInformationClient;
import by.afinny.moneytransfer.repository.BrokerageRepository;
import by.afinny.moneytransfer.repository.PayeeRepository;
import by.afinny.moneytransfer.repository.TransferOrderRepository;
import by.afinny.moneytransfer.repository.TransferTypeRepository;
import by.afinny.moneytransfer.service.TransferService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransferServiceImpl implements TransferService {

    private final TransferOrderRepository transferOrderRepository;
    private final TransferTypeRepository transferTypeRepository;
    private final PayeeRepository payeeRepository;
    private final BrokerageRepository brokerageRepository;
    private final TransferHistoryMapper transferHistoryMapper;
    private final TransferMapper transferMapper;
    private final CreditCardClient creditCardClient;
    private final DepositDebitCardStatementClient depositDebitCardStatementClient;
    private final TransferOrderDao transferOrderDao;
    private final InvestmentsInvestmentClient investmentsInvestmentClient;
    private final UserInformationClient userInformationClient;
    private final ApplicationEventPublisher eventPublisher;
    private final DepositDebitCardStatementClient depositClient;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteIdDraftTransferOrder(UUID clientId, UUID transferId) {
        log.info("deleteIdDraftTransferOrder() method invoke with transferId: {}", transferId);
        TransferOrder foundTransferOrder = getTransferOrderByClientIdAndId(clientId, transferId);
        if (!foundTransferOrder.getTransferStatus().equals(TransferStatus.DRAFT)) {
            throw new TransferOrderStatusException("Transfer order with this " + transferId + " has a " +
                    foundTransferOrder.getTransferStatus() + ", you can delete a transfer order with the draft status");
        }
        transferOrderRepository.delete(foundTransferOrder);
    }

    private TransferOrder getTransferOrderById(UUID transferId) {
        return transferOrderRepository.findById(transferId).orElseThrow(
                () -> new EntityNotFoundException("no payment with the specified id " + transferId + " was found"));
    }

    private TransferOrder getTransferOrderByClientIdAndId(UUID clientId, UUID transferId) {
        TransferOrder transferOrder = transferOrderRepository.findByClientIdAndId(clientId, transferId);
        if (transferOrder == null) {
            throw new EntityNotFoundException("no payment with the specified id " + transferId + " was found");
        }
        return transferOrder;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public IsFavoriteTransferDto getFavoriteTransferOrder(UUID clientId, UUID transferOrderId) {
        log.info("getFavoriteTransferOrder() method invoke with transferId: {}", transferOrderId);
        TransferOrder transferOrder = transferOrderRepository.findByClientIdAndId(clientId, transferOrderId);
        if (transferOrder == null) {
            throw new EntityNotFoundException("no payment with the specified id " + transferOrderId + " was found");
        }
        transferOrder.setIsFavorite(!transferOrder.getIsFavorite());
        transferMapper.toIsFavoriteTransferDto(transferOrder);
        transferOrderRepository.save(transferOrder);
        return transferMapper.toIsFavoriteTransferDto(transferOrder);
    }

    @Override
    public TransferDto getFavoriteTransfer(UUID clientId, UUID transferOrderId) {
        log.info("getFavoriteTransfer() method invoked.");
        TransferOrder transferOrder = transferOrderRepository.findByClientIdAndId(clientId, transferOrderId);
        if (transferOrder == null) {
            throw new EntityNotFoundException("transfer with id " + transferOrderId + " wasn't found");
        }
        return transferMapper.transferOrderToTransferDto(transferOrder);
    }

    @Override
    public List<TransferOrderHistoryDto> getTransferOrderHistory(FilterOptionsDto filterOptions) {
        List<TransferOrder> transferOrders = transferOrderDao.getTransferOrderByFilterOptions(filterOptions);
        if (transferOrders.isEmpty()) {
            throw new EntityNotFoundException("transfer order not found");
        }
        return transferHistoryMapper.toTransferOrderHistoryDto(transferOrders);
    }


    @Override
    public DetailsHistoryDto getDetailsHistory(UUID clientId, UUID transferOrderId) {
        log.info("getDetailsHistory() method invoke with transferOrderId: {}", transferOrderId);
        TransferOrder transferOrder = transferOrderRepository.findByClientIdAndId(clientId, transferOrderId);
        if (transferOrder == null) {
            throw new EntityNotFoundException("transfer order id " + transferOrderId +
                    " wasn't found");

        }
        return transferMapper.toDetailsHistoryDto(transferOrder);
    }

    @Override
    public List<CreditCardStatementDto> getCreditCardStatement(UUID cardId, UUID clientId, String from, String to, Integer pageNumber, Integer pageSize) {
        log.info("getTransferOrderHistory() method invoke with cardId: {}", cardId);
        LocalDateTime convertedFrom = toDateTime(from);
        LocalDateTime convertedTo = toDateTime(to);
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by("completedAt").descending());
        String remitterCardNumber = creditCardClient.getCardNumber(cardId).getBody();
        List<TransferOrder> transferOrders = transferOrderRepository.findAllByRemitterCardNumber(remitterCardNumber, clientId, convertedFrom, convertedTo, pageable);
        if (transferOrders.isEmpty()) {
            throw new EntityNotFoundException("transfer order with remitter card number: " + remitterCardNumber + " not found");
        }
        return transferMapper.toCreditCardStatementDto(transferOrders);
    }

    @Override
    public List<DebitCardStatementDto> getViewDebitCardStatement(UUID cardId, UUID clientId, String from, String to,
                                                                 Integer pageNumber, Integer pageSize) {
        log.info("getViewDebitCardStatement() method invoke with transferId: {}", cardId);
        LocalDateTime convertedFrom = toDateTime(from);
        LocalDateTime convertedTo = toDateTime(to);
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by("completedAt").descending());
        CardNumberDto body = depositDebitCardStatementClient.getCardNumberByCardId(cardId).getBody();
        List<TransferOrder> transferOrders = transferOrderRepository.findAllByRemitterCardNumber(body.getCardNumber(), clientId, convertedFrom, convertedTo, pageable);
        if (transferOrders.isEmpty()) {
            throw new EntityNotFoundException("transfer order with remitter card number: " + body.getCardNumber() + " not found");
        }
        return transferHistoryMapper.toDebitCardStatementDto(transferOrders);
    }

    @Override
    public List<TransferTypeName> getTransferType() {
        log.info("getTransferType() method invoke");
        return List.of(TransferTypeName.BETWEEN_CARDS, TransferTypeName.TO_ANOTHER_CARD,
                TransferTypeName.BY_PHONE_NUMBER, TransferTypeName.BY_PAYEE_DETAILS);
    }

    @Override
    public List<TransferTypeName> getPaymentType() {
        log.info("getPaymentType() method invoke");
        return List.of(TransferTypeName.FAVORITES, TransferTypeName.AUTOPAYMENTS,
                TransferTypeName.BANKING_SERVICES, TransferTypeName.INFO_SERVISES,
                TransferTypeName.PAYMENT_FOR_SERVICES, TransferTypeName.UTILITIES, TransferTypeName.OTHER_PAYMENTS);

    }

    @Override
    @Transactional
    public ResponseBrokerageAccountDto refillBrokerageAccount(UUID clientId, RequestRefillBrokerageAccountDto requestRefillBrokerageAccountDto) {
        log.info("refillBrokerageAccount() method invoke");
        Brokerage brokerage = createBrokerage(requestRefillBrokerageAccountDto.getBrokerageId());
        brokerageRepository.save(brokerage);
        log.info("brokerage was saved");
        Payee payee = createPayee();
        payeeRepository.save(payee);
        log.info("payee was saved");
        TransferOrder transferOrder = createTransferOrder(brokerage, requestRefillBrokerageAccountDto, clientId, payee);
        transferOrderRepository.save(transferOrder);
        log.info("transferOrder was saved");
        ResponseClientDataDto responseClientDataDto = userInformationClient.getClientData(clientId).getBody();
        sendToKafka(transferOrder);
        return transferMapper.toResponseBrokerageAccountDto(transferOrder, responseClientDataDto);
    }

    public void sendToKafka(TransferOrder transferOrder) {
        ProducerTransferOrderEvent producerTransferOrderEvent = transferMapper.toProducerTransferOrderEvent(transferOrder);
        log.info("Publishing event...");
        eventPublisher.publishEvent(producerTransferOrderEvent);
    }

    @Override
    @Transactional
    public ChangeStatusResponseDto changeStatus(UUID transferId) {
        log.info("changeStatus() method invoke");

        TransferOrder transferOrder = transferOrderRepository.findById(transferId)
                .orElseThrow(() -> new EntityNotFoundException("transfer order with id: " + transferId + " not found"));

        transferOrder.setTransferStatus(TransferStatus.IN_PROGRESS);

        transferOrderRepository.save(transferOrder);

        ChangeStatusResponseDto changeStatusResponseDto = transferMapper.toChangeStatusResponseDto(transferOrder);

        return changeStatusResponseDto;
    }

    @Override
    @Transactional
    public void modifyTransferOrder(ConsumerTransferOrderEvent consumerTransferOrderEvent) {
        log.info("modifyTransferOrder() method invoke");
        UUID transferOrderId = consumerTransferOrderEvent.getId();
        TransferOrder transferOrder = transferOrderRepository
                .findById(transferOrderId)
                .orElseThrow(() -> new EntityNotFoundException("TransferOrder with id=" + transferOrderId + " wasn't found"));
        transferOrder.setCompletedAt(consumerTransferOrderEvent.getCreatedAt());
        transferOrder.setAuthorizationCode(consumerTransferOrderEvent.getAuthorizationCode());
        transferOrder.setTransferStatus(consumerTransferOrderEvent.getStatus());
    }


    @Override
    @Transactional
    public CreatePaymentResponseDto createPaymentOrTransfer(UUID clientId, CreatePaymentDto createPaymentDto) {
        log.info("createPaymentOrTransfer() method invoke");

        TransferOrder payment;
        Optional<TransferType> transferType = transferTypeRepository.findById(createPaymentDto.getTransferTypeId());

        if (transferType.isPresent()) {
            Payee savedPayee = payeeRepository.save(createPaymentDtoToPayee(createPaymentDto));
            payment = transferOrderRepository.saveAndFlush(createPaymentDtoToTransferOrder(clientId,
                                                                                           transferType.get(),
                                                                                           savedPayee,
                                                                                           createPaymentDto));
        } else {
            throw new EntityNotFoundException("Transfer type with ID " + transferType + " is not found");
        }

        log.info("TransferOrder " + payment.getId() + " saved");

        sendToKafkaToCreatePaymentEvent(payment);
        CreatePaymentResponseDto createPaymentResponseDto = transferMapper.toCreatePaymentResponseDto(payment);

        return createPaymentResponseDto;
    }

    public void sendToKafkaToCreatePaymentEvent(TransferOrder payment) {
        ProducerСreatePaymentEvent event = transferMapper.toСreatePaymentEvent(payment);
        log.info("Publishing event: create payment");
        eventPublisher.publishEvent(event);
    }

    @Transactional
    public void updatePaymentOrTransfer(ConsumerСreatePaymentEvent event) {
        log.info("updateBrokeragePayment() method invoke");

        if (event.getTransferStatus() == TransferStatus.REJECTED) {
            updatePaymentStatusCreatePaymentOrTransfer(event);
        } else if (event.getTransferStatus() == TransferStatus.PERFORMED) {
            CreatePaymentDepositDto createPaymentDepositDto = transferMapper.toCreatePaymentDepositDto(event);
            Boolean response = Optional.of(depositClient.writeOffSum(event.getClientId(), createPaymentDepositDto).getBody())
                    .orElse(false);

            if (response) {
                updatePaymentStatusCreatePaymentOrTransfer(event);
            } else {
                throw new RuntimeException("Bad transaction in deposit service");
            }
        } else {
            throw new RuntimeException("TransferStatus " + event.getTransferStatus() + " is unknown");
        }
    }


    private TransferOrder createTransferOrder(Brokerage brokerage,
                                              RequestRefillBrokerageAccountDto requestRefillBrokerageAccountDto,
                                              UUID clientId,
                                              Payee payee) {
        TransferType transferType = getTransferType(requestRefillBrokerageAccountDto.getTransferTypeId());
        return TransferOrder.builder()
                .createdAt(LocalDateTime.now())
                .transferType(transferType)
                .purpose("Пополнение брокерского счета")
                .remitterCardNumber(requestRefillBrokerageAccountDto.getRemitterCardNumber())
                .payee(payee)
                .sum(requestRefillBrokerageAccountDto.getSum())
                .sumCommission(new BigDecimal("0.0000"))
                .transferStatus(TransferStatus.IN_PROGRESS)
                .isFavorite(false)
                .clientId(clientId)
                .operationType(OperationType.EXPENSE)
                .brokerage(brokerage)
                .build();
    }

    private Payee createPayee() {
        return Payee.builder()
                .payeeType(PayeeType.INDIVIDUALS)
                .bic("Some BIC")                                    //Узнать у аналитиков БИК текущего банка
                .payeeAccountNumber("номер счета пользователя")     //Узнать у аналитиков, где получить номер счета пользователя
                .build();
    }

    private Brokerage createBrokerage(UUID brokerageId) {
        String brokerageAccountName = investmentsInvestmentClient.getBrokerageAccountName(brokerageId).getBody();
        return Brokerage.builder()
                .id(brokerageId)
                .brokerageAccountName(brokerageAccountName)
                .build();
    }

    private TransferType getTransferType(Integer transferTypeId) {
        return transferTypeRepository
                .findById(transferTypeId)
                .orElseThrow(() -> new EntityNotFoundException("transferType with id=" + transferTypeId + " not found"));
    }

    private Payee createPaymentDtoToPayee(CreatePaymentDto source) {
        return Payee.builder()
                .payeeType(PayeeType.INDIVIDUALS)
                .name(Optional.of(source.getName()).orElse(""))
                .inn(Optional.of(source.getInn()).orElse(""))
                .bic(source.getBic())
                .payeeAccountNumber(source.getPayeeAccountNumber())
                .payeeCardNumber(Optional.of(source.getPayeeCardNumber()).orElse(""))
                .build();
    }

    private TransferOrder createPaymentDtoToTransferOrder(UUID clientId,
                                                          TransferType transferType,
                                                          Payee savedPayee,
                                                          CreatePaymentDto source) {

        return TransferOrder.builder()
                .createdAt(LocalDateTime.now())
                .transferType(transferType)
                .remitterCardNumber(source.getRemitterCardNumber())
                .payee(savedPayee)
                .sum(new BigDecimal(source.getSum()))
                .sumCommission(new BigDecimal(source.getSumCommission()))
                .transferStatus(TransferStatus.IN_PROGRESS)
                .clientId(clientId)
                .operationType(OperationType.EXPENSE)
                .currencyExchange(BigDecimal.valueOf(1.0))
                .purpose(Optional.of(source.getPurpose()).orElse(""))
                .isFavorite(false)
                .build();
    }

    private void updatePaymentStatusCreatePaymentOrTransfer(ConsumerСreatePaymentEvent event) {
        TransferOrder payment = getTransferOrderById(event.getTransferOrderId());
        payment.setTransferStatus(event.getTransferStatus());
        payment.setCompletedAt(event.getCompletedAt());
        payment.setAuthorizationCode(event.getAuthorizationCode());
        transferOrderRepository.saveAndFlush(payment);
        log.info("TransferOrder " + payment.getId() + " updated");
    }

    private LocalDateTime toDateTime(String dateTime){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return LocalDateTime.parse(dateTime, formatter);
    }
}