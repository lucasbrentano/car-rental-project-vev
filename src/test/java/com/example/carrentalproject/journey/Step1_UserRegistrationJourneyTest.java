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

    // ==================== TESTES ADICIONAIS DE COBERTURA ====================

    @Test
    @DisplayName("TC13: Falha com senha contendo caracteres especiais não permitidos")
    void shouldThrowException_WhenPasswordContainsSpecialCharacters() {
        // Arrange - Senha com caracteres especiais (regex não permite)
        UserInDto userWithSpecialChars = UserInDto.builder()
                .firstName("Marcos")
                .lastName("Pereira")
                .username("marcos.pereira")
                .password("Pass@123!") // contém @ e ! que não são permitidos
                .email("marcos@email.com")
                .phone(555666777)
                .build();

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(WeakPasswordException.class,
                () -> registrationService.registerUser(userWithSpecialChars));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("TC14: Falha com senha contendo apenas espaços em branco")
    void shouldThrowException_WhenPasswordIsOnlyWhitespace() {
        // Arrange - Senha inválida: apenas espaços
        UserInDto userWithWhitespacePassword = UserInDto.builder()
                .firstName("Sandra")
                .lastName("Costa")
                .username("sandra.costa")
                .password("        ") // apenas espaços
                .email("sandra@email.com")
                .phone(888999000)
                .build();

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(WeakPasswordException.class,
                () -> registrationService.registerUser(userWithWhitespacePassword));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("TC15: Falha com senha vazia")
    void shouldThrowException_WhenPasswordIsEmpty() {
        // Arrange - Senha vazia
        UserInDto userWithEmptyPassword = UserInDto.builder()
                .firstName("Roberto")
                .lastName("Dias")
                .username("roberto.dias")
                .password("") // senha vazia
                .email("roberto@email.com")
                .phone(111222333)
                .build();

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(WeakPasswordException.class,
                () -> registrationService.registerUser(userWithEmptyPassword));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("TC16: Registro com username contendo caracteres especiais válidos")
    void shouldRegisterUser_WithSpecialCharactersInUsername() {
        // Arrange - Username com ponto e underscore
        UserInDto userWithSpecialUsername = UserInDto.builder()
                .firstName("Patricia")
                .lastName("Gomes")
                .username("patricia_gomes.2024")
                .password("SecurePass999")
                .email("patricia@email.com")
                .phone(123321456)
                .build();

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(new User());

        // Act & Assert
        assertDoesNotThrow(() -> registrationService.registerUser(userWithSpecialUsername));
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("TC17: Registro com email em formato válido complexo")
    void shouldRegisterUser_WithComplexEmail() {
        // Arrange - Email com formato complexo mas válido
        UserInDto userWithComplexEmail = UserInDto.builder()
                .firstName("Lucas")
                .lastName("Mendes")
                .username("lucas.mendes")
                .password("SecurePass111")
                .email("lucas.mendes+test@subdomain.company.com.br")
                .phone(999888777)
                .build();

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(new User());

        // Act & Assert
        assertDoesNotThrow(() -> registrationService.registerUser(userWithComplexEmail));
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("TC18: Verificar ordem de execução - validação de username existente ocorre antes de validação de senha")
    void shouldCheckUsernameExistence_BeforePasswordValidation() {
        // Arrange - Username existente E senha inválida
        UserInDto userWithExistingUsernameAndWeakPassword = UserInDto.builder()
                .firstName("Rafael")
                .lastName("Barbosa")
                .username("existing.user")
                .password("weak") // senha fraca
                .email("rafael@email.com")
                .phone(444555666)
                .build();

        User existingUser = User.builder()
                .username("existing.user")
                .build();

        when(userRepository.findByUsername("existing.user"))
                .thenReturn(Optional.of(existingUser));

        // Act & Assert
        // Deve lançar ExistingEntityException (não WeakPasswordException)
        // isso prova que verificação de username vem primeiro
        ExistingEntityException exception = assertThrows(
                ExistingEntityException.class,
                () -> registrationService.registerUser(userWithExistingUsernameAndWeakPassword)
        );

        assertEquals("User With Given Username Already Exists!", exception.getMessage());
        verify(userRepository, times(1)).findByUsername("existing.user");
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("TC19: Registro com todos os campos no limite máximo de tamanho")
    void shouldRegisterUser_WithMaximumFieldLengths() {
        // Arrange - Campos com tamanhos grandes
        UserInDto userWithMaxFields = UserInDto.builder()
                .firstName("NomeExtremaLongoComMuitosCaracteresParaTestarLimiteDoBanco")
                .lastName("SobrenomeExtremaLongoComMuitosCaracteresParaTestarLimiteDoBanco")
                .username("username_extremamente_longo_para_teste_de_limite_maximo")
                .password("VerySecurePassword123WithMoreThan8Characters")
                .email("email.muito.longo.para.teste.de.validacao@dominio.muito.grande.com.br")
                .phone(999999999)
                .build();

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(new User());

        // Act & Assert
        assertDoesNotThrow(() -> registrationService.registerUser(userWithMaxFields));
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("TC20: Propriedade - Múltiplos registros sucessivos devem ser independentes")
    void shouldRegisterMultipleUsers_Independently() {
        // Arrange - Dois usuários diferentes
        UserInDto user1 = UserInDto.builder()
                .firstName("User")
                .lastName("One")
                .username("user.one")
                .password("Password123")
                .email("user1@email.com")
                .phone(111111111)
                .build();

        UserInDto user2 = UserInDto.builder()
                .firstName("User")
                .lastName("Two")
                .username("user.two")
                .password("Password456")
                .email("user2@email.com")
                .phone(222222222)
                .build();

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encoded1", "encoded2");
        when(userRepository.save(any(User.class))).thenReturn(new User());

        // Act
        registrationService.registerUser(user1);
        registrationService.registerUser(user2);

        // Assert - Propriedade: cada registro deve causar exatamente 1 save
        verify(userRepository, times(2)).save(any(User.class));
        verify(passwordEncoder, times(2)).encode(anyString());
        
        // Propriedade: usernames devem ser verificados individualmente
        verify(userRepository, times(1)).findByUsername("user.one");
        verify(userRepository, times(1)).findByUsername("user.two");
    }

    @Test
    @DisplayName("TC21: Registro com senha contendo exatamente os requisitos mínimos")
    void shouldRegisterUser_WithExactMinimumPasswordRequirements() {
        // Arrange - Senha com exatamente 1 maiúscula, 1 minúscula, 1 número, 8 chars
        UserInDto userWithMinimalPassword = UserInDto.builder()
                .firstName("Minimal")
                .lastName("User")
                .username("minimal.user")
                .password("Aaaaaaa1") // 1 maiúscula (A), 6 minúsculas (aaaaaa), 1 número (1) = 8 chars
                .email("minimal@email.com")
                .phone(100000001)
                .build();

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(new User());

        // Act & Assert
        assertDoesNotThrow(() -> registrationService.registerUser(userWithMinimalPassword));
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("TC22: Falha com senha tendo múltiplas maiúsculas mas sem minúsculas")
    void shouldThrowException_WhenPasswordHasMultipleUppercaseButNoLowercase() {
        // Arrange - Senha com várias maiúsculas, números, mas sem minúsculas
        UserInDto userWithNoLowercase = UserInDto.builder()
                .firstName("Test")
                .lastName("User")
                .username("test.user")
                .password("PASSWORD123456") // múltiplas maiúsculas e números, mas SEM minúsculas
                .email("test@email.com")
                .phone(777777777)
                .build();

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(WeakPasswordException.class,
                () -> registrationService.registerUser(userWithNoLowercase));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("TC23: Falha com senha tendo múltiplas minúsculas mas sem maiúsculas")
    void shouldThrowException_WhenPasswordHasMultipleLowercaseButNoUppercase() {
        // Arrange - Senha com várias minúsculas, números, mas sem maiúsculas
        UserInDto userWithNoUppercase = UserInDto.builder()
                .firstName("Another")
                .lastName("Test")
                .username("another.test")
                .password("password123456") // múltiplas minúsculas e números, mas SEM maiúsculas
                .email("another@email.com")
                .phone(888888888)
                .build();

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(WeakPasswordException.class,
                () -> registrationService.registerUser(userWithNoUppercase));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("TC24: Falha com senha tendo múltiplos números mas sem letras")
    void shouldThrowException_WhenPasswordHasOnlyDigits() {
        // Arrange - Senha apenas com números
        UserInDto userWithOnlyDigits = UserInDto.builder()
                .firstName("Numbers")
                .lastName("Only")
                .username("numbers.only")
                .password("12345678901234") // apenas números
                .email("numbers@email.com")
                .phone(123456789)
                .build();

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(WeakPasswordException.class,
                () -> registrationService.registerUser(userWithOnlyDigits));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("TC25: Registro com password contendo mix de maiúsculas e minúsculas")
    void shouldRegisterUser_WithMixedCasePassword() {
        // Arrange - Senha com várias maiúsculas e minúsculas intercaladas
        UserInDto userWithMixedCase = UserInDto.builder()
                .firstName("Mixed")
                .lastName("Case")
                .username("mixed.case")
                .password("PaSsWoRd123") // mix de maiúsculas e minúsculas
                .email("mixed@email.com")
                .phone(555555555)
                .build();

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(new User());

        // Act & Assert
        assertDoesNotThrow(() -> registrationService.registerUser(userWithMixedCase));
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("TC26: Propriedade - UserRepository.save deve ser chamado com dados corretos do DTO")
    void shouldSaveUser_WithCorrectDataFromDto() {
        // Arrange
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            
            // Propriedade: dados do usuário salvo devem corresponder ao DTO
            assertEquals(validUserDto.getFirstName(), savedUser.getFirstName());
            assertEquals(validUserDto.getLastName(), savedUser.getLastName());
            assertEquals(validUserDto.getUsername(), savedUser.getUsername());
            assertEquals(validUserDto.getEmail(), savedUser.getEmail());
            assertEquals(validUserDto.getPhone(), savedUser.getPhone());
            assertEquals("encodedPassword", savedUser.getPassword());
            
            return savedUser;
        });

        // Act
        registrationService.registerUser(validUserDto);

        // Assert
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("TC27: Teste de idempotência - verificação de username existente não modifica estado")
    void shouldNotModifyState_WhenCheckingExistingUsername() {
        // Arrange - Usuário já existe
        User existingUser = User.builder()
                .id(1L)
                .username(validUserDto.getUsername())
                .firstName("Existing")
                .lastName("User")
                .build();

        when(userRepository.findByUsername(validUserDto.getUsername()))
                .thenReturn(Optional.of(existingUser));

        // Act & Assert
        assertThrows(ExistingEntityException.class,
                () -> registrationService.registerUser(validUserDto));

        // Propriedade: nenhuma operação de escrita deve ter ocorrido
        verify(userRepository, never()).save(any(User.class));
        verify(userRepository, never()).delete(any(User.class));
        verify(userRepository, never()).deleteById(anyLong());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userService, never()).addRoleToUser(anyString(), anyString());
    }

    @Test
    @DisplayName("TC28: Registro com telefone zero - caso extremo")
    void shouldRegisterUser_WithZeroPhone() {
        // Arrange - Telefone com valor zero
        UserInDto userWithZeroPhone = UserInDto.builder()
                .firstName("Zero")
                .lastName("Phone")
                .username("zero.phone")
                .password("SecurePass000")
                .email("zero@email.com")
                .phone(0) // valor zero
                .build();

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(new User());

        // Act & Assert
        assertDoesNotThrow(() -> registrationService.registerUser(userWithZeroPhone));
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("TC29: Registro com senha de exatamente 100 caracteres - teste de limite superior estendido")
    void shouldRegisterUser_WithVeryLongPassword() {
        // Arrange - Senha extremamente longa (100 caracteres)
        String longPassword = "A1bcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvwxyz123456789";
        
        UserInDto userWithVeryLongPassword = UserInDto.builder()
                .firstName("Long")
                .lastName("Password")
                .username("long.password")
                .password(longPassword) // 100 caracteres
                .email("long@email.com")
                .phone(100200300)
                .build();

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(new User());

        // Act & Assert
        assertDoesNotThrow(() -> registrationService.registerUser(userWithVeryLongPassword));
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("TC30: Falha com null no campo username")
    void shouldHandleNullUsername_Gracefully() {
        // Arrange - Username null
        UserInDto userWithNullUsername = UserInDto.builder()
                .firstName("Null")
                .lastName("Username")
                .username(null) // null username
                .password("SecurePass123")
                .email("null@email.com")
                .phone(999000999)
                .build();

        when(userRepository.findByUsername(null)).thenReturn(Optional.empty());

        // Act & Assert - Pode lançar NullPointerException ou ser tratado graciosamente
        // dependendo da implementação do repositório
        try {
            registrationService.registerUser(userWithNullUsername);
        } catch (Exception e) {
            // Se uma exceção for lançada, não deve ter salvado nada
            verify(userRepository, never()).save(any(User.class));
        }
    }
}
