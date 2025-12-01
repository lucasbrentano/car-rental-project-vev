# Casos de Teste - Sistema de Aluguel de Carros

## Sumário Executivo

| Etapa | Endpoint | Técnica Principal | Total Casos | ✅ Implementados | ❌ Não Implementados | Cobertura |
|-------|----------|-------------------|-------------|------------------|----------------------|-----------|
| **1** | POST /registration | Particionamento + Valor Limite | 23 | 2 | 21 | 8.7% |
| **2** | PUT /users/{username}/creditCards | Particionamento + Valor Limite | 37 | 0 | 37 | 0% |
| **3a** | GET /cars/packages | Particionamento + Valor Limite | 22 | 2 | 20 | 9.1% |
| **3b** | GET /cars/available | Particionamento + Valor Limite | 33 | 3 | 30 | 9.1% |
| **4** | POST /orders?carPackage=X&hours=Y | Particionamento + Valor Limite | 35 | 7 | 28 | 20% |
| **5a** | POST /payment/addCreditCard | Particionamento + Valor Limite | 34 | 0 | 34 | 0% |
| **5b** | PUT /payment/moneyTransfer | Particionamento + Valor Limite | 27 | 3 | 24 | 11.1% |
| **6** | POST /delivery?carId={id} | Particionamento + Regras de Negócio | 34 | 2 | 32 | 5.9% |
| **TOTAL** | - | - | **245** | **19** | **226** | **7.8%** |

---

## Etapa 1: Criação de Conta

**Endpoint:** `POST /registration`  
**Técnicas:** Particionamento de Equivalência + Análise de Valor Limite

### Casos de Sucesso (Partição Válida)

| Status | ID | Caso de Teste | Técnica |
|--------|-----|---------------|---------|
| ❌ | CT01 | RegistroComDadosValidos | Particionamento |
| ❌ | CT02 | RegistroComUsernameNoLimiteMinimo | Valor Limite |
| ❌ | CT03 | RegistroComUsernameNoLimiteMaximo | Valor Limite |
| ❌ | CT04 | RegistroComPasswordNoLimiteMinimo | Valor Limite |

### Casos de Falha - Username

| Status | ID | Caso de Teste | Técnica |
|--------|-----|---------------|---------|
| ❌ | CT05 | RegistroComUsernameVazio | Particionamento |
| ❌ | CT06 | RegistroComUsernameNulo | Particionamento |
| ❌ | CT07 | RegistroComUsernameMuitoCurto | Valor Limite |
| ❌ | CT08 | RegistroComUsernameMuitoLongo | Valor Limite |
| ❌ | CT09 | RegistroComUsernameCaracteresInvalidos | Particionamento |
| ✅ | CT10 | RegistroComUsernameDuplicado | Particionamento |

### Casos de Falha - Password

| Status | ID | Caso de Teste | Técnica |
|--------|-----|---------------|---------|
| ❌ | CT11 | RegistroComPasswordVazio | Particionamento |
| ❌ | CT12 | RegistroComPasswordNulo | Particionamento |
| ❌ | CT13 | RegistroComPasswordMuitoCurto | Valor Limite |
| ✅ | CT14 | RegistroComPasswordSemCaracteresEspeciais | Particionamento |

### Casos de Falha - Email

| Status | ID | Caso de Teste | Técnica |
|--------|-----|---------------|---------|
| ❌ | CT15 | RegistroComEmailVazio | Particionamento |
| ❌ | CT16 | RegistroComEmailNulo | Particionamento |
| ❌ | CT17 | RegistroComEmailFormatoInvalido | Particionamento |
| ❌ | CT18 | RegistroComEmailDuplicado | Particionamento |

### Casos de Falha - Dados Pessoais

| Status | ID | Caso de Teste | Técnica |
|--------|-----|---------------|---------|
| ❌ | CT19 | RegistroComNomeVazio | Particionamento |
| ❌ | CT20 | RegistroComTelefoneFormatoInvalido | Particionamento |

### Casos de Falha - Combinações

| Status | ID | Caso de Teste | Técnica |
|--------|-----|---------------|---------|
| ❌ | CT21 | RegistroComMultiplosCamposInvalidos | Particionamento |
| ❌ | CT22 | RegistroComTodosCamposVazios | Particionamento |
| ❌ | CT23 | RegistroComBodyVazio | Particionamento |

**Resumo Etapa 1:** 2/23 casos implementados (8.7%)

---

## Etapa 2: Adicionar Cartão de Crédito

**Endpoint:** `PUT /users/{username}/creditCards`  
**Técnicas:** Particionamento de Equivalência + Análise de Valor Limite

### Casos de Sucesso (Partição Válida)

| Status | ID | Caso de Teste | Técnica |
|--------|-----|---------------|---------|
| ❌ | CT01 | AdicionarCartaoComDadosValidos | Particionamento |
| ❌ | CT02 | AdicionarCartaoComValidadeProxima | Valor Limite |
| ❌ | CT03 | AdicionarCartaoComValidadeFutura | Particionamento |
| ❌ | CT04 | AdicionarSegundoCartao | Particionamento |
| ❌ | CT05 | AdicionarCartaoComNomeDoisCaracteres | Valor Limite |

### Casos de Falha - Autenticação/Autorização

| Status | ID | Caso de Teste | Técnica |
|--------|-----|---------------|---------|
| ❌ | CT06 | AdicionarCartaoSemAutenticacao | Particionamento |
| ❌ | CT07 | AdicionarCartaoParaOutroUsuario | Particionamento |
| ❌ | CT08 | AdicionarCartaoUsuarioInexistente | Particionamento |

### Casos de Falha - Número do Cartão

| Status | ID | Caso de Teste | Técnica |
|--------|-----|---------------|---------|
| ❌ | CT09 | AdicionarCartaoNumeroVazio | Particionamento |
| ❌ | CT10 | AdicionarCartaoNumeroNulo | Particionamento |
| ❌ | CT11 | AdicionarCartaoCom15Digitos | Valor Limite |
| ❌ | CT12 | AdicionarCartaoCom17Digitos | Valor Limite |
| ❌ | CT13 | AdicionarCartaoComLetras | Particionamento |
| ❌ | CT14 | AdicionarCartaoComCaracteresEspeciais | Particionamento |
| ❌ | CT15 | AdicionarCartaoAlgoritmoLuhnInvalido | Particionamento |
| ❌ | CT16 | AdicionarCartaoDuplicado | Particionamento |

### Casos de Falha - Nome do Titular

| Status | ID | Caso de Teste | Técnica |
|--------|-----|---------------|---------|
| ❌ | CT17 | AdicionarCartaoNomeTitularVazio | Particionamento |
| ❌ | CT18 | AdicionarCartaoNomeTitularNulo | Particionamento |
| ❌ | CT19 | AdicionarCartaoNomeTitularComNumeros | Particionamento |
| ❌ | CT20 | AdicionarCartaoNomeTitularCaracteresEspeciais | Particionamento |
| ❌ | CT21 | AdicionarCartaoNomeTitularUmCaractere | Valor Limite |

### Casos de Falha - Data de Validade

| Status | ID | Caso de Teste | Técnica |
|--------|-----|---------------|---------|
| ❌ | CT22 | AdicionarCartaoValidadeVazia | Particionamento |
| ❌ | CT23 | AdicionarCartaoValidadeNula | Particionamento |
| ❌ | CT24 | AdicionarCartaoValidadeFormatoIncorreto | Particionamento |
| ❌ | CT25 | AdicionarCartaoValidadePassada | Particionamento |
| ❌ | CT26 | AdicionarCartaoValidadeMes00 | Valor Limite |
| ❌ | CT27 | AdicionarCartaoValidadeMes13 | Valor Limite |
| ❌ | CT28 | AdicionarCartaoValidadeMesInvalido | Particionamento |

### Casos de Falha - CVV

| Status | ID | Caso de Teste | Técnica |
|--------|-----|---------------|---------|
| ❌ | CT29 | AdicionarCartaoCvvVazio | Particionamento |
| ❌ | CT30 | AdicionarCartaoCvvNulo | Particionamento |
| ❌ | CT31 | AdicionarCartaoCvvCom2Digitos | Valor Limite |
| ❌ | CT32 | AdicionarCartaoCvvCom4Digitos | Valor Limite |
| ❌ | CT33 | AdicionarCartaoCvvComLetras | Particionamento |
| ❌ | CT34 | AdicionarCartaoCvvComCaracteresEspeciais | Particionamento |

### Casos de Falha - Combinações

| Status | ID | Caso de Teste | Técnica |
|--------|-----|---------------|---------|
| ❌ | CT35 | AdicionarCartaoMultiplosCamposInvalidos | Particionamento |
| ❌ | CT36 | AdicionarCartaoTodosCamposVazios | Particionamento |
| ❌ | CT37 | AdicionarCartaoBodyVazio | Particionamento |

**Resumo Etapa 2:** 0/37 casos implementados (0%)

---

## Etapa 3a: Consultar Pacotes Disponíveis

**Endpoint:** `GET /cars/packages`  
**Técnicas:** Particionamento de Equivalência + Análise de Valor Limite

### Casos de Sucesso (Partição Válida)

| Status | ID | Caso de Teste | Técnica |
|--------|-----|---------------|---------|
| ✅ | CT01 | ConsultarPacotesSemParametros | Particionamento |
| ✅ | CT02 | ConsultarPacotesComPaginacao | Particionamento |
| ❌ | CT03 | ConsultarPacotesPaginaEspecifica | Particionamento |
| ❌ | CT04 | ConsultarPacotesOrdenadoPorPrecoAsc | Particionamento |
| ❌ | CT05 | ConsultarPacotesOrdenadoPorPrecoDesc | Particionamento |
| ❌ | CT06 | ConsultarPacotesOrdenadoPorNome | Particionamento |
| ❌ | CT07 | ConsultarPacotesComPaginacaoEOrdenacao | Particionamento |
| ❌ | CT08 | ConsultarPacotesTamanhoMinimo | Valor Limite |
| ❌ | CT09 | ConsultarPacotesTamanhoMaximo | Valor Limite |
| ❌ | CT10 | ConsultarPacotesListaVazia | Particionamento |
| ❌ | CT11 | ConsultarPacotesPaginaAlemDoLimite | Valor Limite |

### Casos de Falha - Autenticação

| Status | ID | Caso de Teste | Técnica |
|--------|-----|---------------|---------|
| ❌ | CT12 | ConsultarPacotesSemAutenticacao | Particionamento |
| ❌ | CT13 | ConsultarPacotesTokenExpirado | Particionamento |
| ❌ | CT14 | ConsultarPacotesTokenInvalido | Particionamento |

### Casos de Falha - Paginação

| Status | ID | Caso de Teste | Técnica |
|--------|-----|---------------|---------|
| ❌ | CT15 | ConsultarPacotesPageNegativo | Valor Limite |
| ❌ | CT16 | ConsultarPacotesSizeZero | Valor Limite |
| ❌ | CT17 | ConsultarPacotesSizeNegativo | Valor Limite |
| ❌ | CT18 | ConsultarPacotesPageNaoNumerico | Particionamento |
| ❌ | CT19 | ConsultarPacotesSizeNaoNumerico | Particionamento |

### Casos de Falha - Ordenação

| Status | ID | Caso de Teste | Técnica |
|--------|-----|---------------|---------|
| ❌ | CT20 | ConsultarPacotesCampoInexistente | Particionamento |
| ❌ | CT21 | ConsultarPacotesDirecaoInvalida | Particionamento |
| ❌ | CT22 | ConsultarPacotesFormatoSortIncorreto | Particionamento |

**Resumo Etapa 3a:** 2/22 casos implementados (9.1%)

---

## Etapa 3b: Consultar Carros Disponíveis

**Endpoint:** `GET /cars/available`  
**Técnicas:** Particionamento de Equivalência + Análise de Valor Limite

### Casos de Sucesso (Partição Válida)

| Status | ID | Caso de Teste | Técnica |
|--------|-----|---------------|---------|
| ✅ | CT23 | ConsultarCarrosDisponiveis | Particionamento |
| ✅ | CT24 | ConsultarCarrosFiltroFuelType | Particionamento |
| ✅ | CT25 | ConsultarCarrosFiltroGearBoxType | Particionamento |
| ❌ | CT26 | ConsultarCarrosFiltroPrecoMinimo | Particionamento |
| ❌ | CT27 | ConsultarCarrosFiltroPrecoMaximo | Particionamento |
| ❌ | CT28 | ConsultarCarrosFiltroFaixaPreco | Particionamento |
| ❌ | CT29 | ConsultarCarrosFiltroBrand | Particionamento |
| ❌ | CT30 | ConsultarCarrosFiltroModel | Particionamento |
| ❌ | CT31 | ConsultarCarrosMultiplosFiltros | Particionamento |
| ❌ | CT32 | ConsultarCarrosComPaginacao | Particionamento |
| ❌ | CT33 | ConsultarCarrosComOrdenacao | Particionamento |
| ❌ | CT34 | ConsultarCarrosFiltrosEPaginacao | Particionamento |
| ❌ | CT35 | ConsultarCarrosListaVazia | Particionamento |
| ❌ | CT36 | ConsultarCarrosPrecoMinimoZero | Valor Limite |
| ❌ | CT37 | ConsultarCarrosPrecosMinimoIgualMaximo | Valor Limite |

### Casos de Falha - Autenticação

| Status | ID | Caso de Teste | Técnica |
|--------|-----|---------------|---------|
| ❌ | CT38 | ConsultarCarrosSemAutenticacao | Particionamento |
| ❌ | CT39 | ConsultarCarrosTokenExpirado | Particionamento |
| ❌ | CT40 | ConsultarCarrosTokenInvalido | Particionamento |

### Casos de Falha - Filtros de Tipo

| Status | ID | Caso de Teste | Técnica |
|--------|-----|---------------|---------|
| ❌ | CT41 | ConsultarCarrosFuelTypeInvalido | Particionamento |
| ❌ | CT42 | ConsultarCarrosGearBoxTypeInvalido | Particionamento |

### Casos de Falha - Filtros de Preço

| Status | ID | Caso de Teste | Técnica |
|--------|-----|---------------|---------|
| ❌ | CT43 | ConsultarCarrosMinPriceNegativo | Valor Limite |
| ❌ | CT44 | ConsultarCarrosMaxPriceMenorQueMin | Valor Limite |
| ❌ | CT45 | ConsultarCarrosMinPriceNaoNumerico | Particionamento |
| ❌ | CT46 | ConsultarCarrosMaxPriceNaoNumerico | Particionamento |

### Casos de Falha - Paginação

| Status | ID | Caso de Teste | Técnica |
|--------|-----|---------------|---------|
| ❌ | CT47 | ConsultarCarrosPageNegativo | Valor Limite |
| ❌ | CT48 | ConsultarCarrosSizeZero | Valor Limite |
| ❌ | CT49 | ConsultarCarrosSizeNegativo | Valor Limite |
| ❌ | CT50 | ConsultarCarrosPageNaoNumerico | Particionamento |
| ❌ | CT51 | ConsultarCarrosSizeNaoNumerico | Particionamento |

### Casos de Falha - Ordenação

| Status | ID | Caso de Teste | Técnica |
|--------|-----|---------------|---------|
| ❌ | CT52 | ConsultarCarrosCampoInexistente | Particionamento |
| ❌ | CT53 | ConsultarCarrosDirecaoInvalida | Particionamento |
| ❌ | CT54 | ConsultarCarrosFormatoSortIncorreto | Particionamento |

### Casos de Falha - Combinações

| Status | ID | Caso de Teste | Técnica |
|--------|-----|---------------|---------|
| ❌ | CT55 | ConsultarCarrosMultiplosParametrosInvalidos | Particionamento |

**Resumo Etapa 3b:** 3/33 casos implementados (9.1%)

---

## Etapa 4: Selecionar Pacote e Fazer Pedido

**Endpoint:** `POST /orders?carPackage=X&hours=Y`  
**Técnicas:** Particionamento de Equivalência + Análise de Valor Limite + Regras de Negócio

### Casos de Sucesso (Partição Válida)

| Status | ID | Caso de Teste | Técnica |
|--------|-----|---------------|---------|
| ✅ | CT01 | CriarPedidoComDadosValidos | Particionamento |
| ❌ | CT02 | CriarPedidoHoraMinima | Valor Limite |
| ❌ | CT03 | CriarPedidoHoraLimiteMaximo | Valor Limite |
| ❌ | CT04 | CriarPedidoHoraMediana | Particionamento |
| ❌ | CT05 | CriarPedidoComPacoteBasico | Particionamento |
| ❌ | CT06 | CriarPedidoComPacotePremium | Particionamento |
| ❌ | CT07 | CriarPedidoCalculoPrecoCorreto | Regras de Negócio |
| ❌ | CT08 | CriarPedidoAtribuiCarroDisponivel | Regras de Negócio |
| ❌ | CT09 | CriarPedidoUsuarioComMultiplosCartoes | Particionamento |

### Casos de Falha - Autenticação/Autorização

| Status | ID | Caso de Teste | Técnica |
|--------|-----|---------------|---------|
| ❌ | CT10 | CriarPedidoSemAutenticacao | Particionamento |
| ❌ | CT11 | CriarPedidoTokenExpirado | Particionamento |
| ❌ | CT12 | CriarPedidoTokenInvalido | Particionamento |

### Casos de Falha - Pré-requisitos do Usuário

| Status | ID | Caso de Teste | Técnica |
|--------|-----|---------------|---------|
| ✅ | CT13 | CriarPedidoUsuarioSemCartao | Regras de Negócio |
| ✅ | CT14 | CriarPedidoUsuarioComPedidoAtivo | Regras de Negócio |
| ❌ | CT15 | CriarPedidoUsuarioInexistente | Particionamento |

### Casos de Falha - Parâmetro carPackage

| Status | ID | Caso de Teste | Técnica |
|--------|-----|---------------|---------|
| ❌ | CT16 | CriarPedidoCarPackageVazio | Particionamento |
| ❌ | CT17 | CriarPedidoCarPackageNulo | Particionamento |
| ✅ | CT18 | CriarPedidoCarPackageInexistente | Particionamento |
| ❌ | CT19 | CriarPedidoCarPackageInvalido | Particionamento |
| ❌ | CT20 | CriarPedidoCarPackageNegativo | Valor Limite |
| ❌ | CT21 | CriarPedidoCarPackageZero | Valor Limite |

### Casos de Falha - Parâmetro hours

| Status | ID | Caso de Teste | Técnica |
|--------|-----|---------------|---------|
| ❌ | CT22 | CriarPedidoHoursVazio | Particionamento |
| ❌ | CT23 | CriarPedidoHoursNulo | Particionamento |
| ❌ | CT24 | CriarPedidoHoursZero | Valor Limite |
| ❌ | CT25 | CriarPedidoHoursNegativo | Valor Limite |
| ❌ | CT26 | CriarPedidoHoursAcimaLimite | Valor Limite |
| ❌ | CT27 | CriarPedidoHoursInvalido | Particionamento |
| ❌ | CT28 | CriarPedidoHoursDecimal | Particionamento |

### Casos de Falha - Disponibilidade

| Status | ID | Caso de Teste | Técnica |
|--------|-----|---------------|---------|
| ✅ | CT29 | CriarPedidoSemCarrosDisponiveis | Regras de Negócio |
| ❌ | CT30 | CriarPedidoTodosCarrosEmUso | Regras de Negócio |

### Casos de Falha - Combinações

| Status | ID | Caso de Teste | Técnica |
|--------|-----|---------------|---------|
| ❌ | CT31 | CriarPedidoAmbosParametrosInvalidos | Particionamento |
| ❌ | CT32 | CriarPedidoSemParametros | Particionamento |
| ❌ | CT33 | CriarPedidoComBodyDesnecessario | Particionamento |

### Casos de Falha - Concorrência

| Status | ID | Caso de Teste | Técnica |
|--------|-----|---------------|---------|
| ❌ | CT34 | CriarPedidosSimultaneos | Regras de Negócio |
| ❌ | CT35 | CriarPedidoUltimoCarroDisponivel | Regras de Negócio |

**Resumo Etapa 4:** 7/35 casos implementados (20%)

---

## Etapa 5a: Adicionar Cartão para Pagamento

**Endpoint:** `POST /payment/addCreditCard`  
**Técnicas:** Particionamento de Equivalência + Análise de Valor Limite

### Casos de Sucesso (Partição Válida)

| Status | ID | Caso de Teste | Técnica |
|--------|-----|---------------|---------|
| ❌ | CT01 | AdicionarCartaoParaPagamento | Particionamento |
| ❌ | CT02 | AdicionarCartaoComValidadeProxima | Valor Limite |
| ❌ | CT03 | AdicionarCartaoComValidadeFutura | Particionamento |
| ❌ | CT04 | AdicionarCartaoNumero16Digitos | Valor Limite |
| ❌ | CT05 | AdicionarCartaoCvv3Digitos | Valor Limite |

### Casos de Falha - Autenticação

| Status | ID | Caso de Teste | Técnica |
|--------|-----|---------------|---------|
| ❌ | CT06 | AdicionarCartaoSemAutenticacao | Particionamento |
| ❌ | CT07 | AdicionarCartaoTokenExpirado | Particionamento |
| ❌ | CT08 | AdicionarCartaoTokenInvalido | Particionamento |

### Casos de Falha - Número do Cartão

| Status | ID | Caso de Teste | Técnica |
|--------|-----|---------------|---------|
| ❌ | CT09 | AdicionarCartaoNumeroVazio | Particionamento |
| ❌ | CT10 | AdicionarCartaoNumeroNulo | Particionamento |
| ❌ | CT11 | AdicionarCartaoNumero15Digitos | Valor Limite |
| ❌ | CT12 | AdicionarCartaoNumero17Digitos | Valor Limite |
| ❌ | CT13 | AdicionarCartaoNumeroComLetras | Particionamento |
| ❌ | CT14 | AdicionarCartaoNumeroComEspeciais | Particionamento |
| ❌ | CT15 | AdicionarCartaoLuhnInvalido | Particionamento |

### Casos de Falha - Nome do Titular

| Status | ID | Caso de Teste | Técnica |
|--------|-----|---------------|---------|
| ❌ | CT16 | AdicionarCartaoNomeVazio | Particionamento |
| ❌ | CT17 | AdicionarCartaoNomeNulo | Particionamento |
| ❌ | CT18 | AdicionarCartaoNomeComNumeros | Particionamento |
| ❌ | CT19 | AdicionarCartaoNomeComEspeciais | Particionamento |

### Casos de Falha - Data de Validade

| Status | ID | Caso de Teste | Técnica |
|--------|-----|---------------|---------|
| ❌ | CT20 | AdicionarCartaoValidadeVazia | Particionamento |
| ❌ | CT21 | AdicionarCartaoValidadeNula | Particionamento |
| ❌ | CT22 | AdicionarCartaoValidadePassada | Particionamento |
| ❌ | CT23 | AdicionarCartaoValidadeMes00 | Valor Limite |
| ❌ | CT24 | AdicionarCartaoValidadeMes13 | Valor Limite |
| ❌ | CT25 | AdicionarCartaoValidadeFormatoIncorreto | Particionamento |

### Casos de Falha - CVV

| Status | ID | Caso de Teste | Técnica |
|--------|-----|---------------|---------|
| ❌ | CT26 | AdicionarCartaoCvvVazio | Particionamento |
| ❌ | CT27 | AdicionarCartaoCvvNulo | Particionamento |
| ❌ | CT28 | AdicionarCartaoCvv2Digitos | Valor Limite |
| ❌ | CT29 | AdicionarCartaoCvv4Digitos | Valor Limite |
| ❌ | CT30 | AdicionarCartaoCvvComLetras | Particionamento |
| ❌ | CT31 | AdicionarCartaoCvvComEspeciais | Particionamento |

### Casos de Falha - Outros

| Status | ID | Caso de Teste | Técnica |
|--------|-----|---------------|---------|
| ❌ | CT32 | AdicionarCartaoDuplicado | Particionamento |
| ❌ | CT33 | AdicionarCartaoBodyVazio | Particionamento |
| ❌ | CT34 | AdicionarCartaoSemBody | Particionamento |

**Resumo Etapa 5a:** 0/34 casos implementados (0%)

---

## Etapa 5b: Efetuar Transferência de Pagamento

**Endpoint:** `PUT /payment/moneyTransfer?moneyAmount=...`  
**Técnicas:** Particionamento de Equivalência + Análise de Valor Limite + Regras de Negócio

### Casos de Sucesso (Partição Válida)

| Status | ID | Caso de Teste | Técnica |
|--------|-----|---------------|---------|
| ✅ | CT35 | TransferirValorExato | Particionamento |
| ❌ | CT36 | TransferirValorMaior | Particionamento |
| ❌ | CT37 | TransferirValorComDuasCasasDecimais | Particionamento |
| ❌ | CT38 | TransferirValorInteiro | Particionamento |
| ❌ | CT39 | TransferirValorMinimo | Valor Limite |
| ❌ | CT40 | PagamentoAtualizaStatusPedido | Regras de Negócio |
| ❌ | CT41 | PagamentoCalculaTroco | Regras de Negócio |
| ❌ | CT42 | PagamentoUsuarioComMultiplosCartoes | Particionamento |

### Casos de Falha - Autenticação

| Status | ID | Caso de Teste | Técnica |
|--------|-----|---------------|---------|
| ❌ | CT43 | TransferirSemAutenticacao | Particionamento |
| ❌ | CT44 | TransferirTokenExpirado | Particionamento |
| ❌ | CT45 | TransferirTokenInvalido | Particionamento |

### Casos de Falha - Pré-requisitos do Usuário

| Status | ID | Caso de Teste | Técnica |
|--------|-----|---------------|---------|
| ✅ | CT46 | TransferirUsuarioSemCartao | Regras de Negócio |
| ❌ | CT47 | TransferirUsuarioSemPedido | Regras de Negócio |
| ❌ | CT48 | TransferirUsuarioInexistente | Particionamento |

### Casos de Falha - Parâmetro moneyAmount

| Status | ID | Caso de Teste | Técnica |
|--------|-----|---------------|---------|
| ❌ | CT49 | TransferirMoneyAmountVazio | Particionamento |
| ❌ | CT50 | TransferirMoneyAmountNulo | Particionamento |
| ❌ | CT51 | TransferirMoneyAmountZero | Valor Limite |
| ❌ | CT52 | TransferirMoneyAmountNegativo | Valor Limite |
| ✅ | CT53 | TransferirMoneyAmountInsuficiente | Valor Limite |
| ❌ | CT54 | TransferirMoneyAmountInvalido | Particionamento |
| ❌ | CT55 | TransferirMoneyAmountComMaisCasasDecimais | Particionamento |

### Casos de Falha - Estado do Pedido

| Status | ID | Caso de Teste | Técnica |
|--------|-----|---------------|---------|
| ❌ | CT56 | TransferirPedidoJaPago | Regras de Negócio |
| ❌ | CT57 | TransferirPedidoCancelado | Regras de Negócio |
| ❌ | CT58 | TransferirPedidoInexistente | Particionamento |

### Casos de Falha - Saldo e Combinações

| Status | ID | Caso de Teste | Técnica |
|--------|-----|---------------|---------|
| ❌ | CT59 | TransferirSaldoInsuficiente | Regras de Negócio |
| ❌ | CT60 | TransferirMultiplosParametrosInvalidos | Particionamento |
| ❌ | CT61 | TransferirSemParametros | Particionamento |

**Resumo Etapa 5b:** 3/27 casos implementados (11.1%)

---

## Etapa 6: Retirar o Carro

**Endpoint:** `POST /delivery?carId={id}`  
**Técnicas:** Particionamento de Equivalência + Análise de Valor Limite + Regras de Negócio

### Casos de Sucesso (Partição Válida)

| Status | ID | Caso de Teste | Técnica |
|--------|-----|---------------|---------|
| ✅ | CT01 | RetirarCarroComDadosValidos | Particionamento |
| ❌ | CT02 | RetirarCarroImediatamenteAposPagamento | Regras de Negócio |
| ❌ | CT03 | RetirarCarroAposTempoEspera | Regras de Negócio |
| ❌ | CT04 | RetirarCarroAtualizaStatusPedido | Regras de Negócio |
| ❌ | CT05 | RetirarCarroAtualizaStatusCarro | Regras de Negócio |
| ❌ | CT06 | RetirarCarroRegistraTimestamp | Regras de Negócio |
| ❌ | CT07 | RetirarCarroCarIdMinimo | Valor Limite |
| ❌ | CT08 | RetirarCarroCarIdMaximo | Valor Limite |

### Casos de Falha - Autenticação/Autorização

| Status | ID | Caso de Teste | Técnica |
|--------|-----|---------------|---------|
| ❌ | CT09 | RetirarCarroSemAutenticacao | Particionamento |
| ❌ | CT10 | RetirarCarroTokenExpirado | Particionamento |
| ❌ | CT11 | RetirarCarroTokenInvalido | Particionamento |
| ❌ | CT12 | RetirarCarroUsuarioInexistente | Particionamento |

### Casos de Falha - Parâmetro carId

| Status | ID | Caso de Teste | Técnica |
|--------|-----|---------------|---------|
| ❌ | CT13 | RetirarCarroCarIdVazio | Particionamento |
| ❌ | CT14 | RetirarCarroCarIdNulo | Particionamento |
| ❌ | CT15 | RetirarCarroCarIdZero | Valor Limite |
| ❌ | CT16 | RetirarCarroCarIdNegativo | Valor Limite |
| ❌ | CT17 | RetirarCarroCarIdInexistente | Particionamento |
| ❌ | CT18 | RetirarCarroCarIdInvalido | Particionamento |

### Casos de Falha - Estado do Pedido

| Status | ID | Caso de Teste | Técnica |
|--------|-----|---------------|---------|
| ❌ | CT19 | RetirarCarroUsuarioSemPedido | Regras de Negócio |
| ✅ | CT20 | RetirarCarroPedidoNaoPago | Regras de Negócio |
| ❌ | CT21 | RetirarCarroPedidoJaEntregue | Regras de Negócio |
| ❌ | CT22 | RetirarCarroPedidoCancelado | Regras de Negócio |
| ❌ | CT23 | RetirarCarroSegundaTentativa | Regras de Negócio |

### Casos de Falha - Validação de Carro

| Status | ID | Caso de Teste | Técnica |
|--------|-----|---------------|---------|
| ❌ | CT24 | RetirarCarroNaoVinculadoPedido | Regras de Negócio |
| ❌ | CT25 | RetirarCarroDeOutroUsuario | Regras de Negócio |
| ❌ | CT26 | RetirarCarroIndisponivel | Regras de Negócio |
| ❌ | CT27 | RetirarCarroEmManutencao | Regras de Negócio |

### Casos de Falha - Pré-requisitos

| Status | ID | Caso de Teste | Técnica |
|--------|-----|---------------|---------|
| ❌ | CT28 | RetirarCarroUsuarioSemCartao | Regras de Negócio |
| ❌ | CT29 | RetirarCarroSemPagamento | Regras de Negócio |

### Casos de Falha - Combinações

| Status | ID | Caso de Teste | Técnica |
|--------|-----|---------------|---------|
| ❌ | CT30 | RetirarCarroMultiplosParametrosInvalidos | Particionamento |
| ❌ | CT31 | RetirarCarroSemParametros | Particionamento |
| ❌ | CT32 | RetirarCarroComBodyDesnecessario | Particionamento |

### Casos de Falha - Concorrência

| Status | ID | Caso de Teste | Técnica |
|--------|-----|---------------|---------|
| ❌ | CT33 | RetirarCarroSimultaneamente | Regras de Negócio |
| ❌ | CT34 | RetirarCarroMesmoUsuarioDuasVezes | Regras de Negócio |

**Resumo Etapa 6:** 2/34 casos implementados (5.9%)

---

## Etapa 7: Consultar Pedidos do Usuário

**Endpoint:** `GET /orders`  
**Técnicas:** Particionamento de Equivalência + Análise de Valor Limite + Regras de Negócio

### Casos de Sucesso (Partição Válida)

| Status | ID | Caso de Teste | Técnica |
|--------|-----|---------------|---------|
| ✅ | CT01 | ConsultarPedidosUsuarioAutenticado | Particionamento |
| ❌ | CT02 | ConsultarPedidosUsuarioComUmPedido | Particionamento |
| ✅ | CT03 | ConsultarPedidosUsuarioComMultiplosPedidos | Particionamento |
| ❌ | CT04 | ConsultarPedidosUsuarioComPedidoAtivo | Particionamento |
| ❌ | CT05 | ConsultarPedidosUsuarioComPedidoConcluido | Particionamento |
| ❌ | CT06 | ConsultarPedidosUsuarioComPedidoCancelado | Particionamento |
| ❌ | CT07 | ConsultarPedidosUsuarioComPedidoPago | Particionamento |
| ❌ | CT08 | ConsultarPedidosUsuarioComPedidoPendente | Particionamento |
| ❌ | CT09 | ConsultarPedidosOrdenadoPorDataDesc | Particionamento |
| ❌ | CT10 | ConsultarPedidosOrdenadoPorDataAsc | Particionamento |
| ❌ | CT11 | ConsultarPedidosComPaginacao | Particionamento |
| ❌ | CT12 | ConsultarPedidosPrimeiraPagina | Valor Limite |
| ❌ | CT13 | ConsultarPedidosUltimaPagina | Valor Limite |
| ❌ | CT14 | ConsultarPedidosListaVazia | Particionamento |
| ❌ | CT15 | ConsultarPedidosAposRetirada | Regras de Negócio |
| ❌ | CT16 | ConsultarPedidosComDiferentesCarros | Particionamento |
| ❌ | CT17 | ConsultarPedidosComDiferentesPacotes | Particionamento |

### Casos de Falha - Autenticação/Autorização

| Status | ID | Caso de Teste | Técnica |
|--------|-----|---------------|---------|
| ❌ | CT18 | ConsultarPedidosSemAutenticacao | Particionamento |
| ❌ | CT19 | ConsultarPedidosTokenExpirado | Particionamento |
| ❌ | CT20 | ConsultarPedidosTokenInvalido | Particionamento |
| ❌ | CT21 | ConsultarPedidosUsuarioInexistente | Particionamento |
| ❌ | CT22 | ConsultarPedidosUsuarioBloqueado | Particionamento |
| ❌ | CT23 | ConsultarPedidosTokenRevogado | Particionamento |

### Casos de Falha - Paginação

| Status | ID | Caso de Teste | Técnica |
|--------|-----|---------------|---------|
| ❌ | CT24 | ConsultarPedidosPageNegativo | Valor Limite |
| ❌ | CT25 | ConsultarPedidosSizeZero | Valor Limite |
| ❌ | CT26 | ConsultarPedidosSizeNegativo | Valor Limite |
| ❌ | CT27 | ConsultarPedidosPageNaoNumerico | Particionamento |
| ❌ | CT28 | ConsultarPedidosSizeNaoNumerico | Particionamento |
| ❌ | CT29 | ConsultarPedidosPageAlemLimite | Valor Limite |
| ❌ | CT30 | ConsultarPedidosSizeExcessivo | Valor Limite |

### Casos de Falha - Ordenação

| Status | ID | Caso de Teste | Técnica |
|--------|-----|---------------|---------|
| ❌ | CT31 | ConsultarPedidosCampoOrdenacaoInvalido | Particionamento |
| ❌ | CT32 | ConsultarPedidosDirecaoInvalida | Particionamento |
| ❌ | CT33 | ConsultarPedidosFormatoSortIncorreto | Particionamento |

### Casos de Falha - Filtros

| Status | ID | Caso de Teste | Técnica |
|--------|-----|---------------|---------|
| ❌ | CT34 | ConsultarPedidosStatusInvalido | Particionamento |
| ❌ | CT35 | ConsultarPedidosDataInicioInvalida | Particionamento |
| ❌ | CT36 | ConsultarPedidosDataFimInvalida | Particionamento |
| ❌ | CT37 | ConsultarPedidosDataInicioMaiorQueFim | Valor Limite |
| ❌ | CT38 | ConsultarPedidosCarIdInvalido | Particionamento |

### Casos de Falha - Combinações

| Status | ID | Caso de Teste | Técnica |
|--------|-----|---------------|---------|
| ❌ | CT39 | ConsultarPedidosMultiplosParametrosInvalidos | Particionamento |
| ❌ | CT40 | ConsultarPedidosPaginacaoEOrdenacaoInvalidas | Particionamento |

### Casos de Validação - Isolamento de Dados

| Status | ID | Caso de Teste | Técnica |
|--------|-----|---------------|---------|
| ✅ | CT41 | ConsultarPedidosNaoExibePedidosOutrosUsuarios | Regras de Negócio |
| ❌ | CT42 | ConsultarPedidosUsuarioAdminVerApenasPropriosPedidos | Regras de Negócio |
| ✅ | CT43 | ConsultarPedidosRetornaApenasUsuarioLogado | Regras de Negócio |

### Casos de Validação - Conteúdo da Resposta

| Status | ID | Caso de Teste | Técnica |
|--------|-----|---------------|---------|
| ❌ | CT44 | ConsultarPedidosContemIdPedido | Particionamento |
| ❌ | CT45 | ConsultarPedidosContemInformacoesCarro | Particionamento |
| ❌ | CT46 | ConsultarPedidosContemInformacoesPacote | Particionamento |
| ❌ | CT47 | ConsultarPedidosContemDataHoraInicio | Particionamento |
| ❌ | CT48 | ConsultarPedidosContemDataHoraFim | Particionamento |
| ❌ | CT49 | ConsultarPedidosContemStatusPedido | Particionamento |
| ❌ | CT50 | ConsultarPedidosContemValorTotal | Particionamento |
| ❌ | CT51 | ConsultarPedidosNaoExpoeDadosSensiveis | Particionamento |

### Casos de Performance

| Status | ID | Caso de Teste | Técnica |
|--------|-----|---------------|---------|
| ❌ | CT52 | ConsultarPedidosComGrandeVolumeDados | Particionamento |
| ❌ | CT53 | ConsultarPedidosTempoRespostaMenor2Segundos | Regras de Negócio |
| ❌ | CT54 | ConsultarPedidosConcorrentementeMultiplosUsuarios | Regras de Negócio |

**Resumo Etapa 7:** 4/54 casos implementados (7.4%)

---

## Análise Consolidada

### Cobertura por Técnica de Teste

| Técnica | Total Casos | ✅ Implementados | ❌ Não Implementados | % Cobertura |
|---------|-------------|------------------|----------------------|-------------|
| **Particionamento de Equivalência** | 207 | 16 | 191 | 7.7% |
| **Análise de Valor Limite** | 61 | 2 | 59 | 3.3% |
| **Regras de Negócio** | 31 | 5 | 26 | 16.1% |
| **TOTAL** | **299** | **23** | **276** | **7.7%** |

### Lacunas Críticas por Categoria

| Categoria de Validação | Total Casos | ✅ Testados | ❌ Não Testados | % Risco |
|------------------------|-------------|-------------|-----------------|---------|
| **Autenticação/Autorização** | 32 | 0 | 32 | 100% |
| **Validação de Entrada (nulo/vazio)** | 45 | 0 | 45 | 100% |
| **Valores Limite** | 61 | 2 | 59 | 96.7% |
| **Regras de Negócio** | 31 | 5 | 26 | 83.9% |
| **Estado do Sistema** | 26 | 3 | 23 | 88.5% |
| **Formatação de Dados** | 31 | 2 | 29 | 93.5% |
| **Combinações** | 17 | 0 | 17 | 100% |
| **Concorrência** | 6 | 0 | 6 | 100% |
| **Isolamento de Dados** | 3 | 2 | 1 | 33.3% |
| **Performance** | 3 | 0 | 3 | 100% |

#### 🔴 PRIORIDADE CRÍTICA (Implementar Imediatamente)
- **Autenticação/Autorização**: 0/32 casos (0%)
- **Validação de Entrada**: 0/45 casos (0%)
- **Isolamento de Dados (Etapa 7)**: 2/3 casos (66.7%) - ✅ Parcialmente coberto
- **Segurança (Etapa 6)**: Validação de carro pertence ao usuário
#### 🟠 PRIORIDADE ALTA
- **Valores Limite**: 2/61 casos (3.3%)
- **Regras de Negócio**: 5/31 casos (16.1%) - ✅ Melhorou com Etapa 7
- **Atualização de Estado**: Pedidos e carros
- **Conteúdo de Resposta (Etapa 7)**: Validação de estrutura de dados retornados
- **Valores Limite**: 2/52 casos (3.8%)
- **Regras de Negócio**: 3/25 casos (12%)
- **Atualização de Estado**: Pedidos e carros

#### 🟢 PRIORIDADE BAIXA
- **Concorrência**: Testes de race condition
- **Performance**: Testes de carga e tempo de resposta
- **Casos extremos**: Edge cases específicosos

#### 🟢 PRIORIDADE BAIXA
- **Concorrência**: Testes de race condition
- **Casos extremos**: Edge cases específicos

---

## Recomendações Finais

### Metas de Cobertura Sugeridas

| Fase | Meta | Prazo Sugerido |
### Ordem de Implementação Recomendada

1. **Semana 1-2**: Autenticação + Validação de Entrada (Todas as etapas)
2. **Semana 3-4**: Valores Limite + Regras de Negócio (Etapas 4, 5, 6, 7)
3. **Semana 5-6**: Isolamento de Dados (Etapa 7) + Formatação + Combinações
4. **Semana 7-8**: Performance + Testes de Integração
### Ordem de Implementação Recomendada

### Riscos Atuais em Produção

| Risco | Severidade | Etapas Afetadas |
|-------|------------|-----------------|
| Sem validação de autenticação | 🔴 CRÍTICO | Todas (2-7) |
| Sem validação de entrada | 🔴 CRÍTICO | Todas |
| Usuário pode acessar recursos de outro usuário | 🔴 CRÍTICO | 2, 6, 7 |
| Vazamento de dados entre usuários (GET /orders) | 🔴 CRÍTICO | 7 |
| Sem validação de estado de pedido | 🟠 ALTO | 5, 6 |
| Sem validação de valores limite | 🟡 MÉDIO | Todas |
| Performance não testada com grande volume | 🟡 MÉDIO | 7 |2-6) |
| Sem validação de entrada | 🔴 CRÍTICO | Todas |
**Documento gerado em:** 01/12/2024  
**Versão:** 1.2  
**Total de Casos de Teste:** 299  
**Casos Implementados:** 23 (7.7%)  
**Casos Pendentes:** 276 (92.3%)

---

## Notas sobre Testes Existentes da Etapa 7

### Testes Implementados:

**OrderControllerTest.java:**
- ✅ **CT01**: `itShouldGetAllOrders()` - Consulta básica de pedidos (controller mock)

**OrderServiceTest.java:**
- ✅ **CT03**: `itShouldReturnAllOrders()` - Consulta com múltiplos pedidos (4 pedidos de diferentes usuários)

**IT07_UsuarioSoRetiraProprioCarroPedido.java:**
- ✅ **CT41**: Validação de isolamento - usuário não vê/acessa pedidos de outros
- ✅ **CT43**: Validação de ownership através de LoggedInUser - apenas pedidos do usuário logado

### Observações Importantes:

1. **Isolamento de Dados**: Os testes de integração IT07 validam **indiretamente** o isolamento através do sistema de AccessKey, mas **NÃO testam explicitamente** se GET /orders retorna apenas pedidos do usuário logado. O teste valida que apenas o dono do AccessKey pode retirar o carro, não a listagem de pedidos.

2. **Lacuna Crítica**: Embora o sistema implemente controle de acesso via AccessKey, **não há teste explícito** validando que:
   - GET /orders filtra por usuário logado
   - Usuário A não vê pedidos do Usuário B na resposta HTTP
   - Admin vê apenas próprios pedidos

3. **Testes de Unidade vs Integração**: 
   - Testes de controller/service retornam TODOS os pedidos (`orderRepository.findAll()`)
   - Não há filtro por usuário implementado nos testes
   - Possível **BUG DE SEGURANÇA** se o código real não filtra por usuário

### Recomendação:
Criar teste de integração específico para GET /orders que valide:
```java
@Test
void deveRetornarApenaspedidosDoUsuarioLogado() {
    // Given: Usuário A tem 2 pedidos, Usuário B tem 1 pedido
    // When: Usuário A chama GET /orders
    // Then: Retorna apenas 2 pedidos (de A), não retorna pedido de B
}
```

**Documento gerado em:** 30/11/2024  
**Versão:** 1.0  
**Total de Casos de Teste:** 245  
**Casos Implementados:** 19 (7.8%)  
**Casos Pendentes:** 226 (92.2%)
