package io.github.padmakargarg.mediapicker

import io.github.padmakargarg.mediapicker.domain.model.MediaPickerConfig
import io.github.padmakargarg.mediapicker.domain.model.MediaPickerResult

/**
 * Android implementation stub for MediaPicker.
 * Full native Android PhotoPicker, SAF and Camera implementation will be delivered in Phases 3-4.
 */
class AndroidMediaPicker : MediaPicker {
    override val platformName: String = "Android"

    override suspend fun pickImage(config: MediaPickerConfig): MediaPickerResult = MediaPickerResult.Cancelled
    override suspend fun pickMultipleImages(config: MediaPickerConfig): MediaPickerResult = MediaPickerResult.Cancelled
    override suspend fun pickVideo(config: MediaPickerConfig): MediaPickerResult = MediaPickerResult.Cancelled
    override suspend fun pickMultipleVideos(config: MediaPickerConfig): MediaPickerResult = MediaPickerResult.Cancelled
    override suspend fun pickMedia(config: MediaPickerConfig): MediaPickerResult = MediaPickerResult.Cancelled
    override suspend fun capturePhoto(config: MediaPickerConfig): MediaPickerResult = MediaPickerResult.Cancelled
    override suspend fun pickFile(config: MediaPickerConfig): MediaPickerResult = MediaPickerResult.Cancelled
    override suspend fun pickMultipleFiles(config: MediaPickerConfig): MediaPickerResult = MediaPickerResult.Cancelled
    override suspend fun pickAudio(config: MediaPickerConfig): MediaPickerResult = MediaPickerResult.Cancelled
    override suspend fun pickMultipleAudio(config: MediaPickerConfig): MediaPickerResult = MediaPickerResult.Cancelled
}
