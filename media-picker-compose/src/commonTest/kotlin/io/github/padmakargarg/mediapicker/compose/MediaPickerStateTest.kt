package io.github.padmakargarg.mediapicker.compose

import io.github.padmakargarg.mediapicker.MediaPicker
import io.github.padmakargarg.mediapicker.domain.model.MediaPickerConfig
import io.github.padmakargarg.mediapicker.domain.model.MediaPickerError
import io.github.padmakargarg.mediapicker.domain.model.MediaPickerItem
import io.github.padmakargarg.mediapicker.domain.model.MediaPickerResult
import io.github.padmakargarg.mediapicker.domain.model.MediaType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class MediaPickerStateTest {

    private class FakeMediaPicker : MediaPicker {
        override val platformName: String = "TestPlatform"
        var resultToReturn: MediaPickerResult = MediaPickerResult.Cancelled

        override suspend fun pickImage(config: MediaPickerConfig): MediaPickerResult = resultToReturn
        override suspend fun pickMultipleImages(config: MediaPickerConfig): MediaPickerResult = resultToReturn
        override suspend fun pickVideo(config: MediaPickerConfig): MediaPickerResult = resultToReturn
        override suspend fun pickMultipleVideos(config: MediaPickerConfig): MediaPickerResult = resultToReturn
        override suspend fun pickMedia(config: MediaPickerConfig): MediaPickerResult = resultToReturn
        override suspend fun capturePhoto(config: MediaPickerConfig): MediaPickerResult = resultToReturn
        override suspend fun pickFile(config: MediaPickerConfig): MediaPickerResult = resultToReturn
        override suspend fun pickMultipleFiles(config: MediaPickerConfig): MediaPickerResult = resultToReturn
        override suspend fun pickAudio(config: MediaPickerConfig): MediaPickerResult = resultToReturn
        override suspend fun pickMultipleAudio(config: MediaPickerConfig): MediaPickerResult = resultToReturn
    }

    @Test
    fun testInitialStateIsIdle() {
        val testDispatcher = StandardTestDispatcher()
        val testScope = TestScope(testDispatcher)
        val fakePicker = FakeMediaPicker()

        val state = MediaPickerState(fakePicker, testScope)

        assertEquals(MediaPickerUiState.Idle, state.uiState)
        assertTrue(state.items.isEmpty())
        assertFalse(state.isLoading)
    }

    @Test
    fun testSuccessfulPickUpdatesStateToSuccess() = runTest {
        val fakePicker = FakeMediaPicker()
        val sampleItem = MediaPickerItem(
            id = "1",
            name = "test.jpg",
            mimeType = "image/jpeg",
            extension = "jpg",
            sizeInBytes = 1024L,
            mediaType = MediaType.IMAGE,
            uri = "content://media/1"
        )
        fakePicker.resultToReturn = MediaPickerResult.Success(listOf(sampleItem))

        val state = MediaPickerState(fakePicker, this)
        state.pickImage()
        testScheduler.advanceUntilIdle()

        assertIs<MediaPickerUiState.Success>(state.uiState)
        assertEquals(listOf(sampleItem), state.items)
        assertFalse(state.isLoading)
    }

    @Test
    fun testCancelledPickUpdatesStateToCancelled() = runTest {
        val fakePicker = FakeMediaPicker()
        fakePicker.resultToReturn = MediaPickerResult.Cancelled

        val state = MediaPickerState(fakePicker, this)
        state.pickFile()
        testScheduler.advanceUntilIdle()

        assertEquals(MediaPickerUiState.Cancelled, state.uiState)
        assertTrue(state.items.isEmpty())
    }

    @Test
    fun testErrorPickUpdatesStateToError() = runTest {
        val fakePicker = FakeMediaPicker()
        val error = MediaPickerError.PermissionDenied("Permission denied")
        fakePicker.resultToReturn = MediaPickerResult.Error(error)

        val state = MediaPickerState(fakePicker, this)
        state.capturePhoto()
        testScheduler.advanceUntilIdle()

        assertIs<MediaPickerUiState.Error>(state.uiState)
        assertEquals(error, (state.uiState as MediaPickerUiState.Error).error)
    }

    @Test
    fun testClearResetsStateToIdle() = runTest {
        val fakePicker = FakeMediaPicker()
        fakePicker.resultToReturn = MediaPickerResult.Cancelled

        val state = MediaPickerState(fakePicker, this)
        state.pickAudio()
        testScheduler.advanceUntilIdle()

        assertEquals(MediaPickerUiState.Cancelled, state.uiState)
        state.clear()
        assertEquals(MediaPickerUiState.Idle, state.uiState)
    }
}
