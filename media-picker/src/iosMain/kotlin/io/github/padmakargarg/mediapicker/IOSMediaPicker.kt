package io.github.padmakargarg.mediapicker

import io.github.padmakargarg.mediapicker.bridge.IOSMediaPickerBridge
import io.github.padmakargarg.mediapicker.data.repository.MediaPickerRepositoryImpl
import io.github.padmakargarg.mediapicker.domain.model.MediaPickerConfig
import io.github.padmakargarg.mediapicker.domain.model.MediaPickerResult
import platform.UIKit.UIViewController

/**
 * iOS implementation of [MediaPicker].
 *
 * Provides native integration with iOS Photos picker ([platform.PhotosUI.PHPickerViewController])
 * for images and videos, and [platform.UIKit.UIDocumentPickerViewController] for documents and files.
 *
 * @param viewController Optional [UIViewController] to bind upon creation. If null, the picker
 * dynamically discovers the active top-most view controller.
 */
class IOSMediaPicker(
    viewController: UIViewController? = null
) : MediaPicker {

    override val platformName: String = "iOS"

    private val bridge = IOSMediaPickerBridge(viewController)
    private val delegate = DefaultMediaPicker(MediaPickerRepositoryImpl(bridge), platformName)

    /**
     * Binds an active [UIViewController] to this picker.
     */
    fun bind(viewController: UIViewController) {
        bridge.bind(viewController)
    }

    /**
     * Unbinds the view controller to prevent any retain cycles or memory leaks.
     */
    fun unbind() {
        bridge.unbind()
    }

    override suspend fun pickImage(config: MediaPickerConfig): MediaPickerResult =
        delegate.pickImage(config)

    override suspend fun pickMultipleImages(config: MediaPickerConfig): MediaPickerResult =
        delegate.pickMultipleImages(config)

    override suspend fun pickVideo(config: MediaPickerConfig): MediaPickerResult =
        delegate.pickVideo(config)

    override suspend fun pickMultipleVideos(config: MediaPickerConfig): MediaPickerResult =
        delegate.pickMultipleVideos(config)

    override suspend fun pickMedia(config: MediaPickerConfig): MediaPickerResult =
        delegate.pickMedia(config)

    override suspend fun capturePhoto(config: MediaPickerConfig): MediaPickerResult =
        delegate.capturePhoto(config)

    override suspend fun captureVideo(config: MediaPickerConfig): MediaPickerResult =
        delegate.captureVideo(config)

    override suspend fun pickFile(config: MediaPickerConfig): MediaPickerResult =
        delegate.pickFile(config)

    override suspend fun pickMultipleFiles(config: MediaPickerConfig): MediaPickerResult =
        delegate.pickMultipleFiles(config)

    override suspend fun pickAudio(config: MediaPickerConfig): MediaPickerResult =
        delegate.pickAudio(config)

    override suspend fun pickMultipleAudio(config: MediaPickerConfig): MediaPickerResult =
        delegate.pickMultipleAudio(config)
}
