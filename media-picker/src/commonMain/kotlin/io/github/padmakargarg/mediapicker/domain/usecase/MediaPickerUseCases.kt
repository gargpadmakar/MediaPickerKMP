package io.github.padmakargarg.mediapicker.domain.usecase

import io.github.padmakargarg.mediapicker.domain.model.MediaPickerConfig
import io.github.padmakargarg.mediapicker.domain.model.MediaPickerResult
import io.github.padmakargarg.mediapicker.domain.repository.MediaPickerRepository

class PickImageUseCase(private val repository: MediaPickerRepository) {
    suspend operator fun invoke(config: MediaPickerConfig = MediaPickerConfig.DEFAULT): MediaPickerResult {
        val effectiveConfig = config.copy(allowMultiple = false, maxSelection = 1)
        val result = repository.pickImage(effectiveConfig)
        return MediaPickerValidator.validateResult(result, effectiveConfig)
    }
}

class PickMultipleImagesUseCase(private val repository: MediaPickerRepository) {
    suspend operator fun invoke(config: MediaPickerConfig = MediaPickerConfig.multiple()): MediaPickerResult {
        val effectiveConfig = config.copy(allowMultiple = true)
        val result = repository.pickMultipleImages(effectiveConfig)
        return MediaPickerValidator.validateResult(result, effectiveConfig)
    }
}

class PickVideoUseCase(private val repository: MediaPickerRepository) {
    suspend operator fun invoke(config: MediaPickerConfig = MediaPickerConfig.DEFAULT): MediaPickerResult {
        val effectiveConfig = config.copy(allowMultiple = false, maxSelection = 1)
        val result = repository.pickVideo(effectiveConfig)
        return MediaPickerValidator.validateResult(result, effectiveConfig)
    }
}

class PickMultipleVideosUseCase(private val repository: MediaPickerRepository) {
    suspend operator fun invoke(config: MediaPickerConfig = MediaPickerConfig.multiple()): MediaPickerResult {
        val effectiveConfig = config.copy(allowMultiple = true)
        val result = repository.pickMultipleVideos(effectiveConfig)
        return MediaPickerValidator.validateResult(result, effectiveConfig)
    }
}

class PickMediaUseCase(private val repository: MediaPickerRepository) {
    suspend operator fun invoke(config: MediaPickerConfig = MediaPickerConfig.DEFAULT): MediaPickerResult {
        val result = repository.pickMedia(config)
        return MediaPickerValidator.validateResult(result, config)
    }
}

class CapturePhotoUseCase(private val repository: MediaPickerRepository) {
    suspend operator fun invoke(config: MediaPickerConfig = MediaPickerConfig.DEFAULT): MediaPickerResult {
        val effectiveConfig = config.copy(allowMultiple = false, maxSelection = 1)
        val result = repository.capturePhoto(effectiveConfig)
        return MediaPickerValidator.validateResult(result, effectiveConfig)
    }
}

class CaptureVideoUseCase(private val repository: MediaPickerRepository) {
    suspend operator fun invoke(config: MediaPickerConfig = MediaPickerConfig.DEFAULT): MediaPickerResult {
        val effectiveConfig = config.copy(allowMultiple = false, maxSelection = 1)
        val result = repository.captureVideo(effectiveConfig)
        return MediaPickerValidator.validateResult(result, effectiveConfig)
    }
}

class PickFileUseCase(private val repository: MediaPickerRepository) {
    suspend operator fun invoke(config: MediaPickerConfig = MediaPickerConfig.DEFAULT): MediaPickerResult {
        val effectiveConfig = config.copy(allowMultiple = false, maxSelection = 1)
        val result = repository.pickFile(effectiveConfig)
        return MediaPickerValidator.validateResult(result, effectiveConfig)
    }
}

class PickMultipleFilesUseCase(private val repository: MediaPickerRepository) {
    suspend operator fun invoke(config: MediaPickerConfig = MediaPickerConfig.multiple()): MediaPickerResult {
        val effectiveConfig = config.copy(allowMultiple = true)
        val result = repository.pickMultipleFiles(effectiveConfig)
        return MediaPickerValidator.validateResult(result, effectiveConfig)
    }
}

class PickAudioUseCase(private val repository: MediaPickerRepository) {
    suspend operator fun invoke(config: MediaPickerConfig = MediaPickerConfig.DEFAULT): MediaPickerResult {
        val effectiveConfig = config.copy(allowMultiple = false, maxSelection = 1)
        val result = repository.pickAudio(effectiveConfig)
        return MediaPickerValidator.validateResult(result, effectiveConfig)
    }
}

class PickMultipleAudioUseCase(private val repository: MediaPickerRepository) {
    suspend operator fun invoke(config: MediaPickerConfig = MediaPickerConfig.multiple()): MediaPickerResult {
        val effectiveConfig = config.copy(allowMultiple = true)
        val result = repository.pickMultipleAudio(effectiveConfig)
        return MediaPickerValidator.validateResult(result, effectiveConfig)
    }
}
