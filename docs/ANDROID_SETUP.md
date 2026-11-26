# Android Setup Guide

MediaPickerKMP leverages modern Android Activity Result APIs, the AndroidX Photo Picker, and Storage Access Framework (SAF).

## Minimum SDK
- Minimum SDK: **API 24 (Android 7.0)**
- Compile SDK: **API 36**
- Target SDK: **API 36**

## AndroidManifest Configuration

For camera photo capture, declare the camera feature and configure a `FileProvider`.

### 1. Permissions & Features (Optional for Gallery, Required for Camera)
```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-feature
        android:name="android.hardware.camera"
        android:required="false" />

    <application>
        <!-- FileProvider for secure camera capture output -->
        <provider
            android:name="androidx.core.content.FileProvider"
            android:authorities="${applicationId}.mediapicker.fileprovider"
            android:exported="false"
            android:grantUriPermissions="true">
            <meta-data
                android:name="android.support.FILE_PROVIDER_PATHS"
                android:resource="@xml/media_picker_file_paths" />
        </provider>
    </application>
</manifest>
```

### 2. FileProvider Paths (`res/xml/media_picker_file_paths.xml`)
Create `res/xml/media_picker_file_paths.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<paths xmlns:android="http://schemas.android.com/apk/res/android">
    <cache-path name="media_picker_cache" path="media_picker_cache/" />
    <cache-path name="camera_captures" path="camera_captures/" />
</paths>
```

## Initializing in Android
When using Compose Multiplatform:
```kotlin
val picker = rememberMediaPicker()
```
When using without Compose:
```kotlin
val picker = AndroidMediaPicker()
picker.register(activity)
```
