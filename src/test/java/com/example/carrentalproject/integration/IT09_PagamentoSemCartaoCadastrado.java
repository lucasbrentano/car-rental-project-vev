package com.example.carrentalproject.integration;

import com.example.carrentalproject.domain.User;
import com.example.carrentalproject.dto.CreditCardDto;
import com.example.carrentalproject.exception.NoCreditCardException;
import com.example.carrentalproject.repository.CreditCardRepository;
import com.example.carrentalproject.repository.UserRepository;
import com.example.carrentalproject.security.LoggedInUser;
import com.example.carrentalproject.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * IT09 - Cenário 5: Validação de Cartão de Crédito - PagamentoSemCartaoCadastrado
 * 
 * Objetivo: Validar que não é possível realizar pagamento sem ter cartão cadastrado.
 * Testa a regra de negócio que exige CreditCard antes de fazer moneyTransfer.
 * 
 * Cenário:
 * - Criar usuário e pedido
 * - Tentar transferir dinheiro sem ter cartão cadastrado
 * - Esperar NoCreditCardException
 * - Validar que nenhuma transação é processada
 * 
 * IMPORTANTE: PaymentService.moneyTransfer() valida:
 * 1. user.getCreditCard() != null (lança NoCreditCardException)
 * 2. Se cartão existe, adiciona moneyAmount ao accountBalance
 * 
 * Validações:
 * 1. Regra de negócio: cartão é pré-requisito para pagamento
 * 2. Tratamento de exceção NoCreditCardException
 * 3. Estado do usuário permanece consistente (sem cartão)
 * 4. Nenhuma transação financeira é processada sem cartão
 */
@SpringBootTest
@Transactional
@DisplayName("IT09 - Pagamento Sem Cartão Cadastrado")
public class IT09_PagamentoSemCartaoCadastrado {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CreditCardRepository creditCardRepository;

    @Autowired
    private PaymentService paymentService;

    @MockBean
    private LoggedInUser loggedInUser;

    private User userSemCartao;

    @BeforeEach
    void setUp() {
        System.out.println("\n=== SETUP IT09: Validar Pagamento Sem Cartão ===");

        // Limpar dados de teste anteriores
        creditCardRepository.deleteAll();
        userRepository.deleteAll();

        // Criar usuário SEM cartão de crédito
        userSemCartao = User.builder()
                .firstName("Charlie")
                .lastName("CardlessUser")
                .username("user_no_card_it09")
                .password("password123")
                .email("charlie.it09@example.com")
                .phone(777888999)
                .build();
        userSemCartao = userRepository.save(userSemCartao);
        System.out.println("✓ Usuário sem cartão criado: " + userSemCartao.getUsername());
    }

    @Test
    @DisplayName("Deve lançar NoCreditCardException ao tentar transferir sem cartão")
    void deveLancarExcecaoAoTransferirSemCartao() {
        System.out.println("\n--- Teste: Transferir Sem Cartão ---");

        // Mockar LoggedInUser para retornar usuário sem cartão
        when(loggedInUser.getUser()).thenReturn(userSemCartao);

        // Tentar transferir dinheiro
        NoCreditCardException exception = assertThrows(NoCreditCardException.class, () -> {
            paymentService.moneyTransfer(1000L);
        }, "Deve lançar NoCreditCardException ao tentar transferir sem cartão");

        // Validar mensagem da exceção
        assertTrue(exception.getMessage().contains("Do Not Have Credit Card") ||
                   exception.getMessage().contains("No Credit Card"),
                "Mensagem deve indicar ausência de cartão");
        System.out.println("✓ NoCreditCardException lançada: " + exception.getMessage());

        // Verificar que usuário continua sem cartão
        userSemCartao = userRepository.findById(userSemCartao.getId()).orElseThrow();
        assertNull(userSemCartao.getCreditCard(), "Usuário deve continuar sem cartão");
        System.out.println("✓ Usuário permanece sem cartão");

        // Verificar que nenhum cartão foi criado no banco
        assertEquals(0, creditCardRepository.count(), "Nenhum cartão deve existir no banco");
        System.out.println("✓ Nenhum cartão criado no banco");
    }

    @Test
    @DisplayName("Deve permitir adicionar cartão e transferir dinheiro")
    void devePermitirAdicionarCartaoETransferir() {
        System.out.println("\n--- Teste: Adicionar Cartão e Transferir ---");

        // Mockar LoggedInUser
        when(loggedInUser.getUser()).thenReturn(userSemCartao);

        // Passo 1: Adicionar cartão
        CreditCardDto creditCardDto = CreditCardDto.builder()
                .cardNumber(9999888877776666L)
                .month(12)
                .year(2028)
                .CVV(555)
                .build();

        paymentService.addCreditCard(creditCardDto);
        System.out.println("✓ Cartão adicionado");

        // Recarregar usuário
        User userAtualizado = userRepository.findById(userSemCartao.getId()).orElseThrow();
        assertNotNull(userAtualizado.getCreditCard(), "Usuário deve ter cartão");
        assertEquals(0L, userAtualizado.getCreditCard().getAccountBalance());
        System.out.println("✓ Cartão confirmado com saldo = 0");

        // Atualizar mock
        when(loggedInUser.getUser()).thenReturn(userAtualizado);

        // Passo 2: Transferir dinheiro
        paymentService.moneyTransfer(500L);
        System.out.println("✓ Transferência realizada");

        // Verificar saldo
        User userFinal = userRepository.findById(userSemCartao.getId()).orElseThrow();
        assertEquals(500L, userFinal.getCreditCard().getAccountBalance());
        System.out.println("✓ Saldo atualizado para 500");
    }

    @Test
    @DisplayName("Deve impedir múltiplas tentativas de transferência sem cartão")
    void deveImpedirMultiplasTentativasSemCartao() {
        System.out.println("\n--- Teste: Múltiplas Tentativas Sem Cartão ---");

        // Mockar LoggedInUser
        when(loggedInUser.getUser()).thenReturn(userSemCartao);

        // Tentar transferir 3 vezes (todas devem falhar)
        for (int i = 1; i <= 3; i++) {
            NoCreditCardException exception = assertThrows(NoCreditCardException.class, () -> {
                paymentService.moneyTransfer(100L);
            });
            System.out.println("✓ Tentativa " + i + " falhou: " + exception.getMessage());
        }

        // Verificar que usuário continua sem cartão
        User userAtualizado = userRepository.findById(userSemCartao.getId()).orElseThrow();
        assertNull(userAtualizado.getCreditCard());
        System.out.println("✓ Após 3 tentativas: usuário continua sem cartão");
    }
}
