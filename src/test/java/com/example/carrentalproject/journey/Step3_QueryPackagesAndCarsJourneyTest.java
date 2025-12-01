package com.example.carrentalproject.journey;

import com.example.carrentalproject.constant.FuelType;
import com.example.carrentalproject.constant.GearBoxType;
import com.example.carrentalproject.domain.Car;
import com.example.carrentalproject.domain.CarPackage;
import com.example.carrentalproject.domain.CarParameters;
import com.example.carrentalproject.repository.CarPackageRepository;
import com.example.carrentalproject.repository.CarParametersRepository;
import com.example.carrentalproject.repository.CarRepository;
import com.example.carrentalproject.service.CarService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes de Jornada - Etapa 3: Usuária consulta pacotes e carros disponíveis
 * GET /cars/packages
 * GET /cars/available
 * 
 * Técnicas aplicadas:
 * - Particionamento de equivalência
 * - Análise de valores limite
 * - Testes de propriedade
 * - Uso de dublês (Mocks)
 * - Testes de cobertura essencial
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Journey Step 3 - Query Packages and Available Cars Tests")
class Step3_QueryPackagesAndCarsJourneyTest {

    @Mock
    private CarRepository carRepository;

    @Mock
    private CarPackageRepository carPackageRepository;

    @Mock
    private CarParametersRepository carParametersRepository;

    @InjectMocks
    private CarService carService;

    private List<CarPackage> packageList;
    private List<Car> carList;

    @BeforeEach
    void setUp() {
        // Setup de pacotes
        CarPackage economyPackage = CarPackage.builder()
                .id(1L)
                .packageName("Economy")
                .pricePerHour(50)
                .cars(new ArrayList<>())
                .build();

        CarPackage standardPackage = CarPackage.builder()
                .id(2L)
                .packageName("Standard")
                .pricePerHour(100)
                .cars(new ArrayList<>())
                .build();

        CarPackage luxuryPackage = CarPackage.builder()
                .id(3L)
                .packageName("Luxury")
                .pricePerHour(300)
                .cars(new ArrayList<>())
                .build();

        packageList = Arrays.asList(economyPackage, standardPackage, luxuryPackage);

        // Setup de carros
        CarParameters economyParams = CarParameters.builder()
                .id(1L)
                .fuelType(FuelType.PETROL)
                .gearBoxType(GearBoxType.MANUAL)
                .numberOfDoors(4)
                .numberOfSeats(5)
                .isAirConditioningAvailable(false)
                .build();

        CarParameters luxuryParams = CarParameters.builder()
                .id(2L)
                .fuelType(FuelType.DIESEL)
                .gearBoxType(GearBoxType.AUTOMATIC)
                .numberOfDoors(4)
                .numberOfSeats(5)
                .isAirConditioningAvailable(true)
                .build();

        Car availableCar1 = Car.builder()
                .id(1L)
                .registrationNr("ABC1234")
                .brand("Toyota")
                .model("Corolla")
                .isAvailable(true)
                .carPackage(economyPackage)
                .carParameters(economyParams)
                .build();

        Car availableCar2 = Car.builder()
                .id(2L)
                .registrationNr("DEF5678")
                .brand("Honda")
                .model("Civic")
                .isAvailable(true)
                .carPackage(standardPackage)
                .carParameters(economyParams)
                .build();

        Car unavailableCar = Car.builder()
                .id(3L)
                .registrationNr("GHI9012")
                .brand("BMW")
                .model("X5")
                .isAvailable(false)
                .carPackage(luxuryPackage)
                .carParameters(luxuryParams)
                .build();

        Car availableCar3 = Car.builder()
                .id(4L)
                .registrationNr("JKL3456")
                .brand("Mercedes")
                .model("C-Class")
                .isAvailable(true)
                .carPackage(luxuryPackage)
                .carParameters(luxuryParams)
                .build();

        carList = Arrays.asList(availableCar1, availableCar2, unavailableCar, availableCar3);
    }

    // ==================== TESTES DE GET /cars/packages ====================

    @Test
    @DisplayName("TC1: Consultar todos os pacotes - partição válida com dados")
    void shouldReturnAllPackages_WhenPackagesExist() {
        // Arrange
        when(carPackageRepository.findAll()).thenReturn(packageList);

        // Act
        List<CarPackage> result = carService.getCarPackages();

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        verify(carPackageRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("TC2: Consultar pacotes quando não há nenhum - partição vazia")
    void shouldReturnEmptyList_WhenNoPackagesExist() {
        // Arrange - Lista vazia
        when(carPackageRepository.findAll()).thenReturn(new ArrayList<>());

        // Act
        List<CarPackage> result = carService.getCarPackages();

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("TC3: Consultar pacotes com apenas 1 pacote - valor limite inferior")
    void shouldReturnSinglePackage_WhenOnlyOneExists() {
        // Arrange - Um único pacote
        CarPackage singlePackage = CarPackage.builder()
                .id(1L)
                .packageName("Basic")
                .pricePerHour(30)
                .build();

        when(carPackageRepository.findAll()).thenReturn(Arrays.asList(singlePackage));

        // Act
        List<CarPackage> result = carService.getCarPackages();

        // Assert
        assertEquals(1, result.size());
        assertEquals("Basic", result.get(0).getPackageName());
    }

    @Test
    @DisplayName("TC4: Consultar pacotes com diferentes preços - particionamento por faixa de preço")
    void shouldReturnPackages_WithDifferentPriceRanges() {
        // Arrange - Pacotes com preços variados
        CarPackage cheapPackage = CarPackage.builder()
                .id(1L)
                .packageName("Budget")
                .pricePerHour(20) // Faixa baixa
                .build();

        CarPackage midPackage = CarPackage.builder()
                .id(2L)
                .packageName("Standard")
                .pricePerHour(100) // Faixa média
                .build();

        CarPackage expensivePackage = CarPackage.builder()
                .id(3L)
                .packageName("Premium")
                .pricePerHour(500) // Faixa alta
                .build();

        List<CarPackage> packages = Arrays.asList(cheapPackage, midPackage, expensivePackage);
        when(carPackageRepository.findAll()).thenReturn(packages);

        // Act
        List<CarPackage> result = carService.getCarPackages();

        // Assert
        assertEquals(3, result.size());
        assertTrue(result.stream().anyMatch(p -> p.getPricePerHour() < 50));
        assertTrue(result.stream().anyMatch(p -> p.getPricePerHour() >= 50 && p.getPricePerHour() < 200));
        assertTrue(result.stream().anyMatch(p -> p.getPricePerHour() >= 200));
    }

    @Test
    @DisplayName("TC5: Propriedade - Pacotes devem ter preço por hora positivo")
    void shouldHavePositivePricePerHour_ForAllPackages() {
        // Arrange
        when(carPackageRepository.findAll()).thenReturn(packageList);

        // Act
        List<CarPackage> result = carService.getCarPackages();

        // Assert - Propriedade: todos os preços devem ser positivos
        assertTrue(result.stream().allMatch(p -> p.getPricePerHour() > 0),
                "All packages should have positive price per hour");
        assertFalse(result.stream().anyMatch(p -> p.getPricePerHour() <= 0),
                "No package should have zero or negative price");
    }

    // ==================== TESTES DE GET /cars/available ====================

    @Test
    @DisplayName("TC6: Consultar carros disponíveis - partição válida com carros disponíveis")
    void shouldReturnAvailableCars_WhenAvailableCarsExist() {
        // Arrange
        List<Car> availableCars = carList.stream()
                .filter(Car::getIsAvailable)
                .collect(Collectors.toList());

        when(carRepository.findAvailableCars(any(PageRequest.class)))
                .thenReturn(availableCars);

        // Act
        List<Car> result = carService.getAvailableCars(1, Sort.Direction.ASC);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size()); // 3 carros disponíveis
        assertTrue(result.stream().allMatch(Car::getIsAvailable));
        verify(carRepository, times(1)).findAvailableCars(any(PageRequest.class));
    }

    @Test
    @DisplayName("TC7: Consultar carros disponíveis quando nenhum está disponível - partição vazia")
    void shouldReturnEmptyList_WhenNoAvailableCars() {
        // Arrange - Nenhum carro disponível
        when(carRepository.findAvailableCars(any(PageRequest.class)))
                .thenReturn(new ArrayList<>());

        // Act
        List<Car> result = carService.getAvailableCars(1, Sort.Direction.ASC);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("TC8: Consultar carros disponíveis com apenas 1 carro - valor limite inferior")
    void shouldReturnSingleCar_WhenOnlyOneAvailable() {
        // Arrange - Apenas um carro disponível
        Car singleCar = Car.builder()
                .id(1L)
                .registrationNr("XYZ9876")
                .brand("Ford")
                .model("Focus")
                .isAvailable(true)
                .build();

        when(carRepository.findAvailableCars(any(PageRequest.class)))
                .thenReturn(Arrays.asList(singleCar));

        // Act
        List<Car> result = carService.getAvailableCars(1, Sort.Direction.ASC);

        // Assert
        assertEquals(1, result.size());
        assertEquals("XYZ9876", result.get(0).getRegistrationNr());
    }

    @Test
    @DisplayName("TC9: Consultar carros com paginação - primeira página")
    void shouldReturnFirstPage_WhenQueryingAvailableCars() {
        // Arrange
        List<Car> availableCars = carList.stream()
                .filter(Car::getIsAvailable)
                .collect(Collectors.toList());

        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id"));
        when(carRepository.findAvailableCars(pageRequest)).thenReturn(availableCars);

        // Act
        List<Car> result = carService.getAvailableCars(1, Sort.Direction.ASC);

        // Assert
        assertEquals(3, result.size());
        verify(carRepository).findAvailableCars(pageRequest);
    }

    @Test
    @DisplayName("TC10: Consultar carros ordenados crescente - partição de ordenação ASC")
    void shouldReturnCarsInAscendingOrder_WhenSortDirectionIsASC() {
        // Arrange
        List<Car> availableCars = carList.stream()
                .filter(Car::getIsAvailable)
                .collect(Collectors.toList());

        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id"));
        when(carRepository.findAvailableCars(pageRequest)).thenReturn(availableCars);

        // Act
        List<Car> result = carService.getAvailableCars(1, Sort.Direction.ASC);

        // Assert
        assertNotNull(result);
        verify(carRepository).findAvailableCars(
                argThat(pr -> pr.getSort().getOrderFor("id").getDirection() == Sort.Direction.ASC)
        );
    }

    @Test
    @DisplayName("TC11: Consultar carros ordenados decrescente - partição de ordenação DESC")
    void shouldReturnCarsInDescendingOrder_WhenSortDirectionIsDESC() {
        // Arrange
        List<Car> availableCars = carList.stream()
                .filter(Car::getIsAvailable)
                .collect(Collectors.toList());

        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "id"));
        when(carRepository.findAvailableCars(pageRequest)).thenReturn(availableCars);

        // Act
        List<Car> result = carService.getAvailableCars(1, Sort.Direction.DESC);

        // Assert
        assertNotNull(result);
        verify(carRepository).findAvailableCars(
                argThat(pr -> pr.getSort().getOrderFor("id").getDirection() == Sort.Direction.DESC)
        );
    }

    @Test
    @DisplayName("TC12: Consultar carros com diferentes pacotes - particionamento por tipo de pacote")
    void shouldReturnCars_WithDifferentPackageTypes() {
        // Arrange
        List<Car> availableCars = carList.stream()
                .filter(Car::getIsAvailable)
                .collect(Collectors.toList());

        when(carRepository.findAvailableCars(any(PageRequest.class)))
                .thenReturn(availableCars);

        // Act
        List<Car> result = carService.getAvailableCars(1, Sort.Direction.ASC);

        // Assert
        assertTrue(result.stream().anyMatch(c -> c.getCarPackage().getPackageName().equals("Economy")));
        assertTrue(result.stream().anyMatch(c -> c.getCarPackage().getPackageName().equals("Standard")));
        assertTrue(result.stream().anyMatch(c -> c.getCarPackage().getPackageName().equals("Luxury")));
    }

    // ==================== TESTES DE PROPRIEDADE ====================

    @Test
    @DisplayName("TC13: Propriedade - Todos os carros retornados devem estar marcados como disponíveis")
    void shouldOnlyReturnCars_MarkedAsAvailable() {
        // Arrange
        List<Car> availableCars = carList.stream()
                .filter(Car::getIsAvailable)
                .collect(Collectors.toList());

        when(carRepository.findAvailableCars(any(PageRequest.class)))
                .thenReturn(availableCars);

        // Act
        List<Car> result = carService.getAvailableCars(1, Sort.Direction.ASC);

        // Assert - Propriedade: TODOS os carros devem ter isAvailable = true
        assertTrue(result.stream().allMatch(Car::getIsAvailable),
                "All returned cars must be available");
        assertFalse(result.stream().anyMatch(car -> !car.getIsAvailable()),
                "No unavailable car should be returned");
    }

    @Test
    @DisplayName("TC14: Propriedade - Carros devem ter número de registro único")
    void shouldHaveUniqueRegistrationNumbers_ForAllCars() {
        // Arrange
        List<Car> availableCars = carList.stream()
                .filter(Car::getIsAvailable)
                .collect(Collectors.toList());

        when(carRepository.findAvailableCars(any(PageRequest.class)))
                .thenReturn(availableCars);

        // Act
        List<Car> result = carService.getAvailableCars(1, Sort.Direction.ASC);

        // Assert - Propriedade: números de registro devem ser únicos
        long uniqueCount = result.stream()
                .map(Car::getRegistrationNr)
                .distinct()
                .count();
        
        assertEquals(result.size(), uniqueCount,
                "All cars should have unique registration numbers");
    }

    @Test
    @DisplayName("TC15: Propriedade - Integração entre pacotes e carros disponíveis")
    void shouldHaveConsistentData_BetweenPackagesAndAvailableCars() {
        // Arrange
        when(carPackageRepository.findAll()).thenReturn(packageList);
        
        List<Car> availableCars = carList.stream()
                .filter(Car::getIsAvailable)
                .collect(Collectors.toList());
        
        when(carRepository.findAvailableCars(any(PageRequest.class)))
                .thenReturn(availableCars);

        // Act
        List<CarPackage> packages = carService.getCarPackages();
        List<Car> cars = carService.getAvailableCars(1, Sort.Direction.ASC);

        // Assert - Propriedade: carros devem referenciar pacotes existentes
        List<String> packageNames = packages.stream()
                .map(CarPackage::getPackageName)
                .collect(Collectors.toList());

        assertTrue(cars.stream()
                .allMatch(car -> packageNames.contains(car.getCarPackage().getPackageName())),
                "All available cars should reference existing packages");
        
        // Propriedade: pelo menos um carro deve ter cada pacote disponível
        assertNotNull(cars.stream()
                .filter(c -> c.getCarPackage().getPackageName().equals("Economy"))
                .findFirst()
                .orElse(null));
    }
}
