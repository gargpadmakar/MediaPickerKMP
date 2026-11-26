package io.github.padmakargarg.mediapicker.compose

import android.content.ContextWrapper
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import io.github.padmakargarg.mediapicker.AndroidMediaPicker
import io.github.padmakargarg.mediapicker.MediaPicker

@Composable
actual fun rememberMediaPicker(): MediaPicker {
    val context = LocalContext.current
    val activity = remember(context) {
        var currentContext = context
        while (currentContext is ContextWrapper) {
            if (currentContext is ComponentActivity) {
                return@remember currentContext
            }
            currentContext = currentContext.baseContext
        }
        null
    }

    val picker = remember { AndroidMediaPicker(activity) }

    DisposableEffect(picker, activity) {
        if (activity != null) {
            picker.bind(activity)
        }
        onDispose {
            picker.unbind()
        }
    }

    return picker
}
