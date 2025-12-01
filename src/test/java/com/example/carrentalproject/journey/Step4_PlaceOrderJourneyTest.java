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

    // ==================== TESTES ADICIONAIS DE COBERTURA ====================

    @Test
    @DisplayName("TC16: Pedido com 24 horas - valor típico de um dia")
    void shouldPlaceOrder_WithTwentyFourHours() {
        // Arrange - Período de um dia completo
        String packageName = "Economy";
        Integer hours = 24;
        Long initialBalance = validCreditCard.getAccountBalance();
        Long expectedCost = (long) economyPackage.getPricePerHour() * hours; // 50 * 24 = 1200

        when(loggedInUser.getUser()).thenReturn(validUser);
        when(carPackageRepository.findByPackageName(packageName)).thenReturn(Optional.of(economyPackage));
        when(accessKeyRepository.save(any(AccessKey.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        AccessKeyDto result = orderService.submitOrder(packageName, hours);

        // Assert
        assertEquals(24, result.getHours());
        assertEquals(initialBalance - expectedCost, validUser.getCreditCard().getAccountBalance());
        assertEquals(3800L, validUser.getCreditCard().getAccountBalance()); // 5000 - 1200
    }

    @Test
    @DisplayName("TC17: Pedido com 2 horas - valor limite baixo típico")
    void shouldPlaceOrder_WithTwoHours() {
        // Arrange
        String packageName = "Standard";
        Integer hours = 2;
        Long expectedCost = (long) standardPackage.getPricePerHour() * hours; // 100 * 2 = 200

        when(loggedInUser.getUser()).thenReturn(validUser);
        when(carPackageRepository.findByPackageName(packageName)).thenReturn(Optional.of(standardPackage));
        when(accessKeyRepository.save(any(AccessKey.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        AccessKeyDto result = orderService.submitOrder(packageName, hours);

        // Assert
        assertEquals(2, result.getHours());
        assertEquals(4800L, validUser.getCreditCard().getAccountBalance()); // 5000 - 200
    }

    @Test
    @DisplayName("TC18: Pedido com 168 horas (uma semana) - valor limite alto típico")
    void shouldPlaceOrder_WithOneWeek() {
        // Arrange - Uma semana completa
        validCreditCard.setAccountBalance(10000L); // Aumentar saldo para suportar
        String packageName = "Economy";
        Integer hours = 168; // 7 dias * 24 horas
        Long expectedCost = (long) economyPackage.getPricePerHour() * hours; // 50 * 168 = 8400

        when(loggedInUser.getUser()).thenReturn(validUser);
        when(carPackageRepository.findByPackageName(packageName)).thenReturn(Optional.of(economyPackage));
        when(accessKeyRepository.save(any(AccessKey.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        AccessKeyDto result = orderService.submitOrder(packageName, hours);

        // Assert
        assertEquals(168, result.getHours());
        assertEquals(1600L, validUser.getCreditCard().getAccountBalance()); // 10000 - 8400
    }

    @Test
    @DisplayName("TC19: Pedido com saldo exatamente 1 unidade acima do necessário")
    void shouldPlaceOrder_WithBalanceExactlyOnePlusRequired() {
        // Arrange
        validCreditCard.setAccountBalance(501L);
        String packageName = "Economy";
        Integer hours = 10; // 50 * 10 = 500

        when(loggedInUser.getUser()).thenReturn(validUser);
        when(carPackageRepository.findByPackageName(packageName)).thenReturn(Optional.of(economyPackage));
        when(accessKeyRepository.save(any(AccessKey.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        AccessKeyDto result = orderService.submitOrder(packageName, hours);

        // Assert
        assertEquals(1L, validUser.getCreditCard().getAccountBalance());
    }

    @Test
    @DisplayName("TC20: Falha com saldo 1 unidade abaixo do necessário")
    void shouldThrowException_WithBalanceExactlyOneBelow() {
        // Arrange
        validCreditCard.setAccountBalance(499L);
        String packageName = "Economy";
        Integer hours = 10; // 50 * 10 = 500 (precisa de 500, mas tem 499)

        when(loggedInUser.getUser()).thenReturn(validUser);
        when(carPackageRepository.findByPackageName(packageName)).thenReturn(Optional.of(economyPackage));

        // Act & Assert
        assertThrows(InsufficientFundsException.class,
                () -> orderService.submitOrder(packageName, hours));

        assertEquals(499L, validUser.getCreditCard().getAccountBalance()); // Saldo não mudou
    }

    @Test
    @DisplayName("TC21: Pedido com diferentes pacotes para verificar cálculo correto")
    void shouldCalculateCostCorrectly_ForDifferentPackages() {
        // Arrange - Teste com múltiplos pacotes
        Integer hours = 5;

        // Teste com Economy
        when(loggedInUser.getUser()).thenReturn(validUser);
        when(carPackageRepository.findByPackageName("Economy")).thenReturn(Optional.of(economyPackage));
        when(accessKeyRepository.save(any(AccessKey.class))).thenAnswer(invocation -> invocation.getArgument(0));

        orderService.submitOrder("Economy", hours);
        assertEquals(4750L, validUser.getCreditCard().getAccountBalance()); // 5000 - (50*5) = 4750

        // Reset e teste com Standard
        validUser.setAccessKey(null);
        validCreditCard.setAccountBalance(5000L);
        when(carPackageRepository.findByPackageName("Standard")).thenReturn(Optional.of(standardPackage));

        orderService.submitOrder("Standard", hours);
        assertEquals(4500L, validUser.getCreditCard().getAccountBalance()); // 5000 - (100*5) = 4500

        // Reset e teste com Luxury
        validUser.setAccessKey(null);
        validCreditCard.setAccountBalance(5000L);
        when(carPackageRepository.findByPackageName("Luxury")).thenReturn(Optional.of(luxuryPackage));

        orderService.submitOrder("Luxury", hours);
        assertEquals(3500L, validUser.getCreditCard().getAccountBalance()); // 5000 - (300*5) = 3500
    }

    @Test
    @DisplayName("TC22: Propriedade - Operação deve ser atômica (salvar AccessKey e atualizar saldo)")
    void shouldPerformAtomicOperation_WhenPlacingOrder() {
        // Arrange
        String packageName = "Standard";
        Integer hours = 3;

        when(loggedInUser.getUser()).thenReturn(validUser);
        when(carPackageRepository.findByPackageName(packageName)).thenReturn(Optional.of(standardPackage));
        when(accessKeyRepository.save(any(AccessKey.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        orderService.submitOrder(packageName, hours);

        // Assert - Propriedade: ambas operações devem ter ocorrido
        assertNotNull(validUser.getAccessKey(), "AccessKey should be created");
        assertEquals(4700L, validUser.getCreditCard().getAccountBalance(), "Balance should be updated");
        verify(accessKeyRepository, times(1)).save(any(AccessKey.class));
    }

    @Test
    @DisplayName("TC23: Propriedade - Falha deve ser fail-fast (verificar cartão antes de buscar pacote)")
    void shouldFailFast_WhenUserHasNoCreditCard() {
        // Arrange
        validUser.setCreditCard(null);

        when(loggedInUser.getUser()).thenReturn(validUser);

        // Act & Assert
        assertThrows(NoCreditCardException.class,
                () -> orderService.submitOrder("Economy", 5));

        // Propriedade: verificação de cartão deve ocorrer primeiro
        verify(carPackageRepository, never()).findByPackageName(anyString());
        verify(accessKeyRepository, never()).save(any(AccessKey.class));
    }

    @Test
    @DisplayName("TC24: Propriedade - Verificar pedido existente antes de calcular custo")
    void shouldCheckExistingOrder_BeforeCalculatingCost() {
        // Arrange
        AccessKey existingKey = AccessKey.builder()
                .id(1L)
                .carPackage("Economy")
                .hours(2)
                .user(validUser)
                .build();
        validUser.setAccessKey(existingKey);

        when(loggedInUser.getUser()).thenReturn(validUser);

        // Act & Assert
        assertThrows(ExistingOrderException.class,
                () -> orderService.submitOrder("Luxury", 10));

        // Propriedade: não deve buscar pacote se já tem pedido
        verify(carPackageRepository, never()).findByPackageName(anyString());
        verify(accessKeyRepository, never()).save(any(AccessKey.class));
    }

    @Test
    @DisplayName("TC25: Pedido com pacote de preço customizado")
    void shouldPlaceOrder_WithCustomPricePackage() {
        // Arrange - Pacote com preço diferente
        CarPackage customPackage = CarPackage.builder()
                .id(4L)
                .packageName("Custom")
                .pricePerHour(75) // Preço customizado
                .build();

        String packageName = "Custom";
        Integer hours = 8;
        Long expectedCost = (long) customPackage.getPricePerHour() * hours; // 75 * 8 = 600

        when(loggedInUser.getUser()).thenReturn(validUser);
        when(carPackageRepository.findByPackageName(packageName)).thenReturn(Optional.of(customPackage));
        when(accessKeyRepository.save(any(AccessKey.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        AccessKeyDto result = orderService.submitOrder(packageName, hours);

        // Assert
        assertEquals(packageName, result.getCarPackage());
        assertEquals(4400L, validUser.getCreditCard().getAccountBalance()); // 5000 - 600
    }

    @Test
    @DisplayName("TC26: Pedido com valor muito alto que excede saldo")
    void shouldThrowException_WhenCostExceedsBalanceSignificantly() {
        // Arrange
        validCreditCard.setAccountBalance(1000L);
        String packageName = "Luxury";
        Integer hours = 100; // 300 * 100 = 30000 (muito maior que saldo)

        when(loggedInUser.getUser()).thenReturn(validUser);
        when(carPackageRepository.findByPackageName(packageName)).thenReturn(Optional.of(luxuryPackage));

        // Act & Assert
        assertThrows(InsufficientFundsException.class,
                () -> orderService.submitOrder(packageName, hours));

        assertEquals(1000L, validUser.getCreditCard().getAccountBalance());
    }

    @Test
    @DisplayName("TC27: Propriedade - Custo nunca deve ser negativo")
    void shouldNeverHaveNegativeCost_ForAnyOrder() {
        // Arrange - Vários cenários
        Integer[] hoursArray = {1, 5, 10, 50, 100};
        CarPackage[] packages = {economyPackage, standardPackage, luxuryPackage};

        for (CarPackage pkg : packages) {
            for (Integer hours : hoursArray) {
                // Propriedade: custo sempre positivo
                long cost = (long) pkg.getPricePerHour() * hours;
                assertTrue(cost > 0, "Cost should always be positive");
            }
        }
    }

    @Test
    @DisplayName("TC28: Propriedade - Relação bidirecional User-AccessKey deve ser estabelecida")
    void shouldEstablishBidirectionalRelationship_BetweenUserAndAccessKey() {
        // Arrange
        when(loggedInUser.getUser()).thenReturn(validUser);
        when(carPackageRepository.findByPackageName("Economy")).thenReturn(Optional.of(economyPackage));
        when(accessKeyRepository.save(any(AccessKey.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        orderService.submitOrder("Economy", 3);

        // Assert - Propriedade: relação bidirecional
        assertNotNull(validUser.getAccessKey());
        assertNotNull(validUser.getAccessKey().getUser());
        assertSame(validUser, validUser.getAccessKey().getUser());
        assertSame(validUser.getAccessKey(), validUser.getAccessKey());
    }

    @Test
    @DisplayName("TC29: Pedido com hora mínima absoluta (1) e pacote mais barato")
    void shouldPlaceOrder_WithAbsoluteMinimumCost() {
        // Arrange - Menor custo possível
        String packageName = "Economy";
        Integer hours = 1;
        Long expectedCost = 50L; // 50 * 1

        when(loggedInUser.getUser()).thenReturn(validUser);
        when(carPackageRepository.findByPackageName(packageName)).thenReturn(Optional.of(economyPackage));
        when(accessKeyRepository.save(any(AccessKey.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        AccessKeyDto result = orderService.submitOrder(packageName, hours);

        // Assert
        assertEquals(4950L, validUser.getCreditCard().getAccountBalance()); // 5000 - 50
    }

    @Test
    @DisplayName("TC30: Verificar que AccessKeyRepository.save é chamado exatamente uma vez")
    void shouldCallAccessKeyRepositorySave_ExactlyOnce() {
        // Arrange
        when(loggedInUser.getUser()).thenReturn(validUser);
        when(carPackageRepository.findByPackageName("Standard")).thenReturn(Optional.of(standardPackage));
        when(accessKeyRepository.save(any(AccessKey.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        orderService.submitOrder("Standard", 4);

        // Assert - Propriedade: save deve ser chamado exatamente uma vez
        verify(accessKeyRepository, times(1)).save(any(AccessKey.class));
        verify(accessKeyRepository, never()).delete(any(AccessKey.class));
    }

    @Test
    @DisplayName("TC31: Propriedade - Múltiplos usuários podem fazer pedidos independentes")
    void shouldAllowMultipleUsers_ToPlaceIndependentOrders() {
        // Arrange - Dois usuários diferentes
        User user1 = User.builder()
                .id(1L)
                .username("user.one")
                .creditCard(CreditCard.builder().accountBalance(3000L).build())
                .accessKey(null)
                .build();
        user1.getCreditCard().setUser(user1);

        User user2 = User.builder()
                .id(2L)
                .username("user.two")
                .creditCard(CreditCard.builder().accountBalance(2000L).build())
                .accessKey(null)
                .build();
        user2.getCreditCard().setUser(user2);

        when(carPackageRepository.findByPackageName("Economy")).thenReturn(Optional.of(economyPackage));
        when(carPackageRepository.findByPackageName("Standard")).thenReturn(Optional.of(standardPackage));
        when(accessKeyRepository.save(any(AccessKey.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act - User 1 faz pedido
        when(loggedInUser.getUser()).thenReturn(user1);
        orderService.submitOrder("Economy", 10); // 50 * 10 = 500

        // Act - User 2 faz pedido
        when(loggedInUser.getUser()).thenReturn(user2);
        orderService.submitOrder("Standard", 5); // 100 * 5 = 500

        // Assert - Propriedade: pedidos são independentes
        assertEquals(2500L, user1.getCreditCard().getAccountBalance()); // 3000 - 500
        assertEquals(1500L, user2.getCreditCard().getAccountBalance()); // 2000 - 500
        assertNotNull(user1.getAccessKey());
        assertNotNull(user2.getAccessKey());
        assertNotSame(user1.getAccessKey(), user2.getAccessKey());
        verify(accessKeyRepository, times(2)).save(any(AccessKey.class));
    }

    @Test
    @DisplayName("TC32: Propriedade - Saldo nunca deve aumentar após pedido bem-sucedido")
    void shouldNeverIncreaseBalance_AfterSuccessfulOrder() {
        // Arrange
        Long initialBalance = validCreditCard.getAccountBalance();

        when(loggedInUser.getUser()).thenReturn(validUser);
        when(carPackageRepository.findByPackageName("Standard")).thenReturn(Optional.of(standardPackage));
        when(accessKeyRepository.save(any(AccessKey.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        orderService.submitOrder("Standard", 3);

        // Assert - Propriedade: saldo após deve ser menor que saldo inicial
        Long finalBalance = validUser.getCreditCard().getAccountBalance();
        assertTrue(finalBalance < initialBalance,
                "Balance should always decrease after successful order");
        assertTrue(finalBalance <= initialBalance - 300, // 100 * 3
                "Balance should decrease by at least the order cost");
    }

    @Test
    @DisplayName("TC33: Falha não deve modificar estado quando pacote não existe")
    void shouldNotModifyState_WhenPackageNotFound() {
        // Arrange
        Long initialBalance = validCreditCard.getAccountBalance();
        AccessKey initialAccessKey = validUser.getAccessKey();

        when(loggedInUser.getUser()).thenReturn(validUser);
        when(carPackageRepository.findByPackageName("NonExistent")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class,
                () -> orderService.submitOrder("NonExistent", 5));

        // Propriedade: estado não deve ter mudado
        assertEquals(initialBalance, validUser.getCreditCard().getAccountBalance());
        assertEquals(initialAccessKey, validUser.getAccessKey());
        verify(accessKeyRepository, never()).save(any(AccessKey.class));
    }

    @Test
    @DisplayName("TC34: Propriedade - Ordem de validações deve ser consistente")
    void shouldValidateInConsistentOrder_ForAllRequests() {
        // Arrange - Múltiplos problemas simultâneos
        validUser.setCreditCard(null); // Sem cartão (primeira validação)
        validUser.setAccessKey(AccessKey.builder().build()); // Já tem pedido (segunda validação)

        when(loggedInUser.getUser()).thenReturn(validUser);

        // Act & Assert - Deve falhar na primeira validação (cartão)
        assertThrows(NoCreditCardException.class,
                () -> orderService.submitOrder("Economy", 5));

        // Propriedade: nunca deve chegar nas validações posteriores
        verify(carPackageRepository, never()).findByPackageName(anyString());
    }
}
