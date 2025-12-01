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

    // ==================== TESTES ADICIONAIS DE COBERTURA ====================

    @Test
    @DisplayName("TC16: Consultar pacotes com muitos itens - valor limite superior")
    void shouldReturnManyPackages_WhenLargeNumberExists() {
        // Arrange - Muitos pacotes (10+)
        List<CarPackage> manyPackages = new ArrayList<>();
        for (int i = 1; i <= 15; i++) {
            manyPackages.add(CarPackage.builder()
                    .id((long) i)
                    .packageName("Package" + i)
                    .pricePerHour(i * 50)
                    .build());
        }

        when(carPackageRepository.findAll()).thenReturn(manyPackages);

        // Act
        List<CarPackage> result = carService.getCarPackages();

        // Assert
        assertEquals(15, result.size());
        verify(carPackageRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("TC17: Consultar carros disponíveis com muitos itens - valor limite superior")
    void shouldReturnManyCars_WhenLargeNumberAvailable() {
        // Arrange - Muitos carros disponíveis
        List<Car> manyCars = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            manyCars.add(Car.builder()
                    .id((long) i)
                    .registrationNr("CAR" + String.format("%04d", i))
                    .brand("Brand" + i)
                    .model("Model" + i)
                    .isAvailable(true)
                    .carPackage(packageList.get(i % 3))
                    .build());
        }

        when(carRepository.findAvailableCars(any(PageRequest.class))).thenReturn(manyCars);

        // Act
        List<Car> result = carService.getAvailableCars(1, Sort.Direction.ASC);

        // Assert
        assertEquals(20, result.size());
        assertTrue(result.stream().allMatch(Car::getIsAvailable));
    }

    @Test
    @DisplayName("TC18: Consultar segunda página de carros - teste de paginação")
    void shouldReturnSecondPage_WhenQueryingCars() {
        // Arrange
        List<Car> availableCars = Arrays.asList(
                Car.builder().id(11L).registrationNr("PAGE2-001").brand("Brand1").model("Model1").isAvailable(true).build(),
                Car.builder().id(12L).registrationNr("PAGE2-002").brand("Brand2").model("Model2").isAvailable(true).build()
        );

        PageRequest pageRequest = PageRequest.of(1, 10, Sort.by(Sort.Direction.ASC, "id"));
        when(carRepository.findAvailableCars(pageRequest)).thenReturn(availableCars);

        // Act
        List<Car> result = carService.getAvailableCars(2, Sort.Direction.ASC);

        // Assert
        assertEquals(2, result.size());
        verify(carRepository).findAvailableCars(
                argThat(pr -> pr.getPageNumber() == 1 && pr.getPageSize() == 10)
        );
    }

    @Test
    @DisplayName("TC19: Consultar terceira página de carros - teste de paginação avançada")
    void shouldReturnThirdPage_WhenQueryingCars() {
        // Arrange
        List<Car> availableCars = Arrays.asList(
                Car.builder().id(21L).registrationNr("PAGE3-001").brand("Tesla").model("Model3").isAvailable(true).build()
        );

        PageRequest pageRequest = PageRequest.of(2, 10, Sort.by(Sort.Direction.ASC, "id"));
        when(carRepository.findAvailableCars(pageRequest)).thenReturn(availableCars);

        // Act
        List<Car> result = carService.getAvailableCars(3, Sort.Direction.ASC);

        // Assert
        assertEquals(1, result.size());
        verify(carRepository).findAvailableCars(
                argThat(pr -> pr.getPageNumber() == 2)
        );
    }

    @Test
    @DisplayName("TC20: Consultar pacotes com preço mínimo (1) - valor limite inferior de preço")
    void shouldReturnPackage_WithMinimumPrice() {
        // Arrange
        CarPackage minPricePackage = CarPackage.builder()
                .id(1L)
                .packageName("Minimum")
                .pricePerHour(1) // Preço mínimo
                .build();

        when(carPackageRepository.findAll()).thenReturn(Arrays.asList(minPricePackage));

        // Act
        List<CarPackage> result = carService.getCarPackages();

        // Assert
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getPricePerHour());
    }

    @Test
    @DisplayName("TC21: Consultar pacotes com preço muito alto (10000) - valor limite superior de preço")
    void shouldReturnPackage_WithVeryHighPrice() {
        // Arrange
        CarPackage expensivePackage = CarPackage.builder()
                .id(1L)
                .packageName("UltraLuxury")
                .pricePerHour(10000) // Preço muito alto
                .build();

        when(carPackageRepository.findAll()).thenReturn(Arrays.asList(expensivePackage));

        // Act
        List<CarPackage> result = carService.getCarPackages();

        // Assert
        assertEquals(1, result.size());
        assertEquals(10000, result.get(0).getPricePerHour());
    }

    @Test
    @DisplayName("TC22: Consultar carros com diferentes marcas - particionamento por marca")
    void shouldReturnCars_WithDifferentBrands() {
        // Arrange
        List<Car> diverseBrandCars = Arrays.asList(
                Car.builder().id(1L).registrationNr("AAA1111").brand("Toyota").model("Camry").isAvailable(true).build(),
                Car.builder().id(2L).registrationNr("BBB2222").brand("Honda").model("Accord").isAvailable(true).build(),
                Car.builder().id(3L).registrationNr("CCC3333").brand("Ford").model("Mustang").isAvailable(true).build(),
                Car.builder().id(4L).registrationNr("DDD4444").brand("BMW").model("320i").isAvailable(true).build()
        );

        when(carRepository.findAvailableCars(any(PageRequest.class))).thenReturn(diverseBrandCars);

        // Act
        List<Car> result = carService.getAvailableCars(1, Sort.Direction.ASC);

        // Assert
        long uniqueBrands = result.stream().map(Car::getBrand).distinct().count();
        assertEquals(4, uniqueBrands);
        assertTrue(result.stream().anyMatch(c -> c.getBrand().equals("Toyota")));
        assertTrue(result.stream().anyMatch(c -> c.getBrand().equals("Honda")));
        assertTrue(result.stream().anyMatch(c -> c.getBrand().equals("Ford")));
        assertTrue(result.stream().anyMatch(c -> c.getBrand().equals("BMW")));
    }

    @Test
    @DisplayName("TC23: Consultar carros com diferentes parâmetros - particionamento por configuração")
    void shouldReturnCars_WithDifferentParameters() {
        // Arrange
        CarParameters manualParams = CarParameters.builder()
                .id(1L)
                .fuelType(FuelType.PETROL)
                .gearBoxType(GearBoxType.MANUAL)
                .numberOfDoors(2)
                .numberOfSeats(2)
                .isAirConditioningAvailable(false)
                .build();

        CarParameters automaticParams = CarParameters.builder()
                .id(2L)
                .fuelType(FuelType.DIESEL)
                .gearBoxType(GearBoxType.AUTOMATIC)
                .numberOfDoors(4)
                .numberOfSeats(5)
                .isAirConditioningAvailable(true)
                .build();

        List<Car> diverseParamCars = Arrays.asList(
                Car.builder().id(1L).registrationNr("MAN001").brand("Toyota").model("Corolla")
                        .isAvailable(true).carParameters(manualParams).build(),
                Car.builder().id(2L).registrationNr("AUT001").brand("BMW").model("X5")
                        .isAvailable(true).carParameters(automaticParams).build()
        );

        when(carRepository.findAvailableCars(any(PageRequest.class))).thenReturn(diverseParamCars);

        // Act
        List<Car> result = carService.getAvailableCars(1, Sort.Direction.ASC);

        // Assert
        assertTrue(result.stream().anyMatch(c -> 
                c.getCarParameters().getGearBoxType() == GearBoxType.MANUAL));
        assertTrue(result.stream().anyMatch(c -> 
                c.getCarParameters().getGearBoxType() == GearBoxType.AUTOMATIC));
        assertTrue(result.stream().anyMatch(c -> 
                c.getCarParameters().getIsAirConditioningAvailable()));
        assertTrue(result.stream().anyMatch(c -> 
                !c.getCarParameters().getIsAirConditioningAvailable()));
    }

    @Test
    @DisplayName("TC24: Propriedade - Resultado de consulta deve ser determinístico com mesmos parâmetros")
    void shouldReturnSameResults_WhenQueryingWithSameParameters() {
        // Arrange
        List<Car> availableCars = carList.stream()
                .filter(Car::getIsAvailable)
                .collect(Collectors.toList());

        when(carRepository.findAvailableCars(any(PageRequest.class))).thenReturn(availableCars);

        // Act - Chamar duas vezes com mesmos parâmetros
        List<Car> result1 = carService.getAvailableCars(1, Sort.Direction.ASC);
        List<Car> result2 = carService.getAvailableCars(1, Sort.Direction.ASC);

        // Assert - Propriedade: resultados devem ser idênticos
        assertEquals(result1.size(), result2.size());
        assertEquals(result1.stream().map(Car::getId).collect(Collectors.toList()),
                     result2.stream().map(Car::getId).collect(Collectors.toList()));
        
        verify(carRepository, times(2)).findAvailableCars(any(PageRequest.class));
    }

    @Test
    @DisplayName("TC25: Propriedade - Pacotes devem ter nomes únicos")
    void shouldHaveUniquePackageNames_ForAllPackages() {
        // Arrange
        when(carPackageRepository.findAll()).thenReturn(packageList);

        // Act
        List<CarPackage> result = carService.getCarPackages();

        // Assert - Propriedade: nomes de pacotes devem ser únicos
        long uniqueNames = result.stream()
                .map(CarPackage::getPackageName)
                .distinct()
                .count();
        
        assertEquals(result.size(), uniqueNames,
                "All packages should have unique names");
    }

    @Test
    @DisplayName("TC26: Propriedade - IDs de pacotes devem ser únicos e positivos")
    void shouldHaveUniquePositiveIds_ForAllPackages() {
        // Arrange
        when(carPackageRepository.findAll()).thenReturn(packageList);

        // Act
        List<CarPackage> result = carService.getCarPackages();

        // Assert
        long uniqueIds = result.stream().map(CarPackage::getId).distinct().count();
        assertEquals(result.size(), uniqueIds, "All packages should have unique IDs");
        
        assertTrue(result.stream().allMatch(p -> p.getId() > 0),
                "All package IDs should be positive");
    }

    @Test
    @DisplayName("TC27: Propriedade - IDs de carros devem ser únicos e positivos")
    void shouldHaveUniquePositiveIds_ForAllCars() {
        // Arrange
        List<Car> availableCars = carList.stream()
                .filter(Car::getIsAvailable)
                .collect(Collectors.toList());

        when(carRepository.findAvailableCars(any(PageRequest.class))).thenReturn(availableCars);

        // Act
        List<Car> result = carService.getAvailableCars(1, Sort.Direction.ASC);

        // Assert
        long uniqueIds = result.stream().map(Car::getId).distinct().count();
        assertEquals(result.size(), uniqueIds, "All cars should have unique IDs");
        
        assertTrue(result.stream().allMatch(c -> c.getId() > 0),
                "All car IDs should be positive");
    }

    @Test
    @DisplayName("TC28: Consultar carros com todos os parâmetros obrigatórios preenchidos")
    void shouldReturnCars_WithAllMandatoryFieldsFilled() {
        // Arrange
        List<Car> availableCars = carList.stream()
                .filter(Car::getIsAvailable)
                .collect(Collectors.toList());

        when(carRepository.findAvailableCars(any(PageRequest.class))).thenReturn(availableCars);

        // Act
        List<Car> result = carService.getAvailableCars(1, Sort.Direction.ASC);

        // Assert - Propriedade: todos os campos obrigatórios devem estar preenchidos
        assertTrue(result.stream().allMatch(c -> c.getRegistrationNr() != null && !c.getRegistrationNr().isEmpty()),
                "All cars should have registration number");
        assertTrue(result.stream().allMatch(c -> c.getBrand() != null && !c.getBrand().isEmpty()),
                "All cars should have brand");
        assertTrue(result.stream().allMatch(c -> c.getModel() != null && !c.getModel().isEmpty()),
                "All cars should have model");
        assertTrue(result.stream().allMatch(c -> c.getIsAvailable() != null),
                "All cars should have availability status");
    }

    @Test
    @DisplayName("TC29: Consultar pacotes verificando campos obrigatórios")
    void shouldReturnPackages_WithAllMandatoryFieldsFilled() {
        // Arrange
        when(carPackageRepository.findAll()).thenReturn(packageList);

        // Act
        List<CarPackage> result = carService.getCarPackages();

        // Assert - Propriedade: todos os campos obrigatórios devem estar preenchidos
        assertTrue(result.stream().allMatch(p -> p.getPackageName() != null && !p.getPackageName().isEmpty()),
                "All packages should have name");
        assertTrue(result.stream().allMatch(p -> p.getPricePerHour() != null),
                "All packages should have price per hour");
        assertTrue(result.stream().allMatch(p -> p.getId() != null),
                "All packages should have ID");
    }

    @Test
    @DisplayName("TC30: Consultar carros com diferentes tipos de combustível")
    void shouldReturnCars_WithDifferentFuelTypes() {
        // Arrange
        CarParameters petrolParams = CarParameters.builder()
                .id(1L).fuelType(FuelType.PETROL).gearBoxType(GearBoxType.MANUAL)
                .numberOfDoors(4).numberOfSeats(5).isAirConditioningAvailable(true).build();

        CarParameters dieselParams = CarParameters.builder()
                .id(2L).fuelType(FuelType.DIESEL).gearBoxType(GearBoxType.AUTOMATIC)
                .numberOfDoors(4).numberOfSeats(5).isAirConditioningAvailable(true).build();

        CarParameters electricParams = CarParameters.builder()
                .id(3L).fuelType(FuelType.ELECTRIC).gearBoxType(GearBoxType.AUTOMATIC)
                .numberOfDoors(4).numberOfSeats(5).isAirConditioningAvailable(true).build();

        List<Car> diverseFuelCars = Arrays.asList(
                Car.builder().id(1L).registrationNr("PET001").brand("Toyota").model("Corolla")
                        .isAvailable(true).carParameters(petrolParams).build(),
                Car.builder().id(2L).registrationNr("DIE001").brand("VW").model("Golf")
                        .isAvailable(true).carParameters(dieselParams).build(),
                Car.builder().id(3L).registrationNr("ELE001").brand("Tesla").model("Model3")
                        .isAvailable(true).carParameters(electricParams).build()
        );

        when(carRepository.findAvailableCars(any(PageRequest.class))).thenReturn(diverseFuelCars);

        // Act
        List<Car> result = carService.getAvailableCars(1, Sort.Direction.ASC);

        // Assert
        assertTrue(result.stream().anyMatch(c -> c.getCarParameters().getFuelType() == FuelType.PETROL));
        assertTrue(result.stream().anyMatch(c -> c.getCarParameters().getFuelType() == FuelType.DIESEL));
        assertTrue(result.stream().anyMatch(c -> c.getCarParameters().getFuelType() == FuelType.ELECTRIC));
    }

    @Test
    @DisplayName("TC31: Consultar página vazia quando não há mais carros")
    void shouldReturnEmptyList_WhenPageExceedsAvailableCars() {
        // Arrange - Página além dos carros disponíveis
        when(carRepository.findAvailableCars(any(PageRequest.class))).thenReturn(new ArrayList<>());

        // Act
        List<Car> result = carService.getAvailableCars(100, Sort.Direction.ASC);

        // Assert
        assertTrue(result.isEmpty());
        verify(carRepository, times(1)).findAvailableCars(any(PageRequest.class));
    }

    @Test
    @DisplayName("TC32: Propriedade - Ordem de pacotes deve ser consistente entre chamadas")
    void shouldReturnPackages_InConsistentOrder() {
        // Arrange
        when(carPackageRepository.findAll()).thenReturn(packageList);

        // Act - Chamar múltiplas vezes
        List<CarPackage> result1 = carService.getCarPackages();
        List<CarPackage> result2 = carService.getCarPackages();
        List<CarPackage> result3 = carService.getCarPackages();

        // Assert - Propriedade: ordem deve ser consistente
        assertEquals(result1.size(), result2.size());
        assertEquals(result2.size(), result3.size());
        
        for (int i = 0; i < result1.size(); i++) {
            assertEquals(result1.get(i).getId(), result2.get(i).getId());
            assertEquals(result2.get(i).getId(), result3.get(i).getId());
        }
        
        verify(carPackageRepository, times(3)).findAll();
    }

    @Test
    @DisplayName("TC33: Consultar carros com carPackage não-nulo")
    void shouldReturnCars_WithNonNullCarPackage() {
        // Arrange
        List<Car> availableCars = carList.stream()
                .filter(Car::getIsAvailable)
                .collect(Collectors.toList());

        when(carRepository.findAvailableCars(any(PageRequest.class))).thenReturn(availableCars);

        // Act
        List<Car> result = carService.getAvailableCars(1, Sort.Direction.ASC);

        // Assert - Propriedade: todos os carros devem ter um pacote associado
        assertTrue(result.stream().allMatch(c -> c.getCarPackage() != null),
                "All cars should have an associated package");
    }

    @Test
    @DisplayName("TC34: Propriedade - Múltiplas consultas não devem alterar estado do sistema")
    void shouldNotModifySystemState_WhenQueryingMultipleTimes() {
        // Arrange
        when(carPackageRepository.findAll()).thenReturn(packageList);
        List<Car> availableCars = carList.stream()
                .filter(Car::getIsAvailable)
                .collect(Collectors.toList());
        when(carRepository.findAvailableCars(any(PageRequest.class))).thenReturn(availableCars);

        // Act - Realizar múltiplas consultas
        carService.getCarPackages();
        carService.getAvailableCars(1, Sort.Direction.ASC);
        carService.getCarPackages();
        carService.getAvailableCars(1, Sort.Direction.DESC);

        // Assert - Propriedade: apenas operações de leitura devem ter sido chamadas
        verify(carPackageRepository, times(2)).findAll();
        verify(carRepository, times(2)).findAvailableCars(any(PageRequest.class));
        
        // Verificar que nenhuma operação de escrita foi chamada
        verify(carPackageRepository, never()).save(any(CarPackage.class));
        verify(carRepository, never()).save(any(Car.class));
        verify(carPackageRepository, never()).delete(any(CarPackage.class));
        verify(carRepository, never()).delete(any(Car.class));
    }
}
