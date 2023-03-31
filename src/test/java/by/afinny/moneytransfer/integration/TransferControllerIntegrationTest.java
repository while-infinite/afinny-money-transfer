package by.afinny.moneytransfer.integration;

import by.afinny.moneytransfer.controller.TransferController;
import by.afinny.moneytransfer.dto.IsFavoriteTransferDto;
import by.afinny.moneytransfer.dto.TransferDto;
import by.afinny.moneytransfer.dto.TransferOrderIdDto;
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
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
public class TransferControllerIntegrationTest {

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

    private TransferOrderIdDto transferOrderIdDtoRequest;
    private IsFavoriteTransferDto isFavoriteTransferDtoResponseExpect;
    private TransferDto transferDtoResponseExpect;
    private List<TransferTypeName> transferTypeListResponseExpect;
    private List<TransferTypeName> paymentTypeListResponseExpect;

    private final UUID FIRST_CLIENT_ID_PERSIST = UUID.randomUUID();
    private final UUID SECOND_CLIENT_ID_PERSIST = UUID.randomUUID();
    private final UUID CLIENT_ID_NOT_PERSIST = UUID.randomUUID();
    private final UUID TRANSFER_ID_NOT_PERSIST = UUID.randomUUID();
    private UUID FIRST_TRANSFER_ID_PERSIST;
    private UUID SECOND_TRANSFER_ID_PERSIST;

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
                .createdAt(LocalDateTime.now())
                .transferType(transferType)
                .remitterCardNumber("1234567812345678")
                .payee(payee)
                .sum(new BigDecimal("5.0000"))
                .transferStatus(TransferStatus.DRAFT)
                .authorizationCode("1256")
                .currencyExchange(new BigDecimal("500.0000"))
                .isFavorite(true)
                .startDate(LocalDateTime.now())
                .transferPeriodicity(TransferPeriodicity.MONTHLY)
                .clientId(FIRST_CLIENT_ID_PERSIST)
                .operationType(OperationType.EXPENSE)
                .build();

        secondTransferOrder = TransferOrder.builder()
                .createdAt(LocalDateTime.now())
                .transferType(transferType)
                .remitterCardNumber("1234567812345678")
                .payee(payee)
                .sum(new BigDecimal("5.0000"))
                .transferStatus(TransferStatus.DRAFT)
                .authorizationCode("1256")
                .currencyExchange(new BigDecimal("500.0000"))
                .isFavorite(true)
                .startDate(LocalDateTime.now())
                .transferPeriodicity(TransferPeriodicity.MONTHLY)
                .clientId(SECOND_CLIENT_ID_PERSIST)
                .operationType(OperationType.EXPENSE)
                .build();

        payeeRepository.save(payee);
        transferTypeRepository.save(transferType);
        transferOrderRepository.save(firstTransferOrder);
        transferOrderRepository.save(secondTransferOrder);

        FIRST_TRANSFER_ID_PERSIST = firstTransferOrder.getId();
        SECOND_TRANSFER_ID_PERSIST = secondTransferOrder.getId();

        transferOrderIdDtoRequest = TransferOrderIdDto.builder()
                .transferOrderId(firstTransferOrder.getId())
                .build();

        isFavoriteTransferDtoResponseExpect = IsFavoriteTransferDto.builder()
                .isFavorite(!firstTransferOrder.getIsFavorite())
                .build();

        transferDtoResponseExpect = TransferDto.builder()
                .transferTypeId(transferType.getId())
                .payeeId(payee.getId())
                .purpose(secondTransferOrder.getPurpose())
                .sum(secondTransferOrder.getSum())
                .isFavorite(secondTransferOrder.getIsFavorite())
                .build();

        transferTypeListResponseExpect = List.of(
                TransferTypeName.BETWEEN_CARDS,
                TransferTypeName.TO_ANOTHER_CARD,
                TransferTypeName.BY_PHONE_NUMBER,
                TransferTypeName.BY_PAYEE_DETAILS);

        paymentTypeListResponseExpect = List.of(
                TransferTypeName.FAVORITES,
                TransferTypeName.AUTOPAYMENTS,
                TransferTypeName.BANKING_SERVICES,
                TransferTypeName.INFO_SERVISES,
                TransferTypeName.PAYMENT_FOR_SERVICES,
                TransferTypeName.UTILITIES,
                TransferTypeName.OTHER_PAYMENTS);
    }

    @Test
    @DisplayName("If deleteIdDraftTransfer was successful then return status NO CONTENT")
    void deleteIdDraftTransfer_shouldReturnNoContent() throws Exception {
        //ACT & VERIFY
        mockMvc.perform(delete(TransferController.URL_TRANSFER +
                        TransferController.URL_DELETE_TRANSFER, FIRST_TRANSFER_ID_PERSIST)
                        .param(TransferController.PARAM_CLIENT_ID, FIRST_CLIENT_ID_PERSIST.toString()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("If deleteIdDraftTransfer wasn't successful then return status BAD REQUEST")
    void deleteIdDraftTransfer_shouldReturnBadRequest() throws Exception {
        //ACT & VERIFY
        mockMvc.perform(delete(TransferController.URL_TRANSFER +
                        TransferController.URL_DELETE_TRANSFER, TRANSFER_ID_NOT_PERSIST)
                        .param(TransferController.PARAM_CLIENT_ID, FIRST_CLIENT_ID_PERSIST.toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("If getFavoriteTransfers was successful then return IsFavoriteTransferDto")
    void getFavoriteTransfers_shouldReturnIsFavoriteTransferDto() throws Exception {
        //ACT
        MvcResult result = mockMvc.perform(patch(TransferController.URL_TRANSFER +
                        TransferController.URL_FAVORITES)
                        .param(TransferController.PARAM_CLIENT_ID, FIRST_CLIENT_ID_PERSIST.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferOrderIdDtoRequest)))
                .andExpect(status().isOk())
                .andReturn();
        //VERIFY
        assertThat(result.getResponse().getContentAsString())
                .isEqualTo(objectMapper.writeValueAsString(isFavoriteTransferDtoResponseExpect));
    }

    @Test
    @DisplayName("If get getFavoriteTransfers wasn't successful then return status BAD REQUEST")
    void getFavoriteTransfers_shouldReturnBadRequest() throws Exception {
        //ACT & VERIFY
        mockMvc.perform(patch(TransferController.URL_TRANSFER +
                        TransferController.URL_FAVORITES)
                        .param(TransferController.PARAM_CLIENT_ID, CLIENT_ID_NOT_PERSIST.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferOrderIdDtoRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("If getFavoriteTransfers was successful then return TransferDto")
    void getFavoriteTransfers_shouldReturnTransferDto() throws Exception {
        //ACT
        MvcResult result = mockMvc.perform(get(TransferController.URL_TRANSFER +
                        TransferController.URL_FAVORITES_TRANSFER, SECOND_TRANSFER_ID_PERSIST)
                        .param(TransferController.PARAM_CLIENT_ID, SECOND_CLIENT_ID_PERSIST.toString()))
                .andExpect(status().isOk())
                .andReturn();
        //VERIFY
        assertThat(result.getResponse().getContentAsString())
                .isEqualTo(objectMapper.writeValueAsString(transferDtoResponseExpect));
    }

    @Test
    @DisplayName("If getFavoriteTransfers wasn't successful then return status BAD REQUEST")
    void getFavoriteTransfers_shouldReturnBandRequest() throws Exception {
        //ACT & VERIFY
        mockMvc.perform(get(TransferController.URL_TRANSFER +
                        TransferController.URL_FAVORITES_TRANSFER, TRANSFER_ID_NOT_PERSIST)
                        .param(TransferController.PARAM_CLIENT_ID, CLIENT_ID_NOT_PERSIST.toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("if getTransferType was successful then return List<TransferTypeName>")
    void getTransferType_shouldReturnListOfTransferTypeName() throws Exception {
        //ACT
        MvcResult result = mockMvc.perform(get(TransferController.URL_TRANSFER +
                        TransferController.URL_TRANSFER_TYPE))
                .andExpect(status().isOk())
                .andReturn();
        //VERIFY
        assertThat(result.getResponse().getContentAsString())
                .isEqualTo(objectMapper.writeValueAsString(transferTypeListResponseExpect));
    }

    @Test
    @DisplayName("If getPaymentType was successful then return List<TransferTypeName>")
    void getPaymentType_shouldReturnListOfTransferTypeName() throws Exception {
        //ACT
        MvcResult result = mockMvc.perform(get(TransferController.URL_TRANSFER +
                        TransferController.URL_PAYMENT_TYPE))
                .andExpect(status().isOk())
                .andReturn();
        //VERIFY
        assertThat(result.getResponse().getContentAsString())
                .isEqualTo(objectMapper.writeValueAsString(paymentTypeListResponseExpect));
    }
}
