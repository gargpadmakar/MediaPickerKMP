package io.github.padmakargarg.mediapicker.compose

import io.github.padmakargarg.mediapicker.domain.model.MediaPickerError
import io.github.padmakargarg.mediapicker.domain.model.MediaPickerItem

/**
 * Represents the distinct UI states of a media picker operation in Compose.
 */
sealed interface MediaPickerUiState {
    /**
     * Default state when no media picker operation is in progress.
     */
    data object Idle : MediaPickerUiState

    /**
     * Active state when a media picker or camera capture is currently open/launching.
     */
    data object Loading : MediaPickerUiState

    /**
     * State returned when media items have been successfully selected or captured.
     *
     * @property items The selected or captured [MediaPickerItem] elements.
     */
    data class Success(val items: List<MediaPickerItem>) : MediaPickerUiState

    /**
     * State returned when the user dismissed or cancelled the picker without selecting media.
     */
    data object Cancelled : MediaPickerUiState

    /**
     * State returned when an operation or permission failure occurred.
     *
     * @property error The detailed domain [MediaPickerError].
     */
    data class Error(val error: MediaPickerError) : MediaPickerUiState
}
