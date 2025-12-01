package com.example.carrentalproject.journey;

import com.example.carrentalproject.domain.CreditCard;
import com.example.carrentalproject.domain.User;
import com.example.carrentalproject.dto.CreditCardDto;
import com.example.carrentalproject.exception.NoCreditCardException;
import com.example.carrentalproject.repository.CreditCardRepository;
import com.example.carrentalproject.repository.UserRepository;
import com.example.carrentalproject.security.LoggedInUser;
import com.example.carrentalproject.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes de Jornada - Etapa 5: Usuária efetua pagamento
 * POST /payment/addCreditCard
 * PUT /payment/moneyTransfer?moneyAmount=...
 * 
 * Técnicas aplicadas:
 * - Particionamento de equivalência
 * - Análise de valores limite
 * - Testes de propriedade
 * - Uso de dublês (Mocks)
 * - Testes de cobertura essencial
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Journey Step 5 - Payment Tests")
class Step5_PaymentJourneyTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CreditCardRepository creditCardRepository;

    @Mock
    private LoggedInUser loggedInUser;

    @InjectMocks
    private PaymentService paymentService;

    private User validUser;
    private CreditCardDto validCreditCardDto;
    private CreditCard existingCreditCard;

    @BeforeEach
    void setUp() {
        // Setup de usuário sem cartão (para testes de addCreditCard)
        validUser = User.builder()
                .id(1L)
                .firstName("Maria")
                .lastName("Silva")
                .username("maria.silva")
                .email("maria.silva@email.com")
                .phone(987654321)
                .creditCard(null)
                .build();

        // Setup de DTO de cartão de crédito
        validCreditCardDto = CreditCardDto.builder()
                .cardNumber(1234567890123456L)
                .month(12)
                .year(2025)
                .CVV(123)
                .build();

        // Setup de cartão de crédito existente (para testes de moneyTransfer)
        existingCreditCard = CreditCard.builder()
                .id(1L)
                .cardNumber(1234567890123456L)
                .month(12)
                .year(2025)
                .CVV(123)
                .accountBalance(1000L)
                .user(validUser)
                .build();
    }

    // ==================== TESTES DE POST /payment/addCreditCard ====================

    @Test
    @DisplayName("TC1: Adicionar cartão com sucesso - partição válida")
    void shouldAddCreditCard_Successfully() {
        // Arrange
        CreditCard savedCard = CreditCard.builder()
                .id(1L)
                .cardNumber(validCreditCardDto.getCardNumber())
                .month(validCreditCardDto.getMonth())
                .year(validCreditCardDto.getYear())
                .CVV(validCreditCardDto.getCVV())
                .accountBalance(0L)
                .build();

        when(loggedInUser.getUser()).thenReturn(validUser);
        when(creditCardRepository.save(any(CreditCard.class))).thenReturn(savedCard);
        when(userRepository.save(any(User.class))).thenReturn(validUser);

        // Act
        paymentService.addCreditCard(validCreditCardDto);

        // Assert
        assertNotNull(validUser.getCreditCard());
        assertEquals(validCreditCardDto.getCardNumber(), validUser.getCreditCard().getCardNumber());
        verify(creditCardRepository, times(1)).save(any(CreditCard.class));
        verify(userRepository, times(1)).save(validUser);
    }

    @Test
    @DisplayName("TC2: Falha ao adicionar segundo cartão - partição inválida")
    void shouldThrowException_WhenUserAlreadyHasCreditCard() {
        // Arrange - Usuário já possui cartão
        validUser.setCreditCard(existingCreditCard);

        when(loggedInUser.getUser()).thenReturn(validUser);

        // Act & Assert
        IllegalCallerException exception = assertThrows(IllegalCallerException.class,
                () -> paymentService.addCreditCard(validCreditCardDto));

        assertEquals("You Already Have Credit Card!", exception.getMessage());
        verify(creditCardRepository, never()).save(any(CreditCard.class));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("TC3: Propriedade - Cartão deve sempre ser criado com saldo zero")
    void shouldAlwaysCreateCreditCard_WithZeroBalance() {
        // Arrange
        when(loggedInUser.getUser()).thenReturn(validUser);
        when(creditCardRepository.save(any(CreditCard.class))).thenAnswer(invocation -> {
            CreditCard card = invocation.getArgument(0);
            // Propriedade: saldo inicial SEMPRE deve ser zero
            assertEquals(0L, card.getAccountBalance());
            return card;
        });
        when(userRepository.save(any(User.class))).thenReturn(validUser);

        // Act
        paymentService.addCreditCard(validCreditCardDto);

        // Assert
        verify(creditCardRepository, times(1)).save(any(CreditCard.class));
    }

    @Test
    @DisplayName("TC4: Propriedade - Relação bidirecional entre User e CreditCard")
    void shouldEstablishBidirectionalRelationship_BetweenUserAndCard() {
        // Arrange
        CreditCard savedCard = CreditCard.builder()
                .id(1L)
                .cardNumber(validCreditCardDto.getCardNumber())
                .month(validCreditCardDto.getMonth())
                .year(validCreditCardDto.getYear())
                .CVV(validCreditCardDto.getCVV())
                .accountBalance(0L)
                .build();

        when(loggedInUser.getUser()).thenReturn(validUser);
        when(creditCardRepository.save(any(CreditCard.class))).thenReturn(savedCard);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            // Propriedade: usuário deve ter cartão associado
            assertNotNull(user.getCreditCard());
            // Propriedade: cartão deve ter usuário associado
            assertNotNull(user.getCreditCard().getUser());
            assertEquals(user, user.getCreditCard().getUser());
            return user;
        });

        // Act
        paymentService.addCreditCard(validCreditCardDto);

        // Assert
        assertEquals(validUser, validUser.getCreditCard().getUser());
    }

    // ==================== TESTES DE PUT /payment/moneyTransfer ====================

    @Test
    @DisplayName("TC5: Transferir valor mínimo (1) - valor limite inferior")
    void shouldTransferMoney_WithMinimumAmount() {
        // Arrange - Transferência mínima
        validUser.setCreditCard(existingCreditCard);
        Long initialBalance = existingCreditCard.getAccountBalance();
        Long transferAmount = 1L; // Valor mínimo

        when(loggedInUser.getUser()).thenReturn(validUser);
        when(userRepository.save(any(User.class))).thenReturn(validUser);

        // Act
        paymentService.moneyTransfer(transferAmount);

        // Assert
        assertEquals(initialBalance + transferAmount, validUser.getCreditCard().getAccountBalance());
        assertEquals(1001L, validUser.getCreditCard().getAccountBalance()); // 1000 + 1
        verify(userRepository, times(1)).save(validUser);
    }

    @Test
    @DisplayName("TC6: Transferir valor médio - partição válida")
    void shouldTransferMoney_WithMediumAmount() {
        // Arrange
        validUser.setCreditCard(existingCreditCard);
        Long initialBalance = existingCreditCard.getAccountBalance();
        Long transferAmount = 500L;

        when(loggedInUser.getUser()).thenReturn(validUser);
        when(userRepository.save(any(User.class))).thenReturn(validUser);

        // Act
        paymentService.moneyTransfer(transferAmount);

        // Assert
        assertEquals(initialBalance + transferAmount, validUser.getCreditCard().getAccountBalance());
        assertEquals(1500L, validUser.getCreditCard().getAccountBalance()); // 1000 + 500
    }

    @Test
    @DisplayName("TC7: Transferir valor grande (10000) - partição válida alta")
    void shouldTransferMoney_WithLargeAmount() {
        // Arrange
        validUser.setCreditCard(existingCreditCard);
        Long initialBalance = existingCreditCard.getAccountBalance();
        Long transferAmount = 10000L;

        when(loggedInUser.getUser()).thenReturn(validUser);
        when(userRepository.save(any(User.class))).thenReturn(validUser);

        // Act
        paymentService.moneyTransfer(transferAmount);

        // Assert
        assertEquals(initialBalance + transferAmount, validUser.getCreditCard().getAccountBalance());
        assertEquals(11000L, validUser.getCreditCard().getAccountBalance()); // 1000 + 10000
    }

    @Test
    @DisplayName("TC8: Transferir para saldo zero - valor limite de saldo zero")
    void shouldTransferMoney_ToZeroBalance() {
        // Arrange - Saldo inicial zero
        existingCreditCard.setAccountBalance(0L);
        validUser.setCreditCard(existingCreditCard);
        Long transferAmount = 500L;

        when(loggedInUser.getUser()).thenReturn(validUser);
        when(userRepository.save(any(User.class))).thenReturn(validUser);

        // Act
        paymentService.moneyTransfer(transferAmount);

        // Assert
        assertEquals(500L, validUser.getCreditCard().getAccountBalance()); // 0 + 500
    }

    @Test
    @DisplayName("TC9: Múltiplas transferências acumulativas - partição de múltiplas operações")
    void shouldAccumulateBalance_WithMultipleTransfers() {
        // Arrange
        validUser.setCreditCard(existingCreditCard);
        Long initialBalance = existingCreditCard.getAccountBalance(); // 1000

        when(loggedInUser.getUser()).thenReturn(validUser);
        when(userRepository.save(any(User.class))).thenReturn(validUser);

        // Act - Múltiplas transferências
        paymentService.moneyTransfer(100L); // 1000 + 100 = 1100
        paymentService.moneyTransfer(200L); // 1100 + 200 = 1300
        paymentService.moneyTransfer(300L); // 1300 + 300 = 1600

        // Assert
        assertEquals(1600L, validUser.getCreditCard().getAccountBalance());
        verify(userRepository, times(3)).save(validUser);
    }

    @Test
    @DisplayName("TC10: Falha ao transferir sem cartão - partição inválida")
    void shouldThrowException_WhenTransferWithoutCreditCard() {
        // Arrange - Usuário sem cartão
        validUser.setCreditCard(null);

        when(loggedInUser.getUser()).thenReturn(validUser);

        // Act & Assert
        NoCreditCardException exception = assertThrows(NoCreditCardException.class,
                () -> paymentService.moneyTransfer(500L));

        assertEquals("You Do Not Have Credit Card!", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    // ==================== TESTES DE PROPRIEDADE ====================

    @Test
    @DisplayName("TC11: Propriedade - Saldo sempre deve aumentar após transferência")
    void shouldAlwaysIncreaseBalance_AfterTransfer() {
        // Arrange
        validUser.setCreditCard(existingCreditCard);
        Long initialBalance = existingCreditCard.getAccountBalance();
        Long transferAmount = 250L;

        when(loggedInUser.getUser()).thenReturn(validUser);
        when(userRepository.save(any(User.class))).thenReturn(validUser);

        // Act
        paymentService.moneyTransfer(transferAmount);

        // Assert - Propriedade: saldo após > saldo antes
        Long finalBalance = validUser.getCreditCard().getAccountBalance();
        assertTrue(finalBalance > initialBalance,
                "Balance should always increase after money transfer");
        assertEquals(initialBalance + transferAmount, finalBalance);
    }

    @Test
    @DisplayName("TC12: Propriedade - Incremento de saldo deve ser exatamente o valor transferido")
    void shouldIncrementBalance_ByExactTransferAmount() {
        // Arrange
        validUser.setCreditCard(existingCreditCard);
        Long initialBalance = existingCreditCard.getAccountBalance();
        Long transferAmount = 777L;

        when(loggedInUser.getUser()).thenReturn(validUser);
        when(userRepository.save(any(User.class))).thenReturn(validUser);

        // Act
        paymentService.moneyTransfer(transferAmount);

        // Assert - Propriedade: incremento = valor transferido
        Long increment = validUser.getCreditCard().getAccountBalance() - initialBalance;
        assertEquals(transferAmount, increment);
    }

    @Test
    @DisplayName("TC13: Propriedade - Transferência deve ser comutativa (ordem não importa)")
    void shouldBeCommutative_ForMultipleTransfers() {
        // Arrange - Cenário 1
        CreditCard card1 = CreditCard.builder()
                .accountBalance(1000L)
                .build();
        User user1 = User.builder().creditCard(card1).build();
        card1.setUser(user1);

        // Arrange - Cenário 2 (mesmos valores, ordem diferente)
        CreditCard card2 = CreditCard.builder()
                .accountBalance(1000L)
                .build();
        User user2 = User.builder().creditCard(card2).build();
        card2.setUser(user2);

        when(userRepository.save(any(User.class))).thenReturn(user1, user2);

        // Act - Cenário 1: transferir 100, depois 200
        when(loggedInUser.getUser()).thenReturn(user1);
        paymentService.moneyTransfer(100L);
        paymentService.moneyTransfer(200L);
        Long balance1 = user1.getCreditCard().getAccountBalance();

        // Act - Cenário 2: transferir 200, depois 100
        when(loggedInUser.getUser()).thenReturn(user2);
        paymentService.moneyTransfer(200L);
        paymentService.moneyTransfer(100L);
        Long balance2 = user2.getCreditCard().getAccountBalance();

        // Assert - Propriedade: resultado deve ser o mesmo independente da ordem
        assertEquals(balance1, balance2);
        assertEquals(1300L, balance1); // 1000 + 100 + 200
        assertEquals(1300L, balance2); // 1000 + 200 + 100
    }

    @Test
    @DisplayName("TC14: Propriedade - Saldo nunca deve ser negativo")
    void shouldNeverHaveNegativeBalance_AfterAnyOperation() {
        // Arrange
        existingCreditCard.setAccountBalance(0L);
        validUser.setCreditCard(existingCreditCard);

        when(loggedInUser.getUser()).thenReturn(validUser);
        when(userRepository.save(any(User.class))).thenReturn(validUser);

        // Act - Múltiplas transferências
        paymentService.moneyTransfer(50L);
        paymentService.moneyTransfer(100L);
        paymentService.moneyTransfer(25L);

        // Assert - Propriedade: saldo sempre >= 0
        Long finalBalance = validUser.getCreditCard().getAccountBalance();
        assertTrue(finalBalance >= 0,
                "Balance should never be negative");
        assertEquals(175L, finalBalance);
    }

    @Test
    @DisplayName("TC15: Propriedade - Transferência zero não deve alterar saldo")
    void shouldNotChangeBalance_WithZeroTransfer() {
        // Arrange
        validUser.setCreditCard(existingCreditCard);
        Long initialBalance = existingCreditCard.getAccountBalance();
        Long transferAmount = 0L; // Transferência zero (caso limite)

        when(loggedInUser.getUser()).thenReturn(validUser);
        when(userRepository.save(any(User.class))).thenReturn(validUser);

        // Act
        paymentService.moneyTransfer(transferAmount);

        // Assert - Propriedade: saldo não deve mudar com transferência zero
        assertEquals(initialBalance, validUser.getCreditCard().getAccountBalance());
        assertEquals(1000L, validUser.getCreditCard().getAccountBalance());
    }
}
