package io.github.padmakargarg.mediapicker.util

import io.github.padmakargarg.mediapicker.domain.model.MediaPickerConfig
import io.github.padmakargarg.mediapicker.domain.model.MediaPickerItem
import io.github.padmakargarg.mediapicker.domain.model.MediaType
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.AVFoundation.*
import platform.CoreGraphics.CGSize
import platform.CoreMedia.CMTimeGetSeconds
import platform.Foundation.*
import platform.UIKit.UIImage
import platform.UniformTypeIdentifiers.*

/**
 * Utility for extracting metadata and managing file caching for iOS media items.
 */
@OptIn(ExperimentalForeignApi::class)
object IOSMetadataExtractor {

    private const val CACHE_DIR_NAME = "mediapicker_cache"

    /**
     * Maps a list of MIME types to matching iOS [UTType] objects for picker filtering.
     */
    fun mimeTypesToUTTypes(mimeTypes: List<String>): List<UTType> {
        if (mimeTypes.isEmpty() || mimeTypes.contains("*/*")) {
            return listOf(UTTypeItem)
        }
        val types = mimeTypes.mapNotNull { mime ->
            when (mime) {
                "image/*" -> UTTypeImage
                "video/*" -> UTTypeMovie
                "audio/*" -> UTTypeAudio
                "text/*" -> UTTypeText
                "application/pdf" -> UTTypePDF
                "text/plain" -> UTTypePlainText
                "text/html" -> UTTypeHTML
                "application/json" -> UTTypeJSON
                "application/zip" -> UTTypeZIP
                else -> UTType.typeWithMIMEType(mime) ?: UTTypeItem
            }
        }.distinct()
        return types.ifEmpty { listOf(UTTypeItem) }
    }

    /**
     * Extracts unified [MediaPickerItem] from a given [NSURL].
     */
    fun extractItem(
        url: NSURL,
        config: MediaPickerConfig,
        defaultMediaType: MediaType = MediaType.UNKNOWN,
        forceCacheCopy: Boolean = false
    ): MediaPickerItem {
        val id = NSUUID.UUID().UUIDString
        val path = url.path ?: ""
        val fileName = url.lastPathComponent ?: "media_$id"
        val extension = url.pathExtension?.takeIf { it.isNotBlank() }?.lowercase()

        // 1. File Size
        var sizeInBytes: Long? = null
        try {
            val fileManager = NSFileManager.defaultManager
            val attributes = fileManager.attributesOfItemAtPath(path, error = null)
            val fileSizeNumber = attributes?.get(NSFileSize) as? NSNumber
            sizeInBytes = fileSizeNumber?.longLongValue
        } catch (_: Exception) {}

        // 2. MIME Type & UTType
        var mimeType: String? = null
        if (!extension.isNullOrBlank()) {
            val utType = UTType.typeWithFilenameExtension(extension)
            mimeType = utType?.preferredMIMEType
        }
        if (mimeType.isNullOrBlank()) {
            mimeType = fallbackMimeTypeForExtension(extension)
        }

        // 3. Resolve MediaType
        val mediaType = when {
            defaultMediaType != MediaType.UNKNOWN -> defaultMediaType
            !mimeType.isNullOrBlank() -> MediaType.fromMimeType(mimeType)
            else -> resolveMediaTypeFromExtension(extension)
        }

        // 4. Dimensions & Duration extraction
        var width: Int? = null
        var height: Int? = null
        var durationMs: Long? = null

        if (config.extractMetadata) {
            when (mediaType) {
                MediaType.IMAGE -> {
                    try {
                        val image = UIImage.imageWithContentsOfFile(path)
                        if (image != null) {
                            image.size.useContents {
                                width = this.width.toInt()
                                height = this.height.toInt()
                            }
                        }
                    } catch (_: Exception) {}
                }
                MediaType.VIDEO, MediaType.AUDIO -> {
                    try {
                        val asset: AVAsset = AVURLAsset(uRL = url, options = null)
                        val durationSeconds = CMTimeGetSeconds(asset.duration)
                        if (!durationSeconds.isNaN() && durationSeconds > 0) {
                            durationMs = (durationSeconds * 1000).toLong()
                        }
                        if (mediaType == MediaType.VIDEO) {
                            val track = asset.tracks.filterIsInstance<AVAssetTrack>().firstOrNull { it.mediaType == AVMediaTypeVideo }
                            track?.naturalSize?.useContents {
                                width = this.width.toInt()
                                height = this.height.toInt()
                            }
                        }
                    } catch (_: Exception) {}
                }
                else -> {}
            }
        }

        // 5. Cache copy if requested
        var localPath: String? = if (path.isNotBlank()) path else null
        if (config.copyToCache || forceCacheCopy) {
            val copiedPath = copyUrlToCache(url, fileName)
            if (copiedPath != null) {
                localPath = copiedPath
            }
        }

        return MediaPickerItem(
            id = id,
            name = fileName,
            mimeType = mimeType,
            extension = extension,
            sizeInBytes = sizeInBytes,
            mediaType = mediaType,
            uri = url.absoluteString ?: ("file://" + path),
            localPath = localPath,
            width = width,
            height = height,
            durationMs = durationMs
        )
    }

    /**
     * Copies a file from the given [NSURL] into the app's cache directory.
     * Returns the absolute path of the newly copied file, or null on failure.
     */
    fun copyUrlToCache(url: NSURL, targetFileName: String): String? {
        return try {
            val fileManager = NSFileManager.defaultManager
            val cachesDir = (NSSearchPathForDirectoriesInDomains(
                NSCachesDirectory,
                NSUserDomainMask,
                true
            ).firstOrNull() as? String) ?: NSTemporaryDirectory()

            val cacheFolder = "$cachesDir/$CACHE_DIR_NAME"
            if (!fileManager.fileExistsAtPath(cacheFolder)) {
                fileManager.createDirectoryAtPath(cacheFolder, withIntermediateDirectories = true, attributes = null, error = null)
            }

            val uniquePrefix = "${NSDate().timeIntervalSince1970.toLong()}_${NSUUID.UUID().UUIDString.take(8)}"
            val destinationPath = "$cacheFolder/${uniquePrefix}_$targetFileName"
            val destinationUrl = NSURL.fileURLWithPath(destinationPath)

            if (fileManager.fileExistsAtPath(destinationPath)) {
                fileManager.removeItemAtPath(destinationPath, error = null)
            }

            val success = fileManager.copyItemAtURL(url, toURL = destinationUrl, error = null)
            if (success) destinationPath else null
        } catch (_: Exception) {
            null
        }
    }

    private fun resolveMediaTypeFromExtension(extension: String?): MediaType {
        if (extension == null) return MediaType.UNKNOWN
        return when (extension.lowercase()) {
            "jpg", "jpeg", "png", "gif", "bmp", "webp", "heic", "heif", "tiff" -> MediaType.IMAGE
            "mp4", "mov", "m4v", "avi", "mkv", "webm", "3gp" -> MediaType.VIDEO
            "mp3", "wav", "aac", "m4a", "flac", "ogg", "opus" -> MediaType.AUDIO
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "csv", "zip", "json" -> MediaType.DOCUMENT
            else -> MediaType.UNKNOWN
        }
    }

    private fun fallbackMimeTypeForExtension(extension: String?): String? {
        if (extension == null) return null
        return when (extension.lowercase()) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "gif" -> "image/gif"
            "webp" -> "image/webp"
            "heic" -> "image/heic"
            "heif" -> "image/heif"
            "mp4" -> "video/mp4"
            "mov" -> "video/quicktime"
            "mp3" -> "audio/mpeg"
            "wav" -> "audio/wav"
            "aac" -> "audio/aac"
            "m4a" -> "audio/mp4"
            "pdf" -> "application/pdf"
            "txt" -> "text/plain"
            "csv" -> "text/csv"
            "zip" -> "application/zip"
            "json" -> "application/json"
            "doc" -> "application/msword"
            "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            "xls" -> "application/vnd.ms-excel"
            "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            "ppt" -> "application/vnd.ms-powerpoint"
            "pptx" -> "application/vnd.openxmlformats-officedocument.presentationml.presentation"
            else -> null
        }
    }
}
