# Fluxo de Busca Assíncrona de Suspeitos

## Visão Geral

Este documento descreve o fluxo completo de busca assíncrona de suspeitos por imagem, envolvendo integração entre Java e Python.

## Fluxo Completo

### 1. Iniciação da Busca (Frontend → Java)

```http
POST /api/nexus/suspects/search-suspect-s3
Content-Type: application/json

{
  image: Multipart form data
}
```

**Resposta imediata:**
```json
{
  "message": "Search request submitted",
  "job_id": "abc-123-xyz",
  "status": "PENDING",
  "s3_path": "s3://bucket/path/to/image.jpg",
  "source": "java-api"
}
```

### 2. Processamento (Java → Python)

O Java envia a requisição para o Python e:
- Cria um registro `SearchResult` com status `PENDING`
- Retorna o `job_id` para correlação

### 3. Callback do Python (Python → Java)

Quando o processamento termina, o Python chama:

```http
POST /api/nexus/suspects/complete-search
Content-Type: application/json

{
  "requestId": "abc-123-xyz",
  "idSuspect": 1,
  "s3_path": "s3://api-java-qrcode/ronaldo.jpg_xxx"
}
```

### 4. Polling do Resultado (Frontend → Java)

O frontend pode fazer polling para verificar o status:

```http
GET /api/nexus/suspects/search-result?requestId=abc-123-xyz
```

**Resposta quando ainda processando:**
```json
{
  "requestId": "abc-123-xyz",
  "status": "PENDING",
  "suspectData": null
}
```

**Resposta quando concluído:**
```json
{
  "requestId": "abc-123-xyz",
  "status": "COMPLETED",
  "suspectData": {
    "suspectId": 1,
    "name": "Carlos Silva",
    "birthday": "1985-03-15",
    "status": "ACTIVE",
    "processedUrl": "s3://api-java-qrcode/ronaldo.jpg_xxx",
    "detectionLocation": "Câmera 05",
    "detectionDate": "2024-01-15",
    "horsDetection": "14:30:25"
  }
}
```

## Estrutura de Dados

### Tabela `search_results`

```sql
CREATE TABLE search_results (
    id BIGSERIAL PRIMARY KEY,
    request_id VARCHAR(255) UNIQUE NOT NULL,
    suspect_id BIGINT,
    s3_path VARCHAR(500),
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP
);
```

### Estados do Status

- `PENDING`: Busca em processamento
- `COMPLETED`: Busca finalizada com sucesso
- `FAILED`: Busca falhou (para implementação futura)

## Endpoints Implementados

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/api/nexus/suspects/search-suspect-s3` | Inicia busca assíncrona |
| POST | `/api/nexus/suspects/complete-search` | Webhook para callback do Python |
| GET | `/api/nexus/suspects/search-result` | Consulta resultado por requestId |

## Configuração de Segurança

O endpoint `/complete-search` está configurado como público (`permitAll()`) para permitir que o Python faça o callback sem autenticação.

## Exemplo de Uso no Frontend

```javascript
// 1. Iniciar busca
const response = await fetch('/api/nexus/suspects/search-suspect-s3', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    s3Path: 's3://bucket/image.jpg',
    topK: 5
  })
});

const { job_id } = await response.json();

// 2. Polling do resultado
const pollResult = async () => {
  const result = await fetch(`/api/nexus/suspects/search-result?requestId=${job_id}`);
  const data = await result.json();
  
  if (data.status === 'COMPLETED') {
    console.log('Suspeito encontrado:', data.suspectData);
    return data;
  } else if (data.status === 'PENDING') {
    // Aguardar e tentar novamente
    setTimeout(pollResult, 2000);
  }
};

pollResult();
```