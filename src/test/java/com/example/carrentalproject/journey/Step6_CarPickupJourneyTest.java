package com.example.carrentalproject.journey;

import com.example.carrentalproject.constant.FuelType;
import com.example.carrentalproject.constant.GearBoxType;
import com.example.carrentalproject.domain.AccessKey;
import com.example.carrentalproject.domain.Car;
import com.example.carrentalproject.domain.CarPackage;
import com.example.carrentalproject.domain.CarParameters;
import com.example.carrentalproject.domain.PlacedOrder;
import com.example.carrentalproject.domain.User;
import com.example.carrentalproject.exception.InvalidPackageException;
import com.example.carrentalproject.exception.NoAccessKeyException;
import com.example.carrentalproject.exception.UnavailableCarException;
import com.example.carrentalproject.repository.AccessKeyRepository;
import com.example.carrentalproject.repository.CarRepository;
import com.example.carrentalproject.repository.OrderRepository;
import com.example.carrentalproject.security.LoggedInUser;
import com.example.carrentalproject.service.DeliveryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes de Jornada - Etapa 6: Usuária retira o carro
 * POST /delivery?carId={id}
 * 
 * Técnicas aplicadas:
 * - Particionamento de equivalência
 * - Análise de valores limite
 * - Testes de propriedade
 * - Uso de dublês (Mocks)
 * - Testes de cobertura essencial
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Journey Step 6 - Car Pickup/Delivery Tests")
class Step6_CarPickupJourneyTest {

    @Mock
    private CarRepository carRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private AccessKeyRepository accessKeyRepository;

    @Mock
    private LoggedInUser loggedInUser;

    @InjectMocks
    private DeliveryService deliveryService;

    private User validUser;
    private AccessKey validAccessKey;
    private CarPackage economyPackage;
    private CarPackage luxuryPackage;
    private Car availableEconomyCar;
    private Car availableLuxuryCar;
    private CarParameters carParameters;

    @BeforeEach
    void setUp() {
        // Setup de pacotes
        economyPackage = CarPackage.builder()
                .id(1L)
                .packageName("Economy")
                .pricePerHour(50)
                .build();

        luxuryPackage = CarPackage.builder()
                .id(2L)
                .packageName("Luxury")
                .pricePerHour(300)
                .build();

        // Setup de parâmetros do carro
        carParameters = CarParameters.builder()
                .id(1L)
                .fuelType(FuelType.PETROL)
                .gearBoxType(GearBoxType.MANUAL)
                .numberOfDoors(4)
                .numberOfSeats(5)
                .isAirConditioningAvailable(true)
                .build();

        // Setup de carros
        availableEconomyCar = Car.builder()
                .id(1L)
                .registrationNr("ABC1234")
                .brand("Toyota")
                .model("Corolla")
                .isAvailable(true)
                .carPackage(economyPackage)
                .carParameters(carParameters)
                .build();

        availableLuxuryCar = Car.builder()
                .id(2L)
                .registrationNr("XYZ9876")
                .brand("BMW")
                .model("X5")
                .isAvailable(true)
                .carPackage(luxuryPackage)
                .carParameters(carParameters)
                .build();

        // Setup de AccessKey
        validAccessKey = AccessKey.builder()
                .id(1L)
                .carPackage("Economy")
                .hours(5)
                .build();

        // Setup de usuário
        validUser = User.builder()
                .id(1L)
                .firstName("Maria")
                .lastName("Silva")
                .username("maria.silva")
                .accessKey(validAccessKey)
                .build();

        validAccessKey.setUser(validUser);
    }

    // ==================== PARTICIONAMENTO: Casos Válidos ====================

    @Test
    @DisplayName("TC1: Retirar carro com sucesso - partição válida completa")
    void shouldPickUpCar_Successfully() {
        // Arrange
        when(carRepository.findById(1L)).thenReturn(Optional.of(availableEconomyCar));
        when(loggedInUser.getUser()).thenReturn(validUser);
        doNothing().when(accessKeyRepository).delete(validAccessKey);
        when(orderRepository.save(any(PlacedOrder.class))).thenReturn(new PlacedOrder());

        // Act
        Car result = deliveryService.pickUpTheCar(1L);

        // Assert
        assertNotNull(result);
        assertEquals(availableEconomyCar, result);
        assertFalse(result.getIsAvailable()); // Carro deve ficar indisponível
        verify(accessKeyRepository, times(1)).delete(validAccessKey);
        verify(orderRepository, times(1)).save(any(PlacedOrder.class));
    }

    @Test
    @DisplayName("TC2: Retirar carro do pacote Luxury - partição válida de pacote premium")
    void shouldPickUpCar_FromLuxuryPackage() {
        // Arrange
        validAccessKey.setCarPackage("Luxury");
        
        when(carRepository.findById(2L)).thenReturn(Optional.of(availableLuxuryCar));
        when(loggedInUser.getUser()).thenReturn(validUser);
        doNothing().when(accessKeyRepository).delete(validAccessKey);
        when(orderRepository.save(any(PlacedOrder.class))).thenReturn(new PlacedOrder());

        // Act
        Car result = deliveryService.pickUpTheCar(2L);

        // Assert
        assertEquals(availableLuxuryCar, result);
        assertFalse(result.getIsAvailable());
    }

    @Test
    @DisplayName("TC3: Retirar carro com 1 hora de aluguel - valor limite inferior")
    void shouldPickUpCar_WithMinimumRentalHours() {
        // Arrange - AccessKey com 1 hora (mínimo)
        validAccessKey.setHours(1);

        when(carRepository.findById(1L)).thenReturn(Optional.of(availableEconomyCar));
        when(loggedInUser.getUser()).thenReturn(validUser);
        doNothing().when(accessKeyRepository).delete(validAccessKey);
        when(orderRepository.save(any(PlacedOrder.class))).thenReturn(new PlacedOrder());

        // Act
        Car result = deliveryService.pickUpTheCar(1L);

        // Assert
        assertNotNull(result);
        assertFalse(result.getIsAvailable());
    }

    @Test
    @DisplayName("TC4: Retirar carro com 24 horas de aluguel - valor limite superior")
    void shouldPickUpCar_WithMaximumRentalHours() {
        // Arrange - AccessKey com 24 horas
        validAccessKey.setHours(24);

        when(carRepository.findById(1L)).thenReturn(Optional.of(availableEconomyCar));
        when(loggedInUser.getUser()).thenReturn(validUser);
        doNothing().when(accessKeyRepository).delete(validAccessKey);
        when(orderRepository.save(any(PlacedOrder.class))).thenReturn(new PlacedOrder());

        // Act
        Car result = deliveryService.pickUpTheCar(1L);

        // Assert
        assertNotNull(result);
        assertEquals(24, validAccessKey.getHours());
    }

    @Test
    @DisplayName("TC5: Retirar primeiro carro disponível da lista - partição de seleção")
    void shouldPickUpCar_FirstAvailableInList() {
        // Arrange - Simula retirada do primeiro carro disponível
        when(carRepository.findById(1L)).thenReturn(Optional.of(availableEconomyCar));
        when(loggedInUser.getUser()).thenReturn(validUser);
        doNothing().when(accessKeyRepository).delete(validAccessKey);
        when(orderRepository.save(any(PlacedOrder.class))).thenReturn(new PlacedOrder());

        // Act
        Car result = deliveryService.pickUpTheCar(1L);

        // Assert
        assertEquals(1L, result.getId());
        assertEquals("ABC1234", result.getRegistrationNr());
    }

    // ==================== PARTICIONAMENTO: Casos Inválidos ====================

    @Test
    @DisplayName("TC6: Falha ao retirar carro inexistente - partição inválida")
    void shouldThrowException_WhenCarDoesNotExist() {
        // Arrange - Carro não existe
        Long nonExistentCarId = 999L;
        when(carRepository.findById(nonExistentCarId)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> deliveryService.pickUpTheCar(nonExistentCarId));

        assertEquals("Car With This ID Does Not Exists!", exception.getMessage());
        verify(accessKeyRepository, never()).delete(any(AccessKey.class));
        verify(orderRepository, never()).save(any(PlacedOrder.class));
    }

    @Test
    @DisplayName("TC7: Falha ao retirar carro sem AccessKey - partição inválida")
    void shouldThrowException_WhenUserHasNoAccessKey() {
        // Arrange - Usuário sem AccessKey
        validUser.setAccessKey(null);

        when(carRepository.findById(1L)).thenReturn(Optional.of(availableEconomyCar));
        when(loggedInUser.getUser()).thenReturn(validUser);

        // Act & Assert
        NoAccessKeyException exception = assertThrows(NoAccessKeyException.class,
                () -> deliveryService.pickUpTheCar(1L));

        assertEquals("You Do Not Have An Access Key!", exception.getMessage());
        verify(accessKeyRepository, never()).delete(any(AccessKey.class));
        verify(orderRepository, never()).save(any(PlacedOrder.class));
    }

    @Test
    @DisplayName("TC8: Falha ao retirar carro de pacote diferente - partição inválida de pacote")
    void shouldThrowException_WhenPackageMismatch() {
        // Arrange - AccessKey para Economy, mas tenta pegar carro Luxury
        validAccessKey.setCarPackage("Economy");

        when(carRepository.findById(2L)).thenReturn(Optional.of(availableLuxuryCar));
        when(loggedInUser.getUser()).thenReturn(validUser);

        // Act & Assert
        InvalidPackageException exception = assertThrows(InvalidPackageException.class,
                () -> deliveryService.pickUpTheCar(2L));

        assertEquals("You Cannot Pick Car From This Package!", exception.getMessage());
        assertTrue(availableLuxuryCar.getIsAvailable()); // Carro permanece disponível
        verify(accessKeyRepository, never()).delete(any(AccessKey.class));
    }

    @Test
    @DisplayName("TC9: Falha ao retirar carro indisponível - partição inválida de disponibilidade")
    void shouldThrowException_WhenCarIsUnavailable() {
        // Arrange - Carro já alugado
        availableEconomyCar.setIsAvailable(false);

        when(carRepository.findById(1L)).thenReturn(Optional.of(availableEconomyCar));
        when(loggedInUser.getUser()).thenReturn(validUser);

        // Act & Assert
        UnavailableCarException exception = assertThrows(UnavailableCarException.class,
                () -> deliveryService.pickUpTheCar(1L));

        assertEquals("This Car Is Not Available!", exception.getMessage());
        assertFalse(availableEconomyCar.getIsAvailable());
        verify(accessKeyRepository, never()).delete(any(AccessKey.class));
    }

    @Test
    @DisplayName("TC10: Falha com ID de carro inválido (0) - valor limite inferior inválido")
    void shouldThrowException_WhenCarIdIsZero() {
        // Arrange - ID zero
        Long invalidCarId = 0L;
        when(carRepository.findById(invalidCarId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class,
                () -> deliveryService.pickUpTheCar(invalidCarId));
    }

    // ==================== TESTES DE PROPRIEDADE ====================

    @Test
    @DisplayName("TC11: Propriedade - Carro sempre deve ficar indisponível após retirada")
    void shouldAlwaysMarkCarUnavailable_AfterPickup() {
        // Arrange
        assertTrue(availableEconomyCar.getIsAvailable()); // Inicialmente disponível

        when(carRepository.findById(1L)).thenReturn(Optional.of(availableEconomyCar));
        when(loggedInUser.getUser()).thenReturn(validUser);
        doNothing().when(accessKeyRepository).delete(validAccessKey);
        when(orderRepository.save(any(PlacedOrder.class))).thenReturn(new PlacedOrder());

        // Act
        Car result = deliveryService.pickUpTheCar(1L);

        // Assert - Propriedade: carro SEMPRE deve ficar indisponível
        assertFalse(result.getIsAvailable());
        assertFalse(availableEconomyCar.getIsAvailable());
    }

    @Test
    @DisplayName("TC12: Propriedade - AccessKey sempre deve ser deletada após retirada")
    void shouldAlwaysDeleteAccessKey_AfterPickup() {
        // Arrange
        when(carRepository.findById(1L)).thenReturn(Optional.of(availableEconomyCar));
        when(loggedInUser.getUser()).thenReturn(validUser);
        doNothing().when(accessKeyRepository).delete(validAccessKey);
        when(orderRepository.save(any(PlacedOrder.class))).thenReturn(new PlacedOrder());

        // Act
        deliveryService.pickUpTheCar(1L);

        // Assert - Propriedade: AccessKey SEMPRE deve ser deletada
        verify(accessKeyRepository, times(1)).delete(validAccessKey);
        verify(accessKeyRepository, times(1)).delete(any(AccessKey.class));
    }

    @Test
    @DisplayName("TC13: Propriedade - PlacedOrder sempre deve ser criado após retirada")
    void shouldAlwaysCreatePlacedOrder_AfterPickup() {
        // Arrange
        when(carRepository.findById(1L)).thenReturn(Optional.of(availableEconomyCar));
        when(loggedInUser.getUser()).thenReturn(validUser);
        doNothing().when(accessKeyRepository).delete(validAccessKey);
        when(orderRepository.save(any(PlacedOrder.class))).thenAnswer(invocation -> {
            PlacedOrder order = invocation.getArgument(0);
            // Propriedade: ordem deve ter dados corretos
            assertNotNull(order);
            assertEquals(validUser.getId(), order.getUserId());
            assertEquals(availableEconomyCar.getId(), order.getCarId());
            assertEquals(availableEconomyCar.getBrand(), order.getBrand());
            assertEquals(availableEconomyCar.getModel(), order.getModel());
            assertNotNull(order.getStartTime());
            assertNotNull(order.getEndTime());
            return order;
        });

        // Act
        deliveryService.pickUpTheCar(1L);

        // Assert - Propriedade: ordem SEMPRE deve ser salva
        verify(orderRepository, times(1)).save(any(PlacedOrder.class));
    }

    @Test
    @DisplayName("TC14: Propriedade - Tempo de término deve ser start + horas alugadas")
    void shouldCalculateEndTime_CorrectlyBasedOnRentalHours() {
        // Arrange
        Integer rentalHours = 5;
        validAccessKey.setHours(rentalHours);

        when(carRepository.findById(1L)).thenReturn(Optional.of(availableEconomyCar));
        when(loggedInUser.getUser()).thenReturn(validUser);
        doNothing().when(accessKeyRepository).delete(validAccessKey);
        when(orderRepository.save(any(PlacedOrder.class))).thenAnswer(invocation -> {
            PlacedOrder order = invocation.getArgument(0);
            // Propriedade: endTime = startTime + rentalHours
            LocalDateTime start = order.getStartTime();
            LocalDateTime end = order.getEndTime();
            assertNotNull(start);
            assertNotNull(end);
            assertTrue(end.isAfter(start));
            
            // Verificar que a diferença é aproximadamente o número de horas
            long hoursDifference = java.time.Duration.between(start, end).toHours();
            assertEquals(rentalHours.longValue(), hoursDifference);
            return order;
        });

        // Act
        deliveryService.pickUpTheCar(1L);

        // Assert
        verify(orderRepository, times(1)).save(any(PlacedOrder.class));
    }

    @Test
    @DisplayName("TC15: Propriedade - Operação deve ser atômica (todas as mudanças ou nenhuma)")
    void shouldPerformAtomicOperation_WhenPickingUpCar() {
        // Arrange
        when(carRepository.findById(1L)).thenReturn(Optional.of(availableEconomyCar));
        when(loggedInUser.getUser()).thenReturn(validUser);
        doNothing().when(accessKeyRepository).delete(validAccessKey);
        when(orderRepository.save(any(PlacedOrder.class))).thenReturn(new PlacedOrder());

        // Act
        deliveryService.pickUpTheCar(1L);

        // Assert - Propriedade: todas as operações devem ocorrer
        // 1. AccessKey deletada
        verify(accessKeyRepository, times(1)).delete(validAccessKey);
        // 2. Carro marcado como indisponível
        assertFalse(availableEconomyCar.getIsAvailable());
        // 3. Ordem criada
        verify(orderRepository, times(1)).save(any(PlacedOrder.class));

        // Verificar ordem de execução
        var inOrder = inOrder(accessKeyRepository, orderRepository);
        inOrder.verify(accessKeyRepository).delete(validAccessKey);
        inOrder.verify(orderRepository).save(any(PlacedOrder.class));
    }

    // ==================== TESTES ADICIONAIS DE COBERTURA ====================

    @Test
    @DisplayName("TC16: Retirar carro com 10 horas de aluguel - valor médio")
    void shouldPickUpCar_WithMediumRentalHours() {
        // Arrange
        validAccessKey.setHours(10);

        when(carRepository.findById(1L)).thenReturn(Optional.of(availableEconomyCar));
        when(loggedInUser.getUser()).thenReturn(validUser);
        doNothing().when(accessKeyRepository).delete(validAccessKey);
        when(orderRepository.save(any(PlacedOrder.class))).thenReturn(new PlacedOrder());

        // Act
        Car result = deliveryService.pickUpTheCar(1L);

        // Assert
        assertNotNull(result);
        assertEquals(10, validAccessKey.getHours());
        verify(orderRepository, times(1)).save(any(PlacedOrder.class));
    }

    @Test
    @DisplayName("TC17: Retirar carro com 168 horas (uma semana)")
    void shouldPickUpCar_WithOneWeekRental() {
        // Arrange
        validAccessKey.setHours(168); // 7 dias * 24 horas

        when(carRepository.findById(1L)).thenReturn(Optional.of(availableEconomyCar));
        when(loggedInUser.getUser()).thenReturn(validUser);
        doNothing().when(accessKeyRepository).delete(validAccessKey);
        when(orderRepository.save(any(PlacedOrder.class))).thenReturn(new PlacedOrder());

        // Act
        Car result = deliveryService.pickUpTheCar(1L);

        // Assert
        assertEquals(168, validAccessKey.getHours());
        assertFalse(result.getIsAvailable());
    }

    @Test
    @DisplayName("TC18: Retirar carro com ID positivo grande (1000)")
    void shouldPickUpCar_WithLargeCarId() {
        // Arrange
        Car carWithLargeId = Car.builder()
                .id(1000L)
                .registrationNr("LRG1000")
                .brand("Audi")
                .model("A4")
                .isAvailable(true)
                .carPackage(economyPackage)
                .carParameters(carParameters)
                .build();

        when(carRepository.findById(1000L)).thenReturn(Optional.of(carWithLargeId));
        when(loggedInUser.getUser()).thenReturn(validUser);
        doNothing().when(accessKeyRepository).delete(validAccessKey);
        when(orderRepository.save(any(PlacedOrder.class))).thenReturn(new PlacedOrder());

        // Act
        Car result = deliveryService.pickUpTheCar(1000L);

        // Assert
        assertEquals(1000L, result.getId());
        assertFalse(result.getIsAvailable());
    }

    @Test
    @DisplayName("TC19: Retirar carro com diferentes marcas - particionamento por marca")
    void shouldPickUpCar_WithDifferentBrands() {
        // Arrange - Testar múltiplas marcas
        Car hondaCar = Car.builder()
                .id(3L)
                .registrationNr("HON123")
                .brand("Honda")
                .model("Civic")
                .isAvailable(true)
                .carPackage(economyPackage)
                .carParameters(carParameters)
                .build();

        when(carRepository.findById(3L)).thenReturn(Optional.of(hondaCar));
        when(loggedInUser.getUser()).thenReturn(validUser);
        doNothing().when(accessKeyRepository).delete(validAccessKey);
        when(orderRepository.save(any(PlacedOrder.class))).thenReturn(new PlacedOrder());

        // Act
        Car result = deliveryService.pickUpTheCar(3L);

        // Assert
        assertEquals("Honda", result.getBrand());
        assertEquals("Civic", result.getModel());
        assertFalse(result.getIsAvailable());
    }

    @Test
    @DisplayName("TC20: Falha ao retirar carro com ID negativo")
    void shouldThrowException_WhenCarIdIsNegative() {
        // Arrange - ID negativo
        Long negativeId = -1L;
        when(carRepository.findById(negativeId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class,
                () -> deliveryService.pickUpTheCar(negativeId));
        
        verify(accessKeyRepository, never()).delete(any());
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("TC21: Propriedade - Ordem de validações deve ser consistente")
    void shouldValidateInConsistentOrder_ForAllRequests() {
        // Arrange - Múltiplos problemas: carro inexistente + usuário sem AccessKey
        validUser.setAccessKey(null);

        when(carRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert - Deve falhar na primeira validação (carro existe?)
        assertThrows(EntityNotFoundException.class,
                () -> deliveryService.pickUpTheCar(999L));

        // Propriedade: busca do carro é a primeira operação
        verify(carRepository, times(1)).findById(999L);
        verify(loggedInUser, never()).getUser();
    }

    @Test
    @DisplayName("TC22: Propriedade - StartTime e EndTime devem ser consistentes")
    void shouldHaveConsistentStartAndEndTime_InPlacedOrder() {
        // Arrange
        when(carRepository.findById(1L)).thenReturn(Optional.of(availableEconomyCar));
        when(loggedInUser.getUser()).thenReturn(validUser);
        doNothing().when(accessKeyRepository).delete(validAccessKey);
        when(orderRepository.save(any(PlacedOrder.class))).thenAnswer(invocation -> {
            PlacedOrder order = invocation.getArgument(0);
            
            // Propriedade: EndTime deve ser após StartTime
            assertTrue(order.getEndTime().isAfter(order.getStartTime()),
                    "EndTime must be after StartTime");
            
            // Propriedade: diferença deve ser positiva
            long hoursDiff = java.time.Duration.between(
                    order.getStartTime(), order.getEndTime()).toHours();
            assertTrue(hoursDiff > 0, "Hours difference must be positive");
            
            return order;
        });

        // Act
        deliveryService.pickUpTheCar(1L);

        // Assert
        verify(orderRepository, times(1)).save(any(PlacedOrder.class));
    }

    @Test
    @DisplayName("TC23: Propriedade - PlacedOrder deve conter dados corretos do carro")
    void shouldContainCorrectCarData_InPlacedOrder() {
        // Arrange
        when(carRepository.findById(1L)).thenReturn(Optional.of(availableEconomyCar));
        when(loggedInUser.getUser()).thenReturn(validUser);
        doNothing().when(accessKeyRepository).delete(validAccessKey);
        when(orderRepository.save(any(PlacedOrder.class))).thenAnswer(invocation -> {
            PlacedOrder order = invocation.getArgument(0);
            
            // Propriedade: dados do carro devem corresponder
            assertEquals(availableEconomyCar.getId(), order.getCarId());
            assertEquals(availableEconomyCar.getBrand(), order.getBrand());
            assertEquals(availableEconomyCar.getModel(), order.getModel());
            assertEquals(validUser.getId(), order.getUserId());
            
            return order;
        });

        // Act
        deliveryService.pickUpTheCar(1L);

        // Assert
        verify(orderRepository, times(1)).save(any(PlacedOrder.class));
    }

    @Test
    @DisplayName("TC24: Propriedade - Estado não deve mudar em caso de falha por carro indisponível")
    void shouldNotModifyState_WhenCarIsUnavailable() {
        // Arrange
        availableEconomyCar.setIsAvailable(false);
        Boolean initialAvailability = availableEconomyCar.getIsAvailable();
        AccessKey initialAccessKey = validUser.getAccessKey();

        when(carRepository.findById(1L)).thenReturn(Optional.of(availableEconomyCar));
        when(loggedInUser.getUser()).thenReturn(validUser);

        // Act & Assert
        assertThrows(UnavailableCarException.class,
                () -> deliveryService.pickUpTheCar(1L));

        // Propriedade: estado não deve ter mudado
        assertEquals(initialAvailability, availableEconomyCar.getIsAvailable());
        assertSame(initialAccessKey, validUser.getAccessKey());
        verify(accessKeyRepository, never()).delete(any());
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("TC25: Propriedade - Estado não deve mudar em caso de falha por pacote inválido")
    void shouldNotModifyState_WhenPackageMismatch() {
        // Arrange
        validAccessKey.setCarPackage("Economy");
        Boolean initialAvailability = availableLuxuryCar.getIsAvailable();
        AccessKey initialAccessKey = validUser.getAccessKey();

        when(carRepository.findById(2L)).thenReturn(Optional.of(availableLuxuryCar));
        when(loggedInUser.getUser()).thenReturn(validUser);

        // Act & Assert
        assertThrows(InvalidPackageException.class,
                () -> deliveryService.pickUpTheCar(2L));

        // Propriedade: nenhum estado deve ter mudado
        assertEquals(initialAvailability, availableLuxuryCar.getIsAvailable());
        assertTrue(availableLuxuryCar.getIsAvailable());
        assertSame(initialAccessKey, validUser.getAccessKey());
        verify(accessKeyRepository, never()).delete(any());
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("TC26: Múltiplos usuários podem retirar carros diferentes simultaneamente")
    void shouldAllowMultipleUsers_ToPickUpDifferentCars() {
        // Arrange - Usuário 1
        AccessKey accessKey1 = AccessKey.builder()
                .id(1L)
                .carPackage("Economy")
                .hours(5)
                .build();
        User user1 = User.builder()
                .id(1L)
                .username("user1")
                .accessKey(accessKey1)
                .build();
        accessKey1.setUser(user1);

        // Arrange - Usuário 2
        AccessKey accessKey2 = AccessKey.builder()
                .id(2L)
                .carPackage("Economy")
                .hours(3)
                .build();
        User user2 = User.builder()
                .id(2L)
                .username("user2")
                .accessKey(accessKey2)
                .build();
        accessKey2.setUser(user2);

        // Arrange - Dois carros diferentes
        Car car1 = Car.builder()
                .id(1L)
                .registrationNr("CAR001")
                .brand("Toyota")
                .model("Corolla")
                .isAvailable(true)
                .carPackage(economyPackage)
                .build();

        Car car2 = Car.builder()
                .id(2L)
                .registrationNr("CAR002")
                .brand("Honda")
                .model("Civic")
                .isAvailable(true)
                .carPackage(economyPackage)
                .build();

        when(carRepository.findById(1L)).thenReturn(Optional.of(car1));
        when(carRepository.findById(2L)).thenReturn(Optional.of(car2));
        doNothing().when(accessKeyRepository).delete(any());
        when(orderRepository.save(any(PlacedOrder.class))).thenReturn(new PlacedOrder());

        // Act - User 1 retira car1
        when(loggedInUser.getUser()).thenReturn(user1);
        Car result1 = deliveryService.pickUpTheCar(1L);

        // Act - User 2 retira car2
        when(loggedInUser.getUser()).thenReturn(user2);
        Car result2 = deliveryService.pickUpTheCar(2L);

        // Assert - Propriedade: operações são independentes
        assertFalse(result1.getIsAvailable());
        assertFalse(result2.getIsAvailable());
        assertEquals(1L, result1.getId());
        assertEquals(2L, result2.getId());
        verify(accessKeyRepository, times(2)).delete(any());
        verify(orderRepository, times(2)).save(any());
    }

    @Test
    @DisplayName("TC27: Retirar carro com diferentes parâmetros de configuração")
    void shouldPickUpCar_WithDifferentCarParameters() {
        // Arrange - Carro com parâmetros diferentes
        CarParameters automaticParams = CarParameters.builder()
                .id(2L)
                .fuelType(FuelType.DIESEL)
                .gearBoxType(GearBoxType.AUTOMATIC)
                .numberOfDoors(2)
                .numberOfSeats(4)
                .isAirConditioningAvailable(false)
                .build();

        Car sportsCar = Car.builder()
                .id(5L)
                .registrationNr("SPT999")
                .brand("Porsche")
                .model("911")
                .isAvailable(true)
                .carPackage(economyPackage)
                .carParameters(automaticParams)
                .build();

        when(carRepository.findById(5L)).thenReturn(Optional.of(sportsCar));
        when(loggedInUser.getUser()).thenReturn(validUser);
        doNothing().when(accessKeyRepository).delete(validAccessKey);
        when(orderRepository.save(any(PlacedOrder.class))).thenReturn(new PlacedOrder());

        // Act
        Car result = deliveryService.pickUpTheCar(5L);

        // Assert
        assertNotNull(result.getCarParameters());
        assertEquals(GearBoxType.AUTOMATIC, result.getCarParameters().getGearBoxType());
        assertEquals(FuelType.DIESEL, result.getCarParameters().getFuelType());
        assertFalse(result.getIsAvailable());
    }

    @Test
    @DisplayName("TC28: Propriedade - Carro retornado deve ser o mesmo carro requisitado")
    void shouldReturnSameCar_AsRequested() {
        // Arrange
        when(carRepository.findById(1L)).thenReturn(Optional.of(availableEconomyCar));
        when(loggedInUser.getUser()).thenReturn(validUser);
        doNothing().when(accessKeyRepository).delete(validAccessKey);
        when(orderRepository.save(any(PlacedOrder.class))).thenReturn(new PlacedOrder());

        // Act
        Car result = deliveryService.pickUpTheCar(1L);

        // Assert - Propriedade: carro retornado é o mesmo que foi buscado
        assertSame(availableEconomyCar, result);
        assertEquals(1L, result.getId());
        assertEquals("ABC1234", result.getRegistrationNr());
    }

    @Test
    @DisplayName("TC29: Propriedade - Duração de aluguel deve corresponder às horas em AccessKey")
    void shouldMatchRentalDuration_WithAccessKeyHours() {
        // Arrange
        Integer rentalHours = 12;
        validAccessKey.setHours(rentalHours);

        when(carRepository.findById(1L)).thenReturn(Optional.of(availableEconomyCar));
        when(loggedInUser.getUser()).thenReturn(validUser);
        doNothing().when(accessKeyRepository).delete(validAccessKey);
        when(orderRepository.save(any(PlacedOrder.class))).thenAnswer(invocation -> {
            PlacedOrder order = invocation.getArgument(0);
            
            // Propriedade: duração deve corresponder às horas da AccessKey
            long actualHours = java.time.Duration.between(
                    order.getStartTime(), order.getEndTime()).toHours();
            assertEquals(rentalHours.longValue(), actualHours,
                    "Rental duration must match AccessKey hours");
            
            return order;
        });

        // Act
        deliveryService.pickUpTheCar(1L);

        // Assert
        verify(orderRepository, times(1)).save(any(PlacedOrder.class));
    }

    @Test
    @DisplayName("TC30: Falha não deve criar PlacedOrder quando usuário não tem AccessKey")
    void shouldNotCreateOrder_WhenUserHasNoAccessKey() {
        // Arrange
        validUser.setAccessKey(null);

        when(carRepository.findById(1L)).thenReturn(Optional.of(availableEconomyCar));
        when(loggedInUser.getUser()).thenReturn(validUser);

        // Act & Assert
        assertThrows(NoAccessKeyException.class,
                () -> deliveryService.pickUpTheCar(1L));

        // Propriedade: nenhuma ordem deve ser criada
        verify(orderRepository, never()).save(any());
        verify(accessKeyRepository, never()).delete(any());
    }

    @Test
    @DisplayName("TC31: Retirar carro com nome de pacote exato (case-sensitive)")
    void shouldMatchPackageName_ExactlyCaseSensitive() {
        // Arrange - Nome do pacote deve ser exato
        validAccessKey.setCarPackage("Economy"); // Com 'E' maiúsculo

        when(carRepository.findById(1L)).thenReturn(Optional.of(availableEconomyCar));
        when(loggedInUser.getUser()).thenReturn(validUser);
        doNothing().when(accessKeyRepository).delete(validAccessKey);
        when(orderRepository.save(any(PlacedOrder.class))).thenReturn(new PlacedOrder());

        // Act
        Car result = deliveryService.pickUpTheCar(1L);

        // Assert
        assertEquals("Economy", result.getCarPackage().getPackageName());
        assertFalse(result.getIsAvailable());
    }

    @Test
    @DisplayName("TC32: Propriedade - Verificação de disponibilidade deve ocorrer antes de modificar estado")
    void shouldCheckAvailability_BeforeModifyingState() {
        // Arrange
        availableEconomyCar.setIsAvailable(false);

        when(carRepository.findById(1L)).thenReturn(Optional.of(availableEconomyCar));
        when(loggedInUser.getUser()).thenReturn(validUser);

        // Act & Assert
        assertThrows(UnavailableCarException.class,
                () -> deliveryService.pickUpTheCar(1L));

        // Propriedade: AccessKey não deve ser deletada se carro indisponível
        verify(accessKeyRepository, never()).delete(any());
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("TC33: Propriedade - PlacedOrder deve ter todos os campos obrigatórios preenchidos")
    void shouldHaveAllMandatoryFields_InPlacedOrder() {
        // Arrange
        when(carRepository.findById(1L)).thenReturn(Optional.of(availableEconomyCar));
        when(loggedInUser.getUser()).thenReturn(validUser);
        doNothing().when(accessKeyRepository).delete(validAccessKey);
        when(orderRepository.save(any(PlacedOrder.class))).thenAnswer(invocation -> {
            PlacedOrder order = invocation.getArgument(0);
            
            // Propriedade: todos os campos obrigatórios devem estar preenchidos
            assertNotNull(order.getUserId(), "UserId must not be null");
            assertNotNull(order.getCarId(), "CarId must not be null");
            assertNotNull(order.getBrand(), "Brand must not be null");
            assertNotNull(order.getModel(), "Model must not be null");
            assertNotNull(order.getStartTime(), "StartTime must not be null");
            assertNotNull(order.getEndTime(), "EndTime must not be null");
            
            return order;
        });

        // Act
        deliveryService.pickUpTheCar(1L);

        // Assert
        verify(orderRepository, times(1)).save(any(PlacedOrder.class));
    }

    @Test
    @DisplayName("TC34: Propriedade - Operação de retirada é irreversível (sem rollback manual)")
    void shouldBeIrreversible_AfterSuccessfulPickup() {
        // Arrange
        assertTrue(availableEconomyCar.getIsAvailable());

        when(carRepository.findById(1L)).thenReturn(Optional.of(availableEconomyCar));
        when(loggedInUser.getUser()).thenReturn(validUser);
        doNothing().when(accessKeyRepository).delete(validAccessKey);
        when(orderRepository.save(any(PlacedOrder.class))).thenReturn(new PlacedOrder());

        // Act
        deliveryService.pickUpTheCar(1L);

        // Assert - Propriedade: após retirada, carro permanece indisponível
        assertFalse(availableEconomyCar.getIsAvailable());
        
        // AccessKey foi deletada, não pode ser recuperada
        verify(accessKeyRepository, times(1)).delete(validAccessKey);
        
        // Ordem foi criada
        verify(orderRepository, times(1)).save(any(PlacedOrder.class));
    }
}
