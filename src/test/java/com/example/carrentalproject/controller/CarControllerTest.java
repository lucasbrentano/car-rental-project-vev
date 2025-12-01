package com.example.carrentalproject.controller;

import com.example.carrentalproject.domain.Car;
import com.example.carrentalproject.domain.CarPackage;
import com.example.carrentalproject.domain.CarParameters;
import com.example.carrentalproject.dto.CarDto;
import com.example.carrentalproject.dto.CarPackageDto;
import com.example.carrentalproject.service.CarService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CarControllerTest {

    @Mock
    private CarService carService;

    @InjectMocks
    private CarController carController;

    private Car testCar;
    private CarDto testCarDto;
    private CarPackage testPackage;

    @BeforeEach
    void setup() {
        testCar = new Car();
        testCar.setId(1L);
        testCar.setBrand("Toyota");
        testCar.setModel("Corolla");
        testCar.setRegistrationNr("ABC1234");
        testCar.setIsAvailable(true);

        testCarDto = new CarDto("ABC1234", "Toyota", "Corolla", true);

        testPackage = new CarPackage();
        testPackage.setId(1L);
        testPackage.setPackageName("Basic");
        testPackage.setPricePerHour(50);
    }

    @Test
    void itShouldGetAllCars() {
        // Given
        List<Car> cars = Arrays.asList(testCar);
        when(carService.getAllCars(anyInt(), any(Sort.Direction.class))).thenReturn(cars);

        // When
        List<Car> result = carController.getAllCars(0, Sort.Direction.ASC);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(carService).getAllCars(0, Sort.Direction.ASC);
    }

    @Test
    void itShouldGetCarPackages() {
        // Given
        List<CarPackage> packages = Arrays.asList(testPackage);
        when(carService.getCarPackages()).thenReturn(packages);

        // When
        List<CarPackage> result = carController.getCarPackages();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(carService).getCarPackages();
    }

    @Test
    void itShouldGetAvailableCars() {
        // Given
        List<Car> cars = Arrays.asList(testCar);
        when(carService.getAvailableCars(anyInt(), any(Sort.Direction.class))).thenReturn(cars);

        // When
        List<Car> result = carController.getAvailableCars(0, Sort.Direction.ASC);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getIsAvailable());
        verify(carService).getAvailableCars(0, Sort.Direction.ASC);
    }

    @Test
    void itShouldGetCarById() {
        // Given
        when(carService.getCar(1L)).thenReturn(testCar);

        // When
        Car result = carController.getCar(1L);

        // Then
        assertNotNull(result);
        assertEquals("Toyota", result.getBrand());
        verify(carService).getCar(1L);
    }

    @Test
    void itShouldSaveCar() {
        // Given
        when(carService.saveCar(any(CarDto.class))).thenReturn(testCar);

        // When
        Car result = carController.saveCar(testCarDto);

        // Then
        assertNotNull(result);
        assertEquals("Toyota", result.getBrand());
        verify(carService).saveCar(testCarDto);
    }

    @Test
    void itShouldEditCar() {
        // Given
        when(carService.editCar(eq(1L), any(CarDto.class))).thenReturn(testCar);

        // When
        Car result = carController.editCar(1L, testCarDto);

        // Then
        assertNotNull(result);
        verify(carService).editCar(1L, testCarDto);
    }

    @Test
    void itShouldSetCarParameters() {
        // Given
        CarParameters parameters = new CarParameters();
        when(carService.setCarParameters(eq(1L), any(CarParameters.class))).thenReturn(testCar);

        // When
        Car result = carController.setCarParameters(1L, parameters);

        // Then
        assertNotNull(result);
        verify(carService).setCarParameters(1L, parameters);
    }

    @Test
    void itShouldSetCarPackage() {
        // Given
        when(carService.setCarPackage(1L, "Basic")).thenReturn(testCar);

        // When
        Car result = carController.setCarPackage(1L, "Basic");

        // Then
        assertNotNull(result);
        verify(carService).setCarPackage(1L, "Basic");
    }

    @Test
    void itShouldDeleteCar() {
        // Given
        doNothing().when(carService).deleteCar(1L);

        // When
        carController.deleteCar(1L);

        // Then
        verify(carService).deleteCar(1L);
    }

    @Test
    void itShouldSaveCarPackage() {
        // Given
        CarPackageDto packageDto = new CarPackageDto("Premium", 100);
        when(carService.saveCarPackage(any(CarPackageDto.class))).thenReturn(testPackage);

        // When
        CarPackage result = carController.saveCarPackage(packageDto);

        // Then
        assertNotNull(result);
        verify(carService).saveCarPackage(packageDto);
    }

    @Test
    void itShouldDeleteCarPackage() {
        // Given
        doNothing().when(carService).deleteCarPackage(1L);

        // When
        carController.deleteCarPackage(1L);

        // Then
        verify(carService).deleteCarPackage(1L);
    }
}
