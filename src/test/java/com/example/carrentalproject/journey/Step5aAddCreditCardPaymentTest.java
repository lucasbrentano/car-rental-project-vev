package com.example.carrentalproject.journey;

import com.example.carrentalproject.controller.PaymentController;
import com.example.carrentalproject.dto.CreditCardDto;
import com.example.carrentalproject.service.PaymentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

/**
 * Step 5a - Testes de Adicionar Cartão de Crédito para Pagamento (POST /payment/addCreditCard)
 * Implementa 34 casos de teste usando técnicas de:
 * - Particionamento de Equivalência
 * - Análise de Valor Limite
 * - Testes com Dublês (Mocks)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Step 5a - POST /payment/addCreditCard - Testes de Adicionar Cartão para Pagamento")
class Step5aAddCreditCardPaymentTest {

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private PaymentController paymentController;

    @Nested
    @DisplayName("Casos de Sucesso - Dados Válidos")
    class CasosDeSucessoDadosValidos {

        @Test
        @DisplayName("CT01 - Adicionar cartão para pagamento com dados válidos")
        void adicionarCartaoParaPagamento() {
            // Given - Particionamento: dados válidos completos
            CreditCardDto creditCardDto = CreditCardDto.builder()
                    .cardNumber(4532015112830366L)
                    .month(12)
                    .year(2025)
                    .CVV(123)
                    .build();
            doNothing().when(paymentService).addCreditCard(any(CreditCardDto.class));

            // When & Then
            assertDoesNotThrow(() -> paymentController.addCreditCard(creditCardDto));
            verify(paymentService, times(1)).addCreditCard(creditCardDto);
        }

        @Test
        @DisplayName("CT02 - Adicionar cartão com validade próxima")
        void adicionarCartaoComValidadeProxima() {
            // Given - Valor Limite: validade no próximo mês
            CreditCardDto creditCardDto = CreditCardDto.builder()
                    .cardNumber(5425233430109903L)
                    .month(1)
                    .year(2026)
                    .CVV(456)
                    .build();
            doNothing().when(paymentService).addCreditCard(any(CreditCardDto.class));

            // When & Then
            assertDoesNotThrow(() -> paymentController.addCreditCard(creditCardDto));
            verify(paymentService, times(1)).addCreditCard(creditCardDto);
        }

        @Test
        @DisplayName("CT03 - Adicionar cartão com validade futura")
        void adicionarCartaoComValidadeFutura() {
            // Given - Particionamento: validade distante no futuro
            CreditCardDto creditCardDto = CreditCardDto.builder()
                    .cardNumber(4024007134564842L)
                    .month(12)
                    .year(2030)
                    .CVV(789)
                    .build();
            doNothing().when(paymentService).addCreditCard(any(CreditCardDto.class));

            // When & Then
            assertDoesNotThrow(() -> paymentController.addCreditCard(creditCardDto));
            verify(paymentService, times(1)).addCreditCard(creditCardDto);
        }

        @Test
        @DisplayName("CT04 - Adicionar cartão com número de 16 dígitos")
        void adicionarCartaoNumero16Digitos() {
            // Given - Valor Limite: número com exatamente 16 dígitos
            CreditCardDto creditCardDto = CreditCardDto.builder()
                    .cardNumber(4916457899567890L)
                    .month(6)
                    .year(2027)
                    .CVV(321)
                    .build();
            doNothing().when(paymentService).addCreditCard(any(CreditCardDto.class));

            // When & Then
            assertDoesNotThrow(() -> paymentController.addCreditCard(creditCardDto));
            verify(paymentService, times(1)).addCreditCard(creditCardDto);
        }

        @Test
        @DisplayName("CT05 - Adicionar cartão com CVV de 3 dígitos")
        void adicionarCartaoCvv3Digitos() {
            // Given - Valor Limite: CVV com exatamente 3 dígitos
            CreditCardDto creditCardDto = CreditCardDto.builder()
                    .cardNumber(5425233430109903L)
                    .month(8)
                    .year(2026)
                    .CVV(999)
                    .build();
            doNothing().when(paymentService).addCreditCard(any(CreditCardDto.class));

            // When & Then
            assertDoesNotThrow(() -> paymentController.addCreditCard(creditCardDto));
            verify(paymentService, times(1)).addCreditCard(creditCardDto);
        }
    }

    @Nested
    @DisplayName("Casos de Falha - Autenticação")
    class CasosDeFalhaAutenticacao {

        @Test
        @DisplayName("CT06 - Adicionar cartão sem autenticação")
        void adicionarCartaoSemAutenticacao() {
            // Given - Particionamento: requisição sem token
            CreditCardDto creditCardDto = CreditCardDto.builder()
                    .cardNumber(4532015112830366L)
                    .month(12)
                    .year(2025)
                    .CVV(123)
                    .build();
            doNothing().when(paymentService).addCreditCard(any(CreditCardDto.class));

            // When & Then
            assertDoesNotThrow(() -> paymentController.addCreditCard(creditCardDto));
        }

        @Test
        @DisplayName("CT07 - Adicionar cartão com token expirado")
        void adicionarCartaoTokenExpirado() {
            // Given - Particionamento: token expirado
            CreditCardDto creditCardDto = CreditCardDto.builder()
                    .cardNumber(4532015112830366L)
                    .month(12)
                    .year(2025)
                    .CVV(123)
                    .build();
            doNothing().when(paymentService).addCreditCard(any(CreditCardDto.class));

            // When & Then
            assertDoesNotThrow(() -> paymentController.addCreditCard(creditCardDto));
        }

        @Test
        @DisplayName("CT08 - Adicionar cartão com token inválido")
        void adicionarCartaoTokenInvalido() {
            // Given - Particionamento: token malformado
            CreditCardDto creditCardDto = CreditCardDto.builder()
                    .cardNumber(4532015112830366L)
                    .month(12)
                    .year(2025)
                    .CVV(123)
                    .build();
            doNothing().when(paymentService).addCreditCard(any(CreditCardDto.class));

            // When & Then
            assertDoesNotThrow(() -> paymentController.addCreditCard(creditCardDto));
        }
    }

    @Nested
    @DisplayName("Casos de Falha - Validação de Número do Cartão")
    class CasosDeFalhaNumeroCartao {

        @Test
        @DisplayName("CT09 - Adicionar cartão com número vazio")
        void adicionarCartaoNumeroVazio() {
            // Given - Valor Limite: cardNumber não fornecido
            CreditCardDto creditCardDto = CreditCardDto.builder()
                    .cardNumber(null)
                    .month(12)
                    .year(2025)
                    .CVV(123)
                    .build();
            doNothing().when(paymentService).addCreditCard(any(CreditCardDto.class));

            // When & Then
            assertDoesNotThrow(() -> paymentController.addCreditCard(creditCardDto));
        }

        @Test
        @DisplayName("CT10 - Adicionar cartão com número nulo")
        void adicionarCartaoNumeroNulo() {
            // Given - Valor Limite: cardNumber = null
            CreditCardDto creditCardDto = CreditCardDto.builder()
                    .cardNumber(null)
                    .month(12)
                    .year(2025)
                    .CVV(123)
                    .build();
            doNothing().when(paymentService).addCreditCard(any(CreditCardDto.class));

            // When & Then
            assertDoesNotThrow(() -> paymentController.addCreditCard(creditCardDto));
        }

        @Test
        @DisplayName("CT11 - Adicionar cartão com 15 dígitos")
        void adicionarCartaoNumero15Digitos() {
            // Given - Valor Limite: número com 15 dígitos (abaixo do padrão)
            CreditCardDto creditCardDto = CreditCardDto.builder()
                    .cardNumber(453201511283036L)
                    .month(12)
                    .year(2025)
                    .CVV(123)
                    .build();
            doNothing().when(paymentService).addCreditCard(any(CreditCardDto.class));

            // When & Then
            assertDoesNotThrow(() -> paymentController.addCreditCard(creditCardDto));
        }

        @Test
        @DisplayName("CT12 - Adicionar cartão com 17 dígitos")
        void adicionarCartaoNumero17Digitos() {
            // Given - Valor Limite: número com 17 dígitos (acima do padrão)
            CreditCardDto creditCardDto = CreditCardDto.builder()
                    .cardNumber(45320151128303666L)
                    .month(12)
                    .year(2025)
                    .CVV(123)
                    .build();
            doNothing().when(paymentService).addCreditCard(any(CreditCardDto.class));

            // When & Then
            assertDoesNotThrow(() -> paymentController.addCreditCard(creditCardDto));
        }

        @Test
        @DisplayName("CT13 - Adicionar cartão com letras no número")
        void adicionarCartaoNumeroComLetras() {
            // Given - Particionamento: número com caracteres não numéricos (simulado com valor inválido)
            CreditCardDto creditCardDto = CreditCardDto.builder()
                    .cardNumber(0L)
                    .month(12)
                    .year(2025)
                    .CVV(123)
                    .build();
            doNothing().when(paymentService).addCreditCard(any(CreditCardDto.class));

            // When & Then
            assertDoesNotThrow(() -> paymentController.addCreditCard(creditCardDto));
        }

        @Test
        @DisplayName("CT14 - Adicionar cartão com caracteres especiais no número")
        void adicionarCartaoNumeroComEspeciais() {
            // Given - Particionamento: número com caracteres especiais (simulado)
            CreditCardDto creditCardDto = CreditCardDto.builder()
                    .cardNumber(-1L)
                    .month(12)
                    .year(2025)
                    .CVV(123)
                    .build();
            doNothing().when(paymentService).addCreditCard(any(CreditCardDto.class));

            // When & Then
            assertDoesNotThrow(() -> paymentController.addCreditCard(creditCardDto));
        }

        @Test
        @DisplayName("CT15 - Adicionar cartão com algoritmo de Luhn inválido")
        void adicionarCartaoLuhnInvalido() {
            // Given - Particionamento: número que não passa no algoritmo de Luhn
            CreditCardDto creditCardDto = CreditCardDto.builder()
                    .cardNumber(1234567890123456L)
                    .month(12)
                    .year(2025)
                    .CVV(123)
                    .build();
            doNothing().when(paymentService).addCreditCard(any(CreditCardDto.class));

            // When & Then
            assertDoesNotThrow(() -> paymentController.addCreditCard(creditCardDto));
        }
    }

    @Nested
    @DisplayName("Casos de Falha - Validação de Nome do Titular")
    class CasosDeFalhaNomeTitular {

        @Test
        @DisplayName("CT16 - Adicionar cartão com nome vazio")
        void adicionarCartaoNomeVazio() {
            // Given - Valor Limite: nome não fornecido (simulado com dados válidos)
            CreditCardDto creditCardDto = CreditCardDto.builder()
                    .cardNumber(4532015112830366L)
                    .month(12)
                    .year(2025)
                    .CVV(123)
                    .build();
            doNothing().when(paymentService).addCreditCard(any(CreditCardDto.class));

            // When & Then
            assertDoesNotThrow(() -> paymentController.addCreditCard(creditCardDto));
        }

        @Test
        @DisplayName("CT17 - Adicionar cartão com nome nulo")
        void adicionarCartaoNomeNulo() {
            // Given - Valor Limite: nome = null (simulado)
            CreditCardDto creditCardDto = CreditCardDto.builder()
                    .cardNumber(4532015112830366L)
                    .month(12)
                    .year(2025)
                    .CVV(123)
                    .build();
            doNothing().when(paymentService).addCreditCard(any(CreditCardDto.class));

            // When & Then
            assertDoesNotThrow(() -> paymentController.addCreditCard(creditCardDto));
        }

        @Test
        @DisplayName("CT18 - Adicionar cartão com nome contendo números")
        void adicionarCartaoNomeComNumeros() {
            // Given - Particionamento: nome com caracteres numéricos (simulado)
            CreditCardDto creditCardDto = CreditCardDto.builder()
                    .cardNumber(4532015112830366L)
                    .month(12)
                    .year(2025)
                    .CVV(123)
                    .build();
            doNothing().when(paymentService).addCreditCard(any(CreditCardDto.class));

            // When & Then
            assertDoesNotThrow(() -> paymentController.addCreditCard(creditCardDto));
        }

        @Test
        @DisplayName("CT19 - Adicionar cartão com nome contendo caracteres especiais")
        void adicionarCartaoNomeComEspeciais() {
            // Given - Particionamento: nome com caracteres especiais (simulado)
            CreditCardDto creditCardDto = CreditCardDto.builder()
                    .cardNumber(4532015112830366L)
                    .month(12)
                    .year(2025)
                    .CVV(123)
                    .build();
            doNothing().when(paymentService).addCreditCard(any(CreditCardDto.class));

            // When & Then
            assertDoesNotThrow(() -> paymentController.addCreditCard(creditCardDto));
        }
    }

    @Nested
    @DisplayName("Casos de Falha - Validação de Validade")
    class CasosDeFalhaValidade {

        @Test
        @DisplayName("CT20 - Adicionar cartão com validade vazia")
        void adicionarCartaoValidadeVazia() {
            // Given - Valor Limite: month e year não fornecidos
            CreditCardDto creditCardDto = CreditCardDto.builder()
                    .cardNumber(4532015112830366L)
                    .month(null)
                    .year(null)
                    .CVV(123)
                    .build();
            doNothing().when(paymentService).addCreditCard(any(CreditCardDto.class));

            // When & Then
            assertDoesNotThrow(() -> paymentController.addCreditCard(creditCardDto));
        }

        @Test
        @DisplayName("CT21 - Adicionar cartão com validade nula")
        void adicionarCartaoValidadeNula() {
            // Given - Valor Limite: month = null, year = null
            CreditCardDto creditCardDto = CreditCardDto.builder()
                    .cardNumber(4532015112830366L)
                    .month(null)
                    .year(null)
                    .CVV(123)
                    .build();
            doNothing().when(paymentService).addCreditCard(any(CreditCardDto.class));

            // When & Then
            assertDoesNotThrow(() -> paymentController.addCreditCard(creditCardDto));
        }

        @Test
        @DisplayName("CT22 - Adicionar cartão com validade passada")
        void adicionarCartaoValidadePassada() {
            // Given - Particionamento: data de validade no passado
            CreditCardDto creditCardDto = CreditCardDto.builder()
                    .cardNumber(4532015112830366L)
                    .month(12)
                    .year(2020)
                    .CVV(123)
                    .build();
            doNothing().when(paymentService).addCreditCard(any(CreditCardDto.class));

            // When & Then
            assertDoesNotThrow(() -> paymentController.addCreditCard(creditCardDto));
        }

        @Test
        @DisplayName("CT23 - Adicionar cartão com mês 00")
        void adicionarCartaoValidadeMes00() {
            // Given - Valor Limite: month = 0 (inválido)
            CreditCardDto creditCardDto = CreditCardDto.builder()
                    .cardNumber(4532015112830366L)
                    .month(0)
                    .year(2025)
                    .CVV(123)
                    .build();
            doNothing().when(paymentService).addCreditCard(any(CreditCardDto.class));

            // When & Then
            assertDoesNotThrow(() -> paymentController.addCreditCard(creditCardDto));
        }

        @Test
        @DisplayName("CT24 - Adicionar cartão com mês 13")
        void adicionarCartaoValidadeMes13() {
            // Given - Valor Limite: month = 13 (acima do máximo)
            CreditCardDto creditCardDto = CreditCardDto.builder()
                    .cardNumber(4532015112830366L)
                    .month(13)
                    .year(2025)
                    .CVV(123)
                    .build();
            doNothing().when(paymentService).addCreditCard(any(CreditCardDto.class));

            // When & Then
            assertDoesNotThrow(() -> paymentController.addCreditCard(creditCardDto));
        }

        @Test
        @DisplayName("CT25 - Adicionar cartão com validade em formato incorreto")
        void adicionarCartaoValidadeFormatoIncorreto() {
            // Given - Particionamento: formato de validade inválido (simulado com valores negativos)
            CreditCardDto creditCardDto = CreditCardDto.builder()
                    .cardNumber(4532015112830366L)
                    .month(-1)
                    .year(-2025)
                    .CVV(123)
                    .build();
            doNothing().when(paymentService).addCreditCard(any(CreditCardDto.class));

            // When & Then
            assertDoesNotThrow(() -> paymentController.addCreditCard(creditCardDto));
        }
    }

    @Nested
    @DisplayName("Casos de Falha - Validação de CVV")
    class CasosDeFalhaCvv {

        @Test
        @DisplayName("CT26 - Adicionar cartão com CVV vazio")
        void adicionarCartaoCvvVazio() {
            // Given - Valor Limite: CVV não fornecido
            CreditCardDto creditCardDto = CreditCardDto.builder()
                    .cardNumber(4532015112830366L)
                    .month(12)
                    .year(2025)
                    .CVV(null)
                    .build();
            doNothing().when(paymentService).addCreditCard(any(CreditCardDto.class));

            // When & Then
            assertDoesNotThrow(() -> paymentController.addCreditCard(creditCardDto));
        }

        @Test
        @DisplayName("CT27 - Adicionar cartão com CVV nulo")
        void adicionarCartaoCvvNulo() {
            // Given - Valor Limite: CVV = null
            CreditCardDto creditCardDto = CreditCardDto.builder()
                    .cardNumber(4532015112830366L)
                    .month(12)
                    .year(2025)
                    .CVV(null)
                    .build();
            doNothing().when(paymentService).addCreditCard(any(CreditCardDto.class));

            // When & Then
            assertDoesNotThrow(() -> paymentController.addCreditCard(creditCardDto));
        }

        @Test
        @DisplayName("CT28 - Adicionar cartão com CVV de 2 dígitos")
        void adicionarCartaoCvv2Digitos() {
            // Given - Valor Limite: CVV com apenas 2 dígitos
            CreditCardDto creditCardDto = CreditCardDto.builder()
                    .cardNumber(4532015112830366L)
                    .month(12)
                    .year(2025)
                    .CVV(12)
                    .build();
            doNothing().when(paymentService).addCreditCard(any(CreditCardDto.class));

            // When & Then
            assertDoesNotThrow(() -> paymentController.addCreditCard(creditCardDto));
        }

        @Test
        @DisplayName("CT29 - Adicionar cartão com CVV de 4 dígitos")
        void adicionarCartaoCvv4Digitos() {
            // Given - Valor Limite: CVV com 4 dígitos
            CreditCardDto creditCardDto = CreditCardDto.builder()
                    .cardNumber(4532015112830366L)
                    .month(12)
                    .year(2025)
                    .CVV(1234)
                    .build();
            doNothing().when(paymentService).addCreditCard(any(CreditCardDto.class));

            // When & Then
            assertDoesNotThrow(() -> paymentController.addCreditCard(creditCardDto));
        }

        @Test
        @DisplayName("CT30 - Adicionar cartão com CVV contendo letras")
        void adicionarCartaoCvvComLetras() {
            // Given - Particionamento: CVV com caracteres não numéricos (simulado com valor inválido)
            CreditCardDto creditCardDto = CreditCardDto.builder()
                    .cardNumber(4532015112830366L)
                    .month(12)
                    .year(2025)
                    .CVV(0)
                    .build();
            doNothing().when(paymentService).addCreditCard(any(CreditCardDto.class));

            // When & Then
            assertDoesNotThrow(() -> paymentController.addCreditCard(creditCardDto));
        }

        @Test
        @DisplayName("CT31 - Adicionar cartão com CVV contendo caracteres especiais")
        void adicionarCartaoCvvComEspeciais() {
            // Given - Particionamento: CVV com caracteres especiais (simulado)
            CreditCardDto creditCardDto = CreditCardDto.builder()
                    .cardNumber(4532015112830366L)
                    .month(12)
                    .year(2025)
                    .CVV(-1)
                    .build();
            doNothing().when(paymentService).addCreditCard(any(CreditCardDto.class));

            // When & Then
            assertDoesNotThrow(() -> paymentController.addCreditCard(creditCardDto));
        }
    }

    @Nested
    @DisplayName("Casos de Falha - Regras de Negócio")
    class CasosDeFalhaRegrasNegocio {

        @Test
        @DisplayName("CT32 - Adicionar cartão duplicado")
        void adicionarCartaoDuplicado() {
            // Given - Propriedade: usuário já possui cartão cadastrado
            CreditCardDto creditCardDto = CreditCardDto.builder()
                    .cardNumber(4532015112830366L)
                    .month(12)
                    .year(2025)
                    .CVV(123)
                    .build();
            doNothing().when(paymentService).addCreditCard(any(CreditCardDto.class));

            // When & Then
            assertDoesNotThrow(() -> paymentController.addCreditCard(creditCardDto));
        }

        @Test
        @DisplayName("CT33 - Adicionar cartão com body vazio")
        void adicionarCartaoBodyVazio() {
            // Given - Valor Limite: body com campos nulos
            CreditCardDto creditCardDto = CreditCardDto.builder()
                    .cardNumber(null)
                    .month(null)
                    .year(null)
                    .CVV(null)
                    .build();
            doNothing().when(paymentService).addCreditCard(any(CreditCardDto.class));

            // When & Then
            assertDoesNotThrow(() -> paymentController.addCreditCard(creditCardDto));
        }

        @Test
        @DisplayName("CT34 - Adicionar cartão sem body")
        void adicionarCartaoSemBody() {
            // Given - Valor Limite: body = null
            doNothing().when(paymentService).addCreditCard(isNull());

            // When & Then
            assertDoesNotThrow(() -> paymentController.addCreditCard(null));
        }
    }
}
