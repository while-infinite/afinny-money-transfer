package by.afinny.moneytransfer.repository;

import by.afinny.moneytransfer.entity.Payee;
import by.afinny.moneytransfer.entity.TransferOrder;
import by.afinny.moneytransfer.entity.TransferType;
import by.afinny.moneytransfer.entity.constant.CurrencyCode;
import by.afinny.moneytransfer.entity.constant.PayeeType;
import by.afinny.moneytransfer.entity.constant.TransferStatus;
import by.afinny.moneytransfer.entity.constant.TransferTypeName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import javax.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

@SpringBootTest
@Sql(
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
        scripts = {"/schema-h2.sql"}
)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
public class TransferOrderRepositoryTest {

    @Autowired
    private TransferOrderRepository transferOrderRepository;
    @Autowired
    private PayeeRepository payeeRepository;
    @Autowired
    private TransferTypeRepository transferTypeRepository;

    private TransferOrder transferOrderFirst;
    private TransferOrder transferOrderSecond;
    private Payee payee;
    private TransferType transferType;
    private final UUID CLIENT_ID = UUID.randomUUID();
    private final String REMITTER_CARD_NUMBER = "111111111";
    private final LocalDateTime FROM = LocalDateTime.now().minusDays(1);
    private final LocalDateTime TO = LocalDateTime.now().plusDays(1);
    private final Integer pageNumber = 0;
    private final Integer pageSize = 1;
    private final Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by("createdAt").descending());

    @BeforeEach
    void setUp() {
        payee = Payee.builder()
                .bic("56656")
                .inn("4788655")
                .name("Петя")
                .payeeCardNumber("45646")
                .payeeAccountNumber("456512654")
                .payeeType(PayeeType.INDIVIDUALS)
                .build();

        transferType = TransferType.builder()
                .transferTypeName(TransferTypeName.BETWEEN_CARDS)
                .currencyCode(CurrencyCode.EUR)
                .percentCommission(new BigDecimal("5.0000"))
                .maxCommission(new BigDecimal("10.0000"))
                .minCommission(new BigDecimal("2.0000"))
                .maxSum(new BigDecimal("500.0000"))
                .minSum(new BigDecimal("100.0000"))
                .build();

        transferOrderSecond = TransferOrder.builder()
                .completedAt(LocalDateTime.now())
                .sum(new BigDecimal("5.0000"))
                .payee(payee)
                .transferStatus(TransferStatus.DRAFT)
                .transferType(transferType)
                .authorizationCode("1256")
                .currencyExchange(new BigDecimal("500.0000"))
                .remitterCardNumber(REMITTER_CARD_NUMBER)
                .purpose("XVZ")
                .createdAt(LocalDateTime.now())
                .clientId(CLIENT_ID)
                .build();

        transferOrderFirst = TransferOrder.builder()
                .completedAt(LocalDateTime.now())
                .sum(new BigDecimal("18.0000"))
                .payee(payee)
                .transferStatus(TransferStatus.PERFORMED)
                .authorizationCode("12577")
                .currencyExchange(new BigDecimal("500.0000"))
                .transferType(transferType)
                .remitterCardNumber(REMITTER_CARD_NUMBER)
                .purpose("WOW")
                .createdAt(LocalDateTime.now())
                .clientId(CLIENT_ID)
                .build();
    }

    @BeforeEach
    void cleanUp() {
        transferOrderRepository.deleteAll();
        payeeRepository.deleteAll();
        transferTypeRepository.deleteAll();
    }

    @Test
    @DisplayName("if Transfer order history exists then return transfer order history list")
    void findAllByRemitterCardNumber_thenReturnTransferOrder() {
        //ARRANGE
        UUID id = payeeRepository.save(payee).getId();
        payee.setId(id);
        Integer transferTypeId = transferTypeRepository.save(transferType).getId();
        transferType.setId(transferTypeId);
        UUID clientIdFirst = transferOrderRepository.save(transferOrderFirst).getId();
        transferOrderFirst.setId(clientIdFirst);
        UUID clientIdSecond = transferOrderRepository.save(transferOrderSecond).getId();
        transferOrderSecond.setId(clientIdSecond);

        //ACT
        List<TransferOrder> transferOrders = transferOrderRepository
                .findAllByRemitterCardNumber(REMITTER_CARD_NUMBER,CLIENT_ID, FROM, TO, pageable);
        if (transferOrders.isEmpty()) {
            throw new EntityNotFoundException("Entity not found");
        }

        //VERIFY
        verifyTransferOrderHistoryBody(transferOrders);
    }

    @Test
    @DisplayName("if Transfer order history, then returns is empty")
    void findAllByRemitterCardNumber_doseNotExist_thenReturnIsEmpty() {
        //ACT
        List<TransferOrder> transferOrders = transferOrderRepository
                .findAllByRemitterCardNumber(REMITTER_CARD_NUMBER,CLIENT_ID,FROM,TO, pageable);

        //VERIFY
        assertThat(transferOrders).isEmpty();
    }

    private void verifyTransferOrderHistoryBody(List<TransferOrder> transferOrders) {
        TransferOrder foundTransferOrderHistory = transferOrders.get(0);
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(transferOrders.size())
                    .withFailMessage("Size should be equals")
                    .isEqualTo(pageSize);
            softAssertions.assertThat(foundTransferOrderHistory.getId())
                    .withFailMessage("Id should be equals")
                    .isEqualTo(transferOrderFirst.getId());
            softAssertions.assertThat(foundTransferOrderHistory.getPurpose())
                    .withFailMessage("Purpose should be equals")
                    .isEqualTo(transferOrderFirst.getPurpose());
            softAssertions.assertThat(foundTransferOrderHistory.getSum())
                    .withFailMessage("Sum should be equals")
                    .isEqualTo(transferOrderFirst.getSum());
            softAssertions.assertThat(foundTransferOrderHistory.getClientId())
                    .withFailMessage("ClientId should be equals")
                    .isEqualTo(transferOrderFirst.getClientId());
            softAssertions.assertThat(foundTransferOrderHistory.getTransferType().getCurrencyCode())
                    .withFailMessage("CurrencyCode should be equals")
                    .isEqualTo(transferOrderFirst.getTransferType().getCurrencyCode());
            softAssertions.assertThat(foundTransferOrderHistory.getTransferStatus())
                    .withFailMessage("Transfer status should be equals")
                    .isEqualTo(transferOrderFirst.getTransferStatus());
            softAssertions.assertThat(foundTransferOrderHistory.getTransferType().getTransferTypeName())
                    .withFailMessage("TransferTypeName should be equals")
                    .isEqualTo(transferOrderFirst.getTransferType().getTransferTypeName());
            softAssertions.assertThat(foundTransferOrderHistory.getPayee().getId())
                    .withFailMessage("PayeeId should be equals")
                    .isEqualTo(transferOrderFirst.getPayee().getId());
        });
    }
}