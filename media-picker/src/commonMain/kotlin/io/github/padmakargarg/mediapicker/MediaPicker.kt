package io.github.padmakargarg.mediapicker

import io.github.padmakargarg.mediapicker.domain.model.MediaPickerConfig
import io.github.padmakargarg.mediapicker.domain.model.MediaPickerResult

/**
 * Primary public contract for MediaPicker operations.
 *
 * Provides lifecycle-safe, coroutine-based media selection across Android and iOS.
 */
interface MediaPicker {
    /**
     * Platform identifier (e.g. "Android", "iOS").
     */
    val platformName: String

    /**
     * Picks a single image from the gallery.
     */
    suspend fun pickImage(config: MediaPickerConfig = MediaPickerConfig.DEFAULT): MediaPickerResult

    /**
     * Picks multiple images from the gallery.
     */
    suspend fun pickMultipleImages(config: MediaPickerConfig = MediaPickerConfig.multiple()): MediaPickerResult

    /**
     * Picks a single video from the gallery.
     */
    suspend fun pickVideo(config: MediaPickerConfig = MediaPickerConfig.DEFAULT): MediaPickerResult

    /**
     * Picks multiple videos from the gallery.
     */
    suspend fun pickMultipleVideos(config: MediaPickerConfig = MediaPickerConfig.multiple()): MediaPickerResult

    /**
     * Picks media (mixed images and videos) from the gallery.
     */
    suspend fun pickMedia(config: MediaPickerConfig = MediaPickerConfig.DEFAULT): MediaPickerResult

    /**
     * Captures a photo using the device camera.
     */
    suspend fun capturePhoto(config: MediaPickerConfig = MediaPickerConfig.DEFAULT): MediaPickerResult

    /**
     * Picks a single file or document.
     */
    suspend fun pickFile(config: MediaPickerConfig = MediaPickerConfig.DEFAULT): MediaPickerResult

    /**
     * Picks multiple files or documents.
     */
    suspend fun pickMultipleFiles(config: MediaPickerConfig = MediaPickerConfig.multiple()): MediaPickerResult

    /**
     * Picks a single audio track.
     */
    suspend fun pickAudio(config: MediaPickerConfig = MediaPickerConfig.DEFAULT): MediaPickerResult

    /**
     * Picks multiple audio tracks.
     */
    suspend fun pickMultipleAudio(config: MediaPickerConfig = MediaPickerConfig.multiple()): MediaPickerResult
}
