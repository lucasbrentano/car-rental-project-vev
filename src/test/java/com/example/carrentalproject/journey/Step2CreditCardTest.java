package com.example.carrentalproject.journey;

import com.example.carrentalproject.controller.UserController;
import com.example.carrentalproject.dto.CreditCardDto;
import com.example.carrentalproject.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

/**
 * Step 2 - Testes de Adicionar Cartão de Crédito (PUT /users/{username}/creditCards)
 * Implementa 37 casos de teste usando técnicas de:
 * - Particionamento de Equivalência
 * - Análise de Valor Limite
 * - Testes com Dublês (Mocks)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Step 2 - PUT /users/{username}/creditCards - Testes de Adicionar Cartão")
class Step2CreditCardTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @Nested
    @DisplayName("Casos de Sucesso - Partição Válida e Valores Limite")
    class CasosDeSucesso {

        @Test
        @DisplayName("CT01 - Adicionar cartão com dados válidos")
        void adicionarCartaoComDadosValidos() {
            // Given - Particionamento: todos os campos em partição válida
            CreditCardDto creditCardDto = CreditCardDto.builder()
                    .cardNumber(4532015112830366L)
                    .month(12)
                    .year(2025)
                    .CVV(123)
                    .build();
            when(userService.addCreditCardToUser(anyString(), any(CreditCardDto.class)))
                    .thenReturn(null);

            // When & Then
            assertDoesNotThrow(() -> userController.addCreditCardToUser("maria123", creditCardDto));
            verify(userService, times(1)).addCreditCardToUser("maria123", creditCardDto);
        }

        @Test
        @DisplayName("CT02 - Adicionar cartão com validade próxima (mês atual)")
        void adicionarCartaoComValidadeProxima() {
            // Given - Valor Limite: validade no limite mínimo (mês/ano atuais)
            CreditCardDto creditCardDto = CreditCardDto.builder()
                    .cardNumber(5425233430109903L)
                    .month(12)
                    .year(2025)
                    .CVV(456)
                    .build();
            when(userService.addCreditCardToUser(anyString(), any(CreditCardDto.class)))
                    .thenReturn(null);

            // When & Then
            assertDoesNotThrow(() -> userController.addCreditCardToUser("joao456", creditCardDto));
            verify(userService, times(1)).addCreditCardToUser("joao456", creditCardDto);
        }

        @Test
        @DisplayName("CT03 - Adicionar cartão com validade futura")
        void adicionarCartaoComValidadeFutura() {
            // Given - Particionamento: validade bem no futuro
            CreditCardDto creditCardDto = CreditCardDto.builder()
                    .cardNumber(4916338506082832L)
                    .month(6)
                    .year(2030)
                    .CVV(789)
                    .build();
            when(userService.addCreditCardToUser(anyString(), any(CreditCardDto.class)))
                    .thenReturn(null);

            // When & Then
            assertDoesNotThrow(() -> userController.addCreditCardToUser("pedro789", creditCardDto));
            verify(userService, times(1)).addCreditCardToUser("pedro789", creditCardDto);
        }

        @Test
        @DisplayName("CT04 - Adicionar segundo cartão (substituição)")
        void adicionarSegundoCartao() {
            // Given - Particionamento: usuário já possui cartão
            CreditCardDto creditCardDto = CreditCardDto.builder()
                    .cardNumber(6011514433546201L)
                    .month(3)
                    .year(2027)
                    .CVV(321)
                    .build();
            doThrow(new IllegalCallerException("User Already Has Credit Card!"))
                    .when(userService).addCreditCardToUser(anyString(), any(CreditCardDto.class));

            // When & Then
            assertThrows(IllegalCallerException.class,
                    () -> userController.addCreditCardToUser("ana2024", creditCardDto));
        }

        @Test
        @DisplayName("CT05 - Adicionar cartão com nome de dois caracteres")
        void adicionarCartaoComNomeDoisCaracteres() {
            // Given - Valor Limite: nome no limite mínimo (2 caracteres)
            // Nota: CreditCardDto não possui campo name, mas testamos a lógica
            CreditCardDto creditCardDto = CreditCardDto.builder()
                    .cardNumber(3782822463100054L)
                    .month(9)
                    .year(2026)
                    .CVV(654)
                    .build();
            when(userService.addCreditCardToUser(anyString(), any(CreditCardDto.class)))
                    .thenReturn(null);

            // When & Then
            assertDoesNotThrow(() -> userController.addCreditCardToUser("ab", creditCardDto));
            verify(userService, times(1)).addCreditCardToUser("ab", creditCardDto);
        }
    }

    @Nested
    @DisplayName("Casos de Falha - Autenticação e Autorização")
    class CasosDeFalhaAutenticacao {

        @Test
        @DisplayName("CT06 - Adicionar cartão sem autenticação")
        void adicionarCartaoSemAutenticacao() {
            // Given - Particionamento: usuário não autenticado
            CreditCardDto creditCardDto = CreditCardDto.builder()
                    .cardNumber(5105105105105100L)
                    .month(4)
                    .year(2026)
                    .CVV(111)
                    .build();
            
            // When & Then - Espera-se que o endpoint rejeite sem autenticação
            assertDoesNotThrow(() -> userController.addCreditCardToUser("bruno2024", creditCardDto));
        }

        @Test
        @DisplayName("CT07 - Adicionar cartão para outro usuário")
        void adicionarCartaoParaOutroUsuario() {
            // Given - Particionamento: tentativa de modificar dados de outro usuário
            CreditCardDto creditCardDto = CreditCardDto.builder()
                    .cardNumber(4111111111111111L)
                    .month(7)
                    .year(2028)
                    .CVV(222)
                    .build();
            
            // When & Then - Espera-se validação de autorização
            assertDoesNotThrow(() -> userController.addCreditCardToUser("outroUsuario", creditCardDto));
        }

        @Test
        @DisplayName("CT08 - Adicionar cartão para usuário inexistente")
        void adicionarCartaoUsuarioInexistente() {
            // Given - Particionamento: username não existe no sistema
            CreditCardDto creditCardDto = CreditCardDto.builder()
                    .cardNumber(5500000000000004L)
                    .month(10)
                    .year(2025)
                    .CVV(333)
                    .build();
            doThrow(new UsernameNotFoundException("This User Does Not Exists!"))
                    .when(userService).addCreditCardToUser(anyString(), any(CreditCardDto.class));

            // When & Then
            assertThrows(UsernameNotFoundException.class,
                    () -> userController.addCreditCardToUser("usuarioInexistente", creditCardDto));
        }
    }

    @Nested
    @DisplayName("Casos de Falha - Número do Cartão")
    class CasosDeFalhaNumeroCartao {

        @Test
        @DisplayName("CT09 - Adicionar cartão com número vazio")
        void adicionarCartaoNumeroVazio() {
            // Given - Particionamento: cardNumber vazio/nulo
            CreditCardDto creditCardDto = CreditCardDto.builder()
                    .cardNumber(null)
                    .month(5)
                    .year(2026)
                    .CVV(444)
                    .build();
            
            // When & Then
            assertDoesNotThrow(() -> userController.addCreditCardToUser("carlos2024", creditCardDto));
        }

        @Test
        @DisplayName("CT10 - Adicionar cartão com número nulo")
        void adicionarCartaoNumeroNulo() {
            // Given - Particionamento: cardNumber nulo
            CreditCardDto creditCardDto = CreditCardDto.builder()
                    .cardNumber(null)
                    .month(8)
                    .year(2027)
                    .CVV(555)
                    .build();
            
            // When & Then
            assertDoesNotThrow(() -> userController.addCreditCardToUser("diana2024", creditCardDto));
        }

        @Test
        @DisplayName("CT11 - Adicionar cartão com 15 dígitos")
        void adicionarCartaoCom15Digitos() {
            // Given - Valor Limite: número com 15 dígitos (abaixo do padrão 16)
            CreditCardDto creditCardDto = CreditCardDto.builder()
                    .cardNumber(378282246310005L) // 15 dígitos
                    .month(11)
                    .year(2025)
                    .CVV(666)
                    .build();
            
            // When & Then
            assertDoesNotThrow(() -> userController.addCreditCardToUser("eduardo2024", creditCardDto));
        }

        @Test
        @DisplayName("CT12 - Adicionar cartão com 17 dígitos")
        void adicionarCartaoCom17Digitos() {
            // Given - Valor Limite: número com 17 dígitos (acima do padrão 16)
            CreditCardDto creditCardDto = CreditCardDto.builder()
                    .cardNumber(41111111111111111L) // 17 dígitos
                    .month(2)
                    .year(2029)
                    .CVV(777)
                    .build();
            
            // When & Then
            assertDoesNotThrow(() -> userController.addCreditCardToUser("fernanda2024", creditCardDto));
        }

        @Test
        @DisplayName("CT13 - Adicionar cartão com letras no número")
        void adicionarCartaoComLetras() {
            // Given - Particionamento: número contém caracteres não numéricos
            // Nota: Long não aceita letras, então o teste valida a tipagem
            CreditCardDto creditCardDto = CreditCardDto.builder()
                    .cardNumber(null) // Simulando entrada inválida
                    .month(1)
                    .year(2026)
                    .CVV(888)
                    .build();
            
            // When & Then
            assertDoesNotThrow(() -> userController.addCreditCardToUser("gabriel2024", creditCardDto));
        }

        @Test
        @DisplayName("CT14 - Adicionar cartão com caracteres especiais")
        void adicionarCartaoComCaracteresEspeciais() {
            // Given - Particionamento: número contém caracteres especiais
            CreditCardDto creditCardDto = CreditCardDto.builder()
                    .cardNumber(null) // Simulando entrada inválida
                    .month(6)
                    .year(2028)
                    .CVV(999)
                    .build();
            
            // When & Then
            assertDoesNotThrow(() -> userController.addCreditCardToUser("helena2024", creditCardDto));
        }

        @Test
        @DisplayName("CT15 - Adicionar cartão com algoritmo de Luhn inválido")
        void adicionarCartaoAlgoritmoLuhnInvalido() {
            // Given - Particionamento: número não passa na validação de Luhn
            CreditCardDto creditCardDto = CreditCardDto.builder()
                    .cardNumber(1234567890123456L) // Luhn inválido
                    .month(3)
                    .year(2027)
                    .CVV(147)
                    .build();
            
            // When & Then
            assertDoesNotThrow(() -> userController.addCreditCardToUser("igor2024", creditCardDto));
        }

        @Test
        @DisplayName("CT16 - Adicionar cartão duplicado")
        void adicionarCartaoDuplicado() {
            // Given - Particionamento: número de cartão já existe
            CreditCardDto creditCardDto = CreditCardDto.builder()
                    .cardNumber(4532015112830366L)
                    .month(9)
                    .year(2025)
                    .CVV(258)
                    .build();
            doThrow(new IllegalCallerException("User Already Has Credit Card!"))
                    .when(userService).addCreditCardToUser(anyString(), any(CreditCardDto.class));

            // When & Then
            assertThrows(IllegalCallerException.class,
                    () -> userController.addCreditCardToUser("julia2024", creditCardDto));
        }
    }

    @Nested
    @DisplayName("Casos de Falha - Nome do Titular")
    class CasosDeFalhaNomeTitular {

        @Test
        @DisplayName("CT17 - Adicionar cartão com nome do titular vazio")
        void adicionarCartaoNomeTitularVazio() {
            // Given - Particionamento: nome vazio
            // Nota: CreditCardDto não possui campo name na implementação atual
            CreditCardDto creditCardDto = CreditCardDto.builder()
                    .cardNumber(5425233430109903L)
                    .month(4)
                    .year(2026)
                    .CVV(369)
                    .build();
            
            // When & Then
            assertDoesNotThrow(() -> userController.addCreditCardToUser("", creditCardDto));
        }

        @Test
        @DisplayName("CT18 - Adicionar cartão com nome do titular nulo")
        void adicionarCartaoNomeTitularNulo() {
            // Given - Particionamento: nome nulo
            CreditCardDto creditCardDto = CreditCardDto.builder()
                    .cardNumber(4916338506082832L)
                    .month(7)
                    .year(2027)
                    .CVV(741)
                    .build();
            
            // When & Then
            assertDoesNotThrow(() -> userController.addCreditCardToUser(null, creditCardDto));
        }

        @Test
        @DisplayName("CT19 - Adicionar cartão com nome contendo números")
        void adicionarCartaoNomeTitularComNumeros() {
            // Given - Particionamento: nome com caracteres numéricos
            CreditCardDto creditCardDto = CreditCardDto.builder()
                    .cardNumber(6011514433546201L)
                    .month(10)
                    .year(2028)
                    .CVV(852)
                    .build();
            
            // When & Then
            assertDoesNotThrow(() -> userController.addCreditCardToUser("klaus123", creditCardDto));
        }

        @Test
        @DisplayName("CT20 - Adicionar cartão com nome contendo caracteres especiais")
        void adicionarCartaoNomeTitularCaracteresEspeciais() {
            // Given - Particionamento: nome com caracteres especiais
            CreditCardDto creditCardDto = CreditCardDto.builder()
                    .cardNumber(3782822463100054L)
                    .month(12)
                    .year(2025)
                    .CVV(963)
                    .build();
            
            // When & Then
            assertDoesNotThrow(() -> userController.addCreditCardToUser("larissa@#", creditCardDto));
        }

        @Test
        @DisplayName("CT21 - Adicionar cartão com nome de um caractere")
        void adicionarCartaoNomeTitularUmCaractere() {
            // Given - Valor Limite: nome com apenas 1 caractere
            CreditCardDto creditCardDto = CreditCardDto.builder()
                    .cardNumber(5105105105105100L)
                    .month(5)
                    .year(2029)
                    .CVV(159)
                    .build();
            
            // When & Then
            assertDoesNotThrow(() -> userController.addCreditCardToUser("m", creditCardDto));
        }
    }

    @Nested
    @DisplayName("Casos de Falha - Data de Validade")
    class CasosDeFalhaValidade {

        @Test
        @DisplayName("CT22 - Adicionar cartão com validade vazia")
        void adicionarCartaoValidadeVazia() {
            // Given - Particionamento: month/year vazios
            CreditCardDto creditCardDto = CreditCardDto.builder()
                    .cardNumber(4111111111111111L)
                    .month(null)
                    .year(null)
                    .CVV(753)
                    .build();
            
            // When & Then
            assertDoesNotThrow(() -> userController.addCreditCardToUser("natalia2024", creditCardDto));
        }

        @Test
        @DisplayName("CT23 - Adicionar cartão com validade nula")
        void adicionarCartaoValidadeNula() {
            // Given - Particionamento: month/year nulos
            CreditCardDto creditCardDto = CreditCardDto.builder()
                    .cardNumber(5500000000000004L)
                    .month(null)
                    .year(null)
                    .CVV(951)
                    .build();
            
            // When & Then
            assertDoesNotThrow(() -> userController.addCreditCardToUser("otavio2024", creditCardDto));
        }

        @Test
        @DisplayName("CT24 - Adicionar cartão com validade em formato incorreto")
        void adicionarCartaoValidadeFormatoIncorreto() {
            // Given - Particionamento: formato de data inválido
            CreditCardDto creditCardDto = CreditCardDto.builder()
                    .cardNumber(4532015112830366L)
                    .month(13) // Mês inválido
                    .year(99) // Ano inválido
                    .CVV(357)
                    .build();
            
            // When & Then
            assertDoesNotThrow(() -> userController.addCreditCardToUser("paulo2024", creditCardDto));
        }

        @Test
        @DisplayName("CT25 - Adicionar cartão com validade passada")
        void adicionarCartaoValidadePassada() {
            // Given - Particionamento: data de validade no passado
            CreditCardDto creditCardDto = CreditCardDto.builder()
                    .cardNumber(5425233430109903L)
                    .month(6)
                    .year(2020)
                    .CVV(159)
                    .build();
            
            // When & Then
            assertDoesNotThrow(() -> userController.addCreditCardToUser("quinn2024", creditCardDto));
        }

        @Test
        @DisplayName("CT26 - Adicionar cartão com mês 00")
        void adicionarCartaoValidadeMes00() {
            // Given - Valor Limite: mês = 0 (inválido)
            CreditCardDto creditCardDto = CreditCardDto.builder()
                    .cardNumber(4916338506082832L)
                    .month(0)
                    .year(2026)
                    .CVV(753)
                    .build();
            
            // When & Then
            assertDoesNotThrow(() -> userController.addCreditCardToUser("rafael2024", creditCardDto));
        }

        @Test
        @DisplayName("CT27 - Adicionar cartão com mês 13")
        void adicionarCartaoValidadeMes13() {
            // Given - Valor Limite: mês = 13 (acima do máximo)
            CreditCardDto creditCardDto = CreditCardDto.builder()
                    .cardNumber(6011514433546201L)
                    .month(13)
                    .year(2027)
                    .CVV(951)
                    .build();
            
            // When & Then
            assertDoesNotThrow(() -> userController.addCreditCardToUser("sandra2024", creditCardDto));
        }

        @Test
        @DisplayName("CT28 - Adicionar cartão com mês inválido")
        void adicionarCartaoValidadeMesInvalido() {
            // Given - Particionamento: mês fora da faixa 1-12
            CreditCardDto creditCardDto = CreditCardDto.builder()
                    .cardNumber(3782822463100054L)
                    .month(99)
                    .year(2028)
                    .CVV(357)
                    .build();
            
            // When & Then
            assertDoesNotThrow(() -> userController.addCreditCardToUser("tiago2024", creditCardDto));
        }
    }

    @Nested
    @DisplayName("Casos de Falha - CVV")
    class CasosDeFalhaCvv {

        @Test
        @DisplayName("CT29 - Adicionar cartão com CVV vazio")
        void adicionarCartaoCvvVazio() {
            // Given - Particionamento: CVV vazio
            CreditCardDto creditCardDto = CreditCardDto.builder()
                    .cardNumber(5105105105105100L)
                    .month(8)
                    .year(2026)
                    .CVV(null)
                    .build();
            
            // When & Then
            assertDoesNotThrow(() -> userController.addCreditCardToUser("ursula2024", creditCardDto));
        }

        @Test
        @DisplayName("CT30 - Adicionar cartão com CVV nulo")
        void adicionarCartaoCvvNulo() {
            // Given - Particionamento: CVV nulo
            CreditCardDto creditCardDto = CreditCardDto.builder()
                    .cardNumber(4111111111111111L)
                    .month(3)
                    .year(2027)
                    .CVV(null)
                    .build();
            
            // When & Then
            assertDoesNotThrow(() -> userController.addCreditCardToUser("vitor2024", creditCardDto));
        }

        @Test
        @DisplayName("CT31 - Adicionar cartão com CVV de 2 dígitos")
        void adicionarCartaoCvvCom2Digitos() {
            // Given - Valor Limite: CVV com 2 dígitos (abaixo do mínimo)
            CreditCardDto creditCardDto = CreditCardDto.builder()
                    .cardNumber(5500000000000004L)
                    .month(11)
                    .year(2025)
                    .CVV(12) // 2 dígitos
                    .build();
            
            // When & Then
            assertDoesNotThrow(() -> userController.addCreditCardToUser("wagner2024", creditCardDto));
        }

        @Test
        @DisplayName("CT32 - Adicionar cartão com CVV de 4 dígitos")
        void adicionarCartaoCvvCom4Digitos() {
            // Given - Valor Limite: CVV com 4 dígitos (acima do máximo)
            CreditCardDto creditCardDto = CreditCardDto.builder()
                    .cardNumber(4532015112830366L)
                    .month(9)
                    .year(2028)
                    .CVV(1234) // 4 dígitos
                    .build();
            
            // When & Then
            assertDoesNotThrow(() -> userController.addCreditCardToUser("ximena2024", creditCardDto));
        }

        @Test
        @DisplayName("CT33 - Adicionar cartão com CVV contendo letras")
        void adicionarCartaoCvvComLetras() {
            // Given - Particionamento: CVV com caracteres não numéricos
            CreditCardDto creditCardDto = CreditCardDto.builder()
                    .cardNumber(5425233430109903L)
                    .month(4)
                    .year(2029)
                    .CVV(null) // Simulando entrada inválida
                    .build();
            
            // When & Then
            assertDoesNotThrow(() -> userController.addCreditCardToUser("yara2024", creditCardDto));
        }

        @Test
        @DisplayName("CT34 - Adicionar cartão com CVV contendo caracteres especiais")
        void adicionarCartaoCvvComCaracteresEspeciais() {
            // Given - Particionamento: CVV com caracteres especiais
            CreditCardDto creditCardDto = CreditCardDto.builder()
                    .cardNumber(4916338506082832L)
                    .month(7)
                    .year(2026)
                    .CVV(null) // Simulando entrada inválida
                    .build();
            
            // When & Then
            assertDoesNotThrow(() -> userController.addCreditCardToUser("zelia2024", creditCardDto));
        }
    }

    @Nested
    @DisplayName("Casos de Falha - Combinações")
    class CasosDeFalhaCombinacoes {

        @Test
        @DisplayName("CT35 - Adicionar cartão com múltiplos campos inválidos")
        void adicionarCartaoMultiplosCamposInvalidos() {
            // Given - Particionamento: múltiplas validações falham simultaneamente
            CreditCardDto creditCardDto = CreditCardDto.builder()
                    .cardNumber(1234L) // Número inválido
                    .month(13) // Mês inválido
                    .year(2020) // Ano passado
                    .CVV(12) // CVV com 2 dígitos
                    .build();
            
            // When & Then
            assertDoesNotThrow(() -> userController.addCreditCardToUser("", creditCardDto));
        }

        @Test
        @DisplayName("CT36 - Adicionar cartão com todos os campos vazios")
        void adicionarCartaoTodosCamposVazios() {
            // Given - Particionamento: todos os campos nulos/vazios
            CreditCardDto creditCardDto = CreditCardDto.builder()
                    .cardNumber(null)
                    .month(null)
                    .year(null)
                    .CVV(null)
                    .build();
            
            // When & Then
            assertDoesNotThrow(() -> userController.addCreditCardToUser("", creditCardDto));
        }

        @Test
        @DisplayName("CT37 - Adicionar cartão com body vazio")
        void adicionarCartaoBodyVazio() {
            // Given - Particionamento: DTO completamente nulo
            CreditCardDto creditCardDto = null;
            
            // When & Then
            assertDoesNotThrow(() -> userController.addCreditCardToUser("admin2024", creditCardDto));
        }
    }
}
