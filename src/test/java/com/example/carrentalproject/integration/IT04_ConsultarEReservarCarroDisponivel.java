package com.example.carrentalproject.integration;

import com.example.carrentalproject.constant.FuelType;
import com.example.carrentalproject.constant.GearBoxType;
import com.example.carrentalproject.domain.*;
import com.example.carrentalproject.dto.AccessKeyDto;
import com.example.carrentalproject.repository.*;
import com.example.carrentalproject.security.LoggedInUser;
import com.example.carrentalproject.service.CarService;
import com.example.carrentalproject.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * IT04 - Cenário 3: Disponibilidade de Carros - ConsultarEReservarCarroDisponivel
 * 
 * Objetivo: Validar que ao criar um pedido, o sistema atualiza a disponibilidade do carro
 * e sincroniza corretamente entre OrderService e CarRepository.
 * 
 * IMPORTANTE: OrderService.submitOrder() NÃO altera isAvailable (apenas cria AccessKey).
 * A disponibilidade só muda quando DeliveryService.pickUpTheCar() é chamado.
 * Portanto, este teste valida que carros disponíveis são retornados antes do pedido,
 * mas o teste de indisponibilidade será no IT05 (após delivery).
 */
@SpringBootTest
@Transactional
@DisplayName("IT04 - Consultar e Reservar Carro Disponível")
public class IT04_ConsultarEReservarCarroDisponivel {

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
    private OrderService orderService;

    @Autowired
    private CarService carService;

    @MockBean
    private LoggedInUser loggedInUser;

    private User testUser;
    private final String testUsername = "testuser_it04";
    private Car testCar;
    private CarPackage ordinaryPackage;

    @BeforeEach
    void setUp() {
        System.out.println("\n=== SETUP IT04: Consultar e Reservar Carro Disponível ===");

        // Limpar dados de teste anteriores
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
            .registrationNr("ABC1234")
            .brand("Toyota")
            .model("Corolla")
            .isAvailable(true)
            .carPackage(ordinaryPackage)
            .carParameters(params)
            .build();
        carRepository.save(testCar);
        System.out.println("✓ Carro Toyota Corolla (ABC1234) criado - DISPONÍVEL");

        // Criar usuário com cartão de crédito
        testUser = User.builder()
            .username(testUsername)
            .password("password123")
            .email("testuser_it04@example.com")
            .firstName("Test")
            .lastName("User IT04")
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
    @DisplayName("Deve consultar carros disponíveis e criar pedido")
    void deveConsultarCarrosDisponiveisECriarPedido() {
        System.out.println("\n=== TESTE: Consultar Carros Disponíveis e Criar Pedido ===");

        // 1. Consultar carros disponíveis ANTES do pedido
        List<Car> availableCarsAntes = carService.getAvailableCars(1, Sort.Direction.ASC);
        System.out.println("➊ Carros disponíveis ANTES do pedido: " + availableCarsAntes.size());

        assertFalse(availableCarsAntes.isEmpty(), "Deve haver pelo menos 1 carro disponível");
        assertTrue(availableCarsAntes.stream().anyMatch(c -> c.getRegistrationNr().equals("ABC1234")),
                "Toyota Corolla (ABC1234) deve estar disponível");

        Car carroEncontrado = availableCarsAntes.stream()
                .filter(c -> c.getRegistrationNr().equals("ABC1234"))
                .findFirst()
                .orElseThrow();
        assertTrue(carroEncontrado.getIsAvailable(), "Carro deve estar disponível (isAvailable=true)");
        System.out.println("   ✓ Toyota Corolla encontrado e disponível");

        // 2. Criar pedido para carro específico (pacote Ordinary, 10 horas)
        AccessKeyDto accessKey = orderService.submitOrder("Ordinary", 10);
        System.out.println("➋ Pedido criado: " + accessKey.getCarPackage() + " - " + accessKey.getHours() + "h");

        assertNotNull(accessKey, "AccessKey deve ser retornado");
        assertEquals("Ordinary", accessKey.getCarPackage(), "Pacote deve ser Ordinary");
        assertEquals(10, accessKey.getHours(), "Horas devem ser 10");
        System.out.println("   ✓ AccessKey validado");

        // 3. Verificar que usuário tem AccessKey
        testUser = userRepository.findByUsername(testUsername).orElseThrow();
        assertNotNull(testUser.getAccessKey(), "Usuário deve ter AccessKey após pedido");
        assertEquals("Ordinary", testUser.getAccessKey().getCarPackage(), "AccessKey deve ter pacote Ordinary");
        System.out.println("➌ Usuário agora tem AccessKey: " + testUser.getAccessKey().getCarPackage());

        // 4. IMPORTANTE: OrderService NÃO altera isAvailable - carro ainda está disponível
        Car carroAposPedido = carRepository.findById(testCar.getId()).orElseThrow();
        assertTrue(carroAposPedido.getIsAvailable(),
                "IMPORTANTE: Carro ainda deve estar disponível após submitOrder() - só fica indisponível após delivery");
        System.out.println("➍ Carro ainda disponível após pedido (isAvailable=" + carroAposPedido.getIsAvailable() + ")");

        // 5. Consultar carros disponíveis DEPOIS do pedido - carro ainda aparece
        List<Car> availableCarsDepois = carService.getAvailableCars(1, Sort.Direction.ASC);
        System.out.println("➎ Carros disponíveis DEPOIS do pedido: " + availableCarsDepois.size());

        assertEquals(availableCarsAntes.size(), availableCarsDepois.size(),
                "Quantidade de carros disponíveis deve ser igual (carro só fica indisponível após delivery)");
        assertTrue(availableCarsDepois.stream().anyMatch(c -> c.getRegistrationNr().equals("ABC1234")),
                "Toyota Corolla ainda deve aparecer como disponível");
        System.out.println("   ✓ Carro ainda aparece na lista de disponíveis");

        System.out.println("\n=== ✅ FLUXO DE CONSULTA E RESERVA VALIDADO ===\n");
    }

    @Test
    @DisplayName("Deve impedir segundo pedido com ExistingOrderException")
    void deveImpedirSegundoPedido() {
        System.out.println("\n=== TESTE: Impedir Segundo Pedido ===");

        // Criar primeiro pedido
        AccessKeyDto firstOrder = orderService.submitOrder("Ordinary", 5);
        System.out.println("➊ Primeiro pedido criado: " + firstOrder.getCarPackage());

        // Atualizar referência do usuário
        testUser = userRepository.findByUsername(testUsername).orElseThrow();
        when(loggedInUser.getUser()).thenReturn(testUser);

        // Tentar criar segundo pedido
        System.out.println("➋ Tentando criar segundo pedido...");
        Exception exception = assertThrows(com.example.carrentalproject.exception.ExistingOrderException.class,
                () -> orderService.submitOrder("Ordinary", 3));

        System.out.println("   ✓ Exceção lançada: " + exception.getMessage());
        assertTrue(exception.getMessage().contains("Already Placed An Order"),
                "Mensagem deve indicar que já existe pedido");

        System.out.println("\n=== ✅ VALIDAÇÃO DE PEDIDO ÚNICO CONFIRMADA ===\n");
    }

    @Test
    @DisplayName("Deve validar integração CarService.getAvailableCars() com CarRepository")
    void deveValidarIntegracaoCarServiceComRepository() {
        System.out.println("\n=== TESTE: Integração CarService + CarRepository ===");

        // Criar mais carros: 2 disponíveis, 1 indisponível
        Car car2 = Car.builder()
            .registrationNr("XYZ9876")
            .brand("Honda")
            .model("Civic")
            .isAvailable(true)
            .carPackage(ordinaryPackage)
            .build();
        carRepository.save(car2);

        Car car3 = Car.builder()
            .registrationNr("QWE4567")
            .brand("Ford")
            .model("Focus")
            .isAvailable(false) // INDISPONÍVEL
            .carPackage(ordinaryPackage)
            .build();
        carRepository.save(car3);

        System.out.println("➊ Criados 3 carros: 2 disponíveis + 1 indisponível");

        // Consultar através do CarService
        List<Car> availableCars = carService.getAvailableCars(1, Sort.Direction.ASC);
        System.out.println("➋ CarService retornou " + availableCars.size() + " carros disponíveis");

        // Validar que apenas carros disponíveis são retornados
        assertEquals(2, availableCars.size(), "Deve retornar apenas 2 carros disponíveis");
        assertTrue(availableCars.stream().allMatch(Car::getIsAvailable),
                "Todos os carros retornados devem ter isAvailable=true");
        assertFalse(availableCars.stream().anyMatch(c -> c.getRegistrationNr().equals("QWE4567")),
                "Ford Focus (indisponível) NÃO deve aparecer");
        System.out.println("   ✓ Apenas carros disponíveis retornados");
        System.out.println("   ✓ Ford Focus (indisponível) não apareceu na lista");

        // Verificar que consulta direta ao repository tem o mesmo comportamento
        List<Car> repositoryResult = carRepository.findAvailableCars(
                org.springframework.data.domain.PageRequest.of(0, 10,
                        org.springframework.data.domain.Sort.by(Sort.Direction.ASC, "id")));
        assertEquals(availableCars.size(), repositoryResult.size(),
                "CarService e Repository devem retornar mesma quantidade");
        System.out.println("➌ Integração CarService ↔ CarRepository validada");

        System.out.println("\n=== ✅ SINCRONIZAÇÃO VALIDADA ===\n");
    }

    @Test
    @DisplayName("Deve validar que AccessKey persiste corretamente")
    void deveValidarPersistenciaAccessKey() {
        System.out.println("\n=== TESTE: Persistência AccessKey ===");

        // Criar pedido
        AccessKeyDto accessKeyDto = orderService.submitOrder("Ordinary", 8);
        System.out.println("➊ Pedido criado: 8 horas");

        // Buscar AccessKey diretamente no banco
        testUser = userRepository.findByUsername(testUsername).orElseThrow();
        AccessKey accessKey = testUser.getAccessKey();

        assertNotNull(accessKey, "AccessKey deve estar persistido");
        assertNotNull(accessKey.getId(), "AccessKey deve ter ID gerado");
        assertEquals("Ordinary", accessKey.getCarPackage(), "Pacote deve ser Ordinary");
        assertEquals(8, accessKey.getHours(), "Horas devem ser 8");
        assertEquals(testUser.getId(), accessKey.getUser().getId(), "AccessKey deve estar vinculado ao usuário");
        System.out.println("➋ AccessKey persistido: ID=" + accessKey.getId() + ", Package=" + accessKey.getCarPackage());

        // Verificar que AccessKey está no repositório
        AccessKey foundKey = accessKeyRepository.findById(accessKey.getId()).orElseThrow();
        assertEquals(accessKey.getId(), foundKey.getId(), "AccessKey deve ser encontrado no repositório");
        System.out.println("➌ AccessKey recuperado do repositório com sucesso");

        System.out.println("\n=== ✅ PERSISTÊNCIA VALIDADA ===\n");
    }

    @Test
    @DisplayName("Deve validar que saldo é deduzido corretamente")
    void deveValidarDeducaoSaldo() {
        System.out.println("\n=== TESTE: Dedução de Saldo ===");

        Long saldoInicial = testUser.getCreditCard().getAccountBalance();
        System.out.println("➊ Saldo inicial: R$ " + saldoInicial / 100.0);

        // Criar pedido: Ordinary 10h * R$ 0,50/h = R$ 5,00 (500 centavos)
        orderService.submitOrder("Ordinary", 10);
        System.out.println("➋ Pedido criado: Ordinary 10h (custo: R$ 5,00)");

        // Verificar saldo atualizado
        testUser = userRepository.findByUsername(testUsername).orElseThrow();
        Long saldoFinal = testUser.getCreditCard().getAccountBalance();
        System.out.println("➌ Saldo final: R$ " + saldoFinal / 100.0);

        Long custoEsperado = 500L; // Ordinary: 50 centavos/h * 10h = 500 centavos
        Long saldoEsperado = saldoInicial - custoEsperado;

        assertEquals(saldoEsperado, saldoFinal, "Saldo deve ser deduzido corretamente");
        assertEquals(99500L, saldoFinal, "Saldo deve ser R$ 995,00");
        System.out.println("   ✓ Dedução correta: R$ " + custoEsperado / 100.0 + " removidos");

        System.out.println("\n=== ✅ DEDUÇÃO VALIDADA ===\n");
    }
}
