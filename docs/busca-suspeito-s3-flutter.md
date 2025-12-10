# Busca de Suspeitos S3 - Flutter Integration

## 📋 Visão Geral

Sistema de busca assíncrona de suspeitos por reconhecimento facial usando imagens armazenadas no S3. O fluxo permite upload de imagem, processamento assíncrono e monitoramento via polling.

## 🔄 Fluxo Completo

### 1. Envio da Imagem
- **Endpoint**: `POST /api/nexus/suspects/search-suspect-s3`
- **Input**: Arquivo de imagem (multipart/form-data)
- **Output**: `requestId` para monitoramento

### 2. Monitoramento
- **Endpoint**: `GET /api/nexus/webhooks/search-result/{requestId}`
- **Polling**: A cada 2-3 segundos
- **Status**: `PENDING`, `COMPLETED`, `FAILED`

### 3. Resultado
- Quando `COMPLETED`: retorna dados do suspeito encontrado
- Quando `FAILED`: erro no processamento

## 🛠️ Endpoints

### POST /api/nexus/suspects/search-suspect-s3

Inicia busca assíncrona de suspeito por imagem.

**Request:**
```http
POST /api/nexus/suspects/search-suspect-s3
Content-Type: multipart/form-data
Authorization: Bearer {token}

# Form data:
# image: [arquivo de imagem]
```

**Response:**
```json
{
  "requestId": "76596480-6ea6-4635-a69a-17eacfcab844"
}
```

### GET /api/nexus/webhooks/search-result/{requestId}

Consulta resultado da busca assíncrona.

**Response (PENDING):**
```json
{
  "requestId": "76596480-6ea6-4635-a69a-17eacfcab844",
  "status": "PENDING",
  "suspectData": null
}
```

**Response (COMPLETED):**
```json
{
  "requestId": "76596480-6ea6-4635-a69a-17eacfcab844",
  "status": "COMPLETED",
  "suspectData": {
    "suspectId": 1,
    "name": "João Silva",
    "birthday": "1985-03-15",
    "status": "ATIVO",
    "processedUrl": "https://bucket.s3.amazonaws.com/processed-image.jpg",
    "detectionLocation": "Câmera 05",
    "detectionDate": "2024-01-15",
    "horsDetection": "14:30:25"
  }
}
```

## 📱 Implementação Flutter

### Dependências
```yaml
dependencies:
  http: ^1.1.0
  dio: ^5.3.2
  flutter: 
    sdk: flutter
```

### Modelos
```dart
// models/search_models.dart
class SearchInitResponse {
  final String requestId;

  SearchInitResponse({required this.requestId});

  factory SearchInitResponse.fromJson(Map<String, dynamic> json) {
    return SearchInitResponse(requestId: json['requestId']);
  }
}

class SearchResult {
  final String requestId;
  final SearchStatus status;
  final SuspectData? suspectData;

  SearchResult({
    required this.requestId,
    required this.status,
    this.suspectData,
  });

  factory SearchResult.fromJson(Map<String, dynamic> json) {
    return SearchResult(
      requestId: json['requestId'],
      status: SearchStatus.values.firstWhere(
        (e) => e.name.toUpperCase() == json['status'],
      ),
      suspectData: json['suspectData'] != null 
        ? SuspectData.fromJson(json['suspectData']) 
        : null,
    );
  }
}

enum SearchStatus { PENDING, COMPLETED, FAILED }

class SuspectData {
  final int suspectId;
  final String name;
  final String birthday;
  final String status;
  final String processedUrl;
  final String detectionLocation;
  final String detectionDate;
  final String horsDetection;

  SuspectData({
    required this.suspectId,
    required this.name,
    required this.birthday,
    required this.status,
    required this.processedUrl,
    required this.detectionLocation,
    required this.detectionDate,
    required this.horsDetection,
  });

  factory SuspectData.fromJson(Map<String, dynamic> json) {
    return SuspectData(
      suspectId: json['suspectId'],
      name: json['name'],
      birthday: json['birthday'],
      status: json['status'],
      processedUrl: json['processedUrl'],
      detectionLocation: json['detectionLocation'],
      detectionDate: json['detectionDate'],
      horsDetection: json['horsDetection'],
    );
  }
}
```

### Service
```dart
// services/suspect_search_service.dart
import 'dart:async';
import 'dart:convert';
import 'dart:io';
import 'package:dio/dio.dart';

class SuspectSearchService {
  static const String baseUrl = 'http://localhost:8080/api/nexus';
  final Dio _dio;
  final String token;

  SuspectSearchService(this.token) : _dio = Dio() {
    _dio.options.headers['Authorization'] = 'Bearer $token';
  }

  Future<String> startSearch(File imageFile) async {
    final formData = FormData.fromMap({
      'image': await MultipartFile.fromFile(imageFile.path),
    });

    final response = await _dio.post(
      '$baseUrl/suspects/search-suspect-s3',
      data: formData,
    );

    return response.data['requestId'] ?? response.data;
  }

  Future<SearchResult> getSearchResult(String requestId) async {
    final response = await _dio.get(
      '$baseUrl/webhooks/search-result/$requestId',
    );

    return SearchResult.fromJson(response.data);
  }

  Stream<SearchResult> monitorSearch(String requestId) async* {
    const maxDuration = Duration(minutes: 5);
    const pollInterval = Duration(seconds: 2);
    final startTime = DateTime.now();

    while (DateTime.now().difference(startTime) < maxDuration) {
      try {
        final result = await getSearchResult(requestId);
        yield result;
        
        if (result.status != SearchStatus.PENDING) break;
        
        await Future.delayed(pollInterval);
      } catch (e) {
        yield SearchResult(
          requestId: requestId, 
          status: SearchStatus.FAILED
        );
        break;
      }
    }
  }
}
```

### Widget Principal
```dart
// widgets/suspect_search_widget.dart
import 'dart:io';
import 'package:flutter/material.dart';
import 'package:image_picker/image_picker.dart';

class SuspectSearchWidget extends StatefulWidget {
  final String token;

  const SuspectSearchWidget({Key? key, required this.token}) : super(key: key);

  @override
  State<SuspectSearchWidget> createState() => _SuspectSearchWidgetState();
}

class _SuspectSearchWidgetState extends State<SuspectSearchWidget> {
  late SuspectSearchService _service;
  File? _selectedImage;
  String? _currentRequestId;
  SearchResult? _currentResult;
  StreamSubscription<SearchResult>? _subscription;
  bool _isSearching = false;

  @override
  void initState() {
    super.initState();
    _service = SuspectSearchService(widget.token);
  }

  @override
  void dispose() {
    _subscription?.cancel();
    super.dispose();
  }

  Future<void> _pickImage() async {
    final picker = ImagePicker();
    final pickedFile = await picker.pickImage(source: ImageSource.gallery);
    
    if (pickedFile != null) {
      setState(() {
        _selectedImage = File(pickedFile.path);
      });
    }
  }

  Future<void> _startSearch() async {
    if (_selectedImage == null) return;

    setState(() {
      _isSearching = true;
      _currentResult = null;
    });

    try {
      final requestId = await _service.startSearch(_selectedImage!);
      _currentRequestId = requestId;
      
      _subscription = _service.monitorSearch(requestId).listen((result) {
        setState(() {
          _currentResult = result;
          if (result.status != SearchStatus.PENDING) {
            _isSearching = false;
          }
        });
      });
    } catch (e) {
      setState(() {
        _isSearching = false;
      });
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Erro ao iniciar busca: $e')),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text('Busca de Suspeitos')),
      body: Padding(
        padding: EdgeInsets.all(16),
        child: Column(
          children: [
            _buildImageSection(),
            SizedBox(height: 20),
            _buildSearchButton(),
            SizedBox(height: 20),
            Expanded(child: _buildResultSection()),
          ],
        ),
      ),
    );
  }

  Widget _buildImageSection() {
    return Card(
      child: Padding(
        padding: EdgeInsets.all(16),
        child: Column(
          children: [
            if (_selectedImage != null)
              Image.file(_selectedImage!, height: 200, fit: BoxFit.cover)
            else
              Container(
                height: 200,
                color: Colors.grey[300],
                child: Icon(Icons.image, size: 64, color: Colors.grey[600]),
              ),
            SizedBox(height: 16),
            ElevatedButton.icon(
              onPressed: _pickImage,
              icon: Icon(Icons.photo_library),
              label: Text('Selecionar Imagem'),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildSearchButton() {
    return SizedBox(
      width: double.infinity,
      child: ElevatedButton(
        onPressed: _selectedImage != null && !_isSearching ? _startSearch : null,
        child: _isSearching 
          ? Row(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                SizedBox(
                  width: 20,
                  height: 20,
                  child: CircularProgressIndicator(strokeWidth: 2),
                ),
                SizedBox(width: 8),
                Text('Buscando...'),
              ],
            )
          : Text('Iniciar Busca'),
      ),
    );
  }

  Widget _buildResultSection() {
    if (_currentResult == null && !_isSearching) {
      return Center(child: Text('Selecione uma imagem e inicie a busca'));
    }

    if (_isSearching && _currentResult == null) {
      return Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            CircularProgressIndicator(),
            SizedBox(height: 16),
            Text('Iniciando busca...'),
            if (_currentRequestId != null)
              Text('ID: $_currentRequestId', style: TextStyle(fontSize: 12)),
          ],
        ),
      );
    }

    switch (_currentResult!.status) {
      case SearchStatus.PENDING:
        return Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              CircularProgressIndicator(),
              SizedBox(height: 16),
              Text('Processando busca...'),
              Text('ID: ${_currentResult!.requestId}', style: TextStyle(fontSize: 12)),
            ],
          ),
        );

      case SearchStatus.COMPLETED:
        return _buildSuspectResult(_currentResult!.suspectData!);

      case SearchStatus.FAILED:
        return Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Icon(Icons.error, color: Colors.red, size: 64),
              SizedBox(height: 16),
              Text('Falha na busca'),
              ElevatedButton(
                onPressed: () => setState(() {
                  _currentResult = null;
                  _isSearching = false;
                }),
                child: Text('Tentar Novamente'),
              ),
            ],
          ),
        );
    }
  }

  Widget _buildSuspectResult(SuspectData suspect) {
    return Card(
      child: Padding(
        padding: EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text('Suspeito Encontrado!', 
                 style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold)),
            SizedBox(height: 16),
            if (suspect.processedUrl.isNotEmpty)
              ClipRRect(
                borderRadius: BorderRadius.circular(8),
                child: Image.network(suspect.processedUrl, height: 200),
              ),
            SizedBox(height: 16),
            _buildInfoRow('Nome', suspect.name),
            _buildInfoRow('Data Nascimento', suspect.birthday),
            _buildInfoRow('Status', suspect.status),
            _buildInfoRow('Local Detecção', suspect.detectionLocation),
            _buildInfoRow('Data Detecção', suspect.detectionDate),
            _buildInfoRow('Hora Detecção', suspect.horsDetection),
          ],
        ),
      ),
    );
  }

  Widget _buildInfoRow(String label, String value) {
    return Padding(
      padding: EdgeInsets.symmetric(vertical: 4),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          SizedBox(
            width: 120,
            child: Text('$label:', style: TextStyle(fontWeight: FontWeight.bold)),
          ),
          Expanded(child: Text(value)),
        ],
      ),
    );
  }
}
```

### Uso
```dart
// main.dart ou tela principal
class SearchScreen extends StatelessWidget {
  final String token;

  const SearchScreen({Key? key, required this.token}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return SuspectSearchWidget(token: token);
  }
}
```

## ⚙️ Configurações

### Timeouts e Intervalos
- **Polling**: 2 segundos
- **Timeout total**: 5 minutos
- **Retry**: 3 tentativas em caso de erro

### Tratamento de Erros
- Conexão perdida: retry automático
- Timeout: notificação ao usuário
- Erro de upload: mensagem específica

## 🔧 Considerações

1. **Performance**: Cancele streams ao sair da tela
2. **UX**: Sempre mostre progresso visual
3. **Conectividade**: Trate cenários offline
4. **Memória**: Otimize carregamento de imagens
5. **Bateria**: Pause polling em background