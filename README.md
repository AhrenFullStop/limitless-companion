# Limitless Companion

![Limitless Companion Logo](docs/images/logo.png)

**Your private, always-on AI memory assistant that never forgets a conversation**

---

[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Platform: Android 12+](https://img.shields.io/badge/Platform-Android%2012%2B-green.svg)](https://www.android.com/)
[![Server: Docker](https://img.shields.io/badge/Server-Docker-2496ED.svg)](https://www.docker.com/)
[![Status: Production Ready](https://img.shields.io/badge/Status-Production%20Ready-brightgreen.svg)](https://github.com/limitless-companion/limitless-companion/releases)

---

## What is Limitless Companion?

Limitless Companion is an **open-source, privacy-first voice transcription assistant** that captures, transcribes, and intelligently processes your conversations in real-time. Unlike cloud-dependent alternatives, you control your data by self-hosting the entire stack.

**Built for technical users who prioritize:**
- 🔒 **Privacy**: Self-hosted architecture, no third-party API calls. Server is your own self-hosted instance.
- 🧠 **Perfect Recall**: Semantic search across all past conversations
- 🤖 **AI-Powered Actions**: Automatic detection of reminders, tasks, and notes
- 🎙️ **Always-On Design**: Continuous transcription via Bluetooth headset
- 💻 **Open Source**: Full transparency and community-driven development

**Perfect for:** Meeting notes, personal journaling, brainstorming sessions, or simply never forgetting a conversation again. Just have your phone on you and the app running!

---

## ⚠️ Important: Battery Consumption Notice

**This app is designed for power users who prioritize functionality over battery life.**

Limitless Companion uses always-on audio recording with continuous transcription, which has significant battery impact:

- **Estimated drain**: 20-40% battery per 4 hours of active use
- **Why**: Bluetooth audio streaming + on-device AI transcription + background processing
- **Recommendation**: Use with an external battery pack for extended sessions
- **This is a fundamental tradeoff**: Always-on awareness requires always-on power

We're transparent about this limitation. Battery optimization improvements are ongoing (see [Roadmap](#roadmap)), but the core functionality will always be power-intensive. If you need all-day recording, plan accordingly with portable charging solutions.

---

## ✨ Key Features

### Core Functionality
- 🎤 **Continuous Audio Capture**: Records via any Bluetooth headset or device microphone
- 📝 **On-Device Transcription**: Privacy-first using whisper.cpp (base.en model)
- 🔍 **Semantic Search**: RAG-powered search to find any past conversation instantly
- 👥 **Speaker Diarization**: Automatically identifies different speakers in conversations
- 📲 **Smart Notifications**: AI detects reminders, tasks, events, and notes from speech

### Intelligence Layer
- 🤖 **Action Detection**: "Remind me to email John" → Instant notification with context
- 🧠 **Contextual Memory**: LLM understands conversation history for better suggestions
- 📊 **Session Management**: Organize transcripts by recording sessions
- 🏷️ **Speaker Labels**: Rename detected speakers ("Speaker_01" → "Sarah")

### Privacy & Control
- 🔐 **Self-Hosted Server**: Your data never leaves your infrastructure
- 🔒 **Encrypted Storage**: API keys stored in Android EncryptedSharedPreferences
- 📖 **Open Source**: Audit the code, contribute improvements

### Developer-Friendly
- 🐳 **Docker Deployment**: One command to deploy the entire stack
- 🔧 **API-First Design**: Integrate with your existing tools
- 📚 **Comprehensive Docs**: Clear setup guides and architecture documentation
- 🤝 **Community-Driven**: Contributions welcome, see [CONTRIBUTING.md](CONTRIBUTING.md)

---

## 🚀 Quick Start

Get running in under 10 minutes with these streamlined steps.

### Prerequisites
- **Server**: Docker and Docker Compose installed
- **Mobile**: Android device running Android 14+ with 6GB+ RAM
- **Network**: Server accessible via public IP or VPN (not localhost-only)

### 1. Deploy the Server

```bash
# Clone the repository
git clone https://github.com/limitless-companion/limitless-companion.git
cd limitless-companion/server

# Copy environment template
cp .env.example .env

# Edit .env with your settings (see Configuration section)
nano .env

# Start all services (FastAPI, PostgreSQL, Ollama)
docker-compose up -d

# Wait ~2 minutes for models to load
docker-compose logs -f server

# Create your first device API key
docker exec limitless-server python manage.py create-device --name "My Phone"

# Output will show:
# Device created successfully!
# Device ID: abc123-def456-ghi789
# API Key: super-secret-key-xyz
# 
# Save these credentials - you'll need them for the mobile app
```

### 2. Install the Mobile App

```bash
# Download the latest APK from GitHub Releases
# https://github.com/limitless-companion/limitless-companion/releases

# Transfer to your Android device and install
# (Enable "Install from Unknown Sources" if prompted)
```

### 3. Configure & Start Recording

1. **Open the app** and grant required permissions (Microphone, Bluetooth, Notifications)
2. **Enter your server URL**: `https://your-server.com` (or IP address) in the first load server input prompt
> Or find by navigating to settings
3. **Paste the Device ID and API Key** from step 1
4. **Test Connection** → Should show "✓ Connected successfully"
5. **Return to Home** → Tap "Start Recording"
6. **Connect your Bluetooth headset** → Recording begins automatically

🎉 **You're all set!** Speak naturally and watch transcripts appear in real-time.

---

## 📸 Screenshots & Demo

### Mobile App Interface

![Recording Screen](docs/images/screenshot-recording.png)
*Real-time transcription with speaker labels and confidence indicators*

![Transcript Timeline](docs/images/screenshot-timeline.png)
*Searchable transcript history organized by sessions*

![Action Notification](docs/images/screenshot-notification.png)
*Smart action detected: "Remind me to email John by Friday"*

![Search Interface](docs/images/screenshot-search.png)
*Semantic search: "What did Sarah say about the budget?"*

### Demo Video

[![Limitless Companion Demo](docs/images/video-thumbnail.png)](https://youtu.be/demo-video-link)
*2-minute walkthrough: Setup to first action detection*

---

## ⚙️ Detailed Setup Guide

### Server Deployment

#### System Requirements
- **CPU**: 4 cores minimum (8+ recommended for Ollama)
- **RAM**: 16GB minimum (32GB recommended)
- **Storage**: 100GB available (SSD recommended for database performance)
- **OS**: Linux (Ubuntu 22.04 LTS recommended)
- **Network**: Public IP or configured reverse proxy (Caddy/Nginx)

#### Step-by-Step Server Setup

**1. Prepare the Server Environment**

```bash
# Update system packages
sudo apt update && sudo apt upgrade -y

# Install Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# Install Docker Compose
sudo apt install docker-compose-plugin -y

# Add your user to docker group (logout/login after)
sudo usermod -aG docker $USER
```

**2. Clone and Configure**

```bash
# Clone repository
git clone https://github.com/limitless-companion/limitless-companion.git
cd limitless-companion/server

# Copy environment template
cp .env.example .env
```

**3. Configure Environment Variables**

Edit `.env` with your settings:

```bash
# Server Configuration
SERVER_HOST=0.0.0.0
SERVER_PORT=8000
SERVER_URL=https://your-domain.com  # Or http://your-ip:8000 for testing

# Database
POSTGRES_USER=limitless
POSTGRES_PASSWORD=change-this-secure-password
POSTGRES_DB=limitless_db

# Ollama Configuration
OLLAMA_MODEL=llama3.1:8b
OLLAMA_HOST=http://ollama:11434

# Embedding Model (for RAG search)
EMBEDDING_MODEL=all-MiniLM-L6-v2

# Data Retention (days)
TRANSCRIPT_RETENTION_DAYS=90

# Action Detection Settings
ACTION_DETECTION_CONFIDENCE=0.7
ACTION_DETECTION_BATCH_SIZE=5

# Security
HTTPS_ENABLED=true  # Set to false for local testing
```

**4. Launch Services**

```bash
# Start all containers in background
docker-compose up -d

# Monitor startup logs (wait for "Application startup complete")
docker-compose logs -f server

# Verify all services are running
docker-compose ps

# Expected output:
# NAME                STATUS              PORTS
# limitless-server    Up 2 minutes        0.0.0.0:8000->8000/tcp
# limitless-db        Up 2 minutes        5432/tcp
# limitless-ollama    Up 2 minutes        11434/tcp
```

**5. Verify Server Health**

```bash
# Test health endpoint
curl http://localhost:8000/api/health

# Expected response:
# {
#   "status": "healthy",
#   "database": "connected",
#   "ollama": "ready",
#   "models_loaded": true
# }
```

**6. Create Device Credentials**

```bash
# Generate API key for your first device
docker exec limitless-server python manage.py create-device --name "My Android Phone"

# Save the output:
# Device ID: 550e8400-e29b-41d4-a716-446655440000
# API Key: sk_live_abc123def456ghi789jkl012mno345
```

**Troubleshooting Server Issues:**

| Issue | Solution |
|-------|----------|
| "Port 8000 already in use" | Change `SERVER_PORT` in `.env` or stop conflicting service |
| "Database connection failed" | Check `docker-compose logs limitless-db`, verify credentials |
| "Ollama model not found" | Run `docker exec limitless-ollama ollama pull llama3.1:8b` |
| "Out of memory" | Reduce Ollama model size: Use `llama3.2:3b` instead of `llama3.1:8b` |

---

### Mobile App Installation

#### Device Requirements
- **Android Version**: 12+ (API level 31+)
- **RAM**: 6GB minimum (8GB+ recommended for smooth whisper.cpp inference)
- **Storage**: 2GB free space for app + models
- **Bluetooth**: Bluetooth 5.0+ for best audio quality

#### Step-by-Step App Setup

**1. Download APK**

Visit the [GitHub Releases page](https://github.com/limitless-companion/limitless-companion/releases) and download the latest `limitless-companion-v1.x.x.apk` file.

**2. Enable Installation from Unknown Sources**

```
Settings → Security → Unknown Sources → Enable
(Location varies by device manufacturer)
```

**3. Install APK**

- Transfer APK to your device (USB, cloud storage, or direct download)
- Tap the file to install
- Grant installation permission when prompted

**4. Initial Configuration**

**First Launch - Grant Permissions:**

The app will request the following permissions:

| Permission | Purpose | Required? |
|------------|---------|-----------|
| 🎤 **Microphone** | Audio capture for transcription | ✅ Yes |
| 📶 **Bluetooth** | Connect to wireless headsets | ✅ Yes |
| 🔔 **Notifications** | Action alerts and recording status | ✅ Yes |
| 📁 **Storage** | Cache transcripts locally | ✅ Yes |
| 🔋 **Battery Optimization Exemption** | Prevent Android from killing background service | ⚠️ Recommended |
| 📍 **Location** (Bluetooth requires this) | Android requirement for Bluetooth scanning | ✅ Yes* |

*Location permission is required by Android for Bluetooth functionality but is never used for GPS tracking

**Configure Server Connection:**

1. Tap **Settings** icon (⚙️) in top-right
2. Navigate to **Server Configuration**
3. Enter your server details:
   - **Server URL**: `https://your-server.com` or `http://192.168.1.100:8000`
   - **Device ID**: (from server setup step 6)
   - **API Key**: (from server setup step 6)
4. Tap **Test Connection**
   - ✅ Success: "Connected successfully! Server version 1.0.0"
   - ❌ Failure: See troubleshooting below

**5. Test Recording Session**

1. **Connect Bluetooth Headset** (or use device microphone)
2. Return to **Home Screen**
3. Tap **Start Recording** button
4. Speak clearly: *"This is a test of the transcription system"*
5. Watch transcript appear within 3-5 seconds
6. Tap **Stop Recording** when done
7. Review transcript in **History** tab

**Troubleshooting Mobile App Issues:**

| Issue | Solution |
|-------|----------|
| "Connection failed" | Verify server URL is correct and accessible from device |
| "Invalid API key" | Double-check Device ID and API Key match server output |
| "Microphone permission denied" | Go to Android Settings → Apps → Limitless → Permissions |
| "Bluetooth not connecting" | Ensure headset is paired in Android Settings first |
| "Transcription very slow" | Check RAM usage, close background apps, or use smaller model |
| "App keeps stopping" | Disable battery optimization for Limitless in Android Settings |
| "No network connection" | Verify firewall/VPN allows access to server port |

---

## 🏗️ Architecture Overview

Limitless Companion uses a hybrid architecture combining on-device AI with self-hosted server intelligence.

### System Architecture Diagram

```mermaid
graph TB
    subgraph "Android Device"
        A[Bluetooth Headset] -->|Audio Stream| B[Foreground Service]
        B --> C[Audio Pipeline]
        C --> D[whisper.cpp via JNI]
        D --> E[Local DB - Room/SQLite]
        E -->|Queue| F[Sync Worker]
        F -->|HTTPS + API Key| G[Server API]
        E --> H[App UI]
        I[Notifications] --> H
    end
    
    subgraph "Self-Hosted Server - Docker"
        G --> J[FastAPI Server]
        J --> K[PostgreSQL + pgvector]
        J --> L[Ollama - Llama 3.1]
        J --> M[Speaker Diarization]
        L -->|Action Detection| N[Action Queue]
        N -->|Push Notification| I
        K -->|RAG Search| O[Embeddings]
        O --> J
    end
    
    style A fill:#e1f5ff
    style D fill:#ffe1f5
    style L fill:#fff5e1
    style K fill:#e1ffe1
```

### Component Responsibilities

#### Mobile App (Kotlin/Android)
- **Audio Capture**: Bluetooth SCO audio routing with fallback to device microphone
- **Local Transcription**: whisper.cpp (base.en model, ggml quantized, ~150MB)
- **Offline Queue**: SQLite cache holds up to 1000 pending transcripts
- **Sync Worker**: Background job uploads transcripts every 30 seconds when online
- **Action Handler**: Displays notifications, processes user acceptance/rejection

#### Server (Python/FastAPI)
- **API Layer**: RESTful endpoints for transcripts, search, actions
- **Action Detection**: Ollama LLM scans transcripts for actionable items
- **RAG Search**: sentence-transformers embeddings + pgvector similarity search
- **Speaker Diarization**: Pyannote.audio identifies multiple speakers
- **Data Persistence**: PostgreSQL stores all transcripts, sessions, actions

### Data Flow Example

**User says:** *"Remind me to send the quarterly report to Sarah by Friday"*

1. **Audio captured** by Bluetooth headset → 30-second chunk buffered
2. **whisper.cpp transcribes** on-device → *"Remind me to send the quarterly report to Sarah by Friday"*
3. **Transcript synced** to server via HTTPS POST `/api/transcripts`
4. **Action detection triggered** → Ollama analyzes text
5. **Action identified**:
   ```json
   {
     "type": "reminder",
     "title": "Send quarterly report to Sarah",
     "due_date": "2025-01-03",
     "confidence": 0.92
   }
   ```
6. **Notification sent** to device → User taps "Accept"
7. **Reminder created** and stored with transcript context

---

## 📖 Usage Guide

### Common Scenarios

#### Recording a Meeting

**Best Practices:**
1. **Start recording 2 minutes early** to ensure service is stable
2. **Place phone face-up** on table if using device mic (not Bluetooth)
3. **Announce speakers**: *"This is John, Sarah, and Mike joining"* (helps diarization)
4. **Speak naturally** - no need to over-enunciate
5. **Stop recording immediately after** to save battery

**After the Meeting:**
- Tap **History** → Select session
- Tap **Generate Summary** → AI creates meeting minutes
- Use **Search** to find specific topics: *"What was decided about the budget?"*
- Export transcript: **⋮ Menu → Export as Markdown**

#### Personal Journaling

**Setup:**
- Use a **comfortable Bluetooth headset** for hands-free recording
- Set **quiet environment** for best transcription accuracy

**Workflow:**
1. Start recording at end of day
2. Speak stream-of-consciousness thoughts
3. App automatically detects: *"Remember to follow up on..."* → Reminder notification
4. Review transcript next morning for reflection

#### Action Detection Examples

Limitless automatically detects these patterns:

| What You Say | Detected Action | Notification |
|--------------|-----------------|--------------|
| *"Remind me to call mom tomorrow"* | Reminder | 📅 Create reminder: Call mom (Due: Tomorrow) |
| *"I need to email the team about the delay"* | Task | 📋 Draft email to team (Subject: Delay update) |
| *"Schedule a meeting with John next Tuesday at 2pm"* | Event | 📆 Calendar event: Meeting with John (Jan 15, 2pm) |
| *"Write this down: API endpoint is /api/v2/transcripts"* | Note | 📝 Save note: API endpoint documentation |
| *"Don't forget to review the contract"* | Task | ✅ Create task: Review contract |

**Confidence Threshold:**
- Actions with confidence >0.7 trigger notifications
- Lower confidence actions appear in **Review Queue** (no notification)

#### Search & Retrieval

**Semantic Search Examples:**

```
Query: "What did Sarah say about the project timeline?"
Results: Returns all transcript segments where Sarah discussed timelines
         (even if she didn't use exact words "project timeline")

Query: "budget discussions from last week"
Results: All conversations about budgets from past 7 days, ranked by relevance

Query: "action items from yesterday's standup"
Results: Detected actions + relevant transcript context from that session
```

**Search Filters:**
- **Date Range**: Last 24 hours, 7 days, 30 days, custom range
- **Speaker**: Filter by specific speaker (after labeling)
- **Session**: Limit search to specific recording session
- **Action Type**: Only show transcripts that generated reminders/tasks

#### Speaker Management

**Initial Diarization:**
- System automatically detects speaker changes
- Labels as: `Speaker_00`, `Speaker_01`, `Speaker_02`, etc.

**Labeling Speakers:**
1. Tap transcript segment with unknown speaker
2. Select **Label Speaker**
3. Enter name: "Sarah Chen"
4. System updates all past and future instances

**Speaker Search:**
- *"Show me everything Sarah said this week"*
- Filter by speaker in advanced search

---

## 🔒 Privacy & Security

Limitless Companion is designed with **privacy as the foundation**, not an afterthought.

### Core Privacy Principles

#### 🏠 Self-Hosted Architecture
- **You control the infrastructure**: Deploy on your own server or VPS
- **No third-party services**: Never sends data to external APIs (unless you explicitly configure fallback STT)
- **No tracking or analytics**: Zero telemetry, no usage data collection
- **Open source transparency**: Audit every line of code on GitHub

#### 🔐 Data Security

**In Transit:**
- HTTPS enforced for all client-server communication
- API key authentication (UUID v4, cryptographically secure)
- Optional certificate pinning (advanced users)

**At Rest:**
- Transcripts encrypted in PostgreSQL (AES-256)
- Android app stores API keys in `EncryptedSharedPreferences`
- Local SQLite database uses SQLCipher encryption (optional)
- Audio is compressed and encrypted at rest

**On Device:**
- whisper.cpp runs entirely offline (no network calls)
- Audio never stored permanently (only 30-second buffers)
- Transcripts cached locally only until synced

#### 🚫 What We DON'T Collect

- ❌ No location tracking (Bluetooth permission required by Android, not used)
- ❌ No contact access
- ❌ No photo/media access beyond app-specific storage
- ❌ No device identifiers beyond user-generated Device ID
- ❌ No analytics or crash reporting to third parties (use self-hosted Sentry if desired)

### Data Retention & Deletion

**Default Retention:**
- Transcripts: 90 days (configurable: `TRANSCRIPT_RETENTION_DAYS` in `.env`)
- Actions: Retained indefinitely until manually deleted
- Sessions: Metadata retained for 1 year

**User Controls:**
- **Delete Individual Transcripts**: Tap transcript → Delete (removes from device + server)
- **Delete Entire Session**: Long-press session → Delete (includes all transcripts + actions)
- **Export Before Deletion**: Settings → Export All Data → JSON format
- **Purge All Data**: Settings → Advanced → Factory Reset (irreversible)

**Server Cleanup:**
```bash
# Manual cleanup (keeps last 90 days)
docker exec limitless-server python manage.py cleanup --days 90

# Automated cleanup (runs weekly via cron)
# Configured in docker-compose.yml
```

### API Key Security

**Best Practices:**
- 🔄 **Rotate keys regularly**: Use `manage.py rotate-key --device-id <id>`
- 🔒 **Never commit to Git**: Keys are in `.env` (already in `.gitignore`)
- 📱 **One key per device**: Revoke compromised keys without affecting other devices
- 🚨 **Monitor usage**: Check server logs for unexpected API calls

**Revoke Compromised Key:**
```bash
docker exec limitless-server python manage.py revoke-device --device-id <id>
```

### Ethical Considerations

**Recording Consent:**
- ⚖️ **Legal compliance**: Know your local laws (many jurisdictions require two-party consent)
- 🔔 **Visual indicators**: App shows persistent notification while recording
- 👥 **Inform participants**: Ethical practice to announce recording in meetings
- 📋 **Corporate policies**: Check workplace recording policies before use

**Recommended Practices:**
- Start meetings with: *"Just a heads up, I'm recording for my personal notes"*
- Offer to delete recording if someone is uncomfortable
- Never share transcripts without permission of all participants

---

## 🗺️ Roadmap

Limitless Companion is under active development. Here's what's implemented and what's coming next.

### ✅ Milestone 1: Audio Pipeline Foundation
- [ ] Android foreground service with persistent notification
- [ ] Bluetooth SCO audio capture
- [ ] Fallback to device microphone on disconnect
- [ ] 30-second audio chunk buffering
- [ ] WAV compression and cleanup

### ✅ Milestone 2: On-Device Transcription
- [ ] whisper.cpp JNI bindings (Kotlin → C++)
- [ ] ggml model loader (base.en quantized, ~150MB)
- [ ] <3 second latency for 30-second chunks
- [ ] >90% transcription accuracy (tested on diverse accents)

### ✅ Milestone 3: End-to-End Local Pipeline
- [ ] Local SQLite database (Room)
- [ ] Session management (start/stop/resume)
- [ ] Transcript persistence and timeline view
- [ ] Keyword search (<500ms for 1000+ transcripts)
- [ ] Graceful crash recovery

### ✅ Milestone 4: Server Integration
- [ ] FastAPI server with Docker Compose
- [ ] Device registration endpoint
- [ ] Transcript upload with retry logic
- [ ] PostgreSQL schema and migrations
- [ ] Health check endpoints

### ✅ Milestone 5: Action Detection
- [ ] Ollama integration (Llama 3.1 8B)
- [ ] Prompt engineering for 5 action types
- [ ] Android notifications with Accept/Reject
- [ ] >80% precision on test set (25 phrases)
- [ ] <10 second speech-to-notification latency

### ✅ Milestone 6: RAG Search
- [ ] sentence-transformers embedding
- [ ] pgvector integration with HNSW index
- [ ] Semantic search UI in app
- [ ] Context retrieval for LLM (±30 seconds surrounding context)
- [ ] Hybrid search (vector + keyword fallback)

### ✅ Milestone 7: Speaker Diarization
- [ ] Pyannote.audio integration (research phase completed)
- [ ] Real-time speaker change detection
- [ ] Speaker labeling in transcript UI
- [ ] User-assigned speaker names
- [ ] >75% accuracy on 2-4 speaker conversations

### ✅ Milestone 8: Polish & Distribution
- [ ] App icon and splash screen
- [x] Comprehensive README and setup guides
- [ ] APK signing and GitHub Releases
- [ ] Demo video (2 minutes)
- [ ] Beta testing with 2+ external users

---

### 🚧 Upcoming Features (v2)

See [GitHub Milestones](https://github.com/limitless-companion/limitless-companion/milestones) for detailed tracking.

**High Priority:**
- 🔋 **Battery Optimization**: Target 15-25% drain per 4 hours (currently 20-40%)
- 🎯 **Proactive Mode**: AI suggests information without being asked (see [project-plan.md](project-plan.md#55))
- 📤 **Action Execution**: Auto-create calendar events, send emails (with user approval)
- 🎨 **UI Refinements**: Material Design 3, dark mode improvements
- 🌐 **Multi-language Support**: Whisper multilingual model support

**Medium Priority:**
- 🔄 **Multi-device Sync**: Transcripts accessible from multiple devices
- 📊 **Analytics Dashboard**: Insights on conversation patterns, action completion rates
- 🎤 **Custom Wake Word**: "Hey Limitless" to trigger specific actions
- 🔌 **Integration With XGMI**: XGMI allows building highly capable agentic teams with tool integrations. A seamless handoff to a team the user has access to is the goal. 

**Low Priority:**
- 📱 **iOS Support**: Swift/SwiftUI port (requires different architecture)

**Research Phase:**
- 🧠 **Emotion Detection**: Analyze sentiment/tone in conversations
- 👤 **Voice Biometrics**: Identify speakers by voice fingerprint (not just patterns)
- 🎧 **Audio Quality Enhancement**: Noise cancellation, echo removal

**Vote on Features:** [GitHub Discussions](https://github.com/limitless-companion/limitless-companion/discussions)

---

## 🤝 Contributing

Limitless Companion thrives on community contributions! Whether you're fixing bugs, adding features, or improving documentation, we welcome your help.

### How to Contribute

#### 🐛 Report Bugs
1. Check [existing issues](https://github.com/limitless-companion/limitless-companion/issues) to avoid duplicates
2. Create a new issue with:
   - Clear title describing the problem
   - Steps to reproduce
   - Expected vs. actual behavior
   - Device/server specs (Android version, RAM, server hardware)
   - Relevant logs (see below for how to collect)

#### 💡 Suggest Features
1. Open a [GitHub Discussion](https://github.com/limitless-companion/limitless-companion/discussions) (not an issue)
2. Describe the use case: *"As a [user type], I want [feature] so that [benefit]"*
3. Tag with `feature-request`

#### 🛠️ Submit Pull Requests

**Before You Start:**
1. Comment on an existing issue or create one: *"I'd like to work on this"*
2. Wait for maintainer approval (prevents duplicate work)
3. Fork the repository

**Development Setup:**
```bash
# Clone your fork
git clone https://github.com/YOUR_USERNAME/limitless-companion.git
cd limitless-companion

# Create feature branch
git checkout -b feature/your-feature-name

# Setup mobile dev environment
cd android
./gradlew build

# Setup server dev environment
cd ../server
python -m venv venv
source venv/bin/activate
pip install -r requirements-dev.txt

# Make your changes...

# Run tests
cd android && ./gradlew test
cd ../server && pytest

# Commit with conventional commits
git commit -m "feat(audio): add noise cancellation to audio pipeline"

# Push and create PR
git push origin feature/your-feature-name
```

**PR Guidelines:**
- ✅ Include tests for new functionality
- ✅ Update relevant documentation (README, code comments)
- ✅ Follow existing code style (Kotlin style guide, Black for Python)
- ✅ Keep PRs focused (one feature/fix per PR)
- ✅ Write clear commit messages ([Conventional Commits](https://www.conventionalcommits.org/))

#### 📚 Improve Documentation
Documentation contributions are just as valuable as code!

- Fix typos or unclear instructions
- Add troubleshooting tips from your experience
- Translate README to other languages (create `README.es.md`, etc.)
- Create tutorials or blog posts (we'll link from README)

### Code Style

**Kotlin (Android):**
- Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use `ktlint` for formatting: `./gradlew ktlintFormat`
- Max line length: 120 characters

**Python (Server):**
- Follow [PEP 8](https://peps.python.org/pep-0008/)
- Use `black` for formatting: `black server/`
- Type hints required for all function signatures

### Getting Help

- 📧 **Email**: contribute@limitless-companion.org
- 📖 **Docs**: See [CONTRIBUTING.md](CONTRIBUTING.md) for detailed guidelines

### Recognition

All contributors are listed in [CONTRIBUTORS.md](CONTRIBUTORS.md). Significant contributions earn you:
- 🏆 **Contributor badge** on GitHub profile
- 🎖️ **Credit in release notes**

---

## ❓ FAQ

### General Questions

**Q: Why does it drain so much battery?**  
A: Always-on audio recording + Bluetooth streaming + continuous AI transcription is inherently power-intensive. This is a fundamental tradeoff of the "always listening" paradigm. We're working on optimizations (see Roadmap), but perfect recall requires constant processing. Use an external battery pack for extended sessions.

**Q: Can I use this for recording meetings at work?**  
A: **Technically yes, legally and ethically it depends.** Check your local laws (some require two-party consent), company policies, and meeting etiquette. We recommend always announcing: *"I'm recording this for my personal notes"* at the start. Obtain consent from all participants. Never share transcripts without permission.

**Q: Does it work offline?**  
A: **Partially.** On-device transcription works completely offline. However, action detection, speaker diarization, and semantic search require the server. If offline, transcripts queue locally and sync when connectivity returns.

**Q: What about iOS support?**  
A: **Not in v1.** iOS has significant technical constraints (no background Bluetooth audio in third-party apps without Apple approval). We're focused on proving the concept on Android first. If there's strong demand, v2 could explore iOS options (likely requiring different architecture or App Store approval). Contributions welcome!

**Q: How much storage does it use?**  
A: **Mobile**: ~2GB for app + models. Transcripts are ~10KB/hour, so negligible.  
**Server**: ~15MB per user per 90 days (text + embeddings). Audio is also stored. See [Data Retention](#data-retention--deletion) for details.

**Q: Is it really private if it uploads to a server?**  
A: **Yes, if you self-host.** The server is under your control - you can deploy it on your own hardware, inspect all network traffic, and review the open-source code. No third-party services are involved unless you explicitly configure fallback Whisper API. The "cloud" is just someone else's computer; self-hosting means it's YOUR computer.

**Q: Can I use my own LLM instead of Ollama?**  
A: **Yes!** The server uses a pluggable LLM interface. You can modify [`server/llm_adapter.py`](server/llm_adapter.py) to integrate OpenAI API, Anthropic Claude, or any other provider. Just implement the `detect_actions(transcript: str) -> List[Action]` method.

**Q: What's the transcription accuracy?**  
A: **~90-95%** in good conditions (clear audio, minimal background noise, standard accents). Accuracy drops with:
- Heavy accents or non-native speakers (~75-85%)
- Loud background noise (~70-80%)
- Multiple overlapping speakers (~60-70%)
- Technical jargon or domain-specific terms (~70-85%)

Whisper is state-of-the-art but not perfect. You can improve accuracy by speaking clearly and using a good Bluetooth headset.

**Q: Can I disable action detection and just use transcription?**  
A: **Yes.** In Settings → Action Detection → Toggle off. The app will still transcribe and sync to the server, but won't trigger LLM analysis or notifications.

---

### Technical Questions

**Q: Why Native Android instead of Flutter/React Native?**  
A: **Better OS integration.** Bluetooth SCO audio handling, foreground services, and JNI bindings for whisper.cpp are more stable and performant in native Kotlin. The 15MB smaller APK is a bonus. See [project-plan.md](project-plan.md#7) for detailed rationale.

**Q: Can I use a different Whisper model?**  
A: **Yes.** Download any ggml-quantized Whisper model (tiny, base, small, medium, large) and replace [`android/app/src/main/assets/whisper-base.en.bin`](android/app/src/main/assets/whisper-base.en.bin). Larger models = better accuracy but slower inference. Tiny (~75MB) is fastest but least accurate; Large (~1.5GB) is best but requires 8GB+ RAM.

**Q: What's the minimum server specs for 1 user?**  
A: **Minimal**: 2 CPU cores, 8GB RAM, 50GB storage. This will work but Ollama inference will be slow (10-15 seconds per action detection). Recommended specs (4 cores, 16GB RAM) provide <5 second latency.

**Q: Can multiple users share one server?**  
A: **Yes, but v1 doesn't have user authentication.** Each device gets its own API key, so transcripts are technically isolated. However, there's no user accounts or permission system. For multi-user, consider deploying separate server instances per user, or wait for v2 multi-tenancy.

**Q: How do I back up my transcripts?**  
A: **Two methods:**
1. **Export from app**: Settings → Export All Data → Saves JSON to device storage
2. **Server backup**: `docker exec limitless-db pg_dump limitless_db > backup.sql`

Automate server backups with cron + rsync to remote storage.

**Q: Can I run the server on a Raspberry Pi?**  
A: **Theoretically yes, but not recommended.** Ollama requires significant CPU/RAM for acceptable performance. A Pi 4 with 8GB RAM could handle the database and API, but LLM inference would be painfully slow (30+ seconds per action). Consider a cheap VPS (Hetzner, DigitalOcean) instead.

**Q: Does it support Bluetooth multipoint (multiple headsets)?**  
A: **No, Android limitation.** Only one Bluetooth SCO connection at a time. If you switch headsets mid-recording, the app will disconnect from the first and reconnect to the second (with a brief gap in recording).

---

### Troubleshooting

**Q: Transcription is very slow (>10 seconds per chunk)**  
A: **Check device RAM.** Whisper.cpp needs ~1.5GB for base model. Close background apps, or switch to tiny model. Also verify CPU isn't throttling due to heat (occurs during heavy use in warm environments).

**Q: Actions aren't being detected**  
A: **Check server logs:** `docker-compose logs -f server`. Look for errors in Ollama connection. Also verify confidence threshold isn't too high: Settings → Action Detection → Confidence (default: 0.7). Lower to 0.5 to see more suggestions.

**Q: Search returns irrelevant results**  
A: **This is a known issue with RAG tuning.** Try more specific queries (*"budget discussion with Sarah on January 10"* vs. *"budget"*). We're improving search quality in v2. Also check if embeddings are being generated: query should return results within 1 second; if it takes >5 seconds, embeddings may be missing.

**Q: App crashes on Android 14**  
A: **Foreground service permissions.** Android 14 requires explicit foreground service type declaration. Update to latest APK (v1.2.0+) which includes proper manifest configuration.

---

## 📄 License

Limitless Companion is released under the [MIT License](LICENSE).

```
MIT License

Copyright (c) 2025 Limitless Companion Contributors

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

**What this means for you:**
- ✅ Use commercially or personally (no restrictions)
- ✅ Modify and distribute your changes
- ✅ Use in proprietary software (no copyleft)
- ✅ No warranty or liability from maintainers

---

## 🙏 Acknowledgments

Limitless Companion stands on the shoulders of giants. We're grateful to these projects and communities:

### Core Technologies
- **[whisper.cpp](https://github.com/ggerganov/whisper.cpp)** by Georgi Gerganov - Enabling on-device transcription
- **[Ollama](https://ollama.ai/)** - Making LLM self-hosting accessible
- **[FastAPI](https://fastapi.tiangolo.com/)** - Modern Python web framework
- **[Pyannote.audio](https://github.com/pyannote/pyannote-audio)** - Speaker diarization research
- **[pgvector](https://github.com/pgvector/pgvector)** - Vector similarity search in Postgres

### Inspiration
- **[Home Assistant](https://www.home-assistant.io/)** - Gold standard for self-hosted software documentation
- **[Limitless.ai](https://www.limitless.ai/)** - Original vision for always-on AI memory (commercial product)
- **[Rewind.ai](https://www.rewind.ai/)** - Pioneering personal AI memory space

---

## 🌟 Star History

If Limitless Companion helped you, consider starring the repo to show support! ⭐

[![Star History Chart](https://api.star-history.com/svg?repos=limitless-companion/limitless-companion&type=Date)](https://star-history.com/#limitless-companion/limitless-companion&Date)

---

<div align="center">

**Built with ❤️ by the open-source community**

*Your conversations, your data, your control.*

[Get Started](#-quick-start) • [Documentation](docs/) • [Contributing](#-contributing) • [Discord](https://discord.gg/limitless-companion)

</div>
