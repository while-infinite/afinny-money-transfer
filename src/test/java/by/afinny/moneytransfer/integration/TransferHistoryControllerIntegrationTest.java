package by.afinny.moneytransfer.integration;

import by.afinny.moneytransfer.controller.TransferHistoryController;
import by.afinny.moneytransfer.dto.CardNumberDto;
import by.afinny.moneytransfer.dto.CreditCardStatementDto;
import by.afinny.moneytransfer.dto.DebitCardStatementDto;
import by.afinny.moneytransfer.dto.DetailsHistoryDto;
import by.afinny.moneytransfer.dto.TransferOrderHistoryDto;
import by.afinny.moneytransfer.entity.Payee;
import by.afinny.moneytransfer.entity.TransferOrder;
import by.afinny.moneytransfer.entity.TransferType;
import by.afinny.moneytransfer.entity.constant.CurrencyCode;
import by.afinny.moneytransfer.entity.constant.OperationType;
import by.afinny.moneytransfer.entity.constant.PayeeType;
import by.afinny.moneytransfer.entity.constant.TransferPeriodicity;
import by.afinny.moneytransfer.entity.constant.TransferStatus;
import by.afinny.moneytransfer.entity.constant.TransferTypeName;
import by.afinny.moneytransfer.openfeign.credit.CreditCardClient;
import by.afinny.moneytransfer.openfeign.deposit.DepositDebitCardStatementClient;
import by.afinny.moneytransfer.repository.PayeeRepository;
import by.afinny.moneytransfer.repository.TransferOrderRepository;
import by.afinny.moneytransfer.repository.TransferTypeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import javax.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
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
public class TransferHistoryControllerIntegrationTest {

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

    @MockBean
    private CreditCardClient creditCardClient;
    @MockBean
    private DepositDebitCardStatementClient depositDebitCardStatementClient;

    private Payee payee;
    private TransferType transferType;
    private TransferOrder transferOrder;

    private List<TransferOrderHistoryDto> transferOrderHistoryDtoListExpect;
    private List<CreditCardStatementDto> creditCardStatementDtoListExpect;
    private CardNumberDto cardNumberDto;
    private List<DebitCardStatementDto> debitCardStatementDtoListExpect;
    private DetailsHistoryDto detailsHistoryDtoExpect;

    private final UUID CLIENT_ID_PERSIST = UUID.randomUUID();
    private final UUID CLIENT_ID_NOT_PERSIST = UUID.randomUUID();
    private final UUID CARD_ID = UUID.randomUUID();
    private final LocalDateTime NOW = LocalDateTime.parse("2023-03-28 00:00:00",
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    private final String FROM = "2023-03-27 00:00:00";
    private final String TO = "2023-03-29 00:00:00";
    private final Integer PAGE_NUMBER = 0;
    private final Integer PAGE_SIZE = 1;
    private final String REMITTED_CARD_NUMBER = "1234567812345678";
    private UUID TRANSFER_ORDER_ID;

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

        transferOrder = TransferOrder.builder()
                .createdAt(NOW)
                .transferType(transferType)
                .purpose("purpose")
                .remitterCardNumber(REMITTED_CARD_NUMBER)
                .payee(payee)
                .sum(new BigDecimal("5.0000"))
                .completedAt(NOW)
                .transferStatus(TransferStatus.PERFORMED)
                .authorizationCode("1256")
                .currencyExchange(new BigDecimal("500.0000"))
                .isFavorite(true)
                .startDate(NOW)
                .transferPeriodicity(TransferPeriodicity.MONTHLY)
                .clientId(CLIENT_ID_PERSIST)
                .operationType(OperationType.EXPENSE)
                .build();

        payeeRepository.save(payee);
        transferTypeRepository.save(transferType);
        transferOrderRepository.save(transferOrder);

        TRANSFER_ORDER_ID = transferOrder.getId();

        transferOrderHistoryDtoListExpect = Collections.singletonList(
                TransferOrderHistoryDto.builder()
                        .transferOrderId(transferOrder.getId())
                        .createdAt(transferOrder.getCreatedAt())
                        .purpose(transferOrder.getPurpose())
                        .payeeId(payee.getId())
                        .sum(transferOrder.getSum())
                        .completedAt(transferOrder.getCompletedAt())
                        .transferStatus(transferOrder.getTransferStatus())
                        .transferTypeName(transferType.getTransferTypeName())
                        .currencyCode(transferType.getCurrencyCode())
                        .remitterCardNumber(transferOrder.getRemitterCardNumber())
                        .name(payee.getName())
                        .build());

        creditCardStatementDtoListExpect = Collections.singletonList(
                CreditCardStatementDto.builder()
                        .payeeId(payee.getId())
                        .completedAt(transferOrder.getCompletedAt())
                        .transferOrderId(transferOrder.getId())
                        .sum(transferOrder.getSum())
                        .purpose(transferOrder.getPurpose())
                        .typeName(transferType.getTransferTypeName())
                        .currencyCode(transferType.getCurrencyCode())
                        .build());

        cardNumberDto = CardNumberDto.builder()
                .cardNumber(transferOrder.getRemitterCardNumber())
                .build();

        debitCardStatementDtoListExpect = Collections.singletonList(
                DebitCardStatementDto.builder()
                        .payeeId(payee.getId())
                        .completedAt(transferOrder.getCompletedAt())
                        .transferOrderId(transferOrder.getId())
                        .sum(transferOrder.getSum())
                        .purpose(transferOrder.getPurpose())
                        .typeName(transferType.getTransferTypeName())
                        .currencyCode(transferType.getCurrencyCode())
                        .build());

        detailsHistoryDtoExpect = DetailsHistoryDto.builder()
                .createdAt(transferOrder.getCreatedAt())
                .remitterCardNumber(transferOrder.getRemitterCardNumber())
                .sumCommission(transferOrder.getSumCommission())
                .currencyExchange(transferOrder.getCurrencyExchange())
                .payeeType(payee.getPayeeType())
                .name(payee.getName())
                .inn(payee.getInn())
                .bic(payee.getBic())
                .payeeAccountNumber(payee.getPayeeAccountNumber())
                .payeeCardNumber(payee.getPayeeCardNumber())
                .build();
    }

    @Test
    @DisplayName("If getTransferOrderHistory was successful then return List<TransferOrderHistoryDto>")
    void getTransferOrderHistory_shouldReturnListOfTransferOrderHistoryDto() throws Exception {
        //ACT
        MvcResult result = mockMvc.perform(get(TransferHistoryController.URL_HISTORY)
                        .param(TransferHistoryController.PARAM_CLIENT_ID, CLIENT_ID_PERSIST.toString())
                        .param(TransferHistoryController.PARAM_PAGE_NUMBER, PAGE_NUMBER.toString())
                        .param(TransferHistoryController.PARAM_PAGE_SIZE, PAGE_SIZE.toString()))
                .andExpect(status().isOk())
                .andReturn();
        //VERIFY
        assertThat(result.getResponse().getContentAsString())
                .isEqualTo(objectMapper.writeValueAsString(transferOrderHistoryDtoListExpect));
    }

    @Test
    @DisplayName("If getTransferOrderHistory wasn't successful then return status BAD REQUEST")
    void getTransferOrderHistory_shouldReturnBadRequest() throws Exception {
        //ACT & VERIFY
        mockMvc.perform(get(TransferHistoryController.URL_HISTORY)
                        .param(TransferHistoryController.PARAM_CLIENT_ID, CLIENT_ID_NOT_PERSIST.toString())
                        .param(TransferHistoryController.PARAM_PAGE_NUMBER, PAGE_NUMBER.toString())
                        .param(TransferHistoryController.PARAM_PAGE_SIZE, PAGE_SIZE.toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("If getCreditCardStatement was successful then return List<CreditCardStatementDto>")
    void getCreditCardStatement_shouldReturnListOfCreditCardStatementDto() throws Exception {
        //ARRANGE
        when(creditCardClient.getCardNumber(CARD_ID))
                .thenReturn(ResponseEntity.ok(REMITTED_CARD_NUMBER));
        //ACT
        MvcResult result = mockMvc.perform(get(TransferHistoryController.URL_HISTORY +
                        TransferHistoryController.URL_CREDIT, CARD_ID)
                        .param(TransferHistoryController.PARAM_CLIENT_ID, CLIENT_ID_PERSIST.toString())
                        .param(TransferHistoryController.PARAM_FROM, FROM)
                        .param(TransferHistoryController.PARAM_TO, TO)
                        .param(TransferHistoryController.PARAM_PAGE_NUMBER, PAGE_NUMBER.toString())
                        .param(TransferHistoryController.PARAM_PAGE_SIZE, PAGE_SIZE.toString()))
                .andExpect(status().isOk())
                .andReturn();
        //VERIFY
        assertThat(result.getResponse().getContentAsString())
                .isEqualTo(objectMapper.writeValueAsString(creditCardStatementDtoListExpect));
    }

    @Test
    @DisplayName("If getCreditCardStatement wasn't successful then return status BAD REQUEST")
    void getCreditCardStatement_shouldReturnBadRequest() throws Exception {
        //ARRANGE
        when(creditCardClient.getCardNumber(CARD_ID))
                .thenThrow(new EntityNotFoundException());
        //ACT & VERIFY
        mockMvc.perform(get(TransferHistoryController.URL_HISTORY +
                        TransferHistoryController.URL_CREDIT, CARD_ID)
                        .param(TransferHistoryController.PARAM_CLIENT_ID, CLIENT_ID_NOT_PERSIST.toString())
                        .param(TransferHistoryController.PARAM_FROM, FROM)
                        .param(TransferHistoryController.PARAM_TO, TO)
                        .param(TransferHistoryController.PARAM_PAGE_NUMBER, PAGE_NUMBER.toString())
                        .param(TransferHistoryController.PARAM_PAGE_SIZE, PAGE_SIZE.toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("If getViewDebitCardStatement was successful then return List<DebitCardStatementDto>")
    void getViewDebitCardStatement_shouldReturnListOfDebitCardStatementDto() throws Exception {
        //ARRANGE
        when(depositDebitCardStatementClient.getCardNumberByCardId(CARD_ID))
                .thenReturn(ResponseEntity.ok(cardNumberDto));
        //ACT
        MvcResult result = mockMvc.perform(get(TransferHistoryController.URL_HISTORY +
                        TransferHistoryController.URL_DEPOSIT, CARD_ID)
                        .param(TransferHistoryController.PARAM_CLIENT_ID, CLIENT_ID_PERSIST.toString())
                        .param(TransferHistoryController.PARAM_FROM, FROM)
                        .param(TransferHistoryController.PARAM_TO, TO)
                        .param(TransferHistoryController.PARAM_PAGE_NUMBER, PAGE_NUMBER.toString())
                        .param(TransferHistoryController.PARAM_PAGE_SIZE, PAGE_SIZE.toString()))
                .andExpect(status().isOk())
                .andReturn();
        //VERIFY
        assertThat(result.getResponse().getContentAsString())
                .isEqualTo(objectMapper.writeValueAsString(debitCardStatementDtoListExpect));
    }

    @Test
    @DisplayName("If getViewDebitCardStatement wasn't successful then return status BAD REQUEST")
    void getViewDebitCardStatement_shouldReturnBadRequest() throws Exception {
        //ARRANGE
        when(depositDebitCardStatementClient.getCardNumberByCardId(CARD_ID))
                .thenThrow(new EntityNotFoundException());
        //ACT & VERIFY
        mockMvc.perform(get(TransferHistoryController.URL_HISTORY +
                        TransferHistoryController.URL_DEPOSIT, CARD_ID)
                        .param(TransferHistoryController.PARAM_CLIENT_ID, CLIENT_ID_NOT_PERSIST.toString())
                        .param(TransferHistoryController.PARAM_FROM, FROM)
                        .param(TransferHistoryController.PARAM_TO, TO)
                        .param(TransferHistoryController.PARAM_PAGE_NUMBER, PAGE_NUMBER.toString())
                        .param(TransferHistoryController.PARAM_PAGE_SIZE, PAGE_SIZE.toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("If getDetailsHistory was successful then return DetailsHistoryDto")
    void getDetailsHistory_shouldReturnDetailsHistoryDto() throws Exception {
        //ACT
        MvcResult result = mockMvc.perform(get(TransferHistoryController.URL_HISTORY +
                        TransferHistoryController.URL_DETAILS)
                        .param(TransferHistoryController.PARAM_CLIENT_ID, CLIENT_ID_PERSIST.toString())
                        .param(TransferHistoryController.PARAM_TRANSFER_ORDER_ID, TRANSFER_ORDER_ID.toString()))
                .andExpect(status().isOk())
                .andReturn();
        //VERIFY
        assertThat(result.getResponse().getContentAsString())
                .isEqualTo(objectMapper.writeValueAsString(detailsHistoryDtoExpect));
    }

    @Test
    @DisplayName("If getDetailsHistory wasn't successful then return status BAD REQUEST")
    void getDetailsHistory_shouldReturnBadRequest() throws Exception {
        //ACT & VERIFY
        mockMvc.perform(get(TransferHistoryController.URL_HISTORY +
                        TransferHistoryController.URL_DETAILS)
                        .param(TransferHistoryController.PARAM_CLIENT_ID, CLIENT_ID_NOT_PERSIST.toString())
                        .param(TransferHistoryController.PARAM_TRANSFER_ORDER_ID, TRANSFER_ORDER_ID.toString()))
                .andExpect(status().isBadRequest());
    }
}