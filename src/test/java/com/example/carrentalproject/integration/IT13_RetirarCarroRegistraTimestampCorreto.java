package com.example.carrentalproject.integration;

import com.example.carrentalproject.constant.FuelType;
import com.example.carrentalproject.constant.GearBoxType;
import com.example.carrentalproject.domain.*;
import com.example.carrentalproject.repository.*;
import com.example.carrentalproject.security.LoggedInUser;
import com.example.carrentalproject.service.DeliveryService;
import com.example.carrentalproject.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * IT13 - Cenário 7: Pedidos e Timestamps - RetirarCarroRegistraTimestampCorreto
 * 
 * Objetivo: Validar que ao retirar um carro, o sistema registra PlacedOrder.startTime
 * com o timestamp atual e PlacedOrder.endTime = startTime + hours do AccessKey.
 * Valida: LocalDateTime persistence + timezone + cálculo de endTime
 */
@SpringBootTest
@Transactional
@DisplayName("IT13 - Retirar Carro Registra Timestamp Correto")
public class IT13_RetirarCarroRegistraTimestampCorreto {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CreditCardRepository creditCardRepository;

    @Autowired
    private CarPackageRepository carPackageRepository;

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private CarParametersRepository carParametersRepository;

    @Autowired
    private AccessKeyRepository accessKeyRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderService orderService;

    @Autowired
    private DeliveryService deliveryService;

    @MockBean
    private LoggedInUser loggedInUser;

    private User testUser;
    private Car testCar;
    private CarPackage testPackage;
    private final String testUsername = "testuser_it13";

    @BeforeEach
    void setUp() {
        // Limpar dados anteriores
        orderRepository.deleteAll();
        accessKeyRepository.deleteAll();
        carRepository.deleteAll();
        carParametersRepository.deleteAll();
        creditCardRepository.deleteAll();
        userRepository.deleteAll();
        carPackageRepository.deleteAll();

        // Criar usuário de teste
        testUser = User.builder()
                .firstName("Test")
                .lastName("UserIT13")
                .username(testUsername)
                .password("password123")
                .email("it13@test.com")
                .phone(987654321)
                .build();
        testUser = userRepository.save(testUser);

        // Criar cartão de crédito
        CreditCard creditCard = CreditCard.builder()
                .cardNumber(9876543210123456L)
                .month(12)
                .year(2028)
                .CVV(321)
                .accountBalance(50000L)
                .user(testUser)
                .build();
        creditCard = creditCardRepository.save(creditCard);

        testUser.setCreditCard(creditCard);
        testUser = userRepository.save(testUser);

        // Criar pacote
        testPackage = CarPackage.builder()
                .packageName("STANDARD")
                .pricePerHour(150)
                .build();
        testPackage = carPackageRepository.save(testPackage);

        // Criar parâmetros do carro
        CarParameters parameters = CarParameters.builder()
                .fuelType(FuelType.PETROL)
                .gearBoxType(GearBoxType.MANUAL)
                .numberOfSeats(5)
                .numberOfDoors(4)
                .isAirConditioningAvailable(true)
                .build();
        parameters = carParametersRepository.save(parameters);

        // Criar carro
        testCar = Car.builder()
                .registrationNr("TEST1234")
                .brand("Toyota")
                .model("Corolla")
                .isAvailable(true)
                .carPackage(testPackage)
                .carParameters(parameters)
                .build();
        testCar = carRepository.save(testCar);

        System.out.println("✓ Setup completo: Usuário, Pacote STANDARD, Carro Toyota Corolla");
    }

    @Test
    @DisplayName("Deve registrar startTime aproximadamente igual ao momento da retirada")
    void deveRegistrarStartTimeCorreto() {
        // Given: Usuário cria pedido de 5 horas
        when(loggedInUser.getUser()).thenReturn(testUser);
        orderService.submitOrder("STANDARD", 5);

        // Recarregar user com AccessKey
        testUser = userRepository.findById(testUser.getId()).orElseThrow();
        when(loggedInUser.getUser()).thenReturn(testUser);

        // Capturar timestamp antes da retirada
        LocalDateTime antesRetirada = LocalDateTime.now();

        // When: Retirar carro
        deliveryService.pickUpTheCar(testCar.getId());
        
        // Capturar timestamp depois da retirada
        LocalDateTime depoisRetirada = LocalDateTime.now();

        // Then: PlacedOrder criado com startTime entre antes e depois
        List<PlacedOrder> orders = orderRepository.findAll();
        assertEquals(1, orders.size());

        PlacedOrder order = orders.get(0);
        assertNotNull(order.getStartTime());
        
        // startTime deve estar entre antesRetirada e depoisRetirada (tolerância de alguns segundos)
        assertTrue(
            !order.getStartTime().isBefore(antesRetirada.minusSeconds(1)),
            "startTime deve ser >= momento antes da retirada"
        );
        assertTrue(
            !order.getStartTime().isAfter(depoisRetirada.plusSeconds(1)),
            "startTime deve ser <= momento depois da retirada"
        );

        System.out.println("✓ startTime registrado: " + order.getStartTime());
        System.out.println("✓ Diferença do momento atual: " + 
            Duration.between(order.getStartTime(), LocalDateTime.now()).toMillis() + "ms");
    }

    @Test
    @DisplayName("Deve calcular endTime = startTime + hours do AccessKey")
    void deveCalcularEndTimeCorretamente() {
        // Given: Usuário cria pedido de 8 horas
        when(loggedInUser.getUser()).thenReturn(testUser);
        orderService.submitOrder("STANDARD", 8);

        // Recarregar user
        testUser = userRepository.findById(testUser.getId()).orElseThrow();
        when(loggedInUser.getUser()).thenReturn(testUser);

        // When: Retirar carro
        deliveryService.pickUpTheCar(testCar.getId());

        // Then: endTime = startTime + 8 horas
        PlacedOrder order = orderRepository.findAll().get(0);
        
        LocalDateTime startTime = order.getStartTime();
        LocalDateTime endTime = order.getEndTime();
        LocalDateTime endTimeEsperado = startTime.plusHours(8);

        assertNotNull(startTime);
        assertNotNull(endTime);
        assertEquals(endTimeEsperado, endTime);

        // Validar diferença de exatamente 8 horas
        long horasDiferenca = ChronoUnit.HOURS.between(startTime, endTime);
        assertEquals(8, horasDiferenca);

        System.out.println("✓ startTime: " + startTime);
        System.out.println("✓ endTime: " + endTime);
        System.out.println("✓ Diferença: 8 horas (conforme pedido)");
    }

    @Test
    @DisplayName("Deve validar timestamps com diferentes durações de horas")
    void deveValidarTimestampsComDiferentesDuracoes() {
        // Test 1: 1 hora
        when(loggedInUser.getUser()).thenReturn(testUser);
        orderService.submitOrder("STANDARD", 1);
        testUser = userRepository.findById(testUser.getId()).orElseThrow();
        when(loggedInUser.getUser()).thenReturn(testUser);
        
        deliveryService.pickUpTheCar(testCar.getId());
        
        PlacedOrder order1 = orderRepository.findAll().get(0);
        assertEquals(1, ChronoUnit.HOURS.between(order1.getStartTime(), order1.getEndTime()));
        System.out.println("✓ Teste 1: 1 hora validado");

        // Preparar teste 2: limpar e criar novo carro
        orderRepository.deleteAll();
        accessKeyRepository.deleteAll();
        testUser.setAccessKey(null);
        testUser = userRepository.save(testUser);
        
        CarParameters params2 = CarParameters.builder()
                .fuelType(FuelType.DIESEL)
                .gearBoxType(GearBoxType.AUTOMATIC)
                .numberOfSeats(5)
                .numberOfDoors(4)
                .isAirConditioningAvailable(true)
                .build();
        params2 = carParametersRepository.save(params2);

        Car car2 = Car.builder()
                .registrationNr("TEST5678")
                .brand("Honda")
                .model("Civic")
                .isAvailable(true)
                .carPackage(testPackage)
                .carParameters(params2)
                .build();
        car2 = carRepository.save(car2);

        // Test 2: 72 horas (3 dias)
        when(loggedInUser.getUser()).thenReturn(testUser);
        orderService.submitOrder("STANDARD", 72);
        testUser = userRepository.findById(testUser.getId()).orElseThrow();
        when(loggedInUser.getUser()).thenReturn(testUser);
        
        deliveryService.pickUpTheCar(car2.getId());
        
        PlacedOrder order2 = orderRepository.findAll().get(0);
        assertEquals(72, ChronoUnit.HOURS.between(order2.getStartTime(), order2.getEndTime()));
        System.out.println("✓ Teste 2: 72 horas (3 dias) validado");
    }

    @Test
    @DisplayName("Deve validar persistência de LocalDateTime no banco de dados")
    void deveValidarPersistenciaLocalDateTime() {
        // Given: Criar pedido e retirar carro
        when(loggedInUser.getUser()).thenReturn(testUser);
        orderService.submitOrder("STANDARD", 10);
        testUser = userRepository.findById(testUser.getId()).orElseThrow();
        when(loggedInUser.getUser()).thenReturn(testUser);
        
        deliveryService.pickUpTheCar(testCar.getId());
        
        // Obter ID do pedido
        PlacedOrder orderOriginal = orderRepository.findAll().get(0);
        Long orderId = orderOriginal.getId();
        LocalDateTime startTimeOriginal = orderOriginal.getStartTime();
        LocalDateTime endTimeOriginal = orderOriginal.getEndTime();

        // When: Recarregar order do banco (simula nova transação)
        PlacedOrder orderRecarregado = orderRepository.findById(orderId).orElseThrow();

        // Then: Timestamps devem ser exatamente iguais (persistência correta)
        assertEquals(startTimeOriginal, orderRecarregado.getStartTime());
        assertEquals(endTimeOriginal, orderRecarregado.getEndTime());
        assertEquals(10, ChronoUnit.HOURS.between(
            orderRecarregado.getStartTime(), 
            orderRecarregado.getEndTime()
        ));

        System.out.println("✓ LocalDateTime persistido corretamente no banco");
        System.out.println("✓ startTime: " + orderRecarregado.getStartTime());
        System.out.println("✓ endTime: " + orderRecarregado.getEndTime());
    }

    @Test
    @DisplayName("Deve validar ordem PlacedOrder contém informações corretas do carro")
    void deveValidarInformacoesCarroNaOrdem() {
        // Given: Criar pedido e retirar carro
        when(loggedInUser.getUser()).thenReturn(testUser);
        orderService.submitOrder("STANDARD", 6);
        testUser = userRepository.findById(testUser.getId()).orElseThrow();
        when(loggedInUser.getUser()).thenReturn(testUser);

        // When: Retirar carro
        deliveryService.pickUpTheCar(testCar.getId());

        // Then: PlacedOrder contém informações corretas
        PlacedOrder order = orderRepository.findAll().get(0);
        
        assertEquals(testUser.getId(), order.getUserId());
        assertEquals(testCar.getId(), order.getCarId());
        assertEquals("Toyota", order.getBrand());
        assertEquals("Corolla", order.getModel());
        assertNotNull(order.getStartTime());
        assertNotNull(order.getEndTime());

        System.out.println("✓ Todas as informações da ordem validadas:");
        System.out.println("  - userId: " + order.getUserId());
        System.out.println("  - carId: " + order.getCarId());
        System.out.println("  - brand: " + order.getBrand());
        System.out.println("  - model: " + order.getModel());
        System.out.println("  - startTime: " + order.getStartTime());
        System.out.println("  - endTime: " + order.getEndTime());
    }
}
