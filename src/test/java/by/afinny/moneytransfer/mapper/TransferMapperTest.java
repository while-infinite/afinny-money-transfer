package by.afinny.moneytransfer.mapper;

import by.afinny.moneytransfer.dto.AutoPaymentsDto;
import by.afinny.moneytransfer.dto.ChangeStatusResponseDto;
import by.afinny.moneytransfer.dto.CreatePaymentResponseDto;
import by.afinny.moneytransfer.dto.CreditCardStatementDto;
import by.afinny.moneytransfer.dto.IsFavoriteTransferDto;
import by.afinny.moneytransfer.dto.ResponseBrokerageAccountDto;
import by.afinny.moneytransfer.dto.ResponseClientDataDto;
import by.afinny.moneytransfer.dto.TransferDto;
import by.afinny.moneytransfer.dto.kafka.ProducerTransferOrderEvent;
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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static by.afinny.moneytransfer.entity.constant.TransferStatus.IN_PROGRESS;
import static by.afinny.moneytransfer.entity.constant.TransferStatus.PERFORMED;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
@DisplayName("Verification of correct data generation. It will pass if the fields of the entity and dto are equal")
class TransferMapperTest {

    @InjectMocks
    private final TransferMapper transferMapper = new TransferMapperImpl();

    private TransferOrder autoPayment;
    private List<TransferOrder> transferOrderList = new ArrayList<>();
    private TransferDto transferDto;
    private TransferOrder transferOrder;
    private TransferOrder transferOrder2;
    private TransferType transferType;
    private TransferType transferType2;
    private Payee payee;
    private Payee payee2;
    private TransferStatus transferStatus;
    private TransferPeriodicity transferPeriodicity;
    private ResponseClientDataDto responseClientDataDto;
    private ResponseBrokerageAccountDto responseBrokerageAccountDtoExpected;
    private ProducerTransferOrderEvent producerTransferOrderEventExpected;
    private CreatePaymentResponseDto createPaymentResponseDtoExpexted;
    private ChangeStatusResponseDto changeStatusResponseDtoExpected;

    private final UUID TRANSFER_ORDER_ID = UUID.randomUUID();

    @BeforeAll
    void setUp() {
        autoPayment = TransferOrder.builder()
                .id(TRANSFER_ORDER_ID)
                .transferType(
                        TransferType.builder()
                                .transferTypeName(TransferTypeName.BY_PAYEE_DETAILS).build()
                ).build();

        transferOrderList.add(autoPayment);

        transferType = TransferType.builder()
                .id(1)
                .transferTypeName(TransferTypeName.BY_PAYEE_DETAILS)
                .currencyCode(CurrencyCode.RUB)
                .build();

        payee = Payee.builder()
                .id(UUID.fromString("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12"))
                .build();

        transferStatus = TransferStatus.PERFORMED;

        transferPeriodicity = TransferPeriodicity.MONTHLY;

        transferOrder = TransferOrder.builder()
                .id(UUID.fromString("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11"))
                .createdAt(LocalDate.of(2022, 6, 22).atStartOfDay())
                .transferType(transferType)
                .purpose("food")
                .remitterCardNumber("123456789")
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
                .brokerage(Brokerage.builder().id(UUID.randomUUID()).build())
                .build();

        transferOrderList.add(transferOrder);

        responseClientDataDto = ResponseClientDataDto.builder()
                .firstName("firstName")
                .lastName("lastName")
                .middleName("middleName")
                .build();

        responseBrokerageAccountDtoExpected = ResponseBrokerageAccountDto.builder()
                .id(transferOrder.getId())
                .firstName(responseClientDataDto.getFirstName())
                .lastName(responseClientDataDto.getLastName())
                .middleName(responseClientDataDto.getMiddleName())
                .createdAt(transferOrder.getCreatedAt())
                .status(transferOrder.getTransferStatus())
                .transferTypeId(transferOrder.getTransferType().getId())
                .sum(transferOrder.getSum())
                .remitterCardNumber(transferOrder.getRemitterCardNumber())
                .sumCommission(transferOrder.getSumCommission())
                .brokerageId(transferOrder.getBrokerage().getId())
                .isFavorite(transferOrder.getIsFavorite())
                .purpose(transferOrder.getPurpose())
                .authorizationCode(transferOrder.getAuthorizationCode())
                .build();

        producerTransferOrderEventExpected = ProducerTransferOrderEvent.builder()
                .id(transferOrder.getId())
                .createdAt(transferOrder.getCreatedAt())
                .transferType(transferOrder.getTransferType())
                .purpose(transferOrder.getPurpose())
                .remitterCardNumber(transferOrder.getRemitterCardNumber())
                .sum(transferOrder.getSum())
                .sumCommission(transferOrder.getSumCommission())
                .transferStatus(transferOrder.getTransferStatus())
                .brokerageId(transferOrder.getBrokerage().getId())
                .build();

        transferType2 = TransferType.builder()
                .id((int) Math.random())
                .transferTypeName(TransferTypeName.BY_PHONE_NUMBER)
                .currencyCode(CurrencyCode.RUB)
                .minCommission(BigDecimal.valueOf(0.0))
                .maxCommission(BigDecimal.valueOf(10.0))
                .percentCommission(BigDecimal.valueOf(0.0))
                .minSum(BigDecimal.valueOf(10.0))
                .maxSum(BigDecimal.valueOf(1000.0))
                .build();

        payee2 = Payee.builder()
                .id(UUID.randomUUID())
                .payeeType(PayeeType.INDIVIDUALS)
                .name("name")
                .inn("inn")
                .bic("bic")
                .payeeAccountNumber(UUID.randomUUID().toString())
                .payeeCardNumber("0000111100001111")
                .build();

        transferOrder2 = TransferOrder.builder()
                .id(UUID.randomUUID())
                .createdAt(LocalDateTime.now())
                .transferType(transferType2)
                .remitterCardNumber("0000111100001111")
                .payee(payee2)
                .sum(new BigDecimal("1000"))
                .sumCommission(new BigDecimal("0"))
                .transferStatus(IN_PROGRESS)
                .authorizationCode("1111")
                .clientId(UUID.randomUUID())
                .operationType(OperationType.EXPENSE)
                .currencyExchange(new BigDecimal(1.0))
                .purpose("purpose")
                .isFavorite(false)
                .build();

        createPaymentResponseDtoExpexted = CreatePaymentResponseDto.builder()
                .id(transferOrder2.getId().toString())
                .created_at(transferOrder2.getCreatedAt().toString())
                .status(transferOrder2.getTransferStatus().name())
                .card_number(transferOrder2.getRemitterCardNumber())
                .transfer_type_id(transferOrder2.getTransferType().getId().toString())
                .sum(transferOrder2.getSum().toString())
                .remitter_card_number(transferOrder2.getRemitterCardNumber())
                .name(transferOrder2.getPayee().getName())
                .payee_account_number(transferOrder2.getPayee().getPayeeAccountNumber())
                .payee_card_number(transferOrder2.getPayee().getPayeeCardNumber())
                .sum_commission(transferOrder2.getSumCommission().toString())
                .authorizationCode(transferOrder2.getAuthorizationCode())
                .currencyExchange(transferOrder2.getCurrencyExchange().toString())
                .purpose(transferOrder2.getPurpose())
                .inn(transferOrder2.getPayee().getInn())
                .bic(transferOrder2.getPayee().getBic())
                .build();

        changeStatusResponseDtoExpected = ChangeStatusResponseDto.builder()
                .transferId(UUID.fromString("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11"))
                .status(PERFORMED)
                .build();
    }

    @Test
    @DisplayName("Verify auto payment dto list fields")
    void toAutoPaymentDtoList_shouldReturnAutoPaymentDtoList() {
        //ACT
        List<AutoPaymentsDto> autoPaymentsDtoList = transferMapper.toAutoPaymentDtoList(transferOrderList);
        //VERIFY
        verifyAutoPaymentDtoList(autoPaymentsDtoList, transferOrderList);
    }

    @Test
    @DisplayName("Verify auto payment dto fields")
    void toAutoPaymentDto_shouldReturnAutoPaymentDto() {
        //ACT
        AutoPaymentsDto autoPaymentsDto = transferMapper.toAutoPaymentDto(autoPayment);

        //VERIFY
        verifyAutoPaymentDto(autoPaymentsDto, autoPayment);
    }

    @Test
    @DisplayName("Verify transfer order dto fields setting")
    void toTransferOrderDto_shouldReturnCorrectMappingData() {
        //ACT
        IsFavoriteTransferDto isFavoriteTransferDto = transferMapper.toIsFavoriteTransferDto(transferOrder);

        //VERIFY
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(isFavoriteTransferDto.getIsFavorite())
                    .withFailMessage("The payment ID should be equals")
                    .isEqualTo(transferOrder.getIsFavorite());
        });
    }

    @Test
    void transferOrderToTransferDto() {
        //ACT
        transferDto = transferMapper.transferOrderToTransferDto(transferOrder);

        //VERIFY
        verifyTransferDtoFields();
    }

    @Test
    @DisplayName("Verify transfer order dto fields setting")
    void toCreditCardStatementDto_shouldReturnCorrectMappingData() {
        //ACT
        List<CreditCardStatementDto> creditCardStatementDtos = transferMapper.toCreditCardStatementDto(List.of(transferOrder));

        //VERIFY
        verifyCreditCardStatementDto(creditCardStatementDtos);
    }

    @Test
    @DisplayName("Verify TransferOrder, ResponseClientDataDto to ResponseBrokerageAccountDto")
    void toResponseBrokerageAccountDto_shouldReturnCorrectMappingData() {
        //ACT
        ResponseBrokerageAccountDto responseBrokerageAccountDtoActual = transferMapper.toResponseBrokerageAccountDto(transferOrder, responseClientDataDto);

        //VERIFY
        assertThat(responseBrokerageAccountDtoActual).usingRecursiveComparison().isEqualTo(responseBrokerageAccountDtoExpected);
    }

    @Test
    @DisplayName("Verify toProducerTransferOrderEvent")
    void toProducerTransferOrderEvent_shouldReturnCorrectMappingData() {
        //ACT
        ProducerTransferOrderEvent producerTransferOrderEventActual = transferMapper.toProducerTransferOrderEvent(transferOrder);

        //VERIFY
        assertThat(producerTransferOrderEventActual).usingRecursiveComparison().isEqualTo(producerTransferOrderEventExpected);
    }

    @Test
    @DisplayName("Verify toCreatePaymentResponseDto")
    void toCreatePaymentResponseDto_shouldReturnCorrectMappingData() {
        //ACT
        CreatePaymentResponseDto createPaymentResponseDtoActual = transferMapper.toCreatePaymentResponseDto(transferOrder2);

        //VERIFY
        verifyCreatePaymentResponseDto(createPaymentResponseDtoActual);
    }

    @Test
    @DisplayName("Verify toChangeStatusResponseDto")
    void toChangeStatusResponseDto_shouldReturnCorrectMappingData() {
        //ACT
        ChangeStatusResponseDto changeStatusResponseDtoActual = transferMapper.toChangeStatusResponseDto(transferOrder);

        //VERIFY
        assertThat(changeStatusResponseDtoActual).usingRecursiveComparison().isEqualTo(changeStatusResponseDtoExpected);
    }

    private void verifyAutoPaymentDto(AutoPaymentsDto actual, TransferOrder expected) {
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(actual.getTransferOrderId())
                    .isEqualTo(expected.getId());
            softAssertions.assertThat(actual.getTypeName())
                    .isEqualTo(expected.getTransferType().getTransferTypeName());
        });
    }

    private void verifyAutoPaymentDtoList(List<AutoPaymentsDto> actual, List<TransferOrder> expected) {
        for (int i = 0; i < actual.size(); i++) {
            verifyAutoPaymentDto(actual.get(i), expected.get(i));
        }
    }

    private void verifyTransferDtoFields(){
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(transferDto.getTransferTypeId())
                    .withFailMessage("Transfer id should be equals")
                    .isEqualTo(transferType.getId());
            softAssertions.assertThat(transferDto.getPurpose())
                    .withFailMessage("Transfer purpose should be equals")
                    .isEqualTo(transferOrder.getPurpose());
            softAssertions.assertThat(transferDto.getPayeeId())
                    .withFailMessage("Transfer payee id should be equals")
                    .isEqualTo(payee.getId());
            softAssertions.assertThat(transferDto.getSum())
                    .withFailMessage("Transfer sum should be equals")
                    .isEqualTo(transferOrder.getSum());
            softAssertions.assertThat(transferDto.getIsFavorite())
                    .withFailMessage("Transfer should be favorite")
                    .isEqualTo(transferOrder.getIsFavorite());
        });
    }

    private void verifyCreditCardStatementDto(List<CreditCardStatementDto> creditCardStatementDtos) {
        CreditCardStatementDto creditCardStatementDto = creditCardStatementDtos.get(0);
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(creditCardStatementDto.getTransferOrderId())
                    .isEqualTo(transferOrder.getId());
            softAssertions.assertThat(creditCardStatementDto.getPurpose())
                    .isEqualTo(transferOrder.getPurpose());
            softAssertions.assertThat(creditCardStatementDto.getPayeeId())
                    .isEqualTo(transferOrder.getPayee().getId());
            softAssertions.assertThat(creditCardStatementDto.getSum())
                    .isEqualTo(transferOrder.getSum());
            softAssertions.assertThat(creditCardStatementDto.getTypeName())
                    .isEqualTo(transferOrder.getTransferType().getTransferTypeName());
            softAssertions.assertThat(creditCardStatementDto.getCurrencyCode())
                    .isEqualTo(transferOrder.getTransferType().getCurrencyCode());
        });
    }

    private void verifyCreatePaymentResponseDto(CreatePaymentResponseDto createPaymentResponseDto) {
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(createPaymentResponseDto.getId())
                    .isEqualTo(transferOrder2.getId().toString());
            softAssertions.assertThat(createPaymentResponseDto.getCreated_at())
                    .isEqualTo(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(transferOrder2.getCreatedAt()));
            softAssertions.assertThat(createPaymentResponseDto.getStatus())
                    .isEqualTo(transferOrder2.getTransferStatus().name());
            softAssertions.assertThat(createPaymentResponseDto.getCard_number())
                    .isEqualTo(transferOrder2.getRemitterCardNumber());
            softAssertions.assertThat(createPaymentResponseDto.getTransfer_type_id())
                    .isEqualTo(transferOrder2.getTransferType().getId().toString());
            softAssertions.assertThat(createPaymentResponseDto.getSum())
                    .isEqualTo(transferOrder2.getSum().toString());
            softAssertions.assertThat(createPaymentResponseDto.getSum())
                    .isEqualTo(transferOrder2.getSum().toString());
            softAssertions.assertThat(createPaymentResponseDto.getRemitter_card_number())
                    .isEqualTo(transferOrder2.getRemitterCardNumber());
            softAssertions.assertThat(createPaymentResponseDto.getName())
                    .isEqualTo(transferOrder2.getPayee().getName());
            softAssertions.assertThat(createPaymentResponseDto.getPayee_account_number())
                    .isEqualTo(transferOrder2.getPayee().getPayeeAccountNumber());
            softAssertions.assertThat(createPaymentResponseDto.getPayee_card_number())
                    .isEqualTo(transferOrder2.getPayee().getPayeeCardNumber());
            softAssertions.assertThat(createPaymentResponseDto.getSum_commission())
                    .isEqualTo(transferOrder2.getSumCommission().toString());
            softAssertions.assertThat(createPaymentResponseDto.getAuthorizationCode())
                    .isEqualTo(transferOrder2.getAuthorizationCode());
            softAssertions.assertThat(createPaymentResponseDto.getCurrencyExchange())
                    .isEqualTo(transferOrder2.getCurrencyExchange().toString());
            softAssertions.assertThat(createPaymentResponseDto.getPurpose())
                    .isEqualTo(transferOrder2.getPurpose());
            softAssertions.assertThat(createPaymentResponseDto.getInn())
                    .isEqualTo(transferOrder2.getPayee().getInn());
            softAssertions.assertThat(createPaymentResponseDto.getBic())
                    .isEqualTo(transferOrder2.getPayee().getBic());
        });
    }
}