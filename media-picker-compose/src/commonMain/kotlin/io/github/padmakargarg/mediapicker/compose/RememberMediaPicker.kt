package io.github.padmakargarg.mediapicker.compose

import androidx.compose.runtime.Composable
import io.github.padmakargarg.mediapicker.MediaPicker

/**
 * Creates and remembers a platform-specific [MediaPicker] instance bound to the current lifecycle.
 *
 * - On Android, automatically binds to the host [androidx.activity.ComponentActivity] with lifecycle cleanup.
 * - On iOS, integrates natively with the active top-level UIViewController on the key window scene.
 */
@Composable
expect fun rememberMediaPicker(): MediaPicker
