package by.afinny.moneytransfer.controller;

import by.afinny.moneytransfer.dto.CreditCardStatementDto;
import by.afinny.moneytransfer.dto.DebitCardStatementDto;
import by.afinny.moneytransfer.dto.DetailsHistoryDto;
import by.afinny.moneytransfer.dto.FilterOptionsDto;
import by.afinny.moneytransfer.dto.TransferOrderHistoryDto;
import by.afinny.moneytransfer.entity.constant.CurrencyCode;
import by.afinny.moneytransfer.entity.constant.PayeeType;
import by.afinny.moneytransfer.entity.constant.TransferStatus;
import by.afinny.moneytransfer.entity.constant.TransferTypeName;
import by.afinny.moneytransfer.service.TransferService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import javax.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransferHistoryController.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
public class TransferHistoryControllerTest {

    @MockBean
    TransferService transferService;

    @Autowired
    private MockMvc mockMvc;

    private List<TransferOrderHistoryDto> transferOrderHistoryDto = new ArrayList<>();
    private List<CreditCardStatementDto> creditCardStatementDto = new ArrayList<>();
    private List<DebitCardStatementDto> debitCardStatementDto = new ArrayList<>();
    private DetailsHistoryDto detailsHistoryDto;
    private FilterOptionsDto filterOptionsDto;
    private final UUID TRANSFER_ID = UUID.randomUUID();
    private final UUID CLIENT_ID = UUID.randomUUID();
    private final UUID CARD_ID = UUID.randomUUID();
    private final String FROM = "2023-03-27 00:00:00";
    private final String TO = "2023-03-28 00:00:00";
    private final String FORMAT_DATE = "yyyy-MM-dd";
    private final String REMITTER_CARD_NUMBER = "1111111";
    private final String PAYEE_NAME = "XVZ";
    private final Integer pageNumber = 0;
    private final Integer pageSize = 5;

    @BeforeAll
    void setUp() {
        transferOrderHistoryDto.add(TransferOrderHistoryDto.builder()
                .transferOrderId(TRANSFER_ID)
                .completedAt(LocalDateTime.now())
                .sum(new BigDecimal("5.0"))
                .payeeId(UUID.fromString("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11"))
                .name(PAYEE_NAME)
                .transferStatus(TransferStatus.DRAFT)
                .transferTypeName(TransferTypeName.BETWEEN_CARDS)
                .currencyCode(CurrencyCode.RUB)
                .purpose("XVZ")
                .remitterCardNumber(REMITTER_CARD_NUMBER)
                .createdAt(LocalDateTime.now())
                .build());

        detailsHistoryDto = DetailsHistoryDto.builder()
                .createdAt(LocalDateTime.now())
                .remitterCardNumber("1234567898765432")
                .sumCommission(BigDecimal.ONE)
                .currencyExchange(BigDecimal.ONE)
                .payeeType(PayeeType.INDIVIDUALS)
                .name("RUB")
                .inn("123456789009")
                .bic("123456789")
                .payeeAccountNumber("a0eebc999")
                .payeeCardNumber("9876543210")
                .build();

        creditCardStatementDto.add(CreditCardStatementDto.builder()
                .transferOrderId(UUID.randomUUID())
                .completedAt(LocalDateTime.now())
                .payeeId(UUID.randomUUID())
                .sum(BigDecimal.TEN)
                .purpose("purpose")
                .typeName(TransferTypeName.BY_PAYEE_DETAILS)
                .currencyCode(CurrencyCode.USD)
                .build());

        debitCardStatementDto.add(DebitCardStatementDto.builder()
                .transferOrderId(UUID.randomUUID())
                .completedAt(LocalDateTime.now())
                .payeeId(UUID.randomUUID())
                .sum(BigDecimal.TEN)
                .purpose("purpose")
                .typeName(TransferTypeName.BY_PAYEE_DETAILS)
                .currencyCode(CurrencyCode.USD)
                .build());

        filterOptionsDto = FilterOptionsDto.builder().
                clientId(TRANSFER_ID).
                pageNumber(pageNumber).
                pageSize(pageSize).
                build();
    }

    @Test
    @DisplayName("If transfer order history was successfully then return List<TransferOrderHistoryDto>")
    void transferOrderHistory_shouldReturnList() throws Exception {
        //ARRANGE
        when(transferService.getTransferOrderHistory(any()))
                .thenReturn(transferOrderHistoryDto);

        //ACT
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(TransferHistoryController.URL_HISTORY)
                        .param("clientId", TRANSFER_ID.toString())
                        .param("pageNumber", pageNumber.toString())
                        .param("pageSize", pageSize.toString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        //VERIFY
        verifyBody(result.getResponse().getContentAsString(), asJsonString(transferOrderHistoryDto));
    }

    @Test
    @DisplayName("If transfer order history wasn't successfully then return status BAD REQUEST")
    void transferOrderHistory_ifNotSuccess_thenStatus400() throws Exception {
        //ARRANGE
        when(transferService.getTransferOrderHistory(any()))
                .thenThrow(EntityNotFoundException.class);

        //ACT & VERIFY
        mockMvc.perform(MockMvcRequestBuilders.get(TransferHistoryController.URL_HISTORY)
                        .param("clientId", TRANSFER_ID.toString())
                        .param("pageNumber", pageNumber.toString())
                        .param("pageSize", pageSize.toString()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn();
    }

    @Test
    @DisplayName("If transfer order was successfully then return List<CreditCardStatementDto>")
    void getCreditCardStatement_shouldReturnList() throws Exception {
        //ARRANGE
        when(transferService.getCreditCardStatement(CARD_ID,CLIENT_ID, FROM, TO, pageNumber, pageSize))
                .thenReturn(creditCardStatementDto);

        //ACT
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(TransferHistoryController.URL_HISTORY + TransferHistoryController.URL_CREDIT, CARD_ID)
                        .param(TransferHistoryController.PARAM_CLIENT_ID,CLIENT_ID.toString())
                        .param(TransferHistoryController.PARAM_FROM, FROM.toString())
                        .param(TransferHistoryController.PARAM_TO, TO.toString())
                        .param(TransferHistoryController.PARAM_PAGE_NUMBER, pageNumber.toString())
                        .param(TransferHistoryController.PARAM_PAGE_SIZE, pageSize.toString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        //VERIFY
        verifyBody(result.getResponse().getContentAsString(), asJsonString(creditCardStatementDto));
    }

    @Test
    @DisplayName("If transfer order wasn't successfully then return status BAD REQUEST")
    void getCreditCardStatement_ifNotSuccess_thenStatus400() throws Exception {
        //ARRANGE
        when(transferService.getCreditCardStatement(CARD_ID,CLIENT_ID, FROM, TO, pageNumber, pageSize))
                .thenThrow(EntityNotFoundException.class);

        //ACT & VERIFY
        mockMvc.perform(MockMvcRequestBuilders.get(TransferHistoryController.URL_HISTORY + TransferHistoryController.URL_CREDIT, CARD_ID)
                        .param(TransferHistoryController.PARAM_CLIENT_ID,CLIENT_ID.toString())
                        .param(TransferHistoryController.PARAM_FROM, FROM.toString())
                        .param(TransferHistoryController.PARAM_TO, TO.toString())
                        .param(TransferHistoryController.PARAM_PAGE_NUMBER, pageNumber.toString())
                        .param(TransferHistoryController.PARAM_PAGE_SIZE, pageSize.toString()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn();
    }

    @Test
    @DisplayName("If the transfer order details history was successfully received then return status OK")
    void getDetailsHistory_shouldReturnDetailsHistory() throws Exception {
        //ARRANGE
        when(transferService.getDetailsHistory(any(UUID.class), any(UUID.class))).thenReturn(detailsHistoryDto);

        //ACT
        MvcResult result = mockMvc.perform(get(TransferHistoryController.URL_HISTORY
                        + TransferHistoryController.URL_DETAILS)
                        .param("clientId", CLIENT_ID.toString())
                        .param("transferOrderId", TRANSFER_ID.toString()))
                .andExpect(status().isOk()).andReturn();

        //VERIFY
        verifyBody(asJsonString(detailsHistoryDto), result.getResponse().getContentAsString());
    }

    @Test
    @DisplayName("if the transfer order details history was not successfully found then return status INTERNAL_SERVER_ERROR")
    void getDetailsHistory_ifNotFoundDetailsHistory_then500_INTERNAL_SERVER_ERROR() throws Exception {
        //ARRANGE
        when(transferService.getDetailsHistory(any(UUID.class), any(UUID.class))).thenThrow(RuntimeException.class);

        //ACT & VERIFY
        mockMvc.perform(get(TransferHistoryController.URL_HISTORY
                        + TransferHistoryController.URL_DETAILS, TRANSFER_ID.toString()))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("If debit card statement was successfully then return List<TransferOrderHistoryDto>")
    void viewDebitCardStatement_shouldReturnList() throws Exception {
        //ARRANGE
        when(transferService.getViewDebitCardStatement(TRANSFER_ID,CLIENT_ID, FROM, TO, pageNumber, pageSize))
                .thenReturn(debitCardStatementDto);

        //ACT
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(TransferHistoryController.URL_HISTORY
                                + TransferHistoryController.URL_DEPOSIT, TRANSFER_ID)
                        .param(TransferHistoryController.PARAM_CLIENT_ID,CLIENT_ID.toString())
                        .param(TransferHistoryController.PARAM_FROM, FROM.toString())
                        .param(TransferHistoryController.PARAM_TO, TO.toString())
                        .param(TransferHistoryController.PARAM_PAGE_NUMBER, pageNumber.toString())
                        .param(TransferHistoryController.PARAM_PAGE_SIZE, pageSize.toString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        //VERIFY
        verifyBody(result.getResponse().getContentAsString(), asJsonString(debitCardStatementDto));
    }

    @Test
    @DisplayName("If debit card statement wasn't successfully then return status BAD REQUEST")
    void viewDebitCardStatement_ifNotSuccess_thenStatus400() throws Exception {
        //ARRANGE
        when(transferService.getViewDebitCardStatement(TRANSFER_ID,CLIENT_ID, FROM, TO, pageNumber, pageSize))
                .thenThrow(EntityNotFoundException.class);

        //ACT & VERIFY
        mockMvc.perform(MockMvcRequestBuilders.get(TransferHistoryController.URL_HISTORY
                                + TransferHistoryController.URL_DEPOSIT, TRANSFER_ID)
                        .param(TransferHistoryController.PARAM_CLIENT_ID,CLIENT_ID.toString())
                        .param(TransferHistoryController.PARAM_FROM, FROM.toString())
                        .param(TransferHistoryController.PARAM_TO, TO.toString())
                        .param(TransferHistoryController.PARAM_PAGE_NUMBER, pageNumber.toString())
                        .param(TransferHistoryController.PARAM_PAGE_SIZE, pageSize.toString()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn();
    }

    private void verifyBody(String expectedBody, String actualBody) {
        assertThat(actualBody).isEqualTo(expectedBody);
    }

    private String asJsonString(Object obj) throws JsonProcessingException {
        return new ObjectMapper()
                .setDateFormat(new SimpleDateFormat(FORMAT_DATE))
                .registerModule(new JavaTimeModule()).writeValueAsString(obj);
    }
}
