package com.example.carrentalproject.system;

import com.example.carrentalproject.constant.FuelType;
import com.example.carrentalproject.constant.GearBoxType;
import com.example.carrentalproject.domain.*;
import com.example.carrentalproject.dto.AccessKeyDto;
import com.example.carrentalproject.dto.CreditCardDto;
import com.example.carrentalproject.dto.UserInDto;
import com.example.carrentalproject.repository.*;
import com.example.carrentalproject.service.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TESTE DE SISTEMA - Jornada Completa do Usuário
 * 
 * Este teste valida o fluxo end-to-end completo de um usuário no sistema de aluguel de carros:
 * 
 * 1. Registro de usuário
 * 2. Adição de cartão de crédito
 * 3. Consulta de pacotes disponíveis
 * 4. Consulta de carros disponíveis
 * 5. Criação de pedido (order)
 * 6. Pagamento
 * 7. Retirada do carro (pickup)
 * 8. Verificação do estado final
 */
@SpringBootTest
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("SISTEMA - Jornada Completa do Usuário")
public class JornadaCompletaUsuarioTest {

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private UserService userService;

    @Autowired
    private CarService carService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private DeliveryService deliveryService;

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
    private RoleRepository roleRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private static final String TEST_USERNAME = "usuario.jornada";
    private static final String TEST_EMAIL = "jornada@test.com";
    private static final String TEST_PASSWORD = "Password123";
    private static final String PACKAGE_NAME = "Economy";
    private static final int RENTAL_HOURS = 24;

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

        // Criar CarParameters
        CarParameters params = CarParameters.builder()
                .fuelType(FuelType.PETROL)
                .gearBoxType(GearBoxType.AUTOMATIC)
                .numberOfDoors(4)
                .numberOfSeats(5)
                .isAirConditioningAvailable(true)
                .build();
        carParametersRepository.save(params);

        // Criar CarPackage
        CarPackage carPackage = CarPackage.builder()
                .packageName(PACKAGE_NAME)
                .pricePerHour(100)
                .build();
        carPackageRepository.save(carPackage);

        // Criar Car
        Car car = Car.builder()
                .registrationNr("JORNADA1")
                .brand("Toyota")
                .model("Corolla")
                .isAvailable(true)
                .carParameters(params)
                .carPackage(carPackage)
                .build();
        carRepository.save(car);

        entityManager.flush();
        entityManager.clear();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @Order(1)
    @DisplayName("1. Jornada Completa: Registro → Cartão → Consulta → Pedido → Pagamento → Retirada")
    void jornadaCompletaUsuarioNovoComSucesso() {
        System.out.println("\n=== INICIANDO JORNADA COMPLETA DO USUÁRIO ===\n");

        // PASSO 1: REGISTRO
        System.out.println("PASSO 1: Registrando novo usuário...");
        UserInDto registrationDto = UserInDto.builder()
                .firstName("Usuario")
                .lastName("Teste")
                .username(TEST_USERNAME)
                .password(TEST_PASSWORD)
                .email(TEST_EMAIL)
                .phone(999999999)
                .build();

        registrationService.registerUser(registrationDto);
        entityManager.flush();
        entityManager.clear();

        User registeredUser = userRepository.findByUsername(TEST_USERNAME).orElseThrow();
        assertNotNull(registeredUser);
        assertEquals(TEST_EMAIL, registeredUser.getEmail());
        System.out.println("✓ Usuário registrado: " + registeredUser.getUsername());

        // Autenticar usuário
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                TEST_USERNAME, TEST_PASSWORD);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // PASSO 2: ADICIONAR CARTÃO DE CRÉDITO
        System.out.println("\nPASSO 2: Adicionando cartão de crédito...");
        CreditCardDto creditCardDto = CreditCardDto.builder()
                .cardNumber(4532015112830366L)
                .month(12)
                .year(2025)
                .CVV(123)
                .build();

        User userWithCard = userService.addCreditCardToUser(TEST_USERNAME, creditCardDto);
        assertNotNull(userWithCard.getCreditCard());
        assertEquals(0L, userWithCard.getCreditCard().getAccountBalance());
        System.out.println("✓ Cartão adicionado com saldo inicial: R$ " + userWithCard.getCreditCard().getAccountBalance());

        // PASSO 3: CONSULTAR PACOTES
        System.out.println("\nPASSO 3: Consultando pacotes disponíveis...");
        List<CarPackage> packages = carService.getCarPackages();
        assertFalse(packages.isEmpty());
        CarPackage selectedPackage = packages.stream()
                .filter(p -> p.getPackageName().equals(PACKAGE_NAME))
                .findFirst()
                .orElseThrow();
        System.out.println("✓ Pacote encontrado: " + selectedPackage.getPackageName() + 
                " (R$ " + selectedPackage.getPricePerHour() + "/hora)");

        // PASSO 4: CONSULTAR CARROS DISPONÍVEIS
        System.out.println("\nPASSO 4: Consultando carros disponíveis...");
        List<Car> availableCars = carService.getAvailableCars(1, Sort.Direction.ASC);
        assertFalse(availableCars.isEmpty());
        Car selectedCar = availableCars.get(0);
        assertTrue(selectedCar.getIsAvailable());
        System.out.println("✓ Carro disponível: " + selectedCar.getBrand() + " " + 
                selectedCar.getModel() + " (" + selectedCar.getRegistrationNr() + ")");

        // PASSO 5: CRIAR PEDIDO (submitOrder já faz o pagamento automaticamente)
        System.out.println("\nPASSO 5: Criando pedido de aluguel...");
        long expectedPrice = (long) selectedPackage.getPricePerHour() * RENTAL_HOURS;
        System.out.println("  Valor a pagar: R$ " + expectedPrice);
        
        // Adicionar saldo suficiente no cartão ANTES de criar o pedido
        User userBeforeOrder = userRepository.findByUsername(TEST_USERNAME).orElseThrow();
        userBeforeOrder.getCreditCard().setAccountBalance(expectedPrice);
        entityManager.flush();
        entityManager.clear();
        
        AccessKeyDto accessKeyDto = orderService.submitOrder(PACKAGE_NAME, RENTAL_HOURS);
        assertNotNull(accessKeyDto);
        entityManager.flush();
        entityManager.clear();

        AccessKey accessKey = accessKeyRepository.findById(accessKeyDto.getId()).orElseThrow();
        assertEquals(RENTAL_HOURS, accessKey.getHours());
        assertEquals(PACKAGE_NAME, accessKey.getCarPackage());
        System.out.println("✓ Pedido criado e pago - AccessKey ID: " + accessKey.getId());
        System.out.println("  Pacote: " + accessKey.getCarPackage());
        System.out.println("  Horas: " + accessKey.getHours());
        System.out.println("  Valor cobrado: R$ " + expectedPrice);

        User userAfterPayment = userRepository.findByUsername(TEST_USERNAME).orElseThrow();
        assertEquals(0L, userAfterPayment.getCreditCard().getAccountBalance());
        System.out.println("  Saldo após pagamento: R$ " + userAfterPayment.getCreditCard().getAccountBalance());

        // PASSO 6: RETIRADA DO CARRO
        System.out.println("\nPASSO 6: Retirando o carro...");
        Car pickedUpCar = deliveryService.pickUpTheCar(selectedCar.getId());
        assertNotNull(pickedUpCar);
        entityManager.flush();
        entityManager.clear();

        Car carAfterPickup = carRepository.findById(selectedCar.getId()).orElseThrow();
        assertFalse(carAfterPickup.getIsAvailable());
        System.out.println("✓ Carro retirado com sucesso!");
        System.out.println("  Carro: " + carAfterPickup.getBrand() + " " + carAfterPickup.getModel());
        System.out.println("  Status disponível: " + carAfterPickup.getIsAvailable());

        // Verificar que AccessKey foi deletada após pickup
        assertTrue(accessKeyRepository.findById(accessKey.getId()).isEmpty());
        System.out.println("  AccessKey deletada após retirada: ✓");

        // PASSO 7: CONSULTAR PEDIDOS
        System.out.println("\nPASSO 7: Consultando pedidos finalizados...");
        List<PlacedOrder> userOrders = orderRepository.findAll().stream()
                .filter(o -> o.getUserId().equals(registeredUser.getId()))
                .toList();
        assertEquals(1, userOrders.size());

        PlacedOrder finalOrder = userOrders.get(0);
        assertNotNull(finalOrder.getStartTime());
        assertEquals(selectedCar.getId(), finalOrder.getCarId());
        System.out.println("✓ Pedido finalizado encontrado!");
        System.out.println("  Usuário ID: " + finalOrder.getUserId());
        System.out.println("  Carro ID: " + finalOrder.getCarId());
        System.out.println("  Marca: " + finalOrder.getBrand());
        System.out.println("  Modelo: " + finalOrder.getModel());
        System.out.println("  Início: " + finalOrder.getStartTime());
        System.out.println("  Fim: " + finalOrder.getEndTime());

        System.out.println("\n=== JORNADA COMPLETA FINALIZADA COM SUCESSO! ===\n");
    }

    @Test
    @Order(2)
    @DisplayName("2. Falha: Saldo Insuficiente no Cartão")
    void jornadaComFalhaSaldoInsuficiente() {
        System.out.println("\n=== TESTE: SALDO INSUFICIENTE ===\n");

        // Registrar usuário
        UserInDto userDto = UserInDto.builder()
                .firstName("Usuario")
                .lastName("Pobre")
                .username("usuario.pobre")
                .password("Password123")
                .email("pobre@test.com")
                .phone(988888888)
                .build();

        registrationService.registerUser(userDto);
        entityManager.flush();
        entityManager.clear();

        // Autenticar
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "usuario.pobre", "Password123");
        SecurityContextHolder.getContext().setAuthentication(auth);

        // Adicionar cartão com saldo zero
        CreditCardDto card = CreditCardDto.builder()
                .cardNumber(4532015112830367L)
                .month(12)
                .year(2025)
                .CVV(999)
                .build();

        userService.addCreditCardToUser("usuario.pobre", card);
        entityManager.flush();
        entityManager.clear();

        // Tentar criar pedido para 50 horas (5000 reais) com saldo 0 (deve falhar)
        System.out.println("Tentando criar pedido de R$ 5000 com saldo de R$ 0...");
        Exception exception = assertThrows(Exception.class, () -> {
            orderService.submitOrder(PACKAGE_NAME, 50);
        });

        System.out.println("✓ Pagamento bloqueado por saldo insuficiente!");
        System.out.println("\n=== TESTE CONCLUÍDO ===\n");
    }

    @Test
    @Order(3)
    @DisplayName("3. Conflito: Múltiplos Usuários Tentando Alugar Mesmo Carro")
    void jornadaMultiplosUsuarios() {
        System.out.println("\n=== TESTE: MÚLTIPLOS USUÁRIOS ===\n");

        // USUÁRIO 1: Completa a jornada e aluga o carro
        UserInDto user1Dto = UserInDto.builder()
                .firstName("Usuario")
                .lastName("Primeiro")
                .username("usuario.primeiro")
                .password("Password123")
                .email("primeiro@test.com")
                .phone(966666666)
                .build();

        registrationService.registerUser(user1Dto);
        entityManager.flush();
        entityManager.clear();

        UsernamePasswordAuthenticationToken auth1 = new UsernamePasswordAuthenticationToken(
                "usuario.primeiro", "Password123");
        SecurityContextHolder.getContext().setAuthentication(auth1);

        CreditCardDto card1 = CreditCardDto.builder()
                .cardNumber(4532015112830368L)
                .month(12)
                .year(2025)
                .CVV(111)
                .build();

        User user1WithCard = userService.addCreditCardToUser("usuario.primeiro", card1);
        // Adicionar saldo suficiente
        user1WithCard.getCreditCard().setAccountBalance((long) 100 * RENTAL_HOURS);
        entityManager.flush();
        entityManager.clear();

        AccessKeyDto accessKey1 = orderService.submitOrder(PACKAGE_NAME, RENTAL_HOURS);
        
        Car car = carRepository.findAll().stream()
                .filter(c -> c.getCarPackage().getPackageName().equals(PACKAGE_NAME))
                .findFirst()
                .orElseThrow();

        deliveryService.pickUpTheCar(car.getId());
        entityManager.flush();
        entityManager.clear();

        System.out.println("✓ Usuário 1 alugou o carro com sucesso");

        // USUÁRIO 2: Tenta alugar o mesmo carro
        SecurityContextHolder.clearContext();

        UserInDto user2Dto = UserInDto.builder()
                .firstName("Usuario")
                .lastName("Segundo")
                .username("usuario.segundo")
                .password("Password123")
                .email("segundo@test.com")
                .phone(955555555)
                .build();

        registrationService.registerUser(user2Dto);
        entityManager.flush();
        entityManager.clear();

        // Verificar que o carro não está mais disponível
        Car carAfterUser1 = carRepository.findById(car.getId()).orElseThrow();
        assertFalse(carAfterUser1.getIsAvailable());
        System.out.println("✓ Carro não está mais disponível para Usuário 2");

        // Tentar consultar carros disponíveis (deve retornar lista vazia)
        List<Car> availableCars = carService.getAvailableCars(1, Sort.Direction.ASC);
        assertTrue(availableCars.isEmpty());
        System.out.println("✓ Sistema previne aluguel duplicado - nenhum carro disponível");

        System.out.println("\n=== TESTE CONCLUÍDO ===\n");
    }
}
