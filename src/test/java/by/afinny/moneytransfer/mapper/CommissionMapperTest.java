package by.afinny.moneytransfer.mapper;

import by.afinny.moneytransfer.dto.CommissionDto;
import by.afinny.moneytransfer.entity.TransferType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
public class CommissionMapperTest {

    @InjectMocks
    private CommissionMapperImpl commissionMapper;

    private TransferType transferType;
    private CommissionDto commissionDto;

    @BeforeAll
    void setUp() {
        transferType = TransferType.builder()
                .id(1)
                .fixCommission(new BigDecimal(5))
                .percentCommission(new BigDecimal(6))
                .maxCommission(new BigDecimal(10))
                .minCommission(new BigDecimal(2))
                .maxSum(new BigDecimal(500))
                .minSum(new BigDecimal(100))
                .build();
    }

    @Test
    @DisplayName("Verify commission dto fields setting")
    void mapAccountNumberDto_thenReturn() {
        //ACT
        commissionDto = commissionMapper.toCommissionDto(transferType);

        //VERIFY
        verifyTransferTypeFields();
    }

    private void verifyTransferTypeFields() {
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(commissionDto.getId())
                    .withFailMessage("Id should be equals")
                    .isEqualTo(transferType.getId());
            softAssertions.assertThat(commissionDto.getFixCommission())
                    .withFailMessage("Fix commission should be equals")
                    .isEqualTo(transferType.getFixCommission());
            softAssertions.assertThat(commissionDto.getPercentCommission())
                    .withFailMessage("Percent commission should be equals")
                    .isEqualTo(commissionDto.getPercentCommission());
            softAssertions.assertThat(commissionDto.getMaxCommission())
                    .withFailMessage("Max commission should be equals")
                    .isEqualTo(commissionDto.getMaxCommission());
            softAssertions.assertThat(commissionDto.getMinCommission())
                    .withFailMessage("Min commission should be equals")
                    .isEqualTo(commissionDto.getMinCommission());
            softAssertions.assertThat(commissionDto.getMaxSum())
                    .withFailMessage("Max sum should be equals")
                    .isEqualTo(commissionDto.getMaxSum());
            softAssertions.assertThat(commissionDto.getMinSum())
                    .withFailMessage("Min sum should be equals")
                    .isEqualTo(commissionDto.getMinSum());
        });
    }
}