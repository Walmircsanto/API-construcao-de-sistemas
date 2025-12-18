# Nexus -- Ambiente de Desenvolvimento com Docker

### CASO NAO ESTEJA NA EQUIPE DE DEVS IGNORE OS PONTOS 4, 5

### O DOCKER ATUALMENTE AINDA PRECISA DO ARQUIVO .ENV NA RAIZ DO PROJETO DEVIDO A PREGUICA DO DEV EM CONFIGURAR O RESTANTE, CASO QUERIA RODAR A API PEÇA O ARQUIVO .ENV PARA ALGUM DEV

--------------------------------------------------------------------

Este guia explica como configurar e rodar o ambiente de desenvolvimento
da API Nexus utilizando **Docker**, incluindo:

-   API em Spring Boot\
-   Banco de dados **PostgreSQL**\
  -   Interface de administração **pgAdmin**\
-   Hot Reload com Spring DevTools\
-   Variáveis de ambiente opcionais (sem necessidade de distribuir
    `.env`)

## 1. Pré-requisitos

### Instalar o Docker Desktop (Windows)

1.  Baixe: https://www.docker.com/products/docker-desktop/\
2.  Instale normalmente\
3.  Ative o WSL2 quando solicitado\
4.  Reinicie a máquina\
5.  Teste a instalação:

```
 docker --version
 docker compose version
```


## 2. Estrutura do Projeto

O ambiente de desenvolvimento utiliza:

-   `docker-compose.dev.yml`\
-   API\
-   PostgreSQL\
-   pgAdmin

## 3. Primeira Execução

Dentro da pasta do projeto, execute:

    docker compose -f docker-compose.dev.yml up -d --build

Ver logs da API:

    docker compose -f docker-compose.dev.yml logs -f api

Parar tudo:

    docker compose -f docker-compose.dev.yml down

## 4. Variáveis de Ambiente (.env)

O projeto funciona **sem `.env`**, graças aos valores padrão no
docker-compose.

Exemplo opcional:

    POSTGRES_DB=nexus
    POSTGRES_USER=postgres
    POSTGRES_PASSWORD=admin
    API_PORT=8080

## 5. Hot Reload (DevTools)

No IntelliJ IDEA:

-   Ativar **Build project automatically**\
-   Ativar no Registry: `compiler.automake.allow.when.app.running`

Ao salvar um arquivo `.java`, a API reinicia dentro do container.

## 6. Acessando o PostgreSQL via pgAdmin

Abrir:

    http://localhost:5050

Login:

-   Email: `admin@nexus.com`
-   Senha: `postgres123`

Criar conexão:

-   Host: `postgres`
-   Port: `5432`
-   User: `postgres`
-   Password: `admin`

## 7. Acessando a API

    http://localhost:8080

## 8. Comandos Úteis

Reconstruir API:

    docker compose -f docker-compose.dev.yml up -d --build api

Remover tudo:

    docker compose -f docker-compose.dev.yml down -v

## 9. Problemas Comuns

### Porta 5432 em uso

    netstat -ano | findstr :5432

### API reiniciando sozinha

-   Verifique DevTools\
-   Verifique volumes\
-   Veja o log completo


