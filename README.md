An Android application that creates a resizable floating window (overlay) that can be used to block subtitles or other UI elements on the screen.

## Features

- Creates a floating window that overlays on top of other apps
- Resizable window with draggable edges
- Movable window by dragging the center area
- Double-tap to close the floating window
- Remembers window position and size between sessions
- Runs as a foreground service with a notification
- Supports Android 6.0 (API 23) and above

## How It Works

1. When launched, the app requests the necessary "Draw over other apps" permission
2. After permission is granted, it starts a foreground service that creates a floating window
3. The floating window:
   - Has a semi-transparent gray background with a border for visibility
   - Can be moved by dragging the center area
   - Can be resized by dragging any edge or corner
   - Can be closed by double-tapping anywhere on the window
4. Window position and size are saved automatically when the service is stopped

## Installation

### Prerequisites

- Android device running Android 6.0 (API 23) or higher
- USB debugging enabled for sideloading (if not installing from Play Store)

### Building from Source

1. Clone this repository
2. Open the project in Android Studio
3. Build the project:
   ```
   ./gradlew build
   ```
4. Install the APK on your device:
   ```
   ./gradlew installDebug
   ```

### Direct APK Installation

If you have a pre-built APK, simply transfer it to your device and open it to install.

## Usage

1. Launch the app
2. Grant the "Draw over other apps" permission when prompted
3. A floating gray window will appear on your screen
4. Move the window by dragging the center area
5. Resize the window by dragging any edge or corner
6. Double-tap anywhere on the window to close it

## Technical Details

### Architecture

- **LauncherActivity**: Transparent activity that handles permission requests and starts the service
- **FloatingWindowService**: Foreground service that manages the floating window lifecycle
- **WindowStateHelper**: Utility class that saves and restores window position and size

### Key Components

- Uses `WindowManager` to create and manage the floating window
- Implements touch listeners for moving and resizing functionality
- Uses `GestureDetector` to detect double-tap for closing the window
- Saves window state using `SharedPreferences`
- Runs as a foreground service with a notification to prevent the system from killing it

### Permissions

- `SYSTEM_ALERT_WINDOW`: Required to draw over other apps
- `FOREGROUND_SERVICE`: Required to run the foreground service
- `FOREGROUND_SERVICE_SPECIAL_USE`: Required for the special use case of overlay window

## Development

### Project Structure

```
app/
├── src/main/java/com/example/blocksubtitle/
│   ├── activity/
│   │   └── LauncherActivity.java
│   ├── service/
│   │   └── FloatingWindowService.java
│   └── util/
│       └── WindowStateHelper.java
├── src/main/res/
│   ├── layout/
│   │   └── floating_view.xml
│   ├── drawable/
│   │   └── border.xml
│   └── values/
│       ├── strings.xml
│       └── styles.xml
└── build.gradle
```

### Dependencies

- AndroidX AppCompat
- AndroidX Core
- AndroidX ConstraintLayout
- Material Components for Android

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a pull request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details (if available).
