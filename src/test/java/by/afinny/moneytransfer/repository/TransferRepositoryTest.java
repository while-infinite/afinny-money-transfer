package by.afinny.moneytransfer.repository;

import by.afinny.moneytransfer.entity.TransferType;
import by.afinny.moneytransfer.entity.constant.CurrencyCode;
import by.afinny.moneytransfer.entity.constant.TransferTypeName;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import javax.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

@SpringBootTest
@Sql(
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
        scripts = {"/schema-h2.sql"}
)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
public class TransferRepositoryTest {

    @Autowired
    private TransferTypeRepository transferTypeRepository;

    private TransferType transferType;
    private final TransferTypeName typeName = TransferTypeName.BETWEEN_CARDS;
    private final CurrencyCode currencyCode = CurrencyCode.RUB;

    @BeforeAll
    void setUp() {
        transferType = TransferType.builder()
                .currencyCode(currencyCode)
                .transferTypeName(typeName)
                .percentCommission(new BigDecimal("5.0000"))
                .maxCommission(new BigDecimal("10.0000"))
                .minCommission(new BigDecimal("2.0000"))
                .maxSum(new BigDecimal("500.0000"))
                .minSum(new BigDecimal("100.0000"))
                .build();
    }

    @Test
    @DisplayName("if TransferType exists then return transfer type")
    void transferTypeExists_thenReturnTransferType() {
        //ARRANGE
        Integer id = transferTypeRepository.save(transferType).getId();
        transferType.setId(id);
        //ACT
        TransferType foundTransferType = transferTypeRepository
                .findByTransferTypeNameAndCurrencyCode(typeName,currencyCode)
                .orElseThrow(()-> new EntityNotFoundException("Entity not found"));
        //VERIFY
        verifyTransferTypeBody(foundTransferType);
    }

    @Test
    @DisplayName("if TransferType does not exist, then returns is empty")
    void transferType_doseNotExist_thenReturnIsEmpty() {
        //ACT
        Optional<TransferType> foundTransferType = transferTypeRepository
                .findByTransferTypeNameAndCurrencyCode(typeName,currencyCode);
        //VERIFY
        assertThat(foundTransferType).isEmpty();
    }

    private void verifyTransferTypeBody(TransferType foundTransferType){
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(foundTransferType)
                    .withFailMessage("commissionDto shouldn't be null")
                    .isNotNull();
            softAssertions.assertThat(foundTransferType.getId())
                    .withFailMessage("Id should be equals")
                    .isEqualTo(transferType.getId());
            softAssertions.assertThat(foundTransferType.getMaxCommission())
                    .withFailMessage("Max commission should be equals")
                    .isEqualTo(transferType.getMaxCommission());
            softAssertions.assertThat(foundTransferType.getMinCommission())
                    .withFailMessage("Min commission should be equals")
                    .isEqualTo(transferType.getMinCommission());
            softAssertions.assertThat(foundTransferType.getMaxSum())
                    .withFailMessage("Max sum should be equals")
                    .isEqualTo(transferType.getMaxSum());
            softAssertions.assertThat(foundTransferType.getMinSum())
                    .withFailMessage("Min sum should be equals")
                    .isEqualTo(transferType.getMinSum());
            softAssertions.assertThat(foundTransferType.getPercentCommission())
                    .withFailMessage("Percent commission should be equals")
                    .isEqualTo(transferType.getPercentCommission());
        });
    }
}