package io.github.padmakargarg.mediapicker

import io.github.padmakargarg.mediapicker.domain.model.MediaPickerConfig
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MediaPickerConfigTest {

    private val wildcard = "/" + "*"

    @Test
    fun testDefaultConfig_hasSensibleDefaults() {
        val config = MediaPickerConfig.DEFAULT
        assertFalse(config.allowMultiple)
        assertEquals(1, config.maxSelection)
        assertTrue(config.allowedMimeTypes.isEmpty())
        assertEquals(null, config.maxFileSizeBytes)
        assertFalse(config.copyToCache)
        assertTrue(config.extractMetadata)
    }

    @Test
    fun testSingleFactory() {
        val config = MediaPickerConfig.single(
            allowedMimeTypes = listOf("image/png"),
            maxFileSizeBytes = 1024 * 1024,
            copyToCache = true
        )
        assertFalse(config.allowMultiple)
        assertEquals(1, config.maxSelection)
        assertEquals(listOf("image/png"), config.allowedMimeTypes)
        assertEquals(1024 * 1024, config.maxFileSizeBytes)
        assertTrue(config.copyToCache)
    }

    @Test
    fun testMultipleFactory() {
        val config = MediaPickerConfig.multiple(
            maxSelection = 5,
            allowedMimeTypes = listOf("image$wildcard", "video$wildcard"),
            maxFileSizeBytes = 5 * 1024 * 1024
        )
        assertTrue(config.allowMultiple)
        assertEquals(5, config.maxSelection)
        assertEquals(listOf("image$wildcard", "video$wildcard"), config.allowedMimeTypes)
        assertEquals(5 * 1024 * 1024, config.maxFileSizeBytes)
    }

    @Test
    fun testConfigValidation_rejectsInvalidParameters() {
        assertFailsWith<IllegalArgumentException> {
            MediaPickerConfig(maxSelection = 0)
        }
        assertFailsWith<IllegalArgumentException> {
            MediaPickerConfig(maxFileSizeBytes = 0)
        }
        assertFailsWith<IllegalArgumentException> {
            MediaPickerConfig(maxFileSizeBytes = -100)
        }
    }

    @Test
    fun testIsMimeTypeAllowed_wildcardsAndExactMatches() {
        val config = MediaPickerConfig(
            allowedMimeTypes = listOf("image$wildcard", "application/pdf")
        )

        assertTrue(config.isMimeTypeAllowed("image/jpeg"))
        assertTrue(config.isMimeTypeAllowed("image/png"))
        assertTrue(config.isMimeTypeAllowed("application/pdf"))
        assertFalse(config.isMimeTypeAllowed("video/mp4"))
        assertFalse(config.isMimeTypeAllowed("application/zip"))
        assertFalse(config.isMimeTypeAllowed(null))

        val openConfig = MediaPickerConfig(allowedMimeTypes = emptyList())
        assertTrue(openConfig.isMimeTypeAllowed("any/thing"))
        assertTrue(openConfig.isMimeTypeAllowed(null))

        val universalWildcard = MediaPickerConfig(allowedMimeTypes = listOf("*$wildcard"))
        assertTrue(universalWildcard.isMimeTypeAllowed("image/jpeg"))
        assertTrue(universalWildcard.isMimeTypeAllowed("video/mp4"))
    }
}
