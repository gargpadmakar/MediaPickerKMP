package io.github.padmakargarg.mediapicker

import androidx.activity.ComponentActivity
import io.github.padmakargarg.mediapicker.bridge.AndroidMediaPickerBridge
import io.github.padmakargarg.mediapicker.data.repository.MediaPickerRepositoryImpl
import io.github.padmakargarg.mediapicker.domain.model.MediaPickerConfig
import io.github.padmakargarg.mediapicker.domain.model.MediaPickerResult

/**
 * Android implementation of [MediaPicker].
 *
 * Provides native Android integration with the AndroidX Photo Picker for images/videos
 * and the Storage Access Framework (SAF) for documents and files.
 *
 * @param activity Optional [ComponentActivity] to bind upon creation.
 */
class AndroidMediaPicker(
    activity: ComponentActivity? = null
) : MediaPicker {

    override val platformName: String = "Android"

    private val bridge = AndroidMediaPickerBridge(activity)
    private val delegate = DefaultMediaPicker(MediaPickerRepositoryImpl(bridge), platformName)

    /**
     * Binds an active [ComponentActivity] to this picker.
     */
    fun bind(activity: ComponentActivity) {
        bridge.bind(activity)
    }

    /**
     * Unbinds the activity to prevent any references or memory leaks.
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

    override suspend fun pickFile(config: MediaPickerConfig): MediaPickerResult =
        delegate.pickFile(config)

    override suspend fun pickMultipleFiles(config: MediaPickerConfig): MediaPickerResult =
        delegate.pickMultipleFiles(config)

    override suspend fun pickAudio(config: MediaPickerConfig): MediaPickerResult =
        delegate.pickAudio(config)

    override suspend fun pickMultipleAudio(config: MediaPickerConfig): MediaPickerResult =
        delegate.pickMultipleAudio(config)
}
