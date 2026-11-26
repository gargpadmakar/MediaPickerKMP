package io.github.padmakargarg.mediapicker.domain.model

/**
 * Sealed result type representing the outcome of a media picker operation.
 */
sealed interface MediaPickerResult {
    /**
     * Operation succeeded with one or more picked items.
     */
    data class Success(val items: List<MediaPickerItem>) : MediaPickerResult {
        val firstOrNull: MediaPickerItem? get() = items.firstOrNull()
    }

    /**
     * User cancelled the picker without selecting media.
     */
    data object Cancelled : MediaPickerResult

    /**
     * Operation failed with a [MediaPickerError].
     */
    data class Error(val error: MediaPickerError) : MediaPickerResult
}

inline fun MediaPickerResult.onSuccess(action: (items: List<MediaPickerItem>) -> Unit): MediaPickerResult {
    if (this is MediaPickerResult.Success) action(items)
    return this
}

inline fun MediaPickerResult.onCancelled(action: () -> Unit): MediaPickerResult {
    if (this is MediaPickerResult.Cancelled) action()
    return this
}

inline fun MediaPickerResult.onError(action: (error: MediaPickerError) -> Unit): MediaPickerResult {
    if (this is MediaPickerResult.Error) action(error)
    return this
}
