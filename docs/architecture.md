# Architecture Overview

## System Architecture

Limitless Companion follows a hybrid architecture combining on-device AI processing with self-hosted server intelligence.

### High-Level Components

```
┌─────────────────────────────────────────────────────────────────────┐
│                        NATIVE ANDROID APP (Kotlin)                   │
│                                                                     │
│  ┌─────────────────┐      ┌──────────────────┐                     │
│  │ Foreground      │      │ Audio Pipeline   │                     │
│  │ Service         │─────>│ - AudioRecord    │                     │
│  │ (Always-On)     │      │ - Bluetooth SCO  │                     │
│  └────────┬────────┘      └────────┬─────────┘                     │
│           │                        │                                 │
│           │                        v                                 │
│  ┌────────v──────────────────────────────────┐                     │
│  │  whisper.cpp via JNI                       │                     │
│  │  - ggml quantized model (base.en)          │                     │
│  │  - 30s audio chunks                        │                     │
│  └────────┬───────────────────────────────────┘                     │
│           │                                                           │
│           │ (Transcribed text)                                       │
│           v                                                           │
│  ┌────────────────────────────────────────────┐                     │
│  │  Local DB (Room/SQLite)                    │                     │
│  │  - Cache transcripts before upload          │                     │
│  │  - Queue failed requests                    │                     │
│  └────────┬───────────────────────────────────┘                     │
│           │                                                           │
│           │ HTTPS (API Key Auth)                                     │
│           └─────────────────┬───────────────────────────────────────┘
│                             │
│                             v
│  ┌───────────────────────────────────────────────────────────────────┐
│  │                    SELF-HOSTED SERVER (Docker)                     │
│  │                                                                   │
│  ┌─────────────────────────────────────────────────────────────────┐ │
│  │  FastAPI (Python 3.11+)                                        │ │
│  │  - /api/transcripts (POST) - receive transcript chunks          │ │
│  │  - /api/search (GET) - RAG-based semantic search               │ │
│  │  - /api/actions (GET) - poll detected actions                   │ │
│  └────────┬────────┬────────────┬──────────────────────────────────┘ │
│           │        │            │                                     │
│           │        │            v                                     │
│           │        │   ┌────────────────────┐                        │
│           │        │   │ Action Detection   │                        │
│           │        │   │ (Ollama Llama 3.1) │                        │
│           │        │   └────────────────────┘                        │
│           │        │                                                   │
│           │        v                                                   │
│           │   ┌─────────────────────────────┐                        │
│           │   │ Speaker Diarization         │                        │
│           │   │ (Pyannote/Simple Heuristic) │                        │
│           │   └─────────────────────────────┘                        │
│           │                                                           │
│           v                                                           │
│  ┌────────────────────────────────────────────┐                      │
│  │  PostgreSQL + pgvector                     │                      │
│  │  - transcripts, actions, sessions, users   │                      │
│  │  - embeddings (384-dim sentence-transformers) │                  │
│  └────────────────────────────────────────────┘                      │
│                                                                       │
└───────────────────────────────────────────────────────────────────────┘
```

## Component Details

### Mobile Application (Android)

**Technology Stack**:
- **Framework**: Native Android (Kotlin)
- **Architecture**: Clean Architecture with MVVM
- **UI**: Jetpack Compose
- **Database**: Room + SQLite
- **Background Processing**: WorkManager + Foreground Service

**Key Components**:

#### Audio Pipeline
- **AudioRecord**: Captures audio from device microphone or Bluetooth SCO
- **whisper.cpp**: On-device speech-to-text using quantized GGML models
- **Audio Chunking**: Processes 30-second segments for optimal performance

#### Data Layer
- **Room Database**: Local caching and offline queue management
- **Repository Pattern**: Abstraction over local and remote data sources
- **WorkManager**: Background sync and retry logic

#### UI Layer
- **Jetpack Compose**: Declarative UI with Material Design 3
- **Navigation**: Single-activity architecture with Compose Navigation
- **State Management**: ViewModels with StateFlow

### Server Backend (Python/FastAPI)

**Technology Stack**:
- **Framework**: FastAPI (async Python web framework)
- **Database**: PostgreSQL with pgvector extension
- **LLM**: Ollama for local LLM inference
- **Embeddings**: sentence-transformers for semantic search
- **Containerization**: Docker Compose for deployment

**Key Components**:

#### API Layer
- **REST Endpoints**: JSON API with OpenAPI documentation
- **Authentication**: API key-based authentication per device
- **Validation**: Pydantic models for request/response validation

#### Service Layer
- **Action Detection**: LLM-powered analysis of transcripts for actionable items
- **Speaker Diarization**: Pyannote.audio for speaker identification
- **Embedding Service**: Vector generation for semantic search
- **Search Service**: RAG-based retrieval with pgvector

#### Data Layer
- **SQLAlchemy**: ORM with async support
- **Alembic**: Database migrations
- **pgvector**: Vector similarity search for embeddings

## Data Flow

### Recording Session

1. **Audio Capture**: Android app captures audio via Bluetooth headset
2. **Local Transcription**: whisper.cpp processes 30-second chunks on-device
3. **Local Storage**: Transcripts cached in Room database
4. **Server Sync**: HTTPS upload to FastAPI server with API key auth
5. **Processing Pipeline**:
   - Speaker diarization (if enabled)
   - Embedding generation for search
   - Action detection via LLM
6. **Notification**: Push notification sent to device for detected actions

### Search Query

1. **Query Input**: User enters natural language search
2. **Embedding Generation**: Query converted to vector
3. **Vector Search**: pgvector finds similar transcript chunks
4. **Re-ranking**: Results ranked by relevance and recency
5. **Context Assembly**: Surrounding context added for LLM
6. **Response Generation**: Formatted results returned to app

## Security Architecture

### Authentication
- **API Keys**: Per-device authentication with UUID-based keys
- **HTTPS Only**: All communication encrypted in transit
- **Key Rotation**: Administrative commands for key management

### Data Protection
- **Encryption at Rest**: Database-level encryption for sensitive data
- **Access Control**: Device-scoped data isolation
- **Audit Logging**: Request logging for security monitoring

### Privacy Design
- **Self-Hosted**: No third-party data sharing
- **Local Processing**: Speech-to-text happens on-device
- **User Control**: Data retention and deletion controls

## Deployment Architecture

### Docker Compose Stack
- **FastAPI Server**: Main application container
- **PostgreSQL**: Database with pgvector extension
- **Ollama**: Local LLM inference service
- **Nginx**: Reverse proxy (optional for production)

### Scaling Considerations
- **Horizontal Scaling**: Multiple server instances behind load balancer
- **Database Scaling**: Read replicas for search-heavy workloads
- **Storage**: External object storage for audio files (future)

## Performance Characteristics

### Latency Targets
- **Transcription**: <3 seconds per 30-second chunk (on-device)
- **Action Detection**: <10 seconds from speech to notification
- **Search**: <500ms query response time
- **Sync**: <5 seconds for transcript upload

### Resource Requirements
- **Mobile**: 6GB RAM, Android 12+, Bluetooth 5.0+
- **Server**: 4 CPU cores, 16GB RAM, 100GB storage
- **Network**: Stable connection with <200ms latency

## Development Architecture

### Code Organization
- **Mobile**: Clean Architecture (Domain, Data, Presentation layers)
- **Server**: Modular structure with clear separation of concerns
- **Shared**: Common models and API contracts

### Testing Strategy
- **Unit Tests**: Business logic and utilities
- **Integration Tests**: API endpoints and service interactions
- **E2E Tests**: Critical user workflows

### CI/CD Pipeline
- **GitHub Actions**: Automated testing and building
- **Docker Images**: Pre-built containers for deployment
- **Release Management**: APK generation and GitHub releases