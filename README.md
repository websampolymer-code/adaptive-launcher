# Adaptive Launcher

A lightweight, fast, privacy-focused Android home-screen launcher that automatically organizes your apps by usage patterns.

## Features

- **Home Screen** with multiple pages, configurable grid, and dock
- **App Drawer** with search, alphabetical sorting, and fast scrolling
- **Smart Folders** that automatically sort apps by usage frequency, recency, and duration
- **Manual Folders** for user-controlled organization
- **Pinned Apps** that stay in fixed positions regardless of usage
- **Ranking Presets**: Balanced, Most Frequently Opened, Most Time Spent, Recently Used, Habit-Based, Custom
- **Privacy-First**: No analytics, no tracking, no uploads. All data stays on device
- **Material 3** design with light, dark, and system themes
- **Configurable** grid size, icon size, and ranking parameters

## Development Status

**Phase 1 Complete:**
- Valid Android launcher registration
- Home screen with multiple pages
- App drawer with search
- Application launching
- Basic folder creation
- Manual folder ordering
- Local persistence (Room + DataStore)
- GitHub Actions debug build

**Phase 2 (Planned):**
- Usage Access onboarding
- Usage statistics collection
- Ranking engine
- Smart folder sorting
- Ranking presets
- Pinned applications

**Phase 3 (Planned):**
- Drag-and-drop improvements
- Dock
- Folder customization
- Hidden applications
- Ranking explanation UI

**Phase 4 (Planned):**
- Backup and restore
- Accessibility improvements
- Performance optimization
- Release workflow
- Expanded test coverage

## Architecture

```
com.adaptivelauncher.app/
├── data/
│   ├── db/          # Room database, DAOs, entities
│   ├── preferences/ # DataStore preferences
│   └── repository/  # Data repositories
├── domain/
│   ├── model/       # Domain models
│   └── usecase/     # Use cases
├── ranking/         # Ranking engine
├── ui/
│   ├── home/        # Home screen
│   ├── drawer/      # App drawer
│   ├── folders/     # Folder dialogs
│   ├── settings/    # Settings screen
│   ├── onboarding/  # First-run flow
│   └── theme/       # Material 3 theme
└── usage/           # Usage statistics tracker
```

## Requirements

- Android 8.0 (API 26) or higher
- No root access required
- No special permissions for basic functionality

## How Launcher Permissions Work

### Default Home App
Adaptive Launcher registers as a home screen application. When selected as the default home app, it replaces the system launcher.

### Usage Access (Optional)
Usage Access permission enables smart folder sorting based on app usage patterns. Without this permission:
- The launcher still functions normally
- Manual folders work
- Smart ranking is disabled
- You can grant this permission later in Settings

**To grant Usage Access:**
1. Open Settings
2. Go to Apps → Special app access → Usage access
3. Find Adaptive Launcher
4. Enable "Allow usage tracking"

**To set as default home app:**
1. Open Settings
2. Go to Apps → Default apps → Home app
3. Select Adaptive Launcher

## Smart Folder Ranking Algorithm

### Default Formula

```
SmartScore =
  0.35 × FrequencyScore +
  0.25 × DurationScore +
  0.25 × RecencyScore +
  0.10 × ConsistencyScore +
  0.05 × LauncherInteractionScore
```

### Score Components

- **FrequencyScore**: Logarithmic normalization of launch count
- **DurationScore**: Logarithmic normalization of foreground duration
- **RecencyScore**: Exponential time decay based on last usage
- **ConsistencyScore**: Percentage of active days in period
- **LauncherInteractionScore**: Proportion of launches from this launcher

### Ranking Presets

| Preset | Frequency | Duration | Recency | Consistency | Launcher |
|--------|-----------|----------|---------|-------------|----------|
| Balanced | 35% | 25% | 25% | 10% | 5% |
| Most Frequently Opened | 60% | 10% | 20% | 5% | 5% |
| Most Time Spent | 15% | 60% | 15% | 5% | 5% |
| Recently Used | 15% | 10% | 60% | 10% | 5% |
| Habit-Based | 25% | 15% | 20% | 35% | 5% |

## Privacy Principles

- **No advertising SDK**
- **No analytics SDK**
- **No remote usage tracking**
- **No account required**
- **No cloud service required**
- **No upload of application lists**
- **No upload of usage history**
- **No internet permission**
- **All configuration stored locally**

## Building

### Command Line

```bash
# Clone the repository
git clone https://github.com/your-username/adaptive-launcher.git
cd adaptive-launcher

# Build debug APK
./gradlew assembleDebug

# Run tests
./gradlew testDebugUnitTest

# Run lint
./gradlew lint
```

### Visual Studio Code

1. Install the "Extension Pack for Java" extension
2. Install the "Gradle for Java" extension
3. Open the project folder
4. Use the Gradle sidebar to run tasks

### Output

The debug APK will be at:
```
app/build/outputs/apk/debug/app-debug.apk
```

## Running Tests

```bash
# Unit tests
./gradlew testDebugUnitTest

# Test reports
open app/build/reports/tests/testDebugUnitTest/index.html
```

## GitHub Actions

### Debug Build

The workflow runs automatically on:
- Push to `main` branch
- Pull requests targeting `main`
- Manual workflow dispatch

### Download APK Artifact

1. Open the GitHub repository
2. Click the "Actions" tab
3. Select the latest successful workflow
4. Scroll to the "Artifacts" section
5. Download `adaptive-launcher-debug-apk`
6. Extract the ZIP file
7. Install the APK on your Android device

### Release Build

To build a signed release:

1. Go to Actions → Release Android
2. Click "Run workflow"
3. Enter version name and code
4. Click "Run workflow"

**Configure Release Signing:**

Add these secrets to your GitHub repository:

| Secret | Description |
|--------|-------------|
| `ANDROID_KEYSTORE_BASE64` | Base64-encoded keystore file |
| `ANDROID_KEYSTORE_PASSWORD` | Keystore password |
| `ANDROID_KEY_ALIAS` | Key alias |
| `ANDROID_KEY_PASSWORD` | Key password |

To encode your keystore:
```bash
base64 -i your-keystore.jks | tr -d '\n'
```

## Known Limitations

- Widget support is not yet implemented
- Drag-and-drop reordering is basic
- Work profile apps may not appear in all cases
- Some launcher features require Usage Access

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Ensure all tests pass
6. Submit a pull request

## License

MIT License - see [LICENSE](LICENSE) for details
