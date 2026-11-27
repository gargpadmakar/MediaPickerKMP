package io.github.padmakargarg.mediapicker

import io.github.padmakargarg.mediapicker.domain.model.MediaPickerConfig
import io.github.padmakargarg.mediapicker.domain.model.MediaPickerError
import io.github.padmakargarg.mediapicker.domain.model.MediaPickerResult
import io.github.padmakargarg.mediapicker.util.AndroidMetadataExtractor
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

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

    @Test
    fun testAndroidMediaPicker_captureVideoWithoutActivityFailsGracefully() = runTest {
        val picker = AndroidMediaPicker()
        val result = picker.captureVideo(MediaPickerConfig.DEFAULT)
        assertIs<MediaPickerResult.Error>(result)
        assertIs<MediaPickerError.OperationFailed>(result.error)
    }

    @Test
    fun testAndroidMetadataExtractor_sanitizeFileName_pathTraversalStripped() {
        val unixTraversal = AndroidMetadataExtractor.sanitizeFileName("../../../evil.pdf")
        assertEquals("evil.pdf", unixTraversal)

        val windowsTraversal = AndroidMetadataExtractor.sanitizeFileName("..\\..\\..\\malicious.exe")
        assertEquals("malicious.exe", windowsTraversal)

        val mixedTraversal = AndroidMetadataExtractor.sanitizeFileName("foo/bar\\../../hack.txt")
        assertEquals("hack.txt", mixedTraversal)
    }

    @Test
    fun testAndroidMetadataExtractor_sanitizeFileName_invalidCharactersReplaced() {
        val badChars = AndroidMetadataExtractor.sanitizeFileName("report : * ? < > | # \$.pdf")
        assertFalse(badChars.contains(":"))
        assertFalse(badChars.contains("*"))
        assertFalse(badChars.contains("?"))
        assertFalse(badChars.contains("<"))
        assertFalse(badChars.contains(">"))
        assertFalse(badChars.contains("|"))
        assertTrue(badChars.endsWith(".pdf"))
    }

    @Test
    fun testAndroidMetadataExtractor_sanitizeFileName_blankOrNullFallback() {
        val nullName = AndroidMetadataExtractor.sanitizeFileName(null, "jpg")
        assertTrue(nullName.startsWith("media_"))
        assertTrue(nullName.endsWith(".jpg"))

        val blankName = AndroidMetadataExtractor.sanitizeFileName("   ", "png")
        assertTrue(blankName.startsWith("media_"))
        assertTrue(blankName.endsWith(".png"))

        val dotDots = AndroidMetadataExtractor.sanitizeFileName("...", "bin")
        assertTrue(dotDots.startsWith("media_"))
        assertTrue(dotDots.endsWith(".bin"))
    }
}
