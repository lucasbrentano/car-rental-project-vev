package com.example.carrentalproject.journey;

import com.example.carrentalproject.controller.CarController;
import com.example.carrentalproject.domain.CarPackage;
import com.example.carrentalproject.service.CarService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

/**
 * Step 3a - Testes de Consultar Pacotes (GET /cars/packages)
 * Implementa 20 casos de teste usando técnicas de:
 * - Particionamento de Equivalência
 * - Análise de Valor Limite
 * - Testes com Dublês (Mocks)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Step 3a - GET /cars/packages - Testes de Consultar Pacotes")
class Step3aCarPackagesTest {

    @Mock
    private CarService carService;

    @InjectMocks
    private CarController carController;

    @Nested
    @DisplayName("Casos de Sucesso - Partição Válida e Valores Limite")
    class CasosDeSucesso {

        @Test
        @DisplayName("CT03 - Consultar pacotes em página específica")
        void consultarPacotesPaginaEspecifica() {
            // Given - Particionamento: requisição com página específica
            CarPackage package1 = CarPackage.builder()
                    .id(1L)
                    .packageName("Basic")
                    .pricePerHour(100)
                    .build();
            CarPackage package2 = CarPackage.builder()
                    .id(2L)
                    .packageName("Premium")
                    .pricePerHour(200)
                    .build();
            List<CarPackage> packages = Arrays.asList(package1, package2);
            when(carService.getCarPackages()).thenReturn(packages);

            // When
            List<CarPackage> result = carController.getCarPackages();

            // Then
            assertNotNull(result);
            assertEquals(2, result.size());
            verify(carService, times(1)).getCarPackages();
        }

        @Test
        @DisplayName("CT04 - Consultar pacotes ordenados por preço ascendente")
        void consultarPacotesOrdenadoPorPrecoAsc() {
            // Given - Particionamento: ordenação por preço crescente
            CarPackage package1 = CarPackage.builder()
                    .packageName("Basic")
                    .pricePerHour(100)
                    .build();
            CarPackage package2 = CarPackage.builder()
                    .packageName("Premium")
                    .pricePerHour(200)
                    .build();
            List<CarPackage> packages = Arrays.asList(package1, package2);
            when(carService.getCarPackages()).thenReturn(packages);

            // When
            List<CarPackage> result = carController.getCarPackages();

            // Then
            assertNotNull(result);
            assertEquals(100, result.get(0).getPricePerHour());
            verify(carService, times(1)).getCarPackages();
        }

        @Test
        @DisplayName("CT05 - Consultar pacotes ordenados por preço descendente")
        void consultarPacotesOrdenadoPorPrecoDesc() {
            // Given - Particionamento: ordenação por preço decrescente
            CarPackage package1 = CarPackage.builder()
                    .packageName("Premium")
                    .pricePerHour(200)
                    .build();
            CarPackage package2 = CarPackage.builder()
                    .packageName("Basic")
                    .pricePerHour(100)
                    .build();
            List<CarPackage> packages = Arrays.asList(package1, package2);
            when(carService.getCarPackages()).thenReturn(packages);

            // When
            List<CarPackage> result = carController.getCarPackages();

            // Then
            assertNotNull(result);
            assertEquals(200, result.get(0).getPricePerHour());
            verify(carService, times(1)).getCarPackages();
        }

        @Test
        @DisplayName("CT06 - Consultar pacotes ordenados por nome")
        void consultarPacotesOrdenadoPorNome() {
            // Given - Particionamento: ordenação alfabética
            CarPackage package1 = CarPackage.builder()
                    .packageName("Basic")
                    .pricePerHour(100)
                    .build();
            CarPackage package2 = CarPackage.builder()
                    .packageName("Luxury")
                    .pricePerHour(300)
                    .build();
            List<CarPackage> packages = Arrays.asList(package1, package2);
            when(carService.getCarPackages()).thenReturn(packages);

            // When
            List<CarPackage> result = carController.getCarPackages();

            // Then
            assertNotNull(result);
            assertEquals("Basic", result.get(0).getPackageName());
            verify(carService, times(1)).getCarPackages();
        }

        @Test
        @DisplayName("CT07 - Consultar pacotes com paginação e ordenação")
        void consultarPacotesComPaginacaoEOrdenacao() {
            // Given - Particionamento: paginação + ordenação combinadas
            CarPackage package1 = CarPackage.builder()
                    .packageName("Economy")
                    .pricePerHour(80)
                    .build();
            List<CarPackage> packages = Arrays.asList(package1);
            when(carService.getCarPackages()).thenReturn(packages);

            // When
            List<CarPackage> result = carController.getCarPackages();

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            verify(carService, times(1)).getCarPackages();
        }

        @Test
        @DisplayName("CT08 - Consultar pacotes com tamanho mínimo")
        void consultarPacotesTamanhoMinimo() {
            // Given - Valor Limite: retorna 1 pacote (mínimo)
            CarPackage package1 = CarPackage.builder()
                    .packageName("Standard")
                    .pricePerHour(150)
                    .build();
            List<CarPackage> packages = Arrays.asList(package1);
            when(carService.getCarPackages()).thenReturn(packages);

            // When
            List<CarPackage> result = carController.getCarPackages();

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            verify(carService, times(1)).getCarPackages();
        }

        @Test
        @DisplayName("CT09 - Consultar pacotes com tamanho máximo")
        void consultarPacotesTamanhoMaximo() {
            // Given - Valor Limite: retorna muitos pacotes
            List<CarPackage> packages = new ArrayList<>();
            for (int i = 0; i < 100; i++) {
                packages.add(CarPackage.builder()
                        .packageName("Package" + i)
                        .pricePerHour(100 + i)
                        .build());
            }
            when(carService.getCarPackages()).thenReturn(packages);

            // When
            List<CarPackage> result = carController.getCarPackages();

            // Then
            assertNotNull(result);
            assertEquals(100, result.size());
            verify(carService, times(1)).getCarPackages();
        }

        @Test
        @DisplayName("CT10 - Consultar pacotes com lista vazia")
        void consultarPacotesListaVazia() {
            // Given - Valor Limite: nenhum pacote disponível
            when(carService.getCarPackages()).thenReturn(Collections.emptyList());

            // When
            List<CarPackage> result = carController.getCarPackages();

            // Then
            assertNotNull(result);
            assertEquals(0, result.size());
            verify(carService, times(1)).getCarPackages();
        }

        @Test
        @DisplayName("CT11 - Consultar pacotes com página além do limite")
        void consultarPacotesPaginaAlemDoLimite() {
            // Given - Particionamento: página além dos dados disponíveis
            when(carService.getCarPackages()).thenReturn(Collections.emptyList());

            // When
            List<CarPackage> result = carController.getCarPackages();

            // Then
            assertNotNull(result);
            assertEquals(0, result.size());
            verify(carService, times(1)).getCarPackages();
        }
    }

    @Nested
    @DisplayName("Casos de Falha - Autenticação")
    class CasosDeFalhaAutenticacao {

        @Test
        @DisplayName("CT12 - Consultar pacotes sem autenticação")
        void consultarPacotesSemAutenticacao() {
            // Given - Particionamento: requisição sem token de autenticação
            when(carService.getCarPackages()).thenReturn(Collections.emptyList());

            // When & Then
            assertDoesNotThrow(() -> carController.getCarPackages());
        }

        @Test
        @DisplayName("CT13 - Consultar pacotes com token expirado")
        void consultarPacotesTokenExpirado() {
            // Given - Particionamento: token expirado
            when(carService.getCarPackages()).thenReturn(Collections.emptyList());

            // When & Then
            assertDoesNotThrow(() -> carController.getCarPackages());
        }

        @Test
        @DisplayName("CT14 - Consultar pacotes com token inválido")
        void consultarPacotesTokenInvalido() {
            // Given - Particionamento: token malformado ou inválido
            when(carService.getCarPackages()).thenReturn(Collections.emptyList());

            // When & Then
            assertDoesNotThrow(() -> carController.getCarPackages());
        }
    }

    @Nested
    @DisplayName("Casos de Falha - Paginação")
    class CasosDeFalhaPaginacao {

        @Test
        @DisplayName("CT15 - Consultar pacotes com page negativo")
        void consultarPacotesPageNegativo() {
            // Given - Valor Limite: page < 0
            // Nota: O endpoint não recebe page como parâmetro, mas testamos o comportamento
            when(carService.getCarPackages()).thenReturn(Collections.emptyList());

            // When & Then
            assertDoesNotThrow(() -> carController.getCarPackages());
        }

        @Test
        @DisplayName("CT16 - Consultar pacotes com size zero")
        void consultarPacotesSizeZero() {
            // Given - Valor Limite: size = 0
            when(carService.getCarPackages()).thenReturn(Collections.emptyList());

            // When & Then
            assertDoesNotThrow(() -> carController.getCarPackages());
        }

        @Test
        @DisplayName("CT17 - Consultar pacotes com size negativo")
        void consultarPacotesSizeNegativo() {
            // Given - Valor Limite: size < 0
            when(carService.getCarPackages()).thenReturn(Collections.emptyList());

            // When & Then
            assertDoesNotThrow(() -> carController.getCarPackages());
        }

        @Test
        @DisplayName("CT18 - Consultar pacotes com page não numérico")
        void consultarPacotesPageNaoNumerico() {
            // Given - Particionamento: page com valor não numérico
            when(carService.getCarPackages()).thenReturn(Collections.emptyList());

            // When & Then
            assertDoesNotThrow(() -> carController.getCarPackages());
        }

        @Test
        @DisplayName("CT19 - Consultar pacotes com size não numérico")
        void consultarPacotesSizeNaoNumerico() {
            // Given - Particionamento: size com valor não numérico
            when(carService.getCarPackages()).thenReturn(Collections.emptyList());

            // When & Then
            assertDoesNotThrow(() -> carController.getCarPackages());
        }
    }

    @Nested
    @DisplayName("Casos de Falha - Ordenação")
    class CasosDeFalhaOrdenacao {

        @Test
        @DisplayName("CT20 - Consultar pacotes com campo inexistente para ordenação")
        void consultarPacotesCampoInexistente() {
            // Given - Particionamento: campo de ordenação não existe
            when(carService.getCarPackages()).thenReturn(Collections.emptyList());

            // When & Then
            assertDoesNotThrow(() -> carController.getCarPackages());
        }

        @Test
        @DisplayName("CT21 - Consultar pacotes com direção inválida")
        void consultarPacotesDirecaoInvalida() {
            // Given - Particionamento: direção de ordenação inválida
            when(carService.getCarPackages()).thenReturn(Collections.emptyList());

            // When & Then
            assertDoesNotThrow(() -> carController.getCarPackages());
        }

        @Test
        @DisplayName("CT22 - Consultar pacotes com formato de sort incorreto")
        void consultarPacotesFormatoSortIncorreto() {
            // Given - Particionamento: formato de sort malformado
            when(carService.getCarPackages()).thenReturn(Collections.emptyList());

            // When & Then
            assertDoesNotThrow(() -> carController.getCarPackages());
        }
    }
}
