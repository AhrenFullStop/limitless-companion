# Android Development Setup

## Prerequisites

### System Requirements
- **OS**: macOS, Windows 10/11, or Linux
- **RAM**: 16GB minimum (32GB recommended)
- **Storage**: 50GB free space for Android SDK and emulators
- **Java**: JDK 17 (required for Android Gradle Plugin 8.x)

### Required Software
- **Android Studio**: Latest stable version (Iguana or later)
- **Android SDK**: API level 31+ (Android 12)
- **Git**: Version control system

## Installation Steps

### 1. Install Android Studio

**Download**: https://developer.android.com/studio

**Installation**:
- Run the installer
- Choose "Standard" installation (includes Android SDK)
- Accept all default settings

### 2. Configure Android SDK

**Open Android Studio**:
1. Go to **File → Settings → Appearance & Behavior → System Settings → Android SDK**
2. Install the following:
   - **SDK Platforms**: Android API 34 (Android 14)
   - **SDK Tools**: Android SDK Build-Tools 34.0.0
   - **SDK Tools**: Android SDK Command-line Tools
   - **SDK Tools**: Android SDK Platform-Tools

### 3. Configure Environment Variables

**macOS - your terminal**:
```bash
# Add to ~/.zshrc or ~/.bashrc
export ANDROID_HOME=$HOME/Library/Android/sdk
export PATH=$PATH:$ANDROID_HOME/emulator
export PATH=$PATH:$ANDROID_HOME/platform-tools
```
### 4. Verify Installation

```bash
# Check Java version
java -version
# Should show: OpenJDK 17.x.x

# Check Android tools
adb version
# Should show: Android Debug Bridge version 1.x.x

# Check emulator
emulator -version
# Should show: Android emulator version 34.x.x
```

## Project Setup

### 1. Clone Repository (already done, we are here)

```bash
git clone https://github.com/limitless-companion/limitless-companion.git
cd limitless-companion
```

### 2. Open Project in Android Studio (but I want to edit from within antigravity, here)

1. **Launch Android Studio**
2. **File → Open**
3. Navigate to `limitless-companion/android` directory
4. Click **OK** to open the project

### 3. Sync Gradle

Android Studio will automatically:
- Download Gradle wrapper
- Sync project dependencies
- Index the codebase

**Monitor the sync progress** in the bottom status bar.

### 4. Configure Device/Emulator

#### Physical Device (Recommended)
1. **Enable Developer Options**:
   - Go to **Settings → About Phone**
   - Tap **Build Number** 7 times
   - Return to **Settings → Developer Options**

2. **Enable USB Debugging**:
   - **Settings → Developer Options → USB Debugging** → Enable

3. **Connect Device**:
   - Use USB cable
   - Allow USB debugging on device
   - Verify: `adb devices` shows your device

#### Emulator Setup
1. **Create AVD**:
   - **Tools → Device Manager**
   - **Create Device**
   - Choose **Pixel 7** or similar
   - Select **Android 14 (API 34)** system image
   - **Finish**

2. **Launch Emulator**:
   - Click play button in Device Manager
   - Wait for boot (may take several minutes first time)

## Build Configuration

### Gradle Properties

Create `android/local.properties` (ignored by Git):

```properties
# SDK location (auto-detected by Android Studio)
sdk.dir=/Users/your-username/Library/Android/sdk

# Build optimization
org.gradle.daemon=true
org.gradle.parallel=true
org.gradle.configureondemand=true

# Memory settings
org.gradle.jvmargs=-Xmx4096m -XX:MaxPermSize=1024m
```

### Build Variants

**Debug Build** (default):
- Includes debugging symbols
- Allows USB debugging
- Slower but development-friendly

**Release Build**:
- Optimized and minified
- Requires signing configuration
- Ready for distribution

## Development Workflow

### 1. Run the App

**Using Android Studio**:
1. Select device/emulator from toolbar
2. Click **Run** button (green play icon)
3. Wait for build and deployment

**Using Command Line**:
```bash
cd android

# Debug build
./gradlew installDebug

# Release build
./gradlew installRelease
```

### 2. Debug the App

**Logcat**:
- **View → Tool Windows → Logcat**
- Filter by package: `com.limitless.companion`
- Use search: `LimitlessCompanion`

**Debugger**:
- Set breakpoints in code
- Click **Debug** button (bug icon)
- Use debugger panel for variable inspection

### 3. Test the App

**Unit Tests**:
```bash
./gradlew test
```

**Instrumented Tests**:
```bash
./gradlew connectedAndroidTest
```

**UI Tests** (future):
```bash
./gradlew testDebugUnitTest
```

## Common Issues & Solutions

### Build Issues

**Gradle sync failed**:
```
Error: Could not find method compile() for arguments
```
**Solution**: Update Gradle plugin in project `build.gradle.kts`

**SDK not found**:
```
SDK location not found
```
**Solution**: Check `local.properties` and ANDROID_HOME environment variable

### Device Issues

**Device not recognized**:
```bash
adb kill-server
adb start-server
adb devices
```

**App crashes on launch**:
- Check Logcat for stack traces
- Verify minSdkVersion compatibility
- Check device RAM (6GB+ required)

### Performance Issues

**Slow builds**:
- Enable Gradle daemon in `gradle.properties`
- Use `org.gradle.parallel=true`
- Consider using SSD storage

**App lag**:
- Check device RAM usage
- Profile with Android Studio Profiler
- Reduce background processes

## Advanced Configuration

### Custom Build Types

Add to `android/app/build.gradle.kts`:

```kotlin
buildTypes {
    create("staging") {
        initWith(getByName("debug"))
        buildConfigField("String", "API_BASE_URL", "\"https://staging-api.example.com\"")
        manifestPlaceholders["appNameSuffix"] = " Staging"
    }
}
```

### Signing Configuration

For release builds, create `android/app/signing.properties`:

```properties
storeFile=../keystore.jks
storePassword=your-store-password
keyAlias=your-key-alias
keyPassword=your-key-password
```

### ProGuard/R8 Optimization

Enable code shrinking in `build.gradle.kts`:

```kotlin
buildTypes {
    release {
        isShrinkResources = true
        isMinifyEnabled = true
        proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
}
```

## Testing Strategy

### Unit Tests
- Test business logic in ViewModels
- Test data transformations
- Test utility functions
- **Framework**: JUnit 4 + Mockito

### Integration Tests
- Test database operations
- Test API calls
- Test service interactions
- **Framework**: JUnit 4 + Espresso

### Manual Testing Checklist

**Audio Recording**:
- [ ] Bluetooth headset connection
- [ ] Microphone fallback
- [ ] Background recording stability
- [ ] Battery impact monitoring

**Transcription**:
- [ ] whisper.cpp model loading
- [ ] 30-second chunk processing
- [ ] Accuracy validation
- [ ] Memory usage monitoring

**Sync & API**:
- [ ] HTTPS communication
- [ ] API key authentication
- [ ] Offline queue management
- [ ] Error handling and retries

## Deployment

### APK Generation

**Debug APK**:
```bash
./gradlew assembleDebug
# Output: android/app/build/outputs/apk/debug/app-debug.apk
```

**Release APK**:
```bash
./gradlew assembleRelease
# Output: android/app/build/outputs/apk/release/app-release.apk
```

### Bundle (AAB) for Play Store

```bash
./gradlew bundleRelease
# Output: android/app/build/outputs/bundle/release/app-release.aab
```

### GitHub Releases

1. **Create Release**: Go to GitHub repository → Releases → Create new release
2. **Upload APK**: Attach the generated APK file
3. **Tag Version**: Use semantic versioning (e.g., `v1.0.0`)
4. **Release Notes**: Describe changes and improvements

## Development Tips

### Code Style
- Follow Kotlin coding conventions
- Use `ktlint` for formatting: `./gradlew ktlintFormat`
- Maximum line length: 120 characters

### Performance Monitoring
- Use Android Studio Profiler for CPU/memory analysis
- Monitor battery usage with Battery Historian
- Test on various device configurations

### Security Best Practices
- Store sensitive data in EncryptedSharedPreferences
- Use HTTPS for all network communication
- Implement certificate pinning for production
- Regular dependency updates with `dependabot`

### Continuous Integration

GitHub Actions workflow (`.github/workflows/android-ci.yml`):

```yaml
name: Android CI
on: [push, pull_request]
jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: '17'
      - uses: gradle/gradle-build-action@v3
      - run: ./gradlew test
      - run: ./gradlew assembleDebug
```

## Getting Help

### Documentation
- [Android Developer Documentation](https://developer.android.com/)
- [Kotlin Documentation](https://kotlinlang.org/docs/)
- [Jetpack Compose Guide](https://developer.android.com/jetpack/compose)
