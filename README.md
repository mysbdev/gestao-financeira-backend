# FINANSE — Backend

Backend Spring Boot do FINANSE, modelado a partir do DER/RFs do TCC.

## Como rodar

Pré-requisitos: JDK 17+ (veja a nota sobre JAVA_HOME abaixo se usar Windows/IntelliJ).

```bash
mvn spring-boot:run
```

Sobe em `http://localhost:8080`, com H2 em memória já populado
(`src/main/resources/data.sql`) com dois usuários de teste — senha `123456`
para os dois:

- **usuário 1 — maria@exemplo.com** — 78% do limite do cartão + meta de
  reserva 64% concluída, onboarding já feito.
- **usuário 2 — joao@exemplo.com** — sem uso relevante do rotativo, sem
  metas, onboarding **não** concluído (para testar o fluxo de onboarding).

Console do H2: `http://localhost:8080/h2-console`
(JDBC URL: `jdbc:h2:mem:finanse`, usuário `sa`, senha em branco).

## Autenticação

A API usa JWT. Endpoints em `/api/auth/**` são públicos; todo o resto exige
o header `Authorization: Bearer <token>`.

```
POST /api/auth/cadastro   { nome, email, senha, dataNascimento }
POST /api/auth/login      { email, senha }
```

Ambos retornam `{ token, usuarioId, nome, email, onboardingCompleto }`.

## Endpoints

| Recurso | Rota | RF |
|---|---|---|
| Cadastro/Login | `POST /api/auth/cadastro`, `POST /api/auth/login` | Quadro 3 |
| Onboarding | `GET/POST /api/usuarios/{id}/onboarding` | User Flow |
| Transações | `GET /api/transacoes?usuarioId=`, `POST`, `DELETE /{id}` | RF08, RF10, RF16 |
| Categorias | `GET/POST /api/categorias`, `PUT/DELETE /{id}` | — |
| Metas | `GET /api/metas?usuarioId=`, `POST`, `PUT/{id}`, `POST /{id}/aportes`, `PATCH /{id}/pausar`, `PATCH /{id}/reativar`, `DELETE /{id}` | RF13 |
| Conteúdo educativo | `GET /api/usuarios/{id}/educacao/recomendacoes` | RF17 |

## RF10 — Categorização automática

Ao registrar uma despesa **sem informar categoriaId**, o
`CategorizacaoService` tenta classificar pela descrição usando um
dicionário de palavras-chave (sem custo, sem chamada externa). Se
encontrar, preenche a categoria automaticamente e grava a sugestão em
`SugestaoIa` (com nível de confiança), para o usuário confirmar ou trocar
depois. Se não encontrar nenhuma palavra-chave, a transação fica sem
categoria — o usuário escolhe manualmente.

Trocar por uma API de NLP de verdade depois é só reescrever o método
`classificar()` dessa classe — o resto do sistema não muda.

## RF16 + RF17 — Rotativo e recomendação de conteúdo

Toda despesa `viaCartaoCredito=true` atualiza `limiteCreditoUtilizado` do
usuário. O `ConteudoEducativoService` usa esse percentual (limiar em
`RegrasFinanceiras.LIMIAR_ALERTA_ROTATIVO`, hoje 50%) + progresso das metas
ativas para decidir os conteúdos recomendados (Quadro 16).

## Nota sobre JDK no Windows/IntelliJ

Se o build falhar com `No compiler is provided in this environment`, o
Maven está usando um JRE em vez de um JDK completo. No IntelliJ: File →
Project Structure → Project → SDK → selecione um JDK 17 de verdade
(geralmente em `C:\Users\<você>\.jdks\`).

## Estrutura

```
entity/       -> entidades JPA
repository/   -> Spring Data JPA
security/     -> JWT (filtro, util, SecurityConfig, UserDetailsService)
service/      -> regras de negócio
  CategorizacaoService       -> RF10 (heurística de palavras-chave)
  TransacaoService           -> RF08 + RF10 + RF16
  ConteudoEducativoService   -> RF17
  OnboardingService          -> fluxo de onboarding
  AuthService                -> cadastro/login
controller/   -> endpoints REST
dto/          -> objetos de requisição/resposta
enums/        -> categorias de conteúdo educativo
config/       -> RegrasFinanceiras (constantes) + GlobalExceptionHandler
```

## Próximos passos sugeridos

1. Front-end (Next.js) consumindo esses endpoints.
2. Refresh token / expiração mais robusta do JWT.
3. Endpoints de histórico/gráficos (RF14, RF11) — hoje só o histórico cru
   via `GET /api/transacoes`.
4. Trocar H2 por Postgres em produção (bloco já comentado em
   `application.properties`).
