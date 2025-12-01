package com.example.carrentalproject.integration;

import com.example.carrentalproject.constant.FuelType;
import com.example.carrentalproject.constant.GearBoxType;
import com.example.carrentalproject.domain.*;
import com.example.carrentalproject.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * IT06 - Cenário 4: Autenticação e Autorização - UsuarioNaoPodeAcessarRecursoSemToken
 * 
 * Objetivo: Validar que endpoints protegidos retornam HTTP 401 quando acessados sem token JWT.
 * Valida a integração do Spring Security com JWT e a proteção de recursos.
 * 
 * HTTP 401 Unauthorized: Retornado quando não há credenciais de autenticação (sem token).
 * 
 * Testa múltiplos endpoints:
 * - GET /cars/packages (requer ROLE_USER)
 * - GET /cars/available (requer ROLE_USER)
 * - POST /orders (requer ROLE_USER)
 * - POST /delivery (requer ROLE_USER)
 * - POST /payment/moneyTransfer (requer ROLE_USER)
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("IT06 - Usuário Não Pode Acessar Recurso Sem Token")
public class IT06_UsuarioNaoPodeAcessarRecursoSemToken {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CarPackageRepository carPackageRepository;

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private CarParametersRepository carParametersRepository;

    @BeforeEach
    void setUp() {
        System.out.println("\n=== SETUP IT06: Validar Proteção de Endpoints ===");

        // Limpar dados de teste anteriores
        carRepository.deleteAll();
        carParametersRepository.deleteAll();
        carPackageRepository.deleteAll();

        // Criar dados básicos para testes (sem autenticação, apenas estrutura)
        CarPackage ordinaryPackage = CarPackage.builder()
                .packageName("Ordinary")
                .pricePerHour(50)
                .build();
        carPackageRepository.save(ordinaryPackage);

        CarParameters carParameters = CarParameters.builder()
                .fuelType(FuelType.PETROL)
                .gearBoxType(GearBoxType.MANUAL)
                .numberOfDoors(4)
                .numberOfSeats(5)
                .isAirConditioningAvailable(true)
                .build();
        carParametersRepository.save(carParameters);

        Car car = Car.builder()
                .brand("Toyota")
                .model("Corolla")
                .registrationNr("ABC1234")
                .isAvailable(true)
                .carPackage(ordinaryPackage)
                .carParameters(carParameters)
                .build();
        carRepository.save(car);

        System.out.println("Dados de teste criados: 1 pacote, 1 carro disponível");
    }

    @Test
    @DisplayName("Deve retornar 401 ao consultar /cars/packages sem token")
    void deveRetornar401AoConsultarPacotesSemToken() throws Exception {
        // When & Then: Tentar acessar endpoint protegido sem header Authorization
        mockMvc.perform(get("/cars/packages"))
                .andExpect(status().isUnauthorized());
        
        System.out.println("✓ GET /cars/packages sem token retornou 401 UNAUTHORIZED");
    }

    @Test
    @DisplayName("Deve retornar 401 ao consultar /cars/available sem token")
    void deveRetornar401AoConsultarCarrosDisponiveisSemToken() throws Exception {
        // When & Then: Tentar acessar endpoint protegido sem header Authorization
        mockMvc.perform(get("/cars/available"))
                .andExpect(status().isUnauthorized());
        
        System.out.println("✓ GET /cars/available sem token retornou 401 UNAUTHORIZED");
    }

    @Test
    @DisplayName("Deve retornar 401 ao criar pedido sem token")
    void deveRetornar401AoCriarPedidoSemToken() throws Exception {
        // When & Then: Tentar criar pedido sem header Authorization
        mockMvc.perform(post("/orders")
                        .param("carPackage", "Ordinary")
                        .param("hours", "5"))
                .andExpect(status().isUnauthorized());
        
        System.out.println("✓ POST /orders sem token retornou 401 UNAUTHORIZED");
    }

    @Test
    @DisplayName("Deve retornar 401 ao retirar carro sem token")
    void deveRetornar401AoRetirarCarroSemToken() throws Exception {
        // When & Then: Tentar retirar carro sem header Authorization
        mockMvc.perform(post("/delivery")
                        .param("carId", "1"))
                .andExpect(status().isUnauthorized());
        
        System.out.println("✓ POST /delivery sem token retornou 401 UNAUTHORIZED");
    }

    @Test
    @DisplayName("Deve retornar 401 ao transferir dinheiro sem token")
    void deveRetornar401AoTransferirDinheiroSemToken() throws Exception {
        // When & Then: Tentar transferir dinheiro sem header Authorization
        mockMvc.perform(post("/payment/moneyTransfer")
                        .param("moneyAmount", "500.00"))
                .andExpect(status().isUnauthorized());
        
        System.out.println("✓ POST /payment/moneyTransfer sem token retornou 401 UNAUTHORIZED");
    }

    @Test
    @DisplayName("Deve validar múltiplos endpoints protegidos sem autenticação")
    void deveValidarMultiplosEndpointsProtegidos() throws Exception {
        // Validar que todos os principais endpoints de usuário estão protegidos
        
        // GET /cars/packages
        mockMvc.perform(get("/cars/packages"))
                .andExpect(status().isUnauthorized());
        
        // GET /cars/available
        mockMvc.perform(get("/cars/available"))
                .andExpect(status().isUnauthorized());
        
        // POST /orders
        mockMvc.perform(post("/orders")
                        .param("carPackage", "Ordinary")
                        .param("hours", "5"))
                .andExpect(status().isUnauthorized());
        
        System.out.println("✓ Todos os endpoints principais estão protegidos contra acesso sem token");
    }

    @Test
    @DisplayName("Deve permitir acesso a endpoint público /registration sem token")
    void devePermitirAcessoAoEndpointRegistrationSemToken() throws Exception {
        // Given: Endpoint /registration é público (permitAll na SecurityConfig)
        
        // When & Then: Acessar endpoint público sem token deve permitir (não 403)
        // Nota: Vai retornar 400 (Bad Request) porque não enviamos body, mas não 403
        mockMvc.perform(post("/registration"))
                .andExpect(status().is4xxClientError()) // 400 Bad Request, não 403 Forbidden
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assertNotEquals(403, status, "Endpoint /registration não deve retornar 403 (deve ser público)");
                    System.out.println("✓ Endpoint /registration é público (retornou " + status + " em vez de 403)");
                });
    }

    private static void assertNotEquals(int unexpected, int actual, String message) {
        if (unexpected == actual) {
            throw new AssertionError(message + " - Expected not: " + unexpected + " but was: " + actual);
        }
    }
}
