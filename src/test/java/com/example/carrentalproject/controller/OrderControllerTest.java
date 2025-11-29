package com.example.carrentalproject.controller;

import com.example.carrentalproject.domain.PlacedOrder;
import com.example.carrentalproject.dto.AccessKeyDto;
import com.example.carrentalproject.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    private MockMvc mockMvc;

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderController orderController;

    private PlacedOrder order1;
    private PlacedOrder order2;
    private List<PlacedOrder> mockOrders;
    private AccessKeyDto mockDto;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(orderController).build();

        order1 = PlacedOrder.builder()
                .id(1L)
                .userId(10L)
                .carId(100L)
                .brand("Toyota")
                .model("Corolla")
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now().plusHours(2))
                .build();

        order2 = PlacedOrder.builder()
                .id(2L)
                .userId(20L)
                .carId(200L)
                .brand("BMW")
                .model("M3")
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now().plusHours(3))
                .build();

        mockOrders = Arrays.asList(order1, order2);

        mockDto = AccessKeyDto.builder()
                .id(5L)
                .carPackage("Sporty")
                .hours(2)
                .build();
    }

    // GET /orders 
    @Test
    void itShouldReturnListOfOrders() throws Exception {
        when(orderService.getOrders()).thenReturn(mockOrders);

        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].brand").value("Toyota"))
                .andExpect(jsonPath("$[1].brand").value("BMW"));
    }

    @Test
    void itShouldCallServiceGetOrdersOnce() throws Exception {
        when(orderService.getOrders()).thenReturn(mockOrders);

        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk());

        verify(orderService, times(1)).getOrders();
    }

    // POST /orders 
    @Test
    void itShouldSubmitOrderAndReturnAccessKeyDto() throws Exception {
        when(orderService.submitOrder("Sporty", 2)).thenReturn(mockDto);

        mockMvc.perform(post("/orders")
                .param("carPackage", "Sporty")
                .param("hours", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5L))
                .andExpect(jsonPath("$.carPackage").value("Sporty"))
                .andExpect(jsonPath("$.hours").value(2));
    }

    @Test
    void itShouldCallServiceSubmitOrderWithCorrectParameters() throws Exception {
        when(orderService.submitOrder("Luxury", 5)).thenReturn(mockDto);

        mockMvc.perform(post("/orders")
                .param("carPackage", "Luxury")
                .param("hours", "5"))
                .andExpect(status().isOk());

        verify(orderService).submitOrder("Luxury", 5);
    }

    @Test
    void itShouldReturn400WhenMissingParameters() throws Exception {
        mockMvc.perform(post("/orders")
                .param("hours", "2"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/orders")
                .param("carPackage", "Sporty"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/orders"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void itShouldReturn500WhenServiceThrowsException() throws Exception {
        when(orderService.submitOrder("Sporty", 2))
                .thenThrow(new RuntimeException("Error"));

        mockMvc.perform(post("/orders")
                .param("carPackage", "Sporty")
                .param("hours", "2"))
                .andExpect(status().isInternalServerError());
    }
}
