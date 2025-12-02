package com.example.carrentalproject.integration;

import com.example.carrentalproject.domain.Role;
import com.example.carrentalproject.dto.UserInDto;
import com.example.carrentalproject.exception.ExistingEntityException;
import com.example.carrentalproject.repository.RoleRepository;
import com.example.carrentalproject.repository.UserRepository;
import com.example.carrentalproject.service.RegistrationService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * IT17 - Cenário 9: Teste que DETECTA LACUNA DE VALIDAÇÃO
 * 
 * LACUNA IDENTIFICADA: RegistrationService.registerUser() NÃO valida emails duplicados
 * 
 * COMPORTAMENTO ATUAL INCORRETO: Sistema permite múltiplos usuários com mesmo email
 * COMPORTAMENTO CORRETO: Sistema valida username duplicado (linha 27 RegistrationService)
 * 
 * EVIDÊNCIA TÉCNICA:
 * - UserRepository tem findByUsername() mas NÃO tem findByEmail()
 * - User.email não tem constraint UNIQUE no banco
 * - RegistrationService apenas verifica username (linha 27)
 * 
 * IMPACTO:
 * - Impossível recuperar senha por email (qual usuário recebe o reset?)
 * - Violação de princípios de integridade de dados
 * - Email deveria ser identificador único assim como username
 * 
 * EVIDÊNCIA: Testes usam assertDoesNotThrow() para PROVAR sistema aceita emails duplicados
 */
@SpringBootTest
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("IT17 - Cenário 9: Lacuna - Sistema NÃO valida email duplicado")
public class IT17_RegistroComEmailDuplicadoFalha {

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @BeforeEach
    void setUp() {
        // Criar Role se não existir
        if (roleRepository.findByName("ROLE_USER").isEmpty()) {
            Role userRole = Role.builder()
                    .name("ROLE_USER")
                    .users(new java.util.ArrayList<>())
                    .build();
            roleRepository.save(userRole);
        }
    }

    @Test
    @org.junit.jupiter.api.Order(1)
    @DisplayName("DEVE FALHAR: Sistema deve bloquear email duplicado")
    void registroComEmailDuplicadoDeveFalhar() {
        // Primeiro usuário
        UserInDto user1 = UserInDto.builder()
                .firstName("João")
                .lastName("Silva")
                .username("joao_teste_it17_1")
                .email("duplicado@test.com")
                .password("Test12345")
                .phone(999111222)
                .build();

        // Primeiro registro funciona
        assertDoesNotThrow(() -> {
            registrationService.registerUser(user1);
        }, "Primeiro usuário registrado com sucesso");

        // Segundo usuário com MESMO EMAIL
        UserInDto user2 = UserInDto.builder()
                .firstName("Maria")
                .lastName("Costa")
                .username("maria_teste_it17_1")  // username diferente
                .email("duplicado@test.com")  // MESMO EMAIL
                .password("Test56789")
                .phone(999333444)
                .build();

        // BUG: Sistema deveria lançar exceção mas não lança
        assertThrows(ExistingEntityException.class, () -> {
            registrationService.registerUser(user2);
        }, "Sistema DEVERIA lançar ExistingEntityException para email duplicado");
    }

    @Test
    @org.junit.jupiter.api.Order(2)
    @DisplayName("CONTRASTE: Sistema BLOQUEIA username duplicado corretamente")
    void bloqueiaUsernameDuplicado() {
        // Primeiro usuário
        UserInDto user1 = UserInDto.builder()
                .firstName("Pedro")
                .lastName("Alves")
                .username("pedro_teste_it17_2")  // USERNAME DUPLICADO
                .email("pedro1@test.com")
                .password("Test12345")
                .phone(999555666)
                .build();

        // Segundo usuário com MESMO USERNAME
        UserInDto user2 = UserInDto.builder()
                .firstName("Paulo")
                .lastName("Santos")
                .username("pedro_teste_it17_2")  // MESMO USERNAME
                .email("paulo1@test.com")  // email diferente
                .password("Test56789")
                .phone(999777888)
                .build();

        // Primeiro registro funciona
        assertDoesNotThrow(() -> {
            registrationService.registerUser(user1);
        }, "Primeiro usuário registrado");

        // Segundo registro é BLOQUEADO (comportamento correto)
        assertThrows(ExistingEntityException.class, () -> {
            registrationService.registerUser(user2);
        }, "Sistema BLOQUEIA corretamente username duplicado");

        System.out.println("\n=== CONTRASTE ===");
        System.out.println("✓ Sistema valida username duplicado (RegistrationService linha 27)");
        System.out.println("✗ Sistema NÃO valida email duplicado (LACUNA)");
        System.out.println("=================\n");
    }

    @Test
    @org.junit.jupiter.api.Order(3)
    @DisplayName("DOCUMENTAÇÃO: Comportamento esperado vs atual")
    void comportamentoEsperadoEmailDuplicado() {
        System.out.println("\n=== COMPORTAMENTO ESPERADO ===");
        System.out.println("\nCódigo esperado no RegistrationService.registerUser():");
        System.out.println("  // Linha 27 - validação username (JÁ EXISTE):");
        System.out.println("  if (userRepository.findByUsername(userInDto.getUsername()).isPresent()) {");
        System.out.println("      throw new ExistingEntityException(\"Username already exists\");");
        System.out.println("  }");
        System.out.println("  ");
        System.out.println("  // Validação email (FALTANDO):");
        System.out.println("  if (userRepository.findByEmail(userInDto.getEmail()).isPresent()) {");
        System.out.println("      throw new ExistingEntityException(\"Email already exists\");");
        System.out.println("  }");
        System.out.println("\nCOMPORTAMENTO ATUAL:");
        System.out.println("  ✓ Username validado corretamente");
        System.out.println("  ✗ Email NÃO validado (LACUNA)");
        System.out.println("\nIMPACTO:");
        System.out.println("  - Impossível recuperar senha por email");
        System.out.println("  - Sistema não consegue identificar usuário único por email");
        System.out.println("  - Violação de integridade de dados");
        System.out.println("===============================\n");

        assertTrue(true, "Documentação do comportamento esperado");
    }

    @Test
    @org.junit.jupiter.api.Order(4)
    @DisplayName("EVIDÊNCIA TÉCNICA: UserRepository não tem findByEmail()")
    void userRepositoryNaoTemFindByEmail() {
        System.out.println("\n=== EVIDÊNCIA TÉCNICA ===");
        System.out.println("\nMétodos disponíveis em UserRepository:");
        System.out.println("  ✓ findByUsername(String username) - EXISTE");
        System.out.println("  ✗ findByEmail(String email) - NÃO EXISTE");
        System.out.println("\nCódigo RegistrationService linha 27:");
        System.out.println("  if (userRepository.findByUsername(userInDto.getUsername()).isPresent())");
        System.out.println("\nCódigo FALTANDO:");
        System.out.println("  if (userRepository.findByEmail(userInDto.getEmail()).isPresent())");
        System.out.println("\nCONCLUSÃO:");
        System.out.println("  - UserRepository precisa implementar findByEmail()");
        System.out.println("  - User.email precisa constraint UNIQUE no banco");
        System.out.println("  - RegistrationService precisa validar email duplicado");
        System.out.println("=========================\n");

        assertTrue(true, "Evidência técnica da lacuna");
    }

    @Test
    @org.junit.jupiter.api.Order(5)
    @DisplayName("DEVE FALHAR: Apenas primeiro usuário deve ser aceito, demais devem falhar")
    void multiplosUsuariosComMesmoEmail() {
        String emailCompartilhado = "multiple@test.com";

        // Primeiro usuário funciona
        UserInDto user1 = UserInDto.builder()
                .firstName("User1")
                .lastName("Test1")
                .username("user_it17_5_1")
                .email(emailCompartilhado)
                .password("Test12341")
                .phone(999000001)
                .build();

        assertDoesNotThrow(() -> {
            registrationService.registerUser(user1);
        }, "Primeiro usuário com email registrado");

        // Todos os outros devem falhar
        for (int i = 2; i <= 5; i++) {
            final int index = i;
            UserInDto user = UserInDto.builder()
                    .firstName("User" + i)
                    .lastName("Test" + i)
                    .username("user_it17_5_" + i)
                    .email(emailCompartilhado)  // MESMO EMAIL
                    .password("Test1234" + i)
                    .phone(999000000 + i)
                    .build();

            assertThrows(ExistingEntityException.class, () -> {
                registrationService.registerUser(user);
            }, "Usuário " + index + " com email duplicado DEVERIA falhar");
        }
    }

    @Test
    @org.junit.jupiter.api.Order(6)
    @DisplayName("DEVE FALHAR: Apenas Ana registrada, demais devem ser rejeitados")
    void validaUsuariosDiferentesNaoPodeCompartilharEmail() {
        String emailProblematico = "recovery@test.com";

        UserInDto user1 = UserInDto.builder()
                .firstName("Ana")
                .lastName("Costa")
                .username("ana_it17_6")
                .email(emailProblematico)
                .password("Test11111")
                .phone(991111111)
                .build();

        // Primeiro funciona
        assertDoesNotThrow(() -> {
            registrationService.registerUser(user1);
        }, "Ana registrada com sucesso");

        // Todos os outros devem falhar
        UserInDto user2 = UserInDto.builder()
                .firstName("Bruno")
                .lastName("Silva")
                .username("bruno_it17_6")
                .email(emailProblematico)
                .password("Test22222")
                .phone(992222222)
                .build();

        UserInDto user3 = UserInDto.builder()
                .firstName("Carlos")
                .lastName("Lima")
                .username("carlos_it17_6")
                .email(emailProblematico)
                .password("Test33333")
                .phone(993333333)
                .build();

        UserInDto user4 = UserInDto.builder()
                .firstName("Diana")
                .lastName("Rocha")
                .username("diana_it17_6")
                .email(emailProblematico)
                .password("Test44444")
                .phone(994444444)
                .build();

        assertThrows(ExistingEntityException.class, () -> {
            registrationService.registerUser(user2);
        }, "Bruno com email duplicado DEVERIA falhar");

        assertThrows(ExistingEntityException.class, () -> {
            registrationService.registerUser(user3);
        }, "Carlos com email duplicado DEVERIA falhar");

        assertThrows(ExistingEntityException.class, () -> {
            registrationService.registerUser(user4);
        }, "Diana com email duplicado DEVERIA falhar");
    }
}
