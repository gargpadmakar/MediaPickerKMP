package io.github.padmakargarg.mediapicker.domain.usecase

import io.github.padmakargarg.mediapicker.domain.model.MediaPickerConfig
import io.github.padmakargarg.mediapicker.domain.model.MediaPickerError
import io.github.padmakargarg.mediapicker.domain.model.MediaPickerItem
import io.github.padmakargarg.mediapicker.domain.model.MediaPickerResult

/**
 * Domain-level validator enforcing business constraints across all picker operations.
 */
object MediaPickerValidator {

    /**
     * Validates picked items against [config].
     */
    fun validateResult(result: MediaPickerResult, config: MediaPickerConfig): MediaPickerResult {
        if (result !is MediaPickerResult.Success) return result

        val items = result.items

        // Selection limit validation
        if (config.allowMultiple && items.size > config.maxSelection) {
            return MediaPickerResult.Error(
                MediaPickerError.SelectionLimitExceeded(
                    requested = items.size,
                    maxAllowed = config.maxSelection
                )
            )
        }

        if (!config.allowMultiple && items.size > 1) {
            return MediaPickerResult.Error(
                MediaPickerError.SelectionLimitExceeded(
                    requested = items.size,
                    maxAllowed = 1
                )
            )
        }

        // File size and MIME type validation
        for (item in items) {
            val itemSize = item.sizeInBytes
            if (config.maxFileSizeBytes != null && itemSize != null && itemSize > config.maxFileSizeBytes) {
                return MediaPickerResult.Error(
                    MediaPickerError.FileTooLarge(
                        sizeInBytes = itemSize,
                        maxSizeBytes = config.maxFileSizeBytes
                    )
                )
            }

            if (config.allowedMimeTypes.isNotEmpty() && !config.isMimeTypeAllowed(item.mimeType)) {
                return MediaPickerResult.Error(
                    MediaPickerError.UnsupportedType(
                        mimeType = item.mimeType
                    )
                )
            }
        }

        return result
    }
}
