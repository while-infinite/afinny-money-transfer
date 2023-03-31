package by.afinny.moneytransfer.controller;

import by.afinny.moneytransfer.dto.ChangeStatusRequestDto;
import by.afinny.moneytransfer.dto.ChangeStatusResponseDto;
import by.afinny.moneytransfer.dto.CreatePaymentDto;
import by.afinny.moneytransfer.dto.CreatePaymentResponseDto;
import by.afinny.moneytransfer.dto.IsFavoriteTransferDto;
import by.afinny.moneytransfer.dto.RequestRefillBrokerageAccountDto;
import by.afinny.moneytransfer.dto.ResponseBrokerageAccountDto;
import by.afinny.moneytransfer.dto.TransferDto;
import by.afinny.moneytransfer.dto.TransferOrderIdDto;
import by.afinny.moneytransfer.entity.TransferOrder;
import by.afinny.moneytransfer.entity.constant.TransferStatus;
import by.afinny.moneytransfer.entity.constant.TransferTypeName;
import by.afinny.moneytransfer.exception.handler.ExceptionHandlerController;
import by.afinny.moneytransfer.service.TransferService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static by.afinny.moneytransfer.entity.constant.TransferStatus.IN_PROGRESS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransferController.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
public class TransferControllerTest {

    @MockBean
    private TransferService transferService;

    private MockMvc mockMvc;
    private TransferOrder transferOrder;
    private IsFavoriteTransferDto isFavoriteTransferDto;
    private TransferOrderIdDto transferOrderIdDto;
    private TransferDto transferDto;
    private List<TransferTypeName> typeNameList;
    private List<TransferTypeName> paymentTypeNameList;
    private ResponseBrokerageAccountDto responseBrokerageAccountDto;
    private RequestRefillBrokerageAccountDto requestRefillBrokerageAccountDto;
    private CreatePaymentResponseDto createPaymentResponseDto;
    private final UUID OPERATION_ID = UUID.randomUUID();
    private final UUID TRANSFER_ID = UUID.randomUUID();
    public static final String URL_CREATE_PAYMENT_OR_TRANSFER = "/auth/payments/new-payment";
    private ChangeStatusResponseDto changeStatusResponseDto;
    private ChangeStatusRequestDto changeStatusRequestDto;

    @BeforeAll
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new TransferController(transferService))
                .setControllerAdvice(ExceptionHandlerController.class).build();

        transferOrder = TransferOrder.builder()
                .id(OPERATION_ID)
                .build();

        isFavoriteTransferDto = IsFavoriteTransferDto.builder()
                .isFavorite(true)
                .build();

        transferOrderIdDto = TransferOrderIdDto.builder()
                .transferOrderId(UUID.randomUUID())
                .build();

        transferDto = TransferDto.builder()
                .transferTypeId(1)
                .purpose("food")
                .payeeId(UUID.fromString("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11"))
                .sum(new BigDecimal("123.5"))
                .isFavorite(true)
                .build();

        typeNameList = List.of(TransferTypeName.BETWEEN_CARDS,
                TransferTypeName.TO_ANOTHER_CARD,
                TransferTypeName.BY_PHONE_NUMBER,
                TransferTypeName.BY_PAYEE_DETAILS);

        paymentTypeNameList = List.of(TransferTypeName.FAVORITES,
                TransferTypeName.AUTOPAYMENTS,
                TransferTypeName.BANKING_SERVICES,
                TransferTypeName.INFO_SERVISES,
                TransferTypeName.PAYMENT_FOR_SERVICES,
                TransferTypeName.UTILITIES,
                TransferTypeName.OTHER_PAYMENTS);

        responseBrokerageAccountDto = ResponseBrokerageAccountDto.builder()
                .id(UUID.fromString("27d9f248-f99a-4526-bca7-ed6b5984842f"))
                .brokerageId(UUID.randomUUID())
                .authorizationCode("authorizationCode")
                .createdAt(LocalDateTime.now())
                .firstName("firstName")
                .lastName("lastName")
                .middleName("middleName")
                .sumCommission(new BigDecimal("100.00"))
                .remitterCardNumber("remittedCardNumber")
                .status(TransferStatus.DRAFT)
                .transferTypeId(1)
                .isFavorite(true)
                .build();

        requestRefillBrokerageAccountDto = RequestRefillBrokerageAccountDto.builder()
                .brokerageId(UUID.randomUUID())
                .remitterCardNumber("remitterCardNumber")
                .transferTypeId(1)
                .sum(new BigDecimal("10.00"))
                .build();

        createPaymentResponseDto = CreatePaymentResponseDto.builder()
                .id(UUID.randomUUID().toString())
                .created_at(LocalDateTime.now().toString())
                .status(TransferStatus.PERFORMED.name())
                .card_number("0000111100001111")
                .transfer_type_id("1")
                .sum(BigDecimal.valueOf(1000.0).toString())
                .remitter_card_number("0000111100001111")
                .name("name")
                .payee_account_number(UUID.randomUUID().toString())
                .payee_card_number("0000111100001111")
                .sum_commission(BigDecimal.valueOf(0.0).toString())
                .authorizationCode("1111")
                .currencyExchange(BigDecimal.valueOf(1.0).toString())
                .purpose("purpose")
                .inn("inn")
                .bic("bic")
                .build();

        changeStatusResponseDto = ChangeStatusResponseDto.builder()
                .transferId(TRANSFER_ID)
                .status(IN_PROGRESS)
                .build();

        changeStatusRequestDto = ChangeStatusRequestDto.builder()
                .transferId(TRANSFER_ID)
                .status(IN_PROGRESS)
                .build();
    }

    @Test
    @DisplayName("If draft transfer was successfully delete then return status No_Content")
    void deleteDraftTransferOrder_ifSuccessfullyDeleted_then204_NO_CONTENT() throws Exception {
        //ARRANGE
        ArgumentCaptor<UUID> operationIdCaptor = ArgumentCaptor.forClass(UUID.class);

        //ACT
        ResultActions perform = mockMvc.perform(
                delete(TransferController.URL_TRANSFER +
                        TransferController.URL_DELETE_TRANSFER, OPERATION_ID)
                        .param("clientId", OPERATION_ID.toString()));

        //VERIFY
        perform.andExpect(status().isNoContent());
        verify(transferService, times(1)).deleteIdDraftTransferOrder(operationIdCaptor.capture(), operationIdCaptor.capture());
        assertThat(OPERATION_ID).isEqualTo(operationIdCaptor.getValue());
    }

    @Test
    @DisplayName("If draft transfer wasn't successfully delete then return status BAD_REQUEST")
    void deleteDraftTransferOrder_ifNotDeleted_then400_BAD_REQUEST() throws Exception {
        //ARRANGE
        doThrow(EntityNotFoundException.class).when(transferService).deleteIdDraftTransferOrder(any(UUID.class), any(UUID.class));

        //ACT
        ResultActions perform = mockMvc.perform(
                delete(TransferController.URL_TRANSFER +
                        TransferController.URL_DELETE_TRANSFER, OPERATION_ID)
                        .param("clientId", OPERATION_ID.toString()));

        //VERIFY
        perform.andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("If the favorite transfer order was successfully received, return the OK status")
    void getTransferOrder_ifSuccess_thenStatus200() throws Exception {
        //ARRANGE
        when(transferService.getFavoriteTransferOrder(any(UUID.class), any(UUID.class))).thenReturn(isFavoriteTransferDto);

        //ACT
        ResultActions perform = mockMvc.perform(
                        patch(TransferController.URL_TRANSFER + TransferController.URL_FAVORITES)
                                .param("clientId", OPERATION_ID.toString())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(transferOrderIdDto)))
                .andExpect(status().isOk());

        //VERIFY
        verifyBody(perform.andReturn().getResponse().getContentAsString(), asJsonString(isFavoriteTransferDto));
    }

    @Test
    @DisplayName("If changing status wasn't successfully received then return Internal Server Error")
    void getTransferOrder_ifNotSuccess_thenStatus500() throws Exception {
        //ARRANGE
        doThrow(new EntityNotFoundException("Server Error")).when(transferService)
                .getFavoriteTransferOrder(any(UUID.class), any(UUID.class));

        //ACT & VERIFY
        mockMvc.perform(
                        patch(TransferController.URL_TRANSFER + TransferController.URL_FAVORITES)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(TRANSFER_ID)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("if transfer was successfully found by ID then return status OK")
    void getFavoriteTransfers_shouldReturnTransferInfo() throws Exception {
        //ARRANGE
        when(transferService.getFavoriteTransfer(any(UUID.class), any(UUID.class))).thenReturn(transferDto);

        //ACT
        MvcResult result = mockMvc.perform(get("/auth/payments/favorites/{transferOrderId}", TRANSFER_ID.toString())
                        .param("clientId", OPERATION_ID.toString()))
                .andExpect(status().isOk())
                .andReturn();

        //VERIFY
        verifyBody(asJsonString(transferDto), result.getResponse().getContentAsString());
    }

    @Test
    @DisplayName("if transfer wasn't successfully found by ID then return status INTERNAL_SERVER_ERROR")
    void getFavoriteTransfers_ifNotFound_then500_INTERNAL_SERVER_ERROR() throws Exception {
        //ARRANGE
        when(transferService.getFavoriteTransfer(any(UUID.class), any(UUID.class))).thenThrow(RuntimeException.class);

        //ACT & VERIFY
        mockMvc.perform(
                        get("/auth/payments/favorites/{transferOrderId}", TRANSFER_ID.toString()))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("if transfer type name was found then return status OK")
    void getTransferType_shouldReturnStatusOK() throws Exception {
        //ARRANGE
        when(transferService.getTransferType()).thenReturn(typeNameList);

        //ACT
        MvcResult result = mockMvc.perform(
                        get(TransferController.URL_TRANSFER + TransferController.URL_TRANSFER_TYPE))
                .andExpect(status().isOk()).andReturn();

        //VERIFY
        verifyBody(asJsonString(typeNameList), result.getResponse().getContentAsString());
    }

    @Test
    @DisplayName("If transfer type name wasn't successfully then return status BAD REQUEST")
    void getTransferType_ifNotSuccess_then_BAD_REQUEST() throws Exception {
        //ARRANGE
        when(transferService.getTransferType()).thenThrow(new RuntimeException());

        //ACT & VERIFY
        mockMvc.perform(get(TransferController.URL_TRANSFER + TransferController.URL_TRANSFER_TYPE))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("if payment type name was found then return status OK")
    void getPaymentType_shouldReturnStatusOK() throws Exception {
        //ARRANGE
        when(transferService.getPaymentType()).thenReturn(paymentTypeNameList);

        //ACT
        MvcResult result = mockMvc.perform(
                        get(TransferController.URL_TRANSFER + TransferController.URL_PAYMENT_TYPE))
                .andExpect(status().isOk()).andReturn();

        //VERIFY
        verifyBody(asJsonString(paymentTypeNameList), result.getResponse().getContentAsString());
    }

    @Test
    @DisplayName("If payment type name wasn't successfully then return status BAD REQUEST")
    void getPaymentType_ifNotSuccess_then_BAD_REQUEST() throws Exception {
        //ARRANGE
        when(transferService.getPaymentType()).thenThrow(new RuntimeException());

        //ACT & VERIFY
        mockMvc.perform(get(TransferController.URL_TRANSFER + TransferController.URL_PAYMENT_TYPE))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("if refillBrokerageAccount was successfully then return status OK")
    void refillBrokerageAccount_shouldReturnStatusOK() throws Exception {
        //ARRANGE
        when(transferService.refillBrokerageAccount(any(UUID.class), any(RequestRefillBrokerageAccountDto.class)))
                .thenReturn(responseBrokerageAccountDto);
        //ACT
        MvcResult result = mockMvc.perform(
                        post(TransferController.URL_TRANSFER + TransferController.URL_NEW)
                                .param("clientId", UUID.randomUUID().toString())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(requestRefillBrokerageAccountDto)))
                .andExpect(status().isOk())
                .andReturn();
        //VERIFY
        verifyBody(asJsonString(responseBrokerageAccountDto), result.getResponse().getContentAsString());
    }

    @Test
    @DisplayName("If refillBrokerageAccount wasn't successfully then return status BAD REQUEST")
    void refillBrokerageAccount_ifNotSuccess_then_BAD_REQUEST() throws Exception {
        //ARRANGE
        when(transferService.refillBrokerageAccount(any(UUID.class), any(RequestRefillBrokerageAccountDto.class)))
                .thenThrow(new EntityNotFoundException());
        //ACT & VERIFY
        mockMvc.perform(post(TransferController.URL_TRANSFER + TransferController.URL_NEW)
                        .param("clientId", UUID.randomUUID().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(requestRefillBrokerageAccountDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("if createPaymentOrTransfer was successfully then return status OK")
    void createPaymentOrTransfer_shouldReturnStatusOK() throws Exception {
        //ARRANGE
        when(transferService.createPaymentOrTransfer(any(UUID.class), any(CreatePaymentDto.class)))
                .thenReturn(createPaymentResponseDto);
        //ACT
        MvcResult result = mockMvc.perform(
                        post(URL_CREATE_PAYMENT_OR_TRANSFER)
                                .param("clientId", UUID.randomUUID().toString())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(new CreatePaymentDto())))
                .andExpect(status().isOk())
                .andReturn();
        //VERIFY
        verifyBody(asJsonString(createPaymentResponseDto), result.getResponse().getContentAsString());
    }

    @Test
    @DisplayName("If createPaymentOrTransfer wasn't successfully then return status BAD REQUEST")
    void createPaymentOrTransfer_ifNotSuccess_then_BAD_REQUEST() throws Exception {
        //ARRANGE
        when(transferService.createPaymentOrTransfer(any(UUID.class), any(CreatePaymentDto.class)))
                .thenThrow(new EntityNotFoundException());
        //ACT & VERIFY
        mockMvc.perform(post(URL_CREATE_PAYMENT_OR_TRANSFER)
                        .param("clientId", UUID.randomUUID().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(new CreatePaymentDto())))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("if changeStatus was successfully then return status OK")
    void changeStatus_shouldReturnStatusOK() throws Exception {
        //ARRANGE
        when(transferService.changeStatus(any(UUID.class)))
                .thenReturn(changeStatusResponseDto);
        //ACT
        MvcResult result = mockMvc.perform(
                        patch(TransferController.URL_TRANSFER + TransferController.URL_CHANGE_STATUS,
                                TRANSFER_ID)
                                .contentType(MediaType.APPLICATION_JSON)
                                )
                .andExpect(status().isOk())
                .andReturn();
        //VERIFY
        verifyBody(asJsonString(changeStatusRequestDto), result.getResponse().getContentAsString());
    }

    private String asJsonString(Object obj) throws JsonProcessingException {
        return new ObjectMapper().findAndRegisterModules().enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS).writeValueAsString(obj);
    }

    private void verifyBody(String actualBody, String expectedBody) {
        assertThat(actualBody).isEqualTo(expectedBody);
    }
}