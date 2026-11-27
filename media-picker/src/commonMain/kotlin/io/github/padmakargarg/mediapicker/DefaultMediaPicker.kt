package io.github.padmakargarg.mediapicker

import io.github.padmakargarg.mediapicker.domain.model.MediaPickerConfig
import io.github.padmakargarg.mediapicker.domain.model.MediaPickerError
import io.github.padmakargarg.mediapicker.domain.model.MediaPickerResult
import io.github.padmakargarg.mediapicker.domain.repository.MediaPickerRepository
import io.github.padmakargarg.mediapicker.domain.usecase.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex

/**
 * Thread-safe default implementation of [MediaPicker] wrapping domain use cases.
 *
 * Guarantees single-flight launch protection using a mutex, preventing duplicate concurrent launches.
 */
class DefaultMediaPicker(
    private val repository: MediaPickerRepository,
    override val platformName: String = "Common"
) : MediaPicker {

    private val pickImageUseCase = PickImageUseCase(repository)
    private val pickMultipleImagesUseCase = PickMultipleImagesUseCase(repository)
    private val pickVideoUseCase = PickVideoUseCase(repository)
    private val pickMultipleVideosUseCase = PickMultipleVideosUseCase(repository)
    private val pickMediaUseCase = PickMediaUseCase(repository)
    private val capturePhotoUseCase = CapturePhotoUseCase(repository)
    private val captureVideoUseCase = CaptureVideoUseCase(repository)
    private val pickFileUseCase = PickFileUseCase(repository)
    private val pickMultipleFilesUseCase = PickMultipleFilesUseCase(repository)
    private val pickAudioUseCase = PickAudioUseCase(repository)
    private val pickMultipleAudioUseCase = PickMultipleAudioUseCase(repository)

    private val launchMutex = Mutex()

    private suspend inline fun executeGuarded(
        crossinline block: suspend () -> MediaPickerResult
    ): MediaPickerResult {
        if (!launchMutex.tryLock()) {
            return MediaPickerResult.Error(
                MediaPickerError.OperationFailed(
                    message = "A media picker operation is already in progress."
                )
            )
        }
        return try {
            block()
        } catch (e: CancellationException) {
            MediaPickerResult.Cancelled
        } catch (t: Throwable) {
            MediaPickerResult.Error(MediaPickerError.OperationFailed(cause = t))
        } finally {
            launchMutex.unlock()
        }
    }

    override suspend fun pickImage(config: MediaPickerConfig): MediaPickerResult =
        executeGuarded { pickImageUseCase(config) }

    override suspend fun pickMultipleImages(config: MediaPickerConfig): MediaPickerResult =
        executeGuarded { pickMultipleImagesUseCase(config) }

    override suspend fun pickVideo(config: MediaPickerConfig): MediaPickerResult =
        executeGuarded { pickVideoUseCase(config) }

    override suspend fun pickMultipleVideos(config: MediaPickerConfig): MediaPickerResult =
        executeGuarded { pickMultipleVideosUseCase(config) }

    override suspend fun pickMedia(config: MediaPickerConfig): MediaPickerResult =
        executeGuarded { pickMediaUseCase(config) }

    override suspend fun capturePhoto(config: MediaPickerConfig): MediaPickerResult =
        executeGuarded { capturePhotoUseCase(config) }

    override suspend fun captureVideo(config: MediaPickerConfig): MediaPickerResult =
        executeGuarded { captureVideoUseCase(config) }

    override suspend fun pickFile(config: MediaPickerConfig): MediaPickerResult =
        executeGuarded { pickFileUseCase(config) }

    override suspend fun pickMultipleFiles(config: MediaPickerConfig): MediaPickerResult =
        executeGuarded { pickMultipleFilesUseCase(config) }

    override suspend fun pickAudio(config: MediaPickerConfig): MediaPickerResult =
        executeGuarded { pickAudioUseCase(config) }

    override suspend fun pickMultipleAudio(config: MediaPickerConfig): MediaPickerResult =
        executeGuarded { pickMultipleAudioUseCase(config) }
}
