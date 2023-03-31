package by.afinny.moneytransfer.mapper;

import by.afinny.moneytransfer.dto.DebitCardStatementDto;
import by.afinny.moneytransfer.dto.TransferOrderHistoryDto;
import by.afinny.moneytransfer.entity.Payee;
import by.afinny.moneytransfer.entity.TransferOrder;
import by.afinny.moneytransfer.entity.TransferType;
import by.afinny.moneytransfer.entity.constant.CurrencyCode;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
public class TransferHistoryMapperTest {

    @InjectMocks
    private TransferHistoryMapperImpl transferHistoryMapper;

    private List<TransferOrder> transferOrderList = new ArrayList<>();
    private List<TransferOrderHistoryDto> transferOrderHistoryDto;
    private List<DebitCardStatementDto> debitCardStatementDto;
    private final UUID TRANSFER_ID = UUID.randomUUID();
    private final String REMITTER_CARD_NUMBER = "1111111";
    private final String PAYEE_NAME= "XVZ";

    @BeforeAll
    void setUp() {
        transferOrderList.add(TransferOrder.builder()
                .id(TRANSFER_ID)
                .completedAt(LocalDateTime.now())
                .sum(new BigDecimal("5.0"))
                .payee(Payee.builder()
                        .id(UUID.fromString("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11"))
                        .name(PAYEE_NAME)
                        .build())
                .transferStatus(TransferStatus.DRAFT)
                .transferType(TransferType.builder()
                        .transferTypeName(TransferTypeName.BETWEEN_CARDS)
                        .currencyCode(CurrencyCode.RUB)
                        .build())
                .purpose("XVZ")
                .remitterCardNumber(REMITTER_CARD_NUMBER)
                .createdAt(LocalDateTime.now())
                .build());
    }

    @Test
    @DisplayName("Verify transfer order history dto fields setting")
    void mapTransferOrderHistoryDto_thenReturn() {
        //ACT
        transferOrderHistoryDto = transferHistoryMapper.toTransferOrderHistoryDto(transferOrderList);

        //VERIFY
        verifyTransferOrderHistoryFields();
    }

    @Test
    @DisplayName("Verify transfer order dto fields setting")
    void toDebitCardStatementDto_shouldReturnCorrectMappingData() {
        //ACT
        debitCardStatementDto = transferHistoryMapper.toDebitCardStatementDto(transferOrderList);

        //VERIFY
        verifyDebitCardStatementDto();
    }

    private void verifyTransferOrderHistoryFields() {
        TransferOrderHistoryDto transferOrderHistory = transferOrderHistoryDto.get(0);
        TransferOrder transferOrder = transferOrderList.get(0);
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(transferOrderHistory.getTransferOrderId())
                    .withFailMessage("Id should be equals")
                    .isEqualTo(transferOrder.getId());
            softAssertions.assertThat(transferOrderHistory.getCompletedAt())
                    .withFailMessage("CompletedAt should be equals")
                    .isEqualTo(transferOrder.getCompletedAt());
            softAssertions.assertThat(transferOrderHistory.getCreatedAt())
                    .withFailMessage("CreatedAt commission should be equals")
                    .isEqualTo(transferOrder.getCreatedAt());
            softAssertions.assertThat(transferOrderHistory.getRemitterCardNumber())
                    .withFailMessage("CompletedAt should be equals")
                    .isEqualTo(transferOrder.getRemitterCardNumber());
            softAssertions.assertThat(transferOrderHistory.getName())
                    .withFailMessage("Purpose should be equals")
                    .isEqualTo(transferOrder.getPayee().getName());
            softAssertions.assertThat(transferOrderHistory.getSum())
                    .withFailMessage("Sum should be equals")
                    .isEqualTo(transferOrder.getSum());
            softAssertions.assertThat(transferOrderHistory.getCurrencyCode())
                    .withFailMessage("CurrencyCode should be equals")
                    .isEqualTo(transferOrder.getTransferType().getCurrencyCode());
            softAssertions.assertThat(transferOrderHistory.getTransferStatus())
                    .withFailMessage("TransferStatus should be equals")
                    .isEqualTo(transferOrder.getTransferStatus());
            softAssertions.assertThat(transferOrderHistory.getTransferTypeName())
                    .withFailMessage("TransferTypeName should be equals")
                    .isEqualTo(transferOrder.getTransferType().getTransferTypeName());
            softAssertions.assertThat(transferOrderHistory.getPayeeId())
                    .withFailMessage("PayeeId should be equals")
                    .isEqualTo(transferOrder.getPayee().getId());
        });
    }

    private void verifyDebitCardStatementDto() {
        DebitCardStatementDto debitCardStatement = debitCardStatementDto.get(0);
        TransferOrder transferOrder = transferOrderList.get(0);
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(debitCardStatement.getTransferOrderId())
                    .isEqualTo(transferOrder.getId());
            softAssertions.assertThat(debitCardStatement.getPurpose())
                    .isEqualTo(transferOrder.getPurpose());
            softAssertions.assertThat(debitCardStatement.getPayeeId())
                    .isEqualTo(transferOrder.getPayee().getId());
            softAssertions.assertThat(debitCardStatement.getSum())
                    .isEqualTo(transferOrder.getSum());
            softAssertions.assertThat(debitCardStatement.getTypeName())
                    .isEqualTo(transferOrder.getTransferType().getTransferTypeName());
            softAssertions.assertThat(debitCardStatement.getCurrencyCode())
                    .isEqualTo(transferOrder.getTransferType().getCurrencyCode());
        });
    }
}
