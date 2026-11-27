package io.github.padmakargarg.mediapicker.domain.repository

import io.github.padmakargarg.mediapicker.domain.model.MediaPickerConfig
import io.github.padmakargarg.mediapicker.domain.model.MediaPickerResult

/**
 * Domain repository contract defining media picker operations.
 */
interface MediaPickerRepository {
    suspend fun pickImage(config: MediaPickerConfig): MediaPickerResult
    suspend fun pickMultipleImages(config: MediaPickerConfig): MediaPickerResult
    suspend fun pickVideo(config: MediaPickerConfig): MediaPickerResult
    suspend fun pickMultipleVideos(config: MediaPickerConfig): MediaPickerResult
    suspend fun pickMedia(config: MediaPickerConfig): MediaPickerResult
    suspend fun capturePhoto(config: MediaPickerConfig): MediaPickerResult
    suspend fun captureVideo(config: MediaPickerConfig): MediaPickerResult
    suspend fun pickFile(config: MediaPickerConfig): MediaPickerResult
    suspend fun pickMultipleFiles(config: MediaPickerConfig): MediaPickerResult
    suspend fun pickAudio(config: MediaPickerConfig): MediaPickerResult
    suspend fun pickMultipleAudio(config: MediaPickerConfig): MediaPickerResult
}
