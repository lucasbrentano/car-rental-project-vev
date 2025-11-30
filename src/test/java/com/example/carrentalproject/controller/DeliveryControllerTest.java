package com.example.carrentalproject.controller;

import com.example.carrentalproject.domain.Car;
import com.example.carrentalproject.exception.InvalidPackageException;
import com.example.carrentalproject.exception.NoAccessKeyException;
import com.example.carrentalproject.exception.UnavailableCarException;
import com.example.carrentalproject.service.DeliveryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import javax.persistence.EntityNotFoundException;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DeliveryController.class)
class DeliveryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DeliveryService deliveryService;

    // POST /delivery

    @Test
    void itShouldCallServiceWithCorrectId() throws Exception {
        Car mockCar = new Car();
        mockCar.setId(1L);

        when(deliveryService.pickUpTheCar(1L)).thenReturn(mockCar);

        mockMvc.perform(post("/delivery")
                        .param("carId", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(deliveryService, times(1)).pickUpTheCar(1L);
    }

    @Test
    void itShouldReturnCarFromService() throws Exception {
        Car car = new Car();
        car.setId(10L);
        car.setBrand("Toyota");
        car.setModel("Corolla");

        when(deliveryService.pickUpTheCar(10L)).thenReturn(car);

        mockMvc.perform(post("/delivery")
                        .param("carId", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.brand").value("Toyota"))
                .andExpect(jsonPath("$.model").value("Corolla"));
    }

    @Test
    void itShouldReturn404WhenEntityNotFoundExceptionIsThrown() throws Exception {
        when(deliveryService.pickUpTheCar(2L))
                .thenThrow(new EntityNotFoundException("not found"));

        mockMvc.perform(post("/delivery")
                        .param("carId", "2")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void itShouldReturn400WhenNoAccessKeyExceptionIsThrown() throws Exception {
        when(deliveryService.pickUpTheCar(3L))
                .thenThrow(new NoAccessKeyException("no key"));

        mockMvc.perform(post("/delivery")
                        .param("carId", "3")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void itShouldReturn400WhenInvalidPackageExceptionIsThrown() throws Exception {
        when(deliveryService.pickUpTheCar(4L))
                .thenThrow(new InvalidPackageException("invalid package"));

        mockMvc.perform(post("/delivery")
                        .param("carId", "4")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void itShouldReturn400WhenUnavailableCarExceptionIsThrown() throws Exception {
        when(deliveryService.pickUpTheCar(5L))
                .thenThrow(new UnavailableCarException("unavailable"));

        mockMvc.perform(post("/delivery")
                        .param("carId", "5")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}

