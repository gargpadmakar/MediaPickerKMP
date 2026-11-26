package io.github.padmakargarg.mediapicker

import io.github.padmakargarg.mediapicker.domain.model.MediaPickerConfig
import io.github.padmakargarg.mediapicker.domain.model.MediaPickerError
import io.github.padmakargarg.mediapicker.domain.model.MediaPickerResult
import io.github.padmakargarg.mediapicker.util.IOSMetadataExtractor
import kotlinx.coroutines.test.runTest
import platform.UIKit.UIViewController
import platform.UniformTypeIdentifiers.*
import kotlin.test.*

class IOSMediaPickerTest {

    @Test
    fun testPlatformNameIsIOS() {
        val picker = IOSMediaPicker()
        assertEquals("iOS", picker.platformName)
    }

    @Test
    fun testBindAndUnbind() {
        val picker = IOSMediaPicker()
        val dummyController = UIViewController()
        picker.bind(dummyController)
        picker.unbind()
    }

    @Test
    fun testMimeTypesToUTTypes_wildcardsAndDefaults() {
        val emptyTypes = IOSMetadataExtractor.mimeTypesToUTTypes(emptyList())
        assertEquals(listOf(UTTypeItem), emptyTypes)

        val anyTypes = IOSMetadataExtractor.mimeTypesToUTTypes(listOf("*/*"))
        assertEquals(listOf(UTTypeItem), anyTypes)

        val imageTypes = IOSMetadataExtractor.mimeTypesToUTTypes(listOf("image/*"))
        assertEquals(listOf(UTTypeImage), imageTypes)

        val videoTypes = IOSMetadataExtractor.mimeTypesToUTTypes(listOf("video/*"))
        assertEquals(listOf(UTTypeMovie), videoTypes)

        val audioTypes = IOSMetadataExtractor.mimeTypesToUTTypes(listOf("audio/*"))
        assertEquals(listOf(UTTypeAudio), audioTypes)

        val pdfTypes = IOSMetadataExtractor.mimeTypesToUTTypes(listOf("application/pdf"))
        assertEquals(listOf(UTTypePDF), pdfTypes)
    }

    @Test
    fun testPhase6StubsReturnError() = runTest {
        val picker = IOSMediaPicker()
        val config = MediaPickerConfig.DEFAULT

        val captureResult = picker.capturePhoto(config)
        assertIs<MediaPickerResult.Error>(captureResult)
        assertIs<MediaPickerError.OperationFailed>(captureResult.error)
        assertTrue(captureResult.error.message.contains("Phase 6"))

        val audioResult = picker.pickAudio(config)
        assertIs<MediaPickerResult.Error>(audioResult)
        assertIs<MediaPickerError.OperationFailed>(audioResult.error)
        assertTrue(audioResult.error.message.contains("Phase 6"))

        val multiAudioResult = picker.pickMultipleAudio(config)
        assertIs<MediaPickerResult.Error>(multiAudioResult)
        assertIs<MediaPickerError.OperationFailed>(multiAudioResult.error)
        assertTrue(multiAudioResult.error.message.contains("Phase 6"))
    }
}
