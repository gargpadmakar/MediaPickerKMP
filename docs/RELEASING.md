# Releasing MediaPickerKMP

This document describes the process of releasing new versions of MediaPickerKMP.

## Versioning Policy
This project follows [Semantic Versioning (SemVer)](https://semver.org/).
- **MAJOR**: Breaking changes to commonMain public APIs.
- **MINOR**: New platform features, new media types, non-breaking API additions.
- **PATCH**: Bug fixes, security patches, performance improvements.

## Publishing Targets

### 1. JitPack (Current Default)
JitPack builds directly from the GitHub repository release tag:
- Group: `com.github.gargpadmakar.MediaPickerKMP`
- Modules: `media-picker`, `media-picker-compose`
- Tag: `v1.0.0`

### 2. Maven Central (Sonatype OSSRH)
To publish to Maven Central:
1. Ensure the following environment variables or `~/.gradle/gradle.properties` entries are defined:
   ```properties
   signing.keyId=YOUR_KEY_ID
   signing.password=YOUR_GPG_PASSPHRASE
   signing.secretKeyRingFile=/path/to/secring.gpg
   ossrhUsername=YOUR_SONATYPE_USER
   ossrhPassword=YOUR_SONATYPE_TOKEN
   ```
2. Run publication:
   ```bash
   ./gradlew publishToSonatype closeAndReleaseSonatypeStagingRepository
   ```

## Release Steps Checklist
1. Verify all tests pass:
   ```bash
   ./gradlew :media-picker:allTests :media-picker-compose:allTests :sample:assembleDebug
   ```
2. Update version in `gradle.properties` or `build.gradle.kts`.
3. Update `CHANGELOG.md` with release notes and date.
4. Commit changes: `git commit -m "Release vX.Y.Z"`
5. Create tag: `git tag -a vX.Y.Z -m "Release vX.Y.Z"`
6. Push commit and tag: `git push origin main --tags`
7. Create a GitHub Release referencing the new tag.
