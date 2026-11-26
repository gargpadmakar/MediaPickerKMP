package io.github.padmakargarg.mediapicker.domain.model

/**
 * Standard errors produced during picker operations.
 */
sealed interface MediaPickerError {
    val message: String

    data class PermissionDenied(
        override val message: String = "Permission was denied by the user."
    ) : MediaPickerError

    data class CameraUnavailable(
        override val message: String = "Camera hardware is unavailable or not accessible."
    ) : MediaPickerError

    data class FileNotFound(
        override val message: String = "The selected file was not found."
    ) : MediaPickerError

    data class UnsupportedType(
        val mimeType: String? = null,
        override val message: String = "The media type '${mimeType ?: "unknown"}' is not supported."
    ) : MediaPickerError

    data class FileTooLarge(
        val sizeInBytes: Long? = null,
        val maxSizeBytes: Long? = null,
        override val message: String = "File size (${sizeInBytes ?: 0} bytes) exceeds the maximum limit (${maxSizeBytes ?: 0} bytes)."
    ) : MediaPickerError

    data class SelectionLimitExceeded(
        val requested: Int,
        val maxAllowed: Int,
        override val message: String = "Selected $requested items, exceeding maximum allowed limit of $maxAllowed."
    ) : MediaPickerError

    data class OperationFailed(
        val cause: Throwable? = null,
        override val message: String = cause?.message ?: "The media picker operation failed."
    ) : MediaPickerError
}
