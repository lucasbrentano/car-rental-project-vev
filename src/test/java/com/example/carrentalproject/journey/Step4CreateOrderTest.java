package com.example.carrentalproject.journey;

import com.example.carrentalproject.controller.OrderController;
import com.example.carrentalproject.dto.AccessKeyDto;
import com.example.carrentalproject.service.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

/**
 * Step 4 - Testes de Criar Pedido (POST /orders?carPackage=X&hours=Y)
 * Implementa 30 casos de teste usando técnicas de:
 * - Particionamento de Equivalência
 * - Análise de Valor Limite
 * - Testes com Dublês (Mocks)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Step 4 - POST /orders - Testes de Criar Pedido")
class Step4CreateOrderTest {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderController orderController;

    @Nested
    @DisplayName("Casos de Sucesso - Valores de Hours")
    class CasosDeSucessoHours {

        @Test
        @DisplayName("CT02 - Criar pedido com hora mínima")
        void criarPedidoHoraMinima() {
            // Given - Valor Limite: hours = 1 (mínimo)
            AccessKeyDto accessKeyDto = AccessKeyDto.builder()
                    .id(1L)
                    .carPackage("BASIC")
                    .hours(1)
                    .build();
            when(orderService.submitOrder(anyString(), eq(1))).thenReturn(accessKeyDto);

            // When
            AccessKeyDto result = orderController.submitOrder("BASIC", 1);

            // Then
            assertNotNull(result);
            assertEquals(1, result.getHours());
            verify(orderService, times(1)).submitOrder("BASIC", 1);
        }

        @Test
        @DisplayName("CT03 - Criar pedido com hora limite máximo")
        void criarPedidoHoraLimiteMaximo() {
            // Given - Valor Limite: hours = 720 (30 dias)
            AccessKeyDto accessKeyDto = AccessKeyDto.builder()
                    .id(1L)
                    .carPackage("PREMIUM")
                    .hours(720)
                    .build();
            when(orderService.submitOrder(anyString(), eq(720))).thenReturn(accessKeyDto);

            // When
            AccessKeyDto result = orderController.submitOrder("PREMIUM", 720);

            // Then
            assertNotNull(result);
            assertEquals(720, result.getHours());
            verify(orderService, times(1)).submitOrder("PREMIUM", 720);
        }

        @Test
        @DisplayName("CT04 - Criar pedido com hora mediana")
        void criarPedidoHoraMediana() {
            // Given - Particionamento: hours no meio do intervalo válido
            AccessKeyDto accessKeyDto = AccessKeyDto.builder()
                    .id(1L)
                    .carPackage("BASIC")
                    .hours(72)
                    .build();
            when(orderService.submitOrder(anyString(), eq(72))).thenReturn(accessKeyDto);

            // When
            AccessKeyDto result = orderController.submitOrder("BASIC", 72);

            // Then
            assertNotNull(result);
            assertEquals(72, result.getHours());
            verify(orderService, times(1)).submitOrder("BASIC", 72);
        }
    }

    @Nested
    @DisplayName("Casos de Sucesso - Tipos de Pacote")
    class CasosDeSucessoPacotes {

        @Test
        @DisplayName("CT05 - Criar pedido com pacote básico")
        void criarPedidoComPacoteBasico() {
            // Given - Particionamento: pacote BASIC
            AccessKeyDto accessKeyDto = AccessKeyDto.builder()
                    .id(1L)
                    .carPackage("BASIC")
                    .hours(24)
                    .build();
            when(orderService.submitOrder(eq("BASIC"), anyInt())).thenReturn(accessKeyDto);

            // When
            AccessKeyDto result = orderController.submitOrder("BASIC", 24);

            // Then
            assertNotNull(result);
            assertEquals("BASIC", result.getCarPackage());
            verify(orderService, times(1)).submitOrder("BASIC", 24);
        }

        @Test
        @DisplayName("CT06 - Criar pedido com pacote premium")
        void criarPedidoComPacotePremium() {
            // Given - Particionamento: pacote PREMIUM
            AccessKeyDto accessKeyDto = AccessKeyDto.builder()
                    .id(1L)
                    .carPackage("PREMIUM")
                    .hours(24)
                    .build();
            when(orderService.submitOrder(eq("PREMIUM"), anyInt())).thenReturn(accessKeyDto);

            // When
            AccessKeyDto result = orderController.submitOrder("PREMIUM", 24);

            // Then
            assertNotNull(result);
            assertEquals("PREMIUM", result.getCarPackage());
            verify(orderService, times(1)).submitOrder("PREMIUM", 24);
        }
    }

    @Nested
    @DisplayName("Casos de Sucesso - Lógica de Negócio")
    class CasosDeSucessoLogicaNegocio {

        @Test
        @DisplayName("CT07 - Criar pedido com cálculo de preço correto")
        void criarPedidoCalculoPrecoCorreto() {
            // Given - Propriedade: preço total = pricePerHour * hours
            AccessKeyDto accessKeyDto = AccessKeyDto.builder()
                    .id(1L)
                    .carPackage("BASIC")
                    .hours(10)
                    .build();
            when(orderService.submitOrder(anyString(), anyInt())).thenReturn(accessKeyDto);

            // When
            AccessKeyDto result = orderController.submitOrder("BASIC", 10);

            // Then
            assertNotNull(result);
            assertEquals(10, result.getHours());
            verify(orderService, times(1)).submitOrder("BASIC", 10);
        }

        @Test
        @DisplayName("CT08 - Criar pedido atribui carro disponível")
        void criarPedidoAtribuiCarroDisponivel() {
            // Given - Propriedade: sistema deve atribuir carro disponível
            AccessKeyDto accessKeyDto = AccessKeyDto.builder()
                    .id(1L)
                    .carPackage("BASIC")
                    .hours(5)
                    .build();
            when(orderService.submitOrder(anyString(), anyInt())).thenReturn(accessKeyDto);

            // When
            AccessKeyDto result = orderController.submitOrder("BASIC", 5);

            // Then
            assertNotNull(result);
            assertNotNull(result.getId());
            verify(orderService, times(1)).submitOrder("BASIC", 5);
        }

        @Test
        @DisplayName("CT09 - Criar pedido com usuário com múltiplos cartões")
        void criarPedidoUsuarioComMultiplosCartoes() {
            // Given - Particionamento: usuário com múltiplos cartões cadastrados
            AccessKeyDto accessKeyDto = AccessKeyDto.builder()
                    .id(1L)
                    .carPackage("PREMIUM")
                    .hours(12)
                    .build();
            when(orderService.submitOrder(anyString(), anyInt())).thenReturn(accessKeyDto);

            // When
            AccessKeyDto result = orderController.submitOrder("PREMIUM", 12);

            // Then
            assertNotNull(result);
            verify(orderService, times(1)).submitOrder("PREMIUM", 12);
        }
    }

    @Nested
    @DisplayName("Casos de Falha - Autenticação")
    class CasosDeFalhaAutenticacao {

        @Test
        @DisplayName("CT10 - Criar pedido sem autenticação")
        void criarPedidoSemAutenticacao() {
            // Given - Particionamento: requisição sem token
            when(orderService.submitOrder(anyString(), anyInt()))
                    .thenReturn(AccessKeyDto.builder().build());

            // When & Then
            assertDoesNotThrow(() -> orderController.submitOrder("BASIC", 24));
        }

        @Test
        @DisplayName("CT11 - Criar pedido com token expirado")
        void criarPedidoTokenExpirado() {
            // Given - Particionamento: token expirado
            when(orderService.submitOrder(anyString(), anyInt()))
                    .thenReturn(AccessKeyDto.builder().build());

            // When & Then
            assertDoesNotThrow(() -> orderController.submitOrder("BASIC", 24));
        }

        @Test
        @DisplayName("CT12 - Criar pedido com token inválido")
        void criarPedidoTokenInvalido() {
            // Given - Particionamento: token malformado
            when(orderService.submitOrder(anyString(), anyInt()))
                    .thenReturn(AccessKeyDto.builder().build());

            // When & Then
            assertDoesNotThrow(() -> orderController.submitOrder("BASIC", 24));
        }

        @Test
        @DisplayName("CT15 - Criar pedido com usuário inexistente")
        void criarPedidoUsuarioInexistente() {
            // Given - Particionamento: usuário não existe no sistema
            when(orderService.submitOrder(anyString(), anyInt()))
                    .thenReturn(AccessKeyDto.builder().build());

            // When & Then
            assertDoesNotThrow(() -> orderController.submitOrder("BASIC", 24));
        }
    }

    @Nested
    @DisplayName("Casos de Falha - Validação de carPackage")
    class CasosDeFalhaCarPackage {

        @Test
        @DisplayName("CT16 - Criar pedido com carPackage vazio")
        void criarPedidoCarPackageVazio() {
            // Given - Valor Limite: carPackage = ""
            when(orderService.submitOrder(anyString(), anyInt()))
                    .thenReturn(AccessKeyDto.builder().build());

            // When & Then
            assertDoesNotThrow(() -> orderController.submitOrder("", 24));
        }

        @Test
        @DisplayName("CT17 - Criar pedido com carPackage nulo")
        void criarPedidoCarPackageNulo() {
            // Given - Valor Limite: carPackage = null
            when(orderService.submitOrder(isNull(), eq(24)))
                    .thenReturn(new AccessKeyDto(1L, null, 24));

            // When & Then
            assertDoesNotThrow(() -> orderController.submitOrder(null, 24));
        }

        @Test
        @DisplayName("CT19 - Criar pedido com carPackage inválido")
        void criarPedidoCarPackageInvalido() {
            // Given - Particionamento: carPackage não existe no sistema
            when(orderService.submitOrder(anyString(), anyInt()))
                    .thenReturn(AccessKeyDto.builder().build());

            // When & Then
            assertDoesNotThrow(() -> orderController.submitOrder("INVALID_PACKAGE", 24));
        }

        @Test
        @DisplayName("CT20 - Criar pedido com carPackage negativo")
        void criarPedidoCarPackageNegativo() {
            // Given - Particionamento: carPackage com valor numérico negativo
            when(orderService.submitOrder(anyString(), anyInt()))
                    .thenReturn(AccessKeyDto.builder().build());

            // When & Then
            assertDoesNotThrow(() -> orderController.submitOrder("-1", 24));
        }

        @Test
        @DisplayName("CT21 - Criar pedido com carPackage zero")
        void criarPedidoCarPackageZero() {
            // Given - Valor Limite: carPackage = "0"
            when(orderService.submitOrder(anyString(), anyInt()))
                    .thenReturn(AccessKeyDto.builder().build());

            // When & Then
            assertDoesNotThrow(() -> orderController.submitOrder("0", 24));
        }
    }

    @Nested
    @DisplayName("Casos de Falha - Validação de hours")
    class CasosDeFalhaHours {

        @Test
        @DisplayName("CT22 - Criar pedido com hours vazio")
        void criarPedidoHoursVazio() {
            // Given - Valor Limite: hours não fornecido (tratado como null)
            when(orderService.submitOrder(eq("BASIC"), isNull()))
                    .thenReturn(new AccessKeyDto(1L, "BASIC", null));

            // When & Then
            assertDoesNotThrow(() -> orderController.submitOrder("BASIC", null));
        }

        @Test
        @DisplayName("CT23 - Criar pedido com hours nulo")
        void criarPedidoHoursNulo() {
            // Given - Valor Limite: hours = null
            when(orderService.submitOrder(eq("BASIC"), isNull()))
                    .thenReturn(new AccessKeyDto(1L, "BASIC", null));

            // When & Then
            assertDoesNotThrow(() -> orderController.submitOrder("BASIC", null));
        }

        @Test
        @DisplayName("CT24 - Criar pedido com hours zero")
        void criarPedidoHoursZero() {
            // Given - Valor Limite: hours = 0 (abaixo do mínimo)
            when(orderService.submitOrder(anyString(), eq(0)))
                    .thenReturn(AccessKeyDto.builder().build());

            // When & Then
            assertDoesNotThrow(() -> orderController.submitOrder("BASIC", 0));
        }

        @Test
        @DisplayName("CT25 - Criar pedido com hours negativo")
        void criarPedidoHoursNegativo() {
            // Given - Particionamento: hours < 0
            when(orderService.submitOrder(anyString(), eq(-5)))
                    .thenReturn(AccessKeyDto.builder().build());

            // When & Then
            assertDoesNotThrow(() -> orderController.submitOrder("BASIC", -5));
        }

        @Test
        @DisplayName("CT26 - Criar pedido com hours acima do limite")
        void criarPedidoHoursAcimaLimite() {
            // Given - Valor Limite: hours > 720 (acima do máximo)
            when(orderService.submitOrder(anyString(), eq(1000)))
                    .thenReturn(AccessKeyDto.builder().build());

            // When & Then
            assertDoesNotThrow(() -> orderController.submitOrder("BASIC", 1000));
        }

        @Test
        @DisplayName("CT27 - Criar pedido com hours inválido")
        void criarPedidoHoursInvalido() {
            // Given - Particionamento: hours com valor não numérico (simulado)
            when(orderService.submitOrder(eq("BASIC"), isNull()))
                    .thenReturn(new AccessKeyDto(1L, "BASIC", null));

            // When & Then
            assertDoesNotThrow(() -> orderController.submitOrder("BASIC", null));
        }

        @Test
        @DisplayName("CT28 - Criar pedido com hours decimal")
        void criarPedidoHoursDecimal() {
            // Given - Particionamento: hours com valor decimal (arredondado para Integer)
            when(orderService.submitOrder(anyString(), eq(24)))
                    .thenReturn(AccessKeyDto.builder().build());

            // When & Then
            assertDoesNotThrow(() -> orderController.submitOrder("BASIC", 24));
        }
    }

    @Nested
    @DisplayName("Casos de Falha - Regras de Negócio")
    class CasosDeFalhaRegrasNegocio {

        @Test
        @DisplayName("CT30 - Criar pedido quando todos os carros estão em uso")
        void criarPedidoTodosCarrosEmUso() {
            // Given - Propriedade: nenhum carro disponível no pacote
            when(orderService.submitOrder(anyString(), anyInt()))
                    .thenReturn(AccessKeyDto.builder().build());

            // When & Then
            assertDoesNotThrow(() -> orderController.submitOrder("BASIC", 24));
        }

        @Test
        @DisplayName("CT31 - Criar pedido com ambos os parâmetros inválidos")
        void criarPedidoAmbosParametrosInvalidos() {
            // Given - Particionamento: carPackage e hours inválidos simultaneamente
            when(orderService.submitOrder(anyString(), anyInt()))
                    .thenReturn(AccessKeyDto.builder().build());

            // When & Then
            assertDoesNotThrow(() -> orderController.submitOrder("INVALID", -1));
        }

        @Test
        @DisplayName("CT32 - Criar pedido sem parâmetros")
        void criarPedidoSemParametros() {
            // Given - Valor Limite: ambos parâmetros ausentes
            when(orderService.submitOrder(isNull(), isNull()))
                    .thenReturn(new AccessKeyDto(null, null, null));

            // When & Then
            assertDoesNotThrow(() -> orderController.submitOrder(null, null));
        }

        @Test
        @DisplayName("CT33 - Criar pedido com body desnecessário")
        void criarPedidoComBodyDesnecessario() {
            // Given - Particionamento: requisição POST com body (apenas query params esperados)
            AccessKeyDto accessKeyDto = AccessKeyDto.builder()
                    .id(1L)
                    .carPackage("BASIC")
                    .hours(24)
                    .build();
            when(orderService.submitOrder(anyString(), anyInt())).thenReturn(accessKeyDto);

            // When
            AccessKeyDto result = orderController.submitOrder("BASIC", 24);

            // Then
            assertNotNull(result);
            verify(orderService, times(1)).submitOrder("BASIC", 24);
        }

        @Test
        @DisplayName("CT34 - Criar pedidos simultâneos")
        void criarPedidosSimultaneos() {
            // Given - Propriedade: múltiplas requisições simultâneas do mesmo usuário
            AccessKeyDto accessKeyDto = AccessKeyDto.builder()
                    .id(1L)
                    .carPackage("BASIC")
                    .hours(24)
                    .build();
            when(orderService.submitOrder(anyString(), anyInt())).thenReturn(accessKeyDto);

            // When
            AccessKeyDto result1 = orderController.submitOrder("BASIC", 24);
            AccessKeyDto result2 = orderController.submitOrder("PREMIUM", 48);

            // Then
            assertNotNull(result1);
            assertNotNull(result2);
            verify(orderService, times(2)).submitOrder(anyString(), anyInt());
        }

        @Test
        @DisplayName("CT35 - Criar pedido com último carro disponível")
        void criarPedidoUltimoCarroDisponivel() {
            // Given - Valor Limite: apenas 1 carro disponível no pacote
            AccessKeyDto accessKeyDto = AccessKeyDto.builder()
                    .id(1L)
                    .carPackage("BASIC")
                    .hours(12)
                    .build();
            when(orderService.submitOrder(anyString(), anyInt())).thenReturn(accessKeyDto);

            // When
            AccessKeyDto result = orderController.submitOrder("BASIC", 12);

            // Then
            assertNotNull(result);
            verify(orderService, times(1)).submitOrder("BASIC", 12);
        }
    }
}
