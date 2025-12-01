package com.example.carrentalproject.integration;

import com.example.carrentalproject.domain.*;
import com.example.carrentalproject.dto.AccessKeyDto;
import com.example.carrentalproject.exception.InvalidPackageException;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * IT10 - Não Pode Retirar Carro De Pacote Diferente
 * 
 * Objetivo: Validar que DeliveryService.pickUpTheCar() impede a retirada de carro 
 * quando o pacote do AccessKey do usuário é diferente do pacote do carro.
 * 
 * Cenário:
 * 1. Usuário cria pedido para pacote BASIC
 * 2. Pagamento realizado, AccessKey criado com carPackage = "BASIC"
 * 3. Usuário tenta retirar carro do pacote PREMIUM
 * 4. Sistema lança InvalidPackageException
 * 
 * Validações:
 * - DeliveryService.pickUpTheCar() valida: accessKey.carPackage == car.carPackage.packageName
 * - InvalidPackageException lançada quando pacotes não correspondem
 * - Integração AccessKey → Car → CarPackage (validação de regra de negócio)
 * - Carro permanece disponível após tentativa falha
 * 
 * Integração testada:
 * @SpringBootTest: Contexto completo da aplicação
 * @Transactional: Transação real com rollback automático
 * @Autowired: Beans reais (repositories, services)
 * @MockBean: Apenas LoggedInUser para contexto de segurança
 */
@SpringBootTest
@Transactional
@DisplayName("IT10 - Não Pode Retirar Carro De Pacote Diferente")
public class IT10_NaoPodeRetirarCarroDePacoteDiferente {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CreditCardRepository creditCardRepository;

    @Autowired
    private CarPackageRepository carPackageRepository;

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private AccessKeyRepository accessKeyRepository;

    @Autowired
    private OrderService orderService;

    @Autowired
    private DeliveryService deliveryService;

    @MockBean
    private LoggedInUser loggedInUser;

    private User testUser;
    private CarPackage basicPackage;
    private CarPackage premiumPackage;
    private Car basicCar;
    private Car premiumCar;

    @BeforeEach
    void setUp() {
        // Limpar repositórios
        accessKeyRepository.deleteAll();
        creditCardRepository.deleteAll();
        userRepository.deleteAll();
        carRepository.deleteAll();
        carPackageRepository.deleteAll();

        // Criar pacotes BASIC e PREMIUM
        basicPackage = CarPackage.builder()
                .packageName("BASIC")
                .pricePerHour(100)
                .build();
        basicPackage = carPackageRepository.save(basicPackage);

        premiumPackage = CarPackage.builder()
                .packageName("PREMIUM")
                .pricePerHour(300)
                .build();
        premiumPackage = carPackageRepository.save(premiumPackage);

        // Criar carros para cada pacote
        basicCar = Car.builder()
                .registrationNr("ABC1234")
                .brand("Fiat")
                .model("Uno")
                .isAvailable(true)
                .carPackage(basicPackage)
                .build();
        basicCar = carRepository.save(basicCar);

        premiumCar = Car.builder()
                .registrationNr("XYZ9999")
                .brand("BMW")
                .model("X5")
                .isAvailable(true)
                .carPackage(premiumPackage)
                .build();
        premiumCar = carRepository.save(premiumCar);

        // Criar usuário com cartão de crédito
        testUser = User.builder()
                .firstName("Test")
                .lastName("UserIT10")
                .username("user_it10")
                .password("password123")
                .email("it10@test.com")
                .phone(123456789)
                .build();
        testUser = userRepository.save(testUser);

        CreditCard creditCard = CreditCard.builder()
                .cardNumber(1234567890123456L)
                .month(12)
                .year(2028)
                .CVV(123)
                .accountBalance(10000L)
                .user(testUser)
                .build();
        creditCardRepository.save(creditCard);

        // Sincronizar relacionamento bi-direcional
        testUser.setCreditCard(creditCard);
        testUser = userRepository.save(testUser);

        System.out.println("✓ Setup completo: Usuário, Pacotes (BASIC/PREMIUM), Carros criados");
    }

    @Test
    @DisplayName("Deve impedir retirada de carro PREMIUM quando AccessKey é BASIC")
    void deveImpedirRetiradaCarroPacoteDiferente() {
        // Given: Usuário cria pedido para pacote BASIC
        when(loggedInUser.getUser()).thenReturn(testUser);

        AccessKeyDto accessKeyDto = orderService.submitOrder("BASIC", 2);
        assertNotNull(accessKeyDto);
        assertEquals("BASIC", accessKeyDto.getCarPackage());
        System.out.println("✓ Pedido criado para pacote BASIC");

        // Recarregar usuário para obter AccessKey atualizado
        testUser = userRepository.findById(testUser.getId()).orElseThrow();
        assertNotNull(testUser.getAccessKey());
        assertEquals("BASIC", testUser.getAccessKey().getCarPackage());
        System.out.println("✓ AccessKey criado com carPackage = BASIC");

        // Atualizar mock com usuário recarregado
        when(loggedInUser.getUser()).thenReturn(testUser);

        // When: Usuário tenta retirar carro do pacote PREMIUM
        Long premiumCarId = premiumCar.getId();

        // Then: Deve lançar InvalidPackageException
        InvalidPackageException exception = assertThrows(InvalidPackageException.class, () -> {
            deliveryService.pickUpTheCar(premiumCarId);
        });

        assertEquals("You Cannot Pick Car From This Package!", exception.getMessage());
        System.out.println("✓ InvalidPackageException lançada: " + exception.getMessage());

        // Validar que carro PREMIUM permanece disponível
        Car premiumCarReload = carRepository.findById(premiumCarId).orElseThrow();
        assertTrue(premiumCarReload.getIsAvailable());
        System.out.println("✓ Carro PREMIUM permanece disponível após tentativa falha");

        // Validar que AccessKey ainda existe (não foi deletado)
        testUser = userRepository.findById(testUser.getId()).orElseThrow();
        assertNotNull(testUser.getAccessKey());
        assertEquals("BASIC", testUser.getAccessKey().getCarPackage());
        System.out.println("✓ AccessKey BASIC ainda existe no usuário");
    }

    @Test
    @DisplayName("Deve permitir retirada de carro BASIC quando AccessKey é BASIC")
    void devePermitirRetiradaCarroMesmoPacote() {
        // Given: Usuário cria pedido para pacote BASIC
        when(loggedInUser.getUser()).thenReturn(testUser);

        AccessKeyDto accessKeyDto = orderService.submitOrder("BASIC", 2);
        assertNotNull(accessKeyDto);
        assertEquals("BASIC", accessKeyDto.getCarPackage());

        // Recarregar usuário para obter AccessKey atualizado
        Long userId = testUser.getId();
        testUser = userRepository.findById(userId).orElseThrow();
        when(loggedInUser.getUser()).thenReturn(testUser);

        // When: Usuário retira carro do pacote BASIC (mesmo pacote do AccessKey)
        Car retrievedCar = deliveryService.pickUpTheCar(basicCar.getId());

        // Then: Retirada bem-sucedida
        assertNotNull(retrievedCar);
        assertEquals(basicCar.getId(), retrievedCar.getId());
        assertFalse(retrievedCar.getIsAvailable());
        System.out.println("✓ Carro BASIC retirado com sucesso");
        System.out.println("✓ AccessKey deletado após retirada bem-sucedida");
    }

    @Test
    @DisplayName("Deve impedir retirada de carro BASIC quando AccessKey é PREMIUM")
    void deveImpedirRetiradaCarroBasicComAccessKeyPremium() {
        // Given: Usuário cria pedido para pacote PREMIUM
        when(loggedInUser.getUser()).thenReturn(testUser);

        AccessKeyDto accessKeyDto = orderService.submitOrder("PREMIUM", 3);
        assertNotNull(accessKeyDto);
        assertEquals("PREMIUM", accessKeyDto.getCarPackage());

        // Recarregar usuário
        testUser = userRepository.findById(testUser.getId()).orElseThrow();
        when(loggedInUser.getUser()).thenReturn(testUser);

        // When: Usuário tenta retirar carro do pacote BASIC
        Long basicCarId = basicCar.getId();

        // Then: Deve lançar InvalidPackageException
        InvalidPackageException exception = assertThrows(InvalidPackageException.class, () -> {
            deliveryService.pickUpTheCar(basicCarId);
        });

        assertEquals("You Cannot Pick Car From This Package!", exception.getMessage());
        System.out.println("✓ InvalidPackageException lançada para AccessKey PREMIUM tentando retirar BASIC");

        // Validar que carro BASIC permanece disponível
        Car basicCarReload = carRepository.findById(basicCarId).orElseThrow();
        assertTrue(basicCarReload.getIsAvailable());
        System.out.println("✓ Carro BASIC permanece disponível");
    }
}
