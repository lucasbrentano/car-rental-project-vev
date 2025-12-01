package com.example.carrentalproject.journey;

import com.example.carrentalproject.domain.CreditCard;
import com.example.carrentalproject.domain.User;
import com.example.carrentalproject.dto.CreditCardDto;
import com.example.carrentalproject.exception.NoCreditCardException;
import com.example.carrentalproject.repository.CreditCardRepository;
import com.example.carrentalproject.repository.RoleRepository;
import com.example.carrentalproject.repository.UserRepository;
import com.example.carrentalproject.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Year;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes de Jornada - Etapa 2: Usuária adiciona cartão de crédito
 * PUT /users/{username}/creditCards
 * 
 * Técnicas aplicadas:
 * - Particionamento de equivalência
 * - Análise de valores limite
 * - Testes de propriedade
 * - Uso de dublês (Mocks)
 * - Testes de cobertura essencial
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Journey Step 2 - Add Credit Card Tests")
class Step2_AddCreditCardJourneyTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CreditCardRepository creditCardRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User validUser;
    private CreditCardDto validCreditCardDto;

    @BeforeEach
    void setUp() {
        validUser = User.builder()
                .id(1L)
                .firstName("Maria")
                .lastName("Silva")
                .username("maria.silva")
                .email("maria.silva@email.com")
                .phone(987654321)
                .creditCard(null)
                .build();

        validCreditCardDto = CreditCardDto.builder()
                .cardNumber(1234567890123456L) // 16 dígitos
                .month(12)
                .year(2025)
                .CVV(123)
                .build();
    }

    // ==================== PARTICIONAMENTO: Casos Válidos ====================

    @Test
    @DisplayName("TC1: Adicionar cartão com dados válidos - partição válida completa")
    void shouldAddCreditCard_WithValidData() {
        // Arrange
        CreditCard savedCard = CreditCard.builder()
                .id(1L)
                .cardNumber(validCreditCardDto.getCardNumber())
                .month(validCreditCardDto.getMonth())
                .year(validCreditCardDto.getYear())
                .CVV(validCreditCardDto.getCVV())
                .accountBalance(0L)
                .build();

        when(userRepository.findByUsername(validUser.getUsername())).thenReturn(Optional.of(validUser));
        when(creditCardRepository.save(any(CreditCard.class))).thenReturn(savedCard);
        when(userRepository.save(any(User.class))).thenReturn(validUser);

        // Act
        User result = userService.addCreditCardToUser(validUser.getUsername(), validCreditCardDto);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getCreditCard());
        assertEquals(validCreditCardDto.getCardNumber(), result.getCreditCard().getCardNumber());
        verify(creditCardRepository, times(1)).save(any(CreditCard.class));
        verify(userRepository, times(1)).save(validUser);
    }

    @Test
    @DisplayName("TC2: Adicionar cartão com mês janeiro (1) - valor limite inferior válido")
    void shouldAddCreditCard_WithJanuary() {
        // Arrange - Mês 1 (Janeiro) - limite inferior válido
        CreditCardDto cardWithMinMonth = CreditCardDto.builder()
                .cardNumber(1111222233334444L)
                .month(1) // Janeiro - limite inferior
                .year(2025)
                .CVV(456)
                .build();

        CreditCard savedCard = CreditCard.builder()
                .cardNumber(cardWithMinMonth.getCardNumber())
                .month(cardWithMinMonth.getMonth())
                .year(cardWithMinMonth.getYear())
                .CVV(cardWithMinMonth.getCVV())
                .accountBalance(0L)
                .build();

        when(userRepository.findByUsername(validUser.getUsername())).thenReturn(Optional.of(validUser));
        when(creditCardRepository.save(any(CreditCard.class))).thenReturn(savedCard);
        when(userRepository.save(any(User.class))).thenReturn(validUser);

        // Act
        User result = userService.addCreditCardToUser(validUser.getUsername(), cardWithMinMonth);

        // Assert
        assertNotNull(result.getCreditCard());
        assertEquals(1, result.getCreditCard().getMonth());
    }

    @Test
    @DisplayName("TC3: Adicionar cartão com mês dezembro (12) - valor limite superior válido")
    void shouldAddCreditCard_WithDecember() {
        // Arrange - Mês 12 (Dezembro) - limite superior válido
        CreditCardDto cardWithMaxMonth = CreditCardDto.builder()
                .cardNumber(5555666677778888L)
                .month(12) // Dezembro - limite superior
                .year(2025)
                .CVV(789)
                .build();

        CreditCard savedCard = CreditCard.builder()
                .cardNumber(cardWithMaxMonth.getCardNumber())
                .month(cardWithMaxMonth.getMonth())
                .year(cardWithMaxMonth.getYear())
                .CVV(cardWithMaxMonth.getCVV())
                .accountBalance(0L)
                .build();

        when(userRepository.findByUsername(validUser.getUsername())).thenReturn(Optional.of(validUser));
        when(creditCardRepository.save(any(CreditCard.class))).thenReturn(savedCard);
        when(userRepository.save(any(User.class))).thenReturn(validUser);

        // Act
        User result = userService.addCreditCardToUser(validUser.getUsername(), cardWithMaxMonth);

        // Assert
        assertEquals(12, result.getCreditCard().getMonth());
    }

    @Test
    @DisplayName("TC4: Adicionar cartão com ano corrente (2025) - valor limite inferior válido")
    void shouldAddCreditCard_WithCurrentYear() {
        // Arrange - Ano atual (2025) - limite inferior válido
        CreditCardDto cardWithCurrentYear = CreditCardDto.builder()
                .cardNumber(9999888877776666L)
                .month(6)
                .year(2025) // Ano atual
                .CVV(321)
                .build();

        CreditCard savedCard = CreditCard.builder()
                .cardNumber(cardWithCurrentYear.getCardNumber())
                .month(cardWithCurrentYear.getMonth())
                .year(cardWithCurrentYear.getYear())
                .CVV(cardWithCurrentYear.getCVV())
                .accountBalance(0L)
                .build();

        when(userRepository.findByUsername(validUser.getUsername())).thenReturn(Optional.of(validUser));
        when(creditCardRepository.save(any(CreditCard.class))).thenReturn(savedCard);
        when(userRepository.save(any(User.class))).thenReturn(validUser);

        // Act
        User result = userService.addCreditCardToUser(validUser.getUsername(), cardWithCurrentYear);

        // Assert
        assertEquals(2025, result.getCreditCard().getYear());
    }

    @Test
    @DisplayName("TC5: Adicionar cartão com ano futuro (2035) - valor válido dentro do range")
    void shouldAddCreditCard_WithFutureYear() {
        // Arrange - Ano futuro válido (10 anos à frente)
        CreditCardDto cardWithFutureYear = CreditCardDto.builder()
                .cardNumber(1234432112344321L)
                .month(3)
                .year(2035) // 10 anos no futuro
                .CVV(654)
                .build();

        CreditCard savedCard = CreditCard.builder()
                .cardNumber(cardWithFutureYear.getCardNumber())
                .month(cardWithFutureYear.getMonth())
                .year(cardWithFutureYear.getYear())
                .CVV(cardWithFutureYear.getCVV())
                .accountBalance(0L)
                .build();

        when(userRepository.findByUsername(validUser.getUsername())).thenReturn(Optional.of(validUser));
        when(creditCardRepository.save(any(CreditCard.class))).thenReturn(savedCard);
        when(userRepository.save(any(User.class))).thenReturn(validUser);

        // Act
        User result = userService.addCreditCardToUser(validUser.getUsername(), cardWithFutureYear);

        // Assert
        assertEquals(2035, result.getCreditCard().getYear());
    }

    @Test
    @DisplayName("TC6: Adicionar cartão com CVV mínimo (001) - valor limite inferior")
    void shouldAddCreditCard_WithMinimumCVV() {
        // Arrange - CVV com 3 dígitos começando em 001
        CreditCardDto cardWithMinCVV = CreditCardDto.builder()
                .cardNumber(4444333322221111L)
                .month(8)
                .year(2026)
                .CVV(1) // CVV mínimo (será armazenado como 001)
                .build();

        CreditCard savedCard = CreditCard.builder()
                .cardNumber(cardWithMinCVV.getCardNumber())
                .month(cardWithMinCVV.getMonth())
                .year(cardWithMinCVV.getYear())
                .CVV(cardWithMinCVV.getCVV())
                .accountBalance(0L)
                .build();

        when(userRepository.findByUsername(validUser.getUsername())).thenReturn(Optional.of(validUser));
        when(creditCardRepository.save(any(CreditCard.class))).thenReturn(savedCard);
        when(userRepository.save(any(User.class))).thenReturn(validUser);

        // Act
        User result = userService.addCreditCardToUser(validUser.getUsername(), cardWithMinCVV);

        // Assert
        assertEquals(1, result.getCreditCard().getCVV());
    }

    @Test
    @DisplayName("TC7: Adicionar cartão com CVV máximo (999) - valor limite superior")
    void shouldAddCreditCard_WithMaximumCVV() {
        // Arrange - CVV máximo de 3 dígitos
        CreditCardDto cardWithMaxCVV = CreditCardDto.builder()
                .cardNumber(7777888899990000L)
                .month(11)
                .year(2028)
                .CVV(999) // CVV máximo
                .build();

        CreditCard savedCard = CreditCard.builder()
                .cardNumber(cardWithMaxCVV.getCardNumber())
                .month(cardWithMaxCVV.getMonth())
                .year(cardWithMaxCVV.getYear())
                .CVV(cardWithMaxCVV.getCVV())
                .accountBalance(0L)
                .build();

        when(userRepository.findByUsername(validUser.getUsername())).thenReturn(Optional.of(validUser));
        when(creditCardRepository.save(any(CreditCard.class))).thenReturn(savedCard);
        when(userRepository.save(any(User.class))).thenReturn(validUser);

        // Act
        User result = userService.addCreditCardToUser(validUser.getUsername(), cardWithMaxCVV);

        // Assert
        assertEquals(999, result.getCreditCard().getCVV());
    }

    @Test
    @DisplayName("TC8: Adicionar cartão com número mínimo (16 dígitos começando com 1) - partição válida")
    void shouldAddCreditCard_WithMinimumCardNumber() {
        // Arrange - Número de cartão com 16 dígitos começando em 1000...
        CreditCardDto cardWithMinNumber = CreditCardDto.builder()
                .cardNumber(1000000000000000L) // Menor número válido de 16 dígitos
                .month(5)
                .year(2027)
                .CVV(111)
                .build();

        CreditCard savedCard = CreditCard.builder()
                .cardNumber(cardWithMinNumber.getCardNumber())
                .month(cardWithMinNumber.getMonth())
                .year(cardWithMinNumber.getYear())
                .CVV(cardWithMinNumber.getCVV())
                .accountBalance(0L)
                .build();

        when(userRepository.findByUsername(validUser.getUsername())).thenReturn(Optional.of(validUser));
        when(creditCardRepository.save(any(CreditCard.class))).thenReturn(savedCard);
        when(userRepository.save(any(User.class))).thenReturn(validUser);

        // Act
        User result = userService.addCreditCardToUser(validUser.getUsername(), cardWithMinNumber);

        // Assert
        assertEquals(1000000000000000L, result.getCreditCard().getCardNumber());
    }

    @Test
    @DisplayName("TC9: Adicionar cartão com número máximo (16 dígitos - 9999...) - valor limite superior")
    void shouldAddCreditCard_WithMaximumCardNumber() {
        // Arrange - Número de cartão máximo com 16 dígitos
        CreditCardDto cardWithMaxNumber = CreditCardDto.builder()
                .cardNumber(9999999999999999L) // Maior número de 16 dígitos
                .month(9)
                .year(2030)
                .CVV(777)
                .build();

        CreditCard savedCard = CreditCard.builder()
                .cardNumber(cardWithMaxNumber.getCardNumber())
                .month(cardWithMaxNumber.getMonth())
                .year(cardWithMaxNumber.getYear())
                .CVV(cardWithMaxNumber.getCVV())
                .accountBalance(0L)
                .build();

        when(userRepository.findByUsername(validUser.getUsername())).thenReturn(Optional.of(validUser));
        when(creditCardRepository.save(any(CreditCard.class))).thenReturn(savedCard);
        when(userRepository.save(any(User.class))).thenReturn(validUser);

        // Act
        User result = userService.addCreditCardToUser(validUser.getUsername(), cardWithMaxNumber);

        // Assert
        assertEquals(9999999999999999L, result.getCreditCard().getCardNumber());
    }

    // ==================== PARTICIONAMENTO: Casos Inválidos ====================

    @Test
    @DisplayName("TC10: Falha ao adicionar cartão para usuário inexistente - partição inválida")
    void shouldThrowException_WhenUserDoesNotExist() {
        // Arrange - Usuário não existe
        when(userRepository.findByUsername("nonexistent.user")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UsernameNotFoundException.class,
                () -> userService.addCreditCardToUser("nonexistent.user", validCreditCardDto));

        verify(creditCardRepository, never()).save(any(CreditCard.class));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("TC11: Falha ao adicionar segundo cartão - partição inválida (usuário já tem cartão)")
    void shouldThrowException_WhenUserAlreadyHasCreditCard() {
        // Arrange - Usuário já possui um cartão
        CreditCard existingCard = CreditCard.builder()
                .id(1L)
                .cardNumber(1111222233334444L)
                .month(6)
                .year(2026)
                .CVV(123)
                .accountBalance(1000L)
                .build();

        validUser.setCreditCard(existingCard);

        when(userRepository.findByUsername(validUser.getUsername())).thenReturn(Optional.of(validUser));

        // Act & Assert
        IllegalCallerException exception = assertThrows(IllegalCallerException.class,
                () -> userService.addCreditCardToUser(validUser.getUsername(), validCreditCardDto));

        assertEquals("User Already Has Credit Card!", exception.getMessage());
        verify(creditCardRepository, never()).save(any(CreditCard.class));
    }

    // ==================== TESTES DE PROPRIEDADE ====================

    @Test
    @DisplayName("TC12: Propriedade - Saldo inicial deve sempre ser zero")
    void shouldAlwaysInitializeCreditCard_WithZeroBalance() {
        // Arrange
        when(userRepository.findByUsername(validUser.getUsername())).thenReturn(Optional.of(validUser));
        when(creditCardRepository.save(any(CreditCard.class))).thenAnswer(invocation -> {
            CreditCard card = invocation.getArgument(0);
            // Propriedade: saldo inicial SEMPRE deve ser zero
            assertEquals(0L, card.getAccountBalance());
            return card;
        });
        when(userRepository.save(any(User.class))).thenReturn(validUser);

        // Act
        userService.addCreditCardToUser(validUser.getUsername(), validCreditCardDto);

        // Assert
        verify(creditCardRepository, times(1)).save(any(CreditCard.class));
    }

    @Test
    @DisplayName("TC13: Propriedade - Cartão deve sempre ser associado ao usuário corretamente")
    void shouldAlwaysAssociateCreditCard_WithUser() {
        // Arrange
        CreditCard savedCard = CreditCard.builder()
                .id(1L)
                .cardNumber(validCreditCardDto.getCardNumber())
                .month(validCreditCardDto.getMonth())
                .year(validCreditCardDto.getYear())
                .CVV(validCreditCardDto.getCVV())
                .accountBalance(0L)
                .build();

        when(userRepository.findByUsername(validUser.getUsername())).thenReturn(Optional.of(validUser));
        when(creditCardRepository.save(any(CreditCard.class))).thenReturn(savedCard);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            // Propriedade: usuário deve ter o cartão associado
            assertNotNull(user.getCreditCard());
            // Propriedade: cartão deve ter o usuário associado
            assertNotNull(user.getCreditCard().getUser());
            assertEquals(user, user.getCreditCard().getUser());
            return user;
        });

        // Act
        User result = userService.addCreditCardToUser(validUser.getUsername(), validCreditCardDto);

        // Assert
        assertNotNull(result.getCreditCard());
        assertEquals(validUser, result.getCreditCard().getUser());
    }

    @Test
    @DisplayName("TC14: Propriedade - Dados do cartão devem ser preservados após salvamento")
    void shouldPreserveCreditCardData_AfterSaving() {
        // Arrange
        CreditCardDto originalDto = CreditCardDto.builder()
                .cardNumber(1357924680135792L)
                .month(7)
                .year(2029)
                .CVV(246)
                .build();

        CreditCard savedCard = CreditCard.builder()
                .id(10L)
                .cardNumber(originalDto.getCardNumber())
                .month(originalDto.getMonth())
                .year(originalDto.getYear())
                .CVV(originalDto.getCVV())
                .accountBalance(0L)
                .build();

        when(userRepository.findByUsername(validUser.getUsername())).thenReturn(Optional.of(validUser));
        when(creditCardRepository.save(any(CreditCard.class))).thenReturn(savedCard);
        when(userRepository.save(any(User.class))).thenReturn(validUser);

        // Act
        User result = userService.addCreditCardToUser(validUser.getUsername(), originalDto);

        // Assert - Propriedade: todos os dados devem ser preservados
        CreditCard savedCardFromUser = result.getCreditCard();
        assertEquals(originalDto.getCardNumber(), savedCardFromUser.getCardNumber());
        assertEquals(originalDto.getMonth(), savedCardFromUser.getMonth());
        assertEquals(originalDto.getYear(), savedCardFromUser.getYear());
        assertEquals(originalDto.getCVV(), savedCardFromUser.getCVV());
    }

    @Test
    @DisplayName("TC15: Propriedade - Operação deve ser atômica (salvar cartão e associar usuário)")
    void shouldPerformAtomicOperation_WhenAddingCreditCard() {
        // Arrange
        CreditCard savedCard = CreditCard.builder()
                .id(1L)
                .cardNumber(validCreditCardDto.getCardNumber())
                .month(validCreditCardDto.getMonth())
                .year(validCreditCardDto.getYear())
                .CVV(validCreditCardDto.getCVV())
                .accountBalance(0L)
                .build();

        when(userRepository.findByUsername(validUser.getUsername())).thenReturn(Optional.of(validUser));
        when(creditCardRepository.save(any(CreditCard.class))).thenReturn(savedCard);
        when(userRepository.save(any(User.class))).thenReturn(validUser);

        // Act
        userService.addCreditCardToUser(validUser.getUsername(), validCreditCardDto);

        // Assert - Propriedade: operação atômica - ambas as operações devem ocorrer
        verify(creditCardRepository, times(1)).save(any(CreditCard.class));
        verify(userRepository, times(1)).save(validUser);
        
        // Verificar ordem: cartão salvo primeiro, depois usuário
        var inOrder = inOrder(creditCardRepository, userRepository);
        inOrder.verify(creditCardRepository).save(any(CreditCard.class));
        inOrder.verify(userRepository).save(validUser);
    }
}
