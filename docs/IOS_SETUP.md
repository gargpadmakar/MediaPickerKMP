# iOS Setup Guide

MediaPickerKMP integrates natively with Apple's `PHPickerViewController`, `UIDocumentPickerViewController`, and `UIImagePickerController`.

## Requirements
- Target iOS: **iOS 14.0+**
- Swift / Kotlin Multiplatform: iOS Arm64, iOS Simulator Arm64

## Info.plist Configuration

Add privacy usage descriptions to your iOS project `iosApp/iosApp/Info.plist`:

```xml
<!-- Required for Camera Capture -->
<key>NSCameraUsageDescription</key>
<string>This application requires access to the camera to capture photos.</string>

<!-- Required for Photo Library Access (if modifying/saving) -->
<key>NSPhotoLibraryUsageDescription</key>
<string>This application requires access to your photo library to select media.</string>

<!-- Required for Microphone (if video recording with sound) -->
<key>NSMicrophoneUsageDescription</key>
<string>This application requires microphone access when recording video.</string>
```

## Simulator Limitations
- **Camera Capture**: The iOS Simulator does not provide native camera hardware. Calling `capturePhoto()` on a simulator without a camera will return `MediaPickerResult.Error` with `MediaPickerError.OperationFailed("Camera is not available on this device.")`.
- **Photo & Document Picker**: Fully functional in simulator using mock system assets and files.

## Security Scoped Resources
When picking documents, `UIDocumentPickerViewController` returns security-scoped URLs. MediaPickerKMP automatically calls `startAccessingSecurityScopedResource()` and releases it in a `finally` block after copying or extracting metadata.
