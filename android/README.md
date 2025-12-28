# Limitless Companion - Android App

Native Android application for Limitless Companion, a privacy-first voice transcription assistant.

## Architecture

This app follows **Clean Architecture** principles with the following layers:

```
├── domain/           # Business logic (Use Cases, Models)
├── data/             # Data layer (Repository, API, Database)
├── ui/               # Presentation layer (Compose UI, ViewModels)
├── services/         # Background services (Audio, Transcription)
└── util/             # Utilities and helpers
```

## Key Components

### Audio Pipeline
- **AudioCaptureService**: Bluetooth SCO audio recording
- **TranscriptionService**: Foreground service managing transcription lifecycle
- **WhisperService**: JNI wrapper for whisper.cpp integration

### Data Management
- **Room Database**: Local SQLite with encryption
- **Repository Pattern**: Abstraction over local and remote data sources
- **WorkManager**: Background sync and retry logic

### UI Architecture
- **Jetpack Compose**: Declarative UI framework
- **Material Design 3**: Modern Android design system
- **Clean Architecture**: Separation of concerns with ViewModels

## Development Setup

### Prerequisites
- Android Studio Iguana or later
- JDK 17
- Android SDK API 34
- Physical Android device (recommended) or emulator

### Getting Started

1. **Open in Android Studio**:
   ```
   File → Open → Select 'android' directory
   ```

2. **Sync Gradle**:
   - Android Studio will automatically sync dependencies
   - Monitor progress in bottom status bar

3. **Configure Device**:
   - Enable Developer Options and USB Debugging
   - Connect device or launch emulator

4. **Build and Run**:
   - Select device from toolbar
   - Click Run button (green play icon)

## Project Structure

```
android/
├── app/
│   ├── src/main/java/com/limitless/companion/
│   │   ├── MainActivity.kt              # App entry point
│   │   ├── services/                    # Background services
│   │   │   ├── TranscriptionService.kt  # Main transcription service
│   │   │   ├── AudioCaptureService.kt   # Audio recording
│   │   │   └── WhisperService.kt        # Whisper JNI wrapper
│   │   ├── ui/                          # UI layer
│   │   │   ├── screens/                 # Compose screens
│   │   │   └── components/              # Reusable UI components
│   │   ├── data/                        # Data layer
│   │   │   ├── local/                   # Local storage (Room, SharedPrefs)
│   │   │   ├── remote/                  # API clients
│   │   │   └── repository/              # Repository implementations
│   │   ├── domain/                      # Domain layer
│   │   │   ├── model/                   # Data models
│   │   │   └── usecase/                 # Business logic
│   │   ├── util/                        # Utilities
│   │   └── whisper/                     # Whisper integration
│   │       └── jni/                     # JNI bindings
│   ├── src/main/res/                    # Resources
│   └── src/test/                        # Unit tests
├── gradle/wrapper/                      # Gradle wrapper
├── build.gradle.kts                     # Root build config
├── settings.gradle.kts                  # Project settings
└── gradle.properties                    # Gradle properties
```

## Key Technologies

### Core Android
- **Kotlin**: Primary programming language
- **Jetpack Compose**: UI framework
- **Room**: Local database
- **WorkManager**: Background tasks
- **Koin**: Dependency injection

### Audio & ML
- **whisper.cpp**: On-device speech-to-text
- **JNI**: Native code integration
- **Android AudioRecord**: Audio capture

### Networking & Security
- **Retrofit**: HTTP client
- **OkHttp**: Networking library
- **EncryptedSharedPreferences**: Secure local storage

## Building Features

### Audio Recording
- Bluetooth SCO audio routing
- Fallback to device microphone
- 30-second chunk processing
- Background service management

### Transcription
- whisper.cpp integration via JNI
- On-device processing (no network required)
- GGML quantized models
- Real-time confidence scoring

### Data Synchronization
- Local SQLite caching
- Background upload to server
- Conflict resolution
- Offline-first design

### Action Detection
- Push notifications for detected actions
- User acceptance/rejection flow
- Deep linking to action handlers

## Testing Strategy

### Unit Tests
- Business logic in ViewModels
- Data transformations
- Utility functions
- **Framework**: JUnit 4 + Mockito

### Integration Tests
- Database operations
- API calls
- Service interactions
- **Framework**: JUnit 4 + Espresso

### Manual Testing
- Audio recording stability
- Bluetooth connectivity
- Battery usage monitoring
- UI responsiveness

## Performance Considerations

### Battery Optimization
- Foreground service with notification
- Doze mode exemptions (selective)
- Audio processing optimization
- Background task scheduling

### Memory Management
- whisper.cpp context reuse
- Audio buffer management
- Image resource optimization
- Leak prevention

### Storage Efficiency
- Audio compression before upload
- Transcript text optimization
- Cache size limits
- Automatic cleanup

## Deployment

### APK Generation

**Debug APK** (development):
```bash
./gradlew assembleDebug
# Output: app/build/outputs/apk/debug/app-debug.apk
```

**Release APK** (production):
```bash
./gradlew assembleRelease
# Output: app/build/outputs/apk/release/app-release.apk
```

### Bundle (AAB) for Google Play
```bash
./gradlew bundleRelease
# Output: app/build/outputs/bundle/release/app-release.aab
```

## Contributing

### Code Style
- Follow Kotlin coding conventions
- Use `ktlint` for formatting
- Maximum line length: 120 characters
- KDoc documentation for public APIs

### Testing Requirements
- Unit test coverage: 70% minimum
- Integration tests for critical paths
- Manual testing checklist completion

### Pull Request Process
1. Create feature branch
2. Implement with tests
3. Update documentation
4. Submit PR with description
5. Address review feedback

## Troubleshooting

### Common Issues

**Build Failures**:
- Clean and rebuild: `./gradlew clean build`
- Invalidate caches: File → Invalidate Caches / Restart
- Check JDK version compatibility

**Device Connection Issues**:
- Verify USB debugging enabled
- Try different USB cable/port
- Check device storage space

**Audio Problems**:
- Verify microphone permissions
- Test Bluetooth headset pairing
- Check audio focus conflicts

**Performance Issues**:
- Monitor memory usage in profiler
- Check for background processes
- Verify whisper model loading

### Getting Help

- **Documentation**: Check `/docs` directory
- **Issues**: GitHub Issues for bugs
- **Discussions**: GitHub Discussions for questions
- **Contributing Guide**: See root `/CONTRIBUTING.md`

## Future Enhancements

- **Real-time streaming**: Continuous transcription
- **Multi-language support**: Additional Whisper models
- **Advanced diarization**: Speaker identification
- **Offline search**: Local RAG implementation
- **Widget support**: Home screen recording controls