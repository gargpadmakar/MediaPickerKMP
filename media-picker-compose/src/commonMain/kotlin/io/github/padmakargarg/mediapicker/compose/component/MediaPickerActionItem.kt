package io.github.padmakargarg.mediapicker.compose.component

import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Model representing an actionable picker item in a dialog or bottom sheet.
 */
data class MediaPickerActionItem(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val icon: ImageVector? = null,
    val onClick: () -> Unit
)
