package com.example.carrentalproject.journey;

import com.example.carrentalproject.domain.AccessKey;
import com.example.carrentalproject.domain.CarPackage;
import com.example.carrentalproject.domain.CreditCard;
import com.example.carrentalproject.domain.User;
import com.example.carrentalproject.dto.AccessKeyDto;
import com.example.carrentalproject.exception.ExistingOrderException;
import com.example.carrentalproject.exception.InsufficientFundsException;
import com.example.carrentalproject.exception.NoCreditCardException;
import com.example.carrentalproject.repository.AccessKeyRepository;
import com.example.carrentalproject.repository.CarPackageRepository;
import com.example.carrentalproject.repository.OrderRepository;
import com.example.carrentalproject.security.LoggedInUser;
import com.example.carrentalproject.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.persistence.EntityNotFoundException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes de Jornada - Etapa 4: Usuária seleciona um pacote e faz um pedido
 * POST /orders?carPackage=X&hours=Y
 * 
 * Técnicas aplicadas:
 * - Particionamento de equivalência
 * - Análise de valores limite
 * - Testes de propriedade
 * - Uso de dublês (Mocks)
 * - Testes de cobertura essencial
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Journey Step 4 - Place Order Tests")
class Step4_PlaceOrderJourneyTest {

    @Mock
    private CarPackageRepository carPackageRepository;

    @Mock
    private AccessKeyRepository accessKeyRepository;

    @Mock
    private LoggedInUser loggedInUser;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    private User validUser;
    private CreditCard validCreditCard;
    private CarPackage economyPackage;
    private CarPackage standardPackage;
    private CarPackage luxuryPackage;

    @BeforeEach
    void setUp() {
        // Setup de cartão de crédito
        validCreditCard = CreditCard.builder()
                .id(1L)
                .cardNumber(1234567890123456L)
                .month(12)
                .year(2025)
                .CVV(123)
                .accountBalance(5000L) // Saldo suficiente para testes
                .build();

        // Setup de usuário
        validUser = User.builder()
                .id(1L)
                .firstName("Maria")
                .lastName("Silva")
                .username("maria.silva")
                .email("maria.silva@email.com")
                .phone(987654321)
                .creditCard(validCreditCard)
                .accessKey(null)
                .build();

        validCreditCard.setUser(validUser);

        // Setup de pacotes
        economyPackage = CarPackage.builder()
                .id(1L)
                .packageName("Economy")
                .pricePerHour(50)
                .build();

        standardPackage = CarPackage.builder()
                .id(2L)
                .packageName("Standard")
                .pricePerHour(100)
                .build();

        luxuryPackage = CarPackage.builder()
                .id(3L)
                .packageName("Luxury")
                .pricePerHour(300)
                .build();
    }

    // ==================== PARTICIONAMENTO: Casos Válidos ====================

    @Test
    @DisplayName("TC1: Pedido bem-sucedido com pacote Economy e 1 hora - partição válida mínima")
    void shouldPlaceOrder_WithEconomyPackageOneHour() {
        // Arrange - Valor mínimo de horas (1)
        String packageName = "Economy";
        Integer hours = 1;
        Long initialBalance = validCreditCard.getAccountBalance();
        Long expectedCost = (long) economyPackage.getPricePerHour() * hours; // 50 * 1 = 50

        when(loggedInUser.getUser()).thenReturn(validUser);
        when(carPackageRepository.findByPackageName(packageName)).thenReturn(Optional.of(economyPackage));
        when(accessKeyRepository.save(any(AccessKey.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        AccessKeyDto result = orderService.submitOrder(packageName, hours);

        // Assert
        assertNotNull(result);
        assertEquals(packageName, result.getCarPackage());
        assertEquals(hours, result.getHours());
        assertEquals(initialBalance - expectedCost, validUser.getCreditCard().getAccountBalance());
        verify(accessKeyRepository, times(1)).save(any(AccessKey.class));
    }

    @Test
    @DisplayName("TC2: Pedido com pacote Standard e múltiplas horas - partição válida")
    void shouldPlaceOrder_WithStandardPackageMultipleHours() {
        // Arrange
        String packageName = "Standard";
        Integer hours = 5;
        Long initialBalance = validCreditCard.getAccountBalance();
        Long expectedCost = (long) standardPackage.getPricePerHour() * hours; // 100 * 5 = 500

        when(loggedInUser.getUser()).thenReturn(validUser);
        when(carPackageRepository.findByPackageName(packageName)).thenReturn(Optional.of(standardPackage));
        when(accessKeyRepository.save(any(AccessKey.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        AccessKeyDto result = orderService.submitOrder(packageName, hours);

        // Assert
        assertNotNull(result);
        assertEquals(packageName, result.getCarPackage());
        assertEquals(hours, result.getHours());
        assertEquals(initialBalance - expectedCost, validUser.getCreditCard().getAccountBalance());
    }

    @Test
    @DisplayName("TC3: Pedido com pacote Luxury e período longo - partição válida alta")
    void shouldPlaceOrder_WithLuxuryPackageLongPeriod() {
        // Arrange
        String packageName = "Luxury";
        Integer hours = 10;
        Long initialBalance = validCreditCard.getAccountBalance();
        Long expectedCost = (long) luxuryPackage.getPricePerHour() * hours; // 300 * 10 = 3000

        when(loggedInUser.getUser()).thenReturn(validUser);
        when(carPackageRepository.findByPackageName(packageName)).thenReturn(Optional.of(luxuryPackage));
        when(accessKeyRepository.save(any(AccessKey.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        AccessKeyDto result = orderService.submitOrder(packageName, hours);

        // Assert
        assertEquals(initialBalance - expectedCost, validUser.getCreditCard().getAccountBalance());
        assertEquals(2000L, validUser.getCreditCard().getAccountBalance()); // 5000 - 3000 = 2000
    }

    @Test
    @DisplayName("TC4: Pedido que usa exatamente todo o saldo - valor limite superior exato")
    void shouldPlaceOrder_UsingExactBalance() {
        // Arrange - Saldo exato para o pedido
        String packageName = "Economy";
        Integer hours = 100; // 50 * 100 = 5000 (saldo total)
        
        when(loggedInUser.getUser()).thenReturn(validUser);
        when(carPackageRepository.findByPackageName(packageName)).thenReturn(Optional.of(economyPackage));
        when(accessKeyRepository.save(any(AccessKey.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        AccessKeyDto result = orderService.submitOrder(packageName, hours);

        // Assert
        assertNotNull(result);
        assertEquals(0L, validUser.getCreditCard().getAccountBalance()); // Saldo zerado
    }

    @Test
    @DisplayName("TC5: Pedido com saldo ligeiramente superior ao custo - valor limite")
    void shouldPlaceOrder_WithBalanceJustAboveCost() {
        // Arrange - Saldo um pouco maior que o custo
        validCreditCard.setAccountBalance(1001L);
        String packageName = "Standard";
        Integer hours = 10; // 100 * 10 = 1000

        when(loggedInUser.getUser()).thenReturn(validUser);
        when(carPackageRepository.findByPackageName(packageName)).thenReturn(Optional.of(standardPackage));
        when(accessKeyRepository.save(any(AccessKey.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        AccessKeyDto result = orderService.submitOrder(packageName, hours);

        // Assert
        assertEquals(1L, validUser.getCreditCard().getAccountBalance()); // 1001 - 1000 = 1
    }

    // ==================== PARTICIONAMENTO: Casos Inválidos ====================

    @Test
    @DisplayName("TC6: Falha ao fazer pedido sem cartão de crédito - partição inválida")
    void shouldThrowException_WhenUserHasNoCreditCard() {
        // Arrange - Usuário sem cartão
        validUser.setCreditCard(null);

        when(loggedInUser.getUser()).thenReturn(validUser);

        // Act & Assert
        NoCreditCardException exception = assertThrows(NoCreditCardException.class,
                () -> orderService.submitOrder("Economy", 5));

        assertEquals("You Do Not Have Credit Card!", exception.getMessage());
        verify(carPackageRepository, never()).findByPackageName(anyString());
        verify(accessKeyRepository, never()).save(any(AccessKey.class));
    }

    @Test
    @DisplayName("TC7: Falha ao fazer segundo pedido - partição inválida (pedido existente)")
    void shouldThrowException_WhenUserAlreadyHasOrder() {
        // Arrange - Usuário já possui um pedido
        AccessKey existingKey = AccessKey.builder()
                .id(1L)
                .carPackage("Standard")
                .hours(3)
                .user(validUser)
                .build();
        validUser.setAccessKey(existingKey);

        when(loggedInUser.getUser()).thenReturn(validUser);

        // Act & Assert
        ExistingOrderException exception = assertThrows(ExistingOrderException.class,
                () -> orderService.submitOrder("Luxury", 2));

        assertEquals("You Have Already Placed An Order!", exception.getMessage());
        verify(carPackageRepository, never()).findByPackageName(anyString());
        verify(accessKeyRepository, never()).save(any(AccessKey.class));
    }

    @Test
    @DisplayName("TC8: Falha com pacote inexistente - partição inválida")
    void shouldThrowException_WhenPackageDoesNotExist() {
        // Arrange - Pacote não existe
        String nonExistentPackage = "SuperPremium";

        when(loggedInUser.getUser()).thenReturn(validUser);
        when(carPackageRepository.findByPackageName(nonExistentPackage)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class,
                () -> orderService.submitOrder(nonExistentPackage, 5));

        verify(accessKeyRepository, never()).save(any(AccessKey.class));
    }

    @Test
    @DisplayName("TC9: Falha com saldo insuficiente - valor limite inferior inválido")
    void shouldThrowException_WhenInsufficientFunds() {
        // Arrange - Saldo insuficiente (falta 1 unidade)
        validCreditCard.setAccountBalance(999L);
        String packageName = "Standard";
        Integer hours = 10; // 100 * 10 = 1000 (precisa de 1000, mas tem 999)

        when(loggedInUser.getUser()).thenReturn(validUser);
        when(carPackageRepository.findByPackageName(packageName)).thenReturn(Optional.of(standardPackage));

        // Act & Assert
        InsufficientFundsException exception = assertThrows(InsufficientFundsException.class,
                () -> orderService.submitOrder(packageName, hours));

        assertEquals("You Do Not Have Enough Money!", exception.getMessage());
        assertEquals(999L, validUser.getCreditCard().getAccountBalance()); // Saldo não alterado
        verify(accessKeyRepository, never()).save(any(AccessKey.class));
    }

    @Test
    @DisplayName("TC10: Falha com saldo zero e pedido qualquer - valor limite zero")
    void shouldThrowException_WhenBalanceIsZero() {
        // Arrange - Saldo zerado
        validCreditCard.setAccountBalance(0L);

        when(loggedInUser.getUser()).thenReturn(validUser);
        when(carPackageRepository.findByPackageName("Economy")).thenReturn(Optional.of(economyPackage));

        // Act & Assert
        assertThrows(InsufficientFundsException.class,
                () -> orderService.submitOrder("Economy", 1));

        assertEquals(0L, validUser.getCreditCard().getAccountBalance());
    }

    // ==================== TESTES DE PROPRIEDADE ====================

    @Test
    @DisplayName("TC11: Propriedade - Saldo sempre deve ser decrementado corretamente")
    void shouldAlwaysDecrementBalance_ByCorrectAmount() {
        // Arrange
        String packageName = "Standard";
        Integer hours = 3;
        Long initialBalance = validCreditCard.getAccountBalance();
        Long expectedCost = (long) standardPackage.getPricePerHour() * hours;

        when(loggedInUser.getUser()).thenReturn(validUser);
        when(carPackageRepository.findByPackageName(packageName)).thenReturn(Optional.of(standardPackage));
        when(accessKeyRepository.save(any(AccessKey.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        orderService.submitOrder(packageName, hours);

        // Assert - Propriedade: saldo atual = saldo inicial - (preço * horas)
        Long currentBalance = validUser.getCreditCard().getAccountBalance();
        assertEquals(initialBalance - expectedCost, currentBalance);
        assertEquals(initialBalance - (standardPackage.getPricePerHour() * hours), currentBalance);
    }

    @Test
    @DisplayName("TC12: Propriedade - AccessKey deve sempre ser associada ao usuário")
    void shouldAlwaysAssociateAccessKey_WithUser() {
        // Arrange
        when(loggedInUser.getUser()).thenReturn(validUser);
        when(carPackageRepository.findByPackageName("Economy")).thenReturn(Optional.of(economyPackage));
        when(accessKeyRepository.save(any(AccessKey.class))).thenAnswer(invocation -> {
            AccessKey key = invocation.getArgument(0);
            key.setId(1L);
            // O serviço define a associação antes de salvar
            return key;
        });

        // Act
        orderService.submitOrder("Economy", 2);

        // Assert - Propriedade: usuário deve ter AccessKey associada
        assertNotNull(validUser.getAccessKey());
        assertEquals(validUser, validUser.getAccessKey().getUser());
    }

    @Test
    @DisplayName("TC13: Propriedade - Dados do pedido devem corresponder aos parâmetros informados")
    void shouldPreserveOrderData_InAccessKey() {
        // Arrange
        String packageName = "Luxury";
        Integer hours = 7;

        when(loggedInUser.getUser()).thenReturn(validUser);
        when(carPackageRepository.findByPackageName(packageName)).thenReturn(Optional.of(luxuryPackage));
        when(accessKeyRepository.save(any(AccessKey.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        AccessKeyDto result = orderService.submitOrder(packageName, hours);

        // Assert - Propriedade: dados do pedido devem ser preservados
        assertEquals(packageName, result.getCarPackage());
        assertEquals(hours, result.getHours());
        assertEquals(packageName, validUser.getAccessKey().getCarPackage());
        assertEquals(hours, validUser.getAccessKey().getHours());
    }

    @Test
    @DisplayName("TC14: Propriedade - Custo calculado deve ser deterministico")
    void shouldCalculateCost_Deterministically() {
        // Arrange
        String packageName = "Standard";
        Integer hours = 4;
        Integer pricePerHour = standardPackage.getPricePerHour();

        when(loggedInUser.getUser()).thenReturn(validUser);
        when(carPackageRepository.findByPackageName(packageName)).thenReturn(Optional.of(standardPackage));
        when(accessKeyRepository.save(any(AccessKey.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Long initialBalance1 = validCreditCard.getAccountBalance();

        // Act - Primeira execução
        orderService.submitOrder(packageName, hours);
        Long balanceAfter1 = validUser.getCreditCard().getAccountBalance();

        // Resetar para testar determinismo
        validUser.setAccessKey(null);
        validCreditCard.setAccountBalance(initialBalance1);
        Long initialBalance2 = validCreditCard.getAccountBalance();

        // Act - Segunda execução
        orderService.submitOrder(packageName, hours);
        Long balanceAfter2 = validUser.getCreditCard().getAccountBalance();

        // Assert - Propriedade: cálculo deve ser determinístico
        assertEquals(initialBalance1 - (pricePerHour * hours), balanceAfter1);
        assertEquals(initialBalance2 - (pricePerHour * hours), balanceAfter2);
        assertEquals(balanceAfter1, balanceAfter2);
    }

    @Test
    @DisplayName("TC15: Propriedade - Pedido nunca deve deixar saldo negativo")
    void shouldNeverAllowNegativeBalance_AfterOrder() {
        // Arrange - Tentar vários cenários
        String packageName = "Luxury";
        Integer hours = 20; // 300 * 20 = 6000 > 5000 (saldo)

        when(loggedInUser.getUser()).thenReturn(validUser);
        when(carPackageRepository.findByPackageName(packageName)).thenReturn(Optional.of(luxuryPackage));

        // Act & Assert - Propriedade: saldo nunca deve ficar negativo
        assertThrows(InsufficientFundsException.class,
                () -> orderService.submitOrder(packageName, hours));

        // Verificar que o saldo permanece não-negativo
        assertTrue(validUser.getCreditCard().getAccountBalance() >= 0,
                "Balance should never be negative");
        assertEquals(5000L, validUser.getCreditCard().getAccountBalance(),
                "Balance should remain unchanged after failed transaction");
    }
}
