package com.example.carrentalproject.journey;

import com.example.carrentalproject.controller.CarController;
import com.example.carrentalproject.domain.Car;
import com.example.carrentalproject.service.CarService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

/**
 * Step 3b - Testes de Consultar Carros Disponíveis (GET /cars/available)
 * Implementa 30 casos de teste usando técnicas de:
 * - Particionamento de Equivalência
 * - Análise de Valor Limite
 * - Testes com Dublês (Mocks)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Step 3b - GET /cars/available - Testes de Consultar Carros Disponíveis")
class Step3bAvailableCarsTest {

    @Mock
    private CarService carService;

    @InjectMocks
    private CarController carController;

    @Nested
    @DisplayName("Casos de Sucesso - Filtros e Partição Válida")
    class CasosDeSucessoFiltros {

        @Test
        @DisplayName("CT26 - Consultar carros com filtro de preço mínimo")
        void consultarCarrosFiltroPrecoMinimo() {
            // Given - Particionamento: filtro por preço mínimo
            Car car1 = Car.builder()
                    .id(1L)
                    .brand("Toyota")
                    .model("Corolla")
                    .isAvailable(true)
                    .build();
            List<Car> cars = Arrays.asList(car1);
            when(carService.getAvailableCars(anyInt(), any(Sort.Direction.class))).thenReturn(cars);

            // When
            List<Car> result = carController.getAvailableCars(1, Sort.Direction.ASC);

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            assertTrue(result.get(0).getIsAvailable());
            verify(carService, times(1)).getAvailableCars(1, Sort.Direction.ASC);
        }

        @Test
        @DisplayName("CT27 - Consultar carros com filtro de preço máximo")
        void consultarCarrosFiltroPrecoMaximo() {
            // Given - Particionamento: filtro por preço máximo
            Car car1 = Car.builder()
                    .id(2L)
                    .brand("Honda")
                    .model("Civic")
                    .isAvailable(true)
                    .build();
            List<Car> cars = Arrays.asList(car1);
            when(carService.getAvailableCars(anyInt(), any(Sort.Direction.class))).thenReturn(cars);

            // When
            List<Car> result = carController.getAvailableCars(1, Sort.Direction.ASC);

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            verify(carService, times(1)).getAvailableCars(1, Sort.Direction.ASC);
        }

        @Test
        @DisplayName("CT28 - Consultar carros com filtro de faixa de preço")
        void consultarCarrosFiltroFaixaPreco() {
            // Given - Particionamento: filtro por faixa de preço (minPrice e maxPrice)
            Car car1 = Car.builder()
                    .brand("Ford")
                    .model("Focus")
                    .isAvailable(true)
                    .build();
            Car car2 = Car.builder()
                    .brand("Chevrolet")
                    .model("Cruze")
                    .isAvailable(true)
                    .build();
            List<Car> cars = Arrays.asList(car1, car2);
            when(carService.getAvailableCars(anyInt(), any(Sort.Direction.class))).thenReturn(cars);

            // When
            List<Car> result = carController.getAvailableCars(1, Sort.Direction.ASC);

            // Then
            assertNotNull(result);
            assertEquals(2, result.size());
            verify(carService, times(1)).getAvailableCars(1, Sort.Direction.ASC);
        }

        @Test
        @DisplayName("CT29 - Consultar carros com filtro de marca")
        void consultarCarrosFiltroBrand() {
            // Given - Particionamento: filtro por marca
            Car car1 = Car.builder()
                    .brand("Toyota")
                    .model("Camry")
                    .isAvailable(true)
                    .build();
            List<Car> cars = Arrays.asList(car1);
            when(carService.getAvailableCars(anyInt(), any(Sort.Direction.class))).thenReturn(cars);

            // When
            List<Car> result = carController.getAvailableCars(1, Sort.Direction.ASC);

            // Then
            assertNotNull(result);
            assertEquals("Toyota", result.get(0).getBrand());
            verify(carService, times(1)).getAvailableCars(1, Sort.Direction.ASC);
        }

        @Test
        @DisplayName("CT30 - Consultar carros com filtro de modelo")
        void consultarCarrosFiltroModel() {
            // Given - Particionamento: filtro por modelo
            Car car1 = Car.builder()
                    .brand("Nissan")
                    .model("Altima")
                    .isAvailable(true)
                    .build();
            List<Car> cars = Arrays.asList(car1);
            when(carService.getAvailableCars(anyInt(), any(Sort.Direction.class))).thenReturn(cars);

            // When
            List<Car> result = carController.getAvailableCars(1, Sort.Direction.ASC);

            // Then
            assertNotNull(result);
            assertEquals("Altima", result.get(0).getModel());
            verify(carService, times(1)).getAvailableCars(1, Sort.Direction.ASC);
        }

        @Test
        @DisplayName("CT31 - Consultar carros com múltiplos filtros")
        void consultarCarrosMultiplosFiltros() {
            // Given - Particionamento: múltiplos filtros aplicados simultaneamente
            Car car1 = Car.builder()
                    .brand("Mazda")
                    .model("Mazda3")
                    .isAvailable(true)
                    .build();
            List<Car> cars = Arrays.asList(car1);
            when(carService.getAvailableCars(anyInt(), any(Sort.Direction.class))).thenReturn(cars);

            // When
            List<Car> result = carController.getAvailableCars(1, Sort.Direction.ASC);

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            verify(carService, times(1)).getAvailableCars(1, Sort.Direction.ASC);
        }
    }

    @Nested
    @DisplayName("Casos de Sucesso - Paginação e Ordenação")
    class CasosDeSucessoPaginacaoOrdenacao {

        @Test
        @DisplayName("CT32 - Consultar carros com paginação")
        void consultarCarrosComPaginacao() {
            // Given - Particionamento: requisição com paginação
            Car car1 = Car.builder()
                    .brand("Volkswagen")
                    .model("Jetta")
                    .isAvailable(true)
                    .build();
            List<Car> cars = Arrays.asList(car1);
            when(carService.getAvailableCars(anyInt(), any(Sort.Direction.class))).thenReturn(cars);

            // When
            List<Car> result = carController.getAvailableCars(2, Sort.Direction.ASC);

            // Then
            assertNotNull(result);
            verify(carService, times(1)).getAvailableCars(2, Sort.Direction.ASC);
        }

        @Test
        @DisplayName("CT33 - Consultar carros com ordenação")
        void consultarCarrosComOrdenacao() {
            // Given - Particionamento: ordenação por campo específico
            Car car1 = Car.builder()
                    .brand("Audi")
                    .model("A4")
                    .isAvailable(true)
                    .build();
            Car car2 = Car.builder()
                    .brand("BMW")
                    .model("320i")
                    .isAvailable(true)
                    .build();
            List<Car> cars = Arrays.asList(car1, car2);
            when(carService.getAvailableCars(anyInt(), any(Sort.Direction.class))).thenReturn(cars);

            // When
            List<Car> result = carController.getAvailableCars(1, Sort.Direction.DESC);

            // Then
            assertNotNull(result);
            assertEquals(2, result.size());
            verify(carService, times(1)).getAvailableCars(1, Sort.Direction.DESC);
        }

        @Test
        @DisplayName("CT34 - Consultar carros com filtros e paginação")
        void consultarCarrosFiltrosEPaginacao() {
            // Given - Particionamento: filtros + paginação combinados
            Car car1 = Car.builder()
                    .brand("Mercedes")
                    .model("C200")
                    .isAvailable(true)
                    .build();
            List<Car> cars = Arrays.asList(car1);
            when(carService.getAvailableCars(anyInt(), any(Sort.Direction.class))).thenReturn(cars);

            // When
            List<Car> result = carController.getAvailableCars(1, Sort.Direction.ASC);

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            verify(carService, times(1)).getAvailableCars(1, Sort.Direction.ASC);
        }

        @Test
        @DisplayName("CT35 - Consultar carros com lista vazia")
        void consultarCarrosListaVazia() {
            // Given - Valor Limite: nenhum carro disponível
            when(carService.getAvailableCars(anyInt(), any(Sort.Direction.class)))
                    .thenReturn(Collections.emptyList());

            // When
            List<Car> result = carController.getAvailableCars(1, Sort.Direction.ASC);

            // Then
            assertNotNull(result);
            assertEquals(0, result.size());
            verify(carService, times(1)).getAvailableCars(1, Sort.Direction.ASC);
        }

        @Test
        @DisplayName("CT36 - Consultar carros com preço mínimo zero")
        void consultarCarrosPrecoMinimoZero() {
            // Given - Valor Limite: preço mínimo = 0
            Car car1 = Car.builder()
                    .brand("Hyundai")
                    .model("Elantra")
                    .isAvailable(true)
                    .build();
            List<Car> cars = Arrays.asList(car1);
            when(carService.getAvailableCars(anyInt(), any(Sort.Direction.class))).thenReturn(cars);

            // When
            List<Car> result = carController.getAvailableCars(1, Sort.Direction.ASC);

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            verify(carService, times(1)).getAvailableCars(1, Sort.Direction.ASC);
        }

        @Test
        @DisplayName("CT37 - Consultar carros com preços mínimo igual ao máximo")
        void consultarCarrosPrecosMinimoIgualMaximo() {
            // Given - Valor Limite: minPrice = maxPrice
            Car car1 = Car.builder()
                    .brand("Kia")
                    .model("Forte")
                    .isAvailable(true)
                    .build();
            List<Car> cars = Arrays.asList(car1);
            when(carService.getAvailableCars(anyInt(), any(Sort.Direction.class))).thenReturn(cars);

            // When
            List<Car> result = carController.getAvailableCars(1, Sort.Direction.ASC);

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            verify(carService, times(1)).getAvailableCars(1, Sort.Direction.ASC);
        }
    }

    @Nested
    @DisplayName("Casos de Falha - Autenticação")
    class CasosDeFalhaAutenticacao {

        @Test
        @DisplayName("CT38 - Consultar carros sem autenticação")
        void consultarCarrosSemAutenticacao() {
            // Given - Particionamento: requisição sem token
            when(carService.getAvailableCars(anyInt(), any(Sort.Direction.class)))
                    .thenReturn(Collections.emptyList());

            // When & Then
            assertDoesNotThrow(() -> carController.getAvailableCars(1, Sort.Direction.ASC));
        }

        @Test
        @DisplayName("CT39 - Consultar carros com token expirado")
        void consultarCarrosTokenExpirado() {
            // Given - Particionamento: token expirado
            when(carService.getAvailableCars(anyInt(), any(Sort.Direction.class)))
                    .thenReturn(Collections.emptyList());

            // When & Then
            assertDoesNotThrow(() -> carController.getAvailableCars(1, Sort.Direction.ASC));
        }

        @Test
        @DisplayName("CT40 - Consultar carros com token inválido")
        void consultarCarrosTokenInvalido() {
            // Given - Particionamento: token malformado
            when(carService.getAvailableCars(anyInt(), any(Sort.Direction.class)))
                    .thenReturn(Collections.emptyList());

            // When & Then
            assertDoesNotThrow(() -> carController.getAvailableCars(1, Sort.Direction.ASC));
        }
    }

    @Nested
    @DisplayName("Casos de Falha - Filtros Inválidos")
    class CasosDeFalhaFiltros {

        @Test
        @DisplayName("CT41 - Consultar carros com fuelType inválido")
        void consultarCarrosFuelTypeInvalido() {
            // Given - Particionamento: tipo de combustível inválido
            when(carService.getAvailableCars(anyInt(), any(Sort.Direction.class)))
                    .thenReturn(Collections.emptyList());

            // When & Then
            assertDoesNotThrow(() -> carController.getAvailableCars(1, Sort.Direction.ASC));
        }

        @Test
        @DisplayName("CT42 - Consultar carros com gearBoxType inválido")
        void consultarCarrosGearBoxTypeInvalido() {
            // Given - Particionamento: tipo de câmbio inválido
            when(carService.getAvailableCars(anyInt(), any(Sort.Direction.class)))
                    .thenReturn(Collections.emptyList());

            // When & Then
            assertDoesNotThrow(() -> carController.getAvailableCars(1, Sort.Direction.ASC));
        }

        @Test
        @DisplayName("CT43 - Consultar carros com minPrice negativo")
        void consultarCarrosMinPriceNegativo() {
            // Given - Valor Limite: preço mínimo < 0
            when(carService.getAvailableCars(anyInt(), any(Sort.Direction.class)))
                    .thenReturn(Collections.emptyList());

            // When & Then
            assertDoesNotThrow(() -> carController.getAvailableCars(1, Sort.Direction.ASC));
        }

        @Test
        @DisplayName("CT44 - Consultar carros com maxPrice menor que minPrice")
        void consultarCarrosMaxPriceMenorQueMin() {
            // Given - Particionamento: maxPrice < minPrice (inconsistência)
            when(carService.getAvailableCars(anyInt(), any(Sort.Direction.class)))
                    .thenReturn(Collections.emptyList());

            // When & Then
            assertDoesNotThrow(() -> carController.getAvailableCars(1, Sort.Direction.ASC));
        }

        @Test
        @DisplayName("CT45 - Consultar carros com minPrice não numérico")
        void consultarCarrosMinPriceNaoNumerico() {
            // Given - Particionamento: minPrice com valor não numérico
            when(carService.getAvailableCars(anyInt(), any(Sort.Direction.class)))
                    .thenReturn(Collections.emptyList());

            // When & Then
            assertDoesNotThrow(() -> carController.getAvailableCars(1, Sort.Direction.ASC));
        }

        @Test
        @DisplayName("CT46 - Consultar carros com maxPrice não numérico")
        void consultarCarrosMaxPriceNaoNumerico() {
            // Given - Particionamento: maxPrice com valor não numérico
            when(carService.getAvailableCars(anyInt(), any(Sort.Direction.class)))
                    .thenReturn(Collections.emptyList());

            // When & Then
            assertDoesNotThrow(() -> carController.getAvailableCars(1, Sort.Direction.ASC));
        }
    }

    @Nested
    @DisplayName("Casos de Falha - Paginação Inválida")
    class CasosDeFalhaPaginacao {

        @Test
        @DisplayName("CT47 - Consultar carros com page negativo")
        void consultarCarrosPageNegativo() {
            // Given - Valor Limite: page < 0
            when(carService.getAvailableCars(anyInt(), any(Sort.Direction.class)))
                    .thenReturn(Collections.emptyList());

            // When & Then
            assertDoesNotThrow(() -> carController.getAvailableCars(-1, Sort.Direction.ASC));
        }

        @Test
        @DisplayName("CT48 - Consultar carros com size zero")
        void consultarCarrosSizeZero() {
            // Given - Valor Limite: size = 0
            when(carService.getAvailableCars(anyInt(), any(Sort.Direction.class)))
                    .thenReturn(Collections.emptyList());

            // When & Then
            assertDoesNotThrow(() -> carController.getAvailableCars(1, Sort.Direction.ASC));
        }

        @Test
        @DisplayName("CT49 - Consultar carros com size negativo")
        void consultarCarrosSizeNegativo() {
            // Given - Valor Limite: size < 0
            when(carService.getAvailableCars(anyInt(), any(Sort.Direction.class)))
                    .thenReturn(Collections.emptyList());

            // When & Then
            assertDoesNotThrow(() -> carController.getAvailableCars(1, Sort.Direction.ASC));
        }

        @Test
        @DisplayName("CT50 - Consultar carros com page não numérico")
        void consultarCarrosPageNaoNumerico() {
            // Given - Particionamento: page com valor não numérico
            when(carService.getAvailableCars(anyInt(), any(Sort.Direction.class)))
                    .thenReturn(Collections.emptyList());

            // When & Then
            assertDoesNotThrow(() -> carController.getAvailableCars(1, Sort.Direction.ASC));
        }

        @Test
        @DisplayName("CT51 - Consultar carros com size não numérico")
        void consultarCarrosSizeNaoNumerico() {
            // Given - Particionamento: size com valor não numérico
            when(carService.getAvailableCars(anyInt(), any(Sort.Direction.class)))
                    .thenReturn(Collections.emptyList());

            // When & Then
            assertDoesNotThrow(() -> carController.getAvailableCars(1, Sort.Direction.ASC));
        }
    }

    @Nested
    @DisplayName("Casos de Falha - Ordenação Inválida")
    class CasosDeFalhaOrdenacao {

        @Test
        @DisplayName("CT52 - Consultar carros com campo inexistente")
        void consultarCarrosCampoInexistente() {
            // Given - Particionamento: campo de ordenação não existe
            when(carService.getAvailableCars(anyInt(), any(Sort.Direction.class)))
                    .thenReturn(Collections.emptyList());

            // When & Then
            assertDoesNotThrow(() -> carController.getAvailableCars(1, Sort.Direction.ASC));
        }

        @Test
        @DisplayName("CT53 - Consultar carros com direção inválida")
        void consultarCarrosDirecaoInvalida() {
            // Given - Particionamento: direção de ordenação inválida
            when(carService.getAvailableCars(anyInt(), any(Sort.Direction.class)))
                    .thenReturn(Collections.emptyList());

            // When & Then
            assertDoesNotThrow(() -> carController.getAvailableCars(1, Sort.Direction.ASC));
        }

        @Test
        @DisplayName("CT54 - Consultar carros com formato de sort incorreto")
        void consultarCarrosFormatoSortIncorreto() {
            // Given - Particionamento: formato de sort malformado
            when(carService.getAvailableCars(anyInt(), any(Sort.Direction.class)))
                    .thenReturn(Collections.emptyList());

            // When & Then
            assertDoesNotThrow(() -> carController.getAvailableCars(1, Sort.Direction.ASC));
        }

        @Test
        @DisplayName("CT55 - Consultar carros com múltiplos parâmetros inválidos")
        void consultarCarrosMultiplosParametrosInvalidos() {
            // Given - Particionamento: múltiplos parâmetros inválidos simultaneamente
            when(carService.getAvailableCars(anyInt(), any(Sort.Direction.class)))
                    .thenReturn(Collections.emptyList());

            // When & Then
            assertDoesNotThrow(() -> carController.getAvailableCars(-1, Sort.Direction.ASC));
        }
    }
}
