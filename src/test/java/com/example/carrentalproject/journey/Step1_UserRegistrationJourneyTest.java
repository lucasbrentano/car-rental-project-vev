package com.example.carrentalproject.journey;

import com.example.carrentalproject.domain.User;
import com.example.carrentalproject.dto.UserInDto;
import com.example.carrentalproject.exception.ExistingEntityException;
import com.example.carrentalproject.exception.WeakPasswordException;
import com.example.carrentalproject.repository.UserRepository;
import com.example.carrentalproject.service.RegistrationService;
import com.example.carrentalproject.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Testes de Jornada - Etapa 1: Usuária cria conta
 * POST /registration
 * 
 * Técnicas aplicadas:
 * - Particionamento de equivalência
 * - Análise de valores limite
 * - Testes de propriedade
 * - Uso de dublês (Mocks)
 * - Testes de cobertura essencial
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Journey Step 1 - User Registration Tests")
class Step1_UserRegistrationJourneyTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserService userService;

    @InjectMocks
    private RegistrationService registrationService;

    private UserInDto validUserDto;

    @BeforeEach
    void setUp() {
        validUserDto = UserInDto.builder()
                .firstName("Maria")
                .lastName("Silva")
                .username("maria.silva")
                .password("SecurePass123")
                .email("maria.silva@email.com")
                .phone(987654321)
                .build();
    }

    // ==================== PARTICIONAMENTO: Casos Válidos ====================

    @Test
    @DisplayName("TC1: Registro bem-sucedido com dados válidos - partição válida completa")
    void shouldRegisterUserSuccessfully_WithValidData() {
        // Arrange
        when(userRepository.findByUsername(validUserDto.getUsername())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(validUserDto.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(new User());
        when(userService.addRoleToUser(anyString(), anyString())).thenReturn(new User());

        // Act & Assert
        assertDoesNotThrow(() -> registrationService.registerUser(validUserDto));
        
        verify(userRepository, times(1)).findByUsername(validUserDto.getUsername());
        verify(passwordEncoder, times(1)).encode(validUserDto.getPassword());
        verify(userRepository, times(1)).save(any(User.class));
        verify(userService, times(1)).addRoleToUser(validUserDto.getUsername(), "ROLE_USER");
    }

    @Test
    @DisplayName("TC2: Registro com senha mínima válida (8 caracteres) - valor limite inferior")
    void shouldRegisterUser_WithMinimumValidPassword() {
        // Arrange - Senha com exatamente 8 caracteres (limite inferior válido)
        UserInDto userWithMinPassword = UserInDto.builder()
                .firstName("Ana")
                .lastName("Costa")
                .username("ana.costa")
                .password("Pass123A") // 8 caracteres: maiúscula, minúscula, número
                .email("ana@email.com")
                .phone(123456789)
                .build();

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(new User());

        // Act & Assert
        assertDoesNotThrow(() -> registrationService.registerUser(userWithMinPassword));
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("TC3: Registro com senha muito longa (50 caracteres) - valor limite superior")
    void shouldRegisterUser_WithLongValidPassword() {
        // Arrange - Senha longa mas válida
        UserInDto userWithLongPassword = UserInDto.builder()
                .firstName("Julia")
                .lastName("Santos")
                .username("julia.santos")
                .password("VeryLongSecurePassword123WithManyCharacters456789") // 50 caracteres
                .email("julia@email.com")
                .phone(111222333)
                .build();

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(new User());

        // Act & Assert
        assertDoesNotThrow(() -> registrationService.registerUser(userWithLongPassword));
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("TC4: Registro com telefone de 9 dígitos - partição válida de phone")
    void shouldRegisterUser_WithValidNineDigitPhone() {
        // Arrange - Telefone com 9 dígitos (máximo permitido pela coluna)
        UserInDto userWithMaxPhone = UserInDto.builder()
                .firstName("Carlos")
                .lastName("Oliveira")
                .username("carlos.oliveira")
                .password("SecurePass456")
                .email("carlos@email.com")
                .phone(999999999) // 9 dígitos - limite superior
                .build();

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(new User());

        // Act & Assert
        assertDoesNotThrow(() -> registrationService.registerUser(userWithMaxPhone));
        verify(userRepository, times(1)).save(any(User.class));
    }

    // ==================== PARTICIONAMENTO: Casos Inválidos ====================

    @Test
    @DisplayName("TC5: Falha ao registrar usuário duplicado - partição inválida (username existente)")
    void shouldThrowException_WhenUsernameAlreadyExists() {
        // Arrange - Partição inválida: usuário já existe
        User existingUser = User.builder()
                .username(validUserDto.getUsername())
                .build();

        when(userRepository.findByUsername(validUserDto.getUsername()))
                .thenReturn(Optional.of(existingUser));

        // Act & Assert
        ExistingEntityException exception = assertThrows(
                ExistingEntityException.class,
                () -> registrationService.registerUser(validUserDto)
        );

        assertEquals("User With Given Username Already Exists!", exception.getMessage());
        verify(userRepository, times(1)).findByUsername(validUserDto.getUsername());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("TC6: Falha com senha sem letra maiúscula - partição inválida de senha")
    void shouldThrowException_WhenPasswordHasNoUppercase() {
        // Arrange - Senha inválida: sem letra maiúscula
        UserInDto userWithInvalidPassword = UserInDto.builder()
                .firstName("Pedro")
                .lastName("Alves")
                .username("pedro.alves")
                .password("password123") // sem maiúscula
                .email("pedro@email.com")
                .phone(123456789)
                .build();

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(WeakPasswordException.class,
                () -> registrationService.registerUser(userWithInvalidPassword));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("TC7: Falha com senha sem letra minúscula - partição inválida de senha")
    void shouldThrowException_WhenPasswordHasNoLowercase() {
        // Arrange - Senha inválida: sem letra minúscula
        UserInDto userWithInvalidPassword = UserInDto.builder()
                .firstName("Laura")
                .lastName("Martins")
                .username("laura.martins")
                .password("PASSWORD123") // sem minúscula
                .email("laura@email.com")
                .phone(987654321)
                .build();

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(WeakPasswordException.class,
                () -> registrationService.registerUser(userWithInvalidPassword));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("TC8: Falha com senha sem número - partição inválida de senha")
    void shouldThrowException_WhenPasswordHasNoDigit() {
        // Arrange - Senha inválida: sem número
        UserInDto userWithInvalidPassword = UserInDto.builder()
                .firstName("Ricardo")
                .lastName("Ferreira")
                .username("ricardo.ferreira")
                .password("PasswordOnly") // sem número
                .email("ricardo@email.com")
                .phone(111222333)
                .build();

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(WeakPasswordException.class,
                () -> registrationService.registerUser(userWithInvalidPassword));
        verify(userRepository, never()).save(any(User.class));
    }

    // ==================== VALORES LIMITE ====================

    @Test
    @DisplayName("TC9: Falha com senha de 7 caracteres - valor limite inferior inválido")
    void shouldThrowException_WhenPasswordHasSevenCharacters() {
        // Arrange - Senha com 7 caracteres (abaixo do mínimo de 8)
        UserInDto userWithShortPassword = UserInDto.builder()
                .firstName("Beatriz")
                .lastName("Lima")
                .username("beatriz.lima")
                .password("Pass12A") // 7 caracteres - limite inferior inválido
                .email("beatriz@email.com")
                .phone(444555666)
                .build();

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(WeakPasswordException.class,
                () -> registrationService.registerUser(userWithShortPassword));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("TC10: Registro com telefone de 1 dígito - valor limite inferior válido")
    void shouldRegisterUser_WithMinimumPhone() {
        // Arrange - Telefone com 1 dígito (limite inferior)
        UserInDto userWithMinPhone = UserInDto.builder()
                .firstName("Fernando")
                .lastName("Rocha")
                .username("fernando.rocha")
                .password("SecurePass789")
                .email("fernando@email.com")
                .phone(1) // 1 dígito - limite inferior
                .build();

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(new User());

        // Act & Assert
        assertDoesNotThrow(() -> registrationService.registerUser(userWithMinPhone));
        verify(userRepository, times(1)).save(any(User.class));
    }

    // ==================== TESTES DE PROPRIEDADE ====================

    @Test
    @DisplayName("TC11: Propriedade - Senha deve sempre ser encodada antes de salvar")
    void shouldAlwaysEncodePassword_BeforeSaving() {
        // Arrange
        String rawPassword = "MyPassword123";
        String encodedPassword = "encoded_MyPassword123";
        
        UserInDto user = UserInDto.builder()
                .firstName("Gabriela")
                .lastName("Souza")
                .username("gabriela.souza")
                .password(rawPassword)
                .email("gabriela@email.com")
                .phone(777888999)
                .build();

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            // Propriedade: senha salva NUNCA deve ser igual à senha original
            assertNotEquals(rawPassword, savedUser.getPassword());
            // Propriedade: senha salva deve ser a versão encodada
            assertEquals(encodedPassword, savedUser.getPassword());
            return savedUser;
        });

        // Act
        registrationService.registerUser(user);

        // Assert
        verify(passwordEncoder, times(1)).encode(rawPassword);
    }

    @Test
    @DisplayName("TC12: Propriedade - Role ROLE_USER deve sempre ser atribuída após registro")
    void shouldAlwaysAssignRoleUser_AfterSuccessfulRegistration() {
        // Arrange
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(new User());
        when(userService.addRoleToUser(anyString(), eq("ROLE_USER"))).thenReturn(new User());

        // Act
        registrationService.registerUser(validUserDto);

        // Assert - Propriedade: toda usuária registrada deve ter ROLE_USER
        verify(userService, times(1)).addRoleToUser(
                eq(validUserDto.getUsername()),
                eq("ROLE_USER")
        );
        
        // Propriedade: addRoleToUser deve ser chamada exatamente uma vez
        verify(userService, times(1)).addRoleToUser(anyString(), anyString());
    }
}
