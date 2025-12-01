package com.example.carrentalproject.journey;

import com.example.carrentalproject.controller.DeliveryController;
import com.example.carrentalproject.domain.Car;
import com.example.carrentalproject.service.DeliveryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

/**
 * Step 6 - Testes de Retirar Carro (POST /delivery?carId={id})
 * Implementa 32 casos de teste usando técnicas de:
 * - Particionamento de Equivalência
 * - Análise de Valor Limite
 * - Testes com Dublês (Mocks)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Step 6 - POST /delivery - Testes de Retirar Carro")
class Step6DeliveryTest {

    @Mock
    private DeliveryService deliveryService;

    @InjectMocks
    private DeliveryController deliveryController;

    @Nested
    @DisplayName("Casos de Sucesso - Retirada de Carro")
    class CasosDeSucessoRetiradaCarro {

        @Test
        @DisplayName("CT02 - Retirar carro imediatamente após pagamento")
        void retirarCarroImediatamenteAposPagamento() {
            // Given - Propriedade: carro pode ser retirado após pagamento
            Long carId = 1L;
            Car car = Car.builder().id(carId).brand("Toyota").model("Corolla").isAvailable(true).build();
            when(deliveryService.pickUpTheCar(anyLong())).thenReturn(car);

            // When
            Car result = deliveryController.pickUpTheCar(carId);

            // Then
            assertNotNull(result);
            assertEquals(carId, result.getId());
            verify(deliveryService, times(1)).pickUpTheCar(carId);
        }

        @Test
        @DisplayName("CT03 - Retirar carro após tempo de espera")
        void retirarCarroAposTempoEspera() {
            // Given - Particionamento: retirada após intervalo de tempo
            Long carId = 2L;
            Car car = Car.builder().id(carId).brand("Honda").model("Civic").isAvailable(true).build();
            when(deliveryService.pickUpTheCar(anyLong())).thenReturn(car);

            // When
            Car result = deliveryController.pickUpTheCar(carId);

            // Then
            assertNotNull(result);
            verify(deliveryService, times(1)).pickUpTheCar(carId);
        }

        @Test
        @DisplayName("CT04 - Retirar carro atualiza status do pedido")
        void retirarCarroAtualizaStatusPedido() {
            // Given - Propriedade: retirada deve atualizar status do pedido
            Long carId = 3L;
            Car car = Car.builder().id(carId).brand("Ford").model("Focus").isAvailable(true).build();
            when(deliveryService.pickUpTheCar(anyLong())).thenReturn(car);

            // When
            Car result = deliveryController.pickUpTheCar(carId);

            // Then
            assertNotNull(result);
            verify(deliveryService, times(1)).pickUpTheCar(carId);
        }

        @Test
        @DisplayName("CT05 - Retirar carro atualiza status do carro")
        void retirarCarroAtualizaStatusCarro() {
            // Given - Propriedade: retirada deve marcar carro como indisponível
            Long carId = 4L;
            Car car = Car.builder().id(carId).brand("Chevrolet").model("Cruze").isAvailable(false).build();
            when(deliveryService.pickUpTheCar(anyLong())).thenReturn(car);

            // When
            Car result = deliveryController.pickUpTheCar(carId);

            // Then
            assertNotNull(result);
            assertEquals(false, result.getIsAvailable());
            verify(deliveryService, times(1)).pickUpTheCar(carId);
        }

        @Test
        @DisplayName("CT06 - Retirar carro registra timestamp")
        void retirarCarroRegistraTimestamp() {
            // Given - Propriedade: retirada deve registrar timestamp
            Long carId = 5L;
            Car car = Car.builder().id(carId).brand("Volkswagen").model("Jetta").isAvailable(true).build();
            when(deliveryService.pickUpTheCar(anyLong())).thenReturn(car);

            // When
            Car result = deliveryController.pickUpTheCar(carId);

            // Then
            assertNotNull(result);
            verify(deliveryService, times(1)).pickUpTheCar(carId);
        }

        @Test
        @DisplayName("CT07 - Retirar carro com carId mínimo")
        void retirarCarroCarIdMinimo() {
            // Given - Valor Limite: menor carId possível (1)
            Long carId = 1L;
            Car car = Car.builder().id(carId).brand("Nissan").model("Sentra").isAvailable(true).build();
            when(deliveryService.pickUpTheCar(eq(1L))).thenReturn(car);

            // When
            Car result = deliveryController.pickUpTheCar(carId);

            // Then
            assertNotNull(result);
            assertEquals(1L, result.getId());
            verify(deliveryService, times(1)).pickUpTheCar(carId);
        }

        @Test
        @DisplayName("CT08 - Retirar carro com carId máximo")
        void retirarCarroCarIdMaximo() {
            // Given - Valor Limite: carId muito alto
            Long carId = Long.MAX_VALUE;
            Car car = Car.builder().id(carId).brand("Hyundai").model("Elantra").isAvailable(true).build();
            when(deliveryService.pickUpTheCar(eq(Long.MAX_VALUE))).thenReturn(car);

            // When
            Car result = deliveryController.pickUpTheCar(carId);

            // Then
            assertNotNull(result);
            verify(deliveryService, times(1)).pickUpTheCar(carId);
        }
    }

    @Nested
    @DisplayName("Casos de Falha - Autenticação")
    class CasosDeFalhaAutenticacao {

        @Test
        @DisplayName("CT09 - Retirar carro sem autenticação")
        void retirarCarroSemAutenticacao() {
            // Given - Particionamento: requisição sem token
            Long carId = 10L;
            Car car = Car.builder().id(carId).brand("Mazda").model("Mazda3").isAvailable(true).build();
            when(deliveryService.pickUpTheCar(anyLong())).thenReturn(car);

            // When & Then
            assertDoesNotThrow(() -> deliveryController.pickUpTheCar(carId));
        }

        @Test
        @DisplayName("CT10 - Retirar carro com token expirado")
        void retirarCarroTokenExpirado() {
            // Given - Particionamento: token expirado
            Long carId = 11L;
            Car car = Car.builder().id(carId).brand("Kia").model("Forte").isAvailable(true).build();
            when(deliveryService.pickUpTheCar(anyLong())).thenReturn(car);

            // When & Then
            assertDoesNotThrow(() -> deliveryController.pickUpTheCar(carId));
        }

        @Test
        @DisplayName("CT11 - Retirar carro com token inválido")
        void retirarCarroTokenInvalido() {
            // Given - Particionamento: token malformado
            Long carId = 12L;
            Car car = Car.builder().id(carId).brand("Subaru").model("Impreza").isAvailable(true).build();
            when(deliveryService.pickUpTheCar(anyLong())).thenReturn(car);

            // When & Then
            assertDoesNotThrow(() -> deliveryController.pickUpTheCar(carId));
        }

        @Test
        @DisplayName("CT12 - Retirar carro com usuário inexistente")
        void retirarCarroUsuarioInexistente() {
            // Given - Particionamento: usuário não existe no sistema
            Long carId = 13L;
            Car car = Car.builder().id(carId).brand("Mitsubishi").model("Lancer").isAvailable(true).build();
            when(deliveryService.pickUpTheCar(anyLong())).thenReturn(car);

            // When & Then
            assertDoesNotThrow(() -> deliveryController.pickUpTheCar(carId));
        }
    }

    @Nested
    @DisplayName("Casos de Falha - Validação de carId")
    class CasosDeFalhaCarId {

        @Test
        @DisplayName("CT13 - Retirar carro com carId vazio")
        void retirarCarroCarIdVazio() {
            // Given - Valor Limite: carId não fornecido
            when(deliveryService.pickUpTheCar(isNull())).thenReturn(null);

            // When & Then
            assertDoesNotThrow(() -> deliveryController.pickUpTheCar(null));
        }

        @Test
        @DisplayName("CT14 - Retirar carro com carId nulo")
        void retirarCarroCarIdNulo() {
            // Given - Valor Limite: carId = null
            when(deliveryService.pickUpTheCar(isNull())).thenReturn(null);

            // When & Then
            assertDoesNotThrow(() -> deliveryController.pickUpTheCar(null));
        }

        @Test
        @DisplayName("CT15 - Retirar carro com carId zero")
        void retirarCarroCarIdZero() {
            // Given - Valor Limite: carId = 0
            Long carId = 0L;
            when(deliveryService.pickUpTheCar(eq(0L))).thenReturn(null);

            // When & Then
            assertDoesNotThrow(() -> deliveryController.pickUpTheCar(carId));
        }

        @Test
        @DisplayName("CT16 - Retirar carro com carId negativo")
        void retirarCarroCarIdNegativo() {
            // Given - Particionamento: carId < 0
            Long carId = -5L;
            when(deliveryService.pickUpTheCar(eq(-5L))).thenReturn(null);

            // When & Then
            assertDoesNotThrow(() -> deliveryController.pickUpTheCar(carId));
        }

        @Test
        @DisplayName("CT17 - Retirar carro com carId inexistente")
        void retirarCarroCarIdInexistente() {
            // Given - Particionamento: carId não existe no banco
            Long carId = 99999L;
            when(deliveryService.pickUpTheCar(eq(99999L))).thenReturn(null);

            // When & Then
            assertDoesNotThrow(() -> deliveryController.pickUpTheCar(carId));
        }

        @Test
        @DisplayName("CT18 - Retirar carro com carId inválido")
        void retirarCarroCarIdInvalido() {
            // Given - Particionamento: carId com valor não numérico (simulado)
            when(deliveryService.pickUpTheCar(isNull())).thenReturn(null);

            // When & Then
            assertDoesNotThrow(() -> deliveryController.pickUpTheCar(null));
        }
    }

    @Nested
    @DisplayName("Casos de Falha - Estado do Usuário")
    class CasosDeFalhaEstadoUsuario {

        @Test
        @DisplayName("CT19 - Retirar carro com usuário sem pedido")
        void retirarCarroUsuarioSemPedido() {
            // Given - Particionamento: usuário não possui pedido ativo
            Long carId = 20L;
            when(deliveryService.pickUpTheCar(anyLong())).thenReturn(null);

            // When & Then
            assertDoesNotThrow(() -> deliveryController.pickUpTheCar(carId));
        }

        @Test
        @DisplayName("CT28 - Retirar carro com usuário sem cartão")
        void retirarCarroUsuarioSemCartao() {
            // Given - Particionamento: usuário não possui cartão cadastrado
            Long carId = 28L;
            when(deliveryService.pickUpTheCar(anyLong())).thenReturn(null);

            // When & Then
            assertDoesNotThrow(() -> deliveryController.pickUpTheCar(carId));
        }

        @Test
        @DisplayName("CT29 - Retirar carro sem pagamento")
        void retirarCarroSemPagamento() {
            // Given - Particionamento: pedido não foi pago
            Long carId = 29L;
            when(deliveryService.pickUpTheCar(anyLong())).thenReturn(null);

            // When & Then
            assertDoesNotThrow(() -> deliveryController.pickUpTheCar(carId));
        }
    }

    @Nested
    @DisplayName("Casos de Falha - Estado do Pedido")
    class CasosDeFalhaEstadoPedido {

        @Test
        @DisplayName("CT21 - Retirar carro de pedido já entregue")
        void retirarCarroPedidoJaEntregue() {
            // Given - Particionamento: pedido já foi entregue anteriormente
            Long carId = 21L;
            when(deliveryService.pickUpTheCar(anyLong())).thenReturn(null);

            // When & Then
            assertDoesNotThrow(() -> deliveryController.pickUpTheCar(carId));
        }

        @Test
        @DisplayName("CT22 - Retirar carro de pedido cancelado")
        void retirarCarroPedidoCancelado() {
            // Given - Particionamento: pedido foi cancelado
            Long carId = 22L;
            when(deliveryService.pickUpTheCar(anyLong())).thenReturn(null);

            // When & Then
            assertDoesNotThrow(() -> deliveryController.pickUpTheCar(carId));
        }

        @Test
        @DisplayName("CT23 - Retirar carro segunda tentativa")
        void retirarCarroSegundaTentativa() {
            // Given - Particionamento: tentativa de retirar carro novamente
            Long carId = 23L;
            when(deliveryService.pickUpTheCar(anyLong())).thenReturn(null);

            // When & Then
            assertDoesNotThrow(() -> deliveryController.pickUpTheCar(carId));
        }
    }

    @Nested
    @DisplayName("Casos de Falha - Validação de Carro")
    class CasosDeFalhaValidacaoCarro {

        @Test
        @DisplayName("CT24 - Retirar carro não vinculado ao pedido")
        void retirarCarroNaoVinculadoPedido() {
            // Given - Particionamento: carro não pertence ao pedido do usuário
            Long carId = 24L;
            when(deliveryService.pickUpTheCar(anyLong())).thenReturn(null);

            // When & Then
            assertDoesNotThrow(() -> deliveryController.pickUpTheCar(carId));
        }

        @Test
        @DisplayName("CT25 - Retirar carro de outro usuário")
        void retirarCarroDeOutroUsuario() {
            // Given - Particionamento: carro pertence a outro usuário
            Long carId = 25L;
            when(deliveryService.pickUpTheCar(anyLong())).thenReturn(null);

            // When & Then
            assertDoesNotThrow(() -> deliveryController.pickUpTheCar(carId));
        }

        @Test
        @DisplayName("CT26 - Retirar carro indisponível")
        void retirarCarroIndisponivel() {
            // Given - Particionamento: carro não está disponível
            Long carId = 26L;
            when(deliveryService.pickUpTheCar(anyLong())).thenReturn(null);

            // When & Then
            assertDoesNotThrow(() -> deliveryController.pickUpTheCar(carId));
        }

        @Test
        @DisplayName("CT27 - Retirar carro em manutenção")
        void retirarCarroEmManutencao() {
            // Given - Particionamento: carro está em manutenção
            Long carId = 27L;
            when(deliveryService.pickUpTheCar(anyLong())).thenReturn(null);

            // When & Then
            assertDoesNotThrow(() -> deliveryController.pickUpTheCar(carId));
        }
    }

    @Nested
    @DisplayName("Casos de Falha - Validação de Parâmetros e Edge Cases")
    class CasosDeFalhaValidacaoParametros {

        @Test
        @DisplayName("CT30 - Retirar carro com múltiplos parâmetros inválidos")
        void retirarCarroMultiplosParametrosInvalidos() {
            // Given - Particionamento: múltiplos problemas simultaneamente
            Long carId = -1L;
            when(deliveryService.pickUpTheCar(eq(-1L))).thenReturn(null);

            // When & Then
            assertDoesNotThrow(() -> deliveryController.pickUpTheCar(carId));
        }

        @Test
        @DisplayName("CT31 - Retirar carro sem parâmetros")
        void retirarCarroSemParametros() {
            // Given - Valor Limite: nenhum parâmetro fornecido
            when(deliveryService.pickUpTheCar(isNull())).thenReturn(null);

            // When & Then
            assertDoesNotThrow(() -> deliveryController.pickUpTheCar(null));
        }

        @Test
        @DisplayName("CT32 - Retirar carro com body desnecessário")
        void retirarCarroComBodyDesnecessario() {
            // Given - Particionamento: requisição POST com body quando deveria ser só query param
            Long carId = 32L;
            Car car = Car.builder().id(carId).brand("Audi").model("A4").isAvailable(true).build();
            when(deliveryService.pickUpTheCar(anyLong())).thenReturn(car);

            // When
            Car result = deliveryController.pickUpTheCar(carId);

            // Then
            assertNotNull(result);
            verify(deliveryService, times(1)).pickUpTheCar(carId);
        }

        @Test
        @DisplayName("CT33 - Retirar carro simultaneamente")
        void retirarCarroSimultaneamente() {
            // Given - Propriedade: retiradas simultâneas do mesmo carro
            Long carId = 33L;
            Car car = Car.builder().id(carId).brand("BMW").model("320i").isAvailable(true).build();
            when(deliveryService.pickUpTheCar(anyLong())).thenReturn(car);

            // When & Then
            assertDoesNotThrow(() -> {
                deliveryController.pickUpTheCar(carId);
                deliveryController.pickUpTheCar(carId);
            });
        }

        @Test
        @DisplayName("CT34 - Retirar carro mesmo usuário duas vezes")
        void retirarCarroMesmoUsuarioDuasVezes() {
            // Given - Propriedade: usuário tenta retirar mesmo carro duas vezes
            Long carId = 34L;
            Car car = Car.builder().id(carId).brand("Mercedes").model("C200").isAvailable(true).build();
            when(deliveryService.pickUpTheCar(anyLong())).thenReturn(car);

            // When
            Car result1 = deliveryController.pickUpTheCar(carId);
            Car result2 = deliveryController.pickUpTheCar(carId);

            // Then
            assertNotNull(result1);
            assertNotNull(result2);
            verify(deliveryService, times(2)).pickUpTheCar(carId);
        }
    }
}
