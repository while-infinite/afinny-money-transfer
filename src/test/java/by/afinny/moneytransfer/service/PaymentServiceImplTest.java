package by.afinny.moneytransfer.service;

import by.afinny.moneytransfer.dto.AutoPaymentDto;
import by.afinny.moneytransfer.dto.AutoPaymentsDto;
import by.afinny.moneytransfer.entity.TransferOrder;
import by.afinny.moneytransfer.entity.TransferType;
import by.afinny.moneytransfer.entity.constant.TransferPeriodicity;
import by.afinny.moneytransfer.entity.constant.TransferTypeName;
import by.afinny.moneytransfer.mapper.TransferMapper;
import by.afinny.moneytransfer.repository.TransferOrderRepository;
import by.afinny.moneytransfer.service.impl.PaymentServiceImpl;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@ActiveProfiles("test")
class PaymentServiceImplTest {

    @InjectMocks
    private PaymentServiceImpl paymentService;
    @Mock
    private TransferOrderRepository transferOrderRepository;
    @Spy
    private TransferMapper transferOrderMapper;

    private final UUID TRANSFER_ORDER_ID = UUID.randomUUID();
    private final UUID CLIENT_ID = UUID.randomUUID();
    private List<TransferOrder> transferOrderList = new ArrayList<>();

    private AutoPaymentDto autoPaymentDto;
    private TransferOrder transferOrder;

    @BeforeEach
    void setUp() {
        autoPaymentDto = AutoPaymentDto.builder()
                .periodicity(TransferPeriodicity.MONTHLY)
                .startDate(LocalDateTime.now()).build();

        transferOrder = TransferOrder.builder()
                .id(TRANSFER_ORDER_ID)
                .transferPeriodicity(autoPaymentDto.getPeriodicity())
                .startDate(autoPaymentDto.getStartDate())
                .transferType(
                        TransferType.builder()
                                .transferTypeName(TransferTypeName.BY_PAYEE_DETAILS).build())
                .build();
        transferOrderList.add(transferOrder);
    }

    @Test
    @DisplayName("If addAutoPayment disabled auto payment then update TransferOrder and return AutoPaymentDto")
    void addAutoPayment_shouldDisableAutoPaymentAndReturnAutoPaymentDto() {
        //ARRANGE
        when(transferOrderRepository.findById(TRANSFER_ORDER_ID)).thenReturn(Optional.of(transferOrder));
        when(transferOrderRepository.save(transferOrder)).thenReturn(transferOrder);

        //ACT
        AutoPaymentDto actual = paymentService.addAutoPayment(this.autoPaymentDto, TRANSFER_ORDER_ID);

        //VERIFY
        verifyAutoPayment(new AutoPaymentDto(), actual);
    }

    @Test
    @DisplayName("If addAutoPayment enabled auto payment then update TransferOrder and return AutoPaymentDto")
    void addAutoPayment_shouldEnableAutoPaymentAndReturnAutoPaymentDto() {
        //ARRANGE
        transferOrder = TransferOrder.builder()
                .id(TRANSFER_ORDER_ID).build();
        when(transferOrderRepository.findById(TRANSFER_ORDER_ID)).thenReturn(Optional.of(transferOrder));
        when(transferOrderRepository.save(transferOrder)).thenReturn(transferOrder);

        //ACT
        AutoPaymentDto actual = paymentService.addAutoPayment(this.autoPaymentDto, TRANSFER_ORDER_ID);

        //VERIFY
        verifyAutoPayment(autoPaymentDto, actual);
    }


    @Test
    @DisplayName("If addAutoPayment not success then throw EntityNotFoundException")
    void addAutoPayment_ifNotSuccess_thenThrow() {
        //ARRANGE
        when(transferOrderRepository.findById(any(UUID.class))).thenThrow(EntityNotFoundException.class);

        //ACT
        ThrowableAssert.ThrowingCallable addAutoPaymentMethodInvocation = () -> paymentService.
                addAutoPayment(this.autoPaymentDto, TRANSFER_ORDER_ID);

        //VERIFY
        Assertions.assertThatThrownBy(addAutoPaymentMethodInvocation).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("if client id was found return AutoPaymentsDto")
    void viewAutoPayments_shouldReturnAutoPaymentsDto() {
        //ARRANGE
        when(transferOrderRepository.findByClientIdAndStartDateNotNullAndTransferPeriodicityNotNull(CLIENT_ID))
                .thenReturn(transferOrderList);
        List<AutoPaymentsDto> expected = transferOrderMapper.toAutoPaymentDtoList(transferOrderList);

        //ACT
        List<AutoPaymentsDto> actual = paymentService.viewAutoPayments(CLIENT_ID);

        //VERIFY
        verifyAutoPayments(actual, expected);
    }

    @Test
    @DisplayName("if client was not found then throw EntityNotFoundException")
    void viewAutoPayments_ifClientIdNotFound_thenThrow() {
        //ARRANGE
        when(transferOrderRepository.findByClientIdAndStartDateNotNullAndTransferPeriodicityNotNull(any(UUID.class)))
                .thenThrow(EntityNotFoundException.class);

        //ACT
        ThrowableAssert.ThrowingCallable viewAutoPaymentsMethodInvocation = () ->
                paymentService.viewAutoPayments(UUID.randomUUID());

        //VERIFY
        Assertions.assertThatThrownBy(viewAutoPaymentsMethodInvocation).isInstanceOf(EntityNotFoundException.class);
    }

    private void verifyAutoPayment(AutoPaymentDto expected, AutoPaymentDto actual) {
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(actual.getPeriodicity())
                    .isEqualTo(expected.getPeriodicity());
            softAssertions.assertThat(actual.getStartDate())
                    .isEqualTo(expected.getStartDate());
        });
    }

    private void verifyAutoPayments(List<AutoPaymentsDto> actual, List<AutoPaymentsDto> expected) {
        for (int i = 0; i < actual.size(); i++) {
            int finalI = i;
            assertSoftly(softAssertions -> {
                softAssertions.assertThat(actual.get(finalI).getTransferOrderId())
                        .isEqualTo(expected.get(finalI).getTransferOrderId());
                softAssertions.assertThat(actual.get(finalI).getTypeName())
                        .isEqualTo(expected.get(finalI).getTypeName());
            });
        }
    }
}