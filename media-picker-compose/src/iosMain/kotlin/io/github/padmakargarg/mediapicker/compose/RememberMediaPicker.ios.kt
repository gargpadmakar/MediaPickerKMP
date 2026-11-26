package io.github.padmakargarg.mediapicker.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.github.padmakargarg.mediapicker.IOSMediaPicker
import io.github.padmakargarg.mediapicker.MediaPicker

@Composable
actual fun rememberMediaPicker(): MediaPicker {
    return remember { IOSMediaPicker() }
}
