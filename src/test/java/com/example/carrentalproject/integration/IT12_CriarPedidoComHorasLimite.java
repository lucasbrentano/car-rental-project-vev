package com.example.carrentalproject.integration;

import com.example.carrentalproject.domain.*;
import com.example.carrentalproject.dto.AccessKeyDto;
import com.example.carrentalproject.repository.*;
import com.example.carrentalproject.security.LoggedInUser;
import com.example.carrentalproject.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * IT12 - Cenário 7: Pedidos e Timestamps - CriarPedidoComHorasLimite
 * 
 * Objetivo: Validar que o sistema aceita pedidos com hours no limite mínimo (1)
 * e máximo (720), calculando o preço corretamente em ambos os casos.
 * Valida: Lógica de cálculo + valor limite
 */
@SpringBootTest
@Transactional
@DisplayName("IT12 - Criar Pedido Com Horas Limite")
public class IT12_CriarPedidoComHorasLimite {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CreditCardRepository creditCardRepository;

    @Autowired
    private CarPackageRepository carPackageRepository;

    @Autowired
    private AccessKeyRepository accessKeyRepository;

    @Autowired
    private OrderService orderService;

    @MockBean
    private LoggedInUser loggedInUser;

    private User testUser;
    private CarPackage testPackage;
    private final String testUsername = "testuser_it12";

    @BeforeEach
    void setUp() {
        // Limpar dados anteriores
        accessKeyRepository.deleteAll();
        creditCardRepository.deleteAll();
        userRepository.deleteAll();
        carPackageRepository.deleteAll();

        // Criar usuário de teste
        testUser = User.builder()
                .firstName("Test")
                .lastName("UserIT12")
                .username(testUsername)
                .password("password123")
                .email("it12@test.com")
                .phone(123456789)
                .build();
        testUser = userRepository.save(testUser);

        // Criar cartão de crédito com saldo suficiente para teste máximo (720h * 100 = 72000)
        CreditCard creditCard = CreditCard.builder()
                .cardNumber(1234567890123456L)
                .month(12)
                .year(2028)
                .CVV(123)
                .accountBalance(100000L) // Saldo suficiente para 720 horas
                .user(testUser)
                .build();
        creditCard = creditCardRepository.save(creditCard);

        testUser.setCreditCard(creditCard);
        testUser = userRepository.save(testUser);

        // Criar pacote de teste
        testPackage = CarPackage.builder()
                .packageName("BASIC")
                .pricePerHour(100)
                .build();
        testPackage = carPackageRepository.save(testPackage);

        System.out.println("✓ Setup completo: Usuário com saldo 100000, pacote BASIC (100/hora)");
    }

    @Test
    @DisplayName("Deve criar pedido com hours = 1 (mínimo) e calcular preço correto")
    void deveCriarPedidoComHoraMinima() {
        // Given: Usuário autenticado com saldo suficiente
        when(loggedInUser.getUser()).thenReturn(testUser);
        
        Integer horasMinimas = 1;
        Integer precoEsperado = testPackage.getPricePerHour() * horasMinimas; // 100 * 1 = 100

        // When: Criar pedido com 1 hora
        AccessKeyDto accessKey = orderService.submitOrder("BASIC", horasMinimas);

        // Then: AccessKey criado com 1 hora
        assertNotNull(accessKey);
        assertEquals("BASIC", accessKey.getCarPackage());
        assertEquals(horasMinimas, accessKey.getHours());

        // Verificar débito no cartão
        User userAtualizado = userRepository.findById(testUser.getId()).orElseThrow();
        Long saldoEsperado = 100000L - precoEsperado;
        assertEquals(saldoEsperado, userAtualizado.getCreditCard().getAccountBalance());
        
        System.out.println("✓ Pedido criado com 1 hora (mínimo)");
        System.out.println("✓ Preço calculado: " + precoEsperado);
        System.out.println("✓ Saldo restante: " + saldoEsperado);
    }

    @Test
    @DisplayName("Deve criar pedido com hours = 720 (máximo) e calcular preço correto")
    void deveCriarPedidoComHoraMaxima() {
        // Given: Usuário autenticado com saldo suficiente
        when(loggedInUser.getUser()).thenReturn(testUser);
        
        Integer horasMaximas = 720; // 30 dias
        Integer precoEsperado = testPackage.getPricePerHour() * horasMaximas; // 100 * 720 = 72000

        // When: Criar pedido com 720 horas
        AccessKeyDto accessKey = orderService.submitOrder("BASIC", horasMaximas);

        // Then: AccessKey criado com 720 horas
        assertNotNull(accessKey);
        assertEquals("BASIC", accessKey.getCarPackage());
        assertEquals(horasMaximas, accessKey.getHours());

        // Verificar débito no cartão
        User userAtualizado = userRepository.findById(testUser.getId()).orElseThrow();
        Long saldoEsperado = 100000L - precoEsperado;
        assertEquals(saldoEsperado, userAtualizado.getCreditCard().getAccountBalance());
        
        System.out.println("✓ Pedido criado com 720 horas (máximo - 30 dias)");
        System.out.println("✓ Preço calculado: " + precoEsperado);
        System.out.println("✓ Saldo restante: " + saldoEsperado);
    }

    @Test
    @DisplayName("Deve validar cálculo de preço com diferentes valores de hours")
    void deveValidarCalculoPrecoComDiferentesValores() {
        // Given: Múltiplos testes com diferentes hours
        when(loggedInUser.getUser()).thenReturn(testUser);

        // Test 1: hours = 24 (1 dia)
        AccessKeyDto accessKey24h = orderService.submitOrder("BASIC", 24);
        assertEquals(24, accessKey24h.getHours());
        
        // Limpar AccessKey para próximo teste
        testUser = userRepository.findById(testUser.getId()).orElseThrow();
        accessKeyRepository.delete(testUser.getAccessKey());
        testUser.setAccessKey(null);
        testUser = userRepository.save(testUser);
        when(loggedInUser.getUser()).thenReturn(testUser);

        // Test 2: hours = 168 (1 semana)
        AccessKeyDto accessKey168h = orderService.submitOrder("BASIC", 168);
        assertEquals(168, accessKey168h.getHours());

        // Verificar saldo final: 100000 - (24*100) - (168*100) = 100000 - 2400 - 16800 = 80800
        User userFinal = userRepository.findById(testUser.getId()).orElseThrow();
        assertEquals(80800L, userFinal.getCreditCard().getAccountBalance());

        System.out.println("✓ Pedido 1: 24 horas = R$ 2400");
        System.out.println("✓ Pedido 2: 168 horas = R$ 16800");
        System.out.println("✓ Total debitado: R$ 19200");
        System.out.println("✓ Saldo final: R$ 80800");
    }

    @Test
    @DisplayName("Deve validar limites com pacote de preço diferente")
    void deveValidarLimitesComPacotePrecoDiferente() {
        // Given: Pacote PREMIUM com preço maior
        CarPackage premiumPackage = CarPackage.builder()
                .packageName("PREMIUM")
                .pricePerHour(300)
                .build();
        premiumPackage = carPackageRepository.save(premiumPackage);

        when(loggedInUser.getUser()).thenReturn(testUser);

        // When: Criar pedido com 1 hora no pacote PREMIUM
        AccessKeyDto accessKey = orderService.submitOrder("PREMIUM", 1);

        // Then: Preço = 300 * 1 = 300
        assertEquals("PREMIUM", accessKey.getCarPackage());
        assertEquals(1, accessKey.getHours());
        
        User userAtualizado = userRepository.findById(testUser.getId()).orElseThrow();
        assertEquals(99700L, userAtualizado.getCreditCard().getAccountBalance()); // 100000 - 300

        System.out.println("✓ Pedido PREMIUM: 1 hora = R$ 300");
        System.out.println("✓ Cálculo validado com preço diferente");
    }
}
