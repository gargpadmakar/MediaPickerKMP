package io.github.padmakargarg.mediapicker.compose.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
        Column(modifier = modifier.fillMaxWidth()) {
            Text(
                text = "Selected Items (${items.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
            )

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(items, key = { it.id }) { item ->
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
}
