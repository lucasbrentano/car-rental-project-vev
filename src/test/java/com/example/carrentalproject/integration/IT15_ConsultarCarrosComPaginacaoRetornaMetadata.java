package com.example.carrentalproject.integration;

import com.example.carrentalproject.constant.FuelType;
import com.example.carrentalproject.constant.GearBoxType;
import com.example.carrentalproject.domain.Car;
import com.example.carrentalproject.domain.CarPackage;
import com.example.carrentalproject.domain.CarParameters;
import com.example.carrentalproject.repository.CarPackageRepository;
import com.example.carrentalproject.repository.CarParametersRepository;
import com.example.carrentalproject.repository.CarRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * IT15 - Teste de Integração: Consultar Carros com Paginação e Metadata
 * 
 * Valida paginação de carros com metadata completa (Page<Car>).
 * 
 * NOTA: Como o endpoint atual retorna List<Car> e não Page<Car>,
 * este teste valida diretamente o repositório para documentar
 * o comportamento esperado quando paginação for implementada.
 */
@SpringBootTest
@Transactional
@DisplayName("IT15 - Consultar Carros com Paginação e Metadata")
class IT15_ConsultarCarrosComPaginacaoRetornaMetadata {

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

        // Criar pacote
        CarPackage basicPackage = CarPackage.builder()
            .packageName("BASIC")
            .pricePerHour(50)
            .build();
        basicPackage = carPackageRepository.save(basicPackage);

        // Criar 25 carros para testar paginação
        for (int i = 1; i <= 25; i++) {
            Car car = Car.builder()
                .registrationNr("REG" + String.format("%05d", i))
                .brand("Brand" + i)
                .model("Model" + i)
                .isAvailable(true)
                .carPackage(basicPackage)
                .build();
            car = carRepository.save(car);

            CarParameters params = CarParameters.builder()
                .fuelType(FuelType.PETROL)
                .gearBoxType(GearBoxType.AUTOMATIC)
                .numberOfSeats(5)
                .numberOfDoors(4)
                .isAirConditioningAvailable(true)
                .car(car)
                .build();
            carParametersRepository.save(params);
        }
    }

    @Test
    @DisplayName("Deve retornar primeira página com metadata correta (page=0, size=10)")
    void deveRetornarPrimeiraPaginaComMetadata() {
        // Given
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "brand"));

        // When
        Page<Car> page = carRepository.findAll(pageable);

        // Then
        assertNotNull(page);
        assertEquals(10, page.getContent().size());
        assertEquals(25, page.getTotalElements());
        assertEquals(3, page.getTotalPages());
        assertEquals(0, page.getNumber());
        assertEquals(10, page.getSize());
        
        assertTrue(page.hasNext());
        assertFalse(page.hasPrevious());
        assertTrue(page.isFirst());
        assertFalse(page.isLast());

        System.out.println("✓ Primeira Página: " + page.getContent().size() + " de " + page.getTotalElements());
    }

    @Test
    @DisplayName("Deve retornar segunda página com metadata correta (page=1, size=10)")
    void deveRetornarSegundaPaginaComMetadata() {
        // Given
        Pageable pageable = PageRequest.of(1, 10);

        // When
        Page<Car> page = carRepository.findAll(pageable);

        // Then
        assertEquals(10, page.getContent().size());
        assertEquals(1, page.getNumber());
        assertTrue(page.hasNext());
        assertTrue(page.hasPrevious());
        assertFalse(page.isFirst());
        assertFalse(page.isLast());
    }

    @Test
    @DisplayName("Deve retornar última página com metadata correta (page=2, size=10)")
    void deveRetornarUltimaPaginaComMetadata() {
        // Given
        Pageable pageable = PageRequest.of(2, 10);

        // When
        Page<Car> page = carRepository.findAll(pageable);

        // Then
        assertEquals(5, page.getContent().size()); // 25 % 10 = 5
        assertEquals(2, page.getNumber());
        assertEquals(5, page.getNumberOfElements());
        
        assertFalse(page.hasNext());
        assertTrue(page.hasPrevious());
        assertFalse(page.isFirst());
        assertTrue(page.isLast());
    }

    @Test
    @DisplayName("Deve retornar página vazia quando page está além do limite")
    void deveRetornarPaginaVaziaAlemDoLimite() {
        // Given
        Pageable pageable = PageRequest.of(10, 10);

        // When
        Page<Car> page = carRepository.findAll(pageable);

        // Then
        assertTrue(page.getContent().isEmpty());
        assertEquals(0, page.getNumberOfElements());
        assertEquals(25, page.getTotalElements());
        assertEquals(3, page.getTotalPages());
    }

    @Test
    @DisplayName("Deve validar ordenação com paginação")
    void deveValidarOrdenacaoComPaginacao() {
        // Given
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "model"));

        // When
        Page<Car> page = carRepository.findAll(pageable);

        // Then
        List<Car> cars = page.getContent();
        assertEquals(10, cars.size());
        
        // Validar ordenação descendente
        for (int i = 1; i < cars.size(); i++) {
            String previousModel = cars.get(i - 1).getModel();
            String currentModel = cars.get(i).getModel();
            assertTrue(previousModel.compareTo(currentModel) >= 0);
        }
    }

    @Test
    @DisplayName("Deve validar Page.map() para transformação de DTOs")
    void deveValidarPageMapParaTransformacao() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Car> page = carRepository.findAll(pageable);

        // When - Transformar Page<Car> em Page<String>
        Page<String> mappedPage = page.map(car -> car.getBrand() + " " + car.getModel());

        // Then
        assertEquals(page.getTotalElements(), mappedPage.getTotalElements());
        assertEquals(page.getTotalPages(), mappedPage.getTotalPages());
        assertEquals(page.getNumber(), mappedPage.getNumber());
        
        mappedPage.getContent().forEach(carName -> {
            assertTrue(carName.contains("Brand"));
            assertTrue(carName.contains("Model"));
        });

        System.out.println("✓ Page.map() preserva metadata corretamente");
    }
}
