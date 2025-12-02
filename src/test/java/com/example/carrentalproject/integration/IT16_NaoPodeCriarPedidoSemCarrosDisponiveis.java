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
 * IT16 - Cenário 9: Teste que DETECTA LACUNA DE VALIDAÇÃO
 * 
 * LACUNA IDENTIFICADA: OrderService.submitOrder() NÃO valida disponibilidade de carros
 * 
 * COMPORTAMENTO ATUAL INCORRETO: Sistema aceita pedido mesmo quando:
 * - Não existem carros no banco de dados
 * - Todos os carros do pacote estão indisponíveis (isAvailable = false)
 * 
 * COMPORTAMENTO ESPERADO: Sistema deveria lançar UnavailableCarException
 * VALIDAÇÃO FALTANTE: OrderService linhas 43-80 não verificam Car.isAvailable
 * 
 * EVIDÊNCIA: Testes usam assertDoesNotThrow() para PROVAR que sistema aceita pedidos inválidos
 */
@SpringBootTest
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("IT16 - Cenário 9: Lacuna - Sistema NÃO valida disponibilidade de carros")
public class IT16_NaoPodeCriarPedidoSemCarrosDisponiveis {

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
    private RoleRepository roleRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private User testUser;
    private CarPackage testPackage;
    private CarParameters testParameters;

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
                .firstName("Carlos")
                .lastName("Silva")
                .username("carlos_teste_it16")
                .email("carlos.it16@test.com")
                .password("Test12345")
                .phone(999888777)
                .build();

        registrationService.registerUser(userDto);
        testUser = userRepository.findByUsername("carlos_teste_it16").orElseThrow();

        CreditCard card = CreditCard.builder()
                .cardNumber(9876543210123456L)
                .month(12)
                .year(2026)
                .CVV(321)
                .accountBalance(100000L) // R$ 1000.00
                .build();
        card.setUser(testUser);
        creditCardRepository.save(card);
        // Flush e clear para sincronizar relacionamento no banco
        entityManager.flush();
        entityManager.clear();
        // Recarregar usuário para trazer o CreditCard
        testUser = userRepository.findByUsername("carlos_teste_it16").orElseThrow();

        // Criar parâmetros e pacote SEM CARROS
        testParameters = CarParameters.builder()
                .fuelType(FuelType.PETROL)
                .gearBoxType(GearBoxType.MANUAL)
                .numberOfDoors(4)
                .numberOfSeats(5)
                .isAirConditioningAvailable(true)
                .build();
        carParametersRepository.save(testParameters);

        testPackage = CarPackage.builder()
                .packageName("Test Package IT16")
                .pricePerHour(50)
                .build();
        carPackageRepository.save(testPackage);

        // Autenticar usuário
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(testUser.getUsername(), null)
        );
    }

    @Test
    @org.junit.jupiter.api.Order(1)
    @DisplayName("DEVE FALHAR: Sistema deve rejeitar pedido sem nenhum carro disponível")
    void deveRejeitarPedidoSemCarrosDisponiveis() {
        // Verificar que NÃO existem carros
        long totalCarros = carRepository.findAll().stream()
                .filter(car -> car.getCarPackage().getId().equals(testPackage.getId()))
                .count();
        assertEquals(0, totalCarros, "Não deve existir nenhum carro deste pacote");

        // BUG: Sistema deveria lançar exceção mas não lança
        assertThrows(RuntimeException.class, () -> {
            orderService.submitOrder(testPackage.getPackageName(), 24);
        }, "Sistema DEVERIA lançar UnavailableCarException quando não há carros disponíveis");
    }

    @Test
    @org.junit.jupiter.api.Order(2)
    @DisplayName("DEVE FALHAR: Sistema deve rejeitar pedido quando todos carros estão indisponíveis")
    void deveRejeitarPedidoComTodosCarrosIndisponiveis() {
        // Criar 3 carros INDISPONÍVEIS
        for (int i = 1; i <= 3; i++) {
            Car carroIndisponivel = Car.builder()
                    .brand("Brand " + i)
                    .model("Model " + i)
                    .registrationNr("ABC" + i + "234")
                    .carPackage(testPackage)
                    .carParameters(testParameters)
                    .isAvailable(false)  // INDISPONÍVEL
                    .build();
            carRepository.save(carroIndisponivel);
        }

        // Verificar que todos carros estão indisponíveis
        long carrosDisponiveis = carRepository.findAll().stream()
                .filter(car -> car.getCarPackage().getId().equals(testPackage.getId()))
                .filter(Car::getIsAvailable)
                .count();
        assertEquals(0, carrosDisponiveis, "Todos os carros devem estar indisponíveis");

        // BUG: Sistema deveria lançar exceção mas não lança
        assertThrows(RuntimeException.class, () -> {
            orderService.submitOrder(testPackage.getPackageName(), 24);
        }, "Sistema DEVERIA lançar UnavailableCarException quando todos carros estão indisponíveis");
    }

    @Test
    @org.junit.jupiter.api.Order(3)
    @DisplayName("CONTRASTE: submitOrder funciona corretamente COM carros disponíveis")
    void submitOrderFuncionaComCarrosDisponiveis() {
        // Criar carro DISPONÍVEL
        Car carroDisponivel = Car.builder()
                .brand("Volkswagen")
                .model("Gol")
                .registrationNr("ABC1234")
                .carPackage(testPackage)
                .carParameters(testParameters)
                .isAvailable(true)  // DISPONÍVEL
                .build();
        carRepository.save(carroDisponivel);

        // Verificar que existe carro disponível
        long carrosDisponiveis = carRepository.findAll().stream()
                .filter(car -> car.getCarPackage().getId().equals(testPackage.getId()))
                .filter(Car::getIsAvailable)
                .count();
        assertEquals(1, carrosDisponiveis, "Deve existir 1 carro disponível");

        // Sistema funciona corretamente
        assertDoesNotThrow(() -> {
            orderService.submitOrder(testPackage.getPackageName(), 24);
        });

        User userReloaded = userRepository.findByUsername("carlos_teste_it16").orElseThrow();
        assertNotNull(userReloaded.getAccessKey(), "AccessKey criado com sucesso");

        System.out.println("\n=== CONTRASTE ===");
        System.out.println("✓ Com carro disponível, sistema funciona");
        System.out.println("✓ AccessKey criado: " + userReloaded.getAccessKey().getId());
        System.out.println("=================\n");
    }

    @Test
    @org.junit.jupiter.api.Order(4)
    @DisplayName("DEVE FALHAR: Ambos cenários (vazio e indisponível) devem rejeitar pedido")
    void analiseDiferencaEntreVazioEIndisponivel() {
        // CENÁRIO 1: Zero carros - deve falhar
        assertThrows(RuntimeException.class, () -> {
            orderService.submitOrder(testPackage.getPackageName(), 24);
        }, "CENÁRIO 1: Sem carros deve lançar exceção");

        // CENÁRIO 2: Carros existem mas isAvailable=false - deve falhar
        Car carroIndisponivel = Car.builder()
                .brand("Fiat")
                .model("Uno")
                .registrationNr("XYZ1234")
                .carPackage(testPackage)
                .carParameters(testParameters)
                .isAvailable(false)
                .build();
        carRepository.save(carroIndisponivel);

        User userReset = userRepository.findByUsername("carlos_teste_it16").orElseThrow();
        userReset.setAccessKey(null);
        userRepository.save(userReset);

        assertThrows(RuntimeException.class, () -> {
            orderService.submitOrder(testPackage.getPackageName(), 24);
        }, "CENÁRIO 2: Carros indisponíveis deve lançar exceção");
    }

    @Test
    @org.junit.jupiter.api.Order(5)
    @DisplayName("DOCUMENTAÇÃO: Comportamento esperado vs atual")
    void comportamentoEsperadoComCarrosIndisponiveis() {
        System.out.println("\n=== COMPORTAMENTO ESPERADO ===");
        System.out.println("\nCódigo esperado no OrderService.submitOrder():");
        System.out.println("  List<Car> availableCars = carRepository.findAll().stream()");
        System.out.println("      .filter(car -> car.getCarPackage().getPackageName().equals(carPackage))");
        System.out.println("      .filter(Car::getIsAvailable)");
        System.out.println("      .collect(Collectors.toList());");
        System.out.println("  ");
        System.out.println("  if (availableCars.isEmpty()) {");
        System.out.println("      throw new UnavailableCarException(\"No available cars for package: \" + carPackage);");
        System.out.println("  }");
        System.out.println("\nCOMPORTAMENTO ATUAL:");
        System.out.println("  ✗ Nenhuma validação de disponibilidade");
        System.out.println("  ✗ AccessKey criado sem verificar Car.isAvailable");
        System.out.println("  ✗ Pedido aceito mesmo sem carros disponíveis");
        System.out.println("\nIMPACTO:");
        System.out.println("  - Usuário reserva carro que não existe/está indisponível");
        System.out.println("  - Negócio perde credibilidade");
        System.out.println("  - Falha na retirada (CarService.pickUpCar)");
        System.out.println("===============================\n");

        // Este teste não executa código, apenas documenta
        assertTrue(true, "Documentação do comportamento esperado");
    }
}
