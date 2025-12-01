# Casos de Teste a Implementar

Lista consolidada de todos os casos de teste ainda NÃO implementados (❌), agrupados por etapa da jornada do usuário.

---
## Etapa 1 – POST /registration
❌ CT01 RegistroComDadosValidos
❌ CT02 RegistroComUsernameNoLimiteMinimo
❌ CT03 RegistroComUsernameNoLimiteMaximo
❌ CT04 RegistroComPasswordNoLimiteMinimo
❌ CT05 RegistroComUsernameVazio
❌ CT06 RegistroComUsernameNulo
❌ CT07 RegistroComUsernameMuitoCurto
❌ CT08 RegistroComUsernameMuitoLongo
❌ CT09 RegistroComUsernameCaracteresInvalidos
❌ CT11 RegistroComPasswordVazio
❌ CT12 RegistroComPasswordNulo
❌ CT13 RegistroComPasswordMuitoCurto
❌ CT15 RegistroComEmailVazio
❌ CT16 RegistroComEmailNulo
❌ CT17 RegistroComEmailFormatoInvalido
❌ CT18 RegistroComEmailDuplicado
❌ CT19 RegistroComNomeVazio
❌ CT20 RegistroComTelefoneFormatoInvalido
❌ CT21 RegistroComMultiplosCamposInvalidos
❌ CT22 RegistroComTodosCamposVazios
❌ CT23 RegistroComBodyVazio

---
## Etapa 2 – PUT /users/{username}/creditCards
❌ CT01 AdicionarCartaoComDadosValidos
❌ CT02 AdicionarCartaoComValidadeProxima
❌ CT03 AdicionarCartaoComValidadeFutura
❌ CT04 AdicionarSegundoCartao
❌ CT05 AdicionarCartaoComNomeDoisCaracteres
❌ CT06 AdicionarCartaoSemAutenticacao
❌ CT07 AdicionarCartaoParaOutroUsuario
❌ CT08 AdicionarCartaoUsuarioInexistente
❌ CT09 AdicionarCartaoNumeroVazio
❌ CT10 AdicionarCartaoNumeroNulo
❌ CT11 AdicionarCartaoCom15Digitos
❌ CT12 AdicionarCartaoCom17Digitos
❌ CT13 AdicionarCartaoComLetras
❌ CT14 AdicionarCartaoComCaracteresEspeciais
❌ CT15 AdicionarCartaoAlgoritmoLuhnInvalido
❌ CT16 AdicionarCartaoDuplicado
❌ CT17 AdicionarCartaoNomeTitularVazio
❌ CT18 AdicionarCartaoNomeTitularNulo
❌ CT19 AdicionarCartaoNomeTitularComNumeros
❌ CT20 AdicionarCartaoNomeTitularCaracteresEspeciais
❌ CT21 AdicionarCartaoNomeTitularUmCaractere
❌ CT22 AdicionarCartaoValidadeVazia
❌ CT23 AdicionarCartaoValidadeNula
❌ CT24 AdicionarCartaoValidadeFormatoIncorreto
❌ CT25 AdicionarCartaoValidadePassada
❌ CT26 AdicionarCartaoValidadeMes00
❌ CT27 AdicionarCartaoValidadeMes13
❌ CT28 AdicionarCartaoValidadeMesInvalido
❌ CT29 AdicionarCartaoCvvVazio
❌ CT30 AdicionarCartaoCvvNulo
❌ CT31 AdicionarCartaoCvvCom2Digitos
❌ CT32 AdicionarCartaoCvvCom4Digitos
❌ CT33 AdicionarCartaoCvvComLetras
❌ CT34 AdicionarCartaoCvvComCaracteresEspeciais
❌ CT35 AdicionarCartaoMultiplosCamposInvalidos
❌ CT36 AdicionarCartaoTodosCamposVazios
❌ CT37 AdicionarCartaoBodyVazio

---
## Etapa 3a – GET /cars/packages
❌ CT03 ConsultarPacotesPaginaEspecifica
❌ CT04 ConsultarPacotesOrdenadoPorPrecoAsc
❌ CT05 ConsultarPacotesOrdenadoPorPrecoDesc
❌ CT06 ConsultarPacotesOrdenadoPorNome
❌ CT07 ConsultarPacotesComPaginacaoEOrdenacao
❌ CT08 ConsultarPacotesTamanhoMinimo
❌ CT09 ConsultarPacotesTamanhoMaximo
❌ CT10 ConsultarPacotesListaVazia
❌ CT11 ConsultarPacotesPaginaAlemDoLimite
❌ CT12 ConsultarPacotesSemAutenticacao
❌ CT13 ConsultarPacotesTokenExpirado
❌ CT14 ConsultarPacotesTokenInvalido
❌ CT15 ConsultarPacotesPageNegativo
❌ CT16 ConsultarPacotesSizeZero
❌ CT17 ConsultarPacotesSizeNegativo
❌ CT18 ConsultarPacotesPageNaoNumerico
❌ CT19 ConsultarPacotesSizeNaoNumerico
❌ CT20 ConsultarPacotesCampoInexistente
❌ CT21 ConsultarPacotesDirecaoInvalida
❌ CT22 ConsultarPacotesFormatoSortIncorreto

---
## Etapa 3b – GET /cars/available
❌ CT26 ConsultarCarrosFiltroPrecoMinimo
❌ CT27 ConsultarCarrosFiltroPrecoMaximo
❌ CT28 ConsultarCarrosFiltroFaixaPreco
❌ CT29 ConsultarCarrosFiltroBrand
❌ CT30 ConsultarCarrosFiltroModel
❌ CT31 ConsultarCarrosMultiplosFiltros
❌ CT32 ConsultarCarrosComPaginacao
❌ CT33 ConsultarCarrosComOrdenacao
❌ CT34 ConsultarCarrosFiltrosEPaginacao
❌ CT35 ConsultarCarrosListaVazia
❌ CT36 ConsultarCarrosPrecoMinimoZero
❌ CT37 ConsultarCarrosPrecosMinimoIgualMaximo
❌ CT38 ConsultarCarrosSemAutenticacao
❌ CT39 ConsultarCarrosTokenExpirado
❌ CT40 ConsultarCarrosTokenInvalido
❌ CT41 ConsultarCarrosFuelTypeInvalido
❌ CT42 ConsultarCarrosGearBoxTypeInvalido
❌ CT43 ConsultarCarrosMinPriceNegativo
❌ CT44 ConsultarCarrosMaxPriceMenorQueMin
❌ CT45 ConsultarCarrosMinPriceNaoNumerico
❌ CT46 ConsultarCarrosMaxPriceNaoNumerico
❌ CT47 ConsultarCarrosPageNegativo
❌ CT48 ConsultarCarrosSizeZero
❌ CT49 ConsultarCarrosSizeNegativo
❌ CT50 ConsultarCarrosPageNaoNumerico
❌ CT51 ConsultarCarrosSizeNaoNumerico
❌ CT52 ConsultarCarrosCampoInexistente
❌ CT53 ConsultarCarrosDirecaoInvalida
❌ CT54 ConsultarCarrosFormatoSortIncorreto
❌ CT55 ConsultarCarrosMultiplosParametrosInvalidos

---
## Etapa 4 – POST /orders?carPackage=X&hours=Y
❌ CT02 CriarPedidoHoraMinima
❌ CT03 CriarPedidoHoraLimiteMaximo
❌ CT04 CriarPedidoHoraMediana
❌ CT05 CriarPedidoComPacoteBasico
❌ CT06 CriarPedidoComPacotePremium
❌ CT07 CriarPedidoCalculoPrecoCorreto
❌ CT08 CriarPedidoAtribuiCarroDisponivel
❌ CT09 CriarPedidoUsuarioComMultiplosCartoes
❌ CT10 CriarPedidoSemAutenticacao
❌ CT11 CriarPedidoTokenExpirado
❌ CT12 CriarPedidoTokenInvalido
❌ CT15 CriarPedidoUsuarioInexistente
❌ CT16 CriarPedidoCarPackageVazio
❌ CT17 CriarPedidoCarPackageNulo
❌ CT19 CriarPedidoCarPackageInvalido
❌ CT20 CriarPedidoCarPackageNegativo
❌ CT21 CriarPedidoCarPackageZero
❌ CT22 CriarPedidoHoursVazio
❌ CT23 CriarPedidoHoursNulo
❌ CT24 CriarPedidoHoursZero
❌ CT25 CriarPedidoHoursNegativo
❌ CT26 CriarPedidoHoursAcimaLimite
❌ CT27 CriarPedidoHoursInvalido
❌ CT28 CriarPedidoHoursDecimal
❌ CT30 CriarPedidoTodosCarrosEmUso
❌ CT31 CriarPedidoAmbosParametrosInvalidos
❌ CT32 CriarPedidoSemParametros
❌ CT33 CriarPedidoComBodyDesnecessario
❌ CT34 CriarPedidosSimultaneos
❌ CT35 CriarPedidoUltimoCarroDisponivel

---
## Etapa 5a – POST /payment/addCreditCard
❌ CT01 AdicionarCartaoParaPagamento
❌ CT02 AdicionarCartaoComValidadeProxima
❌ CT03 AdicionarCartaoComValidadeFutura
❌ CT04 AdicionarCartaoNumero16Digitos
❌ CT05 AdicionarCartaoCvv3Digitos
❌ CT06 AdicionarCartaoSemAutenticacao
❌ CT07 AdicionarCartaoTokenExpirado
❌ CT08 AdicionarCartaoTokenInvalido
❌ CT09 AdicionarCartaoNumeroVazio
❌ CT10 AdicionarCartaoNumeroNulo
❌ CT11 AdicionarCartaoNumero15Digitos
❌ CT12 AdicionarCartaoNumero17Digitos
❌ CT13 AdicionarCartaoNumeroComLetras
❌ CT14 AdicionarCartaoNumeroComEspeciais
❌ CT15 AdicionarCartaoLuhnInvalido
❌ CT16 AdicionarCartaoNomeVazio
❌ CT17 AdicionarCartaoNomeNulo
❌ CT18 AdicionarCartaoNomeComNumeros
❌ CT19 AdicionarCartaoNomeComEspeciais
❌ CT20 AdicionarCartaoValidadeVazia
❌ CT21 AdicionarCartaoValidadeNula
❌ CT22 AdicionarCartaoValidadePassada
❌ CT23 AdicionarCartaoValidadeMes00
❌ CT24 AdicionarCartaoValidadeMes13
❌ CT25 AdicionarCartaoValidadeFormatoIncorreto
❌ CT26 AdicionarCartaoCvvVazio
❌ CT27 AdicionarCartaoCvvNulo
❌ CT28 AdicionarCartaoCvv2Digitos
❌ CT29 AdicionarCartaoCvv4Digitos
❌ CT30 AdicionarCartaoCvvComLetras
❌ CT31 AdicionarCartaoCvvComEspeciais
❌ CT32 AdicionarCartaoDuplicado
❌ CT33 AdicionarCartaoBodyVazio
❌ CT34 AdicionarCartaoSemBody

---
## Etapa 5b – PUT /payment/moneyTransfer
❌ CT36 TransferirValorMaior
❌ CT37 TransferirValorComDuasCasasDecimais
❌ CT38 TransferirValorInteiro
❌ CT39 TransferirValorMinimo
❌ CT40 PagamentoAtualizaStatusPedido
❌ CT41 PagamentoCalculaTroco
❌ CT42 PagamentoUsuarioComMultiplosCartoes
❌ CT43 TransferirSemAutenticacao
❌ CT44 TransferirTokenExpirado
❌ CT45 TransferirTokenInvalido
❌ CT47 TransferirUsuarioSemPedido
❌ CT48 TransferirUsuarioInexistente
❌ CT49 TransferirMoneyAmountVazio
❌ CT50 TransferirMoneyAmountNulo
❌ CT51 TransferirMoneyAmountZero
❌ CT52 TransferirMoneyAmountNegativo
❌ CT54 TransferirMoneyAmountInvalido
❌ CT55 TransferirMoneyAmountComMaisCasasDecimais
❌ CT56 TransferirPedidoJaPago
❌ CT57 TransferirPedidoCancelado
❌ CT58 TransferirPedidoInexistente
❌ CT59 TransferirSaldoInsuficiente
❌ CT60 TransferirMultiplosParametrosInvalidos
❌ CT61 TransferirSemParametros

---
## Etapa 6 – POST /delivery?carId={id}
❌ CT02 RetirarCarroImediatamenteAposPagamento
❌ CT03 RetirarCarroAposTempoEspera
❌ CT04 RetirarCarroAtualizaStatusPedido
❌ CT05 RetirarCarroAtualizaStatusCarro
❌ CT06 RetirarCarroRegistraTimestamp
❌ CT07 RetirarCarroCarIdMinimo
❌ CT08 RetirarCarroCarIdMaximo
❌ CT09 RetirarCarroSemAutenticacao
❌ CT10 RetirarCarroTokenExpirado
❌ CT11 RetirarCarroTokenInvalido
❌ CT12 RetirarCarroUsuarioInexistente
❌ CT13 RetirarCarroCarIdVazio
❌ CT14 RetirarCarroCarIdNulo
❌ CT15 RetirarCarroCarIdZero
❌ CT16 RetirarCarroCarIdNegativo
❌ CT17 RetirarCarroCarIdInexistente
❌ CT18 RetirarCarroCarIdInvalido
❌ CT19 RetirarCarroUsuarioSemPedido
❌ CT21 RetirarCarroPedidoJaEntregue
❌ CT22 RetirarCarroPedidoCancelado
❌ CT23 RetirarCarroSegundaTentativa
❌ CT24 RetirarCarroNaoVinculadoPedido
❌ CT25 RetirarCarroDeOutroUsuario
❌ CT26 RetirarCarroIndisponivel
❌ CT27 RetirarCarroEmManutencao
❌ CT28 RetirarCarroUsuarioSemCartao
❌ CT29 RetirarCarroSemPagamento
❌ CT30 RetirarCarroMultiplosParametrosInvalidos
❌ CT31 RetirarCarroSemParametros
❌ CT32 RetirarCarroComBodyDesnecessario
❌ CT33 RetirarCarroSimultaneamente
❌ CT34 RetirarCarroMesmoUsuarioDuasVezes

---
## Etapa 7 – GET /orders

### Casos de Sucesso
❌ CT02 ConsultarPedidosUsuarioComUmPedido
❌ CT04 ConsultarPedidosUsuarioComPedidoAtivo
❌ CT05 ConsultarPedidosUsuarioComPedidoConcluido
❌ CT06 ConsultarPedidosUsuarioComPedidoCancelado
❌ CT07 ConsultarPedidosUsuarioComPedidoPago
❌ CT08 ConsultarPedidosUsuarioComPedidoPendente
❌ CT09 ConsultarPedidosOrdenadoPorDataDesc
❌ CT10 ConsultarPedidosOrdenadoPorDataAsc
❌ CT11 ConsultarPedidosComPaginacao
❌ CT12 ConsultarPedidosPrimeiraPagina
❌ CT13 ConsultarPedidosUltimaPagina
❌ CT14 ConsultarPedidosListaVazia
❌ CT15 ConsultarPedidosAposRetirada
❌ CT16 ConsultarPedidosComDiferentesCarros
❌ CT17 ConsultarPedidosComDiferentesPacotes

### Casos de Falha - Autenticação/Autorização
❌ CT18 ConsultarPedidosSemAutenticacao
❌ CT19 ConsultarPedidosTokenExpirado
❌ CT20 ConsultarPedidosTokenInvalido
❌ CT21 ConsultarPedidosUsuarioInexistente
❌ CT22 ConsultarPedidosUsuarioBloqueado
❌ CT23 ConsultarPedidosTokenRevogado

### Casos de Falha - Paginação
❌ CT24 ConsultarPedidosPageNegativo
❌ CT25 ConsultarPedidosSizeZero
❌ CT26 ConsultarPedidosSizeNegativo
❌ CT27 ConsultarPedidosPageNaoNumerico
❌ CT28 ConsultarPedidosSizeNaoNumerico
❌ CT29 ConsultarPedidosPageAlemLimite
❌ CT30 ConsultarPedidosSizeExcessivo

### Casos de Falha - Ordenação
❌ CT31 ConsultarPedidosCampoOrdenacaoInvalido
❌ CT32 ConsultarPedidosDirecaoInvalida
❌ CT33 ConsultarPedidosFormatoSortIncorreto

### Casos de Falha - Filtros
❌ CT34 ConsultarPedidosStatusInvalido
❌ CT35 ConsultarPedidosDataInicioInvalida
❌ CT36 ConsultarPedidosDataFimInvalida
❌ CT37 ConsultarPedidosDataInicioMaiorQueFim
❌ CT38 ConsultarPedidosCarIdInvalido

### Casos de Falha - Combinações
❌ CT39 ConsultarPedidosMultiplosParametrosInvalidos
❌ CT40 ConsultarPedidosPaginacaoEOrdenacaoInvalidas

### Casos de Validação - Isolamento de Dados
❌ CT42 ConsultarPedidosUsuarioAdminVerApenasPropriosPedidos

### Casos de Validação - Conteúdo da Resposta
❌ CT44 ConsultarPedidosContemIdPedido
❌ CT45 ConsultarPedidosContemInformacoesCarro
❌ CT46 ConsultarPedidosContemInformacoesPacote
❌ CT47 ConsultarPedidosContemDataHoraInicio
❌ CT48 ConsultarPedidosContemDataHoraFim
❌ CT49 ConsultarPedidosContemStatusPedido
❌ CT50 ConsultarPedidosContemValorTotal
❌ CT51 ConsultarPedidosNaoExpoeDadosSensiveis

### Casos de Performance
❌ CT52 ConsultarPedidosComGrandeVolumeDados
❌ CT53 ConsultarPedidosTempoRespostaMenor2Segundos
❌ CT54 ConsultarPedidosConcorrentementeMultiplosUsuarios

---
## Testes de Integração

### Cenário 1: Jornada Completa de Sucesso (Happy Path)
❌ IT01 JornadaCompletaUsuarioNovo
- Registrar novo usuário
- Adicionar cartão de crédito ao perfil
- Consultar pacotes disponíveis
- Consultar carros disponíveis
- Criar pedido
- Adicionar cartão para pagamento
- Transferir dinheiro
- Retirar carro
- Valida: Fluxo end-to-end com banco de dados real

### Cenário 2: Fluxo de Pagamento Completo
❌ IT02 FluxoPagamentoCartaoCredito
- Criar usuário e autenticar
- Criar pedido
- Adicionar cartão (POST /payment/addCreditCard)
- Transferir valor exato (PUT /payment/moneyTransfer)
- Verificar saldo atualizado no banco
- Valida: Integração entre PaymentService, UserRepository, CreditCard

❌ IT03 FluxoPagamentoComTroco
- Criar pedido de R$ 500
- Transferir R$ 1000
- Verificar cálculo de troco
- Validar status do pedido como "PAGO"
- Valida: Lógica de negócio de troco + persistência

### Cenário 3: Disponibilidade de Carros
❌ IT04 ConsultarEReservarCarroDisponivel
- Consultar carros disponíveis (GET /cars/available)
- Criar pedido para carro específico
- Verificar que carro fica indisponível
- Consultar novamente e validar que carro não aparece
- Valida: Sincronização entre OrderService e CarRepository

❌ IT05 RetirarCarroAtualizaDisponibilidade
- Criar pedido e pagar
- Retirar carro (POST /delivery)
- Verificar isAvailable = false no banco
- Validar PlacedOrder criado com timestamps
- Valida: Integração DeliveryService + CarRepository + OrderRepository

### Cenário 4: Autenticação e Autorização
❌ IT06 UsuarioNaoPodeAcessarRecursoSemToken
- Tentar consultar /cars/packages sem autenticação
- Esperar HTTP 401 ou 403
- Valida: Spring Security com JWT

❌ IT07 UsuarioSoRetiraProprioCarroPedido
- Usuário A cria pedido
- Usuário B tenta retirar carro do pedido de A
- Esperar exceção ou HTTP 403
- Valida: Autorização baseada em ownership

### Cenário 5: Validação de Cartão de Crédito
❌ IT08 AdicionarCartaoDuplicadoNaoPersiste
- Adicionar cartão com número 1234567890123456
- Tentar adicionar mesmo cartão novamente
- Validar que apenas um registro existe no banco
- Valida: Constraint UNIQUE ou lógica de duplicação

❌ IT09 PagamentoSemCartaoCadastrado
- Criar pedido
- Tentar transferir dinheiro sem ter cartão
- Esperar NoCreditCardException
- Valida: Regra de negócio + tratamento de exceções

### Cenário 6: Regras de Pacotes
❌ IT10 NaoPodeRetirarCarroDePacoteDiferente
- Criar pedido para pacote BASIC
- Pagar e obter access key para BASIC
- Tentar retirar carro do pacote PREMIUM
- Esperar InvalidPackageException
- Valida: Lógica de AccessKey + validação de pacote

❌ IT11 ConsultarPacotesComPaginacaoEOrdenacao
- Consultar página 0, tamanho 5, ordenado por preço ASC
- Validar que retorna no máximo 5 pacotes
- Validar ordenação crescente de preços
- Valida: Pageable do Spring Data JPA

### Cenário 7: Pedidos e Timestamps
❌ IT12 CriarPedidoComHorasLimite
- Criar pedido com hours = 1 (mínimo)
- Criar pedido com hours = 720 (máximo)
- Validar cálculo de preço correto
- Valida: Lógica de cálculo + valor limite

❌ IT13 RetirarCarroRegistraTimestampCorreto
- Retirar carro às 14:00
- Verificar PlacedOrder.startTime ~= 14:00
- Verificar PlacedOrder.endTime = startTime + hours
- Valida: LocalDateTime persistence + timezone

### Cenário 8: Filtros e Buscas
❌ IT14 FiltrarCarrosPorMultiplosCriterios
- Filtrar por fuelType = GASOLINE, minPrice = 100, maxPrice = 500
- Validar que todos resultados atendem critérios
- Valida: Query methods do Spring Data + Specification

❌ IT15 ConsultarCarrosComPaginacaoRetornaMetadata
- Consultar página 1, tamanho 10
- Validar Page.totalElements, totalPages, hasNext
- Valida: PageImpl do Spring Data

### Cenário 9: Edge Cases de Negócio
❌ IT16 NaoPodeCriarPedidoSemCarrosDisponiveis
- Marcar todos carros como isAvailable = false
- Tentar criar pedido
- Esperar UnavailableCarException
- Valida: Validação de disponibilidade em tempo real

❌ IT17 RegistroComEmailDuplicadoFalha
- Registrar user1@example.com
- Tentar registrar user1@example.com novamente
- Esperar DuplicateEmailException ou constraint violation
- Valida: Constraint UNIQUE no banco

❌ IT18 TransferirDinheiroParaPedidoJaPago
- Pagar pedido completamente
- Tentar transferir novamente
- Esperar OrderAlreadyPaidException
- Valida: Idempotência de pagamento

### Cenário 10: Transações e Rollback
❌ IT19 FalhaAoRetirarCarroNaoAfetaBanco
- Configurar mock para falhar em deliveryService.pickUpTheCar()
- Tentar retirar carro
- Verificar que isAvailable continua true (rollback)
- Valida: @Transactional rollback

❌ IT20 CriacaoPedidoComFalhaRollbackCompleto
- Criar pedido mas simular falha ao salvar AccessKey
- Verificar que Order não foi persistido
- Verificar que Car continua disponível
- Valida: Transação ACID + rollback em cascata

---
## Totais
Total casos unitários não implementados: 276
Total casos de integração não implementados: 20
Total geral: 296
