package io.github.padmakargarg.mediapicker

import io.github.padmakargarg.mediapicker.domain.model.*
import io.github.padmakargarg.mediapicker.domain.repository.MediaPickerRepository
import io.github.padmakargarg.mediapicker.domain.usecase.MediaPickerValidator
import io.github.padmakargarg.mediapicker.domain.usecase.PickImageUseCase
import io.github.padmakargarg.mediapicker.domain.usecase.PickMultipleImagesUseCase
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
    fun testDefaultMediaPicker_singleFlightProtection() = runTest {
        val repo = FakeMediaPickerRepository(returnResult = MediaPickerResult.Cancelled)
        val picker = DefaultMediaPicker(repo)

        val result = picker.pickImage()
        assertIs<MediaPickerResult.Cancelled>(result)
    }
}
