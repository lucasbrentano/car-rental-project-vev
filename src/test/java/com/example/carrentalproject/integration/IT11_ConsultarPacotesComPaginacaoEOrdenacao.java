package com.example.carrentalproject.integration;

import com.example.carrentalproject.domain.CarPackage;
import com.example.carrentalproject.repository.CarPackageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * IT11 - Consultar Pacotes Com Paginação E Ordenação
 * 
 * Objetivo: Validar funcionalidade de paginação e ordenação do Spring Data JPA
 * ao consultar CarPackage através do CarPackageRepository.
 * 
 * Cenário:
 * 1. Criar 10 pacotes com preços variados
 * 2. Consultar página 0, tamanho 5, ordenado por pricePerHour ASC
 * 3. Validar que retorna no máximo 5 pacotes
 * 4. Validar ordenação crescente de preços
 * 5. Validar metadados de paginação (totalElements, totalPages, hasNext)
 * 
 * Validações:
 * - Pageable do Spring Data JPA funciona corretamente
 * - Sort por campo pricePerHour em ordem ascendente
 * - Page metadata (size, totalElements, totalPages, hasNext, hasPrevious)
 * - Limites de página respeitados (máximo 5 elementos)
 * - Ordenação correta dos resultados
 * 
 * Integração testada:
 * @SpringBootTest: Contexto completo da aplicação
 * @Transactional: Transação real com rollback automático
 * @Autowired: CarPackageRepository real (não mockado)
 * Spring Data JPA: findAll(Pageable), Page<T>, Sort
 */
@SpringBootTest
@Transactional
@DisplayName("IT11 - Consultar Pacotes Com Paginação E Ordenação")
public class IT11_ConsultarPacotesComPaginacaoEOrdenacao {

    @Autowired
    private CarPackageRepository carPackageRepository;

    @BeforeEach
    void setUp() {
        // Limpar repositório
        carPackageRepository.deleteAll();

        // Criar 10 pacotes com preços variados (não ordenados propositalmente)
        carPackageRepository.save(CarPackage.builder().packageName("Economy").pricePerHour(50).build());
        carPackageRepository.save(CarPackage.builder().packageName("Premium").pricePerHour(500).build());
        carPackageRepository.save(CarPackage.builder().packageName("Basic").pricePerHour(100).build());
        carPackageRepository.save(CarPackage.builder().packageName("Luxury").pricePerHour(800).build());
        carPackageRepository.save(CarPackage.builder().packageName("Standard").pricePerHour(150).build());
        carPackageRepository.save(CarPackage.builder().packageName("Sport").pricePerHour(600).build());
        carPackageRepository.save(CarPackage.builder().packageName("Compact").pricePerHour(80).build());
        carPackageRepository.save(CarPackage.builder().packageName("Executive").pricePerHour(700).build());
        carPackageRepository.save(CarPackage.builder().packageName("Family").pricePerHour(200).build());
        carPackageRepository.save(CarPackage.builder().packageName("Business").pricePerHour(400).build());

        System.out.println("✓ Setup completo: 10 pacotes criados com preços variados");
    }

    @Test
    @DisplayName("Deve retornar página 0 com tamanho 5, ordenado por preço ASC")
    void deveConsultarPacotesComPaginacaoEOrdenacao() {
        // Given: Pageable para página 0, tamanho 5, ordenado por pricePerHour ASC
        Pageable pageable = PageRequest.of(0, 5, Sort.by(Sort.Direction.ASC, "pricePerHour"));

        // When: Consultar pacotes com paginação
        Page<CarPackage> page = carPackageRepository.findAll(pageable);

        // Then: Validar metadados de paginação
        assertNotNull(page);
        assertEquals(10, page.getTotalElements(), "Total de elementos deve ser 10");
        assertEquals(2, page.getTotalPages(), "Total de páginas deve ser 2 (10 elementos / 5 por página)");
        assertEquals(0, page.getNumber(), "Número da página atual deve ser 0");
        assertEquals(5, page.getSize(), "Tamanho da página deve ser 5");
        assertTrue(page.hasNext(), "Deve haver próxima página");
        assertFalse(page.hasPrevious(), "Não deve haver página anterior (página 0)");
        System.out.println("✓ Metadados de paginação validados");

        // Validar conteúdo da página
        List<CarPackage> packages = page.getContent();
        assertEquals(5, packages.size(), "Deve retornar exatamente 5 pacotes");
        System.out.println("✓ Retornou 5 pacotes conforme esperado");

        // Validar ordenação crescente por pricePerHour
        for (int i = 0; i < packages.size() - 1; i++) {
            Integer currentPrice = packages.get(i).getPricePerHour();
            Integer nextPrice = packages.get(i + 1).getPricePerHour();
            assertTrue(currentPrice <= nextPrice, 
                    "Preço deve estar em ordem crescente: " + currentPrice + " <= " + nextPrice);
        }
        System.out.println("✓ Ordenação crescente por pricePerHour validada");

        // Validar preços esperados na primeira página (5 menores preços)
        assertEquals(50, packages.get(0).getPricePerHour(), "1º pacote: Economy (50)");
        assertEquals(80, packages.get(1).getPricePerHour(), "2º pacote: Compact (80)");
        assertEquals(100, packages.get(2).getPricePerHour(), "3º pacote: Basic (100)");
        assertEquals(150, packages.get(3).getPricePerHour(), "4º pacote: Standard (150)");
        assertEquals(200, packages.get(4).getPricePerHour(), "5º pacote: Family (200)");
        System.out.println("✓ Preços dos 5 primeiros pacotes validados (50, 80, 100, 150, 200)");
    }

    @Test
    @DisplayName("Deve retornar página 1 com tamanho 5, ordenado por preço ASC")
    void deveConsultarSegundaPaginaOrdenadaPorPreco() {
        // Given: Pageable para página 1, tamanho 5, ordenado por pricePerHour ASC
        Pageable pageable = PageRequest.of(1, 5, Sort.by(Sort.Direction.ASC, "pricePerHour"));

        // When: Consultar segunda página
        Page<CarPackage> page = carPackageRepository.findAll(pageable);

        // Then: Validar metadados
        assertNotNull(page);
        assertEquals(10, page.getTotalElements());
        assertEquals(2, page.getTotalPages());
        assertEquals(1, page.getNumber(), "Número da página atual deve ser 1");
        assertEquals(5, page.getSize());
        assertFalse(page.hasNext(), "Não deve haver próxima página (última página)");
        assertTrue(page.hasPrevious(), "Deve haver página anterior");
        System.out.println("✓ Metadados da página 1 validados");

        // Validar conteúdo
        List<CarPackage> packages = page.getContent();
        assertEquals(5, packages.size(), "Deve retornar exatamente 5 pacotes");

        // Validar ordenação
        for (int i = 0; i < packages.size() - 1; i++) {
            assertTrue(packages.get(i).getPricePerHour() <= packages.get(i + 1).getPricePerHour());
        }
        System.out.println("✓ Ordenação crescente validada na página 1");

        // Validar preços esperados na segunda página (5 maiores preços)
        assertEquals(400, packages.get(0).getPricePerHour(), "1º pacote: Business (400)");
        assertEquals(500, packages.get(1).getPricePerHour(), "2º pacote: Premium (500)");
        assertEquals(600, packages.get(2).getPricePerHour(), "3º pacote: Sport (600)");
        assertEquals(700, packages.get(3).getPricePerHour(), "4º pacote: Executive (700)");
        assertEquals(800, packages.get(4).getPricePerHour(), "5º pacote: Luxury (800)");
        System.out.println("✓ Preços dos últimos 5 pacotes validados (400, 500, 600, 700, 800)");
    }

    @Test
    @DisplayName("Deve retornar página 0 com tamanho 3, ordenado por preço DESC")
    void deveConsultarPacotesOrdenadoPorPrecoDescendente() {
        // Given: Pageable para página 0, tamanho 3, ordenado por pricePerHour DESC
        Pageable pageable = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "pricePerHour"));

        // When: Consultar pacotes
        Page<CarPackage> page = carPackageRepository.findAll(pageable);

        // Then: Validar metadados
        assertNotNull(page);
        assertEquals(10, page.getTotalElements());
        assertEquals(4, page.getTotalPages(), "Total de páginas deve ser 4 (10 elementos / 3 por página)");
        assertEquals(3, page.getSize());
        assertTrue(page.hasNext());
        System.out.println("✓ Metadados com tamanho 3 validados");

        // Validar conteúdo
        List<CarPackage> packages = page.getContent();
        assertEquals(3, packages.size(), "Deve retornar exatamente 3 pacotes");

        // Validar ordenação decrescente
        for (int i = 0; i < packages.size() - 1; i++) {
            Integer currentPrice = packages.get(i).getPricePerHour();
            Integer nextPrice = packages.get(i + 1).getPricePerHour();
            assertTrue(currentPrice >= nextPrice, 
                    "Preço deve estar em ordem decrescente: " + currentPrice + " >= " + nextPrice);
        }
        System.out.println("✓ Ordenação decrescente por pricePerHour validada");

        // Validar preços esperados (3 maiores preços)
        assertEquals(800, packages.get(0).getPricePerHour(), "1º pacote: Luxury (800)");
        assertEquals(700, packages.get(1).getPricePerHour(), "2º pacote: Executive (700)");
        assertEquals(600, packages.get(2).getPricePerHour(), "3º pacote: Sport (600)");
        System.out.println("✓ Preços dos 3 primeiros pacotes em ordem DESC validados (800, 700, 600)");
    }

    @Test
    @DisplayName("Deve retornar página vazia quando solicitar além do limite")
    void deveRetornarPaginaVaziaAlemDoLimite() {
        // Given: Pageable para página 5 (além do limite de 2 páginas)
        Pageable pageable = PageRequest.of(5, 5, Sort.by(Sort.Direction.ASC, "pricePerHour"));

        // When: Consultar página além do limite
        Page<CarPackage> page = carPackageRepository.findAll(pageable);

        // Then: Deve retornar página vazia
        assertNotNull(page);
        assertEquals(10, page.getTotalElements(), "Total de elementos permanece 10");
        assertEquals(2, page.getTotalPages(), "Total de páginas permanece 2");
        assertEquals(0, page.getContent().size(), "Conteúdo deve estar vazio");
        assertFalse(page.hasNext(), "Não deve haver próxima página");
        assertTrue(page.hasPrevious(), "Deve haver páginas anteriores");
        System.out.println("✓ Página vazia retornada corretamente ao solicitar além do limite");
    }

    @Test
    @DisplayName("Deve retornar todos os pacotes sem paginação")
    void deveConsultarTodosPacotesSemPaginacao() {
        // Given: Pageable com tamanho 100 (maior que total de elementos)
        Pageable pageable = PageRequest.of(0, 100, Sort.by(Sort.Direction.ASC, "pricePerHour"));

        // When: Consultar todos os pacotes
        Page<CarPackage> page = carPackageRepository.findAll(pageable);

        // Then: Deve retornar todos os 10 pacotes
        assertNotNull(page);
        assertEquals(10, page.getTotalElements());
        assertEquals(1, page.getTotalPages(), "Deve ter apenas 1 página");
        assertEquals(10, page.getContent().size(), "Deve retornar todos os 10 pacotes");
        assertFalse(page.hasNext());
        assertFalse(page.hasPrevious());
        System.out.println("✓ Todos os 10 pacotes retornados em uma única página");

        // Validar ordenação completa
        List<CarPackage> packages = page.getContent();
        assertEquals(50, packages.get(0).getPricePerHour());
        assertEquals(800, packages.get(9).getPricePerHour());
        System.out.println("✓ Ordenação completa validada (50 a 800)");
    }
}
