# API Reference — Limitless Companion Server

Base URL: `https://<your-server-url>` (RunOS dev: app `z0dhx`, cluster `tdc`)

All endpoints accept and return `application/json`.

> **Auth**: API key auth middleware is **planned but not yet implemented**. Routes are currently open. See Milestone 4 remaining tasks.

---

## Health

### `GET /health`
Comprehensive system health check.

**Response**:
```json
{
  "status": "healthy | unhealthy",
  "database": "connected | error: <msg>",
  "ollama": "ready | model_not_found: <model> | connection_error: <msg>",
  "models_loaded": true,
  "version": "1.0.0"
}
```

### `GET /health/database`
Database-only health check.

### `GET /health/ollama`
Ollama service health check.

---

## Transcripts

### `POST /transcripts/`
Upload a transcript chunk from the Android app.

**Request body**:
```json
{
  "session_id": "string",
  "text": "string",
  "start_time": 1710000000000,
  "duration_ms": 30000,
  "source": "on_device"
}
```
- `start_time`: Unix epoch milliseconds
- `source`: `"on_device"` or `"remote_api"`

**Response** (`201`):
```json
{
  "id": "uuid",
  "session_id": "string",
  "text": "string",
  "start_time": 1710000000000,
  "duration_ms": 30000,
  "source": "on_device",
  "created_at": "2026-03-17T11:42:07Z"
}
```

---

### `GET /transcripts/`
List transcripts, optionally filtered by session.

**Query params**:
- `session_id` *(optional)*: Filter by session UUID

**Response**: Array of transcript objects (descending by `created_at`).

---

### `POST /transcripts/register-device`
Register or update a device (upsert by `device_id`).

**Request body**:
```json
{
  "device_id": "string",
  "name": "My Pixel 9"
}
```

**Response**:
```json
{
  "status": "success",
  "device_id": "string"
}
```

---

## Planned Endpoints (not yet implemented)

| Method | Path | Milestone | Description |
|--------|------|-----------|-------------|
| `GET` | `/transcripts/search` | M6 | RAG semantic search |
| `GET` | `/actions/` | M5 | List pending detected actions |
| `POST` | `/actions/{id}/execute` | M5 | Accept/reject an action |
| `GET` | `/export` | M8 | Export all data as JSON |

---

## Database Schema (current)

### `transcripts`
| Column | Type | Notes |
|--------|------|-------|
| `id` | UUID PK | Auto-generated |
| `session_id` | String | Groups recordings |
| `text` | Text | Transcribed content |
| `start_time` | BigInt | Epoch ms |
| `duration_ms` | BigInt | Chunk length |
| `source` | String | `on_device` / `remote_api` |
| `created_at` | DateTime | UTC |
| `embedding` | Text | Reserved for pgvector (M6) |

### `devices`
| Column | Type | Notes |
|--------|------|-------|
| `id` | UUID PK | Auto-generated |
| `device_id` | String UNIQUE | Android device identifier |
| `name` | String | Human-readable label |
| `created_at` | DateTime | UTC |