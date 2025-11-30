package com.example.carrentalproject.controller;

import com.example.carrentalproject.dto.UserInDto;
import com.example.carrentalproject.service.RegistrationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class RegistrationControllerTest {

    private MockMvc mockMvc;

    @Mock
    private RegistrationService registrationService;

    @InjectMocks
    private RegistrationController registrationController;

    private ObjectMapper objectMapper;

    private UserInDto userInDto;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(registrationController).build();
        objectMapper = new ObjectMapper();

        userInDto = UserInDto.builder()
                .firstName("Maria")
                .lastName("Silva")
                .email("maria@gmail.com")
                .password("123456")
                .build();

    }

    // POST /registration
    @Test
    void itShouldRegisterUser() throws Exception {
        mockMvc.perform(post("/registration")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(userInDto)))
                .andExpect(status().isOk());

        verify(registrationService, times(1)).registerUser(userInDto);
    }

    @Test
    void itShouldReturn400WhenInvalidRequestBody() throws Exception {
        mockMvc.perform(post("/registration")
                .contentType("application/json")
                .content("{}"))
                .andExpect(status().isBadRequest());

        verify(registrationService, times(0)).registerUser(any());
    }

    @Test
    void itShouldReturn500WhenServiceThrows() throws Exception {
        doThrow(new RuntimeException("Failed"))
                .when(registrationService)
                .registerUser(userInDto);

        mockMvc.perform(post("/registration")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(userInDto)))
                .andExpect(status().isInternalServerError());
    }
}

