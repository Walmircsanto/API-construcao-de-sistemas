# API Sistema de Construção - Nexus

API REST para gerenciamento de usuários e suspeitos, desenvolvida com Spring Boot seguindo os princípios da Arquitetura Limpa.

## 🚀 Tecnologias

- **Java 17**
- **Spring Boot 3.4.9**
- **Spring Security** (JWT)
- **Spring Data JPA**
- **H2 Database** (desenvolvimento)
- **PostgreSQL** (produção)
- **AWS S3** (armazenamento de arquivos)
- **Maven**

## 📋 Pré-requisitos

- Java 17+
- Maven 3.6+
- PostgreSQL (para produção)

## ⚙️ Configuração

### 1. Clone o repositório
```bash
git clone <url-do-repositorio>
cd contrucao-sistemas
```

### 2. Configure as variáveis de ambiente
Crie um arquivo `.env` na raiz do projeto:
```env
# Database
DB_URL=jdbc:postgresql://localhost:5432/nexus_db
DB_USERNAME=seu_usuario
DB_PASSWORD=sua_senha

# JWT
JWT_SECRET=sua_chave_secreta_jwt

# AWS S3
AWS_ACCESS_KEY_ID=sua_access_key
AWS_SECRET_ACCESS_KEY=sua_secret_key
AWS_REGION=us-east-1
AWS_S3_BUCKET=seu-bucket-s3
```

### 3. Execute a aplicação
```bash
# Desenvolvimento (H2)
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Produção (PostgreSQL)
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

A API estará disponível em: `http://localhost:8080`

## 📚 Documentação da API

### Base URL
```
http://localhost:8080/api/nexus
```

## 🔐 Autenticação

### Login
```http
POST /auth/login
Content-Type: application/json

{
  "email": "usuario@email.com",
  "password": "senha123"
}
```

**Resposta:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresIn": 3600
}
```

### Autenticação nas requisições
Adicione o header Authorization em todas as requisições protegidas:
```http
Authorization: Bearer {accessToken}
```

## 👥 Usuários

### Criar usuário
```http
POST /user
Content-Type: application/json

{
  "name": "João Silva",
  "email": "joao@email.com",
  "password": "senha123",
  "role": "USER"
}
```

**Roles disponíveis:** `USER`, `ADMIN`

### Listar usuários
```http
GET /user
Authorization: Bearer {token}
```

### Buscar usuário por ID
```http
GET /user/{id}
Authorization: Bearer {token}
```

### Atualizar usuário
```http
PUT /user/{id}
Authorization: Bearer {token}
Content-Type: application/json

{
  "name": "João Silva Santos",
  "email": "joao.santos@email.com",
  "role": "ADMIN"
}
```

### Atualizar senha
```http
PATCH /user/{id}/password
Authorization: Bearer {token}
Content-Type: application/json

{
  "currentPassword": "senhaAtual",
  "newPassword": "novaSenha123"
}
```

### Deletar usuário
```http
DELETE /user/{id}
Authorization: Bearer {token}
```

## 🕵️ Suspeitos

### Criar suspeito
```http
POST /suspects
Authorization: Bearer {token}
Content-Type: multipart/form-data

# Form data:
# req: {
#   "name": "Carlos Silva",
#   "age": 35,
#   "cpf": "123.456.789-00",
#   "description": "Suspeito de roubo"
# }
# file: [arquivo de imagem]
```

### Listar suspeitos (paginado)
```http
GET /suspects?page=0&size=20
Authorization: Bearer {token}
```

**Parâmetros de query:**
- `page`: Número da página (padrão: 0)
- `size`: Tamanho da página (padrão: 20, máximo: 100)

### Buscar suspeito por ID
```http
GET /suspects/{id}
Authorization: Bearer {token}
```

### Atualizar suspeito
```http
PUT /suspects/{id}
Authorization: Bearer {token}
Content-Type: application/json

{
  "name": "Carlos Silva Santos",
  "age": 36,
  "cpf": "123.456.789-00",
  "description": "Suspeito de roubo e receptação"
}
```

### Deletar suspeito
```http
DELETE /suspects/{id}
Authorization: Bearer {token}
```

## 📄 Estrutura de Respostas

### Resposta de Usuário
```json
{
  "id": 1,
  "name": "João Silva",
  "email": "joao@email.com",
  "role": "USER",
  "enabled": true,
  "locked": false,
  "createdAt": "2024-01-15T10:30:00Z"
}
```

### Resposta de Suspeito
```json
{
  "id": 1,
  "name": "Carlos Silva",
  "age": 35,
  "cpf": "123.456.789-00",
  "description": "Suspeito de roubo",
  "urlImage": "https://bucket.s3.amazonaws.com/image.jpg",
  "createdAt": "2024-01-15T10:30:00Z"
}
```

### Resposta Paginada
```json
{
  "content": [...],
  "page": 0,
  "size": 20,
  "totalElements": 100,
  "totalPages": 5,
  "first": true,
  "last": false
}
```

## ❌ Códigos de Erro

| Código | Descrição |
|--------|-----------|
| 200 | Sucesso |
| 201 | Criado com sucesso |
| 204 | Sem conteúdo (operação realizada) |
| 400 | Dados inválidos |
| 401 | Não autorizado |
| 403 | Acesso negado |
| 404 | Recurso não encontrado |
| 409 | Conflito (email já existe) |
| 500 | Erro interno do servidor |

### Exemplo de resposta de erro
```json
{
  "timestamp": "2024-01-15T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Email já está em uso",
  "path": "/api/nexus/user"
}
```

## 🏗️ Arquitetura

O projeto segue os princípios da **Arquitetura Limpa**:

```
src/main/java/br/com/construcao/sistemas/
├── domain/                 # Entidades de domínio
├── application/           # Casos de uso e gateways
├── infrastructure/        # Implementações e persistência
└── controller/           # Controllers e DTOs
```

### Camadas:
- **Domain**: Regras de negócio puras
- **Application**: Casos de uso e interfaces
- **Infrastructure**: Implementações de banco, gateways
- **Controller**: Entrada HTTP e validações

## 🔧 Desenvolvimento

### Executar testes
```bash
mvn test
```

### Gerar build
```bash
mvn clean package
```

### Executar com Docker
```bash
# Build da imagem
docker build -t nexus-api .

# Executar container
docker run -p 8080:8080 nexus-api
```

## 📝 Logs

A aplicação gera logs de acesso e auditoria. Os logs incluem:
- Tentativas de login
- Operações CRUD
- Erros de autenticação
- Acessos não autorizados

## 🤝 Contribuição

1. Fork o projeto
2. Crie uma branch para sua feature (`git checkout -b feature/AmazingFeature`)
3. Commit suas mudanças (`git commit -m 'Add some AmazingFeature'`)
4. Push para a branch (`git push origin feature/AmazingFeature`)
5. Abra um Pull Request

## 📄 Licença

Este projeto está sob a licença MIT. Veja o arquivo `LICENSE` para mais detalhes.