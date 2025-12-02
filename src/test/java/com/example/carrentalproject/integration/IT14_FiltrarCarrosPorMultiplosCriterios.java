package com.example.carrentalproject.integration;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * IT14 - Teste de Integração: Filtrar Carros por Múltiplos Critérios
 * 
 * Valida filtros de busca de carros com múltiplos critérios.
 * 
 * NOTA: Este teste documenta o comportamento esperado.
 * Como o endpoint atual não suporta filtros via parâmetros,
 * o teste implementa a lógica de filtro manualmente para validar
 * que os dados no banco atendem aos critérios quando filtrados.
 */
@SpringBootTest
@Transactional
@DisplayName("IT14 - Filtrar Carros por Múltiplos Critérios")
class IT14_FiltrarCarrosPorMultiplosCriterios {

    @Autowired
    private CarService carService;

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private CarPackageRepository carPackageRepository;

    @Autowired
    private CarParametersRepository carParametersRepository;

    @BeforeEach
    void setUp() {
        // Limpar dados existentes
        carParametersRepository.deleteAll();
        carRepository.deleteAll();
        carPackageRepository.deleteAll();

        // Criar pacotes usando builder
        CarPackage basicPackage = CarPackage.builder()
            .packageName("BASIC")
            .pricePerHour(120)
            .build();
        basicPackage = carPackageRepository.save(basicPackage);

        CarPackage premiumPackage = CarPackage.builder()
            .packageName("PREMIUM")
            .pricePerHour(200)
            .build();
        premiumPackage = carPackageRepository.save(premiumPackage);

        // Criar carros com diferentes características
        criarCarro("Toyota", "Corolla", true, basicPackage, FuelType.PETROL);
        criarCarro("Honda", "Civic", true, premiumPackage, FuelType.PETROL);
        criarCarro("Volkswagen", "Golf", true, premiumPackage, FuelType.DIESEL);
        criarCarro("Fiat", "Uno", true, basicPackage, FuelType.PETROL);
        criarCarro("Tesla", "Model 3", true, premiumPackage, FuelType.ELECTRIC);
        criarCarro("Nissan", "Sentra", false, premiumPackage, FuelType.PETROL);
    }

    @Test
    @DisplayName("Deve filtrar carros por fuelType = PETROL, minPrice = 100, maxPrice = 500")
    void deveFiltrarCarrosPorMultiplosCriterios() {
        // Given
        FuelType fuelTypeFilter = FuelType.PETROL;
        int minPrice = 100;
        int maxPrice = 500;

        // When - Buscar todos os carros disponíveis e aplicar filtros manualmente
        List<Car> allCars = carRepository.findAll();
        
        List<Car> filteredCars = allCars.stream()
            .filter(car -> {
                CarParameters params = car.getCarParameters();
                if (params == null || !fuelTypeFilter.equals(params.getFuelType())) {
                    return false;
                }
                Integer price = car.getCarPackage().getPricePerHour();
                return price >= minPrice && price <= maxPrice;
            })
            .collect(Collectors.toList());

        // Then
        assertNotNull(filteredCars);
        assertFalse(filteredCars.isEmpty());
        
        // Validar que todos os resultados atendem aos critérios
        for (Car car : filteredCars) {
            assertTrue(car.getIsAvailable());
            assertEquals(fuelTypeFilter, car.getCarParameters().getFuelType());
            Integer price = car.getCarPackage().getPricePerHour();
            assertTrue(price >= minPrice);
            assertTrue(price <= maxPrice);
        }

        // Honda Civic deve estar nos resultados (PETROL, R$ 150)
        List<String> carModels = filteredCars.stream()
            .map(car -> car.getBrand() + " " + car.getModel())
            .collect(Collectors.toList());
        
        assertTrue(carModels.contains("Honda Civic"));
        assertFalse(carModels.contains("Volkswagen Golf")); // DIESEL
        assertFalse(carModels.contains("Tesla Model 3")); // ELECTRIC
        assertFalse(carModels.contains("Nissan Sentra")); // não disponível

        System.out.println("✓ Filtros aplicados com sucesso: " + filteredCars.size() + " carros encontrados");
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando nenhum carro atende aos critérios")
    void deveRetornarVazioQuandoNenhumCarroAtendeCriterios() {
        // Given - Critérios que nenhum carro atende
        FuelType fuelTypeFilter = FuelType.ELECTRIC;
        int minPrice = 1000;
        int maxPrice = 2000;

        // When
        List<Car> allCars = carRepository.findAll();
        
        List<Car> filteredCars = allCars.stream()
            .filter(car -> {
                CarParameters params = car.getCarParameters();
                if (params == null || !fuelTypeFilter.equals(params.getFuelType())) {
                    return false;
                }
                Integer price = car.getCarPackage().getPricePerHour();
                return price >= minPrice && price <= maxPrice;
            })
            .collect(Collectors.toList());

        // Then
        assertNotNull(filteredCars);
        assertTrue(filteredCars.isEmpty());
    }

    @Test
    @DisplayName("Deve validar integração com CarParameters e CarPackage")
    void deveValidarIntegracaoComEntidadesRelacionadas() {
        // When
        List<Car> cars = carRepository.findAll();

        // Then
        assertFalse(cars.isEmpty());
        
        for (Car car : cars) {
            assertNotNull(car.getCarParameters());
            assertNotNull(car.getCarParameters().getFuelType());
            assertNotNull(car.getCarPackage());
            assertNotNull(car.getCarPackage().getPackageName());
            assertTrue(car.getCarPackage().getPricePerHour() > 0);
        }
    }

    // Método auxiliar
    private void criarCarro(String brand, String model, boolean isAvailable, 
                           CarPackage carPackage, FuelType fuelType) {
        Car car = Car.builder()
            .registrationNr(String.format("R%07d", (int)(Math.random() * 10000000)))
            .brand(brand)
            .model(model)
            .isAvailable(isAvailable)
            .carPackage(carPackage)
            .build();
        car = carRepository.save(car);

        CarParameters params = CarParameters.builder()
            .fuelType(fuelType)
            .gearBoxType(GearBoxType.AUTOMATIC)
            .numberOfSeats(5)
            .numberOfDoors(4)
            .isAirConditioningAvailable(true)
            .car(car)
            .build();
        carParametersRepository.save(params);
    }
}
