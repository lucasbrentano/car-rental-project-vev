package com.example.carrentalproject.integration;

import com.example.carrentalproject.constant.FuelType;
import com.example.carrentalproject.constant.GearBoxType;
import com.example.carrentalproject.domain.*;
import com.example.carrentalproject.dto.AccessKeyDto;
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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * IT07 - Cenário 4: Autenticação e Autorização - UsuarioSoRetiraProprioCarroPedido
 * 
 * Objetivo: Validar que usuários só podem retirar carros de seus próprios pedidos.
 * Testa a autorização baseada em ownership através do LoggedInUser.
 * 
 * Cenário:
 * - Usuário A cria pedido (tem AccessKey)
 * - Usuário B tenta retirar carro do pedido de A (sem AccessKey ou com AccessKey diferente)
 * - Sistema deve impedir através de NoAccessKeyException ou InvalidPackageException
 * 
 * IMPORTANTE: DeliveryService.pickUpTheCar() valida:
 * 1. user.getAccessKey() != null (NoAccessKeyException)
 * 2. accessKey.carPackage == car.carPackage (InvalidPackageException)
 * 3. car.isAvailable == true (UnavailableCarException)
 * 
 * A "autorização por ownership" é implícita: só quem tem AccessKey correspondente pode retirar.
 */
@SpringBootTest
@Transactional
@DisplayName("IT07 - Usuário Só Retira Próprio Carro do Pedido")
public class IT07_UsuarioSoRetiraProprioCarroPedido {

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

    @MockBean
    private LoggedInUser loggedInUser;

    private User userA;
    private User userB;
    private Car testCar;
    private CarPackage ordinaryPackage;
    private final String usernameA = "userA_it07";
    private final String usernameB = "userB_it07";

    @BeforeEach
    void setUp() {
        System.out.println("\n=== SETUP IT07: Validar Ownership de Pedidos ===");

        // Limpar dados de teste anteriores
        accessKeyRepository.deleteAll();
        orderRepository.deleteAll();
        carRepository.deleteAll();
        carParametersRepository.deleteAll();
        carPackageRepository.deleteAll();
        creditCardRepository.deleteAll();
        userRepository.deleteAll();

        // Criar pacote Ordinary
        ordinaryPackage = CarPackage.builder()
                .packageName("Ordinary")
                .pricePerHour(50)
                .build();
        carPackageRepository.save(ordinaryPackage);

        // Criar parâmetros do carro
        CarParameters carParameters = CarParameters.builder()
                .fuelType(FuelType.PETROL)
                .gearBoxType(GearBoxType.MANUAL)
                .numberOfDoors(4)
                .numberOfSeats(5)
                .isAirConditioningAvailable(true)
                .build();
        carParametersRepository.save(carParameters);

        // Criar carro disponível
        testCar = Car.builder()
                .brand("Honda")
                .model("Civic")
                .registrationNr("XYZ5678")
                .isAvailable(true)
                .carPackage(ordinaryPackage)
                .carParameters(carParameters)
                .build();
        carRepository.save(testCar);

        // Criar Usuário A com cartão de crédito
        CreditCard creditCardA = CreditCard.builder()
                .cardNumber(1111222233334444L)
                .month(12)
                .year(2025)
                .CVV(111)
                .accountBalance(100000L)
                .build();
        creditCardRepository.save(creditCardA);

        userA = User.builder()
                .username(usernameA)
                .password("password123")
                .firstName("User")
                .lastName("A IT07")
                .email("usera@test.com")
                .phone(111111111)
                .creditCard(creditCardA)
                .build();
        userRepository.save(userA);
        creditCardA.setUser(userA);

        // Criar Usuário B com cartão de crédito (mas SEM pedido)
        CreditCard creditCardB = CreditCard.builder()
                .cardNumber(5555666677778888L)
                .month(12)
                .year(2025)
                .CVV(222)
                .accountBalance(100000L)
                .build();
        creditCardRepository.save(creditCardB);

        userB = User.builder()
                .username(usernameB)
                .password("password456")
                .firstName("User")
                .lastName("B IT07")
                .email("userb@test.com")
                .phone(222222222)
                .creditCard(creditCardB)
                .build();
        userRepository.save(userB);
        creditCardB.setUser(userB);

        System.out.println("Criados: userA (" + usernameA + "), userB (" + usernameB + "), 1 carro disponível");
    }

    @Test
    @DisplayName("Deve impedir que Usuário B retire carro do pedido de Usuário A (sem AccessKey)")
    void deveImpedirUsuarioBRetirarCarroUsuarioASemAccessKey() {
        // Given: Usuário A cria pedido
        when(loggedInUser.getUser()).thenReturn(userA);
        AccessKeyDto accessKeyDto = orderService.submitOrder("Ordinary", 5);
        assertNotNull(accessKeyDto, "Pedido de Usuário A deve ter sido criado");

        // Recarregar userA para obter AccessKey persistido
        userA = userRepository.findByUsername(usernameA).orElseThrow();
        assertNotNull(userA.getAccessKey(), "Usuário A deve ter AccessKey");

        System.out.println("✓ Usuário A criou pedido com AccessKey ID: " + userA.getAccessKey().getId());

        // When: Usuário B (sem AccessKey) tenta retirar o carro
        when(loggedInUser.getUser()).thenReturn(userB);

        // Then: Deve lançar NoAccessKeyException
        Exception exception = assertThrows(Exception.class, () -> {
            deliveryService.pickUpTheCar(testCar.getId());
        });

        assertEquals("com.example.carrentalproject.exception.NoAccessKeyException", 
                exception.getClass().getName(),
                "Deve lançar NoAccessKeyException quando usuário não tem AccessKey");
        assertTrue(exception.getMessage().contains("You Do Not Have An Access Key!"),
                "Mensagem da exceção deve indicar ausência de AccessKey");

        System.out.println("✓ Usuário B foi bloqueado: " + exception.getMessage());

        // Validar que carro continua disponível (pickup não ocorreu)
        Car carAfter = carRepository.findById(testCar.getId()).orElseThrow();
        assertTrue(carAfter.getIsAvailable(), "Carro deve continuar disponível após falha de autorização");
        System.out.println("✓ Carro continua disponível após tentativa não autorizada");
    }

    @Test
    @DisplayName("Deve permitir que Usuário A retire seu próprio carro")
    void devePermitirUsuarioARetirarProprioCarroComSucesso() {
        // Given: Usuário A cria pedido
        when(loggedInUser.getUser()).thenReturn(userA);
        AccessKeyDto accessKeyDto = orderService.submitOrder("Ordinary", 5);
        assertNotNull(accessKeyDto);

        // Recarregar userA para obter AccessKey
        userA = userRepository.findByUsername(usernameA).orElseThrow();
        assertNotNull(userA.getAccessKey());

        System.out.println("✓ Usuário A criou pedido com AccessKey");

        // When: Usuário A retira o carro (autorizado)
        Car carReturned = deliveryService.pickUpTheCar(testCar.getId());

        // Then: Pickup deve ter sucesso
        assertNotNull(carReturned);
        assertFalse(carReturned.getIsAvailable(), "Carro deve ficar indisponível após retirada");

        // Validar que AccessKey foi removido
        Long accessKeyCount = accessKeyRepository.count();
        assertEquals(0L, accessKeyCount, "AccessKey deve ter sido removido após retirada");

        // Validar que PlacedOrder foi criado
        Long placedOrderCount = orderRepository.count();
        assertEquals(1L, placedOrderCount, "PlacedOrder deve ter sido criado");

        System.out.println("✓ Usuário A retirou carro com sucesso (autorizado)");
    }

    @Test
    @DisplayName("Deve validar que usuário sem pedido não consegue retirar nenhum carro")
    void deveValidarQueUsuarioSemPedidoNaoRetiraCarro() {
        // Given: Usuário B não tem pedido (sem AccessKey)
        when(loggedInUser.getUser()).thenReturn(userB);

        // When & Then: Tentar retirar carro deve falhar
        Exception exception = assertThrows(Exception.class, () -> {
            deliveryService.pickUpTheCar(testCar.getId());
        });

        assertEquals("com.example.carrentalproject.exception.NoAccessKeyException",
                exception.getClass().getName());
        assertTrue(exception.getMessage().contains("You Do Not Have An Access Key!"));

        System.out.println("✓ Usuário sem pedido não consegue retirar carro: " + exception.getMessage());

        // Validar que nenhum PlacedOrder foi criado
        Long placedOrderCount = orderRepository.count();
        assertEquals(0L, placedOrderCount, "Nenhum PlacedOrder deve existir");
    }

    @Test
    @DisplayName("Deve impedir Usuário B de retirar carro mesmo se tiver AccessKey de pacote diferente")
    void deveImpedirRetiradaComAccessKeyPacoteDiferente() {
        // Given: Criar pacote Luxury
        CarPackage luxuryPackage = CarPackage.builder()
                .packageName("Luxury")
                .pricePerHour(150)
                .build();
        carPackageRepository.save(luxuryPackage);

        // Usuário A cria pedido Ordinary
        when(loggedInUser.getUser()).thenReturn(userA);
        AccessKeyDto accessKeyDtoA = orderService.submitOrder("Ordinary", 5);
        assertNotNull(accessKeyDtoA);

        System.out.println("✓ Usuário A criou pedido Ordinary");

        // Usuário B cria pedido Luxury manualmente (simulando cenário onde teria pedido de outro pacote)
        AccessKey accessKeyB = AccessKey.builder()
                .carPackage("Luxury")
                .hours(3)
                .user(userB)
                .build();
        accessKeyRepository.save(accessKeyB);
        userB.setAccessKey(accessKeyB);
        userRepository.save(userB);

        System.out.println("✓ Usuário B tem AccessKey do pacote Luxury");

        // When: Usuário B tenta retirar carro do pacote Ordinary (incompatível)
        when(loggedInUser.getUser()).thenReturn(userB);

        // Then: Deve lançar InvalidPackageException
        Exception exception = assertThrows(Exception.class, () -> {
            deliveryService.pickUpTheCar(testCar.getId());
        });

        assertEquals("com.example.carrentalproject.exception.InvalidPackageException",
                exception.getClass().getName());
        assertTrue(exception.getMessage().contains("You Cannot Pick Car From This Package!"));

        System.out.println("✓ Usuário B bloqueado por pacote incompatível: " + exception.getMessage());

        // Validar que carro continua disponível
        Car carAfter = carRepository.findById(testCar.getId()).orElseThrow();
        assertTrue(carAfter.getIsAvailable(), "Carro deve continuar disponível");
    }

    @Test
    @DisplayName("Deve validar integração completa de ownership através de LoggedInUser")
    void deveValidarIntegracaoCompletaOwnershipLoggedInUser() {
        // Given: Usuário A cria pedido
        when(loggedInUser.getUser()).thenReturn(userA);
        orderService.submitOrder("Ordinary", 5);

        // Recarregar userA
        userA = userRepository.findByUsername(usernameA).orElseThrow();

        // Validar que apenas userA tem AccessKey
        assertNotNull(userA.getAccessKey(), "Apenas Usuário A deve ter AccessKey");

        Optional<User> userBReloaded = userRepository.findByUsername(usernameB);
        assertNull(userBReloaded.get().getAccessKey(), "Usuário B NÃO deve ter AccessKey");

        System.out.println("✓ Ownership validado: apenas Usuário A tem AccessKey");

        // Validar que LoggedInUser determina quem pode retirar
        when(loggedInUser.getUser()).thenReturn(userA);
        Car carA = deliveryService.pickUpTheCar(testCar.getId());
        assertNotNull(carA, "Usuário A (dono do pedido) pode retirar");
        assertFalse(carA.getIsAvailable(), "Carro deve ficar indisponível");

        System.out.println("✓ LoggedInUser garante que apenas dono do pedido retira o carro");
    }
}
