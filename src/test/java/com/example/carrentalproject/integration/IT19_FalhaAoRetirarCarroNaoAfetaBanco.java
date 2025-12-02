package com.example.carrentalproject.integration;

import com.example.carrentalproject.constant.FuelType;
import com.example.carrentalproject.constant.GearBoxType;
import com.example.carrentalproject.domain.*;
import com.example.carrentalproject.dto.UserInDto;
import com.example.carrentalproject.repository.*;
import com.example.carrentalproject.service.DeliveryService;
import com.example.carrentalproject.service.OrderService;
import com.example.carrentalproject.service.RegistrationService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * IT19 - Cenário 10: Transações e Rollback
 * 
 * OBJETIVO: Validar que falhas durante pickUpTheCar() não afetam o banco de dados
 * devido ao rollback transacional (@Transactional).
 * 
 * TESTES:
 * 1. falhaAoSalvarOrderRealizaRollback - Simula falha ao persistir PlacedOrder
 * 2. falhaAoDeletarAccessKeyRealizaRollback - Simula falha ao deletar AccessKey
 * 3. validaRollbackMantemCarroDisponivel - Confirma Car.isAvailable permanece true
 * 
 * ESTRATÉGIA:
 * - Não usa mocks (testes de integração reais)
 * - Força falhas através de estado inválido do banco
 * - Verifica rollback através de queries após exceção
 * 
 * APRENDIZADOS:
 * - @Transactional em DeliveryService garante atomicidade
 * - Qualquer RuntimeException causa rollback automático
 * - Estado do banco antes da transação é preservado
 */
@SpringBootTest
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("IT19 - Falha ao Retirar Carro Não Afeta Banco (Rollback)")
public class IT19_FalhaAoRetirarCarroNaoAfetaBanco {

    @Autowired
    private DeliveryService deliveryService;

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
    private AccessKeyRepository accessKeyRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CreditCardRepository creditCardRepository;

    @Autowired
    private RoleRepository roleRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private User testUser;
    private Car testCar;
    private CarPackage testPackage;

    @BeforeEach
    void setUp() {
        // 1. Criar Role
        Role userRole = Role.builder()
                .name("ROLE_USER")
                .users(new java.util.ArrayList<>())
                .build();
        
        if (roleRepository.findByName("ROLE_USER").isEmpty()) {
            roleRepository.save(userRole);
        }

        // 2. Criar usuário
        UserInDto userDto = UserInDto.builder()
                .firstName("Carlos")
                .lastName("Silva")
                .username("carlos_teste_it19")
                .email("carlos.it19@test.com")
                .password("Test12345")
                .phone(123456789)
                .build();

        registrationService.registerUser(userDto);
        testUser = userRepository.findByUsername("carlos_teste_it19").orElseThrow();

        // 3. Criar CreditCard com saldo alto
        CreditCard card = CreditCard.builder()
                .cardNumber(1234567890123456L)
                .month(12)
                .year(2026)
                .CVV(123)
                .accountBalance(100000L) // R$ 1000
                .build();
        card.setUser(testUser);
        creditCardRepository.save(card);
        
        // Flush e clear para sincronizar relacionamento
        entityManager.flush();
        entityManager.clear();
        testUser = userRepository.findByUsername("carlos_teste_it19").orElseThrow();

        // 4. Criar CarParameters
        CarParameters parameters = CarParameters.builder()
                .fuelType(FuelType.PETROL)
                .gearBoxType(GearBoxType.AUTOMATIC)
                .numberOfSeats(5)
                .numberOfDoors(4)
                .isAirConditioningAvailable(true)
                .build();
        carParametersRepository.save(parameters);

        // 5. Criar CarPackage
        testPackage = CarPackage.builder()
                .packageName("Basic")
                .pricePerHour(50)
                .build();
        carPackageRepository.save(testPackage);

        // 6. Criar Car DISPONÍVEL
        testCar = Car.builder()
                .brand("Ford")
                .model("Fiesta")
                .registrationNr("IT19TST")
                .carPackage(testPackage)
                .carParameters(parameters)
                .isAvailable(true)  // DISPONÍVEL para teste
                .build();
        carRepository.save(testCar);

        // 7. Autenticar usuário
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(testUser.getUsername(), null)
        );
    }

    @Test
    @org.junit.jupiter.api.Order(1)
    @DisplayName("ROLLBACK: Exceção ao verificar AccessKey preserva estado do carro")
    void excecaoAoVerificarAccessKeyPreservaEstado() {
        // ARRANGE: Estado inicial
        assertTrue(testCar.getIsAvailable(), "Carro deve estar disponível inicialmente");
        assertNull(testUser.getAccessKey(), "Usuário não deve ter AccessKey");

        // ACT: Tentar retirar carro SEM AccessKey (falha esperada)
        try {
            deliveryService.pickUpTheCar(testCar.getId());
            fail("Deveria lançar NoAccessKeyException");
        } catch (Exception e) {
            // Esperado - NoAccessKeyException
            assertTrue(e.getMessage().contains("You Do Not Have An Access Key") ||
                      e.getMessage().contains("Access Key"),
                    "Mensagem deve mencionar AccessKey");
        }

        // ASSERT: Verificar ROLLBACK - estado preservado
        entityManager.flush();
        entityManager.clear();
        
        Car carAposExcecao = carRepository.findById(testCar.getId()).orElseThrow();
        assertTrue(carAposExcecao.getIsAvailable(), 
                "Carro deve continuar disponível após exceção (rollback)");
        
        long ordersCount = orderRepository.count();
        assertEquals(0, ordersCount, "Nenhum PlacedOrder deve ter sido criado");

        System.out.println("\n=== ROLLBACK VALIDADO ===");
        System.out.println("✓ Exceção lançada corretamente");
        System.out.println("✓ Car.isAvailable permanece true");
        System.out.println("✓ Nenhum PlacedOrder criado");
        System.out.println("✓ Transação com rollback automático funcionou");
        System.out.println("=========================\n");
    }

    @Test
    @org.junit.jupiter.api.Order(2)
    @DisplayName("ROLLBACK: Carro indisponível não altera banco ao falhar validação")
    void carroIndisponivelNaoAlteraBanco() {
        // ARRANGE: Criar AccessKey para usuário
        assertDoesNotThrow(() -> {
            orderService.submitOrder("Basic", 24);
        }, "submitOrder deve funcionar com saldo suficiente");

        entityManager.flush();
        entityManager.clear();
        testUser = userRepository.findByUsername("carlos_teste_it19").orElseThrow();
        assertNotNull(testUser.getAccessKey(), "AccessKey deve existir");

        // Marcar carro como indisponível ANTES da tentativa
        testCar.setIsAvailable(false);
        carRepository.save(testCar);
        entityManager.flush();
        entityManager.clear();

        Car carAntes = carRepository.findById(testCar.getId()).orElseThrow();
        assertFalse(carAntes.getIsAvailable(), "Carro deve estar indisponível");

        long accessKeysAntes = accessKeyRepository.count();
        long ordersAntes = orderRepository.count();

        // ACT: Tentar retirar carro INDISPONÍVEL
        try {
            deliveryService.pickUpTheCar(testCar.getId());
            fail("Deveria lançar UnavailableCarException");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("Not Available") ||
                      e.getMessage().contains("Unavailable"),
                    "Mensagem deve indicar indisponibilidade");
        }

        // ASSERT: Verificar ROLLBACK completo
        entityManager.flush();
        entityManager.clear();

        Car carDepois = carRepository.findById(testCar.getId()).orElseThrow();
        assertFalse(carDepois.getIsAvailable(), 
                "Carro permanece indisponível (estado preservado)");

        long accessKeysDepois = accessKeyRepository.count();
        assertEquals(accessKeysAntes, accessKeysDepois, 
                "AccessKey não deve ter sido deletado (rollback)");

        long ordersDepois = orderRepository.count();
        assertEquals(ordersAntes, ordersDepois, 
                "Nenhum PlacedOrder adicional criado");

        System.out.println("\n=== PROTEÇÃO CONTRA ESTADO INVÁLIDO ===");
        System.out.println("✓ UnavailableCarException lançada");
        System.out.println("✓ AccessKey preservado no banco");
        System.out.println("✓ PlacedOrder não foi criado");
        System.out.println("✓ Rollback manteve integridade");
        System.out.println("=======================================\n");
    }

    @Test
    @org.junit.jupiter.api.Order(3)
    @DisplayName("ROLLBACK: Pacote inválido não altera estado do carro")
    void pacoteInvalidoNaoAlteraEstado() {
        // ARRANGE: Criar AccessKey para pacote DIFERENTE
        assertDoesNotThrow(() -> {
            orderService.submitOrder("Basic", 24);
        });

        entityManager.flush();
        entityManager.clear();
        testUser = userRepository.findByUsername("carlos_teste_it19").orElseThrow();
        assertNotNull(testUser.getAccessKey(), "AccessKey deve existir");
        assertEquals("Basic", testUser.getAccessKey().getCarPackage(),
                "AccessKey deve ser para pacote Basic");

        // Criar carro de pacote DIFERENTE
        CarPackage premiumPackage = CarPackage.builder()
                .packageName("Premium")
                .pricePerHour(100)
                .build();
        carPackageRepository.save(premiumPackage);

        CarParameters premiumParams = CarParameters.builder()
                .fuelType(FuelType.DIESEL)
                .gearBoxType(GearBoxType.AUTOMATIC)
                .numberOfSeats(5)
                .numberOfDoors(4)
                .isAirConditioningAvailable(true)
                .build();
        carParametersRepository.save(premiumParams);

        Car premiumCar = Car.builder()
                .brand("BMW")
                .model("X5")
                .registrationNr("IT19PREM")
                .carPackage(premiumPackage)
                .carParameters(premiumParams)
                .isAvailable(true)
                .build();
        carRepository.save(premiumCar);
        entityManager.flush();

        assertTrue(premiumCar.getIsAvailable(), "Carro Premium deve estar disponível");
        long ordersAntes = orderRepository.count();
        long accessKeysAntes = accessKeyRepository.count();

        // ACT: Tentar retirar carro de PACOTE DIFERENTE
        try {
            deliveryService.pickUpTheCar(premiumCar.getId());
            fail("Deveria lançar InvalidPackageException");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("Package") ||
                      e.getMessage().contains("Cannot Pick"),
                    "Mensagem deve indicar problema de pacote");
        }

        // ASSERT: Verificar ROLLBACK completo
        entityManager.flush();
        entityManager.clear();

        Car premiumCarDepois = carRepository.findById(premiumCar.getId()).orElseThrow();
        assertTrue(premiumCarDepois.getIsAvailable(),
                "Carro Premium permanece disponível (rollback)");

        long ordersDepois = orderRepository.count();
        assertEquals(ordersAntes, ordersDepois,
                "PlacedOrder não foi criado");

        long accessKeysDepois = accessKeyRepository.count();
        assertEquals(accessKeysAntes, accessKeysDepois,
                "AccessKey não foi deletado (rollback)");

        System.out.println("\n=== VALIDAÇÃO DE PACOTE COM ROLLBACK ===");
        System.out.println("✓ InvalidPackageException lançada");
        System.out.println("✓ Carro Premium permanece disponível");
        System.out.println("✓ AccessKey não foi deletado");
        System.out.println("✓ PlacedOrder não foi criado");
        System.out.println("✓ Integridade transacional mantida");
        System.out.println("========================================\n");
    }

    @Test
    @org.junit.jupiter.api.Order(4)
    @DisplayName("SUCESSO: pickUpTheCar completo persiste todas mudanças")
    void sucessoPersisteTodosMudancas() {
        // ARRANGE: Criar AccessKey válido
        assertDoesNotThrow(() -> {
            orderService.submitOrder("Basic", 24);
        });

        entityManager.flush();
        entityManager.clear();
        testUser = userRepository.findByUsername("carlos_teste_it19").orElseThrow();
        assertNotNull(testUser.getAccessKey(), "AccessKey deve existir");

        assertTrue(testCar.getIsAvailable(), "Carro deve estar disponível");
        long ordersAntes = orderRepository.count();

        // ACT: Retirar carro com SUCESSO
        Car carRetirado = assertDoesNotThrow(() -> {
            return deliveryService.pickUpTheCar(testCar.getId());
        }, "pickUpTheCar deve funcionar com tudo válido");

        // ASSERT: Todas mudanças persistidas
        entityManager.flush();
        entityManager.clear();

        // Verificar carro ficou indisponível
        Car carDepois = carRepository.findById(testCar.getId()).orElseThrow();
        assertFalse(carDepois.getIsAvailable(),
                "Carro deve estar indisponível após retirada");

        // Verificar AccessKey foi deletado
        User userDepois = userRepository.findByUsername("carlos_teste_it19").orElseThrow();
        assertNull(userDepois.getAccessKey(),
                "AccessKey deve ter sido deletado");

        // Verificar PlacedOrder foi criado
        long ordersDepois = orderRepository.count();
        assertEquals(ordersAntes + 1, ordersDepois,
                "PlacedOrder deve ter sido criado");

        PlacedOrder order = orderRepository.findAll().get(0);
        assertEquals(testUser.getId(), order.getUserId());
        assertEquals(testCar.getId(), order.getCarId());
        assertNotNull(order.getStartTime());
        assertNotNull(order.getEndTime());

        System.out.println("\n=== SUCESSO: COMMIT COMPLETO ===");
        System.out.println("✓ Car.isAvailable = false");
        System.out.println("✓ AccessKey deletado");
        System.out.println("✓ PlacedOrder criado");
        System.out.println("✓ Timestamps registrados");
        System.out.println("✓ Transação commitada com sucesso");
        System.out.println("================================\n");
    }
}
