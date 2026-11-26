package io.github.padmakargarg.mediapicker

import io.github.padmakargarg.mediapicker.domain.model.MediaPickerConfig
import io.github.padmakargarg.mediapicker.domain.model.MediaPickerError
import io.github.padmakargarg.mediapicker.domain.model.MediaPickerResult
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class AndroidMediaPickerTest {

    @Test
    fun testAndroidMediaPicker_withoutActivityFailsGracefully() = runTest {
        val picker = AndroidMediaPicker()
        assertEquals("Android", picker.platformName)

        val result = picker.pickImage(MediaPickerConfig.DEFAULT)
        assertIs<MediaPickerResult.Error>(result)
        assertIs<MediaPickerError.OperationFailed>(result.error)
    }

    @Test
    fun testAndroidMediaPicker_pickAudioWithoutActivityFailsGracefully() = runTest {
        val picker = AndroidMediaPicker()
        val result = picker.pickAudio(MediaPickerConfig.DEFAULT)
        assertIs<MediaPickerResult.Error>(result)
        assertIs<MediaPickerError.OperationFailed>(result.error)
    }

    @Test
    fun testAndroidMediaPicker_pickMultipleAudioWithoutActivityFailsGracefully() = runTest {
        val picker = AndroidMediaPicker()
        val result = picker.pickMultipleAudio(MediaPickerConfig.multiple())
        assertIs<MediaPickerResult.Error>(result)
        assertIs<MediaPickerError.OperationFailed>(result.error)
    }

    @Test
    fun testAndroidMediaPicker_capturePhotoWithoutActivityFailsGracefully() = runTest {
        val picker = AndroidMediaPicker()
        val result = picker.capturePhoto(MediaPickerConfig.DEFAULT)
        assertIs<MediaPickerResult.Error>(result)
        assertIs<MediaPickerError.OperationFailed>(result.error)
    }
}
