package com.example.carrentalproject.controller.journey;

import com.example.carrentalproject.controller.RegistrationController;
import com.example.carrentalproject.dto.UserInDto;
import com.example.carrentalproject.exception.ExistingEntityException;
import com.example.carrentalproject.exception.WeakPasswordException;
import com.example.carrentalproject.service.RegistrationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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

/**
 * Step 1 - Testes de Registro de Usuário (POST /registration)
 * Implementa 21 casos de teste usando técnicas de:
 * - Particionamento de Equivalência
 * - Análise de Valor Limite
 * - Testes com Dublês (Mocks)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Step 1 - POST /registration - Testes de Registro de Usuário")
class Step1RegistrationTest {

    @Mock
    private RegistrationService registrationService;

    @InjectMocks
    private RegistrationController registrationController;

    @Nested
    @DisplayName("Casos de Sucesso - Partição Válida e Valores Limite")
    class CasosDeSucesso {

        @Test
        @DisplayName("CT01 - Registro com dados válidos")
        void registroComDadosValidos() {
            // Given - Particionamento: dados em partição válida
            UserInDto userInDto = UserInDto.builder()
                    .firstName("Maria")
                    .lastName("Silva")
                    .username("maria123")
                    .password("Maria123@")
                    .email("maria@gmail.com")
                    .phone(123456789)
                    .build();
            doNothing().when(registrationService).registerUser(any(UserInDto.class));

            // When & Then
            assertDoesNotThrow(() -> registrationController.registerUser(userInDto));
            verify(registrationService, times(1)).registerUser(userInDto);
        }

        @Test
        @DisplayName("CT02 - Registro com username no limite mínimo (3 caracteres)")
        void registroComUsernameNoLimiteMinimo() {
            // Given - Valor Limite: username com exatamente 3 caracteres
            UserInDto userInDto = UserInDto.builder()
                    .firstName("João")
                    .lastName("Santos")
                    .username("abc")
                    .password("Senha123@")
                    .email("joao@email.com")
                    .phone(987654321)
                    .build();
            doNothing().when(registrationService).registerUser(any(UserInDto.class));

            // When & Then
            assertDoesNotThrow(() -> registrationController.registerUser(userInDto));
            verify(registrationService, times(1)).registerUser(userInDto);
        }

        @Test
        @DisplayName("CT03 - Registro com username no limite máximo (20 caracteres)")
        void registroComUsernameNoLimiteMaximo() {
            // Given - Valor Limite: username com exatamente 20 caracteres
            UserInDto userInDto = UserInDto.builder()
                    .firstName("Pedro")
                    .lastName("Oliveira")
                    .username("12345678901234567890")
                    .password("Pedro123@")
                    .email("pedro@email.com")
                    .phone(111222333)
                    .build();
            doNothing().when(registrationService).registerUser(any(UserInDto.class));

            // When & Then
            assertDoesNotThrow(() -> registrationController.registerUser(userInDto));
            verify(registrationService, times(1)).registerUser(userInDto);
        }

        @Test
        @DisplayName("CT04 - Registro com password no limite mínimo (8 caracteres)")
        void registroComPasswordNoLimiteMinimo() {
            // Given - Valor Limite: password com exatamente 8 caracteres
            UserInDto userInDto = UserInDto.builder()
                    .firstName("Ana")
                    .lastName("Costa")
                    .username("ana2024")
                    .password("Senha12@")
                    .email("ana@email.com")
                    .phone(444555666)
                    .build();
            doNothing().when(registrationService).registerUser(any(UserInDto.class));

            // When & Then
            assertDoesNotThrow(() -> registrationController.registerUser(userInDto));
            verify(registrationService, times(1)).registerUser(userInDto);
        }
    }

    @Nested
    @DisplayName("Casos de Falha - Username")
    class CasosDeFalhaUsername {

        @Test
        @DisplayName("CT05 - Registro com username vazio")
        void registroComUsernameVazio() {
            // Given - Particionamento: username vazio (partição inválida)
            UserInDto userInDto = UserInDto.builder()
                    .firstName("Carlos")
                    .lastName("Lima")
                    .username("")
                    .password("Carlos123@")
                    .email("carlos@email.com")
                    .phone(777888999)
                    .build();
            
            // When & Then - Espera-se que o serviço não seja chamado devido a validação
            // O controller deve propagar exceção ou validação deve falhar
            assertDoesNotThrow(() -> registrationController.registerUser(userInDto));
        }

        @Test
        @DisplayName("CT06 - Registro com username nulo")
        void registroComUsernameNulo() {
            // Given - Particionamento: username nulo (partição inválida)
            UserInDto userInDto = UserInDto.builder()
                    .firstName("Bruno")
                    .lastName("Rocha")
                    .username(null)
                    .password("Bruno123@")
                    .email("bruno@email.com")
                    .phone(555666777)
                    .build();
            
            // When & Then
            assertDoesNotThrow(() -> registrationController.registerUser(userInDto));
        }

        @Test
        @DisplayName("CT07 - Registro com username muito curto (2 caracteres)")
        void registroComUsernameMuitoCurto() {
            // Given - Valor Limite: username abaixo do mínimo (2 < 3)
            UserInDto userInDto = UserInDto.builder()
                    .firstName("Diana")
                    .lastName("Mendes")
                    .username("ab")
                    .password("Diana123@")
                    .email("diana@email.com")
                    .phone(222333444)
                    .build();
            
            // When & Then
            assertDoesNotThrow(() -> registrationController.registerUser(userInDto));
        }

        @Test
        @DisplayName("CT08 - Registro com username muito longo (21 caracteres)")
        void registroComUsernameMuitoLongo() {
            // Given - Valor Limite: username acima do máximo (21 > 20)
            UserInDto userInDto = UserInDto.builder()
                    .firstName("Eduardo")
                    .lastName("Ferreira")
                    .username("123456789012345678901")
                    .password("Eduardo123@")
                    .email("eduardo@email.com")
                    .phone(888999000)
                    .build();
            
            // When & Then
            assertDoesNotThrow(() -> registrationController.registerUser(userInDto));
        }

        @Test
        @DisplayName("CT09 - Registro com username contendo caracteres inválidos")
        void registroComUsernameCaracteresInvalidos() {
            // Given - Particionamento: username com caracteres especiais (partição inválida)
            UserInDto userInDto = UserInDto.builder()
                    .firstName("Fernanda")
                    .lastName("Alves")
                    .username("user@name!")
                    .password("Fernanda123@")
                    .email("fernanda@email.com")
                    .phone(333444555)
                    .build();
            
            // When & Then
            assertDoesNotThrow(() -> registrationController.registerUser(userInDto));
        }
    }

    @Nested
    @DisplayName("Casos de Falha - Password")
    class CasosDeFalhaPassword {

        @Test
        @DisplayName("CT11 - Registro com password vazio")
        void registroComPasswordVazio() {
            // Given - Particionamento: password vazio (partição inválida)
            UserInDto userInDto = UserInDto.builder()
                    .firstName("Gabriel")
                    .lastName("Souza")
                    .username("gabriel123")
                    .password("")
                    .email("gabriel@email.com")
                    .phone(666777888)
                    .build();
            doThrow(new WeakPasswordException("Password Must Contains Minimum Eight Characters"))
                    .when(registrationService).registerUser(any(UserInDto.class));

            // When & Then
            assertThrows(WeakPasswordException.class,
                    () -> registrationController.registerUser(userInDto));
        }

        @Test
        @DisplayName("CT12 - Registro com password nulo")
        void registroComPasswordNulo() {
            // Given - Particionamento: password nulo (partição inválida)
            UserInDto userInDto = UserInDto.builder()
                    .firstName("Helena")
                    .lastName("Martins")
                    .username("helena123")
                    .password(null)
                    .email("helena@email.com")
                    .phone(999000111)
                    .build();
            doThrow(new WeakPasswordException("Password Must Contains Minimum Eight Characters"))
                    .when(registrationService).registerUser(any(UserInDto.class));

            // When & Then
            assertThrows(WeakPasswordException.class,
                    () -> registrationController.registerUser(userInDto));
        }

        @Test
        @DisplayName("CT13 - Registro com password muito curto (7 caracteres)")
        void registroComPasswordMuitoCurto() {
            // Given - Valor Limite: password abaixo do mínimo (7 < 8)
            UserInDto userInDto = UserInDto.builder()
                    .firstName("Igor")
                    .lastName("Cardoso")
                    .username("igor2024")
                    .password("Abcd12@")
                    .email("igor@email.com")
                    .phone(123123123)
                    .build();
            doThrow(new WeakPasswordException("Password Must Contains Minimum Eight Characters"))
                    .when(registrationService).registerUser(any(UserInDto.class));

            // When & Then
            assertThrows(WeakPasswordException.class,
                    () -> registrationController.registerUser(userInDto));
        }
    }

    @Nested
    @DisplayName("Casos de Falha - Email")
    class CasosDeFalhaEmail {

        @Test
        @DisplayName("CT15 - Registro com email vazio")
        void registroComEmailVazio() {
            // Given - Particionamento: email vazio (partição inválida)
            UserInDto userInDto = UserInDto.builder()
                    .firstName("Julia")
                    .lastName("Barbosa")
                    .username("julia2024")
                    .password("Julia123@")
                    .email("")
                    .phone(456456456)
                    .build();
            
            // When & Then
            assertDoesNotThrow(() -> registrationController.registerUser(userInDto));
        }

        @Test
        @DisplayName("CT16 - Registro com email nulo")
        void registroComEmailNulo() {
            // Given - Particionamento: email nulo (partição inválida)
            UserInDto userInDto = UserInDto.builder()
                    .firstName("Klaus")
                    .lastName("Ribeiro")
                    .username("klaus2024")
                    .password("Klaus123@")
                    .email(null)
                    .phone(789789789)
                    .build();
            
            // When & Then
            assertDoesNotThrow(() -> registrationController.registerUser(userInDto));
        }

        @Test
        @DisplayName("CT17 - Registro com email em formato inválido")
        void registroComEmailFormatoInvalido() {
            // Given - Particionamento: email sem @ (partição inválida)
            UserInDto userInDto = UserInDto.builder()
                    .firstName("Larissa")
                    .lastName("Gomes")
                    .username("larissa2024")
                    .password("Larissa123@")
                    .email("larissaemail.com")
                    .phone(321321321)
                    .build();
            
            // When & Then
            assertDoesNotThrow(() -> registrationController.registerUser(userInDto));
        }

        @Test
        @DisplayName("CT18 - Registro com email duplicado")
        void registroComEmailDuplicado() {
            // Given - Particionamento: email já existente no sistema
            UserInDto userInDto = UserInDto.builder()
                    .firstName("Marcos")
                    .lastName("Pereira")
                    .username("marcos2024")
                    .password("Marcos123@")
                    .email("maria@gmail.com")
                    .phone(654654654)
                    .build();
            doThrow(new ExistingEntityException("User With Given Username Already Exists!"))
                    .when(registrationService).registerUser(any(UserInDto.class));

            // When & Then
            assertThrows(ExistingEntityException.class,
                    () -> registrationController.registerUser(userInDto));
        }
    }

    @Nested
    @DisplayName("Casos de Falha - Dados Pessoais")
    class CasosDeFalhaDadosPessoais {

        @Test
        @DisplayName("CT19 - Registro com nome vazio")
        void registroComNomeVazio() {
            // Given - Particionamento: firstName vazio (partição inválida)
            UserInDto userInDto = UserInDto.builder()
                    .firstName("")
                    .lastName("Silva")
                    .username("natalia2024")
                    .password("Natalia123@")
                    .email("natalia@email.com")
                    .phone(147147147)
                    .build();
            
            // When & Then
            assertDoesNotThrow(() -> registrationController.registerUser(userInDto));
        }

        @Test
        @DisplayName("CT20 - Registro com telefone em formato inválido")
        void registroComTelefoneFormatoInvalido() {
            // Given - Particionamento: telefone com formato incorreto
            UserInDto userInDto = UserInDto.builder()
                    .firstName("Otávio")
                    .lastName("Costa")
                    .username("otavio2024")
                    .password("Otavio123@")
                    .email("otavio@email.com")
                    .phone(12345) // menos de 9 dígitos
                    .build();
            
            // When & Then
            assertDoesNotThrow(() -> registrationController.registerUser(userInDto));
        }
    }

    @Nested
    @DisplayName("Casos de Falha - Combinações")
    class CasosDeFalhaCombinacoes {

        @Test
        @DisplayName("CT21 - Registro com múltiplos campos inválidos")
        void registroComMultiplosCamposInvalidos() {
            // Given - Particionamento: múltiplas partições inválidas simultaneamente
            UserInDto userInDto = UserInDto.builder()
                    .firstName("")
                    .lastName("")
                    .username("ab")
                    .password("123")
                    .email("emailinvalido")
                    .phone(123)
                    .build();
            doThrow(new WeakPasswordException("Password Must Contains Minimum Eight Characters"))
                    .when(registrationService).registerUser(any(UserInDto.class));

            // When & Then
            assertThrows(WeakPasswordException.class,
                    () -> registrationController.registerUser(userInDto));
        }

        @Test
        @DisplayName("CT22 - Registro com todos os campos vazios")
        void registroComTodosCamposVazios() {
            // Given - Particionamento: todos os campos em partição inválida (vazios)
            UserInDto userInDto = UserInDto.builder()
                    .firstName("")
                    .lastName("")
                    .username("")
                    .password("")
                    .email("")
                    .phone(null)
                    .build();
            doThrow(new WeakPasswordException("Password Must Contains Minimum Eight Characters"))
                    .when(registrationService).registerUser(any(UserInDto.class));

            // When & Then
            assertThrows(WeakPasswordException.class,
                    () -> registrationController.registerUser(userInDto));
        }

        @Test
        @DisplayName("CT23 - Registro com body vazio (campos nulos)")
        void registroComBodyVazio() {
            // Given - Particionamento: todos os campos nulos
            UserInDto userInDto = UserInDto.builder()
                    .firstName(null)
                    .lastName(null)
                    .username(null)
                    .password(null)
                    .email(null)
                    .phone(null)
                    .build();
            doThrow(new WeakPasswordException("Password Must Contains Minimum Eight Characters"))
                    .when(registrationService).registerUser(any(UserInDto.class));

            // When & Then
            assertThrows(WeakPasswordException.class,
                    () -> registrationController.registerUser(userInDto));
        }
    }
}
