# Architecture Overview

MediaPickerKMP adheres to Clean Architecture and MVVM principles across Kotlin Multiplatform targets.

## Layer Structure

```mermaid
flowchart TD
    subgraph Presentation["Presentation Layer (:media-picker-compose)"]
        UI["Compose Screens<br/>& Components"]
        State["MediaPickerState<br/>(Reactive StateFlow)"]
        UI --> State
    end

    subgraph Domain["Domain Layer (:media-picker commonMain)"]
        Interface["MediaPicker<br/>Interface"]
        Default["DefaultMediaPicker<br/>(Mutex Guard)"]
        Config["MediaPickerConfig<br/>& Result Models"]
        Interface --> Default
    end

    subgraph Data["Data Layer (:media-picker commonMain)"]
        Repo["MediaPicker<br/>Repository"]
        RepoImpl["Repository Impl<br/>(Validation)"]
        Bridge["Platform Bridge<br/>(expect / actual)"]
        Repo --> RepoImpl
        RepoImpl --> Bridge
    end

    subgraph Android["Android Platform (androidMain)"]
        ABridge["Android Bridge"]
        ALauncher["Activity Result<br/>Launcher"]
        AExtractor["Metadata Extractor<br/>& Stream Cacher"]
        ABridge --> ALauncher
        ALauncher --> AExtractor
    end

    subgraph IOS["iOS Platform (iosMain)"]
        IBridge["iOS Bridge"]
        IPresenter["ViewController<br/>Presenter"]
        IExtractor["iOS Metadata<br/>& Scoped URL"]
        IBridge --> IPresenter
        IPresenter --> IExtractor
    end

    Presentation --> Domain
    Domain --> Data
    Bridge --> Android
    Bridge --> IOS
```

## Core Design Principles

1. **Platform Independence**: `commonMain` contains zero platform-specific types.
2. **Single-Flight Operation Mutex**: `DefaultMediaPicker` guards all pick operations using a coroutine mutex. Overlapping requests immediately fail with `MediaPickerError.OperationFailed` rather than corrupting state.
3. **Stream-Based I/O**: Files copied to cache are streamed with fixed buffers (8 KB), ensuring constant memory usage even with gigabyte-sized video or audio files.
4. **Lifecycle-Safe Presentation**: Android uses `ActivityResultLauncher` registered on Activity creation or Composable composition. iOS uses `IOSViewControllerPresenter` to present over the active `UIWindowScene` on the main queue with immediate failure fallback if no window is active.
