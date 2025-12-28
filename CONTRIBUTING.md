# Contributing to Limitless Companion

Thank you for your interest in contributing to Limitless Companion! This document provides guidelines and instructions for contributing to the project.

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [How Can I Contribute?](#how-can-i-contribute)
- [Development Setup](#development-setup)
- [Code Style Guidelines](#code-style-guidelines)
- [Pull Request Process](#pull-request-process)
- [Testing Requirements](#testing-requirements)
- [Documentation](#documentation)
- [Community](#community)

## Code of Conduct

This project adheres to the Contributor Covenant [Code of Conduct](CODE_OF_CONDUCT.md). By participating, you are expected to uphold this code. Please report unacceptable behavior to the project maintainers.

## How Can I Contribute?

### Reporting Bugs

Before creating bug reports, please check the [existing issues](https://github.com/limitless-companion/limitless-companion/issues) to avoid duplicates.

When creating a bug report, include:

- **Clear title**: Descriptive summary of the issue
- **Steps to reproduce**: Detailed steps to reproduce the behavior
- **Expected behavior**: What you expected to happen
- **Actual behavior**: What actually happened
- **Environment details**:
  - Android version and device model (if mobile issue)
  - Server OS and Docker version (if server issue)
  - App version or commit hash
  - Relevant logs (see [Collecting Logs](#collecting-logs))
- **Screenshots**: If applicable

### Suggesting Features

Feature requests are welcome! Please:

1. Open a [GitHub Discussion](https://github.com/limitless-companion/limitless-companion/discussions) (not an issue)
2. Use the format: *"As a [user type], I want [feature] so that [benefit]"*
3. Explain the use case and expected behavior
4. Tag with `feature-request`

### Submitting Pull Requests

1. **Check for existing work**: Comment on the issue you want to work on or create one
2. **Wait for approval**: Maintainers will assign the issue to you
3. **Fork and branch**: Create a feature branch from `main`
4. **Make your changes**: Follow code style guidelines
5. **Test thoroughly**: Add tests for new functionality
6. **Update docs**: If you change APIs or add features
7. **Submit PR**: Use the pull request template

## Development Setup

### Prerequisites

- **For Mobile Development**:
  - Android Studio (latest stable version)
  - JDK 17+
  - Android SDK with API level 31+ (Android 12)
  - Physical Android device or emulator

- **For Server Development**:
  - Python 3.11+
  - Docker and Docker Compose
  - Git

### Setting Up Mobile Development

```bash
# Clone your fork
git clone https://github.com/YOUR_USERNAME/limitless-companion.git
cd limitless-companion

# Open Android project in Android Studio
# File → Open → Select 'android' directory

# Or build from command line
cd android
./gradlew build

# Run tests
./gradlew test

# Run on connected device
./gradlew installDebug
```

**Sync with Gradle**: When you first open the project, Android Studio will sync Gradle dependencies automatically.

### Setting Up Server Development

```bash
# Create virtual environment
cd server
python -m venv venv

# Activate virtual environment
# On macOS/Linux:
source venv/bin/activate
# On Windows:
venv\Scripts\activate

# Install dependencies
pip install -r requirements.txt
pip install -r requirements-dev.txt

# Copy environment template
cp .env.example .env

# Start services (PostgreSQL, Ollama)
docker-compose up -d

# Run development server
uvicorn app.main:app --reload --host 0.0.0.0 --port 8000

# Run tests
pytest

# Run with coverage
pytest --cov=app tests/
```

### Project Structure

```
limitless-companion/
├── android/          # Native Android app (Kotlin)
├── server/           # FastAPI backend (Python)
├── docs/             # Documentation
├── .github/          # GitHub templates and workflows
└── plans/            # Project planning documents
```

## Code Style Guidelines

### Python (Server)

- **Follow PEP 8**: Use `black` for automatic formatting
- **Type hints required**: All function signatures must have type annotations
- **Docstrings**: Use Google-style docstrings for functions and classes
- **Max line length**: 88 characters (black default)

```python
def transcribe_audio(audio_data: bytes, model: str = "base.en") -> dict[str, Any]:
    """
    Transcribe audio using Whisper model.

    Args:
        audio_data: Raw audio bytes in WAV format
        model: Whisper model identifier (default: "base.en")

    Returns:
        Dictionary containing transcript text and metadata

    Raises:
        ValueError: If audio_data is empty or invalid format
    """
    pass
```

**Formatting**:
```bash
# Format code
black server/

# Check formatting
black --check server/

# Sort imports
isort server/

# Lint
flake8 server/
mypy server/
```

### Kotlin (Android)

- **Follow Kotlin conventions**: Use `ktlint` for formatting
- **Max line length**: 120 characters
- **Naming**:
  - Classes: PascalCase (`TranscriptionService`)
  - Functions: camelCase (`startRecording()`)
  - Constants: UPPER_SNAKE_CASE (`MAX_BUFFER_SIZE`)
- **Null safety**: Use nullable types (`?`) explicitly
- **Documentation**: KDoc comments for public APIs

```kotlin
/**
 * Service for continuous audio recording and transcription.
 *
 * This foreground service captures audio from Bluetooth or device microphone,
 * buffers it in 30-second chunks, and transcribes using whisper.cpp.
 *
 * @property audioSource The audio input source (Bluetooth or device mic)
 * @property bufferSize Size of audio buffer in bytes
 */
class TranscriptionService : Service() {
    // Implementation
}
```

**Formatting**:
```bash
# Format code
./gradlew ktlintFormat

# Check formatting
./gradlew ktlintCheck
```

### Commit Messages

Use [Conventional Commits](https://www.conventionalcommits.org/):

```
<type>(<scope>): <subject>

<body>

<footer>
```

**Types**:
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code style changes (formatting, no logic change)
- `refactor`: Code refactoring
- `test`: Adding or updating tests
- `chore`: Maintenance tasks

**Examples**:
```
feat(audio): add noise cancellation to audio pipeline

Implement spectral subtraction algorithm to reduce background noise
in audio recordings. Improves transcription accuracy by ~15% in
noisy environments.

Closes #123
```

```
fix(server): resolve database connection timeout issue

Increase connection pool size and add retry logic for transient
failures. This fixes intermittent 500 errors during high load.

Fixes #456
```

## Pull Request Process

### Before Submitting

1. **Rebase on latest main**: `git pull --rebase upstream main`
2. **Run all tests**: Ensure tests pass locally
3. **Update documentation**: If APIs or features changed
4. **Self-review**: Check your own code for obvious issues

### PR Template

When you create a PR, fill out the template:

- **Description**: What does this PR do?
- **Related Issue**: Link to issue number
- **Type of Change**: Bug fix, feature, docs, etc.
- **Testing**: How was this tested?
- **Screenshots**: If UI changes
- **Checklist**: Confirm all requirements met

### Review Process

1. **Automated checks**: GitHub Actions will run tests and linters
2. **Maintainer review**: At least one maintainer must approve
3. **Address feedback**: Make requested changes
4. **Merge**: Maintainer will merge when approved

**Expected Review Time**: 1-3 business days for initial feedback.

## Testing Requirements

### Mobile App Tests

- **Unit tests**: Required for all new business logic
- **Integration tests**: For service interactions
- **UI tests**: For critical user flows (optional but encouraged)

```bash
# Run unit tests
./gradlew test

# Run instrumented tests (requires device/emulator)
./gradlew connectedAndroidTest

# Generate coverage report
./gradlew jacocoTestReport
```

**Minimum coverage**: 70% for new code

### Server Tests

- **Unit tests**: Required for all services and utilities
- **Integration tests**: For API endpoints
- **Fixtures**: Use pytest fixtures for common setup

```bash
# Run all tests
pytest

# Run specific test file
pytest tests/test_services/test_whisper_service.py

# Run with coverage
pytest --cov=app --cov-report=html tests/

# Coverage requirement: 80% for new code
```

**Test structure**:
```python
def test_transcribe_audio_success():
    """Test successful audio transcription."""
    # Arrange
    audio_data = load_sample_audio()
    
    # Act
    result = transcribe_audio(audio_data)
    
    # Assert
    assert result["text"] is not None
    assert len(result["text"]) > 0
```

## Documentation

### When to Update Docs

- **API changes**: Update [`docs/api/api-reference.md`](docs/api/api-reference.md)
- **New features**: Update README.md and relevant guides
- **Configuration changes**: Update setup guides
- **Architecture changes**: Update [`docs/architecture.md`](docs/architecture.md)

### Documentation Style

- **Clear and concise**: Use simple language
- **Code examples**: Include working examples
- **Screenshots**: For UI features
- **Keep updated**: Docs should match current code

## Collecting Logs

### Mobile App Logs

```bash
# View logcat output filtered to app
adb logcat | grep "LimitlessCompanion"

# Save logs to file
adb logcat -d > app_logs.txt
```

### Server Logs

```bash
# View live logs
docker-compose logs -f server

# Save logs to file
docker-compose logs --no-color > server_logs.txt

# View specific service logs
docker-compose logs ollama
```

## Community

### Communication Channels

- **GitHub Issues**: Bug reports and feature requests
- **GitHub Discussions**: Questions and general discussion
- **Pull Requests**: Code contributions

### Getting Help

- Read the [README.md](README.md) and [documentation](docs/)
- Search [existing issues](https://github.com/limitless-companion/limitless-companion/issues)
- Ask in [GitHub Discussions](https://github.com/limitless-companion/limitless-companion/discussions)

### Recognition

All contributors are listed in [CONTRIBUTORS.md](CONTRIBUTORS.md). Significant contributions earn you:
- 🏆 Contributor badge on GitHub profile
- 🎖️ Credit in release notes
- 🙏 Our eternal gratitude!

---

## Quick Reference

### First Time Contributing?

1. Fork the repository
2. Clone your fork: `git clone https://github.com/YOUR_USERNAME/limitless-companion.git`
3. Create a branch: `git checkout -b feature/my-feature`
4. Make changes and commit: `git commit -m "feat: add my feature"`
5. Push to your fork: `git push origin feature/my-feature`
6. Open a Pull Request

### Need Help?

Don't hesitate to ask! We're here to help new contributors succeed.

**Happy Contributing! 🚀**
