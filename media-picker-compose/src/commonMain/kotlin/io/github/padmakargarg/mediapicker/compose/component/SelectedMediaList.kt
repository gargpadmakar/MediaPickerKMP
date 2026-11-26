package io.github.padmakargarg.mediapicker.compose.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.padmakargarg.mediapicker.domain.model.MediaPickerItem

/**
 * List composable displaying multiple [MediaPickerItem] elements.
 * Uses a [Column] layout so it can be safely embedded inside scrollable
 * containers (e.g. [LazyColumn] or [verticalScroll]) without infinite constraint issues.
 */
@Composable
fun SelectedMediaList(
    items: List<MediaPickerItem>,
    modifier: Modifier = Modifier,
    onRemoveItem: ((MediaPickerItem) -> Unit)? = null
) {
    if (items.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No media selected yet.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Selected Items (${items.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
            )

            items.forEach { item ->
                MediaPreview(
                    item = item,
                    onRemove = if (onRemoveItem != null) {
                        { onRemoveItem(item) }
                    } else null
                )
            }
        }
    }
}
