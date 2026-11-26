package io.github.padmakargarg.mediapicker.domain.model

/**
 * Unified representation of a selected media item across Android and iOS.
 *
 * @property id Unique identifier of the item.
 * @property name File name with extension where available.
 * @property mimeType Standard MIME type string (e.g. "image/jpeg", "application/pdf").
 * @property extension File extension without leading dot (e.g. "jpg", "pdf").
 * @property sizeInBytes File size in bytes where available.
 * @property mediaType Categorized [MediaType].
 * @property uri Platform-independent URI string (e.g. "content://...", "file://...").
 * @property localPath Local file path if copied to app cache or local storage.
 * @property width Visual width in pixels (for images and videos) where available.
 * @property height Visual height in pixels (for images and videos) where available.
 * @property durationMs Media duration in milliseconds (for audio and video) where available.
 */
data class MediaPickerItem(
    val id: String,
    val name: String? = null,
    val mimeType: String? = null,
    val extension: String? = null,
    val sizeInBytes: Long? = null,
    val mediaType: MediaType = MediaType.UNKNOWN,
    val uri: String,
    val localPath: String? = null,
    val width: Int? = null,
    val height: Int? = null,
    val durationMs: Long? = null
)
