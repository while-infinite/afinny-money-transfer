package by.afinny.moneytransfer.service;

import by.afinny.moneytransfer.dto.CommissionDto;
import by.afinny.moneytransfer.entity.TransferType;
import by.afinny.moneytransfer.entity.constant.CurrencyCode;
import by.afinny.moneytransfer.entity.constant.TransferTypeName;
import by.afinny.moneytransfer.mapper.CommissionMapper;
import by.afinny.moneytransfer.repository.TransferTypeRepository;
import by.afinny.moneytransfer.service.impl.CommissionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.springframework.test.context.ActiveProfiles;

import javax.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@ActiveProfiles("test")
public class CommissionServiceTest {

    @InjectMocks
    private CommissionServiceImpl commissionService;

    @Mock
    private TransferTypeRepository transferTypeRepository;
    @Mock
    private CommissionMapper commissionMapper;

    private CommissionDto commissionDto;
    private final TransferTypeName transferTypeName = TransferTypeName.BETWEEN_CARDS;
    private final CurrencyCode currencyCode = CurrencyCode.RUB;
    private TransferType transferType;

    @BeforeEach
    void setUp() {
        transferType = TransferType.builder()
                .id(1)
                .fixCommission(new BigDecimal(5))
                .percentCommission(new BigDecimal(6))
                .maxCommission(new BigDecimal(10))
                .minCommission(new BigDecimal(2))
                .maxSum(new BigDecimal(500))
                .minSum(new BigDecimal(100))
                .currencyCode(currencyCode)
                .transferTypeName(transferTypeName)
                .build();

        commissionDto = CommissionDto.builder()
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
    @DisplayName("if card with incoming card number was found then return accountNumber")
    void getCommissionData_shouldReturnCommissionDto(){
        //ARRANGE
        when(transferTypeRepository.findByTransferTypeNameAndCurrencyCode(transferTypeName,currencyCode))
                .thenReturn(Optional.of(transferType));
        when(commissionMapper.toCommissionDto(transferType)).thenReturn(commissionDto);

        //ACT
        CommissionDto result = commissionService.getCommissionData(transferTypeName, currencyCode);

        //VERIFY
        verifyTransferTypeBody(result);
    }

    @Test
    @DisplayName("if card with incoming card number wasn't found then throws EntityNotFoundException")
    void getCommissionData_ifNotSuccess_thenThrow(){
        //ARRANGE
        when(transferTypeRepository.findByTransferTypeNameAndCurrencyCode(transferTypeName, currencyCode))
                .thenReturn(Optional.empty());

        //ACT
        ThrowingCallable getAccountByCardIdMethodInvocation = ()-> commissionService
                .getCommissionData(transferTypeName,currencyCode);

        //VERIFY
        assertThatThrownBy(getAccountByCardIdMethodInvocation).isInstanceOf(EntityNotFoundException.class);
    }

    private void verifyTransferTypeBody(CommissionDto commissionDtoTest){

        assertSoftly(softAssertions -> {
            softAssertions.assertThat(commissionDtoTest)
                    .withFailMessage("commissionDto shouldn't be null")
                    .isNotNull();
            softAssertions.assertThat(commissionDtoTest.getId())
                    .withFailMessage("Id should be equals")
                    .isEqualTo(commissionDto.getId());
            softAssertions.assertThat(commissionDtoTest.getFixCommission())
                    .withFailMessage("Fix commission should be equals")
                    .isEqualTo(commissionDto.getFixCommission());
            softAssertions.assertThat(commissionDtoTest.getPercentCommission())
                    .withFailMessage("Percent commission should be equals")
                    .isEqualTo(commissionDto.getPercentCommission());
            softAssertions.assertThat(commissionDtoTest.getMaxCommission())
                    .withFailMessage("Max commission should be equals")
                    .isEqualTo(commissionDto.getMaxCommission());
            softAssertions.assertThat(commissionDtoTest.getMinCommission())
                    .withFailMessage("Min commission should be equals")
                    .isEqualTo(commissionDto.getMinCommission());
            softAssertions.assertThat(commissionDtoTest.getMaxSum())
                    .withFailMessage("Max sum should be equals")
                    .isEqualTo(commissionDto.getMaxSum());
            softAssertions.assertThat(commissionDtoTest.getMinSum())
                    .withFailMessage("Min sum should be equals")
                    .isEqualTo(commissionDto.getMinSum());
        });
    }
}





