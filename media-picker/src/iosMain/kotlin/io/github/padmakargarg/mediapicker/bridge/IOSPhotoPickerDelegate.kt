package io.github.padmakargarg.mediapicker.bridge

import io.github.padmakargarg.mediapicker.domain.model.MediaPickerConfig
import io.github.padmakargarg.mediapicker.domain.model.MediaPickerItem
import io.github.padmakargarg.mediapicker.domain.model.MediaPickerResult
import io.github.padmakargarg.mediapicker.domain.model.MediaType
import io.github.padmakargarg.mediapicker.util.IOSMetadataExtractor
import kotlinx.coroutines.CancellableContinuation
import platform.Foundation.NSError
import platform.Foundation.NSLock
import platform.Foundation.NSURL
import platform.PhotosUI.PHPickerResult
import platform.PhotosUI.PHPickerViewController
import platform.PhotosUI.PHPickerViewControllerDelegateProtocol
import platform.UniformTypeIdentifiers.UTTypeImage
import platform.UniformTypeIdentifiers.UTTypeItem
import platform.UniformTypeIdentifiers.UTTypeMovie
import platform.darwin.NSObject
import kotlin.coroutines.resume

/**
 * Delegate handling PHPickerViewController callbacks and asynchronously converting PHPickerResults
 * into unified [MediaPickerItem] models.
 */
class IOSPhotoPickerDelegate(
    private val continuation: CancellableContinuation<MediaPickerResult>,
    private val config: MediaPickerConfig,
    private val defaultMediaType: MediaType = MediaType.UNKNOWN
) : NSObject(), PHPickerViewControllerDelegateProtocol {

    private val lock = NSLock()
    private val collectedItems = mutableListOf<MediaPickerItem>()

    override fun picker(picker: PHPickerViewController, didFinishPicking: List<*>) {
        picker.dismissViewControllerAnimated(flag = true, completion = null)

        val results = didFinishPicking.filterIsInstance<PHPickerResult>()
        if (results.isEmpty()) {
            if (continuation.isActive) {
                continuation.resume(MediaPickerResult.Cancelled)
            }
            return
        }

        var remainingCount = results.size

        for (result in results) {
            val itemProvider = result.itemProvider
            val typeIdentifier = resolveTypeIdentifier(itemProvider)

            itemProvider.loadFileRepresentationForTypeIdentifier(typeIdentifier) { tempUrl: NSURL?, _: NSError? ->
                lock.lock()
                try {
                    if (tempUrl != null) {
                        try {
                            val item = IOSMetadataExtractor.extractItem(
                                url = tempUrl,
                                config = config,
                                defaultMediaType = defaultMediaType,
                                forceCacheCopy = true
                            )
                            collectedItems.add(item)
                        } catch (_: Exception) {}
                    }
                    remainingCount--

                    if (remainingCount == 0 && continuation.isActive) {
                        if (collectedItems.isNotEmpty()) {
                            continuation.resume(MediaPickerResult.Success(collectedItems.toList()))
                        } else {
                            continuation.resume(MediaPickerResult.Cancelled)
                        }
                    }
                } finally {
                    lock.unlock()
                }
            }
        }
    }

    private fun resolveTypeIdentifier(itemProvider: platform.Foundation.NSItemProvider): String {
        return when (defaultMediaType) {
            MediaType.IMAGE -> UTTypeImage.identifier
            MediaType.VIDEO -> UTTypeMovie.identifier
            else -> {
                when {
                    itemProvider.hasItemConformingToTypeIdentifier(UTTypeImage.identifier) -> UTTypeImage.identifier
                    itemProvider.hasItemConformingToTypeIdentifier(UTTypeMovie.identifier) -> UTTypeMovie.identifier
                    else -> (itemProvider.registeredTypeIdentifiers.firstOrNull() as? String) ?: UTTypeItem.identifier
                }
            }
        }
    }
}
