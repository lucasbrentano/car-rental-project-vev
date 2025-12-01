package com.example.carrentalproject.controller;

import com.example.carrentalproject.domain.PlacedOrder;
import com.example.carrentalproject.dto.AccessKeyDto;
import com.example.carrentalproject.exception.ExistingOrderException;
import com.example.carrentalproject.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderController orderController;

    private PlacedOrder testOrder;
    private AccessKeyDto testAccessKey;

    @BeforeEach
    void setup() {
        testOrder = new PlacedOrder();
        testOrder.setId(1L);
        testOrder.setUserId(1L);
        testOrder.setCarId(1L);
        testOrder.setBrand("Toyota");
        testOrder.setModel("Corolla");
        testOrder.setStartTime(LocalDateTime.now());
        testOrder.setEndTime(LocalDateTime.now().plusHours(5));

        testAccessKey = new AccessKeyDto(1L, "Basic", 5);
    }

    @Test
    void itShouldGetAllOrders() {
        // Given
        List<PlacedOrder> orders = Arrays.asList(testOrder);
        when(orderService.getOrders()).thenReturn(orders);

        // When
        List<PlacedOrder> result = orderController.getOrders();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Toyota", result.get(0).getBrand());
        verify(orderService).getOrders();
    }

    @Test
    void itShouldSubmitOrder() {
        // Given
        when(orderService.submitOrder("Basic", 5)).thenReturn(testAccessKey);

        // When
        AccessKeyDto result = orderController.submitOrder("Basic", 5);

        // Then
        assertNotNull(result);
        assertEquals("Basic", result.getCarPackage());
        assertEquals(5, result.getHours());
        verify(orderService).submitOrder("Basic", 5);
    }

    @Test
    void itShouldThrowExistingOrderException() {
        // Given
        when(orderService.submitOrder("Basic", 5))
                .thenThrow(new ExistingOrderException("User already has an active order"));

        // When & Then
        assertThrows(ExistingOrderException.class, 
                () -> orderController.submitOrder("Basic", 5));
        verify(orderService).submitOrder("Basic", 5);
    }

    @Test
    void itShouldCallServiceWithCorrectParameters() {
        // Given
        when(orderService.submitOrder(anyString(), anyInt())).thenReturn(testAccessKey);

        // When
        orderController.submitOrder("Premium", 10);

        // Then
        verify(orderService).submitOrder("Premium", 10);
    }
}
