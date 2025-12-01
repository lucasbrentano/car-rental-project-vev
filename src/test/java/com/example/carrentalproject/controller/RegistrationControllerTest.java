package com.example.carrentalproject.controller;

import com.example.carrentalproject.dto.UserInDto;
import com.example.carrentalproject.exception.WeakPasswordException;
import com.example.carrentalproject.service.RegistrationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class RegistrationControllerTest {

    @Mock
    private RegistrationService registrationService;

    @InjectMocks
    private RegistrationController registrationController;

    private UserInDto userInDto;

    @BeforeEach
    void setup() {
        userInDto = new UserInDto(
                "Maria",
                "Silva",
                "maria123",
                "Maria123",
                "maria@gmail.com",
                123456789
        );
    }

    @Test
    void itShouldRegisterUser() {
        // Given
        doNothing().when(registrationService).registerUser(any(UserInDto.class));

        // When & Then
        assertDoesNotThrow(() -> registrationController.registerUser(userInDto));
        verify(registrationService, times(1)).registerUser(userInDto);
    }

    @Test
    void itShouldThrowWeakPasswordException() {
        // Given
        doThrow(new WeakPasswordException("Weak password"))
                .when(registrationService)
                .registerUser(any(UserInDto.class));

        // When & Then
        assertThrows(WeakPasswordException.class, 
                () -> registrationController.registerUser(userInDto));
    }

    @Test
    void itShouldCallServiceWithCorrectData() {
        // Given
        doNothing().when(registrationService).registerUser(any(UserInDto.class));

        // When
        registrationController.registerUser(userInDto);

        // Then
        verify(registrationService, times(1)).registerUser(userInDto);
    }
}

