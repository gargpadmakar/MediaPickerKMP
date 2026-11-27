package io.github.padmakargarg.mediapicker.bridge

import io.github.padmakargarg.mediapicker.domain.model.MediaPickerConfig
import io.github.padmakargarg.mediapicker.domain.model.MediaPickerError
import io.github.padmakargarg.mediapicker.domain.model.MediaPickerItem
import io.github.padmakargarg.mediapicker.domain.model.MediaPickerResult
import io.github.padmakargarg.mediapicker.domain.model.MediaType
import io.github.padmakargarg.mediapicker.util.IOSMetadataExtractor
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.CancellableContinuation
import platform.Foundation.*
import platform.UIKit.*
import platform.darwin.NSObject
import kotlin.coroutines.resume

private const val CAMERA_CACHE_FOLDER = "mediapicker_camera"

/**
 * Delegate handling UIImagePickerController camera capture callbacks.
 */
@OptIn(ExperimentalForeignApi::class)
class IOSCameraCaptureDelegate(
    private val continuation: CancellableContinuation<MediaPickerResult>,
    private val config: MediaPickerConfig,
    private val captureMode: MediaType = MediaType.IMAGE,
    private val onComplete: () -> Unit = {}
) : NSObject(), UIImagePickerControllerDelegateProtocol, UINavigationControllerDelegateProtocol {

    override fun imagePickerController(picker: UIImagePickerController, didFinishPickingMediaWithInfo: Map<Any?, *>) {
        picker.dismissViewControllerAnimated(flag = true, completion = null)
        onComplete()

        if (captureMode == MediaType.VIDEO) {
            val videoUrl = didFinishPickingMediaWithInfo[UIImagePickerControllerMediaURL] as? NSURL
            if (videoUrl == null) {
                if (continuation.isActive) {
                    continuation.resume(
                        MediaPickerResult.Error(MediaPickerError.FileNotFound("Captured video data could not be retrieved."))
                    )
                }
                return
            }

            try {
                val item = IOSMetadataExtractor.extractItem(
                    url = videoUrl,
                    config = config,
                    defaultMediaType = MediaType.VIDEO,
                    forceCacheCopy = true
                )
                if (continuation.isActive) {
                    continuation.resume(MediaPickerResult.Success(listOf(item)))
                }
            } catch (e: Exception) {
                if (continuation.isActive) {
                    continuation.resume(
                        MediaPickerResult.Error(MediaPickerError.OperationFailed(message = "Failed to process captured video: ${e.message}"))
                    )
                }
            }
            return
        }

        val image = didFinishPickingMediaWithInfo[UIImagePickerControllerOriginalImage] as? UIImage
        if (image == null) {
            if (continuation.isActive) {
                continuation.resume(
                    MediaPickerResult.Error(MediaPickerError.FileNotFound("Captured image data could not be retrieved."))
                )
            }
            return
        }

        val id = NSUUID.UUID().UUIDString
        val timestamp = NSDate().timeIntervalSince1970.toLong()
        val fileName = "photo_${timestamp}_${id.take(8)}.jpg"

        val fileManager = NSFileManager.defaultManager
        val cachesDir = (NSSearchPathForDirectoriesInDomains(
            NSCachesDirectory,
            NSUserDomainMask,
            true
        ).firstOrNull() as? String) ?: NSTemporaryDirectory()

        val cameraFolder = "$cachesDir/$CAMERA_CACHE_FOLDER"
        if (!fileManager.fileExistsAtPath(cameraFolder)) {
            fileManager.createDirectoryAtPath(cameraFolder, withIntermediateDirectories = true, attributes = null, error = null)
        }

        val destinationPath = "$cameraFolder/$fileName"
        val destinationUrl = NSURL.fileURLWithPath(destinationPath)

        val jpegData = UIImageJPEGRepresentation(image, 0.95)
        if (jpegData == null) {
            if (continuation.isActive) {
                continuation.resume(
                    MediaPickerResult.Error(MediaPickerError.OperationFailed(message = "Failed to encode captured image."))
                )
            }
            return
        }

        val writeSuccess = jpegData.writeToFile(destinationPath, atomically = true)
        if (!writeSuccess) {
            if (fileManager.fileExistsAtPath(destinationPath)) {
                fileManager.removeItemAtPath(destinationPath, error = null)
            }
            if (continuation.isActive) {
                continuation.resume(
                    MediaPickerResult.Error(MediaPickerError.OperationFailed(message = "Failed to save captured photo to cache."))
                )
            }
            return
        }

        var width: Int? = null
        var height: Int? = null
        if (config.extractMetadata) {
            image.size.useContents {
                width = this.width.toInt()
                height = this.height.toInt()
            }
        }

        val sizeInBytes = jpegData.length.toLong()

        val item = MediaPickerItem(
            id = id,
            name = fileName,
            mimeType = "image/jpeg",
            extension = "jpg",
            sizeInBytes = sizeInBytes,
            mediaType = MediaType.IMAGE,
            uri = destinationUrl.absoluteString ?: ("file://$destinationPath"),
            localPath = destinationPath,
            width = width,
            height = height,
            durationMs = null
        )

        if (continuation.isActive) {
            continuation.resume(MediaPickerResult.Success(listOf(item)))
        }
    }

    override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
        picker.dismissViewControllerAnimated(flag = true, completion = null)
        onComplete()
        if (continuation.isActive) {
            continuation.resume(MediaPickerResult.Cancelled)
        }
    }
}
