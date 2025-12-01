package com.example.carrentalproject.controller;

import com.example.carrentalproject.dto.CreditCardDto;
import com.example.carrentalproject.exception.InsufficientFundsException;
import com.example.carrentalproject.exception.NoCreditCardException;
import com.example.carrentalproject.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private PaymentController paymentController;

    private CreditCardDto testCreditCard;

    @BeforeEach
    void setup() {
        testCreditCard = new CreditCardDto(
                1234567890123456L,
                12,
                2025,
                123
        );
    }

    @Test
    void itShouldAddCreditCard() {
        // Given
        doNothing().when(paymentService).addCreditCard(any(CreditCardDto.class));

        // When
        assertDoesNotThrow(() -> paymentController.addCreditCard(testCreditCard));

        // Then
        verify(paymentService).addCreditCard(testCreditCard);
    }

    @Test
    void itShouldPerformMoneyTransfer() {
        // Given
        doNothing().when(paymentService).moneyTransfer(500L);

        // When
        assertDoesNotThrow(() -> paymentController.moneyTransfer(500L));

        // Then
        verify(paymentService).moneyTransfer(500L);
    }

    @Test
    void itShouldThrowInsufficientFundsException() {
        // Given
        doThrow(new InsufficientFundsException("Insufficient funds"))
                .when(paymentService).moneyTransfer(10000L);

        // When & Then
        assertThrows(InsufficientFundsException.class, 
                () -> paymentController.moneyTransfer(10000L));
        verify(paymentService).moneyTransfer(10000L);
    }

    @Test
    void itShouldThrowNoCreditCardException() {
        // Given
        doThrow(new NoCreditCardException("No credit card found"))
                .when(paymentService).moneyTransfer(anyLong());

        // When & Then
        assertThrows(NoCreditCardException.class, 
                () -> paymentController.moneyTransfer(100L));
        verify(paymentService).moneyTransfer(100L);
    }

    @Test
    void itShouldCallServiceWithCorrectAmount() {
        // Given
        doNothing().when(paymentService).moneyTransfer(anyLong());

        // When
        paymentController.moneyTransfer(750L);

        // Then
        verify(paymentService).moneyTransfer(750L);
    }
}
