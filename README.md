# Block Subtitle (悬浮字幕遮挡器)

## 项目概述

这是一款极简的安卓应用，其唯一功能是在屏幕上创建一个可自由移动和调整大小的悬浮窗，用于遮挡视频字幕或其他内容。

## 核心功能与要求

- **启动方式**：点击应用图标后，无需进入任何主界面，直接请求权限并创建悬浮窗。
- **窗口外观**：
  - 默认颜色：浅灰色 (Light Gray)。
  - 初始尺寸：启动时为一个小的默认尺寸，用户可立即调整。
- **交互操作**：
  - **移动**：通过拖拽窗口任意位置自由移动。
  - **调整大小**：通过捏合手势或拖拽边角自由缩放。
  - **关闭**：通过双击悬浮窗任意位置来关闭它并停止服务。
- **排除的功能**：
  - 无需锁定功能。
  - 无需预设尺寸选项。

## 技术实现概要

1.  **项目设置**：
    *   使用 "No Activity" 模板创建项目。
    *   在 `AndroidManifest.xml` 中声明 `SYSTEM_ALERT_WINDOW` 权限。
2.  **权限处理**：
    *   创建一个透明的 `LauncherActivity` 作为入口。
    *   检查并请求 `Settings.canDrawOverlays()` 权限。
    *   权限授予后，启动 `FloatingWindowService` 并关闭自身。
3.  **前台服务 (`FloatingWindowService`)**：
    *   管理悬浮窗的生命周期。
    *   提升为前台服务以提高稳定性，需创建通知渠道和持久通知。
4.  **悬浮窗创建与显示**：
    *   使用 `WindowManager` 添加视图。
    *   布局文件 `floating_view.xml` 定义浅灰色背景视图。
    *   `WindowManager.LayoutParams` 配置窗口属性。
5.  **核心交互逻辑**：
    *   `OnTouchListener` 和 `GestureDetector` 处理手势。
    *   `ACTION_MOVE` 实现拖拽移动。
    *   `ScaleGestureDetector` 实现捏合缩放。
    *   `onDoubleTap` 实现双击关闭服务。
6.  **资源清理**：
    *   `onDestroy()` 中移除悬浮窗视图，防止内存泄漏。
7.  **增强功能 (可选)**：
    *   使用 `SharedPreferences` 保存和恢复窗口位置与大小。
    *   长按调节透明度。
    *   添加细边框作为视觉提示。

## 开发步骤

(详细步骤见 `app/src/main/...` 中的代码实现)

## 项目结构

```
blockSubtitle/
├── app/
│   ├── build.gradle
│   └── src/
│       └── main/
│           ├── AndroidManifest.xml
│           ├── java/
│           │   └── com/example/blocksubtitle/
│           │       ├── activity/
│           │       │   └── LauncherActivity.java
│           │       ├── service/
│           │       │   └── FloatingWindowService.java
│           │       └── util/
│           │           └── WindowStateHelper.java
│           └── res/
│               ├── drawable/
│               │   └── border.xml
│               ├── layout/
│               │   └── floating_view.xml
│               ├── mipmap-*/ 
│               │   └── ic_launcher*.png (占位符)
│               ├── values/
│               │   ├── strings.xml
│               │   └── styles.xml
│               └── xml/
├── build.gradle
├── gradle/
│   └── wrapper/
│       └── gradle-wrapper.properties
├── gradle.properties
├── gradlew
├── gradlew.bat
└── settings.gradle
```

## 核心代码说明

### 1. `LauncherActivity.java`

*   **作用**：应用的唯一入口点。它是一个透明的Activity，不显示任何UI。
*   **流程**：
    1.  检查是否已获得 `SYSTEM_ALERT_WINDOW` (悬浮窗) 权限。
    2.  如果没有权限，启动系统设置页面请求权限。
    3.  如果已有权限，直接启动 `FloatingWindowService` 前台服务。
    4.  启动服务后，立即调用 `finish()` 关闭自己。

### 2. `FloatingWindowService.java`

*   **作用**：核心服务，负责创建、管理和销毁悬浮窗。
*   **关键点**：
    *   **前台服务**：通过 `startForeground()` 启动，以提高进程优先级，防止被系统轻易杀死。需要创建通知渠道和通知。
    *   **WindowManager**：使用 `WindowManager` 来添加、更新和移除悬浮窗视图。
    *   **手势处理**：
        *   `OnTouchListener`：处理基本的触摸事件，如 `ACTION_DOWN` 和 `ACTION_MOVE`，实现窗口拖动。
        *   `ScaleGestureDetector`：检测双指捏合手势，实现窗口缩放。
        *   `GestureDetector`：检测双击手势，用于关闭服务。
    *   **状态保存**：在 `onDestroy()` 中，使用 `WindowStateHelper` 将窗口的最后位置和大小保存到 `SharedPreferences`，以便下次启动时恢复。
    *   **资源清理**：在 `onDestroy()` 中，必须调用 `windowManager.removeView(floatingView)` 来移除窗口，防止内存泄漏。

### 3. `WindowStateHelper.java`

*   **作用**：工具类，用于将悬浮窗的尺寸和位置信息持久化到 `SharedPreferences`，并在应用重启后恢复。

### 4. `floating_view.xml`

*   **作用**：定义悬浮窗的布局。当前是一个简单的 `FrameLayout`，背景为浅灰色，并带有一个细边框。

### 5. `border.xml`

*   **作用**：一个 Drawable 资源，定义了浅灰色背景和一个灰色细边框，作为悬浮窗的视觉提示。

## 构建与运行

1.  **环境**：确保已安装 Android Studio 和 Android SDK。
2.  **导入项目**：在 Android Studio 中选择 "Open an existing project" 并选择此项目根目录。
3.  **同步 Gradle**：打开项目后，Android Studio 会自动同步 Gradle 文件。如果未自动同步，可以点击 "Sync Now" 或使用菜单 `File > Sync Project with Gradle Files`。
4.  **构建**：点击 `Build > Make Project` 或使用快捷键 `Ctrl+F9` (Windows/Linux) / `Cmd+F9` (Mac)。
5.  **运行**：连接安卓设备或启动模拟器，点击 `Run > Run 'app'` 或使用快捷键 `Shift+F10` (Windows/Linux) / `Ctrl+R` (Mac)。

## 注意事项

*   **权限**：应用启动后会请求悬浮窗权限，用户必须手动授权。
*   **兼容性**：代码中已处理不同 Android 版本（特别是 Android 8.0+）的 `WindowManager.LayoutParams.TYPE` 和通知渠道的差异。
*   **资源**：项目中使用的图标 (`ic_launcher*.png`) 是占位符，实际开发中需要替换为真实的图标文件。
*   **扩展性**：代码结构清晰，易于扩展新功能，例如长按调节透明度、添加更多交互手势等。