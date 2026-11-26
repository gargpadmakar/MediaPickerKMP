package io.github.padmakargarg.mediapicker

import io.github.padmakargarg.mediapicker.domain.model.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MediaPickerResultTest {

    @Test
    fun testSuccessResult_firstOrNullHelper() {
        val item1 = MediaPickerItem(id = "1", uri = "uri://1", mediaType = MediaType.IMAGE)
        val item2 = MediaPickerItem(id = "2", uri = "uri://2", mediaType = MediaType.IMAGE)
        val success = MediaPickerResult.Success(listOf(item1, item2))

        assertEquals(2, success.items.size)
        assertEquals(item1, success.firstOrNull)

        val emptySuccess = MediaPickerResult.Success(emptyList())
        assertNull(emptySuccess.firstOrNull)
    }

    @Test
    fun testResultCallbackExtensions() {
        var successCalled = false
        var cancelledCalled = false
        var errorCalled = false

        val item = MediaPickerItem(id = "1", uri = "uri://1")
        val successResult: MediaPickerResult = MediaPickerResult.Success(listOf(item))

        successResult
            .onSuccess { successCalled = true }
            .onCancelled { cancelledCalled = true }
            .onError { errorCalled = true }

        assertTrue(successCalled)
        assertTrue(!cancelledCalled)
        assertTrue(!errorCalled)

        val cancelledResult: MediaPickerResult = MediaPickerResult.Cancelled
        var cancelledReceived = false
        cancelledResult.onCancelled { cancelledReceived = true }
        assertTrue(cancelledReceived)

        val errorResult: MediaPickerResult = MediaPickerResult.Error(MediaPickerError.PermissionDenied())
        var errorReceived = false
        errorResult.onError { errorReceived = true }
        assertTrue(errorReceived)
    }
}
