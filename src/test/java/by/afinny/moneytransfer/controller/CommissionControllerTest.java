package by.afinny.moneytransfer.controller;

import by.afinny.moneytransfer.dto.CommissionDto;
import by.afinny.moneytransfer.entity.constant.CurrencyCode;
import by.afinny.moneytransfer.entity.constant.TransferTypeName;
import by.afinny.moneytransfer.service.CommissionService;
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

import javax.persistence.EntityNotFoundException;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CommissionController.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
public class CommissionControllerTest {

    @MockBean
    CommissionService commissionService;

    @Autowired
    private MockMvc mockMvc;
    private CommissionDto commissionDto;

    private final TransferTypeName typeName = TransferTypeName.BETWEEN_CARDS;
    private final CurrencyCode currencyCode = CurrencyCode.RUB;

    @BeforeAll
    void setUp() {
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
    @DisplayName("If successful then return commissionDto")
    void getCommissionDto_shouldReturnCommissionDto() throws Exception{
        //ARRANGE
        when(commissionService.getCommissionData(typeName,currencyCode)).thenReturn(commissionDto);

        //ACT
        MvcResult result = mockMvc.perform(
                        get(CommissionController.URL_COMMISSION)
                                .param("typeName",typeName.toString())
                                .param("currencyCode", currencyCode.toString()))
                .andExpect(status().isOk())
                .andReturn();
        //VERIFY
        verifyBody(asJsonString(commissionDto), result.getResponse().getContentAsString());
    }

    @Test
    @DisplayName("If unsuccessful then return BAD_REQUEST")
    void getCommissionDto_ifNotSuccess_then500_BAD_REQUEST() throws Exception{
        //ARRANGE
        when(commissionService.getCommissionData(any(), any())).thenThrow(EntityNotFoundException.class);

        //ACT&VERIFY
        mockMvc.perform(
                        get(CommissionController.URL_COMMISSION)
                                .param("typeName", typeName.toString())
                                .param("currencyCode", currencyCode.toString()))
                .andExpect(status().isBadRequest());
    }

    private static String asJsonString(final Object obj) throws JsonProcessingException {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .writeValueAsString(obj);
    }

    private void verifyBody(String expectedBody, String actualBody) {
        assertEquals(actualBody, expectedBody);
    }
}
