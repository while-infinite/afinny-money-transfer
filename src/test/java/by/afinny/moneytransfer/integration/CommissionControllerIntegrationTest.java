package by.afinny.moneytransfer.integration;

import by.afinny.moneytransfer.controller.CommissionController;
import by.afinny.moneytransfer.dto.CommissionDto;
import by.afinny.moneytransfer.entity.TransferType;
import by.afinny.moneytransfer.entity.constant.CurrencyCode;
import by.afinny.moneytransfer.entity.constant.TransferTypeName;
import by.afinny.moneytransfer.repository.TransferTypeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
        scripts = {"/schema-h2.sql"}
)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
public class CommissionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TransferTypeRepository transferTypeRepository;

    private TransferType transferType;

    private CommissionDto commissionDtoResponseExpect;

    private final TransferTypeName TYPE_NAME_PERSIST = TransferTypeName.BETWEEN_CARDS;
    private final CurrencyCode CURRENCY_CODE_PERSIST = CurrencyCode.USD;
    private final TransferTypeName TYPE_NAME_NOT_PERSIST = TransferTypeName.BY_PAYEE_DETAILS;
    private final CurrencyCode CURRENCY_CODE_NOT_PERSIST = CurrencyCode.EUR;


    @BeforeAll
    void setUp() {
        transferType = TransferType.builder()
                .transferTypeName(TYPE_NAME_PERSIST)
                .currencyCode(CURRENCY_CODE_PERSIST)
                .minCommission(new BigDecimal("2.0000"))
                .maxCommission(new BigDecimal("10.0000"))
                .percentCommission(new BigDecimal("5.0000"))
                .minSum(new BigDecimal("100.0000"))
                .maxSum(new BigDecimal("500.0000"))
                .build();

        transferTypeRepository.save(transferType);

        commissionDtoResponseExpect = CommissionDto.builder()
                .id(transferType.getId())
                .minCommission(transferType.getMinCommission())
                .maxCommission(transferType.getMaxCommission())
                .percentCommission(transferType.getPercentCommission())
                .minSum(transferType.getMinSum())
                .maxSum(transferType.getMaxSum())
                .build();
    }

    @Test
    @DisplayName("If getCommissionData was successful then return CommissionDto")
    void getCommissionData_shouldReturnCommissionDto() throws Exception {
        //ACT
        MvcResult result = mockMvc.perform(get(CommissionController.URL_COMMISSION)
                        .param(CommissionController.PARAM_TYPE_NAME, TYPE_NAME_PERSIST.toString())
                        .param(CommissionController.PARAM_CURRENCY_CODE, CURRENCY_CODE_PERSIST.toString()))
                .andExpect(status().isOk())
                .andReturn();
        //VERIFY
        assertThat(result.getResponse().getContentAsString())
                .isEqualTo(objectMapper.writeValueAsString(commissionDtoResponseExpect));
    }

    @Test
    @DisplayName("If getCommissionData wasn't successful then return status BAD REQUEST")
    void getCommissionData_shouldReturnBadRequest() throws Exception {
        //ACT & VERIFY
        mockMvc.perform(get(CommissionController.URL_COMMISSION)
                        .param(CommissionController.PARAM_TYPE_NAME, TYPE_NAME_NOT_PERSIST.toString())
                        .param(CommissionController.PARAM_CURRENCY_CODE, CURRENCY_CODE_NOT_PERSIST.toString()))
                .andExpect(status().isBadRequest());
    }
}
