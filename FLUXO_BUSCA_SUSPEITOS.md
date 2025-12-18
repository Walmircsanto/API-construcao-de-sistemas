# Fluxo de Busca de Suspeitos - Integração Python

Este documento descreve o fluxo completo da funcionalidade de busca de suspeitos que integra a API Java com o serviço Python de reconhecimento facial.

## 🔄 Visão Geral do Fluxo

```
[Cliente] → [API Java] → [Python API] → [Processamento] → [Callback] → [Notificação FCM]
```

## 📋 Fluxo Detalhado

### 1. Iniciação da Busca
**Endpoint:** `POST http://localhost:8080/api/nexus/find-search-suspect`

O cliente envia uma requisição para buscar suspeitos, geralmente com uma imagem para análise facial.

```http
POST /api/nexus/find-search-suspect
Authorization: Bearer {token}
Content-Type: multipart/form-data

# Form data:
# image: [arquivo de imagem para busca]
# additional_params: {...}
```

### 2. Processamento na API Java
A API Java:
- Valida a requisição e autenticação
- Processa a imagem recebida
- Envia a requisição para a API Python
- Retorna um ID de job para acompanhamento

### 3. Chamada para API Python
A API Java faz uma chamada assíncrona para o serviço Python de reconhecimento facial:

```python
# Exemplo da chamada para Python
POST http://python-api/face-recognition/search
{
  "image_data": "base64_encoded_image",
  "callback_url": "http://localhost:8080/api/nexus/webhooks/job-complete",
  "request_id": "unique_job_id"
}
```

### 4. Processamento Python
O serviço Python:
- Recebe a imagem
- Executa algoritmos de reconhecimento facial
- Compara com banco de dados de suspeitos
- Processa os resultados

### 5. Callback - Job Complete
**Endpoint:** `POST http://localhost:8080/api/nexus/webhooks/job-complete`

Quando o processamento é finalizado, o Python chama o webhook:

```json
{
  "s3_path": "s3://bucket/path/to/result.jpg",
  "idSuspect": 123,
  "requestId": "unique_job_id",
  "status": "completed",
  "matches": [
    {
      "suspectId": 456,
      "confidence": 0.95,
      "boundingBox": {...}
    }
  ]
}
```

### 6. Processamento do Callback
O `FaceProcessingWebhookController.jobComplete()` processa o resultado:

```java
@PostMapping("/job-complete")
public ResponseEntity<Void> jobComplete(@RequestBody JobCompleteRequest request) {
    jobService.completeJob(request.getS3_path(), request.getIdSuspect(), request.getRequestId());
    return ResponseEntity.ok().build();
}
```

## 🔔 Sistema de Notificações FCM

### 7. Envio de Notificações
Após processar o callback, o sistema envia notificações push:

#### Busca de Usuários com FCM Token
```java
List<User> users = userRepository.findByStatus(EnumStatus.ATIVO);
```

O sistema busca todos os usuários ativos que possuem `fcmToken` configurado.

#### Criação da Notificação
```java
NotificationRequest notification = new NotificationRequest();
notification.setTitle("Busca de Suspeito Concluída");
notification.setBody("Foram encontrados matches na busca facial");
notification.setTarget("SEARCH_RESULT");
notification.setAction("open_results");
notification.setUserIds(userIds);
```

#### Envio via FCM
```java
notificationProducer.enqueueToUsers(notification);
```

### 8. Estrutura da Notificação FCM

#### Para Busca Bem-sucedida:
```json
{
  "title": "Busca Concluída",
  "body": "Encontrados 3 suspeitos com alta similaridade",
  "image": "https://bucket.s3.amazonaws.com/result.jpg",
  "data": {
    "target": "SEARCH_RESULT",
    "action": "open_results",
    "requestId": "unique_job_id",
    "matchCount": 3
  }
}
```

#### Para Busca sem Resultados:
```json
{
  "title": "Busca Concluída",
  "body": "Nenhum suspeito encontrado na base de dados",
  "data": {
    "target": "SEARCH_RESULT",
    "action": "show_no_results",
    "requestId": "unique_job_id"
  }
}
```

#### Para Erro no Processamento:
```json
{
  "title": "Erro na Busca",
  "body": "Houve um erro ao processar a imagem enviada",
  "data": {
    "target": "ERROR",
    "action": "show_error",
    "requestId": "unique_job_id"
  }
}
```

## 🔧 Configuração FCM

### Usuário com FCM Token
Para receber notificações, o usuário deve ter o `fcmToken` configurado:

```java
@Entity
public class User {
    // ... outros campos
    
    @Column(name = "fcm_token")
    private String fcmToken;
    
    // getters e setters
}
```

### Atualização do FCM Token
```http
PATCH /api/nexus/user/{id}/fcm-token
Authorization: Bearer {token}
Content-Type: application/json

{
  "fcmToken": "firebase_token_here"
}
```

## 📱 Fluxo no Cliente Mobile

### 1. Recebimento da Notificação
```javascript
// React Native / Flutter
onNotificationReceived(notification) {
  if (notification.data.target === 'SEARCH_RESULT') {
    if (notification.data.action === 'open_results') {
      navigateToSearchResults(notification.data.requestId);
    }
  }
}
```

### 2. Busca dos Resultados
```http
GET /api/nexus/search-results/{requestId}
Authorization: Bearer {token}
```

## ⚡ Resumo do Fluxo Completo

1. **Cliente** → Envia imagem para busca
2. **API Java** → Processa e envia para Python
3. **Python** → Executa reconhecimento facial
4. **Python** → Chama webhook `/job-complete`
5. **API Java** → Processa resultado do callback
6. **API Java** → Busca usuários com FCM token ativo
7. **API Java** → Envia notificação FCM para todos os usuários
8. **Cliente Mobile** → Recebe notificação push
9. **Cliente Mobile** → Navega para resultados da busca

## 🔍 Pontos Importantes

- **Assíncrono**: Todo o processamento é assíncrono para não bloquear a API
- **Callback**: Python usa webhook para notificar conclusão
- **FCM Broadcast**: Notificação é enviada para todos os usuários ativos
- **Rastreamento**: Cada busca tem um `requestId` único para rastreamento
- **Fallback**: Sistema trata erros e notifica usuários sobre falhas

## 📹 Simulação de Câmera

### Endpoint de Simulação
**Endpoint:** `POST http://localhost:8080/api/nexus/simular-camera`

Este endpoint simula a detecção de um suspeito por uma câmera de segurança.

```http
POST /api/nexus/simular-camera
Authorization: Bearer {token}
Content-Type: multipart/form-data

# Form data:
# imageWithoutBoundingBox: [imagem original da câmera]
# imageWithBoundingBox: [imagem com bounding box do suspeito detectado]
```

### Fluxo da Simulação de Câmera

#### 1. Recebimento das Imagens
O sistema recebe duas imagens:
- **imageWithoutBoundingBox**: Imagem original capturada pela câmera
- **imageWithBoundingBox**: Imagem processada com o bounding box destacando o suspeito

#### 2. Upload para S3
```java
String imageUrl = uploadFiles.putObject(imageWithoutBoundingBox);
String s3Path = uploadFiles.putObject(imageWithBoundingBox);
```

#### 3. Criação do Incidente Mock
```java
Incident incident = incidentService.createMockIncidentWithImages(imageUrl, s3Path);
```

O sistema cria um incidente simulado com:
- **Location**: "Câmera Simulada"
- **Score**: 98.2% (confiança fixa para simulação)
- **Status**: Aberto
- **URLs das imagens**: Original e processada

#### 4. Notificação Imediata
Após criar o incidente, o sistema envia notificação para todos os usuários:

```java
pushNotificationService.sendNotificationToAll(
    "Câmera Detectou Suspeito",
    "Um suspeito foi detectado pela câmera",
    "INCIDENT",
    incident.getId().toString()
);
```

### Estrutura da Notificação de Câmera

```json
{
  "title": "Câmera Detectou Suspeito",
  "body": "Um suspeito foi detectado pela câmera",
  "image": "https://bucket.s3.amazonaws.com/processed-image.jpg",
  "data": {
    "target": "INCIDENT",
    "action": "open_incident",
    "incidentId": "123",
    "location": "Câmera Simulada"
  }
}
```

### Diferenças entre Busca e Simulação

| Aspecto | Busca de Suspeito | Simulação de Câmera |
|---------|-------------------|---------------------|
| **Origem** | Usuário envia imagem | Sistema simula câmera |
| **Processamento** | Assíncrono via Python | Imediato (mock) |
| **Callback** | Necessário | Não necessário |
| **Notificação** | Após callback | Imediata |
| **Finalidade** | Buscar suspeitos | Simular detecção |
| **Usuário Alvo** | Usuário que fez busca | Todos os usuários |

### Casos de Uso da Simulação

1. **Testes de Sistema**: Validar fluxo de notificações
2. **Demonstrações**: Mostrar funcionamento para clientes
3. **Treinamento**: Treinar operadores do sistema
4. **Desenvolvimento**: Testar interface sem câmeras reais

## 🛠️ Configurações Necessárias

### Variáveis de Ambiente
```env
# Python API
PYTHON_API_URL=http://python-service:5000
PYTHON_API_TIMEOUT=300

# FCM
FCM_SERVER_KEY=your_fcm_server_key
FCM_PROJECT_ID=your_firebase_project_id

# Callback URL
CALLBACK_BASE_URL=http://localhost:8080

# AWS S3 (para simulação)
AWS_S3_BUCKET=camera-simulation-bucket
```

### Dependências
- Firebase Admin SDK para envio de notificações
- Cliente HTTP para comunicação com Python
- Sistema de filas para processamento assíncrono
- AWS S3 SDK para upload de imagens