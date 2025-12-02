package com.example.carrentalproject.integration;

import com.example.carrentalproject.constant.FuelType;
import com.example.carrentalproject.constant.GearBoxType;
import com.example.carrentalproject.domain.Car;
import com.example.carrentalproject.domain.CarPackage;
import com.example.carrentalproject.domain.CarParameters;
import com.example.carrentalproject.domain.CreditCard;
import com.example.carrentalproject.domain.Role;
import com.example.carrentalproject.domain.User;
import com.example.carrentalproject.dto.UserInDto;
import com.example.carrentalproject.exception.ExistingOrderException;
import com.example.carrentalproject.repository.*;
import com.example.carrentalproject.service.OrderService;
import com.example.carrentalproject.service.RegistrationService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * IT18 - Cenário 9: Teste que VALIDA COMPORTAMENTO EXISTENTE
 * 
 * PROTEÇÃO VALIDADA: OrderService.submitOrder() impede pedidos duplicados corretamente
 * 
 * COMPORTAMENTO CORRETO: Sistema lança ExistingOrderException quando:
 * - Usuário já possui AccessKey (linha 52 OrderService)
 * - Tentativa de criar segundo pedido sem finalizar o primeiro
 * 
 * EVIDÊNCIA: 
 * - OrderService linha 52: if(user.getAccessKey() != null)
 * - AccessKey é OneToOne com User
 * - Um usuário só pode ter um pedido ativo por vez
 * - Usuários diferentes podem ter pedidos simultâneos
 */
@SpringBootTest
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("IT18 - Cenário 9: Validação - Sistema impede pedidos duplicados corretamente")
public class IT18_PedidoDuplicadoParaMesmoUsuario {

    @Autowired
    private OrderService orderService;

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private CarPackageRepository carPackageRepository;

    @Autowired
    private CarParametersRepository carParametersRepository;

    @Autowired
    private CreditCardRepository creditCardRepository;

    @Autowired
    private AccessKeyRepository accessKeyRepository;

    @Autowired
    private RoleRepository roleRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private User testUser;
    private CarPackage testPackage;
    private CarParameters testParameters;
    private Car testCar;

    @BeforeEach
    void setUp() {
        // Criar Role se não existir
        if (roleRepository.findByName("ROLE_USER").isEmpty()) {
            Role userRole = Role.builder()
                    .name("ROLE_USER")
                    .users(new java.util.ArrayList<>())
                    .build();
            roleRepository.save(userRole);
        }

        // Criar usuário com cartão de crédito
        UserInDto userDto = UserInDto.builder()
                .firstName("Roberto")
                .lastName("Lima")
                .username("roberto_teste_it18")
                .email("roberto.it18@test.com")
                .password("Test12345")
                .phone(995111222)
                .build();

        registrationService.registerUser(userDto);
        testUser = userRepository.findByUsername("roberto_teste_it18").orElseThrow();

        CreditCard card = CreditCard.builder()
                .cardNumber(9876543210123456L)
                .month(12)
                .year(2026)
                .CVV(321)
                .accountBalance(200000L) // R$ 2000.00 (suficiente para múltiplos pedidos)
                .build();
        card.setUser(testUser);
        creditCardRepository.save(card);
        // Flush e clear para sincronizar relacionamento no banco
        entityManager.flush();
        entityManager.clear();
        // Recarregar usuário para trazer o CreditCard
        testUser = userRepository.findByUsername("roberto_teste_it18").orElseThrow();

        // Criar parâmetros do carro
        testParameters = CarParameters.builder()
                .fuelType(FuelType.PETROL)
                .gearBoxType(GearBoxType.MANUAL)
                .numberOfDoors(5)
                .numberOfSeats(5)
                .isAirConditioningAvailable(true)
                .build();
        carParametersRepository.save(testParameters);

        testPackage = CarPackage.builder()
                .packageName("Test Package IT18")
                .pricePerHour(50)
                .build();
        carPackageRepository.save(testPackage);

        testCar = Car.builder()
                .brand("Test Brand")
                .model("Test Model")
                .registrationNr("IT181234")
                .carPackage(testPackage)
                .carParameters(testParameters)
                .isAvailable(true)  // DISPONÍVEL
                .build();
        carRepository.save(testCar);

        // Autenticar usuário
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(testUser.getUsername(), null)
        );
    }

    @Test
    @org.junit.jupiter.api.Order(1)
    @DisplayName("PROTEÇÃO: Sistema bloqueia segundo pedido do mesmo usuário")
    void bloqueiaSegundoPedido() {
        // Primeiro pedido - deve funcionar
        assertDoesNotThrow(() -> {
            orderService.submitOrder(testPackage.getPackageName(), 24);
        }, "Primeiro pedido deve ser aceito");

        User userReloaded = userRepository.findByUsername("roberto_teste_it18").orElseThrow();
        assertNotNull(userReloaded.getAccessKey(), "Primeiro pedido deve criar AccessKey");

        // Segundo pedido - deve ser bloqueado
        ExistingOrderException exception = assertThrows(ExistingOrderException.class, () -> {
            orderService.submitOrder(testPackage.getPackageName(), 24);
        }, "Segundo pedido deve lançar ExistingOrderException");

        // Validar mensagem (pode ser "You Have Already Placed An Order!" ou similar)
        String message = exception.getMessage();
        assertTrue(message.contains("Already Placed An Order") ||
                   message.contains("existing order") ||
                   message.contains("already"),
                "Mensagem deve indicar pedido existente, mas foi: " + message);
    }

    @Test
    @org.junit.jupiter.api.Order(2)
    @DisplayName("PROTEÇÃO: Bloqueio funciona independente do pacote escolhido")
    void bloqueiaSegundoPedidoIndependenteDoPacote() {
        // Criar segundo pacote
        CarPackage outroPacote = CarPackage.builder()
                .packageName("Outro Pacote IT18")
                .pricePerHour(60)
                .build();
        carPackageRepository.save(outroPacote);

        Car outroCarro = Car.builder()
                .brand("Outro Brand")
                .model("Outro Model")
                .registrationNr("IT182345")
                .carPackage(outroPacote)
                .carParameters(testParameters)
                .isAvailable(true)
                .build();
        carRepository.save(outroCarro);

        // Primeiro pedido (pacote original)
        assertDoesNotThrow(() -> {
            orderService.submitOrder(testPackage.getPackageName(), 24);
        });

        // Segundo pedido (pacote diferente) - deve ser bloqueado
        assertThrows(ExistingOrderException.class, () -> {
            orderService.submitOrder(outroPacote.getPackageName(), 24);
        }, "Sistema deve bloquear pedido mesmo sendo pacote diferente");
    }

    @Test
    @org.junit.jupiter.api.Order(3)
    @DisplayName("VALIDAÇÃO TÉCNICA: AccessKey é não-nulo após primeiro pedido")
    void validacaoAccessKeyNaoNull() {
        User userAntes = userRepository.findByUsername("roberto_teste_it18").orElseThrow();
        assertNull(userAntes.getAccessKey(), "Inicialmente AccessKey deve ser null");

        // Criar pedido
        assertDoesNotThrow(() -> {
            orderService.submitOrder(testPackage.getPackageName(), 24);
        });

        User userDepois = userRepository.findByUsername("roberto_teste_it18").orElseThrow();
        assertNotNull(userDepois.getAccessKey(), "Após submitOrder, AccessKey não é mais null");

        // Validar que é esse campo que impede o segundo pedido
        assertThrows(ExistingOrderException.class, () -> {
            orderService.submitOrder(testPackage.getPackageName(), 24);
        }, "AccessKey não-nulo bloqueia segundo pedido");
    }

    @Test
    @org.junit.jupiter.api.Order(4)
    @DisplayName("PROTEÇÃO: Saldo suficiente não permite pedido duplicado")
    void saldoSuficienteNaoPermitePedidoDuplicado() {
        // Usuário tem R$ 2000.00 no cartão
        // Pedido de 24h a R$ 50/h = R$ 1200 (primeira vez)
        // Sobram R$ 800 (suficiente para outro pedido)

        // Primeiro pedido
        assertDoesNotThrow(() -> {
            orderService.submitOrder(testPackage.getPackageName(), 24);
        });

        CreditCard card = creditCardRepository.findAll().stream()
                .filter(c -> c.getUser().getUsername().equals("roberto_teste_it18"))
                .findFirst()
                .orElseThrow();
        
        long saldoAposPrimeiro = card.getAccountBalance();
        assertTrue(saldoAposPrimeiro >= 80000L, 
                "Saldo restante (R$ 800+) é suficiente para outro pedido");

        // Segundo pedido - bloqueado por AccessKey, NÃO por saldo
        assertThrows(ExistingOrderException.class, () -> {
            orderService.submitOrder(testPackage.getPackageName(), 24);
        }, "Bloqueio é por AccessKey, não por falta de saldo");
    }

    @Test
    @org.junit.jupiter.api.Order(5)
    @DisplayName("VALIDAÇÃO: Um AccessKey por usuário (OneToOne)")
    void umAccessKeyPorUsuario() {
        // Criar pedido
        assertDoesNotThrow(() -> {
            orderService.submitOrder(testPackage.getPackageName(), 24);
        });

        // Verificar que apenas 1 AccessKey foi criado para este usuário
        long countAccessKeys = accessKeyRepository.findAll().stream()
                .filter(ak -> ak.getUser().getUsername().equals("roberto_teste_it18"))
                .count();

        assertEquals(1, countAccessKeys, "Deve existir exatamente 1 AccessKey para o usuário");

        // Tentativa de segundo pedido não cria novo AccessKey
        assertThrows(ExistingOrderException.class, () -> {
            orderService.submitOrder(testPackage.getPackageName(), 24);
        });

        long countAposSegundo = accessKeyRepository.findAll().stream()
                .filter(ak -> ak.getUser().getUsername().equals("roberto_teste_it18"))
                .count();

        assertEquals(1, countAposSegundo, "Ainda deve haver apenas 1 AccessKey");
    }

    @Test
    @org.junit.jupiter.api.Order(6)
    @DisplayName("CONTRASTE: Usuários diferentes podem ter pedidos simultâneos")
    void usuariosDiferentesPodemTerPedidos() {
        // Primeiro usuário faz pedido
        assertDoesNotThrow(() -> {
            orderService.submitOrder(testPackage.getPackageName(), 24);
        });

        User user1Reloaded = userRepository.findByUsername("roberto_teste_it18").orElseThrow();
        assertNotNull(user1Reloaded.getAccessKey(), "Usuário 1 deve ter AccessKey");

        // Criar segundo usuário
        UserInDto user2Dto = UserInDto.builder()
                .firstName("Maria")
                .lastName("Costa")
                .username("maria_teste_it18_6")
                .email("maria.it18.6@test.com")
                .password("Test56789")
                .phone(995333444)
                .build();

        registrationService.registerUser(user2Dto);
        User user2 = userRepository.findByUsername("maria_teste_it18_6").orElseThrow();

        CreditCard card2 = CreditCard.builder()
                .cardNumber(1234567812345678L)
                .month(11)
                .year(2025)
                .CVV(456)
                .accountBalance(200000L)
                .build();
        card2.setUser(user2);
        creditCardRepository.save(card2);
        // Flush e clear para sincronizar relacionamento no banco
        entityManager.flush();
        entityManager.clear();

        // Autenticar segundo usuário
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user2.getUsername(), null)
        );

        // Segundo usuário também pode fazer pedido (não bloqueado)
        assertDoesNotThrow(() -> {
            orderService.submitOrder(testPackage.getPackageName(), 24);
        }, "Usuário diferente pode criar pedido simultâneo");

        User user2Reloaded = userRepository.findByUsername("maria_teste_it18_6").orElseThrow();
        assertNotNull(user2Reloaded.getAccessKey(), "Usuário 2 também deve ter AccessKey");

        // Validar que são AccessKeys diferentes
        User user1Final = userRepository.findByUsername("roberto_teste_it18").orElseThrow();
        assertNotEquals(user1Final.getAccessKey().getId(), user2Reloaded.getAccessKey().getId(),
                "AccessKeys devem ser diferentes");

        System.out.println("\n=== PROTEÇÃO VALIDADA ===");
        System.out.println("✓ Um usuário não pode ter pedidos duplicados");
        System.out.println("✓ Validação via AccessKey (OneToOne)");
        System.out.println("✓ Usuários diferentes podem ter pedidos simultâneos");
        System.out.println("=========================\n");
    }
}
