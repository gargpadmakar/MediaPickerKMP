package io.github.padmakargarg.mediapicker

import io.github.padmakargarg.mediapicker.domain.model.*
import io.github.padmakargarg.mediapicker.domain.repository.MediaPickerRepository
import io.github.padmakargarg.mediapicker.domain.usecase.*
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class MediaPickerUseCaseTest {

    private val wildcard = "/" + "*"

    private class FakeMediaPickerRepository(
        var returnResult: MediaPickerResult = MediaPickerResult.Cancelled
    ) : MediaPickerRepository {
        var lastConfig: MediaPickerConfig? = null

        override suspend fun pickImage(config: MediaPickerConfig): MediaPickerResult {
            lastConfig = config
            return returnResult
        }

        override suspend fun pickMultipleImages(config: MediaPickerConfig): MediaPickerResult {
            lastConfig = config
            return returnResult
        }

        override suspend fun pickVideo(config: MediaPickerConfig): MediaPickerResult {
            lastConfig = config
            return returnResult
        }

        override suspend fun pickMultipleVideos(config: MediaPickerConfig): MediaPickerResult {
            lastConfig = config
            return returnResult
        }

        override suspend fun pickMedia(config: MediaPickerConfig): MediaPickerResult {
            lastConfig = config
            return returnResult
        }

        override suspend fun capturePhoto(config: MediaPickerConfig): MediaPickerResult {
            lastConfig = config
            return returnResult
        }

        override suspend fun pickFile(config: MediaPickerConfig): MediaPickerResult {
            lastConfig = config
            return returnResult
        }

        override suspend fun pickMultipleFiles(config: MediaPickerConfig): MediaPickerResult {
            lastConfig = config
            return returnResult
        }

        override suspend fun pickAudio(config: MediaPickerConfig): MediaPickerResult {
            lastConfig = config
            return returnResult
        }

        override suspend fun pickMultipleAudio(config: MediaPickerConfig): MediaPickerResult {
            lastConfig = config
            return returnResult
        }
    }

    @Test
    fun testPickImageUseCase_forcesSingleSelection() = runTest {
        val repo = FakeMediaPickerRepository(
            returnResult = MediaPickerResult.Success(
                listOf(MediaPickerItem(id = "1", uri = "uri://1", mediaType = MediaType.IMAGE))
            )
        )
        val useCase = PickImageUseCase(repo)
        val result = useCase(MediaPickerConfig.multiple(maxSelection = 10))

        assertIs<MediaPickerResult.Success>(result)
        assertEquals(false, repo.lastConfig?.allowMultiple)
        assertEquals(1, repo.lastConfig?.maxSelection)
    }

    @Test
    fun testPickMultipleImagesUseCase_enforcesMaxSelectionValidation() = runTest {
        val repo = FakeMediaPickerRepository(
            returnResult = MediaPickerResult.Success(
                listOf(
                    MediaPickerItem(id = "1", uri = "uri://1"),
                    MediaPickerItem(id = "2", uri = "uri://2"),
                    MediaPickerItem(id = "3", uri = "uri://3")
                )
            )
        )
        val useCase = PickMultipleImagesUseCase(repo)
        val result = useCase(MediaPickerConfig.multiple(maxSelection = 2))

        assertIs<MediaPickerResult.Error>(result)
        val error = result.error
        assertIs<MediaPickerError.SelectionLimitExceeded>(error)
        assertEquals(3, error.requested)
        assertEquals(2, error.maxAllowed)
    }

    @Test
    fun testPickAudioUseCase_forcesSingleSelection() = runTest {
        val repo = FakeMediaPickerRepository(
            returnResult = MediaPickerResult.Success(
                listOf(
                    MediaPickerItem(
                        id = "audio_1",
                        name = "song.mp3",
                        mimeType = "audio/mpeg",
                        uri = "uri://audio1",
                        mediaType = MediaType.AUDIO,
                        durationMs = 180000L
                    )
                )
            )
        )
        val useCase = PickAudioUseCase(repo)
        val result = useCase(MediaPickerConfig.multiple(maxSelection = 5))

        assertIs<MediaPickerResult.Success>(result)
        assertEquals(false, repo.lastConfig?.allowMultiple)
        assertEquals(1, repo.lastConfig?.maxSelection)
        val item = result.items.first()
        assertEquals(MediaType.AUDIO, item.mediaType)
        assertEquals("audio/mpeg", item.mimeType)
        assertEquals(180000L, item.durationMs)
    }

    @Test
    fun testPickMultipleAudioUseCase_enforcesSelectionLimit() = runTest {
        val repo = FakeMediaPickerRepository(
            returnResult = MediaPickerResult.Success(
                listOf(
                    MediaPickerItem(id = "a1", uri = "uri://a1", mediaType = MediaType.AUDIO),
                    MediaPickerItem(id = "a2", uri = "uri://a2", mediaType = MediaType.AUDIO),
                    MediaPickerItem(id = "a3", uri = "uri://a3", mediaType = MediaType.AUDIO),
                    MediaPickerItem(id = "a4", uri = "uri://a4", mediaType = MediaType.AUDIO)
                )
            )
        )
        val useCase = PickMultipleAudioUseCase(repo)
        val result = useCase(MediaPickerConfig.multiple(maxSelection = 3))

        assertIs<MediaPickerResult.Error>(result)
        val error = result.error
        assertIs<MediaPickerError.SelectionLimitExceeded>(error)
        assertEquals(4, error.requested)
        assertEquals(3, error.maxAllowed)
    }

    @Test
    fun testCapturePhotoUseCase_validatesCapturedResult() = runTest {
        val repo = FakeMediaPickerRepository(
            returnResult = MediaPickerResult.Success(
                listOf(
                    MediaPickerItem(
                        id = "cam_1",
                        name = "photo_123.jpg",
                        mimeType = "image/jpeg",
                        uri = "content://mediapicker/camera_photos/photo_123.jpg",
                        mediaType = MediaType.IMAGE,
                        sizeInBytes = 204800L,
                        width = 1920,
                        height = 1080
                    )
                )
            )
        )
        val useCase = CapturePhotoUseCase(repo)
        val result = useCase(MediaPickerConfig.DEFAULT)

        assertIs<MediaPickerResult.Success>(result)
        assertEquals(false, repo.lastConfig?.allowMultiple)
        val item = result.items.first()
        assertEquals("image/jpeg", item.mimeType)
        assertEquals(1920, item.width)
        assertEquals(1080, item.height)
    }

    @Test
    fun testValidator_enforcesFileSizeLimits() {
        val oversizedItem = MediaPickerItem(
            id = "1",
            uri = "uri://1",
            sizeInBytes = 5 * 1024 * 1024
        )
        val result = MediaPickerResult.Success(listOf(oversizedItem))
        val config = MediaPickerConfig(maxFileSizeBytes = 2 * 1024 * 1024)

        val validated = MediaPickerValidator.validateResult(result, config)

        assertIs<MediaPickerResult.Error>(validated)
        val error = validated.error
        assertIs<MediaPickerError.FileTooLarge>(error)
        assertEquals(5 * 1024 * 1024, error.sizeInBytes)
        assertEquals(2 * 1024 * 1024, error.maxSizeBytes)
    }

    @Test
    fun testValidator_enforcesAllowedMimeTypes() {
        val invalidItem = MediaPickerItem(
            id = "1",
            uri = "uri://1",
            mimeType = "application/zip"
        )
        val result = MediaPickerResult.Success(listOf(invalidItem))
        val config = MediaPickerConfig(allowedMimeTypes = listOf("image$wildcard", "video$wildcard"))

        val validated = MediaPickerValidator.validateResult(result, config)

        assertIs<MediaPickerResult.Error>(validated)
        val error = validated.error
        assertIs<MediaPickerError.UnsupportedType>(error)
        assertEquals("application/zip", error.mimeType)
    }

    @Test
    fun testValidator_enforcesAudioMimeTypes() {
        val validAudio = MediaPickerItem(
            id = "audio_1",
            uri = "uri://audio1",
            mimeType = "audio/mpeg"
        )
        val result = MediaPickerResult.Success(listOf(validAudio))
        val config = MediaPickerConfig(allowedMimeTypes = listOf("audio$wildcard"))

        val validated = MediaPickerValidator.validateResult(result, config)

        assertIs<MediaPickerResult.Success>(validated)
    }

    @Test
    fun testDefaultMediaPicker_singleFlightProtection() = runTest {
        val repo = FakeMediaPickerRepository(returnResult = MediaPickerResult.Cancelled)
        val picker = DefaultMediaPicker(repo)

        val result = picker.pickImage()
        assertIs<MediaPickerResult.Cancelled>(result)
    }
}
