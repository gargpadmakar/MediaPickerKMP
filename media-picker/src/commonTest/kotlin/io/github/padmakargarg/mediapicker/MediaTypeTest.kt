package io.github.padmakargarg.mediapicker

import io.github.padmakargarg.mediapicker.domain.model.MediaType
import kotlin.test.Test
import kotlin.test.assertEquals

class MediaTypeTest {

    @Test
    fun testFromMimeType_resolvesImages() {
        assertEquals(MediaType.IMAGE, MediaType.fromMimeType("image/jpeg"))
        assertEquals(MediaType.IMAGE, MediaType.fromMimeType("image/png"))
        assertEquals(MediaType.IMAGE, MediaType.fromMimeType("IMAGE/WEBP"))
        assertEquals(MediaType.IMAGE, MediaType.fromMimeType("image/gif"))
    }

    @Test
    fun testFromMimeType_resolvesVideos() {
        assertEquals(MediaType.VIDEO, MediaType.fromMimeType("video/mp4"))
        assertEquals(MediaType.VIDEO, MediaType.fromMimeType("video/quicktime"))
        assertEquals(MediaType.VIDEO, MediaType.fromMimeType("video/x-matroska"))
    }

    @Test
    fun testFromMimeType_resolvesAudio() {
        assertEquals(MediaType.AUDIO, MediaType.fromMimeType("audio/mpeg"))
        assertEquals(MediaType.AUDIO, MediaType.fromMimeType("audio/mp4"))
        assertEquals(MediaType.AUDIO, MediaType.fromMimeType("audio/wav"))
        assertEquals(MediaType.AUDIO, MediaType.fromMimeType("audio/ogg"))
    }

    @Test
    fun testFromMimeType_resolvesDocuments() {
        assertEquals(MediaType.DOCUMENT, MediaType.fromMimeType("application/pdf"))
        assertEquals(MediaType.DOCUMENT, MediaType.fromMimeType("application/msword"))
        assertEquals(MediaType.DOCUMENT, MediaType.fromMimeType("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
        assertEquals(MediaType.DOCUMENT, MediaType.fromMimeType("text/plain"))
        assertEquals(MediaType.DOCUMENT, MediaType.fromMimeType("text/csv"))
    }

    @Test
    fun testFromMimeType_resolvesUnknownAndNull() {
        assertEquals(MediaType.UNKNOWN, MediaType.fromMimeType(null))
        assertEquals(MediaType.UNKNOWN, MediaType.fromMimeType(""))
        assertEquals(MediaType.UNKNOWN, MediaType.fromMimeType("application/octet-stream"))
        assertEquals(MediaType.UNKNOWN, MediaType.fromMimeType("binary/custom"))
    }
}
