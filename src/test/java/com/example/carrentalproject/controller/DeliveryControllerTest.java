package com.example.carrentalproject.controller;

import com.example.carrentalproject.domain.Car;
import com.example.carrentalproject.exception.InvalidPackageException;
import com.example.carrentalproject.exception.NoAccessKeyException;
import com.example.carrentalproject.exception.UnavailableCarException;
import com.example.carrentalproject.service.DeliveryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.persistence.EntityNotFoundException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeliveryControllerTest {

    @Mock
    private DeliveryService deliveryService;

    @InjectMocks
    private DeliveryController deliveryController;

    private Car testCar;

    @BeforeEach
    void setup() {
        testCar = new Car();
        testCar.setId(1L);
        testCar.setBrand("Toyota");
        testCar.setModel("Corolla");
        testCar.setIsAvailable(false);
    }

    @Test
    void itShouldPickUpTheCar() {
        // Given
        when(deliveryService.pickUpTheCar(1L)).thenReturn(testCar);

        // When
        Car result = deliveryController.pickUpTheCar(1L);

        // Then
        assertNotNull(result);
        assertEquals("Toyota", result.getBrand());
        assertFalse(result.getIsAvailable());
        verify(deliveryService).pickUpTheCar(1L);
    }

    @Test
    void itShouldThrowEntityNotFoundExceptionWhenCarNotFound() {
        // Given
        when(deliveryService.pickUpTheCar(999L))
                .thenThrow(new EntityNotFoundException("Car not found"));

        // When & Then
        assertThrows(EntityNotFoundException.class, 
                () -> deliveryController.pickUpTheCar(999L));
        verify(deliveryService).pickUpTheCar(999L);
    }

    @Test
    void itShouldThrowUnavailableCarException() {
        // Given
        when(deliveryService.pickUpTheCar(1L))
                .thenThrow(new UnavailableCarException("Car is not available"));

        // When & Then
        assertThrows(UnavailableCarException.class, 
                () -> deliveryController.pickUpTheCar(1L));
        verify(deliveryService).pickUpTheCar(1L);
    }

    @Test
    void itShouldThrowNoAccessKeyException() {
        // Given
        when(deliveryService.pickUpTheCar(1L))
                .thenThrow(new NoAccessKeyException("No access key found"));

        // When & Then
        assertThrows(NoAccessKeyException.class, 
                () -> deliveryController.pickUpTheCar(1L));
        verify(deliveryService).pickUpTheCar(1L);
    }

    @Test
    void itShouldThrowInvalidPackageException() {
        // Given
        when(deliveryService.pickUpTheCar(1L))
                .thenThrow(new InvalidPackageException("Invalid package"));

        // When & Then
        assertThrows(InvalidPackageException.class, 
                () -> deliveryController.pickUpTheCar(1L));
        verify(deliveryService).pickUpTheCar(1L);
    }
}
