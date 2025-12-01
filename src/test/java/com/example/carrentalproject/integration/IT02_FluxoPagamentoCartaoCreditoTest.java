package com.example.carrentalproject.integration;

import com.example.carrentalproject.constant.FuelType;
import com.example.carrentalproject.constant.GearBoxType;
import com.example.carrentalproject.domain.AccessKey;
import com.example.carrentalproject.domain.Car;
import com.example.carrentalproject.domain.CarPackage;
import com.example.carrentalproject.domain.CarParameters;
import com.example.carrentalproject.domain.CreditCard;
import com.example.carrentalproject.domain.Role;
import com.example.carrentalproject.domain.User;
import com.example.carrentalproject.dto.AccessKeyDto;
import com.example.carrentalproject.dto.CreditCardDto;
import com.example.carrentalproject.repository.AccessKeyRepository;
import com.example.carrentalproject.repository.CarPackageRepository;
import com.example.carrentalproject.repository.CarParametersRepository;
import com.example.carrentalproject.repository.CarRepository;
import com.example.carrentalproject.repository.CreditCardRepository;
import com.example.carrentalproject.repository.RoleRepository;
import com.example.carrentalproject.repository.UserRepository;
import com.example.carrentalproject.service.OrderService;
import com.example.carrentalproject.service.UserService;
import com.example.carrentalproject.security.LoggedInUser;
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
 * IT02 - Teste de Integração: Fluxo de Pagamento com Cartão de Crédito
 * 
 * Valida o fluxo de criação de pedido e integração com cartão de crédito:
 * 1. Criar usuário com cartão cadastrado
 * 2. Criar pedido via OrderService.submitOrder
 * 3. Verificar saldo do cartão após pagamento
 * 4. Validar AccessKey criada
 * 
 * Este teste valida a integração entre:
 * OrderService, UserRepository, CreditCardRepository, AccessKeyRepository
 */
@SpringBootTest
@Transactional
@DisplayName("IT02 - Fluxo de Pagamento com Cartão de Crédito")
class IT02_FluxoPagamentoCartaoCreditoTest {

    @Autowired
    private UserService userService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private CarPackageRepository carPackageRepository;

    @Autowired
    private CarParametersRepository carParametersRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private AccessKeyRepository accessKeyRepository;

    @Autowired
    private CreditCardRepository creditCardRepository;

    @MockBean
    private LoggedInUser loggedInUser;

    private String testUsername;
    private User testUser;
    private CreditCard testCard;

    @BeforeEach
    void setUp() {
        // Criar dados de teste necessários
        criarDadosDeTesteSePreciso();
        
        // Criar username único baseado em timestamp
        testUsername = "paymenttest_" + System.currentTimeMillis();
        
        // Criar usuário manualmente para ter controle total
        testUser = new User();
        testUser.setUsername(testUsername);
        testUser.setPassword("$2a$10$hash");  // Password encriptado
        testUser.setEmail(testUsername + "@test.com");
        testUser.setFirstName("Payment");
        testUser.setLastName("Test");
        testUser.setPhone(999999999);
        
        // Criar cartão de crédito com saldo
        testCard = new CreditCard();
        testCard.setCardNumber(5105105105105100L); // Mastercard válido
        testCard.setMonth(12);
        testCard.setYear(2025);
        testCard.setCVV(456);
        testCard.setAccountBalance(100000L); // R$ 1000,00 em centavos
        
        // Salvar usuário primeiro
        userRepository.save(testUser);
        
        // Depois salvar cartão e associar ao usuário
        testCard.setUser(testUser);
        creditCardRepository.save(testCard);
        testUser.setCreditCard(testCard);
        userRepository.save(testUser);
        
        // Configurar mock de LoggedInUser
        when(loggedInUser.getUser()).thenReturn(testUser);
    }

    private void criarDadosDeTesteSePreciso() {
        if (roleRepository.count() == 0) {
            Role userRole = new Role();
            userRole.setId(null);
            userRole.setName("ROLE_USER");
            userRole.setUsers(new java.util.ArrayList<>());
            roleRepository.save(userRole);
            System.out.println("✓ Role ROLE_USER criada");
        }

        if (carPackageRepository.count() == 0) {
            CarPackage ordinary = CarPackage.builder()
                .packageName("Ordinary")
                .pricePerHour(50)
                .build();
            carPackageRepository.save(ordinary);

            CarPackage sporty = CarPackage.builder()
                .packageName("Sporty")
                .pricePerHour(100)
                .build();
            carPackageRepository.save(sporty);

            CarPackage luxury = CarPackage.builder()
                .packageName("Luxury")
                .pricePerHour(200)
                .build();
            carPackageRepository.save(luxury);

            System.out.println("✓ Pacotes criados: Ordinary, Sporty, Luxury");

            // Criar um carro de teste
            CarParameters params = CarParameters.builder()
                .fuelType(FuelType.PETROL)
                .gearBoxType(GearBoxType.MANUAL)
                .numberOfSeats(5)
                .numberOfDoors(4)
                .isAirConditioningAvailable(true)
                .build();
            carParametersRepository.save(params);

            Car testCar = Car.builder()
                .brand("Ford")
                .model("Fiesta")
                .registrationNr("ABC1234")
                .isAvailable(true)
                .carPackage(ordinary)
                .carParameters(params)
                .build();
            carRepository.save(testCar);

            System.out.println("✓ Carro de teste criado: Ford Fiesta");
        }
    }

    @Test
    @DisplayName("Deve completar fluxo de pagamento: criar pedido e deduzir saldo do cartão")
    void deveCompletarFluxoPagamentoCartaoCredito() {
        System.out.println("\n=== IT02: TESTE DE FLUXO DE PAGAMENTO COM CARTÃO DE CRÉDITO ===\n");

        // Verificar saldo inicial
        Long saldoInicial = testUser.getCreditCard().getAccountBalance();
        assertEquals(100000L, saldoInicial, "Saldo inicial deve ser R$ 1000,00");
        System.out.println("✓ Saldo inicial: R$ " + (saldoInicial / 100.0));

        // Criar pedido de 10 horas com pacote Ordinary (R$ 1,00/hora = R$ 10,00)
        String packageName = "Ordinary";
        Integer hours = 10;
        
        System.out.println("\n=== Criando pedido ===");
        System.out.println("  Pacote: " + packageName);
        System.out.println("  Horas: " + hours);
        System.out.println("  Custo: R$ " + (1 * hours));
        
        AccessKeyDto accessKeyDto = orderService.submitOrder(packageName, hours);
        
        assertNotNull(accessKeyDto, "AccessKey deve ter sido criada");
        assertEquals(packageName, accessKeyDto.getCarPackage());
        assertEquals(hours, accessKeyDto.getHours());
        System.out.println("✓ Pedido criado com sucesso");

        // Verificar que o saldo foi deduzido
        testUser = userRepository.findByUsername(testUsername).orElseThrow();
        Long saldoFinal = testUser.getCreditCard().getAccountBalance();
        Long custoEsperado = 500L; // 10h * 50 centavos/h = 500 centavos = R$ 5,00
        Long saldoEsperado = saldoInicial - custoEsperado;
        
        assertEquals(saldoEsperado, saldoFinal, "Saldo deve ser deduzido corretamente");
        assertEquals(99500L, saldoFinal, "Saldo final deve ser R$ 995,00");
        System.out.println("✓ Saldo após pagamento: R$ " + (saldoFinal / 100.0));

        // Verificar que AccessKey foi criada e vinculada ao usuário
        assertNotNull(testUser.getAccessKey(), "Usuário deve ter AccessKey");
        assertEquals(packageName, testUser.getAccessKey().getCarPackage());
        assertEquals(hours, testUser.getAccessKey().getHours());
        System.out.println("✓ AccessKey vinculada ao usuário");

        System.out.println("\n=== ✓ FLUXO DE PAGAMENTO VALIDADO COM SUCESSO ===\n");
    }

    @Test
    @DisplayName("Deve validar que segundo pedido não é permitido (ExistingOrderException)")
    void deveImpedirSegundoPedido() {
        // Criar primeiro pedido
        orderService.submitOrder("Ordinary", 5);
        
        // Tentar criar segundo pedido
        assertThrows(Exception.class, () -> {
            orderService.submitOrder("Sporty", 3);
        }, "Deve lançar exceção ao tentar criar segundo pedido");
    }

    @Test
    @DisplayName("Deve validar cálculo correto do custo em diferentes cenários")
    void deveCalcularCustoCorretamente() {
        Long saldoInicial = testUser.getCreditCard().getAccountBalance();
        
        // Cenário 1: Ordinary (R$ 0,50/hora) x 2 horas = R$ 1,00 (100 centavos)
        orderService.submitOrder("Ordinary", 2);
        
        testUser = userRepository.findByUsername(testUsername).orElseThrow();
        Long saldoApos1 = testUser.getCreditCard().getAccountBalance();
        
        assertEquals(saldoInicial - 100L, saldoApos1, "Deve deduzir R$ 1,00");
    }

    @Test
    @DisplayName("Deve validar persistência de AccessKey no banco de dados")
    void deveValidarPersistenciaAccessKey() {
        // Criar pedido
        AccessKeyDto dto = orderService.submitOrder("Luxury", 1);
        
        // Verificar que AccessKey existe no banco
        AccessKey accessKey = accessKeyRepository.findById(dto.getId()).orElseThrow();
        
        assertNotNull(accessKey);
        assertEquals("Luxury", accessKey.getCarPackage());
        assertEquals(1, accessKey.getHours());
        assertNotNull(accessKey.getUser());
        assertEquals(testUsername, accessKey.getUser().getUsername());
    }

    @Test
    @DisplayName("Deve validar integração OrderService → CreditCardRepository")
    void deveValidarIntegracaoOrderServiceCreditCard() {
        Long cardIdAntes = testUser.getCreditCard().getId();
        
        // Criar pedido
        orderService.submitOrder("Sporty", 5);
        
        // Verificar que o mesmo cartão foi atualizado (não criou novo)
        CreditCard cardDepois = creditCardRepository.findById(cardIdAntes).orElseThrow();
        
        assertNotNull(cardDepois);
        assertEquals(cardIdAntes, cardDepois.getId());
        assertTrue(cardDepois.getAccountBalance() < 100000L, "Saldo deve ter sido deduzido");
    }
}
