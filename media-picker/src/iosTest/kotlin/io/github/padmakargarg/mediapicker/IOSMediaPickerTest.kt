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

        val parameterTypes = IOSMetadataExtractor.mimeTypesToUTTypes(listOf("application/pdf; charset=utf-8", " IMAGE/* "))
        assertEquals(listOf(UTTypePDF, UTTypeImage), parameterTypes)
    }

    @Test
    fun testAudioMimeTypesToUTTypes() {
        val audioWildcard = IOSMetadataExtractor.mimeTypesToUTTypes(listOf("audio/*"))
        assertEquals(listOf(UTTypeAudio), audioWildcard)

        val defaultAudio = IOSMetadataExtractor.mimeTypesToUTTypes(emptyList(), defaultMediaType = io.github.padmakargarg.mediapicker.domain.model.MediaType.AUDIO)
        assertEquals(listOf(UTTypeAudio), defaultAudio)

        val mp3Types = IOSMetadataExtractor.mimeTypesToUTTypes(listOf("audio/mpeg", "audio/mp3"))
        assertEquals(listOf(UTTypeMP3), mp3Types)

        val wavTypes = IOSMetadataExtractor.mimeTypesToUTTypes(listOf("audio/wav", "audio/x-wav"))
        assertEquals(listOf(UTTypeWAV), wavTypes)

        val multiAudio = IOSMetadataExtractor.mimeTypesToUTTypes(
            listOf("audio/mpeg", "audio/wav", "audio/flac", "audio/m4a", "audio/ogg", "audio/opus")
        )
        assertTrue(multiAudio.contains(UTTypeMP3))
        assertTrue(multiAudio.contains(UTTypeWAV))
        assertTrue(multiAudio.isNotEmpty())
    }

    @Test
    fun testCameraAvailabilityHandling() = runTest {
        val picker = IOSMediaPicker()
        val config = MediaPickerConfig.DEFAULT

        val isCameraHardwareAvailable = platform.UIKit.UIImagePickerController.isSourceTypeAvailable(
            platform.UIKit.UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera
        )

        val captureResult = picker.capturePhoto(config)

        if (!isCameraHardwareAvailable) {
            assertIs<MediaPickerResult.Error>(captureResult)
            assertIs<MediaPickerError.CameraUnavailable>(captureResult.error)
            assertTrue(captureResult.error.message.contains("Camera hardware is not available"))
        }

        val videoCaptureResult = picker.captureVideo(config)
        if (!isCameraHardwareAvailable) {
            assertIs<MediaPickerResult.Error>(videoCaptureResult)
            assertIs<MediaPickerError.CameraUnavailable>(videoCaptureResult.error)
            assertTrue(videoCaptureResult.error.message.contains("Camera hardware is not available"))
        }
    }
}
