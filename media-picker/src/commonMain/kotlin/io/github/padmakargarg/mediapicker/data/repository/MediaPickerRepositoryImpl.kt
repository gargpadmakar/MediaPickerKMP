package io.github.padmakargarg.mediapicker.data.repository

import io.github.padmakargarg.mediapicker.data.contract.PlatformMediaPickerBridge
import io.github.padmakargarg.mediapicker.domain.model.MediaPickerConfig
import io.github.padmakargarg.mediapicker.domain.model.MediaPickerResult
import io.github.padmakargarg.mediapicker.domain.repository.MediaPickerRepository

/**
 * Default implementation of [MediaPickerRepository] delegating to [PlatformMediaPickerBridge].
 */
class MediaPickerRepositoryImpl(
    private val bridge: PlatformMediaPickerBridge
) : MediaPickerRepository {
    override suspend fun pickImage(config: MediaPickerConfig): MediaPickerResult = bridge.pickImage(config)
    override suspend fun pickMultipleImages(config: MediaPickerConfig): MediaPickerResult = bridge.pickMultipleImages(config)
    override suspend fun pickVideo(config: MediaPickerConfig): MediaPickerResult = bridge.pickVideo(config)
    override suspend fun pickMultipleVideos(config: MediaPickerConfig): MediaPickerResult = bridge.pickMultipleVideos(config)
    override suspend fun pickMedia(config: MediaPickerConfig): MediaPickerResult = bridge.pickMedia(config)
    override suspend fun capturePhoto(config: MediaPickerConfig): MediaPickerResult = bridge.capturePhoto(config)
    override suspend fun captureVideo(config: MediaPickerConfig): MediaPickerResult = bridge.captureVideo(config)
    override suspend fun pickFile(config: MediaPickerConfig): MediaPickerResult = bridge.pickFile(config)
    override suspend fun pickMultipleFiles(config: MediaPickerConfig): MediaPickerResult = bridge.pickMultipleFiles(config)
    override suspend fun pickAudio(config: MediaPickerConfig): MediaPickerResult = bridge.pickAudio(config)
    override suspend fun pickMultipleAudio(config: MediaPickerConfig): MediaPickerResult = bridge.pickMultipleAudio(config)
}
