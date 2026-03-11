# ForumHub API

REST API para gerenciamento de tópicos de um fórum, desenvolvida como parte do desafio Alura. Permite que usuários autenticados criem, consultem, atualizem e excluam tópicos, com autenticação baseada em JWT.

---

## Tecnologias Utilizadas

| Tecnologia | Versão |
|---|---|
| Java | 17 |
| Spring Boot | 4.0.3 |
| Spring Security | 7.x (JWT stateless) |
| Spring Data JPA | - |
| Flyway | - |
| MySQL | 8+ |
| Auth0 java-jwt | 4.4.0 |
| Lombok | - |
| Maven | 4.x |

---

## Funcionalidades

- Cadastro de tópicos com validação de campos obrigatórios
- Bloqueio de tópicos duplicados (mesmo título e mensagem)
- Listagem de todos os tópicos
- Detalhamento de tópico por ID
- Atualização parcial de tópico
- Exclusão de tópico
- Autenticação via JWT (login com login/senha)
- Proteção de todos os endpoints (exceto `/login`)

---

## Estrutura do Projeto

```
src/main/java/com/tec/forumhub/
├── controller/
│   ├── AutenticacaoController.java   # POST /login
│   └── TopicoController.java         # CRUD /topicos
├── domain/
│   ├── topico/
│   │   ├── DadosAtualizacaoTopico.java
│   │   ├── DadosCadastroTopico.java
│   │   ├── DadosDetalhamentoTopico.java
│   │   ├── DadosListagemTopico.java
│   │   ├── StatusTopico.java         # Enum de status
│   │   ├── Topico.java               # Entidade JPA
│   │   └── TopicoRepository.java
│   └── usuario/
│       ├── AutenticacaoService.java  # UserDetailsService
│       ├── DadosAutenticacao.java
│       ├── Usuario.java              # Entidade JPA + UserDetails
│       └── UsuarioRepository.java
└── infra/
    ├── exception/
    │   └── TratadorDeErros.java      # Handler global de erros
    └── security/
        ├── DadosTokenJWT.java
        ├── SecurityConfigurations.java
        ├── SecurityFilter.java       # Filtro JWT por requisição
        └── TokenService.java         # Geração e validação JWT

src/main/resources/
├── application.properties
└── db/migration/
    ├── V1__create_topicos_table.sql
    ├── V2__create_usuarios_table.sql
    └── V3__insert_admin_user.sql
```

---

## Configuração e Execução

### Pré-requisitos

- Java 17+
- Maven 4+
- MySQL 8+ rodando em `localhost:3306`

### 1. Configurar Banco de Dados

O banco `forumhub` é criado automaticamente na primeira execução (parâmetro `createDatabaseIfNotExist=true` na URL). Certifique-se que o MySQL está rodando e que as credenciais estão corretas.

### 2. Configurar `application.properties`

Edite o arquivo `src/main/resources/application.properties`:

```properties
spring.datasource.username=root
spring.datasource.password=SUA_SENHA_AQUI

api.security.token.secret=SUA_CHAVE_SECRETA_COM_MINIMO_32_CARACTERES
```

Alternativamente, use variáveis de ambiente:
```bash
JWT_SECRET=minha-chave-super-secreta-aqui mvn spring-boot:run
```

### 3. Usuário Admin

Um usuário administrador é criado automaticamente pelo Flyway na migration `V3`:

| Campo | Valor |
|-------|-------|
| login | `admin@forumhub.com` |
| senha | `admin123` |

> Troque a senha em produção criando uma nova migration `V4__update_admin_password.sql` com um hash BCrypt gerado em [bcrypt-generator.com](https://bcrypt-generator.com).

### 4. Executar a Aplicação

```bash
mvn spring-boot:run
```

A aplicação estará disponível em `http://localhost:8080`.

---

## Endpoints da API

### Autenticação

#### POST /login

Autentica o usuário e retorna o token JWT.

**Requisição:**
```http
POST /login
Content-Type: application/json

{
  "login": "admin@forumhub.com",
  "senha": "admin123"
}
```

**Resposta — 200 OK:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

---

### Tópicos

Todos os endpoints de tópicos exigem autenticação. Inclua o token JWT no header:

```
Authorization: Bearer SEU_TOKEN_AQUI
```

#### POST /topicos — Cadastrar tópico

**Requisição:**
```http
POST /topicos
Authorization: Bearer <token>
Content-Type: application/json

{
  "titulo": "Dúvida sobre JPA",
  "mensagem": "Como funciona o lazy loading no Hibernate?",
  "autor": "João Silva",
  "curso": "Java e Spring Boot"
}
```

**Resposta — 201 Created:**
```json
{
  "id": 1,
  "titulo": "Dúvida sobre JPA",
  "mensagem": "Como funciona o lazy loading no Hibernate?",
  "dataCriacao": "2024-01-15T10:30:00",
  "status": "NAO_RESPONDIDO",
  "autor": "João Silva",
  "curso": "Java e Spring Boot"
}
```

**Validações:**
- Todos os campos são obrigatórios
- Não são permitidos tópicos com o mesmo título **e** mensagem (retorna `400 Bad Request`)

---

#### GET /topicos — Listar todos os tópicos

**Requisição:**
```http
GET /topicos
Authorization: Bearer <token>
```

**Resposta — 200 OK:**
```json
[
  {
    "id": 1,
    "titulo": "Dúvida sobre JPA",
    "autor": "João Silva",
    "curso": "Java e Spring Boot",
    "dataCriacao": "2024-01-15T10:30:00",
    "status": "NAO_RESPONDIDO"
  }
]
```

---

#### GET /topicos/{id} — Detalhar tópico

**Requisição:**
```http
GET /topicos/1
Authorization: Bearer <token>
```

**Resposta — 200 OK:**
```json
{
  "id": 1,
  "titulo": "Dúvida sobre JPA",
  "mensagem": "Como funciona o lazy loading no Hibernate?",
  "dataCriacao": "2024-01-15T10:30:00",
  "status": "NAO_RESPONDIDO",
  "autor": "João Silva",
  "curso": "Java e Spring Boot"
}
```

**Erros:**
- `404 Not Found` — ID não encontrado

---

#### PUT /topicos/{id} — Atualizar tópico

Todos os campos são opcionais. Apenas os campos informados serão atualizados.

**Requisição:**
```http
PUT /topicos/1
Authorization: Bearer <token>
Content-Type: application/json

{
  "titulo": "Dúvida sobre JPA - Atualizado",
  "mensagem": "Como funciona o lazy loading e o eager loading?"
}
```

**Resposta — 200 OK:**
```json
{
  "id": 1,
  "titulo": "Dúvida sobre JPA - Atualizado",
  "mensagem": "Como funciona o lazy loading e o eager loading?",
  "dataCriacao": "2024-01-15T10:30:00",
  "status": "NAO_RESPONDIDO",
  "autor": "João Silva",
  "curso": "Java e Spring Boot"
}
```

**Erros:**
- `404 Not Found` — ID não encontrado
- `400 Bad Request` — título e mensagem duplicados

---

#### DELETE /topicos/{id} — Excluir tópico

**Requisição:**
```http
DELETE /topicos/1
Authorization: Bearer <token>
```

**Resposta — 204 No Content**

**Erros:**
- `404 Not Found` — ID não encontrado

---

## Tabela Resumida de Endpoints

| Método | URI | Auth | Descrição |
|--------|-----|------|-----------|
| POST | `/login` | Não | Autenticar e obter token JWT |
| POST | `/topicos` | Sim | Criar novo tópico |
| GET | `/topicos` | Sim | Listar todos os tópicos |
| GET | `/topicos/{id}` | Sim | Detalhar tópico por ID |
| PUT | `/topicos/{id}` | Sim | Atualizar tópico |
| DELETE | `/topicos/{id}` | Sim | Excluir tópico |

---

## Status de Tópicos

| Valor | Descrição |
|-------|-----------|
| `NAO_RESPONDIDO` | Tópico recém criado (padrão) |
| `NAO_SOLUCIONADO` | Respondido mas não solucionado |
| `SOLUCIONADO` | Problema resolvido |
| `FECHADO` | Tópico encerrado |

---

## Autenticação JWT

A API utiliza autenticação stateless com JSON Web Tokens (JWT):

1. O cliente envia `login` e `senha` para `POST /login`
2. A API valida as credenciais e retorna um token JWT com validade de **2 horas**
3. O cliente inclui o token em todas as requisições subsequentes no header `Authorization: Bearer <token>`
4. A API valida o token a cada requisição via `SecurityFilter`

**Algoritmo:** HMAC256
**Emissor (issuer):** `API forumhub`
**Expiração:** 2 horas

---

## Banco de Dados

O schema é gerenciado pelo **Flyway**. As migrações são aplicadas automaticamente na inicialização:

| Migration | Descrição |
|-----------|-----------|
| `V1__create_topicos_table.sql` | Cria a tabela `topicos` |
| `V2__create_usuarios_table.sql` | Cria a tabela `usuarios` |
| `V3__insert_admin_user.sql` | Insere o usuário admin padrão |

> **Importante:** Nunca edite arquivos de migration já aplicados. Para alterações no schema, crie um novo arquivo `V4__descricao.sql`.

---

## Regras de Negócio

1. **Campos obrigatórios:** Ao criar um tópico, `titulo`, `mensagem`, `autor` e `curso` são todos obrigatórios
2. **Sem duplicatas:** A API rejeita tópicos com o mesmo `titulo` **e** `mensagem` com status `400 Bad Request`
3. **Atualização parcial:** No `PUT`, apenas os campos enviados no body são atualizados
4. **Autenticação obrigatória:** Todos os endpoints, exceto `POST /login`, requerem token JWT válido
5. **Sessão stateless:** Nenhuma sessão é mantida no servidor; o estado de autenticação vive apenas no token

---

## Erros Comuns

| Situação | HTTP Status |
|----------|-------------|
| Campos obrigatórios faltando | `400 Bad Request` |
| Tópico duplicado (título + mensagem) | `400 Bad Request` |
| Credenciais inválidas no login | `403 Forbidden` |
| Token JWT ausente ou inválido | `403 Forbidden` |
| ID não encontrado | `404 Not Found` |

---

## Desenvolvimento Local — Dicas

### Importar coleção no Insomnia/Postman

1. Faça `POST /login` e copie o token retornado
2. Em todas as demais requisições, adicione o header:
   ```
   Authorization: Bearer <token_copiado>
   ```

### Verificar logs SQL

Com `spring.jpa.show-sql=true` habilitado (padrão no `application.properties`), todos os SQLs executados são exibidos no console formatados.

### Resetar o banco

Para recriar o schema do zero (ambientes de desenvolvimento):
```sql
DROP DATABASE forumhub;
```
Na próxima execução do app, o banco e as tabelas serão recriados pelo Flyway.
