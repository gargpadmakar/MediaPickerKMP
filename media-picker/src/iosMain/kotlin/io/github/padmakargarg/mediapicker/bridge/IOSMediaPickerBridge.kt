package io.github.padmakargarg.mediapicker.bridge

import io.github.padmakargarg.mediapicker.data.contract.PlatformMediaPickerBridge
import io.github.padmakargarg.mediapicker.domain.model.MediaPickerConfig
import io.github.padmakargarg.mediapicker.domain.model.MediaPickerError
import io.github.padmakargarg.mediapicker.domain.model.MediaPickerResult
import io.github.padmakargarg.mediapicker.domain.model.MediaType
import io.github.padmakargarg.mediapicker.util.IOSMetadataExtractor
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.PhotosUI.PHPickerConfiguration
import platform.PhotosUI.PHPickerFilter
import platform.PhotosUI.PHPickerViewController
import platform.UIKit.UIDocumentPickerViewController
import platform.UIKit.UIViewController
import kotlin.coroutines.resume

/**
 * iOS platform bridge connecting domain operations to native PHPicker and UIDocumentPicker.
 */
class IOSMediaPickerBridge(
    initialViewController: UIViewController? = null
) : PlatformMediaPickerBridge {

    private val presenter = IOSViewControllerPresenter(initialViewController)

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
        return launchDocumentPicker(config, allowMultiple = false)
    }

    override suspend fun pickMultipleFiles(config: MediaPickerConfig): MediaPickerResult {
        return launchDocumentPicker(config, allowMultiple = true)
    }

    override suspend fun capturePhoto(config: MediaPickerConfig): MediaPickerResult {
        return MediaPickerResult.Error(
            MediaPickerError.OperationFailed(message = "iOS Camera Capture will be implemented in Phase 6.")
        )
    }

    override suspend fun pickAudio(config: MediaPickerConfig): MediaPickerResult {
        return MediaPickerResult.Error(
            MediaPickerError.OperationFailed(message = "iOS Audio Picker will be implemented in Phase 6.")
        )
    }

    override suspend fun pickMultipleAudio(config: MediaPickerConfig): MediaPickerResult {
        return MediaPickerResult.Error(
            MediaPickerError.OperationFailed(message = "iOS Audio Picker will be implemented in Phase 6.")
        )
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
            this.selectionLimit = selectionLimit.toLong()
            this.filter = filter
        }

        val pickerController = PHPickerViewController(configuration = pickerConfig)
        val delegate = IOSPhotoPickerDelegate(continuation, config, defaultMediaType)
        pickerController.delegate = delegate

        continuation.invokeOnCancellation {
            presenter.dismiss(pickerController)
        }

        presenter.present(pickerController)
    }

    private suspend fun launchDocumentPicker(
        config: MediaPickerConfig,
        allowMultiple: Boolean
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

        val contentTypes = IOSMetadataExtractor.mimeTypesToUTTypes(config.allowedMimeTypes)
        val documentPicker = UIDocumentPickerViewController(forOpeningContentTypes = contentTypes).apply {
            allowsMultipleSelection = allowMultiple
        }

        val delegate = IOSDocumentPickerDelegate(continuation, config)
        documentPicker.delegate = delegate

        continuation.invokeOnCancellation {
            presenter.dismiss(documentPicker)
        }

        presenter.present(documentPicker)
    }
}
