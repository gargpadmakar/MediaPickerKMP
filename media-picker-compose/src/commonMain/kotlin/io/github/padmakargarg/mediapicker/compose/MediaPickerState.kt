package io.github.padmakargarg.mediapicker.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import io.github.padmakargarg.mediapicker.MediaPicker
import io.github.padmakargarg.mediapicker.domain.model.MediaPickerConfig
import io.github.padmakargarg.mediapicker.domain.model.MediaPickerItem
import io.github.padmakargarg.mediapicker.domain.model.MediaPickerResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * State holder for Compose-based media picker operations.
 *
 * Manages reactive [uiState] and provides declarative triggers for all picker types.
 */
@Stable
class MediaPickerState(
    val mediaPicker: MediaPicker,
    private val coroutineScope: CoroutineScope
) {
    /**
     * Current reactive UI state of the media picker.
     */
    var uiState by mutableStateOf<MediaPickerUiState>(MediaPickerUiState.Idle)
        private set

    /**
     * Currently selected items if the state is [MediaPickerUiState.Success], or empty list otherwise.
     */
    val items: List<MediaPickerItem>
        get() = (uiState as? MediaPickerUiState.Success)?.items.orEmpty()

    /**
     * Returns true if a media picker operation is actively in flight.
     */
    val isLoading: Boolean
        get() = uiState is MediaPickerUiState.Loading

    /**
     * Resets the UI state back to [MediaPickerUiState.Idle].
     */
    fun clear() {
        uiState = MediaPickerUiState.Idle
    }

    /**
     * Updates the current state directly (useful for removing items or custom manipulations).
     */
    fun updateItems(updatedItems: List<MediaPickerItem>) {
        uiState = if (updatedItems.isNotEmpty()) {
            MediaPickerUiState.Success(updatedItems)
        } else {
            MediaPickerUiState.Idle
        }
    }

    private fun execute(block: suspend () -> MediaPickerResult) {
        if (uiState is MediaPickerUiState.Loading) return
        uiState = MediaPickerUiState.Loading
        coroutineScope.launch {
            val result = block()
            uiState = when (result) {
                is MediaPickerResult.Success -> MediaPickerUiState.Success(result.items)
                is MediaPickerResult.Cancelled -> MediaPickerUiState.Cancelled
                is MediaPickerResult.Error -> MediaPickerUiState.Error(result.error)
            }
        }
    }

    fun pickImage(config: MediaPickerConfig = MediaPickerConfig.DEFAULT) =
        execute { mediaPicker.pickImage(config) }

    fun pickMultipleImages(config: MediaPickerConfig = MediaPickerConfig.DEFAULT) =
        execute { mediaPicker.pickMultipleImages(config) }

    fun pickVideo(config: MediaPickerConfig = MediaPickerConfig.DEFAULT) =
        execute { mediaPicker.pickVideo(config) }

    fun pickMultipleVideos(config: MediaPickerConfig = MediaPickerConfig.DEFAULT) =
        execute { mediaPicker.pickMultipleVideos(config) }

    fun pickMedia(config: MediaPickerConfig = MediaPickerConfig.DEFAULT) =
        execute { mediaPicker.pickMedia(config) }

    fun capturePhoto(config: MediaPickerConfig = MediaPickerConfig.DEFAULT) =
        execute { mediaPicker.capturePhoto(config) }

    fun captureVideo(config: MediaPickerConfig = MediaPickerConfig.DEFAULT) =
        execute { mediaPicker.captureVideo(config) }

    fun pickFile(config: MediaPickerConfig = MediaPickerConfig.DEFAULT) =
        execute { mediaPicker.pickFile(config) }

    fun pickMultipleFiles(config: MediaPickerConfig = MediaPickerConfig.DEFAULT) =
        execute { mediaPicker.pickMultipleFiles(config) }

    fun pickAudio(config: MediaPickerConfig = MediaPickerConfig.DEFAULT) =
        execute { mediaPicker.pickAudio(config) }

    fun pickMultipleAudio(config: MediaPickerConfig = MediaPickerConfig.DEFAULT) =
        execute { mediaPicker.pickMultipleAudio(config) }
}

/**
 * Creates and remembers a [MediaPickerState] instance bound to the composable lifecycle.
 */
@Composable
fun rememberMediaPickerState(
    mediaPicker: MediaPicker = rememberMediaPicker(),
    coroutineScope: CoroutineScope = rememberCoroutineScope()
): MediaPickerState {
    return remember(mediaPicker, coroutineScope) {
        MediaPickerState(mediaPicker, coroutineScope)
    }
}
