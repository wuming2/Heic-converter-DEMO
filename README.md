
# HEIC/HEIF Image Converter
![HEIC Converter Banner](https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?auto=format&fit=crop&w=1200&h=400&q=80)


A fully localized, highly secure, and beautifully styled **HEIC/HEIF batch image converter**. This application is dedicated to providing Android users with a high-performance, zero-privacy-risk conversion experience, solving compatibility paintpoints of Apple's HEIC format on other devices and operating systems.

---

## 🎨 Design Philosophy & Core Principles

*   **Pure Local Sandbox**: We firmly believe that your photos are highly sensitive personal assets. This tool runs **entirely offline**—all decoding, EXIF metadata analysis, encoding, and file input/output operations are executed exclusively on your local physical device. No background network upload logic exists.
*   **Material 3 Aesthetic**: Adhering strictly to **Material Design 3** specifications and a seamless **Edge-to-Edge** full-screen design, the interface employs high-contrast rounded cards, fluid transition animations, and clear typographic hierarchy for a refreshing, modern look.
*   **Live Log Console**: To provide advanced users and privacy advocates with absolute peace of mind, we have built-in a **real-time terminal console** in the app. Every single I/O action, conversion progress increment, and skipped non-HEIC format detail is printed in real-time.



## 🚀 Core Features

### 1. 📂 Intelligent Image Filtering & Multi-Selection Queue
*   **Smart Format Filter**: Filters `.heic`/`.heif` files by default. Toggle "Show All Formats" off/on anytime to support importing other formats while auto-detecting and logging skipped files that are not HEIC.
*   **Dynamic Waiting Queue**: Import batches of HEIC images seamlessly. Remove individual items from the conversion queue at any time to manage the conversion scope.

### 2. ⚙️ Advanced Custom Transcoding Engine
*   **Multiple Export Targets**: Batch convert to highly compatible **JPEG**, high-quality **PNG**, or modern **WebP** formats with a single tap.
*   **Lossless & Custom Compression**: Adjust compression quality (0% to 100%) sliders to control file sizes, or enable **Lossless** mode for lossless-supporting formats.
*   **EXIF Metadata Management**:
    *   *Preservation Mode*: Retain camera model, focal length, shutter speed, and orientation metadata (including physical GPS info).
    *   *Sanitize Mode*: Strip sensitive geotags and device identifiers entirely for secure sharing.

### 3. 💾 Flexible Storage Management
*   **Default Sandbox Storage**: Fast and secure conversion. Easily export or share single/batch outputs using the Android system share sheet.
*   **Custom Save Directory**: Grant read/write access to any folder (such as SD cards, public Pictures, or specific albums) via the Android native Storage Access Framework (SAF). Converted images synchronize to your device gallery instantly.

### 4. 📝 Real-Time Terminal Output (Dynamic Terminal)
*   Powered by a fully asynchronous architecture, the background conversion thread links directly with the UI. Beautiful monospaced output displays successful counts, directory loading progress, conversion percentages, and final durations.

---

## 🛠️ Tech Stack & Engineering Practices

This application is a modern Android showcase, demonstrating excellent engineering patterns:

*   **Language**: 100% Kotlin utilizing type safety and modern language features.
*   **UI Framework**: Completely declarative UI written in **Jetpack Compose** for a high-performance, responsive UI.
*   **Architectural Pattern**: Clean **MVVM (Model-View-ViewModel)** with robust state management using `HeicConverterViewModel` and `StateFlow`.
*   **Asynchronous Concurrency**: Powered by **Kotlin Coroutines** and **Flow** to digest images concurrently off the main thread (`Dispatchers.Default` & `Dispatchers.IO`), preventing ANR even when converting hundreds of large files.
*   **Material 3 Themes**: Designed with modern Material 3 typography, dynamic color systems (`Theme.kt`), and consistent grid spacing.
*   **Privacy & Compliance**: Configured with a dedicated `PrivacyPolicyActivity` declaring the exact limits of telemetry SDKs (Firebase Analytics/Crashlytics) so the application remains highly transparent and compliant.

---

## 📖 Quick Start Guide

1.  **Add Photos**: Tap "Select Images Now" or the center card to select HEIC files using the system file picker.
2.  **Adjust Preferences**:
    *   Expand **Advanced Settings** at the bottom.
    *   Select your target format (e.g., JPEG) and adjust the quality slider.
    *   Decide whether to keep EXIF metadata.
    *   Optionally, set a **Custom Save Directory** to directly save files to a specific album.
3.  **Convert**: Tap the **Start Batch Conversion** button.
4.  **Save or Share**:
    *   If a custom folder is set, outputs are already saved to your gallery.
    *   Otherwise, tap **Batch Export & Share** below the console terminal to send outputs to other apps or friends.

---

*Protect your privacy, restore your colors. HEIC Converter is your essential offline utility companion on Android!*


---


# HEIC 转换器 | HEIC/HEIF Image Converter


一款完全本地化、高度安全且界面精美的 **HEIC/HEIF 格式图片批量转换工具**。本应用致力于为 Android 用户提供高性能、零隐私风险的图像转换体验，解决苹果生态（HEIC 格式）在其他设备与系统上的兼容性痛点。

---

## 🎨 设计理念与核心主张

*   **本地安全优先 (Pure Local Sandbox)**：我们坚信，用户的照片是极其敏感的私人资产。本工具采用**全离线运作模式**，所有解码、元数据分析、编码和路径读写操作完全在您的物理设备上运行，没有任何后台网络上传逻辑。
*   **极致视觉体验 (Material 3 Aesthetic)**：严格遵循 **Material Design 3** 规范与流畅的 **Edge-to-Edge（无边框全面屏）** 设计，利用高对比度圆角卡片、平滑过渡动画和清晰的文字层级，为用户奉上清爽、现代感十足的交互界面。
*   **全流程透明化 (Live Log Console)**：为了向高级用户和隐私倡导者提供纯粹的安全感，我们在应用中内置了**实时日志控制台**。应用每一步读写行为、转换进度以及非 HEIC 格式过滤详情均以控制台终端形式实时呈现。

---

## 🚀 核心功能亮点

### 1. 📂 智能图片筛选与多选队列
*   **智能格式过滤器**：默认开启 .heic/.heif 过滤。点击开关可以切换至“显示所有格式”，并在导入时自动识别非 HEIC 格式，提供跳过记录并日志提示。
*   **动态等待队列**：支持批量导入任意数量的 HEIC 图像，支持随时从转换列表队列中一键移除特定项，精细管控转换范围。

### 2. ⚙️ 高级自定义转码引擎
*   **多目标格式导出**：一键转换至高兼容性的 **JPEG**、高画质的 **PNG**，或兼顾体积与画质的现代 **WebP** 格式。
*   **无损品质与自定义压缩**：针对不同格式，可调整压缩品质（0%~100%）滑块以微调文件体积；对于支持无损的格式，更可开启**无损转换 (Lossless)** 模式。
*   **元数据 (EXIF) 保留与擦除**：
    *   *保留模式*：完整转移并克隆相机型号、焦距、快门时间、拍摄方向（GPS 物理信息等，视用户隐私诉求而定）。
    *   *清洗模式*：一键完全剥离照片中附带的敏感地理位置和设备身份标识，守护分享隐私。

### 3. 💾 弹性的路径管理系统
*   **默认沙盒存储**：快速安全转换，完毕后支持灵活的一键导出、单张或批量系统原生分享。
*   **自定义存储目录**：支持通过 Android 原生 Storage Access Framework (SAF) 授予任意外部存储（如 SD 卡、公共 Picture 目录、特定相册文件夹）的读写许可，转换完毕直接同步至手机相册，极速整理。

### 4. 📝 实时日志终端 (Dynamic Terminal)
*   全异步架构支撑下的后台转换线程与 UI 实时联动，将导入成功数、目标保存路径加载进度、单张转换百分比、最终耗时等以酷炫的终端字符输出，让转换不仅快捷，而且清晰可见。

---

## 🛠️ 技术栈与工程实践

本应用是一套极具学习与工程参考价值的现代 Android 项目，其关键技术特性包括：

*   **开发语言**：100% **Kotlin** 编写，深度利用其类型安全和现代语言特性。
*   **UI 框架**：使用 **Jetpack Compose** 进行完全声明式 UI 编程，实现高性能、易维护的响应式界面。
*   **架构模式**：严谨的 **MVVM (Model-View-ViewModel)**，通过 `HeicConverterViewModel` 及 `StateFlow` 实现高可靠的状态管理。
*   **异步并发**：依托 **Kotlin Coroutines（协程）** 与 **Flow**，在非主线程（Dispatchers.Default & Dispatchers.IO）中并发吞吐图片，保证在批量转换数百张大图时 UI 依然如丝般顺滑、无任何 ANR 阻碍。
*   **Material 3 深度集成**：通过 `Theme.kt`、动态配色（Dynamic Color）和统一的间距组件，展示出高颜值的 Android 34 设计。
*   **隐私与安全规范**：内置独立的 `PrivacyPolicyActivity`，详细阐述分析系统对统计分析 SDK（Firebase Analytics/Crashlytics）的使用边界，确保在应用完全离线本地处理图像的同时，应用自身状态监测处于高度合规、透明的范畴。

---

## 📖 快速上手指南

1.  **添加照片**：点击主页醒目的“选择图片 (Select Images Now)”或大卡片，通过系统安全选择器批量挑选 HEIC 照片。
2.  **设置首选项**：
    *   在主界面下方展开 **高级设置 (Advanced Settings)**。
    *   设置您的目标格式（如 JPEG）和品质滑块（如 90%）。
    *   选择是否保留 EXIF 元数据。
    *   根据需要，点击 **自定义存储目录 (Custom Save Directory)** 选择您希望直接存入的手机相册位置。
3.  **开始转换**：点击 **开始批量转换 (Start Batch Conversion)** 按钮。
4.  **保存或分享**：
    *   如果您设置了自定义文件夹，转换结果已经安稳地躺在您的目标路径和手机相册中。
    *   如果没有设置，您可以在控制台下方直接点击 **批量导出分享 (Batch Export & Share)** 将成品发送给好友或分享到其他社交软件中。

---

*保护隐私，还原色彩。HEIC 转换器将是您 Android 设备中不可或缺的离线小助手！*

---
## Run Locally
**Prerequisites:**  [Android Studio](https://developer.android.com/studio)

1. Open Android Studio
2. Select **Open** and choose the directory containing this project
3. Allow Android Studio to fix any incompatibilities as it imports the project.
4. Remove this line from the app's `build.gradle.kts` file: `signingConfig = signingConfigs.getByName("debugConfig")`
5. Run the app on an emulator or physical device
