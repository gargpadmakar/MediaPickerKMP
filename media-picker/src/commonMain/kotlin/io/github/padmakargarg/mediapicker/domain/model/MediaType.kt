package io.github.padmakargarg.mediapicker.domain.model

/**
 * High-level category of media items handled by the picker.
 */
enum class MediaType {
    IMAGE,
    VIDEO,
    AUDIO,
    DOCUMENT,
    UNKNOWN;

    companion object {
        /**
         * Resolves [MediaType] from a standard MIME type string.
         */
        fun fromMimeType(mimeType: String?): MediaType {
            if (mimeType.isNullOrBlank()) return UNKNOWN
            val normalized = mimeType.trim().lowercase()
            return when {
                normalized.startsWith("image/") -> IMAGE
                normalized.startsWith("video/") -> VIDEO
                normalized.startsWith("audio/") -> AUDIO
                normalized.startsWith("application/pdf") ||
                normalized.startsWith("application/msword") ||
                normalized.startsWith("application/vnd.") ||
                normalized.startsWith("text/") -> DOCUMENT
                else -> UNKNOWN
            }
        }
    }
}
