package io.github.padmakargarg.mediapicker.domain.model

/**
 * Configuration options for picker operations.
 *
 * @property allowMultiple Whether multiple media selection is permitted.
 * @property maxSelection Maximum items permitted when allowMultiple is true.
 * @property allowedMimeTypes Filter list of permitted MIME types (e.g. "image/jpeg", "application/pdf"). Empty means all permitted.
 * @property maxFileSizeBytes Maximum allowable file size in bytes. Null means no size limit.
 * @property copyToCache Whether selected files should be copied to the local app cache directory.
 * @property extractMetadata Whether dimensions and duration metadata should be extracted where possible.
 */
data class MediaPickerConfig(
    val allowMultiple: Boolean = false,
    val maxSelection: Int = 1,
    val allowedMimeTypes: List<String> = emptyList(),
    val maxFileSizeBytes: Long? = null,
    val copyToCache: Boolean = false,
    val extractMetadata: Boolean = true
) {
    init {
        require(maxSelection >= 1) { "maxSelection must be at least 1, but was $maxSelection" }
        maxFileSizeBytes?.let {
            require(it > 0) { "maxFileSizeBytes must be greater than 0, but was $it" }
        }
    }

    /**
     * Validates whether a given MIME type is acceptable based on allowed MIME types.
     */
    fun isMimeTypeAllowed(mimeType: String?): Boolean {
        if (allowedMimeTypes.isEmpty()) return true
        if (mimeType.isNullOrBlank()) return false
        val normalized = mimeType.trim().lowercase()

        return allowedMimeTypes.any { pattern ->
            val p = pattern.trim().lowercase()
            when {
                p == "*" || p == "*/*" -> true
                p.endsWith("/*") -> {
                    val prefix = p.substringBefore('/')
                    normalized.startsWith("$prefix/")
                }
                else -> normalized == p
            }
        }
    }

    companion object {
        val DEFAULT = MediaPickerConfig()

        fun single(
            allowedMimeTypes: List<String> = emptyList(),
            maxFileSizeBytes: Long? = null,
            copyToCache: Boolean = false,
            extractMetadata: Boolean = true
        ) = MediaPickerConfig(
            allowMultiple = false,
            maxSelection = 1,
            allowedMimeTypes = allowedMimeTypes,
            maxFileSizeBytes = maxFileSizeBytes,
            copyToCache = copyToCache,
            extractMetadata = extractMetadata
        )

        fun multiple(
            maxSelection: Int = 10,
            allowedMimeTypes: List<String> = emptyList(),
            maxFileSizeBytes: Long? = null,
            copyToCache: Boolean = false,
            extractMetadata: Boolean = true
        ) = MediaPickerConfig(
            allowMultiple = true,
            maxSelection = maxSelection,
            allowedMimeTypes = allowedMimeTypes,
            maxFileSizeBytes = maxFileSizeBytes,
            copyToCache = copyToCache,
            extractMetadata = extractMetadata
        )
    }
}
