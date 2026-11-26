package io.github.padmakargarg.mediapicker.bridge

import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.lang.ref.WeakReference
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Lifecycle-safe bridge for launching ActivityResultContracts asynchronously from coroutines.
 */
class ActivityResultLauncherBridge(
    initialActivity: ComponentActivity? = null
) : DefaultLifecycleObserver {

    private var activityRef: WeakReference<ComponentActivity>? = null

    init {
        initialActivity?.let { bind(it) }
    }

    fun bind(activity: ComponentActivity) {
        activityRef?.get()?.lifecycle?.removeObserver(this)
        activityRef = WeakReference(activity)
        activity.lifecycle.addObserver(this)
    }

    fun unbind() {
        activityRef?.get()?.lifecycle?.removeObserver(this)
        activityRef?.clear()
        activityRef = null
    }

    override fun onDestroy(owner: LifecycleOwner) {
        unbind()
    }

    val activity: ComponentActivity?
        get() = activityRef?.get()?.takeIf { !it.isFinishing && !it.isDestroyed }

    /**
     * Launches an [ActivityResultContract] on the main thread and awaits the result asynchronously.
     */
    suspend fun <I, O> launchContract(
        contract: ActivityResultContract<I, O>,
        input: I
    ): O? = withContext(Dispatchers.Main.immediate) {
        val currentActivity = activity
            ?: throw IllegalStateException("ComponentActivity is not attached or has been destroyed.")

        suspendCancellableCoroutine { continuation ->
            val key = "mediapicker_${UUID.randomUUID()}"
            var launcher: ActivityResultLauncher<I>? = null

            try {
                launcher = currentActivity.activityResultRegistry.register(
                    key,
                    contract
                ) { result ->
                    try {
                        launcher?.unregister()
                    } catch (_: Exception) {}
                    if (continuation.isActive) {
                        continuation.resume(result)
                    }
                }

                continuation.invokeOnCancellation {
                    try {
                        launcher.unregister()
                    } catch (_: Exception) {}
                }

                launcher.launch(input)
            } catch (t: Throwable) {
                try {
                    launcher?.unregister()
                } catch (_: Exception) {}
                if (continuation.isActive) {
                    continuation.resumeWithException(t)
                }
            }
        }
    }
}
