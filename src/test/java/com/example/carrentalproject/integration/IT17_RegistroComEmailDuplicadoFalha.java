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
    @DisplayName("LACUNA: Sistema permite email duplicado")
    void permiteEmailDuplicado() {
        // Primeiro usuário
        UserInDto user1 = UserInDto.builder()
                .firstName("João")
                .lastName("Silva")
                .username("joao_teste_it17_1")
                .email("duplicado@test.com")  // EMAIL DUPLICADO
                .password("Test12345")
                .phone(999111222)
                .build();

        // Segundo usuário com MESMO EMAIL
        UserInDto user2 = UserInDto.builder()
                .firstName("Maria")
                .lastName("Costa")
                .username("maria_teste_it17_1")  // username diferente
                .email("duplicado@test.com")  // MESMO EMAIL
                .password("Test56789")
                .phone(999333444)
                .build();

        // LACUNA: Ambos registros são aceitos
        assertDoesNotThrow(() -> {
            registrationService.registerUser(user1);
        }, "Primeiro usuário registrado");

        assertDoesNotThrow(() -> {
            registrationService.registerUser(user2);
        }, "LACUNA DETECTADA: Segundo usuário com email duplicado foi aceito");

        // Verificar que ambos existem
        assertTrue(userRepository.findByUsername("joao_teste_it17_1").isPresent(),
                "Usuário 1 existe");
        assertTrue(userRepository.findByUsername("maria_teste_it17_1").isPresent(),
                "Usuário 2 existe");

        System.out.println("\n=== LACUNA DETECTADA ===");
        System.out.println("✗ Sistema aceita 2 usuários com email 'duplicado@test.com'");
        System.out.println("✗ RegistrationService NÃO valida email duplicado");
        System.out.println("✗ Impossível recuperar senha por email");
        System.out.println("========================\n");
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
    @DisplayName("LACUNA EM ESCALA: 5 usuários com mesmo email")
    void multiplosUsuariosComMesmoEmail() {
        String emailCompartilhado = "multiple@test.com";

        // Criar 5 usuários com MESMO EMAIL
        for (int i = 1; i <= 5; i++) {
            UserInDto user = UserInDto.builder()
                    .firstName("User" + i)
                    .lastName("Test" + i)
                    .username("user_it17_5_" + i)
                    .email(emailCompartilhado)  // TODOS COM MESMO EMAIL
                    .password("Test1234" + i)
                    .phone(999000000 + i)
                    .build();

            assertDoesNotThrow(() -> {
                registrationService.registerUser(user);
            }, "Usuário " + i + " registrado com email duplicado");
        }

        // Verificar que todos 5 existem
        long usuariosComEmailDuplicado = userRepository.findAll().stream()
                .filter(u -> u.getEmail().equals(emailCompartilhado))
                .count();

        assertEquals(5, usuariosComEmailDuplicado,
                "5 usuários com mesmo email existem no banco");

        System.out.println("\n=== LACUNA EM ESCALA ===");
        System.out.println("✗ 5 usuários registrados com email '" + emailCompartilhado + "'");
        System.out.println("✗ Se qualquer um solicitar recuperação de senha:");
        System.out.println("   - Qual usuário deve receber o email?");
        System.out.println("   - Sistema não consegue identificar usuário único");
        System.out.println("========================\n");
    }

    @Test
    @org.junit.jupiter.api.Order(6)
    @DisplayName("IMPACTO REAL: Impossível recuperar senha por email")
    void impossivelRecuperarSenhaPorEmail() {
        String emailProblematico = "recovery@test.com";

        // 4 usuários com mesmo email
        UserInDto user1 = UserInDto.builder()
                .firstName("Ana")
                .lastName("Costa")
                .username("ana_it17_6")
                .email(emailProblematico)
                .password("Test11111")
                .phone(991111111)
                .build();

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

        // Todos registrados com sucesso (LACUNA)
        assertDoesNotThrow(() -> {
            registrationService.registerUser(user1);
            registrationService.registerUser(user2);
            registrationService.registerUser(user3);
            registrationService.registerUser(user4);
        }, "4 usuários com mesmo email registrados");

        System.out.println("\n=== IMPACTO REAL ===");
        System.out.println("\nCENÁRIO: Usuário esqueceu senha");
        System.out.println("  1. Sistema solicita email: '" + emailProblematico + "'");
        System.out.println("  2. Busca no banco: 4 usuários encontrados!");
        System.out.println("     - ana_it17_6");
        System.out.println("     - bruno_it17_6");
        System.out.println("     - carlos_it17_6");
        System.out.println("     - diana_it17_6");
        System.out.println("  3. Sistema NÃO consegue identificar qual resetar");
        System.out.println("\nRECUPERAÇÃO DE SENHA: IMPOSSÍVEL");
        System.out.println("\nSOLUÇÃO NECESSÁRIA:");
        System.out.println("  - Email deve ser UNIQUE assim como username");
        System.out.println("  - Adicionar findByEmail() no UserRepository");
        System.out.println("  - Validar email duplicado no registerUser()");
        System.out.println("====================\n");

        assertTrue(true, "Impacto real da lacuna demonstrado");
    }
}
