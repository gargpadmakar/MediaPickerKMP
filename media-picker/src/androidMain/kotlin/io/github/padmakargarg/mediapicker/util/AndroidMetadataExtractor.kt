package io.github.padmakargarg.mediapicker.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import io.github.padmakargarg.mediapicker.domain.model.MediaPickerConfig
import io.github.padmakargarg.mediapicker.domain.model.MediaPickerItem
import io.github.padmakargarg.mediapicker.domain.model.MediaType
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

/**
 * Utility for extracting metadata and resolving local copies from Android content URIs.
 */
object AndroidMetadataExtractor {

    private const val BUFFER_SIZE = 8192

    /**
     * Extracts unified [MediaPickerItem] from a given [Uri].
     */
    fun extractItem(
        context: Context,
        uri: Uri,
        config: MediaPickerConfig,
        defaultMediaType: MediaType = MediaType.UNKNOWN
    ): MediaPickerItem {
        val id = UUID.randomUUID().toString()
        var name: String? = null
        var sizeInBytes: Long? = null

        // 1. Query display name and size from ContentResolver
        try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) {
                        name = cursor.getString(nameIndex)
                    }
                    val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (sizeIndex != -1 && !cursor.isNull(sizeIndex)) {
                        sizeInBytes = cursor.getLong(sizeIndex)
                    }
                }
            }
        } catch (_: Exception) {}

        if (name.isNullOrBlank()) {
            name = uri.lastPathSegment
        }

        // Fallback for file size if cursor didn't provide it
        if (sizeInBytes == null || sizeInBytes == 0L) {
            try {
                context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                    val statSize = pfd.statSize
                    if (statSize > 0) sizeInBytes = statSize
                }
            } catch (_: Exception) {}
        }

        // 2. MIME type & extension
        var mimeType = context.contentResolver.getType(uri)
        val extensionFromName = name?.substringAfterLast('.', "")?.takeIf { it.isNotBlank() }

        if (mimeType.isNullOrBlank() && !extensionFromName.isNullOrBlank()) {
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extensionFromName.lowercase())
        }

        val resolvedExtension = extensionFromName
            ?: mimeType?.let { MimeTypeMap.getSingleton().getExtensionFromMimeType(it) }

        // 3. Resolve MediaType
        val mediaType = when {
            defaultMediaType != MediaType.UNKNOWN -> defaultMediaType
            !mimeType.isNullOrBlank() -> MediaType.fromMimeType(mimeType)
            else -> MediaType.UNKNOWN
        }

        // If name has no extension or is purely a numeric ID (e.g. from PhotoPicker), append extension
        if (!name.isNullOrBlank() && !name.contains('.') && !resolvedExtension.isNullOrBlank()) {
            name = "${mediaType.name.lowercase()}_$name.$resolvedExtension"
        }

        // Fallback name if still blank
        if (name.isNullOrBlank()) {
            val prefix = if (mediaType != MediaType.UNKNOWN) mediaType.name.lowercase() else "media"
            val ext = resolvedExtension ?: "bin"
            name = "${prefix}_${id.take(8)}.$ext"
        }

        val sanitizedName = sanitizeFileName(name, resolvedExtension)

        // 4. Dimensions & Duration extraction (if extractMetadata is enabled)
        var width: Int? = null
        var height: Int? = null
        var durationMs: Long? = null

        if (config.extractMetadata) {
            if (mediaType == MediaType.IMAGE) {
                try {
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                        BitmapFactory.decodeStream(stream, null, options)
                        if (options.outWidth > 0 && options.outHeight > 0) {
                            width = options.outWidth
                            height = options.outHeight
                        }
                    }
                } catch (_: Exception) {}
            } else if (mediaType == MediaType.VIDEO || mediaType == MediaType.AUDIO) {
                val retriever = MediaMetadataRetriever()
                try {
                    retriever.setDataSource(context, uri)
                    if (mediaType == MediaType.VIDEO) {
                        width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toIntOrNull()
                        height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toIntOrNull()
                    }
                    durationMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull()
                } catch (_: Exception) {
                } finally {
                    try {
                        retriever.release()
                    } catch (_: Exception) {}
                }
            }
        }

        // 5. Compression or Cache Copy
        var localPath: String? = null
        val isCompressionRequested = mediaType == MediaType.IMAGE &&
            (config.compressionQuality != null || config.maxImageWidth != null || config.maxImageHeight != null)

        val finalName = if (isCompressionRequested && !sanitizedName.endsWith(".jpg", ignoreCase = true)) {
            "${sanitizedName.substringBeforeLast('.')}.jpg"
        } else {
            sanitizedName
        }

        if (isCompressionRequested) {
            val cacheFolder = File(context.cacheDir, "media_picker_cache").apply { if (!exists()) mkdirs() }
            val compressedFile = File(cacheFolder, "compressed_$finalName")
            if (compressedFile.canonicalPath.startsWith(cacheFolder.canonicalPath)) {
                val success = compressImage(
                    context = context,
                    sourceUri = uri,
                    targetFile = compressedFile,
                    quality = config.compressionQuality ?: 80,
                    maxWidth = config.maxImageWidth,
                    maxHeight = config.maxImageHeight
                )
                if (success) {
                    localPath = compressedFile.absolutePath
                    sizeInBytes = compressedFile.length()
                    mimeType = "image/jpeg"
                    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    BitmapFactory.decodeFile(compressedFile.absolutePath, bounds)
                    if (bounds.outWidth > 0 && bounds.outHeight > 0) {
                        width = bounds.outWidth
                        height = bounds.outHeight
                    }
                }
            }
        } else if (config.copyToCache) {
            localPath = copyUriToCache(context, uri, finalName)
        }

        return MediaPickerItem(
            id = id,
            name = finalName,
            mimeType = mimeType,
            extension = resolvedExtension,
            sizeInBytes = sizeInBytes,
            mediaType = mediaType,
            uri = uri.toString(),
            localPath = localPath,
            width = width,
            height = height,
            durationMs = durationMs
        )
    }

    internal fun compressImage(
        context: Context,
        sourceUri: Uri,
        targetFile: File,
        quality: Int = 80,
        maxWidth: Int? = null,
        maxHeight: Int? = null
    ): Boolean {
        return try {
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(sourceUri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, options)
            }
            if (options.outWidth <= 0 || options.outHeight <= 0) return false

            var sampleSize = 1
            if (maxWidth != null && maxHeight != null) {
                while (options.outWidth / (sampleSize * 2) >= maxWidth &&
                    options.outHeight / (sampleSize * 2) >= maxHeight
                ) {
                    sampleSize *= 2
                }
            } else if (maxWidth != null) {
                while (options.outWidth / (sampleSize * 2) >= maxWidth) {
                    sampleSize *= 2
                }
            } else if (maxHeight != null) {
                while (options.outHeight / (sampleSize * 2) >= maxHeight) {
                    sampleSize *= 2
                }
            }

            val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
            val bitmap = context.contentResolver.openInputStream(sourceUri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, decodeOptions)
            } ?: return false

            val scaledBitmap = if ((maxWidth != null && bitmap.width > maxWidth) ||
                (maxHeight != null && bitmap.height > maxHeight)
            ) {
                val ratio = minOf(
                    maxWidth?.toFloat()?.div(bitmap.width) ?: 1f,
                    maxHeight?.toFloat()?.div(bitmap.height) ?: 1f
                )
                val targetW = (bitmap.width * ratio).toInt().coerceAtLeast(1)
                val targetH = (bitmap.height * ratio).toInt().coerceAtLeast(1)
                Bitmap.createScaledBitmap(bitmap, targetW, targetH, true)
            } else {
                bitmap
            }

            FileOutputStream(targetFile).use { out ->
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality.coerceIn(1, 100), out)
            }
            if (scaledBitmap != bitmap) {
                scaledBitmap.recycle()
            }
            bitmap.recycle()
            true
        } catch (_: Exception) {
            false
        }
    }

    internal fun sanitizeFileName(rawName: String?, fallbackExtension: String? = null): String {
        if (rawName.isNullOrBlank()) {
            val ext = fallbackExtension?.takeIf { it.isNotBlank() }?.let { ".$it" } ?: ".bin"
            return "media_${UUID.randomUUID().toString().take(8)}$ext"
        }

        // Strip path traversal and path separators (/ and \)
        var clean = rawName.substringAfterLast('/').substringAfterLast('\\').trim()

        // Replace invalid or dangerous filesystem characters with underscore
        clean = clean.replace(Regex("[^a-zA-Z0-9._-]"), "_")

        // Prevent leading dots (hidden files / relative navigation)
        clean = clean.trimStart('.')

        if (clean.isBlank()) {
            val ext = fallbackExtension?.takeIf { it.isNotBlank() }?.let { ".$it" } ?: ".bin"
            clean = "media_${UUID.randomUUID().toString().take(8)}$ext"
        }

        return clean
    }

    internal fun copyUriToCache(context: Context, uri: Uri, targetFileName: String): String? {
        return try {
            val cacheFolder = File(context.cacheDir, "mediapicker_cache").apply {
                if (!exists()) mkdirs()
            }
            val safeFileName = sanitizeFileName(targetFileName)
            val targetFile = File(cacheFolder, "${System.currentTimeMillis()}_$safeFileName")

            // Strict canonical path traversal guard
            if (!targetFile.canonicalPath.startsWith(cacheFolder.canonicalPath)) {
                return null
            }

            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(targetFile).use { output ->
                    val buffer = ByteArray(BUFFER_SIZE)
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                    }
                    output.flush()
                }
            }
            targetFile.absolutePath
        } catch (_: Exception) {
            null
        }
    }
}
