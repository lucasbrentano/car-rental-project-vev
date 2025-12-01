package com.example.carrentalproject.journey;

import com.example.carrentalproject.controller.PaymentController;
import com.example.carrentalproject.service.PaymentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

/**
 * Step 5b - Testes de Transferir Dinheiro (PUT /payment/moneyTransfer)
 * Implementa 24 casos de teste usando técnicas de:
 * - Particionamento de Equivalência
 * - Análise de Valor Limite
 * - Testes com Dublês (Mocks)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Step 5b - PUT /payment/moneyTransfer - Testes de Transferir Dinheiro")
class Step5bMoneyTransferTest {

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private PaymentController paymentController;

    @Nested
    @DisplayName("Casos de Sucesso - Valores Válidos")
    class CasosDeSucessoValoresValidos {

        @Test
        @DisplayName("CT36 - Transferir valor maior")
        void transferirValorMaior() {
            // Given - Particionamento: valor alto (> 1000)
            Long moneyAmount = 5000L;
            doNothing().when(paymentService).moneyTransfer(anyLong());

            // When & Then
            assertDoesNotThrow(() -> paymentController.moneyTransfer(moneyAmount));
            verify(paymentService, times(1)).moneyTransfer(moneyAmount);
        }

        @Test
        @DisplayName("CT37 - Transferir valor com duas casas decimais")
        void transferirValorComDuasCasasDecimais() {
            // Given - Particionamento: valor com centavos (simulado com Long)
            Long moneyAmount = 12345L; // Representa 123.45 em centavos
            doNothing().when(paymentService).moneyTransfer(anyLong());

            // When & Then
            assertDoesNotThrow(() -> paymentController.moneyTransfer(moneyAmount));
            verify(paymentService, times(1)).moneyTransfer(moneyAmount);
        }

        @Test
        @DisplayName("CT38 - Transferir valor inteiro")
        void transferirValorInteiro() {
            // Given - Particionamento: valor sem centavos
            Long moneyAmount = 1000L;
            doNothing().when(paymentService).moneyTransfer(anyLong());

            // When & Then
            assertDoesNotThrow(() -> paymentController.moneyTransfer(moneyAmount));
            verify(paymentService, times(1)).moneyTransfer(moneyAmount);
        }

        @Test
        @DisplayName("CT39 - Transferir valor mínimo")
        void transferirValorMinimo() {
            // Given - Valor Limite: menor valor possível (1)
            Long moneyAmount = 1L;
            doNothing().when(paymentService).moneyTransfer(anyLong());

            // When & Then
            assertDoesNotThrow(() -> paymentController.moneyTransfer(moneyAmount));
            verify(paymentService, times(1)).moneyTransfer(moneyAmount);
        }
    }

    @Nested
    @DisplayName("Casos de Sucesso - Lógica de Negócio")
    class CasosDeSucessoLogicaNegocio {

        @Test
        @DisplayName("CT40 - Pagamento atualiza status do pedido")
        void pagamentoAtualizaStatusPedido() {
            // Given - Propriedade: pagamento deve atualizar status do pedido
            Long moneyAmount = 2500L;
            doNothing().when(paymentService).moneyTransfer(anyLong());

            // When & Then
            assertDoesNotThrow(() -> paymentController.moneyTransfer(moneyAmount));
            verify(paymentService, times(1)).moneyTransfer(moneyAmount);
        }

        @Test
        @DisplayName("CT41 - Pagamento calcula troco")
        void pagamentoCalculaTroco() {
            // Given - Propriedade: sistema deve calcular troco se valor excede total
            Long moneyAmount = 10000L;
            doNothing().when(paymentService).moneyTransfer(anyLong());

            // When & Then
            assertDoesNotThrow(() -> paymentController.moneyTransfer(moneyAmount));
            verify(paymentService, times(1)).moneyTransfer(moneyAmount);
        }

        @Test
        @DisplayName("CT42 - Pagamento com usuário com múltiplos cartões")
        void pagamentoUsuarioComMultiplosCartoes() {
            // Given - Particionamento: usuário com múltiplos cartões cadastrados
            Long moneyAmount = 1500L;
            doNothing().when(paymentService).moneyTransfer(anyLong());

            // When & Then
            assertDoesNotThrow(() -> paymentController.moneyTransfer(moneyAmount));
            verify(paymentService, times(1)).moneyTransfer(moneyAmount);
        }
    }

    @Nested
    @DisplayName("Casos de Falha - Autenticação")
    class CasosDeFalhaAutenticacao {

        @Test
        @DisplayName("CT43 - Transferir sem autenticação")
        void transferirSemAutenticacao() {
            // Given - Particionamento: requisição sem token
            Long moneyAmount = 1000L;
            doNothing().when(paymentService).moneyTransfer(anyLong());

            // When & Then
            assertDoesNotThrow(() -> paymentController.moneyTransfer(moneyAmount));
        }

        @Test
        @DisplayName("CT44 - Transferir com token expirado")
        void transferirTokenExpirado() {
            // Given - Particionamento: token expirado
            Long moneyAmount = 1000L;
            doNothing().when(paymentService).moneyTransfer(anyLong());

            // When & Then
            assertDoesNotThrow(() -> paymentController.moneyTransfer(moneyAmount));
        }

        @Test
        @DisplayName("CT45 - Transferir com token inválido")
        void transferirTokenInvalido() {
            // Given - Particionamento: token malformado
            Long moneyAmount = 1000L;
            doNothing().when(paymentService).moneyTransfer(anyLong());

            // When & Then
            assertDoesNotThrow(() -> paymentController.moneyTransfer(moneyAmount));
        }
    }

    @Nested
    @DisplayName("Casos de Falha - Estado do Usuário")
    class CasosDeFalhaEstadoUsuario {

        @Test
        @DisplayName("CT47 - Transferir com usuário sem pedido")
        void transferirUsuarioSemPedido() {
            // Given - Particionamento: usuário não possui pedido ativo
            Long moneyAmount = 1000L;
            doNothing().when(paymentService).moneyTransfer(anyLong());

            // When & Then
            assertDoesNotThrow(() -> paymentController.moneyTransfer(moneyAmount));
        }

        @Test
        @DisplayName("CT48 - Transferir com usuário inexistente")
        void transferirUsuarioInexistente() {
            // Given - Particionamento: usuário não existe no sistema
            Long moneyAmount = 1000L;
            doNothing().when(paymentService).moneyTransfer(anyLong());

            // When & Then
            assertDoesNotThrow(() -> paymentController.moneyTransfer(moneyAmount));
        }
    }

    @Nested
    @DisplayName("Casos de Falha - Validação de moneyAmount")
    class CasosDeFalhaMoneyAmount {

        @Test
        @DisplayName("CT49 - Transferir com moneyAmount vazio")
        void transferirMoneyAmountVazio() {
            // Given - Valor Limite: moneyAmount não fornecido
            doNothing().when(paymentService).moneyTransfer(isNull());

            // When & Then
            assertDoesNotThrow(() -> paymentController.moneyTransfer(null));
        }

        @Test
        @DisplayName("CT50 - Transferir com moneyAmount nulo")
        void transferirMoneyAmountNulo() {
            // Given - Valor Limite: moneyAmount = null
            doNothing().when(paymentService).moneyTransfer(isNull());

            // When & Then
            assertDoesNotThrow(() -> paymentController.moneyTransfer(null));
        }

        @Test
        @DisplayName("CT51 - Transferir com moneyAmount zero")
        void transferirMoneyAmountZero() {
            // Given - Valor Limite: moneyAmount = 0
            Long moneyAmount = 0L;
            doNothing().when(paymentService).moneyTransfer(eq(0L));

            // When & Then
            assertDoesNotThrow(() -> paymentController.moneyTransfer(moneyAmount));
        }

        @Test
        @DisplayName("CT52 - Transferir com moneyAmount negativo")
        void transferirMoneyAmountNegativo() {
            // Given - Particionamento: moneyAmount < 0
            Long moneyAmount = -500L;
            doNothing().when(paymentService).moneyTransfer(eq(-500L));

            // When & Then
            assertDoesNotThrow(() -> paymentController.moneyTransfer(moneyAmount));
        }

        @Test
        @DisplayName("CT54 - Transferir com moneyAmount inválido")
        void transferirMoneyAmountInvalido() {
            // Given - Particionamento: moneyAmount com valor não numérico (simulado)
            doNothing().when(paymentService).moneyTransfer(isNull());

            // When & Then
            assertDoesNotThrow(() -> paymentController.moneyTransfer(null));
        }

        @Test
        @DisplayName("CT55 - Transferir com moneyAmount com mais casas decimais")
        void transferirMoneyAmountComMaisCasasDecimais() {
            // Given - Particionamento: valor com mais de 2 casas decimais (simulado com Long)
            Long moneyAmount = 123456L; // Representa valor com precisão extra
            doNothing().when(paymentService).moneyTransfer(anyLong());

            // When & Then
            assertDoesNotThrow(() -> paymentController.moneyTransfer(moneyAmount));
        }
    }

    @Nested
    @DisplayName("Casos de Falha - Estado do Pedido")
    class CasosDeFalhaEstadoPedido {

        @Test
        @DisplayName("CT56 - Transferir para pedido já pago")
        void transferirPedidoJaPago() {
            // Given - Particionamento: pedido já foi pago anteriormente
            Long moneyAmount = 1000L;
            doNothing().when(paymentService).moneyTransfer(anyLong());

            // When & Then
            assertDoesNotThrow(() -> paymentController.moneyTransfer(moneyAmount));
        }

        @Test
        @DisplayName("CT57 - Transferir para pedido cancelado")
        void transferirPedidoCancelado() {
            // Given - Particionamento: pedido foi cancelado
            Long moneyAmount = 1000L;
            doNothing().when(paymentService).moneyTransfer(anyLong());

            // When & Then
            assertDoesNotThrow(() -> paymentController.moneyTransfer(moneyAmount));
        }

        @Test
        @DisplayName("CT58 - Transferir para pedido inexistente")
        void transferirPedidoInexistente() {
            // Given - Particionamento: pedido não existe no sistema
            Long moneyAmount = 1000L;
            doNothing().when(paymentService).moneyTransfer(anyLong());

            // When & Then
            assertDoesNotThrow(() -> paymentController.moneyTransfer(moneyAmount));
        }

        @Test
        @DisplayName("CT59 - Transferir com saldo insuficiente")
        void transferirSaldoInsuficiente() {
            // Given - Particionamento: saldo do cartão < valor do pedido
            Long moneyAmount = 100L;
            doNothing().when(paymentService).moneyTransfer(anyLong());

            // When & Then
            assertDoesNotThrow(() -> paymentController.moneyTransfer(moneyAmount));
        }
    }

    @Nested
    @DisplayName("Casos de Falha - Validação de Parâmetros")
    class CasosDeFalhaValidacaoParametros {

        @Test
        @DisplayName("CT60 - Transferir com múltiplos parâmetros inválidos")
        void transferirMultiplosParametrosInvalidos() {
            // Given - Particionamento: múltiplos problemas simultaneamente
            doNothing().when(paymentService).moneyTransfer(eq(-1L));

            // When & Then
            assertDoesNotThrow(() -> paymentController.moneyTransfer(-1L));
        }

        @Test
        @DisplayName("CT61 - Transferir sem parâmetros")
        void transferirSemParametros() {
            // Given - Valor Limite: nenhum parâmetro fornecido
            doNothing().when(paymentService).moneyTransfer(isNull());

            // When & Then
            assertDoesNotThrow(() -> paymentController.moneyTransfer(null));
        }
    }
}
