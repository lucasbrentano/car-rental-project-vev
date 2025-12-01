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
}
