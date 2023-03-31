package by.afinny.moneytransfer.controller;

import by.afinny.moneytransfer.dto.AutoPaymentDto;
import by.afinny.moneytransfer.dto.AutoPaymentsDto;
import by.afinny.moneytransfer.entity.constant.TransferPeriodicity;
import by.afinny.moneytransfer.entity.constant.TransferTypeName;
import by.afinny.moneytransfer.exception.handler.ExceptionHandlerController;
import by.afinny.moneytransfer.service.PaymentService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@WebMvcTest(AutoPaymentController.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AutoPaymentControllerTest {

    private final UUID TRANSFER_ORDER_ID = UUID.randomUUID();
    @MockBean
    private PaymentService paymentService;
    private MockMvc mockMvc;
    private AutoPaymentDto autoPaymentDto;

    private final UUID CLIENT_ID = UUID.randomUUID();
    private List<AutoPaymentsDto> autoPaymentsDtoList = new ArrayList<>();


    @BeforeAll
    void setUp() {
        mockMvc = standaloneSetup(new AutoPaymentController(paymentService))
                .setControllerAdvice(new ExceptionHandlerController()).build();

        autoPaymentDto = AutoPaymentDto.builder()
                .periodicity(TransferPeriodicity.MONTHLY)
                .startDate(LocalDateTime.now()).build();

        AutoPaymentsDto autoPaymentsDto = AutoPaymentsDto.builder()
                .transferOrderId(UUID.randomUUID())
                .typeName(TransferTypeName.BETWEEN_CARDS).build();
        autoPaymentsDtoList.add(autoPaymentsDto);
    }

    @Test
    @DisplayName("If viewAutoPayments was successfully then return List<AutoPaymentsDto>")
    void viewAutoPayments_shouldReturnList() throws Exception {
        //ARRANGE
        when(paymentService.viewAutoPayments(CLIENT_ID)).thenReturn(autoPaymentsDtoList);
        //ACT
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(AutoPaymentController.URL_AUTO_PAYMENT)
                        .param("clientId", CLIENT_ID.toString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        //VERIFY
        verifyBody(result.getResponse().getContentAsString(), asJsonString(autoPaymentsDtoList));
    }

    @Test
    @DisplayName("If viewAutoPayments wasn't successfully then return status BAD REQUEST")
    void viewAutoPayments_ifNotSuccess_thenStatus400() throws Exception {
        //ARRANGE
        when(paymentService.viewAutoPayments(any(UUID.class))).thenThrow(EntityNotFoundException.class);
        //ACT & VERIFY
        mockMvc.perform(MockMvcRequestBuilders.get(AutoPaymentController.URL_AUTO_PAYMENT)
                        .param("clientId", CLIENT_ID.toString()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn();
    }

    @Test
    @DisplayName("If updateAutoPayment was successfully then return AutoPaymentDto")
    @SneakyThrows
    void updateAutoPayment_shouldReturnAutoPaymentDto() {
        //ARRANGE
        when(paymentService.addAutoPayment(any(AutoPaymentDto.class), any(UUID.class))).thenReturn(autoPaymentDto);

        //ACT
        MvcResult result = mockMvc.perform(
                        patch("/auth/autopayments")
                                .param("transferId", TRANSFER_ORDER_ID.toString())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(autoPaymentDto)))
                .andExpect(status().isOk())
                .andReturn();

        //VERIFY
        verifyBody(asJsonString(autoPaymentDto), result.getResponse().getContentAsString());
    }

    @Test
    @DisplayName("If updateAutoPayment wasn't successfully then return status BAD REQUEST")
    @SneakyThrows
    void updateAutoPayment_ifNotSuccess_thenStatus400() {
        //ARRANGE
        when(paymentService.addAutoPayment(any(AutoPaymentDto.class), any(UUID.class)))
                .thenThrow(EntityNotFoundException.class);
        //ACT & VERIFY
        mockMvc.perform(
                        patch("/auth/autopayments")
                                .param("transferId", TRANSFER_ORDER_ID.toString())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(autoPaymentDto)))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    private void verifyBody(String expectedBody, String actualBody) {
        assertThat(actualBody).isEqualTo(expectedBody);
    }

    private static String asJsonString(final Object obj) throws JsonProcessingException {
        return new ObjectMapper().findAndRegisterModules().writeValueAsString(obj);
    }
}