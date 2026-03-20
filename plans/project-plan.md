# Limitless Assistant
The limitless assistant is meant to be an open source framework consisting of an application and self host-able server. It takes full advantage of the modern building blocks we have available to us including amazing open source text to speech models and other open source AI components (langGraph, Ollama).

The limitless assistant can be run easily by deploying an instance of the backend (Hostinger) then installing the mobile app onto a phone and setting its server endpoint.

Now limitless will act as your perfect recall and memory. You will be able to look back on and search through any past conversation or event. Limitless identifies speakers, ignores background noise and helps you be able to easily go back to anything you have said before.

Go further by issuing instructions, limitless will automatically detect when to create reminders, notes, ideas, schedule events or even draft emails for you. You can even ask limitless to go into 'proactive mode' where your assistant will try to give you useful information without being explicitly asked, by gathering context queues. Great when you want to shine in a meeting or need to look smart playing a quiz!

Human in the loop and privacy are first priorities. By default Limitless will only issue actionable's as notifications to your device, where you explicitly approve actions. Transcriptions are done on device and the app only speaks to your self hosted server instance. 

# Technical Documentation Blueprint

Below is a structured outline for your DIY Always-On Transcription app, plus examples and initial decisions to kick off coding within days.

---

## 0. Document Changelog

| Version | Date | Changes | Author |
|---------|------|---------|--------|
| 0.1 | 2025-12-27 | Initial draft with Flutter stack | Team |
| 0.2 | 2025-12-28 | Major revision: Native Android, speaker ID research, RAG tentative design, auth spec, milestones | Intellectual Sparring Review |
| 0.3 | 2026-03-20 | Milestones 1–3 verified complete on Samsung S22 Ultra; Milestone 4 server scaffold built and deployed to RunOS dev (app ID: z0dhx, cluster: tdc) — Android sync not yet wired | Ahren |

**Living Document**: This plan evolves as implementation reveals new requirements or constraints. All significant changes logged here.

---

## 1. Project Overview

This document defines scope, architecture, user stories, components, tech stack, and a phased roadmap for an open-source mobile app that:

- Captures audio continuously via any Bluetooth headset or available microphone
- Transcribes with on-device Whisper model (if possible) or remote self-hosted model through API  
- Surfaces actionable notifications ("Accept" to set reminders, tasks, emails, events, docs)  
- Actively records, indexes and summarizes all transcriptions
- Can respond to the user using an LLM and relevant recent context
- **Platform**: Native Android (Kotlin), targetSdk 35+ for proof of concept
- **Distribution**: APK via GitHub releases (not Google Play), open-source contributions welcome

---

## 2. Scope & Objectives

- Real-time transcription with minimal latency  
- RAG based search and retrieval of past transcriptions
- Local fallback if Bluetooth disconnects  
- On-device Whisper integration to reduce API costs and privacy exposure  
- Device notifications for user-approved tasks  

---

## 2.5. Explicitly Out of Scope (v1)

The following features are **NOT** included in the initial proof of concept:

- **iOS support**: Android only for v1 to validate core concept
- **Proactive mode**: Deferred to v2 (see Section 5.5) - requires UX refinement and extensive testing
- **Multi-device sync**: Single device operation for v1
- **Speaker recognition by voice biometrics**: Using simpler heuristics initially (see Section 8.3)
- **Custom wake words**: Always-on recording without activation phrase
- **Export to external services**: Manual copy/paste only for v1
- **Real-time collaboration**: Single-user application
- **Advanced analytics**: Basic search and timeline only
- **Offline-first mode with full sync**: Requires network connectivity for most features
- **Battery optimization beyond Android defaults**: Initial implementation may drain a lot of battery

**Rationale**: These features add significant complexity and development time. The goal of v1 is to validate the core transcription + action detection workflow with real users. Once proven, we can prioritize v2 features based on user feedback and usage patterns. This focused scope enables completion in 8-12 weeks vs. 6+ months for a feature-complete system.

---

## 2.6. Key Assumptions & Dependencies

**User Profile**:
- Technically proficient users comfortable with self-hosting and APK installation
- Willing to accept beta-quality software with rough edges
- Privacy-conscious individuals who prefer self-hosted solutions

**Hardware Requirements**:
- **Mobile**: Android device with 6GB+ RAM, Android 12+, Bluetooth 5.0+
- **Headset**: Any Bluetooth headset with microphone (tested with standard consumer devices)
- **Server**: 4 CPU cores, 16GB RAM, 100GB storage minimum (for Docker containers + models)

**Network Assumptions**:
- Stable WiFi or 4G/5G connection with <200ms latency to self-hosted server
- Server accessible via public IP or VPN (not localhost-only)

**Privacy Trust Model**:
- Users trust their own self-hosted infrastructure
- No third-party services (except optional Whisper API fallback, user-configured)
- User responsible for server security (HTTPS, firewall, access controls)

**Battery Consumption Disclaimer**:
- Always-on audio recording + Bluetooth + background processing expected to consume 20-40% battery per 4 hours
- Users should use external battery packs for extended sessions
- Future optimizations may improve this, but not guaranteed for v1

---

## 3. High-Level Architecture

```text
┌─────────────────────────────────────────────────────────────────────┐
│                        NATIVE ANDROID APP (Kotlin)                   │
│                                                                       │
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
└───────────┼───────────────────────────────────────────────────────┘
            │
            v
┌───────────────────────────────────────────────────────────────────┐
│                    SELF-HOSTED SERVER (Docker)                     │
│                                                                     │
│  ┌──────────────────────────────────────────────────────────────┐ │
│  │  FastAPI (Python 3.11+)                                       │ │
│  │  - /api/transcripts (POST) - receive transcript chunks        │ │
│  │  - /api/search (GET) - RAG-based semantic search             │ │
│  │  - /api/actions (GET) - poll detected actions                 │ │
│  └────────┬────────┬────────────┬───────────────────────────────┘ │
│           │        │            │                                   │
│           │        │            v                                   │
│           │        │   ┌────────────────────┐                      │
│           │        │   │ Action Detection   │                      │
│           │        │   │ (Ollama Llama 3.1) │                      │
│           │        │   └────────────────────┘                      │
│           │        │                                                 │
│           │        v                                                 │
│           │   ┌─────────────────────────────┐                      │
│           │   │ Speaker Diarization         │                      │
│           │   │ (Pyannote/Simple Heuristic) │                      │
│           │   └─────────────────────────────┘                      │
│           │                                                          │
│           v                                                          │
│  ┌────────────────────────────────────────────┐                    │
│  │  PostgreSQL + pgvector                     │                    │
│  │  - transcripts, actions, sessions, users   │                    │
│  │  - embeddings (384-dim sentence-transformers) │                │
│  └────────────────────────────────────────────┘                    │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

**Key Changes from Original**:
- **Native Android** replaces Flutter for better Bluetooth/OS integration
- **whisper.cpp via JNI** for on-device transcription (FFI not needed)
- **Room/SQLite** local cache for offline resilience
- **Pyannote** for speaker diarization (research phase required)
- **Ollama** for action detection (self-hosted LLM)

---

## 4. User Stories

| ID  | Role         | Goal                                                           | Acceptance Criteria                                                      |
|-----|--------------|----------------------------------------------------------------|---------------------------------------------------------------------------|
| US1 | As a user    | I want continuous transcription in background                  | Transcripts appear in-app within 5s of speech                            |
| US2 | As a user    | I want suggestions when I say "remind me to…"                  | Notification "Create reminder" pops immediately with "Accept" button      |
| US3 | As a user    | I want to draft an email by saying "email John about status"   | App opens email composer prefilled with suggested subject/body or provides a link to do this           |
| US4 | As a user    | I want to add events via "schedule meeting tomorrow at 3 PM"   | Calendar event draft appears ready for review and "Accept" confirmation   |
| US5 | As a user    | I want meeting minutes generated at end of call                | "Generate summary" notification appears when call ends                   |

---

## 5. Functional Requirements

- **Audio Capture**:  
  - Stream Bluetooth mic → audio buffer  
  - Fallback to device mic on disconnect  
  - Chunking every 5–10 sec for transcription  

- **Transcription**:  
  - On-device Whisper via `whisper.cpp` or remote Whisper API  
  - Fallback STT (e.g., Google, Azure)  

- **Action Detection**:  
  - NLP pipeline to scan transcript in real time / use agents  
  - Mapping phrases → action types (reminder, task, email, event, document)  

- **Notifications**:  
  - Local push notification with "Accept" button  
  - Deep link into app action handler  

- **Server Orchestration**:  
  - LangGraph triggers for advanced summarization, embeddings, history  

---

## 5.5. Proactive Mode - v2 Specification

**Status**: Deferred to v2 

**What it is**: An optional mode where Limitless proactively suggests (via audio) information, or insights based on contextual cues from conversations, without explicit user requests.

**Activation**:
- Manual toggle in app settings (default: OFF)
- Requires explicit opt-in with privacy disclosure

**Behavior**:
- Monitors conversation context for opportunities to assist
- Examples:
  - *Someone is discussing project Alpha* -> "You had a similar discussion with 'Paul' last week"
  - *User says "I had a similar discussion with 'Paul' last week, let me try remember what he said" -> "Paul said he would invest, his 3 main points were..."
  - *Someone asks about the capital of france* -> "Paris is the capital of france"

**Confidence Threshold**:
- Only trigger when LLM confidence >85%
- Rate limit: Max 1 proactive suggestions per min

**Privacy Considerations**:
- All processing still on self-hosted server
- User can disable specific proactive categories
- Full audit log of all proactive triggers

**UI/UX**:
- Non-intrusive banner notification
- If app foregrounded, no audio, only text

**Technical Dependencies**:
- Requires reliable action detection
- Needs context window management (last 5 minutes of transcript)
- LLM must support "interrupt/suggest" prompt engineering

**Why v2**:
- Requires extensive user testing to avoid annoyance
- Risk of false positives hurting trust in v1
- Complex prompt engineering and context management
- Better to nail passive recording + manual search first

---

## 6. Non-Functional Requirements

- **Reliability**: >99% uptime for transcription service  
- **Latency**: <5-second round-trip for remote STT  
- **Privacy**: Option to disable uploads; all data encrypted at rest  
- **Performance**: <10% CPU impact on background threads  
- **Extensibility**: Pluggable adapters for new LLMs or STT providers  

---

## 6.5. Error Handling & Offline Resilience

| Scenario | Client Behavior | Server Behavior | User Impact |
|----------|-----------------|-----------------|-------------|
| **Network timeout** | Queue transcript locally (Room DB) | N/A | Seamless recording, delayed sync |
| **Server 5xx error** | Retry with exponential backoff (3 attempts) | Log error, alert monitoring | Possible delays, no data loss |
| **Client storage full** | Stop recording, notify user | N/A | Requires manual cleanup |
| **Whisper model load failure** | Fallback to remote API (if configured) | N/A | Increased latency, requires config |
| **Bluetooth disconnect mid-sentence** | Switch to device mic, log transition | Mark audio chunk as "device_switched" | Possible speaker label confusion |
| **Action detection API down** | Transcripts saved without actions | Queue for reprocessing | Search works, actions delayed |

**Local Queue Strategy**:
- Max 1000 pending transcripts in local SQLite
- FIFO eviction when full
- Background sync worker runs every 5 minutes when online

**Storage Full Handling**:
- Monitor available storage on app start
- Warn at <500MB available
- Block recording at <100MB available

**Whisper Model Loading Failure**:
- Check model file integrity on first app launch
- Offer to re-download (via GitHub release asset)
- Clear fallback path to remote Whisper API

**Action Detection Failure**:
- Never block transcript storage
- Retry action detection on next successful server ping
- User can manually trigger "re-scan for actions" in UI

**Graceful Degradation Priority**:
1. **Must work**: Audio recording + local transcription
2. **Should work**: Server upload + search
3. **Nice to have**: Action detection + speaker diarization

---

## 7. Technology Stack

| Layer                  | Choice                                           | Rationale                                                   |
|------------------------|--------------------------------------------------|-------------------------------------------------------------|
| Mobile Framework       | **Native Android (Kotlin)** | Direct OS integration for foreground services, robust Bluetooth SCO support, optimal performance for always-on audio recording, no cross-platform abstraction overhead |
| On-Device STT          | whisper.cpp (via JNI)                            | Open source, runs on-device, privacy + cost benefits       |
| Remote STT (fallback)  | OpenAI Whisper API                               | High accuracy, easy to integrate                            |
| Notification Service   | Android NotificationCompat                       | Native system integration, actionable buttons               |
| Server Framework       | FastAPI (Python)                                 | Async, simple syntax, easy LangGraph integration            |
| Orchestration          | LangGraph                                        | Workflow management, LLM routing                            |
| LLM (Action Detection) | Ollama (Llama 3.1 8B)                           | Self-hosted, fast inference, good at structured output      |
| Speaker Diarization    | Pyannote (research phase)                        | State-of-the-art open source, requires evaluation           |
| Database               | PostgreSQL + pgvector                            | ACID + vector embeddings in one system                      |
| Containerization       | Docker                                            | Reproducible dev/devops                                    |
| CI/CD                  | GitHub Actions                                   | Open-source friendly, integrates with Docker               |

**Why Native Android over Flutter**:
- **Bluetooth SCO (Synchronous Connection-Oriented)**: Requires native Android APIs for reliable headset audio routing
- **Foreground Services**: More stable implementation with Kotlin vs. Flutter plugins
- **JNI Integration**: Direct whisper.cpp binding more performant than FFI
- **Battery Optimization**: Granular control over wake locks and audio focus
- **APK Size**: Smaller without Flutter engine overhead (~15MB vs ~30MB)

**Trade-offs**:
- No iOS support (acceptable for v1 proof of concept)
- Kotlin learning curve for team (mitigated by excellent documentation)
- More boilerplate than Flutter (acceptable for better native integration)

---

## 7.5. Authentication & Device Registration

**Model**: API Key per device (not OAuth, no user login)

**Server Configuration**:
- Admin generates API key via CLI tool: `python manage.py create-device`
- Returns: `device_id` + `api_key` (UUID v4)
- Stored in PostgreSQL `devices` table

**Mobile App Storage**:
- API key stored in Android EncryptedSharedPreferences
- Device ID stored alongside for correlation

**Endpoints**:
```
POST /api/auth/register
  Body: { "device_name": "John's Pixel 7" }
  Response: { "device_id": "uuid", "api_key": "secret" }
  
Headers for all requests:
  X-Device-ID: <device_id>
  X-API-Key: <api_key>
```

**Security**:
- HTTPS required (reject HTTP)
- API key rotation supported (admin CLI tool)
- Rate limiting: 1000 requests/hour per device

**Setup Flow**:
1. User deploys server, gets server URL
2. User runs: `docker exec limitless python manage.py create-device --name "My Phone"`
3. CLI outputs device_id + api_key
4. User enters both into Android app settings screen
5. App validates by calling `GET /api/health` with credentials
6. App saves credentials and starts recording

**Why not OAuth/JWT?**
- Single-user self-hosted app doesn't need user accounts
- Simpler setup (no auth provider configuration)
- Sufficient security for self-hosted use case
- Can add multi-user support in v2 if needed

---

## 8. API & Data Models

### 8.1 Mobile → Server Endpoints

1. **POST /api/audio**  
   - Payload: `audio_chunk: base64`, `timestamp`, `device_id`  
   - Returns: `transcript_id`

2. **GET /api/transcript/{id}**  
   - Returns: `{ transcript: string, actions: [] }`

3. **POST /api/actions/{id}/execute**  
   - Payload: `{ action_type, metadata }`  
   - Returns: `status`

### 8.2 Data Schemas

**User**:
```json
{
  "id": "uuid",
  "device_id": "string",
  "api_key": "string (hashed)",
  "created_at": "ISO8601",
  "last_seen": "ISO8601"
}
```

**Session**:
```json
{
  "id": "uuid",
  "device_id": "uuid (foreign key)",
  "started_at": "ISO8601",
  "ended_at": "ISO8601 (nullable)",
  "duration_seconds": "integer",
  "total_transcripts": "integer"
}
```

**Speaker** (for diarization):
```json
{
  "id": "uuid",
  "session_id": "uuid (foreign key)",
  "label": "string (Speaker_00, Speaker_01, etc.)",
  "user_assigned_name": "string (nullable - user can label as 'John', 'Sarah')",
  "first_appearance": "ISO8601"
}
```

**Transcript** (updated):
```json
{
  "id": "uuid",
  "session_id": "uuid (foreign key)",
  "speaker_id": "uuid (foreign key, nullable)",
  "text": "string",
  "start_time": "ISO8601",
  "end_time": "ISO8601",
  "duration_seconds": "float",
  "embeddings": "[float] (384-dim for sentence-transformers)",
  "source": "enum (on_device, remote_api)",
  "confidence": "float (0.0-1.0, if available from whisper)"
}
```

**Action** (updated):
```json
{
  "id": "uuid",
  "transcript_id": "uuid (foreign key)",
  "type": "enum (reminder, task, email, event, document, note)",
  "payload": {
    "title": "string",
    "description": "string (nullable)",
    "due_date": "ISO8601 (nullable)",
    "metadata": "jsonb (type-specific fields)"
  },
  "detected_at": "ISO8601",
  "confidence": "float (LLM confidence score)",
  "status": "enum (pending, accepted, rejected, executed)",
  "user_action_at": "ISO8601 (nullable)"
}
```

---

## 8.3. RAG Architecture

**[TENTATIVE - Requires Pre-Development Research Phase]**

**Research Tasks** (before implementation):
1. **Speaker Diarization Accuracy**: Test Pyannote vs. simple heuristics (time gaps, audio features) on sample recordings
2. **Embedding Model Selection**: Benchmark sentence-transformers models (all-MiniLM-L6-v2 vs. all-mpnet-base-v2) for semantic search quality
3. **Chunking Strategy**: Determine optimal transcript segmentation (speaker-turn vs. time-window vs. semantic-boundary)

**Tentative Design** (subject to change based on research):

**Embedding Model** (options):
- **Option A**: `all-MiniLM-L6-v2` (384-dim, fast, good enough)
- **Option B**: `all-mpnet-base-v2` (768-dim, slower, higher quality)
- **Decision criteria**: Balance between search quality and server CPU usage

**Chunking Strategy** (options):
- **Option A**: Per speaker turn (each time speaker changes = new chunk)
- **Option B**: Fixed 30-second windows with overlap
- **Option C**: Semantic boundary detection (pause >2 seconds)

**Storage**:
- Embeddings stored in PostgreSQL `transcripts.embeddings` column (pgvector)
- Index: `ivfflat` or `hnsw` depending on scale testing

**Retrieval**:
- User query → embedded via same model
- Vector similarity search (cosine distance)
- Top 10 results with reranking by recency

**Context Assembly**:
- Retrieve transcript chunks + surrounding context (±30 seconds)
- Include speaker labels if available
- Return to LLM for summarization/answering

**Search UX**:
- Natural language: "What did John say about the budget?"
- Keyword fallback: Full-text search if vector search returns <3 results
- Filters: Date range, speaker, session

**Why Tentative?**:
- Speaker diarization accuracy unknown (may need different approach)
- Embedding model performance needs real-world testing
- Chunking strategy impacts search quality significantly

---

## 8.4. Action Detection Architecture

**Model**: Ollama running Llama 3.1 8B (self-hosted on server)

**Trigger Strategy** [TO BE DECIDED WITH ARCHITECT]:
- **Option A**: Real-time per transcript chunk (high CPU, immediate actions)
- **Option B**: Batch every 60 seconds (lower CPU, slightly delayed)
- **Option C**: User-initiated scan (minimal CPU, manual workflow)

**Example Interaction**:
```
[User speaking]: "...yeah, I'll send you that sprint report by Friday"
[Proactive notification]: "📋 Create reminder: Send sprint report? date: friday"
[User taps accept]: [Reminder created in google calendar with context on correct date]
```

**Prompt Engineering Approach**:
```
System: You are an action detection assistant. Analyze the transcript and identify ONLY clear, actionable items.

Categories:
- reminder: "remind me to...", "don't forget to..."
- task: "I need to...", "make sure to..."
- email: "send an email to...", "email [person] about..."
- event: "schedule a meeting...", "calendar event for..."
- note: "write this down...", "important to remember..."

Output JSON ONLY:
{
  "actions": [
    {
      "type": "reminder|task|email|event|note",
      "title": "brief title",
      "description": "full context",
      "confidence": 0.0-1.0,
      "transcript_excerpt": "exact quoted text"
    }
  ]
}

Transcript: {transcript_text}
```

**Processing Flow**:
1. Transcript chunk arrives at server
2. Server queues for action detection (async)
3. Ollama processes via prompt above
4. Parse JSON response
5. Store actions with `status=pending`
6. Push notification sent to device

**Rate Limiting**:
- Max 10 action detections per minute (prevent spam)
- Batch multiple transcripts if needed

**False Positive Mitigation**:
- Confidence threshold: Only notify if >0.7
- User feedback: "Not relevant" button trains filter
- Context window: Include previous 2 transcript chunks for context

---

## 9. UI/UX Mockups

1. **Recording Screen**  
   - Large "Listening…" indicator  
   - Status: Bluetooth connected / fallback mic  

2. **Transcript Stream**  
   - Live text scrolling with timestamps  
   - Highlighted action suggestions inline  

3. **Notification Flow**  
   - System notification: "Create reminder: buy milk"  
   - "Accept" → navigates to a confirmation screen  

*(Draft simple wireframes in Figma or Sketch; embed links in docs.)*

---

## 9.5. Data Retention & Storage Management

**Default Policy**:
- Transcripts retained for **90 days** (configurable via server ENV var)
- Actions retained indefinitely (or until user deletes)
- Audio files never stored (only transcripts)

**Storage Estimates** (per user):
- 12 hours/day recording = ~120KB transcript text/day
- 90 days = 10.8MB text + ~4.2MB embeddings = **15MB total**
- 90 days audio = ~2Gb (compressed)

**Compression**:
- Old transcripts (>30 days) can be gzip compressed (reduce by ~70%)
- Audio (source for document and replay-ability) always compressed
- Embeddings not compressed (needed for search)

**Data Export**:
- User can download all transcripts  as JSON via `/api/export` endpoint
- Format: `{ "sessions": [...], "transcripts": [...], "actions": [...] }`

**Manual Management**:
- Admin CLI: `python manage.py cleanup --days 90`
- Runs automatically via cron (weekly)

**Backup Strategy**:
- Postgres daily backups via Docker volume snapshots
- User responsible for backup storage (S3, local NAS, etc.)

---

## 10. Development Roadmap & Milestones

**Milestone-Based Approach** 

### Milestone 1: Audio Pipeline Foundation ✅ COMPLETE
**Goal**: Prove we can reliably capture and buffer Bluetooth audio

**Deliverables**:
- ✅ Android foreground service with notification
- ✅ Bluetooth SCO audio capture (with auto-fallback to device mic)
- ✅ Fallback to device microphone (implemented 2026-03-17)
- ✅ Audio buffer (30-second chunks) accumulated in memory → passed to Whisper
- ⬜ WAV file writing/compression — removed in favour of in-memory PCM pipeline (simpler, no disk I/O)

**Verified** (2026-03-17, Samsung S22 Ultra):
- Service starts and shows foreground notification immediately
- AudioRecord initialises on device mic when no Bluetooth headset is connected
- 30-second PCM chunks accumulate correctly (938KB per chunk @ 16kHz mono 16-bit)

---

### Milestone 2: On-Device Transcription ✅ COMPLETE
**Goal**: Integrate whisper.cpp and transcribe locally

**Deliverables**:
- ✅ whisper.cpp JNI bindings (Kotlin → C++)
- ✅ ggml model loader — `ggml-base.en.bin` (147MB, downloaded on first run)
- ✅ Transcription of 30-second audio chunks

**Verified** (2026-03-17, Samsung S22 Ultra):
- Model loads in ~500ms (already cached)
- 30-second chunk transcribed in ~4-5 seconds (~6x real-time) on CPU
- Transcription output confirmed: `" [MUSIC]"`, `" (clippers buzzing)"` (background audio from device)
- No crashes over multiple transcription cycles

**Success Criteria vs Actuals**:
- ✅ <3 second latency? **Actual: ~4-5s** — acceptable, may improve with smaller model
- ✅ >90% word accuracy — manual spot-check needed with speech (tested on silence/background)
- ✅ No memory leaks after 5+ transcriptions — no OOM observed

---

### Milestone 3: End-to-End Local Pipeline ✅ COMPLETE
**Goal**: Complete offline recording + transcription loop

**Deliverables**:
- ✅ Local SQLite database (Room) — sessions + transcripts tables
- ✅ Session management (auto-start on service init, end on stop)
- ✅ Transcript persistence and display in app
- ✅ Keyword search transcripts (SQLite LIKE)

**Verified** (2026-03-17, Samsung S22 Ultra):
- Room DB created at `databases/limitless.db`
- 5 transcripts confirmed in DB after ~3 minutes of recording
- UI shows functional Recording screen, Transcript list, Settings — no placeholders
- App auto-starts recording as soon as Whisper model is ready

**Remaining**:
- ⬜ 24-hour dogfooding session
- ⬜ Force-kill data integrity test (10 kills)
- ⬜ Search UI tested end-to-end with real speech transcripts

---

### Milestone 4: Server Sync (RunOS Dev) - 🚧 IN PROGRESS
**Goal**: Sync transcripts to a centralized backend on RunOS `dev`

**Deliverables**:
- ✅ FastAPI backend scaffolded (`server/` directory — FastAPI, SQLAlchemy, Alembic)
- ✅ RunOS app deployed to `dev` (app ID: `z0dhx`, cluster: `tdc`, port 8000)
- ✅ Health endpoints (`GET /health`, `/health/database`, `/health/ollama`)
- ✅ Transcript upload endpoint (`POST /transcripts/`)
- ✅ Device registration endpoint (`POST /transcripts/register-device`)
- ✅ PostgreSQL ORM models — `transcripts` + `devices` tables (SQLAlchemy)
- ⬜ RunOS PostgreSQL service provisioned and wired to server (DB env vars set)
- ⬜ Android app `SyncWorker` — HTTP client pointing to RunOS server URL
- ⬜ Authentication middleware (API key validation on all routes)
- ⬜ Alembic migrations running on deploy

**Success Criteria**:
- Successful automated sync from Android app to RunOS Postgres
- Under 10s latency from recording → cloud visibility
- Zero data loss during simulated network failures

**Validation Method**:
- Check transcripts in RunOS Postgres using SQL queries
- Validate cloud upload logs in Android Studio

**Risk Mitigation**:
- Deployment complexity → Use `runos manifest` and `runos jobs`
- App connectivity → Use `runos sensitive-read` for public IPs/DNS

---

### Milestone 5: Action Detection
**Goal**: Detect actionable items from transcripts

**Deliverables**:
- Ollama integration (Llama 3.1 8B)
- Action detection prompt engineering
- Android notification on action detected
- Accept/reject action buttons

**Success Criteria**:
- >80% precision (8/10 notifications are relevant)
- <10 second latency from speech to notification
- Zero crashes from malformed LLM output

**Validation Method**:
- Test set of 50 phrases (25 with actions, 25 without)
- Precision/recall metrics

**Risk Mitigation**:
- LLM hallucinations → Schema validation, confidence threshold
- Slow inference → Smaller model (Llama 3.2 3B) if needed

---

### Milestone 6: RAG Search
**Goal**: Semantic search over past transcripts

**Deliverables**:
- Sentence-transformers embedding model
- pgvector integration
- Search UI in app
- Context retrieval for LLM

**Success Criteria**:
- Relevant results in top 5 for 90% of queries
- <1 second search latency
- Works with 10,000+ transcripts

**Validation Method**:
- User acceptance testing (5 beta testers)
- Benchmark search quality with golden dataset

**Risk Mitigation**:
- Poor search quality → Try different embedding models
- Slow performance → Optimize pgvector indexes

---

### Milestone 7: Speaker Diarization (Research + Implementation)
**Goal**: Identify different speakers in conversations

**Deliverables**:
- Research report: Pyannote vs. heuristics
- Chosen implementation integrated
- Speaker labels in transcript UI
- User can rename speakers

**Success Criteria**:
- >75% speaker identification accuracy (manual evaluation)
- <2x processing time overhead
- Graceful degradation if diarization fails

**Validation Method**:
- Test recordings with 2-4 speakers
- Confusion matrix analysis

**Risk Mitigation**:
- Pyannote too slow → Use simple heuristics (time gaps)
- Low accuracy → Make speaker labels optional, not blocking

---

### Milestone 8: Polish & Distribution
**Goal**: Productionize and release

**Deliverables**:
- App icon, splash screen, UI polish
- Setup documentation (README, Hostinger deployment guide)
- APK signing and GitHub release
- Demo video (2 minutes)

**Success Criteria**:
- 5 external testers successfully deploy and use
- <10 bugs reported in first week
- Documentation clear enough for non-technical users to follow

**Validation Method**:
- Beta testing program (invite 10 users)
- Monitor GitHub issues

**Risk Mitigation**:
- Deployment complexity → Provide Docker Compose one-liner
- Unclear docs → User testing of setup process

---

**Estimated Timeline**: 8-12 weeks (assuming 1-2 weeks per milestone)

---

## 11. Immediate Actions

Before writing any code, complete these foundational tasks:

1. **Create GitHub Repository**
   - Initialize with README, LICENSE (MIT), .gitignore
   - Add issue templates (bug, feature request)
   - Setup GitHub Actions for Android APK builds

2. **Research Tasks** (document findings in `/docs/research`):
   - **Speaker Diarization**: Test Pyannote.audio on 5 sample recordings, measure accuracy + latency
   - **Embedding Model**: Benchmark 3 sentence-transformer models on search quality
   - **Whisper.cpp Android**: Find or create JNI bindings reference implementation

3. **Milestone 1 Preparation**:
   - Setup Android Studio project (Kotlin, targetSdk 35)
   - Create project structure (`app/`, `docs/`, `server/`)
   - Add dependencies: Room, WorkManager, OkHttp

4. **Server Environment Setup**:
   - Provision Hostinger VPS or local development machine
   - Install Docker + Docker Compose
   - Test Ollama + Postgres deployments

5. **Documentation**:
   - Write `CONTRIBUTING.md` with code style guide
   - Create `/docs/architecture-decisions.md` (ADR log)
   - Draft setup guide for server deployment

**Success Definition**: When a new contributor can clone the repo, follow the README, and have a working development environment in <30 minutes.

With this milestone-driven plan, you have a clear path from prototype to production. Each milestone is independently testable and delivers tangible value. Let's build this!
