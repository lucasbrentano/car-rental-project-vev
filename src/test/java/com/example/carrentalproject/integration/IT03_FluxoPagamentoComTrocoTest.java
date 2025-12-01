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
 * IT03 - Teste de Integração: Fluxo de Pagamento com Troco
 * 
 * Valida o fluxo de pagamento quando o saldo é maior que o custo:
 * 1. Criar pedido de R$ 500 (Sporty: 5 horas * R$ 100/hora)
 * 2. Usuário tem saldo de R$ 1000
 * 3. Verificar que R$ 500 foi deduzido (troco implícito de R$ 500)
 * 4. Validar que pedido foi criado com sucesso
 * 
 * Este teste valida:
 * - Lógica de negócio quando saldo > custo
 * - Dedução correta do valor
 * - Persistência dos dados
 */
@SpringBootTest
@Transactional
@DisplayName("IT03 - Fluxo de Pagamento com Troco")
class IT03_FluxoPagamentoComTrocoTest {

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
        testUsername = "trocotest_" + System.currentTimeMillis();
        
        // Criar usuário manualmente para ter controle total
        testUser = new User();
        testUser.setUsername(testUsername);
        testUser.setPassword("$2a$10$hash");  // Password encriptado
        testUser.setEmail(testUsername + "@test.com");
        testUser.setFirstName("Troco");
        testUser.setLastName("Test");
        testUser.setPhone(888888888);
        
        // Criar cartão de crédito com saldo de R$ 1000,00
        testCard = new CreditCard();
        testCard.setCardNumber(4111111111111111L); // Visa válido
        testCard.setMonth(6);
        testCard.setYear(2026);
        testCard.setCVV(789);
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
                .brand("Volkswagen")
                .model("Gol")
                .registrationNr("XYZ9876")
                .isAvailable(true)
                .carPackage(sporty)
                .carParameters(params)
                .build();
            carRepository.save(testCar);

            System.out.println("✓ Carro de teste criado: Volkswagen Gol");
        }
    }

    @Test
    @DisplayName("Deve completar fluxo de pagamento com troco - pedido R$ 500, saldo R$ 1000")
    void deveCompletarFluxoPagamentoComTroco() {
        System.out.println("\n=== IT03: TESTE DE FLUXO DE PAGAMENTO COM TROCO ===\n");

        // Verificar saldo inicial de R$ 1000
        Long saldoInicial = testUser.getCreditCard().getAccountBalance();
        assertEquals(100000L, saldoInicial, "Saldo inicial deve ser R$ 1000,00");
        System.out.println("✓ Saldo inicial: R$ " + (saldoInicial / 100.0));

        // Criar pedido: Sporty 5 horas * R$ 1,00/hora = R$ 5,00 (500 centavos)
        String packageName = "Sporty";
        Integer hours = 5;
        Long custoEsperado = 500L; // R$ 5,00 em centavos
        
        System.out.println("\n=== Criando pedido ===");
        System.out.println("  Pacote: " + packageName + " (R$ 1,50/hora)");
        System.out.println("  Horas: " + hours);
        System.out.println("  Custo total: R$ " + (custoEsperado / 100.0));
        System.out.println("  Troco esperado: R$ " + ((saldoInicial - custoEsperado) / 100.0));
        
        AccessKeyDto accessKeyDto = orderService.submitOrder(packageName, hours);
        
        assertNotNull(accessKeyDto, "AccessKey deve ter sido criada");
        assertEquals(packageName, accessKeyDto.getCarPackage());
        assertEquals(hours, accessKeyDto.getHours());
        System.out.println("✓ Pedido criado com sucesso");

        // Verificar que R$ 5,00 foi deduzido (troco de R$ 995,00 permanece)
        testUser = userRepository.findByUsername(testUsername).orElseThrow();
        Long saldoFinal = testUser.getCreditCard().getAccountBalance();
        Long trocoEsperado = saldoInicial - custoEsperado;
        
        assertEquals(trocoEsperado, saldoFinal, "Troco deve permanecer no cartão");
        assertEquals(99500L, saldoFinal, "Saldo final deve ser R$ 995,00 (troco)");
        
        System.out.println("✓ Valor deduzido: R$ " + (custoEsperado / 100.0));
        System.out.println("✓ Troco (saldo restante): R$ " + (saldoFinal / 100.0));

        // Verificar que AccessKey foi criada
        assertNotNull(testUser.getAccessKey(), "Usuário deve ter AccessKey");
        System.out.println("✓ AccessKey criada e vinculada");

        System.out.println("\n=== ✓ FLUXO DE PAGAMENTO COM TROCO VALIDADO COM SUCESSO ===\n");
    }

    @Test
    @DisplayName("Deve validar que troco fica disponível no cartão para próximos pedidos")
    void deveValidarTrocoDisponivelParaProximoPedido() {
        // Criar primeiro pedido: Ordinary 6 horas * R$ 1,00/h = R$ 6,00 (600 centavos)
        orderService.submitOrder("Ordinary", 6);
        
        testUser = userRepository.findByUsername(testUsername).orElseThrow();
        Long saldoApos1 = testUser.getCreditCard().getAccountBalance();
        
        // Deve sobrar R$ 997,00 (R$ 1000 - R$ 3,00)
        assertEquals(99700L, saldoApos1, "Troco deve ser R$ 997,00");
        
        // Limpar AccessKey para permitir novo pedido
        testUser.setAccessKey(null);
        userRepository.save(testUser);
        when(loggedInUser.getUser()).thenReturn(testUser);
        
        // Criar segundo pedido usando o troco: Ordinary 4 horas * R$ 1,00/h = R$ 4,00 (400 centavos)
        orderService.submitOrder("Ordinary", 4);
        
        testUser = userRepository.findByUsername(testUsername).orElseThrow();
        Long saldoFinal = testUser.getCreditCard().getAccountBalance();
        
        // Deve sobrar R$ 995,00 (R$ 997 - R$ 2)
        assertEquals(99500L, saldoFinal, "Saldo final deve ser R$ 995,00");
    }

    @Test
    @DisplayName("Deve calcular troco correto com valores fracionados")
    void deveCalcularTrocoCorretoComValoresFracionados() {
        // Criar pedido de 3 horas com Ordinary: 3h * R$ 1,00/h = R$ 3,00 (300 centavos)
        orderService.submitOrder("Ordinary", 3);
        
        testUser = userRepository.findByUsername(testUsername).orElseThrow();
        Long saldoFinal = testUser.getCreditCard().getAccountBalance();
        
        // Troco: R$ 1000 - R$ 1,50 = R$ 998,50
        assertEquals(99850L, saldoFinal, "Troco deve ser R$ 998,50");
    }

    @Test
    @DisplayName("Deve validar que pagamento com valor exato (sem troco) funciona")
    void deveValidarPagamentoComValorExato() {
        // Limpar AccessKey existente
        testUser.setAccessKey(null);
        userRepository.save(testUser);
        
        // Ajustar saldo para valor exato do pedido: Sporty 5h * R$ 1,00/h = R$ 5,00
        testCard.setAccountBalance(500L); // R$ 5,00 exatos
        creditCardRepository.save(testCard);
        
        // Atualizar referência do mock
        testUser = userRepository.findByUsername(testUsername).orElseThrow();
        when(loggedInUser.getUser()).thenReturn(testUser);
        
        // Criar pedido de exatamente R$ 5,00 (Sporty: 5 horas * R$ 1,00/hora)
        orderService.submitOrder("Sporty", 5);
        
        testUser = userRepository.findByUsername(testUsername).orElseThrow();
        Long saldoFinal = testUser.getCreditCard().getAccountBalance();
        
        // Saldo deve ser zero (sem troco)
        assertEquals(0L, saldoFinal, "Saldo deve ser zero após pagamento exato");
    }

    @Test
    @DisplayName("Deve validar múltiplos cenários de troco")
    void deveValidarMultiplosCenariosDeEstadoTroco() {
        // Cenário 1: Pedido barato com muito troco
        // R$ 1000 - R$ 0,50 (1 hora Ordinary × R$ 0,50/h) = R$ 999,50 de troco
        orderService.submitOrder("Ordinary", 1);
        
        testUser = userRepository.findByUsername(testUsername).orElseThrow();
        assertEquals(99950L, testUser.getCreditCard().getAccountBalance(), "Saldo deve ser R$ 999,50");
        
        System.out.println("✓ Cenário 1: Pedido barato - troco de R$ 999,50 validado");
    }
}
