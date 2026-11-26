package io.github.padmakargarg.mediapicker.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.padmakargarg.mediapicker.AndroidMediaPicker
import io.github.padmakargarg.mediapicker.compose.rememberMediaPickerPlaceholder

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val picker = AndroidMediaPicker()

        setContent {
            MaterialTheme {
                val placeholder = rememberMediaPickerPlaceholder()
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("MediaPickerKMP Sample - Platform: ${picker.platformName}, Module: $placeholder")
                }
            }
        }
    }
}
