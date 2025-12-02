package com.example.carrentalproject.integration;

import com.example.carrentalproject.constant.FuelType;
import com.example.carrentalproject.constant.GearBoxType;
import com.example.carrentalproject.domain.*;
import com.example.carrentalproject.dto.UserInDto;
import com.example.carrentalproject.repository.*;
import com.example.carrentalproject.service.OrderService;
import com.example.carrentalproject.service.RegistrationService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * IT20 - Cenário 10: Transações e Rollback em Criação de Pedido
 * 
 * OBJETIVO: Validar que falhas durante submitOrder() não persistem mudanças
 * parciais, garantindo atomicidade ACID da transação.
 * 
 * TESTES:
 * 1. saldoInsuficienteNaoDebitaConta - Falha não deduz saldo
 * 2. pacoteInexistenteNaoAlteraBanco - Exceção preserva estado
 * 3. pedidoDuplicadoNaoAlteraAccessKey - Validação preserva dados
 * 4. sucessoCriaAccessKeyEDebitaSaldo - Confirma commit completo
 * 
 * ESTRATÉGIA:
 * - Testa múltiplos pontos de falha no OrderService.submitOrder()
 * - Verifica que saldo, AccessKey e estado do usuário não mudam em falhas
 * - Valida atomicidade: tudo ou nada
 * 
 * DESCOBERTAS:
 * - @Transactional em OrderService garante rollback automático
 * - InsufficientFundsException, EntityNotFoundException, ExistingOrderException
 *   todas causam rollback
 * - CreditCard.accountBalance é revertido em falhas
 */
@SpringBootTest
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("IT20 - Criação de Pedido com Falha Rollback Completo")
public class IT20_CriacaoPedidoComFalhaRollbackCompleto {

    @Autowired
    private OrderService orderService;

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private CarPackageRepository carPackageRepository;

    @Autowired
    private CarParametersRepository carParametersRepository;

    @Autowired
    private AccessKeyRepository accessKeyRepository;

    @Autowired
    private CreditCardRepository creditCardRepository;

    @Autowired
    private RoleRepository roleRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private User testUser;
    private CarPackage testPackage;
    private CreditCard testCard;

    @BeforeEach
    void setUp() {
        // 1. Criar Role
        Role userRole = Role.builder()
                .name("ROLE_USER")
                .users(new java.util.ArrayList<>())
                .build();
        
        if (roleRepository.findByName("ROLE_USER").isEmpty()) {
            roleRepository.save(userRole);
        }

        // 2. Criar usuário
        UserInDto userDto = UserInDto.builder()
                .firstName("Maria")
                .lastName("Santos")
                .username("maria_teste_it20")
                .email("maria.it20@test.com")
                .password("Test12345")
                .phone(987654321)
                .build();

        registrationService.registerUser(userDto);
        testUser = userRepository.findByUsername("maria_teste_it20").orElseThrow();

        // 3. Criar CreditCard com saldo médio
        testCard = CreditCard.builder()
                .cardNumber(9876543210987654L)
                .month(6)
                .year(2027)
                .CVV(456)
                .accountBalance(5000L) // R$ 50 apenas
                .build();
        testCard.setUser(testUser);
        creditCardRepository.save(testCard);
        
        // Flush e clear para sincronizar
        entityManager.flush();
        entityManager.clear();
        testUser = userRepository.findByUsername("maria_teste_it20").orElseThrow();

        // 4. Criar CarParameters
        CarParameters parameters = CarParameters.builder()
                .fuelType(FuelType.PETROL)
                .gearBoxType(GearBoxType.MANUAL)
                .numberOfSeats(5)
                .numberOfDoors(4)
                .isAirConditioningAvailable(false)
                .build();
        carParametersRepository.save(parameters);

        // 5. Criar CarPackage
        testPackage = CarPackage.builder()
                .packageName("Economy")
                .pricePerHour(100) // R$ 1 por hora
                .build();
        carPackageRepository.save(testPackage);

        // 6. Criar Car disponível
        Car testCar = Car.builder()
                .brand("Volkswagen")
                .model("Gol")
                .registrationNr("IT20TST")
                .carPackage(testPackage)
                .carParameters(parameters)
                .isAvailable(true)
                .build();
        carRepository.save(testCar);

        // 7. Autenticar usuário
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(testUser.getUsername(), null)
        );
    }

    @Test
    @org.junit.jupiter.api.Order(1)
    @DisplayName("ROLLBACK: Saldo insuficiente não debita conta")
    void saldoInsuficienteNaoDebitaConta() {
        // ARRANGE: Verificar saldo inicial
        entityManager.flush();
        entityManager.clear();
        testUser = userRepository.findByUsername("maria_teste_it20").orElseThrow();
        
        CreditCard cardAntes = testUser.getCreditCard();
        assertNotNull(cardAntes, "CreditCard deve existir");
        long saldoAntes = cardAntes.getAccountBalance();
        assertEquals(5000L, saldoAntes, "Saldo inicial deve ser 5000");

        long accessKeysAntes = accessKeyRepository.count();
        assertEquals(0, accessKeysAntes, "Não deve haver AccessKeys");

        // ACT: Tentar criar pedido MUITO CARO (saldo insuficiente)
        // 100 horas * 100 por hora = 10000 > 5000 disponível
        try {
            orderService.submitOrder("Economy", 100);
            fail("Deveria lançar InsufficientFundsException");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("Not Have Enough Money") ||
                      e.getMessage().contains("Insufficient"),
                    "Mensagem deve indicar saldo insuficiente");
        }

        // ASSERT: Verificar ROLLBACK - saldo NÃO foi debitado
        entityManager.flush();
        entityManager.clear();
        
        User userDepois = userRepository.findByUsername("maria_teste_it20").orElseThrow();
        CreditCard cardDepois = userDepois.getCreditCard();
        assertNotNull(cardDepois, "CreditCard deve continuar existindo");
        
        long saldoDepois = cardDepois.getAccountBalance();
        assertEquals(saldoAntes, saldoDepois,
                "Saldo NÃO deve ter sido debitado (rollback)");

        long accessKeysDepois = accessKeyRepository.count();
        assertEquals(0, accessKeysDepois,
                "AccessKey NÃO deve ter sido criado");

        assertNull(userDepois.getAccessKey(),
                "User.accessKey deve permanecer null");

        System.out.println("\n=== ROLLBACK: SALDO INSUFICIENTE ===");
        System.out.println("✓ InsufficientFundsException lançada");
        System.out.println("✓ Saldo permanece: " + saldoDepois);
        System.out.println("✓ AccessKey NÃO criado");
        System.out.println("✓ Rollback preservou integridade financeira");
        System.out.println("====================================\n");
    }

    @Test
    @org.junit.jupiter.api.Order(2)
    @DisplayName("ROLLBACK: Pacote inexistente não altera banco")
    void pacoteInexistenteNaoAlteraBanco() {
        // ARRANGE: Estado inicial
        entityManager.flush();
        entityManager.clear();
        testUser = userRepository.findByUsername("maria_teste_it20").orElseThrow();
        
        long saldoAntes = testUser.getCreditCard().getAccountBalance();
        long accessKeysAntes = accessKeyRepository.count();

        // ACT: Tentar criar pedido com PACOTE INEXISTENTE
        try {
            orderService.submitOrder("PacoteFantasma", 5);
            fail("Deveria lançar EntityNotFoundException");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("Does Not Exists") ||
                      e.getMessage().contains("not found"),
                    "Mensagem deve indicar entidade não encontrada");
        }

        // ASSERT: Verificar ROLLBACK completo
        entityManager.flush();
        entityManager.clear();
        
        User userDepois = userRepository.findByUsername("maria_teste_it20").orElseThrow();
        
        long saldoDepois = userDepois.getCreditCard().getAccountBalance();
        assertEquals(saldoAntes, saldoDepois,
                "Saldo não deve ter mudado");

        long accessKeysDepois = accessKeyRepository.count();
        assertEquals(accessKeysAntes, accessKeysDepois,
                "Número de AccessKeys não deve ter mudado");

        assertNull(userDepois.getAccessKey(),
                "AccessKey não deve ter sido atribuído");

        System.out.println("\n=== ROLLBACK: PACOTE INEXISTENTE ===");
        System.out.println("✓ EntityNotFoundException lançada");
        System.out.println("✓ Saldo preservado: " + saldoDepois);
        System.out.println("✓ AccessKey não criado");
        System.out.println("✓ Estado do banco intacto");
        System.out.println("====================================\n");
    }

    @Test
    @org.junit.jupiter.api.Order(3)
    @DisplayName("ROLLBACK: Pedido duplicado não altera AccessKey existente")
    void pedidoDuplicadoNaoAlteraAccessKey() {
        // ARRANGE: Criar PRIMEIRO pedido (sucesso)
        assertDoesNotThrow(() -> {
            orderService.submitOrder("Economy", 10); // 10 * 100 = 1000
        }, "Primeiro pedido deve funcionar");

        entityManager.flush();
        entityManager.clear();
        testUser = userRepository.findByUsername("maria_teste_it20").orElseThrow();
        
        assertNotNull(testUser.getAccessKey(), "AccessKey deve existir");
        Long accessKeyIdOriginal = testUser.getAccessKey().getId();
        String pacoteOriginal = testUser.getAccessKey().getCarPackage();
        Integer horasOriginal = testUser.getAccessKey().getHours();
        
        long saldoAposPrimeiro = testUser.getCreditCard().getAccountBalance();
        assertEquals(4000L, saldoAposPrimeiro, // 5000 - 1000
                "Saldo após primeiro pedido");

        long accessKeysAposPrimeiro = accessKeyRepository.count();

        // ACT: Tentar criar SEGUNDO pedido (duplicado - deve falhar)
        try {
            orderService.submitOrder("Economy", 5);
            fail("Deveria lançar ExistingOrderException");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("Already Placed An Order") ||
                      e.getMessage().contains("Existing"),
                    "Mensagem deve indicar pedido duplicado");
        }

        // ASSERT: Verificar ROLLBACK - AccessKey ORIGINAL preservado
        entityManager.flush();
        entityManager.clear();
        
        User userDepois = userRepository.findByUsername("maria_teste_it20").orElseThrow();
        
        assertNotNull(userDepois.getAccessKey(),
                "AccessKey original deve continuar existindo");
        
        assertEquals(accessKeyIdOriginal, userDepois.getAccessKey().getId(),
                "AccessKey ID não deve ter mudado");
        
        assertEquals(pacoteOriginal, userDepois.getAccessKey().getCarPackage(),
                "Pacote do AccessKey original preservado");
        
        assertEquals(horasOriginal, userDepois.getAccessKey().getHours(),
                "Horas do AccessKey original preservadas");

        long saldoDepois = userDepois.getCreditCard().getAccountBalance();
        assertEquals(saldoAposPrimeiro, saldoDepois,
                "Saldo NÃO deve ter sido debitado novamente");

        long accessKeysDepois = accessKeyRepository.count();
        assertEquals(accessKeysAposPrimeiro, accessKeysDepois,
                "Número de AccessKeys não deve ter aumentado");

        System.out.println("\n=== ROLLBACK: PEDIDO DUPLICADO ===");
        System.out.println("✓ ExistingOrderException lançada");
        System.out.println("✓ AccessKey original preservado (ID: " + accessKeyIdOriginal + ")");
        System.out.println("✓ Saldo não debitado segunda vez");
        System.out.println("✓ Proteção contra duplicação funcionou");
        System.out.println("===================================\n");
    }

    @Test
    @org.junit.jupiter.api.Order(4)
    @DisplayName("SUCESSO: Pedido válido cria AccessKey e debita saldo corretamente")
    void sucessoCriaAccessKeyEDebitaSaldo() {
        // ARRANGE: Estado inicial limpo
        entityManager.flush();
        entityManager.clear();
        testUser = userRepository.findByUsername("maria_teste_it20").orElseThrow();
        
        long saldoAntes = testUser.getCreditCard().getAccountBalance();
        assertEquals(5000L, saldoAntes);
        
        assertNull(testUser.getAccessKey(), "Não deve ter AccessKey inicialmente");
        long accessKeysAntes = accessKeyRepository.count();

        // ACT: Criar pedido VÁLIDO
        assertDoesNotThrow(() -> {
            orderService.submitOrder("Economy", 20); // 20 * 100 = 2000
        }, "submitOrder deve funcionar com dados válidos");

        // ASSERT: Verificar COMMIT completo
        entityManager.flush();
        entityManager.clear();
        
        User userDepois = userRepository.findByUsername("maria_teste_it20").orElseThrow();
        
        // Verificar AccessKey criado
        assertNotNull(userDepois.getAccessKey(),
                "AccessKey deve ter sido criado");
        
        assertEquals("Economy", userDepois.getAccessKey().getCarPackage(),
                "Pacote correto no AccessKey");
        
        assertEquals(20, userDepois.getAccessKey().getHours(),
                "Horas corretas no AccessKey");

        // Verificar saldo debitado
        long saldoDepois = userDepois.getCreditCard().getAccountBalance();
        assertEquals(3000L, saldoDepois, // 5000 - 2000
                "Saldo deve ter sido debitado corretamente");

        // Verificar AccessKey persistido
        long accessKeysDepois = accessKeyRepository.count();
        assertEquals(accessKeysAntes + 1, accessKeysDepois,
                "Um AccessKey deve ter sido adicionado");

        AccessKey accessKey = accessKeyRepository.findById(userDepois.getAccessKey().getId())
                .orElseThrow();
        assertNotNull(accessKey.getUser(),
                "AccessKey deve estar vinculado ao usuário");

        System.out.println("\n=== SUCESSO: COMMIT COMPLETO ===");
        System.out.println("✓ AccessKey criado (ID: " + accessKey.getId() + ")");
        System.out.println("✓ Saldo debitado: 5000 → 3000");
        System.out.println("✓ Relacionamento User-AccessKey estabelecido");
        System.out.println("✓ Transação commitada com sucesso");
        System.out.println("=================================\n");
    }

    @Test
    @org.junit.jupiter.api.Order(5)
    @DisplayName("ROLLBACK: Usuário sem cartão não altera estado")
    void usuarioSemCartaoNaoAlteraEstado() {
        // ARRANGE: Remover cartão do usuário
        entityManager.flush();
        entityManager.clear();
        testUser = userRepository.findByUsername("maria_teste_it20").orElseThrow();
        
        CreditCard card = testUser.getCreditCard();
        if (card != null) {
            creditCardRepository.delete(card);
        }
        
        entityManager.flush();
        entityManager.clear();
        testUser = userRepository.findByUsername("maria_teste_it20").orElseThrow();
        assertNull(testUser.getCreditCard(), "CreditCard deve ter sido removido");

        long accessKeysAntes = accessKeyRepository.count();

        // ACT: Tentar criar pedido SEM cartão
        try {
            orderService.submitOrder("Economy", 10);
            fail("Deveria lançar NoCreditCardException");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("Do Not Have Credit Card") ||
                      e.getMessage().contains("No Credit Card"),
                    "Mensagem deve indicar falta de cartão");
        }

        // ASSERT: Verificar ROLLBACK
        entityManager.flush();
        entityManager.clear();
        
        User userDepois = userRepository.findByUsername("maria_teste_it20").orElseThrow();
        
        assertNull(userDepois.getCreditCard(),
                "CreditCard deve continuar null");

        assertNull(userDepois.getAccessKey(),
                "AccessKey não deve ter sido criado");

        long accessKeysDepois = accessKeyRepository.count();
        assertEquals(accessKeysAntes, accessKeysDepois,
                "Número de AccessKeys não deve ter mudado");

        System.out.println("\n=== ROLLBACK: SEM CARTÃO ===");
        System.out.println("✓ NoCreditCardException lançada");
        System.out.println("✓ AccessKey não criado");
        System.out.println("✓ Estado preservado");
        System.out.println("✓ Validação pré-condição funcionou");
        System.out.println("============================\n");
    }
}
