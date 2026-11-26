package io.github.padmakargarg.mediapicker.bridge

import platform.UIKit.*
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

/**
 * Helper to safely present and dismiss [UIViewController] instances on iOS main queue.
 */
class IOSViewControllerPresenter(
    private var explicitViewController: UIViewController? = null
) {

    fun bind(viewController: UIViewController) {
        explicitViewController = viewController
    }

    fun unbind() {
        explicitViewController = null
    }

    /**
     * Resolves the top-most visible [UIViewController] currently presented on screen.
     */
    fun getTopViewController(): UIViewController? {
        val root = explicitViewController ?: findActiveRootViewController() ?: return null
        return findTop(root)
    }

    private fun findActiveRootViewController(): UIViewController? {
        val application = UIApplication.sharedApplication

        // 1. Check keyWindow
        application.keyWindow?.rootViewController?.let { return it }

        // 2. Check windows list
        val window = application.windows.filterIsInstance<UIWindow>().firstOrNull { it.isKeyWindow() }
            ?: application.windows.filterIsInstance<UIWindow>().firstOrNull()
        window?.rootViewController?.let { return it }

        // 3. Check connected scenes for iOS 13+
        val activeScene = application.connectedScenes
            .filterIsInstance<UIWindowScene>()
            .firstOrNull { it.activationState == UISceneActivationStateForegroundActive }
            ?: application.connectedScenes.filterIsInstance<UIWindowScene>().firstOrNull()

        val sceneWindow = activeScene?.windows?.filterIsInstance<UIWindow>()?.firstOrNull { it.isKeyWindow() }
            ?: activeScene?.windows?.filterIsInstance<UIWindow>()?.firstOrNull()

        return sceneWindow?.rootViewController
    }

    private fun findTop(controller: UIViewController): UIViewController {
        var top = controller
        while (top.presentedViewController != null && !top.presentedViewController!!.isBeingDismissed()) {
            top = top.presentedViewController!!
        }
        return top
    }

    fun present(
        controller: UIViewController,
        animated: Boolean = true,
        completion: (() -> Unit)? = null
    ) {
        dispatch_async(dispatch_get_main_queue()) {
            val top = getTopViewController()
            top?.presentViewController(controller, animated = animated, completion = completion)
        }
    }

    fun dismiss(
        controller: UIViewController,
        animated: Boolean = true,
        completion: (() -> Unit)? = null
    ) {
        dispatch_async(dispatch_get_main_queue()) {
            controller.dismissViewControllerAnimated(flag = animated, completion = completion)
        }
    }
}
