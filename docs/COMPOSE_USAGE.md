# Compose Multiplatform Usage Guide

MediaPickerKMP provides first-class support for Compose Multiplatform via `:media-picker-compose`.

## Dependency

```kotlin
// build.gradle.kts
commonMain.dependencies {
    implementation("com.github.gargpadmakar.MediaPickerKMP:media-picker-compose:1.0.0")
}
```

## Basic Setup

Use `rememberMediaPicker()` to obtain a lifecycle-aware `MediaPicker` instance:

```kotlin
@Composable
fun MyScreen() {
    val coroutineScope = rememberCoroutineScope()
    val picker = rememberMediaPicker()
    var selectedItem by remember { mutableStateOf<MediaPickerItem?>(null) }

    Button(onClick = {
        coroutineScope.launch {
            when (val result = picker.pickImage()) {
                is MediaPickerResult.Success -> selectedItem = result.data
                is MediaPickerResult.Cancelled -> println("Cancelled")
                is MediaPickerResult.Error -> println("Error: ${result.error.message}")
            }
        }
    }) {
        Text("Pick Image")
    }
}
```

## Reactive UI State with `rememberMediaPickerState()`

For complex screens with full state observation and built-in loading/error states:

```kotlin
@Composable
fun MediaScreen() {
    val state = rememberMediaPickerState()

    Column {
        Button(onClick = { state.launchPickImage() }) {
            Text("Select Image")
        }

        when (val uiState = state.uiState) {
            is MediaPickerUiState.Loading -> CircularProgressIndicator()
            is MediaPickerUiState.Success -> {
                Text("Selected: ${uiState.items.size} items")
                SelectedMediaList(items = uiState.items)
            }
            is MediaPickerUiState.Error -> Text("Error: ${uiState.error.message}")
            is MediaPickerUiState.Cancelled -> Text("Selection was cancelled.")
            is MediaPickerUiState.Idle -> Text("No media selected yet.")
        }
    }
}
```

## Prebuilt UI Components

### 1. `MediaPickerBottomSheet`
Provides a ready-to-use Material 3 bottom sheet for choosing between camera, gallery, video, audio, and documents:

```kotlin
var showBottomSheet by remember { mutableStateOf(false) }

if (showBottomSheet) {
    MediaPickerBottomSheet(
        state = state,
        onDismissRequest = { showBottomSheet = false },
        title = "Choose Media"
    )
}
```

### 2. `MediaPickerDialog`
A modal dialog alternative for tablet or desktop layouts.
