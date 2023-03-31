package by.afinny.moneytransfer.integration;

import by.afinny.moneytransfer.controller.AutoPaymentController;
import by.afinny.moneytransfer.dto.AutoPaymentDto;
import by.afinny.moneytransfer.dto.AutoPaymentsDto;
import by.afinny.moneytransfer.entity.Payee;
import by.afinny.moneytransfer.entity.TransferOrder;
import by.afinny.moneytransfer.entity.TransferType;
import by.afinny.moneytransfer.entity.constant.CurrencyCode;
import by.afinny.moneytransfer.entity.constant.OperationType;
import by.afinny.moneytransfer.entity.constant.PayeeType;
import by.afinny.moneytransfer.entity.constant.TransferPeriodicity;
import by.afinny.moneytransfer.entity.constant.TransferStatus;
import by.afinny.moneytransfer.entity.constant.TransferTypeName;
import by.afinny.moneytransfer.repository.PayeeRepository;
import by.afinny.moneytransfer.repository.TransferOrderRepository;
import by.afinny.moneytransfer.repository.TransferTypeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
        scripts = {"/schema-h2.sql"}
)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
public class AutoPaymentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TransferOrderRepository transferOrderRepository;
    @Autowired
    private PayeeRepository payeeRepository;
    @Autowired
    private TransferTypeRepository transferTypeRepository;

    private Payee payee;
    private TransferType transferType;
    private TransferOrder firstTransferOrder;
    private TransferOrder secondTransferOrder;

    private List<AutoPaymentsDto> autoPaymentsDtoListResponseExpect;
    private AutoPaymentDto autoPaymentDtoRequest;
    private AutoPaymentDto autoPaymentDtoResponseExpect;

    private final UUID FIRST_CLIENT_ID_PERSIST = UUID.randomUUID();
    private final UUID SECOND_CLIENT_ID_PERSIST = UUID.randomUUID();
    private final UUID CLIENT_ID_NOT_PERSIST = UUID.randomUUID();
    private final UUID TRANSFER_ID_NOT_PERSIST = UUID.randomUUID();
    private final LocalDateTime NOW = LocalDateTime.now().withNano(0);
    private UUID TRANSFER_ID_PERSIS;

    @BeforeAll
    void setUp() {
        payee = Payee.builder()
                .payeeType(PayeeType.INDIVIDUALS)
                .bic("56656")
                .payeeAccountNumber("456512654")
                .build();

        transferType = TransferType.builder()
                .transferTypeName(TransferTypeName.BETWEEN_CARDS)
                .currencyCode(CurrencyCode.EUR)
                .minCommission(new BigDecimal("2.0000"))
                .maxCommission(new BigDecimal("10.0000"))
                .percentCommission(new BigDecimal("5.0000"))
                .minSum(new BigDecimal("100.0000"))
                .maxSum(new BigDecimal("500.0000"))
                .build();

        firstTransferOrder = TransferOrder.builder()
                .createdAt(NOW)
                .transferType(transferType)
                .remitterCardNumber("1234567812345678")
                .payee(payee)
                .sum(new BigDecimal("5.0000"))
                .transferStatus(TransferStatus.DRAFT)
                .authorizationCode("1256")
                .currencyExchange(new BigDecimal("500.0000"))
                .isFavorite(true)
                .startDate(NOW)
                .transferPeriodicity(TransferPeriodicity.MONTHLY)
                .clientId(FIRST_CLIENT_ID_PERSIST)
                .operationType(OperationType.EXPENSE)
                .build();

        secondTransferOrder = TransferOrder.builder()
                .createdAt(NOW)
                .transferType(transferType)
                .remitterCardNumber("1234567812345678")
                .payee(payee)
                .sum(new BigDecimal("5.0000"))
                .transferStatus(TransferStatus.DRAFT)
                .authorizationCode("1256")
                .currencyExchange(new BigDecimal("500.0000"))
                .isFavorite(true)
                .clientId(SECOND_CLIENT_ID_PERSIST)
                .operationType(OperationType.EXPENSE)
                .build();

        payeeRepository.save(payee);
        transferTypeRepository.save(transferType);
        transferOrderRepository.save(firstTransferOrder);
        transferOrderRepository.save(secondTransferOrder);

        TRANSFER_ID_PERSIS = secondTransferOrder.getId();

        autoPaymentsDtoListResponseExpect = Collections.singletonList(AutoPaymentsDto.builder()
                .transferOrderId(firstTransferOrder.getId())
                .typeName(transferType.getTransferTypeName())
                .build());

        autoPaymentDtoRequest = AutoPaymentDto.builder()
                .startDate(NOW)
                .periodicity(TransferPeriodicity.MONTHLY)
                .build();

        autoPaymentDtoResponseExpect = AutoPaymentDto.builder()
                .startDate(NOW)
                .periodicity(TransferPeriodicity.MONTHLY)
                .build();
    }

    @Test
    @DisplayName("If viewAutoPayments was successfully then return List<AutoPaymentsDto>")
    void viewAutoPayments_shouldReturnList() throws Exception {
        //ACT
        MvcResult result = mockMvc.perform(get(AutoPaymentController.URL_AUTO_PAYMENT)
                        .param(AutoPaymentController.PARAM_CLIENT_ID, FIRST_CLIENT_ID_PERSIST.toString()))
                .andExpect(status().isOk())
                .andReturn();

        //VERIFY
        assertThat(result.getResponse().getContentAsString())
                .isEqualTo(objectMapper.writeValueAsString(autoPaymentsDtoListResponseExpect));
    }

    @Test
    @DisplayName("If viewAutoPayments wasn't successfully then return status BAD REQUEST")
    void viewAutoPayments_ifNotSuccess_thenStatus400() throws Exception {
        //ACT & VERIFY
        mockMvc.perform(get(AutoPaymentController.URL_AUTO_PAYMENT)
                        .param(AutoPaymentController.PARAM_CLIENT_ID, CLIENT_ID_NOT_PERSIST.toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("If updateAutoPayment was successfully then return AutoPaymentDto")
    void updateAutoPayment_shouldReturnAutoPaymentDto() throws Exception {
        //ACT
        MvcResult result = mockMvc.perform(patch(AutoPaymentController.URL_AUTO_PAYMENT)
                        .param(AutoPaymentController.PARAM_TRANSFER_ID, TRANSFER_ID_PERSIS.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(autoPaymentDtoRequest)))
                .andExpect(status().isOk())
                .andReturn();

        //VERIFY
        assertThat(result.getResponse().getContentAsString())
                .isEqualTo(objectMapper.writeValueAsString(autoPaymentDtoResponseExpect));
    }

    @Test
    @DisplayName("If updateAutoPayment wasn't successfully then return status BAD REQUEST")
    void updateAutoPayment_shouldReturnStatus400() throws Exception {
        //ACT & VERIFY
        mockMvc.perform(patch(AutoPaymentController.URL_AUTO_PAYMENT).
                        param(AutoPaymentController.PARAM_TRANSFER_ID, TRANSFER_ID_NOT_PERSIST.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(autoPaymentDtoRequest)))
                .andExpect(status().isBadRequest());
    }
}