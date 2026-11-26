# Security Considerations

Security is a primary focus of MediaPickerKMP across both Android and iOS targets.

## 1. Path Traversal Protection
External content providers and document pickers can return malicious filenames (e.g. `../../../../etc/malicious.txt` or `..\..\Windows\system.ini`).

In `AndroidMetadataExtractor`:
- All display names are processed through `sanitizeFileName()`.
- Directory separators (`/`, `\`), traversal sequences (`../`, `..\`), and leading dots are stripped.
- Illegal or dangerous characters are substituted with safe underscores `_`.
- Before any stream byte is written to local storage, `targetFile.canonicalPath.startsWith(cacheFolder.canonicalPath)` is asserted. Any attempt to write outside the designated cache directory raises a `SecurityException`.

## 2. Temporary File & Cache Management
- Cached copies are saved in app-private storage (`context.cacheDir/media_picker_cache` on Android, `NSTemporaryDirectory()/media_picker_cache` on iOS).
- Call `MediaPicker.clearCache()` to wipe all stored copies when no longer needed.

## 3. Strict MIME Type Normalization
To prevent filter-bypass attacks through parameter stuffing or casing anomalies:
- Input MIME types are lowercased and stripped of semicolon parameters before evaluation (e.g. `application/pdf; charset=utf-8` normalizes to `application/pdf`).
- Wildcards are strictly parsed (`*`, `*/*`, and `prefix/*`).

## 4. File Size Limits
- Set `maxFileSizeBytes` in `MediaPickerConfig` to block unexpected memory exhaustion or storage overflow from oversized media files.
