package io.github.padmakargarg.mediapicker.bridge

import io.github.padmakargarg.mediapicker.domain.model.MediaPickerConfig
import io.github.padmakargarg.mediapicker.domain.model.MediaPickerItem
import io.github.padmakargarg.mediapicker.domain.model.MediaPickerResult
import io.github.padmakargarg.mediapicker.domain.model.MediaType
import io.github.padmakargarg.mediapicker.util.IOSMetadataExtractor
import kotlinx.coroutines.CancellableContinuation
import platform.Foundation.NSURL
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerViewController
import platform.darwin.NSObject
import kotlin.coroutines.resume

/**
 * Delegate handling UIDocumentPickerViewController callbacks with security-scoped resource access.
 */
class IOSDocumentPickerDelegate(
    private val continuation: CancellableContinuation<MediaPickerResult>,
    private val config: MediaPickerConfig,
    private val defaultMediaType: MediaType = MediaType.DOCUMENT,
    private val onComplete: () -> Unit = {}
) : NSObject(), UIDocumentPickerDelegateProtocol {

    override fun documentPicker(controller: UIDocumentPickerViewController, didPickDocumentsAtURLs: List<*>) {
        controller.dismissViewControllerAnimated(flag = true, completion = null)
        onComplete()

        val urls = didPickDocumentsAtURLs.filterIsInstance<NSURL>()
        if (urls.isEmpty()) {
            if (continuation.isActive) {
                continuation.resume(MediaPickerResult.Cancelled)
            }
            return
        }

        val items = mutableListOf<MediaPickerItem>()
        for (url in urls) {
            val accessed = url.startAccessingSecurityScopedResource()
            try {
                val item = IOSMetadataExtractor.extractItem(
                    url = url,
                    config = config,
                    defaultMediaType = defaultMediaType,
                    forceCacheCopy = config.copyToCache
                )
                items.add(item)
            } catch (_: Exception) {
            } finally {
                if (accessed) {
                    url.stopAccessingSecurityScopedResource()
                }
            }
        }

        if (continuation.isActive) {
            if (items.isNotEmpty()) {
                continuation.resume(MediaPickerResult.Success(items))
            } else {
                continuation.resume(MediaPickerResult.Cancelled)
            }
        }
    }

    override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
        controller.dismissViewControllerAnimated(flag = true, completion = null)
        onComplete()
        if (continuation.isActive) {
            continuation.resume(MediaPickerResult.Cancelled)
        }
    }
}
