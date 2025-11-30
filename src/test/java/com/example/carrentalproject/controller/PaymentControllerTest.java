package com.example.carrentalproject.controller;

import com.example.carrentalproject.dto.CreditCardDto;
import com.example.carrentalproject.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private PaymentController paymentController;

    private ObjectMapper objectMapper;

    private CreditCardDto creditCardDto;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(paymentController).build();
        objectMapper = new ObjectMapper();

        creditCardDto = CreditCardDto.builder()
                .cardNumber(1234567890123456L)
                .month(12)
                .year(2029)
                .CVV(123)
                .build();

    }

    // POST /payment/addCreditCard
    @Test
    void itShouldAddCreditCard() throws Exception {
        mockMvc.perform(post("/payment/addCreditCard")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(creditCardDto)))
                .andExpect(status().isOk());

        verify(paymentService, times(1)).addCreditCard(creditCardDto);
    }

    @Test
    void itShouldReturn400WhenInvalidCreditCardDto() throws Exception {
        mockMvc.perform(post("/payment/addCreditCard")
                .contentType("application/json")
                .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void itShouldReturn500WhenAddCreditCardFails() throws Exception {
        doThrow(new RuntimeException("Failed")).when(paymentService).addCreditCard(creditCardDto);

        mockMvc.perform(post("/payment/addCreditCard")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(creditCardDto)))
                .andExpect(status().isInternalServerError());
    }

    // PUT /payment/moneyTransfer
    @Test
    void itShouldTransferMoney() throws Exception {
        mockMvc.perform(put("/payment/moneyTransfer")
                .param("moneyAmount", "100"))
                .andExpect(status().isOk());

        verify(paymentService, times(1)).moneyTransfer(100L);
    }

    @Test
    void itShouldReturn400WhenMissingMoneyAmount() throws Exception {
        mockMvc.perform(put("/payment/moneyTransfer"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void itShouldReturn500WhenMoneyTransferFails() throws Exception {
        doThrow(new RuntimeException("Fail"))
                .when(paymentService).moneyTransfer(200L);

        mockMvc.perform(put("/payment/moneyTransfer")
                .param("moneyAmount", "200"))
                .andExpect(status().isInternalServerError());
    }
}

