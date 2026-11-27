package io.github.padmakargarg.mediapicker.bridge

import io.github.padmakargarg.mediapicker.data.contract.PlatformMediaPickerBridge
import io.github.padmakargarg.mediapicker.domain.model.MediaPickerConfig
import io.github.padmakargarg.mediapicker.domain.model.MediaPickerError
import io.github.padmakargarg.mediapicker.domain.model.MediaPickerResult
import io.github.padmakargarg.mediapicker.domain.model.MediaType
import io.github.padmakargarg.mediapicker.util.IOSMetadataExtractor
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.AVFoundation.*
import platform.PhotosUI.PHPickerConfiguration
import platform.PhotosUI.PHPickerFilter
import platform.PhotosUI.PHPickerViewController
import platform.UIKit.*
import kotlin.coroutines.resume

/**
 * iOS platform bridge connecting domain operations to native PHPicker, UIDocumentPicker and Camera.
 */
class IOSMediaPickerBridge(
    initialViewController: UIViewController? = null
) : PlatformMediaPickerBridge {

    private val presenter = IOSViewControllerPresenter(initialViewController)
    private var activePhotoDelegate: IOSPhotoPickerDelegate? = null
    private var activeDocumentDelegate: IOSDocumentPickerDelegate? = null
    private var activeCameraDelegate: IOSCameraCaptureDelegate? = null

    fun bind(viewController: UIViewController) {
        presenter.bind(viewController)
    }

    fun unbind() {
        presenter.unbind()
    }

    override suspend fun pickImage(config: MediaPickerConfig): MediaPickerResult {
        return launchPhotoPicker(
            config = config,
            filter = PHPickerFilter.imagesFilter,
            selectionLimit = 1,
            defaultMediaType = MediaType.IMAGE
        )
    }

    override suspend fun pickMultipleImages(config: MediaPickerConfig): MediaPickerResult {
        return launchPhotoPicker(
            config = config,
            filter = PHPickerFilter.imagesFilter,
            selectionLimit = config.maxSelection,
            defaultMediaType = MediaType.IMAGE
        )
    }

    override suspend fun pickVideo(config: MediaPickerConfig): MediaPickerResult {
        return launchPhotoPicker(
            config = config,
            filter = PHPickerFilter.videosFilter,
            selectionLimit = 1,
            defaultMediaType = MediaType.VIDEO
        )
    }

    override suspend fun pickMultipleVideos(config: MediaPickerConfig): MediaPickerResult {
        return launchPhotoPicker(
            config = config,
            filter = PHPickerFilter.videosFilter,
            selectionLimit = config.maxSelection,
            defaultMediaType = MediaType.VIDEO
        )
    }

    override suspend fun pickMedia(config: MediaPickerConfig): MediaPickerResult {
        val filter = PHPickerFilter.anyFilterMatchingSubfilters(
            listOf(PHPickerFilter.imagesFilter, PHPickerFilter.videosFilter)
        )
        val selectionLimit = if (config.allowMultiple) config.maxSelection else 1
        return launchPhotoPicker(
            config = config,
            filter = filter,
            selectionLimit = selectionLimit,
            defaultMediaType = MediaType.UNKNOWN
        )
    }

    override suspend fun pickFile(config: MediaPickerConfig): MediaPickerResult {
        return launchDocumentPicker(config, allowMultiple = false, defaultMediaType = MediaType.DOCUMENT)
    }

    override suspend fun pickMultipleFiles(config: MediaPickerConfig): MediaPickerResult {
        return launchDocumentPicker(config, allowMultiple = true, defaultMediaType = MediaType.DOCUMENT)
    }

    override suspend fun capturePhoto(config: MediaPickerConfig): MediaPickerResult {
        return launchCameraCapture(config = config, captureMode = MediaType.IMAGE)
    }

    override suspend fun captureVideo(config: MediaPickerConfig): MediaPickerResult {
        return launchCameraCapture(config = config, captureMode = MediaType.VIDEO)
    }

    private suspend fun launchCameraCapture(
        config: MediaPickerConfig,
        captureMode: MediaType
    ): MediaPickerResult {
        val isCameraAvailable = UIImagePickerController.isSourceTypeAvailable(
            UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera
        )
        if (!isCameraAvailable) {
            return MediaPickerResult.Error(
                MediaPickerError.CameraUnavailable("Camera hardware is not available on this device.")
            )
        }

        val authStatus = AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo)
        when (authStatus) {
            AVAuthorizationStatusRestricted -> {
                return MediaPickerResult.Error(
                    MediaPickerError.PermissionDenied("Camera access is restricted on this device.")
                )
            }
            AVAuthorizationStatusDenied -> {
                return MediaPickerResult.Error(
                    MediaPickerError.PermissionDenied("Camera permission was denied. Please grant camera permission in iOS Settings.")
                )
            }
            AVAuthorizationStatusNotDetermined -> {
                val granted = suspendCancellableCoroutine<Boolean> { cont ->
                    AVCaptureDevice.requestAccessForMediaType(AVMediaTypeVideo) { isGranted ->
                        cont.resume(isGranted)
                    }
                }
                if (!granted) {
                    return MediaPickerResult.Error(
                        MediaPickerError.PermissionDenied("Camera permission was denied by the user.")
                    )
                }
            }
            else -> { /* Authorized, proceed */ }
        }

        return suspendCancellableCoroutine { continuation ->
            val topViewController = presenter.getTopViewController()
            if (topViewController == null) {
                continuation.resume(
                    MediaPickerResult.Error(
                        MediaPickerError.OperationFailed(message = "No active UIViewController found to present Camera.")
                    )
                )
                return@suspendCancellableCoroutine
            }

            val cameraController = UIImagePickerController().apply {
                sourceType = UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera
                if (captureMode == MediaType.VIDEO) {
                    mediaTypes = listOf("public.movie")
                    cameraCaptureMode = UIImagePickerControllerCameraCaptureMode.UIImagePickerControllerCameraCaptureModeVideo
                }
            }

            val delegate = IOSCameraCaptureDelegate(
                continuation = continuation,
                config = config,
                captureMode = captureMode,
                onComplete = { activeCameraDelegate = null }
            )
            activeCameraDelegate = delegate
            cameraController.delegate = delegate

            continuation.invokeOnCancellation {
                activeCameraDelegate = null
                presenter.dismiss(cameraController)
            }

            presenter.present(
                controller = cameraController,
                onFailure = {
                    activeCameraDelegate = null
                    if (continuation.isActive) {
                        continuation.resume(
                            MediaPickerResult.Error(
                                MediaPickerError.OperationFailed(message = "Failed to present Camera: top UIViewController unavailable on main thread.")
                            )
                        )
                    }
                }
            )
        }
    }

    override suspend fun pickAudio(config: MediaPickerConfig): MediaPickerResult {
        return launchDocumentPicker(config, allowMultiple = false, defaultMediaType = MediaType.AUDIO)
    }

    override suspend fun pickMultipleAudio(config: MediaPickerConfig): MediaPickerResult {
        return launchDocumentPicker(config, allowMultiple = true, defaultMediaType = MediaType.AUDIO)
    }

    private suspend fun launchPhotoPicker(
        config: MediaPickerConfig,
        filter: PHPickerFilter?,
        selectionLimit: Int,
        defaultMediaType: MediaType
    ): MediaPickerResult = suspendCancellableCoroutine { continuation ->
        val topViewController = presenter.getTopViewController()
        if (topViewController == null) {
            continuation.resume(
                MediaPickerResult.Error(
                    MediaPickerError.OperationFailed(message = "No active UIViewController found to present PHPicker.")
                )
            )
            return@suspendCancellableCoroutine
        }

        val pickerConfig = PHPickerConfiguration().apply {
            this.selectionLimit = if (selectionLimit >= Int.MAX_VALUE) 0L else selectionLimit.toLong()
            this.filter = filter
        }

        val pickerController = PHPickerViewController(configuration = pickerConfig)
        val delegate = IOSPhotoPickerDelegate(
            continuation = continuation,
            config = config,
            defaultMediaType = defaultMediaType,
            onComplete = { activePhotoDelegate = null }
        )
        activePhotoDelegate = delegate
        pickerController.delegate = delegate

        continuation.invokeOnCancellation {
            activePhotoDelegate = null
            presenter.dismiss(pickerController)
        }

        presenter.present(
            controller = pickerController,
            onFailure = {
                activePhotoDelegate = null
                if (continuation.isActive) {
                    continuation.resume(
                        MediaPickerResult.Error(
                            MediaPickerError.OperationFailed(message = "Failed to present PHPicker: top UIViewController unavailable on main thread.")
                        )
                    )
                }
            }
        )
    }

    private suspend fun launchDocumentPicker(
        config: MediaPickerConfig,
        allowMultiple: Boolean,
        defaultMediaType: MediaType = MediaType.DOCUMENT
    ): MediaPickerResult = suspendCancellableCoroutine { continuation ->
        val topViewController = presenter.getTopViewController()
        if (topViewController == null) {
            continuation.resume(
                MediaPickerResult.Error(
                    MediaPickerError.OperationFailed(message = "No active UIViewController found to present DocumentPicker.")
                )
            )
            return@suspendCancellableCoroutine
        }

        val contentTypes = IOSMetadataExtractor.mimeTypesToUTTypes(
            mimeTypes = config.allowedMimeTypes,
            defaultMediaType = defaultMediaType
        )
        val documentPicker = UIDocumentPickerViewController(forOpeningContentTypes = contentTypes).apply {
            allowsMultipleSelection = allowMultiple
        }

        val delegate = IOSDocumentPickerDelegate(
            continuation = continuation,
            config = config,
            defaultMediaType = defaultMediaType,
            onComplete = { activeDocumentDelegate = null }
        )
        activeDocumentDelegate = delegate
        documentPicker.delegate = delegate

        continuation.invokeOnCancellation {
            activeDocumentDelegate = null
            presenter.dismiss(documentPicker)
        }

        presenter.present(
            controller = documentPicker,
            onFailure = {
                activeDocumentDelegate = null
                if (continuation.isActive) {
                    continuation.resume(
                        MediaPickerResult.Error(
                            MediaPickerError.OperationFailed(message = "Failed to present DocumentPicker: top UIViewController unavailable on main thread.")
                        )
                    )
                }
            }
        )
    }
}
