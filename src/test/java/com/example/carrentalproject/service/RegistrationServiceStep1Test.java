package com.example.carrentalproject.service;

import com.example.carrentalproject.domain.User;
import com.example.carrentalproject.dto.UserInDto;
import com.example.carrentalproject.exception.ExistingEntityException;
import com.example.carrentalproject.exception.WeakPasswordException;
import com.example.carrentalproject.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for Step 1: POST /registration
 * Uses doubles (mocks) to isolate RegistrationService behavior.
 */
public class RegistrationServiceStep1Test {

    UserRepository userRepository;
    PasswordEncoder passwordEncoder;
    UserService userService;
    RegistrationService registrationService;

    @BeforeEach
    void setup() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        userService = mock(UserService.class);
        registrationService = new RegistrationService(userRepository, passwordEncoder, userService);
    }

    private UserInDto validUser() {
        return UserInDto.builder()
                .firstName("John")
                .lastName("Doe")
                .username("johnDoe88")
                .password("StrongP4ss")
                .email("john.doe@example.com")
                .phone(987654321)
                .build();
    }

    @Test
    @DisplayName("CT01 RegistroComDadosValidos: persists encoded password and unique username")
    void registerValidUser() {
        UserInDto dto = validUser();
        when(userRepository.findByUsername(dto.getUsername())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(dto.getPassword())).thenReturn("encoded");

        assertDoesNotThrow(() -> registrationService.registerUser(dto));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertEquals(dto.getUsername(), saved.getUsername());
        assertEquals("encoded", saved.getPassword());
    }

    @Test
    @DisplayName("CT18 RegistroComEmailDuplicado: allowed by service (no email check) -> should save")
    void registerWithDuplicateEmailAllowed() {
        // The service checks only username duplication; email duplication is not validated
        UserInDto dto = validUser();
        when(userRepository.findByUsername(dto.getUsername())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(dto.getPassword())).thenReturn("encoded");

        assertDoesNotThrow(() -> registrationService.registerUser(dto));
        verify(userRepository).save(any(User.class));
    }

    @Nested
    class UsernameValidations {
        @Test
        @DisplayName("CT05 RegistroComUsernameVazio -> ExistingEntityException or save? Expect duplication only; empty is treated as unique")
        void emptyUsername() {
            UserInDto base = validUser();
            final UserInDto dto = UserInDto.builder()
                    .firstName(base.getFirstName())
                    .lastName(base.getLastName())
                    .username("")
                    .password(base.getPassword())
                    .email(base.getEmail())
                    .phone(base.getPhone())
                    .build();

            when(userRepository.findByUsername("")).thenReturn(Optional.empty());
            when(passwordEncoder.encode(dto.getPassword())).thenReturn("enc");

            assertDoesNotThrow(() -> registrationService.registerUser(dto));
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("CT06 RegistroComUsernameNulo -> expect failure (RuntimeException)")
        void nullUsername() {
            UserInDto base = validUser();
            final UserInDto dto = UserInDto.builder()
                    .firstName(base.getFirstName())
                    .lastName(base.getLastName())
                    .username(null)
                    .password(base.getPassword())
                    .email(base.getEmail())
                    .phone(base.getPhone())
                    .build();

            // Service may not guard null explicitly; ensure it fails
            assertThrows(RuntimeException.class, () -> registrationService.registerUser(dto));
        }

        @Test
        @DisplayName("CT18 username duplicado -> ExistingEntityException")
        void duplicateUsername() {
            UserInDto dto = validUser();
            when(userRepository.findByUsername(dto.getUsername())).thenReturn(Optional.of(new User()));

            ExistingEntityException ex = assertThrows(ExistingEntityException.class, () -> registrationService.registerUser(dto));
            assertTrue(ex.getMessage().toLowerCase().contains("exists"));
            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    class PasswordValidations {
        @Test
        @DisplayName("CT13 RegistroComPasswordMuitoCurto -> WeakPasswordException")
        void weakPasswordTooShort() {
            UserInDto base = validUser();
            final UserInDto dto = UserInDto.builder()
                    .firstName(base.getFirstName())
                    .lastName(base.getLastName())
                    .username(base.getUsername())
                    .password("short")
                    .email(base.getEmail())
                    .phone(base.getPhone())
                    .build();

            when(userRepository.findByUsername(dto.getUsername())).thenReturn(Optional.empty());
            WeakPasswordException ex = assertThrows(WeakPasswordException.class, () -> registrationService.registerUser(dto));
            assertTrue(ex.getMessage().toLowerCase().contains("weak"));
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("CT11 RegistroComPasswordVazio -> WeakPasswordException")
        void emptyPassword() {
            UserInDto base = validUser();
            final UserInDto dto = UserInDto.builder()
                    .firstName(base.getFirstName())
                    .lastName(base.getLastName())
                    .username(base.getUsername())
                    .password("")
                    .email(base.getEmail())
                    .phone(base.getPhone())
                    .build();

            when(userRepository.findByUsername(dto.getUsername())).thenReturn(Optional.empty());
            assertThrows(WeakPasswordException.class, () -> registrationService.registerUser(dto));
        }

        @Test
        @DisplayName("CT12 RegistroComPasswordNulo -> expect failure (RuntimeException)")
        void nullPassword() {
            UserInDto base = validUser();
            final UserInDto dto = UserInDto.builder()
                    .firstName(base.getFirstName())
                    .lastName(base.getLastName())
                    .username(base.getUsername())
                    .password(null)
                    .email(base.getEmail())
                    .phone(base.getPhone())
                    .build();

            when(userRepository.findByUsername(dto.getUsername())).thenReturn(Optional.empty());
            assertThrows(RuntimeException.class, () -> registrationService.registerUser(dto));
        }
    }

    @Nested
    class EmailValidations {
        @Test
        @DisplayName("CT15 RegistroComEmailVazio -> allowed by service; saves user")
        void emptyEmail() {
            UserInDto base = validUser();
            final UserInDto dto = UserInDto.builder()
                    .firstName(base.getFirstName())
                    .lastName(base.getLastName())
                    .username(base.getUsername())
                    .password(base.getPassword())
                    .email("")
                    .phone(base.getPhone())
                    .build();

            when(userRepository.findByUsername(dto.getUsername())).thenReturn(Optional.empty());
            when(passwordEncoder.encode(dto.getPassword())).thenReturn("enc");
            assertDoesNotThrow(() -> registrationService.registerUser(dto));
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("CT16 RegistroComEmailNulo -> allowed by service; saves user")
        void nullEmail() {
            UserInDto base = validUser();
            final UserInDto dto = UserInDto.builder()
                    .firstName(base.getFirstName())
                    .lastName(base.getLastName())
                    .username(base.getUsername())
                    .password(base.getPassword())
                    .email(null)
                    .phone(base.getPhone())
                    .build();

            when(userRepository.findByUsername(dto.getUsername())).thenReturn(Optional.empty());
            when(passwordEncoder.encode(dto.getPassword())).thenReturn("enc");
            assertDoesNotThrow(() -> registrationService.registerUser(dto));
            verify(userRepository).save(any(User.class));
        }
    }

    @Nested
    class NameAndPhoneValidations {
        @Test
        @DisplayName("CT19 RegistroComNomeVazio -> allowed by service; saves user")
        void emptyFirstName() {
            UserInDto base = validUser();
            final UserInDto dto = UserInDto.builder()
                    .firstName("")
                    .lastName(base.getLastName())
                    .username(base.getUsername())
                    .password(base.getPassword())
                    .email(base.getEmail())
                    .phone(base.getPhone())
                    .build();

            when(userRepository.findByUsername(dto.getUsername())).thenReturn(Optional.empty());
            when(passwordEncoder.encode(dto.getPassword())).thenReturn("enc");
            assertDoesNotThrow(() -> registrationService.registerUser(dto));
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("CT20 RegistroComTelefoneFormatoInvalido -> allowed by service; saves user")
        void invalidPhoneFormat() {
            UserInDto base = validUser();
            final UserInDto dto = UserInDto.builder()
                    .firstName(base.getFirstName())
                    .lastName(base.getLastName())
                    .username(base.getUsername())
                    .password(base.getPassword())
                    .email(base.getEmail())
                    .phone(-1)
                    .build();

            when(userRepository.findByUsername(dto.getUsername())).thenReturn(Optional.empty());
            when(passwordEncoder.encode(dto.getPassword())).thenReturn("enc");
            assertDoesNotThrow(() -> registrationService.registerUser(dto));
            verify(userRepository).save(any(User.class));
        }
    }
}
