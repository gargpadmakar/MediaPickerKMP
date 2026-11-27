package io.github.padmakargarg.mediapicker.bridge

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import io.github.padmakargarg.mediapicker.data.contract.PlatformMediaPickerBridge
import io.github.padmakargarg.mediapicker.domain.model.MediaPickerConfig
import io.github.padmakargarg.mediapicker.domain.model.MediaPickerError
import io.github.padmakargarg.mediapicker.domain.model.MediaPickerResult
import io.github.padmakargarg.mediapicker.domain.model.MediaType
import io.github.padmakargarg.mediapicker.util.AndroidMetadataExtractor
import java.io.File
import java.util.UUID

/**
 * Android platform bridge connecting domain operations to native Activity Result APIs.
 */
class AndroidMediaPickerBridge(
    initialActivity: ComponentActivity? = null
) : PlatformMediaPickerBridge {

    private val launcherBridge = ActivityResultLauncherBridge(initialActivity)

    fun bind(activity: ComponentActivity) {
        launcherBridge.bind(activity)
    }

    fun unbind() {
        launcherBridge.unbind()
    }

    override suspend fun pickImage(config: MediaPickerConfig): MediaPickerResult {
        val request = if (config.allowedMimeTypes.size == 1 && config.allowedMimeTypes.first().startsWith("image/")) {
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.SingleMimeType(config.allowedMimeTypes.first()))
        } else {
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        }

        val uri = try {
            launcherBridge.launchContract(ActivityResultContracts.PickVisualMedia(), request)
        } catch (t: Throwable) {
            return MediaPickerResult.Error(MediaPickerError.OperationFailed(cause = t))
        }

        return if (uri != null) {
            val activity = launcherBridge.activity
                ?: return MediaPickerResult.Error(MediaPickerError.OperationFailed(message = "Activity destroyed"))
            val item = AndroidMetadataExtractor.extractItem(activity, uri, config, defaultMediaType = MediaType.IMAGE)
            MediaPickerResult.Success(listOf(item))
        } else {
            MediaPickerResult.Cancelled
        }
    }

    override suspend fun pickMultipleImages(config: MediaPickerConfig): MediaPickerResult {
        val request = if (config.allowedMimeTypes.size == 1 && config.allowedMimeTypes.first().startsWith("image/")) {
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.SingleMimeType(config.allowedMimeTypes.first()))
        } else {
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        }

        val contract = ActivityResultContracts.PickMultipleVisualMedia(config.maxSelection)
        val uris = try {
            launcherBridge.launchContract(contract, request)
        } catch (t: Throwable) {
            return MediaPickerResult.Error(MediaPickerError.OperationFailed(cause = t))
        }

        return if (!uris.isNullOrEmpty()) {
            val activity = launcherBridge.activity
                ?: return MediaPickerResult.Error(MediaPickerError.OperationFailed(message = "Activity destroyed"))
            val items = uris.map { uri ->
                AndroidMetadataExtractor.extractItem(activity, uri, config, defaultMediaType = MediaType.IMAGE)
            }
            MediaPickerResult.Success(items)
        } else {
            MediaPickerResult.Cancelled
        }
    }

    override suspend fun pickVideo(config: MediaPickerConfig): MediaPickerResult {
        val request = if (config.allowedMimeTypes.size == 1 && config.allowedMimeTypes.first().startsWith("video/")) {
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.SingleMimeType(config.allowedMimeTypes.first()))
        } else {
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
        }

        val uri = try {
            launcherBridge.launchContract(ActivityResultContracts.PickVisualMedia(), request)
        } catch (t: Throwable) {
            return MediaPickerResult.Error(MediaPickerError.OperationFailed(cause = t))
        }

        return if (uri != null) {
            val activity = launcherBridge.activity
                ?: return MediaPickerResult.Error(MediaPickerError.OperationFailed(message = "Activity destroyed"))
            val item = AndroidMetadataExtractor.extractItem(activity, uri, config, defaultMediaType = MediaType.VIDEO)
            MediaPickerResult.Success(listOf(item))
        } else {
            MediaPickerResult.Cancelled
        }
    }

    override suspend fun pickMultipleVideos(config: MediaPickerConfig): MediaPickerResult {
        val request = if (config.allowedMimeTypes.size == 1 && config.allowedMimeTypes.first().startsWith("video/")) {
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.SingleMimeType(config.allowedMimeTypes.first()))
        } else {
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
        }

        val contract = ActivityResultContracts.PickMultipleVisualMedia(config.maxSelection)
        val uris = try {
            launcherBridge.launchContract(contract, request)
        } catch (t: Throwable) {
            return MediaPickerResult.Error(MediaPickerError.OperationFailed(cause = t))
        }

        return if (!uris.isNullOrEmpty()) {
            val activity = launcherBridge.activity
                ?: return MediaPickerResult.Error(MediaPickerError.OperationFailed(message = "Activity destroyed"))
            val items = uris.map { uri ->
                AndroidMetadataExtractor.extractItem(activity, uri, config, defaultMediaType = MediaType.VIDEO)
            }
            MediaPickerResult.Success(items)
        } else {
            MediaPickerResult.Cancelled
        }
    }

    override suspend fun pickMedia(config: MediaPickerConfig): MediaPickerResult {
        val request = PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)

        return if (config.allowMultiple) {
            val contract = ActivityResultContracts.PickMultipleVisualMedia(config.maxSelection)
            val uris = try {
                launcherBridge.launchContract(contract, request)
            } catch (t: Throwable) {
                return MediaPickerResult.Error(MediaPickerError.OperationFailed(cause = t))
            }

            if (!uris.isNullOrEmpty()) {
                val activity = launcherBridge.activity
                    ?: return MediaPickerResult.Error(MediaPickerError.OperationFailed(message = "Activity destroyed"))
                val items = uris.map { uri ->
                    AndroidMetadataExtractor.extractItem(activity, uri, config)
                }
                MediaPickerResult.Success(items)
            } else {
                MediaPickerResult.Cancelled
            }
        } else {
            val uri = try {
                launcherBridge.launchContract(ActivityResultContracts.PickVisualMedia(), request)
            } catch (t: Throwable) {
                return MediaPickerResult.Error(MediaPickerError.OperationFailed(cause = t))
            }

            if (uri != null) {
                val activity = launcherBridge.activity
                    ?: return MediaPickerResult.Error(MediaPickerError.OperationFailed(message = "Activity destroyed"))
                val item = AndroidMetadataExtractor.extractItem(activity, uri, config)
                MediaPickerResult.Success(listOf(item))
            } else {
                MediaPickerResult.Cancelled
            }
        }
    }

    override suspend fun pickFile(config: MediaPickerConfig): MediaPickerResult {
        val mimeTypes = if (config.allowedMimeTypes.isNotEmpty()) {
            config.allowedMimeTypes.toTypedArray()
        } else {
            arrayOf("*/*")
        }

        val uri = try {
            launcherBridge.launchContract(ActivityResultContracts.OpenDocument(), mimeTypes)
        } catch (t: Throwable) {
            return MediaPickerResult.Error(MediaPickerError.OperationFailed(cause = t))
        }

        return if (uri != null) {
            val activity = launcherBridge.activity
                ?: return MediaPickerResult.Error(MediaPickerError.OperationFailed(message = "Activity destroyed"))

            tryPersistUriPermission(activity, uri)
            val item = AndroidMetadataExtractor.extractItem(activity, uri, config, defaultMediaType = MediaType.DOCUMENT)
            MediaPickerResult.Success(listOf(item))
        } else {
            MediaPickerResult.Cancelled
        }
    }

    override suspend fun pickMultipleFiles(config: MediaPickerConfig): MediaPickerResult {
        val mimeTypes = if (config.allowedMimeTypes.isNotEmpty()) {
            config.allowedMimeTypes.toTypedArray()
        } else {
            arrayOf("*/*")
        }

        val uris = try {
            launcherBridge.launchContract(ActivityResultContracts.OpenMultipleDocuments(), mimeTypes)
        } catch (t: Throwable) {
            return MediaPickerResult.Error(MediaPickerError.OperationFailed(cause = t))
        }

        return if (!uris.isNullOrEmpty()) {
            val activity = launcherBridge.activity
                ?: return MediaPickerResult.Error(MediaPickerError.OperationFailed(message = "Activity destroyed"))

            val items = uris.map { uri ->
                tryPersistUriPermission(activity, uri)
                AndroidMetadataExtractor.extractItem(activity, uri, config, defaultMediaType = MediaType.DOCUMENT)
            }
            MediaPickerResult.Success(items)
        } else {
            MediaPickerResult.Cancelled
        }
    }

    override suspend fun pickAudio(config: MediaPickerConfig): MediaPickerResult {
        val mimeTypes = if (config.allowedMimeTypes.isNotEmpty()) {
            config.allowedMimeTypes.toTypedArray()
        } else {
            arrayOf("audio/*")
        }

        val uri = try {
            launcherBridge.launchContract(ActivityResultContracts.OpenDocument(), mimeTypes)
        } catch (t: Throwable) {
            return MediaPickerResult.Error(MediaPickerError.OperationFailed(cause = t))
        }

        return if (uri != null) {
            val activity = launcherBridge.activity
                ?: return MediaPickerResult.Error(MediaPickerError.OperationFailed(message = "Activity destroyed."))

            tryPersistUriPermission(activity, uri)
            val item = AndroidMetadataExtractor.extractItem(
                context = activity,
                uri = uri,
                config = config,
                defaultMediaType = MediaType.AUDIO
            )
            MediaPickerResult.Success(listOf(item))
        } else {
            MediaPickerResult.Cancelled
        }
    }

    override suspend fun pickMultipleAudio(config: MediaPickerConfig): MediaPickerResult {
        val mimeTypes = if (config.allowedMimeTypes.isNotEmpty()) {
            config.allowedMimeTypes.toTypedArray()
        } else {
            arrayOf("audio/*")
        }

        val uris = try {
            launcherBridge.launchContract(ActivityResultContracts.OpenMultipleDocuments(), mimeTypes)
        } catch (t: Throwable) {
            return MediaPickerResult.Error(MediaPickerError.OperationFailed(cause = t))
        }

        return if (!uris.isNullOrEmpty()) {
            val activity = launcherBridge.activity
                ?: return MediaPickerResult.Error(MediaPickerError.OperationFailed(message = "Activity destroyed."))

            val items = uris.map { uri ->
                tryPersistUriPermission(activity, uri)
                AndroidMetadataExtractor.extractItem(
                    context = activity,
                    uri = uri,
                    config = config,
                    defaultMediaType = MediaType.AUDIO
                )
            }
            MediaPickerResult.Success(items)
        } else {
            MediaPickerResult.Cancelled
        }
    }

    override suspend fun capturePhoto(config: MediaPickerConfig): MediaPickerResult {
        val activity = launcherBridge.activity
            ?: return MediaPickerResult.Error(MediaPickerError.OperationFailed(message = "Activity is not available."))

        val packageManager = activity.packageManager
        val hasCamera = packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
        if (!hasCamera) {
            return MediaPickerResult.Error(
                MediaPickerError.CameraUnavailable("Device does not have camera hardware.")
            )
        }

        val cameraDir = File(activity.cacheDir, "camera_photos").apply {
            if (!exists()) mkdirs()
        }
        val fileName = "photo_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(8)}.jpg"
        val photoFile = File(cameraDir, fileName)

        val authority = "${activity.packageName}.mediapicker.fileprovider"
        val photoUri = try {
            FileProvider.getUriForFile(activity, authority, photoFile)
        } catch (e: Exception) {
            return MediaPickerResult.Error(
                MediaPickerError.OperationFailed(
                    cause = e,
                    message = "Failed to create secure content URI with authority '$authority'."
                )
            )
        }

        val success = try {
            launcherBridge.launchContract(ActivityResultContracts.TakePicture(), photoUri)
        } catch (e: ActivityNotFoundException) {
            if (photoFile.exists()) photoFile.delete()
            return MediaPickerResult.Error(
                MediaPickerError.CameraUnavailable("No camera application found to handle photo capture.")
            )
        } catch (t: Throwable) {
            if (photoFile.exists()) photoFile.delete()
            return MediaPickerResult.Error(MediaPickerError.OperationFailed(cause = t))
        }

        return if (success == true) {
            if (photoFile.exists() && photoFile.length() > 0) {
                val item = AndroidMetadataExtractor.extractItem(
                    context = activity,
                    uri = photoUri,
                    config = config,
                    defaultMediaType = MediaType.IMAGE
                ).copy(
                    localPath = photoFile.absolutePath,
                    name = fileName,
                    extension = "jpg",
                    mimeType = "image/jpeg",
                    sizeInBytes = photoFile.length()
                )
                MediaPickerResult.Success(listOf(item))
            } else {
                if (photoFile.exists()) photoFile.delete()
                MediaPickerResult.Error(
                    MediaPickerError.FileNotFound("Camera output file was not created or is empty.")
                )
            }
        } else {
            if (photoFile.exists()) photoFile.delete()
            MediaPickerResult.Cancelled
        }
    }

    override suspend fun captureVideo(config: MediaPickerConfig): MediaPickerResult {
        val activity = launcherBridge.activity
            ?: return MediaPickerResult.Error(MediaPickerError.OperationFailed(message = "Activity is not available."))

        val packageManager = activity.packageManager
        val hasCamera = packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
        if (!hasCamera) {
            return MediaPickerResult.Error(
                MediaPickerError.CameraUnavailable("Device does not have camera hardware.")
            )
        }

        val cameraDir = File(activity.cacheDir, "camera_videos").apply {
            if (!exists()) mkdirs()
        }
        val fileName = "video_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(8)}.mp4"
        val videoFile = File(cameraDir, fileName)

        val authority = "${activity.packageName}.mediapicker.fileprovider"
        val videoUri = try {
            FileProvider.getUriForFile(activity, authority, videoFile)
        } catch (e: Exception) {
            return MediaPickerResult.Error(
                MediaPickerError.OperationFailed(
                    cause = e,
                    message = "Failed to create secure content URI with authority '$authority'."
                )
            )
        }

        val success = try {
            launcherBridge.launchContract(ActivityResultContracts.CaptureVideo(), videoUri)
        } catch (e: ActivityNotFoundException) {
            if (videoFile.exists()) videoFile.delete()
            return MediaPickerResult.Error(
                MediaPickerError.CameraUnavailable("No camera application found to handle video capture.")
            )
        } catch (t: Throwable) {
            if (videoFile.exists()) videoFile.delete()
            return MediaPickerResult.Error(MediaPickerError.OperationFailed(cause = t))
        }

        return if (success == true) {
            if (videoFile.exists() && videoFile.length() > 0) {
                val item = AndroidMetadataExtractor.extractItem(
                    context = activity,
                    uri = videoUri,
                    config = config,
                    defaultMediaType = MediaType.VIDEO
                ).copy(
                    localPath = videoFile.absolutePath,
                    name = fileName,
                    extension = "mp4",
                    mimeType = "video/mp4",
                    sizeInBytes = videoFile.length()
                )
                MediaPickerResult.Success(listOf(item))
            } else {
                if (videoFile.exists()) videoFile.delete()
                MediaPickerResult.Error(
                    MediaPickerError.FileNotFound("Camera output video file was not created or is empty.")
                )
            }
        } else {
            if (videoFile.exists()) videoFile.delete()
            MediaPickerResult.Cancelled
        }
    }

    private fun tryPersistUriPermission(activity: ComponentActivity, uri: Uri) {
        try {
            activity.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (_: Exception) {}
    }
}
