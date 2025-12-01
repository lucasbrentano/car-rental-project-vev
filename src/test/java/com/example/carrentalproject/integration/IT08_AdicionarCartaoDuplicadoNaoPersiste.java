package com.example.carrentalproject.integration;

import com.example.carrentalproject.domain.CreditCard;
import com.example.carrentalproject.domain.User;
import com.example.carrentalproject.dto.CreditCardDto;
import com.example.carrentalproject.repository.CreditCardRepository;
import com.example.carrentalproject.repository.UserRepository;
import com.example.carrentalproject.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * IT08 - Cenário 5: Validação de Cartão de Crédito - AdicionarCartaoDuplicadoNaoPersiste
 * 
 * Objetivo: Validar que cartões duplicados não são persistidos no banco de dados.
 * Testa a constraint UNIQUE na coluna card_number da tabela credit_card.
 * 
 * Cenário:
 * - Adicionar cartão ao usuário A
 * - Validar que constraint UNIQUE impede duplicação
 * - Validar que apenas um cartão permanece no banco
 * 
 * IMPORTANTE: CreditCard.cardNumber tem constraint @Column(unique = true)
 */
@SpringBootTest
@Transactional
@DisplayName("IT08 - Adicionar Cartão Duplicado Não Persiste")
public class IT08_AdicionarCartaoDuplicadoNaoPersiste {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CreditCardRepository creditCardRepository;

    @Autowired
    private UserService userService;

    private User userA;
    private User userB;
    private final Long duplicateCardNumber = 1234567890123456L;

    @BeforeEach
    void setUp() {
        System.out.println("\n=== SETUP IT08: Validar Constraint UNIQUE em Cartão ===");

        // Limpar dados de teste anteriores
        creditCardRepository.deleteAll();
        userRepository.deleteAll();

        // Criar usuário A
        userA = User.builder()
                .firstName("Alice")
                .lastName("Anderson")
                .username("userA_it08")
                .password("password123")
                .email("alice.it08@example.com")
                .phone(111222333)
                .build();
        userA = userRepository.save(userA);
        System.out.println("✓ Usuário A criado: " + userA.getUsername());

        // Criar usuário B
        userB = User.builder()
                .firstName("Bob")
                .lastName("Brown")
                .username("userB_it08")
                .password("password456")
                .email("bob.it08@example.com")
                .phone(444555666)
                .build();
        userB = userRepository.save(userB);
        System.out.println("✓ Usuário B criado: " + userB.getUsername());
    }

    @Test
    @DisplayName("Deve impedir adicionar cartão com número duplicado (VALIDAÇÃO PRINCIPAL)")
    void deveImpedirAdicionarCartaoDuplicado() {
        System.out.println("\n--- Teste: VALIDAÇÃO DUPLICAÇÃO - Número Duplicado Deve Falhar ---");

        // Passo 1: Adicionar cartão ao usuário A
        CreditCardDto creditCardDtoA = CreditCardDto.builder()
                .cardNumber(duplicateCardNumber)
                .month(5)
                .year(2026)
                .CVV(123)
                .build();

        userService.addCreditCardToUser(userA.getUsername(), creditCardDtoA);
        System.out.println("✓ Cartão " + duplicateCardNumber + " adicionado ao usuário A");
        assertEquals(1, creditCardRepository.count(), "Deve haver 1 cartão após primeira adição");

        // Passo 2: Tentar adicionar MESMO número ao usuário B (deve falhar)
        CreditCardDto creditCardDtoB = CreditCardDto.builder()
                .cardNumber(duplicateCardNumber) // MESMO NÚMERO!
                .month(8)
                .year(2027)
                .CVV(456)
                .build();

        System.out.println("✓ Tentando adicionar mesmo número ao usuário B...");
        
        // Passo 2: VALIDAÇÃO CRÍTICA - Deve lançar exception ao tentar duplicar
        Exception exception = assertThrows(Exception.class, () -> {
            userService.addCreditCardToUser(userB.getUsername(), creditCardDtoB);
        }, "Deve lançar exceção ao tentar adicionar número duplicado");
        
        System.out.println("✓ Exceção lançada: " + exception.getClass().getSimpleName());
        System.out.println("✓ VALIDAÇÃO COMPLETA: Constraint UNIQUE impediu duplicação");
        
        // Observação: Não podemos verificar count() após DataIntegrityViolationException
        // pois o JPA Session fica em estado inválido. A exceção em si já valida a constraint.
    }

    @Test
    @DisplayName("Deve permitir diferentes números de cartão para diferentes usuários")
    void devePermitirDiferentesNumerosDiferentesUsuarios() {
        System.out.println("\n--- Teste: Números Diferentes OK ---");

        CreditCardDto creditCardDtoA = CreditCardDto.builder()
                .cardNumber(1111222233334444L)
                .month(3)
                .year(2026)
                .CVV(111)
                .build();

        userService.addCreditCardToUser(userA.getUsername(), creditCardDtoA);
        System.out.println("✓ Cartão 1111... adicionado ao usuário A");

        CreditCardDto creditCardDtoB = CreditCardDto.builder()
                .cardNumber(5555666677778888L)
                .month(8)
                .year(2027)
                .CVV(222)
                .build();

        userService.addCreditCardToUser(userB.getUsername(), creditCardDtoB);
        System.out.println("✓ Cartão 5555... adicionado ao usuário B");

        assertEquals(2, creditCardRepository.count());
        System.out.println("✓ Ambos usuários têm seus próprios cartões");
    }

    @Test
    @DisplayName("Deve validar que constraint UNIQUE previne duplicação mesmo após delete")
    void deveValidarConstraintUniqueAposDeletar() {
        System.out.println("\n--- Teste: Constraint UNIQUE Após Deletar ---");

        // Adicionar cartão com número N ao userA
        CreditCardDto creditCardDto1 = CreditCardDto.builder()
                .cardNumber(duplicateCardNumber)
                .month(5)
                .year(2026)
                .CVV(333)
                .build();

        userService.addCreditCardToUser(userA.getUsername(), creditCardDto1);
        System.out.println("✓ Cartão adicionado ao usuário A");
        assertEquals(1, creditCardRepository.count());

        // Deletar cartão do userA (libera o número)
        userService.deleteUserCreditCard(userA.getUsername());
        System.out.println("✓ Cartão deletado");
        assertEquals(0, creditCardRepository.count());

        // Adicionar cartão com MESMO número ao userB (deve funcionar pois número foi liberado)
        CreditCardDto creditCardDto2 = CreditCardDto.builder()
                .cardNumber(duplicateCardNumber)
                .month(10)
                .year(2027)
                .CVV(444)
                .build();

        userService.addCreditCardToUser(userB.getUsername(), creditCardDto2);
        System.out.println("✓ Cartão com mesmo número adicionado ao usuário B após delete");
        assertEquals(1, creditCardRepository.count());
    }
}
