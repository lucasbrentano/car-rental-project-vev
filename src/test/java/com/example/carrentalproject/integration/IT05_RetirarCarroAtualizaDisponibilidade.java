package com.example.carrentalproject.integration;

import com.example.carrentalproject.constant.FuelType;
import com.example.carrentalproject.constant.GearBoxType;
import com.example.carrentalproject.domain.*;
import com.example.carrentalproject.dto.AccessKeyDto;
import com.example.carrentalproject.repository.*;
import com.example.carrentalproject.security.LoggedInUser;
import com.example.carrentalproject.service.CarService;
import com.example.carrentalproject.service.DeliveryService;
import com.example.carrentalproject.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * IT05 - Cenário 3: Disponibilidade de Carros - RetirarCarroAtualizaDisponibilidade
 * 
 * Objetivo: Validar que ao retirar um carro através do DeliveryService,
 * o sistema atualiza isAvailable=false no banco, cria PlacedOrder com timestamps,
 * e integra corretamente DeliveryService + CarRepository + OrderRepository.
 */
@SpringBootTest
@Transactional
@DisplayName("IT05 - Retirar Carro Atualiza Disponibilidade")
public class IT05_RetirarCarroAtualizaDisponibilidade {

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

    @Autowired
    private AccessKeyRepository accessKeyRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderService orderService;

    @Autowired
    private DeliveryService deliveryService;

    @Autowired
    private CarService carService;

    @MockBean
    private LoggedInUser loggedInUser;

    private User testUser;
    private final String testUsername = "testuser_it05";
    private Car testCar;
    private CarPackage ordinaryPackage;

    @BeforeEach
    void setUp() {
        System.out.println("\n=== SETUP IT05: Retirar Carro Atualiza Disponibilidade ===");

        // Limpar dados de teste anteriores
        orderRepository.deleteAll();
        accessKeyRepository.deleteAll();
        creditCardRepository.deleteAll();
        userRepository.deleteAll();
        carRepository.deleteAll();
        carPackageRepository.deleteAll();
        carParametersRepository.deleteAll();

        // Criar Role ROLE_USER se não existir
        Role userRole = roleRepository.findByName("ROLE_USER").orElse(null);
        if (userRole == null) {
            userRole = new Role();
            userRole.setName("ROLE_USER");
            userRole.setUsers(new java.util.ArrayList<>());
            roleRepository.save(userRole);
            System.out.println("✓ Role ROLE_USER criada");
        }

        // Criar pacote Ordinary
        ordinaryPackage = CarPackage.builder()
            .packageName("Ordinary")
            .pricePerHour(50)
            .build();
        carPackageRepository.save(ordinaryPackage);
        System.out.println("✓ Pacote Ordinary criado (R$ 0,50/h)");

        // Criar carro disponível com pacote Ordinary
        CarParameters params = CarParameters.builder()
            .fuelType(FuelType.PETROL)
            .gearBoxType(GearBoxType.MANUAL)
            .numberOfSeats(5)
            .numberOfDoors(4)
            .isAirConditioningAvailable(true)
            .build();
        carParametersRepository.save(params);

        testCar = Car.builder()
            .registrationNr("DEF5678")
            .brand("Honda")
            .model("Civic")
            .isAvailable(true)
            .carPackage(ordinaryPackage)
            .carParameters(params)
            .build();
        carRepository.save(testCar);
        System.out.println("✓ Carro Honda Civic (DEF5678) criado - DISPONÍVEL");

        // Criar usuário com cartão de crédito
        testUser = User.builder()
            .username(testUsername)
            .password("password123")
            .email("testuser_it05@example.com")
            .firstName("Test")
            .lastName("User IT05")
            .phone(123456789)
            .build();
        userRepository.save(testUser);

        CreditCard testCard = CreditCard.builder()
            .cardNumber(1234567890123456L)
            .month(12)
            .year(2025)
            .CVV(123)
            .accountBalance(100000L) // R$ 1000,00
            .user(testUser)
            .build();
        creditCardRepository.save(testCard);

        testUser.setCreditCard(testCard);
        userRepository.save(testUser);

        System.out.println("✓ Usuário criado com cartão (saldo: R$ 1000,00)");

        // Mock LoggedInUser
        when(loggedInUser.getUser()).thenReturn(testUser);
        System.out.println("✓ LoggedInUser mockado");

        System.out.println("=== SETUP COMPLETO ===\n");
    }

    @Test
    @DisplayName("Deve retirar carro e atualizar isAvailable para false")
    void deveRetirarCarroEAtualizarDisponibilidade() {
        System.out.println("\n=== TESTE: Retirar Carro e Atualizar Disponibilidade ===");

        // 1. Criar pedido (AccessKey) e pagar
        AccessKeyDto accessKey = orderService.submitOrder("Ordinary", 5);
        System.out.println("➊ Pedido criado e pago: " + accessKey.getCarPackage() + " - " + accessKey.getHours() + "h");

        // Atualizar referência do usuário
        testUser = userRepository.findByUsername(testUsername).orElseThrow();
        when(loggedInUser.getUser()).thenReturn(testUser);

        // Verificar que carro ainda está disponível ANTES da retirada
        Car carroAntes = carRepository.findById(testCar.getId()).orElseThrow();
        assertTrue(carroAntes.getIsAvailable(), "Carro deve estar disponível antes da retirada");
        System.out.println("➋ Carro disponível ANTES da retirada (isAvailable=" + carroAntes.getIsAvailable() + ")");

        // 2. Retirar carro (POST /delivery)
        LocalDateTime timestampAntes = LocalDateTime.now();
        Car carroRetirado = deliveryService.pickUpTheCar(testCar.getId());
        LocalDateTime timestampDepois = LocalDateTime.now();
        System.out.println("➌ Carro retirado através de DeliveryService");

        // 3. Verificar isAvailable = false no banco
        Car carroDepois = carRepository.findById(testCar.getId()).orElseThrow();
        assertFalse(carroDepois.getIsAvailable(), "Carro deve estar INDISPONÍVEL após retirada (isAvailable=false)");
        assertFalse(carroRetirado.getIsAvailable(), "Objeto retornado deve ter isAvailable=false");
        System.out.println("➍ Carro agora INDISPONÍVEL no banco (isAvailable=" + carroDepois.getIsAvailable() + ")");

        // 4. Validar que PlacedOrder foi criado
        List<PlacedOrder> orders = orderRepository.findAll();
        assertEquals(1, orders.size(), "Deve haver 1 PlacedOrder criado");
        PlacedOrder order = orders.get(0);
        System.out.println("➎ PlacedOrder criado: ID=" + order.getId());

        // Validar dados do PlacedOrder
        assertNotNull(order.getId(), "PlacedOrder deve ter ID gerado");
        assertEquals(testUser.getId(), order.getUserId(), "PlacedOrder deve ter userId correto");
        assertEquals(testCar.getId(), order.getCarId(), "PlacedOrder deve ter carId correto");
        assertEquals("Honda", order.getBrand(), "PlacedOrder deve ter brand correto");
        assertEquals("Civic", order.getModel(), "PlacedOrder deve ter model correto");
        System.out.println("   ✓ Dados do PlacedOrder validados");

        // 5. Validar timestamps (startTime e endTime)
        assertNotNull(order.getStartTime(), "PlacedOrder deve ter startTime");
        assertNotNull(order.getEndTime(), "PlacedOrder deve ter endTime");

        // StartTime deve ser aproximadamente o momento da retirada
        assertTrue(order.getStartTime().isAfter(timestampAntes.minusSeconds(5)),
                "StartTime deve ser após timestamp inicial");
        assertTrue(order.getStartTime().isBefore(timestampDepois.plusSeconds(5)),
                "StartTime deve ser antes de timestamp final");

        // EndTime deve ser startTime + 5 horas (duração do pedido)
        LocalDateTime expectedEndTime = order.getStartTime().plusHours(5);
        assertEquals(expectedEndTime, order.getEndTime(),
                "EndTime deve ser startTime + 5 horas");

        System.out.println("➏ Timestamps validados:");
        System.out.println("   StartTime: " + order.getStartTime());
        System.out.println("   EndTime: " + order.getEndTime());
        System.out.println("   Duração: " + accessKey.getHours() + " horas");

        System.out.println("\n=== ✅ FLUXO DE RETIRADA E ATUALIZAÇÃO VALIDADO ===\n");
    }

    @Test
    @DisplayName("Deve remover carro da lista de disponíveis após retirada")
    void deveRemoverCarroDaListaDeDisponiveisAposRetirada() {
        System.out.println("\n=== TESTE: Remover Carro da Lista de Disponíveis ===");

        // Consultar lista ANTES da retirada
        List<Car> availableAntes = carService.getAvailableCars(1, Sort.Direction.ASC);
        System.out.println("➊ Carros disponíveis ANTES: " + availableAntes.size());
        assertTrue(availableAntes.stream().anyMatch(c -> c.getId().equals(testCar.getId())),
                "Carro deve estar na lista ANTES da retirada");

        // Criar pedido e retirar carro
        orderService.submitOrder("Ordinary", 3);
        testUser = userRepository.findByUsername(testUsername).orElseThrow();
        when(loggedInUser.getUser()).thenReturn(testUser);

        deliveryService.pickUpTheCar(testCar.getId());
        System.out.println("➋ Carro retirado");

        // Consultar lista DEPOIS da retirada
        List<Car> availableDepois = carService.getAvailableCars(1, Sort.Direction.ASC);
        System.out.println("➌ Carros disponíveis DEPOIS: " + availableDepois.size());

        assertEquals(availableAntes.size() - 1, availableDepois.size(),
                "Lista deve ter 1 carro a menos");
        assertFalse(availableDepois.stream().anyMatch(c -> c.getId().equals(testCar.getId())),
                "Carro NÃO deve aparecer na lista DEPOIS da retirada");
        System.out.println("   ✓ Carro removido da lista de disponíveis");

        System.out.println("\n=== ✅ SINCRONIZAÇÃO CARSERVICE ↔ CARREPOSITORY VALIDADA ===\n");
    }

    @Test
    @DisplayName("Deve validar integração completa DeliveryService + CarRepository + OrderRepository")
    void deveValidarIntegracaoCompletaDeliveryServiceRepositories() {
        System.out.println("\n=== TESTE: Integração DeliveryService + Repositories ===");

        // Estado inicial
        assertEquals(0, orderRepository.count(), "OrderRepository deve estar vazio inicialmente");
        Car carroInicial = carRepository.findById(testCar.getId()).orElseThrow();
        assertTrue(carroInicial.getIsAvailable(), "Carro deve estar disponível inicialmente");
        System.out.println("➊ Estado inicial: 0 orders, carro disponível");

        // Criar pedido
        orderService.submitOrder("Ordinary", 8);
        User userWithAccessKey = userRepository.findByUsername(testUsername).orElseThrow();
        when(loggedInUser.getUser()).thenReturn(userWithAccessKey);
        System.out.println("➋ Pedido criado: 8 horas");

        // Retirar carro - integração acontece aqui
        deliveryService.pickUpTheCar(testCar.getId());
        System.out.println("➌ Carro retirado - integrações executadas");

        // Validar CarRepository atualizado
        Car carroAtualizado = carRepository.findById(testCar.getId()).orElseThrow();
        assertFalse(carroAtualizado.getIsAvailable(), "CarRepository: isAvailable deve ser false");
        System.out.println("   ✓ CarRepository atualizado (isAvailable=false)");

        // Validar OrderRepository criou PlacedOrder
        assertEquals(1, orderRepository.count(), "OrderRepository: deve ter 1 PlacedOrder");
        PlacedOrder order = orderRepository.findAll().get(0);
        assertEquals(testCar.getId(), order.getCarId(), "OrderRepository: PlacedOrder deve ter carId correto");
        System.out.println("   ✓ OrderRepository criou PlacedOrder (carId=" + order.getCarId() + ")");

        // Validar AccessKeyRepository removeu AccessKey
        Long accessKeyCount = accessKeyRepository.count();
        assertEquals(0L, accessKeyCount, "AccessKeyRepository: AccessKey deve ter sido removido");
        System.out.println("   ✓ AccessKeyRepository removeu AccessKey (count=" + accessKeyCount + ")");

        System.out.println("\n=== ✅ INTEGRAÇÃO COMPLETA VALIDADA ===\n");
    }

    @Test
    @DisplayName("Deve validar múltiplos carros - apenas retirado fica indisponível")
    void deveValidarMultiplosCarrosApenasRetiradoFicaIndisponivel() {
        System.out.println("\n=== TESTE: Múltiplos Carros - Isolamento de Disponibilidade ===");

        // Criar segundo carro disponível (não será retirado)
        Car car2 = Car.builder()
            .registrationNr("GHI9012")
            .brand("Toyota")
            .model("Corolla")
            .isAvailable(true)
            .carPackage(ordinaryPackage)
            .build();
        carRepository.save(car2);
        System.out.println("➊ Criados 2 carros disponíveis");

        // Criar pedido e retirar apenas o primeiro carro
        orderService.submitOrder("Ordinary", 4);
        testUser = userRepository.findByUsername(testUsername).orElseThrow();
        when(loggedInUser.getUser()).thenReturn(testUser);

        deliveryService.pickUpTheCar(testCar.getId());
        System.out.println("➋ Apenas Honda Civic (DEF5678) retirado");

        // Validar que apenas o carro retirado fica indisponível
        Car carroRetirado = carRepository.findById(testCar.getId()).orElseThrow();
        Car carroNaoRetirado = carRepository.findById(car2.getId()).orElseThrow();

        assertFalse(carroRetirado.getIsAvailable(), "Carro retirado deve estar INDISPONÍVEL");
        assertTrue(carroNaoRetirado.getIsAvailable(), "Carro não retirado deve estar DISPONÍVEL");
        System.out.println("➌ Validação:");
        System.out.println("   Honda Civic (retirado): isAvailable=" + carroRetirado.getIsAvailable() + " ✓");
        System.out.println("   Toyota Corolla (não retirado): isAvailable=" + carroNaoRetirado.getIsAvailable() + " ✓");

        // Consultar lista de disponíveis - apenas Toyota deve aparecer
        List<Car> availableCars = carService.getAvailableCars(1, Sort.Direction.ASC);
        assertEquals(1, availableCars.size(), "Deve haver apenas 1 carro disponível");
        assertEquals(car2.getId(), availableCars.get(0).getId(), "Toyota Corolla deve estar na lista");
        System.out.println("➍ Lista de disponíveis contém apenas Toyota Corolla");

        System.out.println("\n=== ✅ ISOLAMENTO DE DISPONIBILIDADE VALIDADO ===\n");
    }

    @Test
    @DisplayName("Deve validar que AccessKey é removido após retirada")
    void deveValidarRemocaoAccessKeyAposRetirada() {
        System.out.println("\n=== TESTE: Remoção de AccessKey ===");

        // Criar pedido
        AccessKeyDto accessKeyDto = orderService.submitOrder("Ordinary", 6);
        testUser = userRepository.findByUsername(testUsername).orElseThrow();
        when(loggedInUser.getUser()).thenReturn(testUser);

        assertNotNull(testUser.getAccessKey(), "Usuário deve ter AccessKey após pedido");
        Long accessKeyId = testUser.getAccessKey().getId();
        System.out.println("➊ AccessKey criado: ID=" + accessKeyId);

        // Retirar carro
        deliveryService.pickUpTheCar(testCar.getId());
        System.out.println("➋ Carro retirado");

        // Validar que AccessKey foi deletado do repositório
        assertFalse(accessKeyRepository.findById(accessKeyId).isPresent(),
                "AccessKey deve ter sido deletado do repositório");
        assertEquals(0L, accessKeyRepository.count(), "AccessKeyRepository deve estar vazio");
        System.out.println("➌ AccessKey deletado do AccessKeyRepository (count=0)");

        System.out.println("➍ Validação: AccessKey não existe mais no banco");

        System.out.println("\n=== ✅ REMOÇÃO DE ACCESSKEY VALIDADA ===\n");
    }
}
