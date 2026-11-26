# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0] - 2026-11-26

### Added
- **Core Engine (`:media-picker`)**:
  - Unified Kotlin Multiplatform API (`MediaPicker`) targeting Android (API 24+) and iOS (iOS 14+).
  - Single and multiple image picker with PhotoPicker / PHPickerViewController integration.
  - Single and multiple video picker with duration, dimension, and size extraction.
  - Single and multiple audio picker with Storage Access Framework (SAF) and UIDocumentPickerViewController.
  - Document and file picker supporting custom MIME types and UTTypes.
  - Camera photo capture integration with FileProvider (Android) and UIImagePickerController (iOS).
  - Clean Architecture with Domain, Data, Bridge, and Presenter layers.
  - Mutex-based single-flight concurrency protection preventing duplicate overlapping launches.
  - Media metadata extraction (dimensions, duration, file size, MIME type detection).
  - Stream-based copy-to-cache pipeline preventing OutOfMemory errors on large assets.
- **Compose Multiplatform Integration (`:media-picker-compose`)**:
  - `rememberMediaPicker()` and `rememberMediaPickerState()` composables for declarative lifecycle-safe usage.
  - `MediaPickerBottomSheet` and `MediaPickerDialog` UI components.
  - `MediaPickerActionItem`, `MediaPreview`, and `SelectedMediaList` preview widgets.
  - Reactive `MediaPickerUiState` (`Idle`, `Loading`, `Success`, `Cancelled`, `Error`).
- **Security & Hardening**:
  - Android path traversal defense in `AndroidMetadataExtractor` blocking `../`, `..\`, and illegal characters.
  - Canonical path containment verification ensuring cached files stay strictly within cache directories.
  - Robust MIME type normalization (parameter stripping, whitespace trimming, case-insensitivity, wildcard support).
  - iOS UIViewController presentation failure detection with graceful error returns and single-continuation resume guarantees.
- **Sample Application (`:sample`)**:
  - Full Compose Multiplatform showcase demonstrating images, videos, audio, documents, and camera captures.
