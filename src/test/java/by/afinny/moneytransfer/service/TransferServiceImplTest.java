package by.afinny.moneytransfer.service;

import by.afinny.moneytransfer.dao.TransferOrderDao;
import by.afinny.moneytransfer.dto.CardNumberDto;
import by.afinny.moneytransfer.dto.ChangeStatusRequestDto;
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
import by.afinny.moneytransfer.dto.ResponseClientDataDto;
import by.afinny.moneytransfer.dto.TransferDto;
import by.afinny.moneytransfer.dto.TransferOrderHistoryDto;
import by.afinny.moneytransfer.dto.kafka.ProducerTransferOrderEvent;
import by.afinny.moneytransfer.dto.kafka.ProducerСreatePaymentEvent;
import by.afinny.moneytransfer.entity.Brokerage;
import by.afinny.moneytransfer.entity.Payee;
import by.afinny.moneytransfer.entity.TransferOrder;
import by.afinny.moneytransfer.entity.TransferType;
import by.afinny.moneytransfer.entity.constant.CurrencyCode;
import by.afinny.moneytransfer.entity.constant.OperationType;
import by.afinny.moneytransfer.entity.constant.PayeeType;
import by.afinny.moneytransfer.entity.constant.TransferPeriodicity;
import by.afinny.moneytransfer.entity.constant.TransferStatus;
import by.afinny.moneytransfer.entity.constant.TransferTypeName;
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
import by.afinny.moneytransfer.service.impl.TransferServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.ThrowableAssert;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import javax.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static by.afinny.moneytransfer.entity.constant.TransferStatus.DRAFT;
import static by.afinny.moneytransfer.entity.constant.TransferStatus.IN_PROGRESS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@ActiveProfiles("test")
public class TransferServiceImplTest {

    @InjectMocks
    private TransferServiceImpl transferService;

    @Mock
    private TransferOrderRepository transferOrderRepository;
    @Mock
    private TransferOrderDao transferOrderDao;
    @Spy
    private TransferMapper transferMapper;
    @Spy
    private CreditCardClient creditCardClient;
    @Spy
    DepositDebitCardStatementClient depositDebitCardStatementClient;
    @Mock
    private TransferHistoryMapper transferHistoryMapper;
    @Mock
    private IsFavoriteTransferDto isFavoriteTransferDto;
    @Mock
    private InvestmentsInvestmentClient investmentsInvestmentClient;
    @Mock
    private BrokerageRepository brokerageRepository;
    @Mock
    private PayeeRepository payeeRepository;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private TransferTypeRepository transferTypeRepository;
    @Mock
    private UserInformationClient userInformationClient;


    private TransferOrder transferOrder;
    private TransferOrder transferOrderDetail;
    private TransferOrder transferFavoriteOrder;
    private TransferType transferType;
    private Payee payee;
    private TransferStatus transferStatus;
    private TransferPeriodicity transferPeriodicity;
    private TransferDto transferDto;
    private CardNumberDto cardNumberDto;
    private FilterOptionsDto filterOptionsDto;
    private List<TransferOrderHistoryDto> transferOrderHistoryDto = new ArrayList<>();
    private List<TransferTypeName> typeNames;
    private List<TransferTypeName> typeNameList;
    private List<DebitCardStatementDto> debitCardStatementDto = new ArrayList<>();
    private List<TransferOrder> transferOrderList = new ArrayList<>();
    private List<TransferTypeName> paymentTypeNameList;
    private ResponseBrokerageAccountDto responseBrokerageAccountDtoExpected;
    private RequestRefillBrokerageAccountDto requestRefillBrokerageAccountDto;
    private Brokerage brokerage2;
    private Payee payee2;
    private Payee payee3;
    private TransferType transferType2;
    private TransferType transferType3;
    private TransferOrder transferOrder2;
    private TransferOrder transferOrder3;
    private ResponseClientDataDto responseClientDataDto;
    private List<CreditCardStatementDto> creditCardStatementDtoListExpect;
    private final String FORMAT_DATE = "yyyy-MM-dd";
    private final UUID OPERATION_ID = UUID.randomUUID();
    private final UUID CREDIT_ID = UUID.randomUUID();
    private final UUID CLIENT_ID = UUID.randomUUID();
    private final String REMITTER_CARD_NUMBER = "1111111";
    private final String PAYEE_NAME = "XVZ";
    private final String FROM = "2023-03-27 00:00:00";
    private final String TO = "2023-03-28 00:00:00";
    private final Integer pageNumber = 1;
    private final Integer pageSize = 5;
    private final Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by("createdAt").descending());
    private final Pageable pageableCompletedAt = PageRequest.of(pageNumber, pageSize, Sort.by("completedAt").descending());
    private DetailsHistoryDto detailsHistoryDto;
    private CreatePaymentDto createPaymentDto;
    private CreatePaymentResponseDto createPaymentResponseDto;
    private ChangeStatusResponseDto changeStatusResponseDto;
    private ChangeStatusRequestDto changeStatusRequestDto;


    @BeforeEach
    void setUp() {
        transferType = TransferType.builder()
                .id(1)
                .build();

        payee = Payee.builder()
                .id(UUID.fromString("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12"))
                .build();

        transferStatus = TransferStatus.PERFORMED;

        transferPeriodicity = TransferPeriodicity.MONTHLY;

        transferDto = TransferDto.builder()
                .transferTypeId(1)
                .purpose("food")
                .payeeId(UUID.fromString("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12"))
                .sum(new BigDecimal("123.5"))
                .isFavorite(true)
                .build();

        transferFavoriteOrder = TransferOrder.builder()
                .id(OPERATION_ID)
                .createdAt(LocalDate.of(2022, 6, 22).atStartOfDay())
                .transferType(transferType)
                .purpose("food")
                .remitterCardNumber("")
                .payee(payee)
                .sum(new BigDecimal("123.5"))
                .sumCommission(new BigDecimal("10.4"))
                .completedAt(LocalDate.of(2022, 6, 22).atStartOfDay())
                .transferStatus(transferStatus)
                .authorizationCode("123")
                .currencyExchange(new BigDecimal("1.2"))
                .isFavorite(true)
                .startDate(LocalDate.of(2022, 6, 22).atStartOfDay())
                .transferPeriodicity(transferPeriodicity)
                .build();

        transferOrder = TransferOrder.builder()
                .id(OPERATION_ID)
                .isFavorite(true)
                .clientId(OPERATION_ID)
                .completedAt(LocalDateTime.now())
                .sum(new BigDecimal("5.0"))
                .payee(Payee.builder()
                        .id(UUID.fromString("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11"))
                        .build())
                .transferStatus(TransferStatus.DRAFT)
                .transferType(TransferType.builder()
                        .transferTypeName(TransferTypeName.BETWEEN_CARDS)
                        .currencyCode(CurrencyCode.RUB)
                        .build())
                .purpose("XVZ")
                .createdAt(LocalDateTime.now())
                .purpose("purpose")
                .sum(BigDecimal.TEN)
                .completedAt(LocalDateTime.now())
                .build();

        isFavoriteTransferDto = IsFavoriteTransferDto.builder()
                .isFavorite(true)
                .build();

        transferOrderList.add(transferOrder);

        transferOrderHistoryDto.add(TransferOrderHistoryDto.builder()
                .transferOrderId(OPERATION_ID)
                .completedAt(LocalDateTime.now())
                .sum(new BigDecimal("5.0"))
                .payeeId(UUID.fromString("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11"))
                .transferStatus(TransferStatus.DRAFT)
                .transferTypeName(TransferTypeName.BETWEEN_CARDS)
                .currencyCode(CurrencyCode.RUB)
                .name(PAYEE_NAME)
                .purpose("XVZ")
                .remitterCardNumber(REMITTER_CARD_NUMBER)
                .createdAt(LocalDateTime.now())
                .build());

        transferOrderDetail = TransferOrder.builder()
                .createdAt(LocalDateTime.now())
                .remitterCardNumber("1234567898765432")
                .sumCommission(BigDecimal.ONE)
                .currencyExchange(BigDecimal.ONE)
                .payee(Payee.builder()
                        .payeeType(PayeeType.INDIVIDUALS)
                        .name("RUB")
                        .inn("123456789009")
                        .bic("123456789")
                        .payeeAccountNumber("a0eebc999")
                        .payeeCardNumber("9876543210")
                        .build())
                .build();

        cardNumberDto = CardNumberDto.builder()
                .cardNumber("88888")
                .build();

        typeNames = List.of(TransferTypeName.BETWEEN_CARDS,
                TransferTypeName.TO_ANOTHER_CARD,
                TransferTypeName.BY_PHONE_NUMBER,
                TransferTypeName.BY_PAYEE_DETAILS);

        paymentTypeNameList = List.of(TransferTypeName.FAVORITES,
                TransferTypeName.AUTOPAYMENTS,
                TransferTypeName.BANKING_SERVICES,
                TransferTypeName.INFO_SERVISES,
                TransferTypeName.PAYMENT_FOR_SERVICES,
                TransferTypeName.UTILITIES,
                TransferTypeName.OTHER_PAYMENTS);

        filterOptionsDto = FilterOptionsDto.builder().
                clientId(OPERATION_ID).
                pageNumber(pageNumber).
                pageSize(pageSize).
                build();

        detailsHistoryDto = DetailsHistoryDto.builder()
                .createdAt(transferOrderDetail.getCreatedAt())
                .remitterCardNumber(transferOrderDetail.getRemitterCardNumber())
                .sumCommission(transferOrderDetail.getSumCommission())
                .currencyExchange(transferOrderDetail.getCurrencyExchange())
                .payeeType(transferOrderDetail.getPayee().getPayeeType())
                .name(transferOrderDetail.getPayee().getName())
                .inn(transferOrderDetail.getPayee().getInn())
                .bic(transferOrderDetail.getPayee().getBic())
                .payeeAccountNumber(transferOrderDetail.getPayee().getPayeeAccountNumber())
                .payeeCardNumber(transferOrderDetail.getPayee().getPayeeCardNumber())
                .build();

        requestRefillBrokerageAccountDto = RequestRefillBrokerageAccountDto.builder()
                .brokerageId(UUID.randomUUID())
                .remitterCardNumber("123")
                .sum(new BigDecimal("100.00"))
                .transferTypeId(0)
                .build();
        brokerage2 = Brokerage.builder()
                .id(requestRefillBrokerageAccountDto.getBrokerageId())
                .brokerageAccountName("brokerageAccountName")
                .build();
        payee2 = Payee.builder()
                .payeeType(PayeeType.INDIVIDUALS)
                .bic("Some BIC")
                .payeeAccountNumber("номер счета пользователя")
                .build();
        transferType2 = TransferType.builder()
                .id(requestRefillBrokerageAccountDto.getTransferTypeId()).build();
        transferOrder2 = TransferOrder.builder()
                .createdAt(LocalDateTime.now())
                .transferType(transferType2)
                .purpose("Пополнение брокерского счета")
                .remitterCardNumber(requestRefillBrokerageAccountDto.getRemitterCardNumber())
                .payee(payee2)
                .sum(requestRefillBrokerageAccountDto.getSum())
                .sumCommission(new BigDecimal("0.0000"))
                .transferStatus(TransferStatus.IN_PROGRESS)
                .isFavorite(false)
                .clientId(CLIENT_ID)
                .operationType(OperationType.EXPENSE)
                .brokerage(brokerage2)
                .build();
        responseClientDataDto = ResponseClientDataDto.builder()
                .firstName("firstName")
                .lastName("lastName")
                .middleName("middleName")
                .build();
        responseBrokerageAccountDtoExpected = ResponseBrokerageAccountDto.builder()
                .id(transferOrder2.getId())
                .firstName(responseClientDataDto.getFirstName())
                .lastName(responseClientDataDto.getLastName())
                .middleName(responseClientDataDto.getMiddleName())
                .createdAt(transferOrder2.getCreatedAt())
                .status(transferOrder2.getTransferStatus())
                .transferTypeId(transferType2.getId())
                .sum(transferOrder2.getSum())
                .remitterCardNumber(transferOrder2.getRemitterCardNumber())
                .sumCommission(transferOrder2.getSumCommission())
                .brokerageId(brokerage2.getId())
                .isFavorite(transferOrder2.getIsFavorite())
                .purpose(transferOrder2.getPurpose())
                .authorizationCode(transferOrder2.getAuthorizationCode())
                .build();

        creditCardStatementDtoListExpect = Collections.singletonList(CreditCardStatementDto.builder().build());

        transferType3 = TransferType.builder()
                .id(1)
                .transferTypeName(TransferTypeName.BY_PHONE_NUMBER)
                .currencyCode(CurrencyCode.RUB)
                .minCommission(BigDecimal.valueOf(0.0))
                .maxCommission(BigDecimal.valueOf(10.0))
                .percentCommission(BigDecimal.valueOf(0.0))
                .minSum(BigDecimal.valueOf(10.0))
                .maxSum(BigDecimal.valueOf(1000.0))
                .build();

        payee3 = Payee.builder()
                .id(UUID.randomUUID())
                .payeeType(PayeeType.INDIVIDUALS)
                .name("name")
                .inn("inn")
                .bic("bic")
                .payeeAccountNumber(UUID.randomUUID().toString())
                .payeeCardNumber("0000111100001111")
                .build();

        transferOrder3 = TransferOrder.builder()
                .id(UUID.randomUUID())
                .createdAt(LocalDateTime.now())
                .transferType(transferType3)
                .remitterCardNumber("0000111100001111")
                .payee(payee3)
                .sum(BigDecimal.valueOf(1000.0))
                .sumCommission(BigDecimal.valueOf(0.0))
                .transferStatus(TransferStatus.IN_PROGRESS)
                .authorizationCode("1111")
                .clientId(UUID.randomUUID())
                .operationType(OperationType.EXPENSE)
                .currencyExchange(BigDecimal.valueOf(1.0))
                .purpose("purpose")
                .isFavorite(false)
                .build();

        createPaymentDto = CreatePaymentDto.builder()
                .transferTypeId(transferType3.getId())
                .sum("300.0")
                .remitterCardNumber(transferOrder3.getRemitterCardNumber())
                .name(transferOrder3.getPayee().getName())
                .payeeAccountNumber(transferOrder3.getPayee().getPayeeAccountNumber())
                .payeeCardNumber(transferOrder3.getPayee().getPayeeCardNumber())
                .inn(transferOrder3.getPayee().getInn())
                .bic(transferOrder3.getPayee().getBic())
                .sumCommission(transferOrder3.getSumCommission().toString())
                .purpose(transferOrder3.getPurpose())
                .build();

        createPaymentResponseDto = CreatePaymentResponseDto.builder()
                .id(transferOrder3.getId().toString())
                .created_at(transferOrder3.getCreatedAt().toString())
                .status(transferOrder3.getTransferStatus().name())
                .card_number(transferOrder3.getRemitterCardNumber())
                .transfer_type_id(transferOrder3.getTransferType().getId().toString())
                .sum(transferOrder3.getSum().toString())
                .remitter_card_number(transferOrder3.getRemitterCardNumber())
                .name(transferOrder3.getPayee().getName())
                .payee_account_number(transferOrder3.getPayee().getPayeeAccountNumber())
                .payee_card_number(transferOrder3.getPayee().getPayeeCardNumber())
                .sum_commission(transferOrder3.getSumCommission().toString())
                .authorizationCode(transferOrder3.getAuthorizationCode())
                .currencyExchange(transferOrder3.getCurrencyExchange().toString())
                .purpose(transferOrder3.getPurpose())
                .inn(transferOrder3.getPayee().getInn())
                .bic(transferOrder3.getPayee().getBic())
                .build();

        changeStatusResponseDto = ChangeStatusResponseDto.builder()
                .transferId(OPERATION_ID)
                .status(IN_PROGRESS)
                .build();

        changeStatusRequestDto = ChangeStatusRequestDto.builder()
                .transferId(OPERATION_ID)
                .status(IN_PROGRESS)
                .build();
    }

    @Test
    @DisplayName("If draft transfer successfully delete then return No Content")
    void deleteDraftTransferOrder_shouldReturnNoContent() {
        //ARRANGE
        when(transferOrderRepository.findByClientIdAndId(OPERATION_ID, OPERATION_ID)).thenReturn(transferOrder);

        //ACT
        transferService.deleteIdDraftTransferOrder(OPERATION_ID, OPERATION_ID);

        //VERIFY
        verify(transferOrderRepository, times(1)).delete(transferOrder);
    }

    @Test
    @DisplayName("If not success delete then throw Runtime Exception")
    void deleteDraftTransferOrder_ifNotSuccess_thenThrow() {
        //ARRANGE
        when(transferOrderRepository.findByClientIdAndId(OPERATION_ID, OPERATION_ID)).thenThrow(EntityNotFoundException.class);

        //ACT
        ThrowingCallable deleteDraftTransferOrderMethodInvocation = () ->
                transferService.deleteIdDraftTransferOrder(OPERATION_ID, OPERATION_ID);

        //VERIFY
        assertThatThrownBy(deleteDraftTransferOrderMethodInvocation).isInstanceOf(EntityNotFoundException.class);
        verify(transferOrderRepository, never()).delete(transferOrder);
    }

    @Test
    @DisplayName("If received dto successfully then return true or false")
    void getFavoriteTransferOrder_shouldReturnTrueOrFalse() {
        //ARRANGE
        when(transferOrderRepository.findByClientIdAndId(CLIENT_ID, OPERATION_ID))
                .thenReturn(transferOrder);
        when(transferMapper.toIsFavoriteTransferDto(transferOrder))
                .thenReturn(isFavoriteTransferDto);

        //ACT
        IsFavoriteTransferDto result = transferService.getFavoriteTransferOrder(CLIENT_ID, OPERATION_ID);

        //VERIFY
        assertThat(result).isEqualTo(isFavoriteTransferDto);
    }

    @Test
    @DisplayName("If not success then throw Runtime Exception")
    void getFavoriteTransferOrder_ifNotSuccess_thenThrow() {
        //ARRANGE
        when(transferOrderRepository.findByClientIdAndId(CLIENT_ID, OPERATION_ID))
                .thenThrow(EntityNotFoundException.class);

        //ACT
        ThrowableAssert.ThrowingCallable getFavoriteTransferOrderMethodInvocation = () ->
                transferService.getFavoriteTransferOrder(CLIENT_ID, OPERATION_ID);

        //VERIFY
        Assertions.assertThatThrownBy(getFavoriteTransferOrderMethodInvocation)
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("if client was found then return transfer order")
    void getTransferOrderHistory_shouldReturnCommissionDto() throws JsonProcessingException {
        //ARRANGE
        when(transferOrderDao.getTransferOrderByFilterOptions(filterOptionsDto)).thenReturn(transferOrderList);
        when(transferHistoryMapper.toTransferOrderHistoryDto(transferOrderList)).thenReturn(transferOrderHistoryDto);

        //ACT
        List<TransferOrderHistoryDto> transferOrderHistory =
                transferHistoryMapper.toTransferOrderHistoryDto(transferOrderDao
                        .getTransferOrderByFilterOptions(filterOptionsDto));

        //VERIFY
        verifyBody(asJsonString(transferOrderHistory), asJsonString(transferOrderHistoryDto));
    }

    @Test
    @DisplayName("if client wasn't found then throws EntityNotFoundException")
    void getTransferOrderHistory_ifNotSuccess_thenThrow() {
        //ARRANGE
        when(transferOrderDao.getTransferOrderByFilterOptions(filterOptionsDto))
                .thenReturn(List.of());

        //ACT
        ThrowingCallable getTransferOrderHistory = () -> transferService
                .getTransferOrderHistory(filterOptionsDto);

        //VERIFY
        Assertions.assertThatThrownBy(getTransferOrderHistory).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("Return details history for specified the transfer order when id was found.")
    void getDetailsHistory_ifDetailsHistoryFound_thenReturnDto() {
        //ARRANGE
        when(transferOrderRepository.findByClientIdAndId(OPERATION_ID, OPERATION_ID)).thenReturn(transferOrderDetail);
        when(transferMapper.toDetailsHistoryDto(transferOrderDetail)).thenReturn(detailsHistoryDto);

        //ACT
        DetailsHistoryDto result = transferService.getDetailsHistory(OPERATION_ID, OPERATION_ID);

        //VERIFY
        assertThat(result).isNotNull();
        assertThat(result).usingRecursiveComparison().isEqualTo(detailsHistoryDto);
    }

    @Test
    @DisplayName("If no history of translation order details is found")
    void getDetailsHistory_ifDetailsHistoryNotFound_thenThrow() {
        //ARRANGE
        when(transferOrderRepository.findByClientIdAndId(OPERATION_ID, OPERATION_ID)).thenThrow(EntityNotFoundException.class);

        //ACT
        ThrowingCallable getDetailsHistoryMethodInvocation = () -> transferService.getDetailsHistory(OPERATION_ID, OPERATION_ID);

        //VERIFY
        Assertions.assertThatThrownBy(getDetailsHistoryMethodInvocation).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("if transfer was successfully found by ID then return transfer info")
    void getFavoriteTransfer_shouldReturnTransferDto() {
        //ARRANGE
        when(transferOrderRepository.findByClientIdAndId(OPERATION_ID, OPERATION_ID)).thenReturn(transferFavoriteOrder);
        when(transferMapper.transferOrderToTransferDto(transferFavoriteOrder)).thenReturn(transferDto);

        //ACT
        TransferDto result = transferService.getFavoriteTransfer(OPERATION_ID, OPERATION_ID);

        //VERIFY
        assertThat(result).isNotNull();
        assertThat(result.toString()).isEqualTo(transferDto.toString());
    }

    @Test
    @DisplayName("if transfer with incoming transfer ID wasn't found then throws EntityNotFoundException")
    void getFavoriteTransfer_ifNotSuccess_thenThrow() {
        //ARRANGE
        when(transferOrderRepository.findByClientIdAndId(OPERATION_ID, OPERATION_ID)).thenThrow(EntityNotFoundException.class);

        //ACT
        ThrowingCallable getFavoriteTransferMethodInvocation = () -> transferService.getFavoriteTransfer(transferOrder.getClientId(), transferOrder.getId());

        //VERIFY
        Assertions.assertThatThrownBy(getFavoriteTransferMethodInvocation).isInstanceOf(EntityNotFoundException.class);
    }

    private void verifyDetailsHistory(TransferOrder transferOrderDetail, DetailsHistoryDto result) {
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(transferOrderDetail.getCreatedAt())
                    .withFailMessage("CreatedAt should be equals")
                    .isEqualTo(result.getCreatedAt());
            softAssertions.assertThat(transferOrderDetail.getRemitterCardNumber())
                    .withFailMessage("RemitterCardNumber should be equals")
                    .isEqualTo(result.getRemitterCardNumber());
            softAssertions.assertThat(transferOrderDetail.getSumCommission())
                    .withFailMessage("SumCommission should be equals")
                    .isEqualTo(result.getSumCommission());
            softAssertions.assertThat(transferOrderDetail.getCurrencyExchange())
                    .withFailMessage("CurrencyExchange should be equals")
                    .isEqualTo(result.getCurrencyExchange());
            softAssertions.assertThat(transferOrderDetail.getPayee().getPayeeType())
                    .withFailMessage("PayeeType should be equals")
                    .isEqualTo(result.getPayeeType());
            softAssertions.assertThat(transferOrderDetail.getPayee().getName())
                    .withFailMessage("Name should be equals")
                    .isEqualTo(result.getName());
            softAssertions.assertThat(transferOrderDetail.getPayee().getInn())
                    .withFailMessage("Inn should be equals")
                    .isEqualTo(result.getInn());
            softAssertions.assertThat(transferOrderDetail.getPayee().getBic())
                    .withFailMessage("Bic should be equals")
                    .isEqualTo(result.getBic());
            softAssertions.assertThat(transferOrderDetail.getPayee().getPayeeAccountNumber())
                    .withFailMessage("PayeeAccountNumber should be equals")
                    .isEqualTo(result.getPayeeAccountNumber());
            softAssertions.assertThat(transferOrderDetail.getPayee().getPayeeCardNumber())
                    .withFailMessage("PayeeCardNumber should be equals")
                    .isEqualTo(result.getPayeeCardNumber());
        });
    }

    @Test
    @DisplayName("if card with incoming card number was found then return accountNumber")
    void getCreditCardStatement_shouldReturnCreditCardStatementDto() {
        //ARRANGE
        when(creditCardClient.getCardNumber(any(UUID.class)))
                .thenReturn(ResponseEntity.ok(REMITTER_CARD_NUMBER));
        when(transferOrderRepository.findAllByRemitterCardNumber(REMITTER_CARD_NUMBER, CLIENT_ID, toDateTime(FROM),
                toDateTime(TO), pageableCompletedAt))
                .thenReturn(transferOrderList);
        when(transferMapper.toCreditCardStatementDto(transferOrderList))
                .thenReturn(creditCardStatementDtoListExpect);

        //ACT
        List<CreditCardStatementDto> creditCardStatementsActual = transferService.getCreditCardStatement(CREDIT_ID, CLIENT_ID, FROM, TO, pageNumber, pageSize);

        //VERIFY
        assertThat(creditCardStatementsActual).usingRecursiveComparison().isEqualTo(creditCardStatementDtoListExpect);
    }

    @Test
    @DisplayName("if card with incoming card number wasn't found then throws EntityNotFoundException")
    void getCreditCardStatement_ifNotSuccess_thenThrow() {
        //ARRANGE
        when(creditCardClient.getCardNumber(any(UUID.class)))
                .thenReturn(ResponseEntity.ok(REMITTER_CARD_NUMBER));
        when(transferOrderRepository.findAllByRemitterCardNumber(REMITTER_CARD_NUMBER, CLIENT_ID, toDateTime(FROM),
                toDateTime(TO), pageableCompletedAt))
                .thenReturn(List.of());

        //ACT
        ThrowingCallable getCreditCardStatement = () -> transferService
                .getCreditCardStatement(CREDIT_ID, CLIENT_ID, FROM, TO, pageNumber, pageSize);

        //VERIFY
        Assertions.assertThatThrownBy(getCreditCardStatement).isInstanceOf(EntityNotFoundException.class);
    }

    private void verifyCreditCardStatement(List<CreditCardStatementDto> creditCardStatements) {
        CreditCardStatementDto creditCardStatementDto = creditCardStatements.get(0);
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(creditCardStatementDto.getTransferOrderId())
                    .isEqualTo(transferOrder.getId());
            softAssertions.assertThat(creditCardStatementDto.getPurpose())
                    .isEqualTo(transferOrder.getPurpose());
            softAssertions.assertThat(creditCardStatementDto.getPayeeId())
                    .isEqualTo(transferOrder.getPayee().getId());
            softAssertions.assertThat(creditCardStatementDto.getSum())
                    .isEqualTo(transferOrder.getSum());
            softAssertions.assertThat(creditCardStatementDto.getCompletedAt())
                    .isEqualTo(transferOrder.getCompletedAt());
            softAssertions.assertThat(creditCardStatementDto.getTypeName())
                    .isEqualTo(transferOrder.getTransferType().getTransferTypeName());
            softAssertions.assertThat(creditCardStatementDto.getCurrencyCode())
                    .isEqualTo(transferOrder.getTransferType().getCurrencyCode());
        });
    }

    @Test
    @DisplayName("if debit card was found then return statement")
    void getViewDebitCardStatement_shouldReturnCommissionDto() throws JsonProcessingException {
        //ARRANGE
        when(depositDebitCardStatementClient.getCardNumberByCardId(any(UUID.class)))
                .thenReturn(ResponseEntity.ok(cardNumberDto));
        when(transferOrderRepository.findAllByRemitterCardNumber(cardNumberDto.getCardNumber(), CLIENT_ID, toDateTime(FROM),
                toDateTime(TO), pageableCompletedAt))
                .thenReturn(transferOrderList);
        when(transferHistoryMapper.toDebitCardStatementDto(transferOrderList)).thenReturn(debitCardStatementDto);

        //ACT
        List<DebitCardStatementDto> debitCardStatement = transferService
                .getViewDebitCardStatement(OPERATION_ID, CLIENT_ID, FROM, TO, pageNumber, pageSize);

        //VERIFY
        verifyBody(asJsonString(debitCardStatement), asJsonString(debitCardStatementDto));
    }

    @Test
    @DisplayName("if debit card found then throws EntityNotFoundException")
    void getViewDebitCardStatement_ifNotSuccess_thenThrow() {
        //ARRANGE
        when(depositDebitCardStatementClient.getCardNumberByCardId(any(UUID.class)))
                .thenReturn(ResponseEntity.ok(cardNumberDto));
        when(transferOrderRepository.findAllByRemitterCardNumber(cardNumberDto.getCardNumber(), CLIENT_ID, toDateTime(FROM),
                toDateTime(TO), pageableCompletedAt))
                .thenReturn(List.of());

        //ACT
        ThrowingCallable getViewDebitCardStatement = () -> transferService
                .getViewDebitCardStatement(OPERATION_ID, CLIENT_ID, FROM, TO, pageNumber, pageSize);

        //VERIFY
        Assertions.assertThatThrownBy(getViewDebitCardStatement).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("if transfer type was found then return transfer type names")
    void getTransferType_shouldReturnListOfTransferType() {
        //ACT
        typeNameList = transferService.getTransferType();

        //VERIFY
        verifyTransferType(typeNameList);
    }

    private void verifyTransferType(List<TransferTypeName> typeNameList) {
        assertSoftly(softAssertions -> softAssertions.assertThat(typeNameList)
                .isEqualTo(typeNames));
    }

    @Test
    @DisplayName("if payment type was found then return list of payment type name")
    void getPaymentType_shouldReturnListOfTransferType() {
        //ACT
        typeNameList = transferService.getPaymentType();

        //VERIFY
        verifyPaymentType(typeNameList);
    }

    @Test
    @DisplayName("if method was successfully then return ResponseBrokerageAccountDto")
    void refillBrokerageAccount_ResponseBrokerageAccountDto() {
        //ARRANGE
        when(investmentsInvestmentClient.getBrokerageAccountName(brokerage2.getId()))
                .thenReturn(ResponseEntity.ok(brokerage2.getBrokerageAccountName()));
        when(transferTypeRepository.findById(transferType2.getId()))
                .thenReturn(Optional.of(transferType2));
        when(userInformationClient.getClientData(CLIENT_ID))
                .thenReturn(ResponseEntity.ok(responseClientDataDto));
        when(transferMapper.toProducerTransferOrderEvent(any(TransferOrder.class)))
                .thenReturn(new ProducerTransferOrderEvent());
        when(transferMapper.toResponseBrokerageAccountDto(any(TransferOrder.class), any(ResponseClientDataDto.class)))
                .thenReturn(responseBrokerageAccountDtoExpected);

        //ACT
        ResponseBrokerageAccountDto responseBrokerageAccountDtoActual = transferService
                .refillBrokerageAccount(CLIENT_ID, requestRefillBrokerageAccountDto);

        //VERIFY
        verify(brokerageRepository).save(any(Brokerage.class));
        verify(payeeRepository).save(any(Payee.class));
        verify(transferOrderRepository).save(any(TransferOrder.class));
        verify(eventPublisher).publishEvent(any(ProducerTransferOrderEvent.class));
        assertThat(responseBrokerageAccountDtoActual).usingRecursiveComparison().isEqualTo(responseBrokerageAccountDtoExpected);
    }

    @Test
    @DisplayName("if  method wasn't successfully then throws EntityNotFoundException")
    void refillBrokerageAccount_ifNotSuccess_thenThrow() {
        //ARRANGE
        when(investmentsInvestmentClient.getBrokerageAccountName(brokerage2.getId()))
                .thenThrow(new EntityNotFoundException());

        //ACT
        ThrowingCallable getResponseBrokerageAccountDtoActual = () -> transferService
                .refillBrokerageAccount(CLIENT_ID, requestRefillBrokerageAccountDto);

        //VERIFY
        Assertions.assertThatThrownBy(getResponseBrokerageAccountDtoActual).isInstanceOf(EntityNotFoundException.class);
    }


    @Test
    @DisplayName("if createPaymentOrTransfer method was successfully then return CreatePaymentOrTransferResponseDto")
    void createPaymentOrTransfer_CreatePaymentOrTransferResponseDto() {
        //ARRANGE
        when(transferTypeRepository.findById(any(Integer.class)))
                .thenReturn(Optional.of(transferType3));
        when(payeeRepository.save(any(Payee.class)))
                .thenReturn(payee3);
        when(transferOrderRepository.saveAndFlush(any(TransferOrder.class)))
                .thenReturn(transferOrder3);
        when(transferMapper.toСreatePaymentEvent(any(TransferOrder.class)))
                .thenReturn(new ProducerСreatePaymentEvent());
        when(transferMapper.toCreatePaymentResponseDto(transferOrder3))
                .thenReturn(createPaymentResponseDto);

        //ACT
        CreatePaymentResponseDto createPaymentResponseDtoActual = transferService
                .createPaymentOrTransfer(CLIENT_ID, createPaymentDto);

        //VERIFY
        verify(payeeRepository).save(any(Payee.class));
        verify(transferOrderRepository).saveAndFlush(any(TransferOrder.class));
        verify(eventPublisher).publishEvent(any(ProducerСreatePaymentEvent.class));
        assertThat(createPaymentResponseDtoActual).usingRecursiveComparison().isEqualTo(createPaymentResponseDto);
    }

    @Test
    @DisplayName("if createPaymentOrTransfer method wasn't successfully then throws EntityNotFoundException")
    void createPaymentOrTransfer_ifNotSuccess_thenThrow() {
        //ARRANGE
        when(transferTypeRepository.findById(any(Integer.class)))
                .thenThrow(new EntityNotFoundException());

        //ACT
        ThrowingCallable createPaymentResponseDtoActual = () -> transferService
                .createPaymentOrTransfer(CLIENT_ID, createPaymentDto);

        //VERIFY
        Assertions.assertThatThrownBy(createPaymentResponseDtoActual).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("If change status transfer successfully  then return ok")
    void changeStatus_shouldReturnOk() {
        //ARRANGE

        when(transferOrderRepository.findById(OPERATION_ID))
                .thenReturn(Optional.ofNullable(transferOrder));
        when(transferOrderRepository.save(transferOrder)).thenReturn(transferOrder);

        when(transferMapper.toChangeStatusResponseDto(transferOrder)).thenReturn(changeStatusResponseDto);

        //ACT

        transferOrder.setTransferStatus(DRAFT);

        ChangeStatusResponseDto changeStatusResponseDtoActual =
                transferService.changeStatus(OPERATION_ID);

        //VERIFY
        verifyBody(changeStatusResponseDtoActual.toString(), String.valueOf(changeStatusResponseDto));

    }


    private void verifyPaymentType(List<TransferTypeName> typeNameList) {
        assertSoftly(softAssertions -> softAssertions.assertThat(typeNameList)
                .isEqualTo(paymentTypeNameList));
    }

    private void verifyBody(String expectedBody, String actualBody) {
        assertThat(actualBody).isEqualTo(expectedBody);
    }

    private String asJsonString(Object obj) throws JsonProcessingException {
        return new ObjectMapper()
                .setDateFormat(new SimpleDateFormat(FORMAT_DATE))
                .registerModule(new JavaTimeModule()).writeValueAsString(obj);
    }

    private LocalDateTime toDateTime(String dateTime){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return LocalDateTime.parse(dateTime, formatter);
    }
}