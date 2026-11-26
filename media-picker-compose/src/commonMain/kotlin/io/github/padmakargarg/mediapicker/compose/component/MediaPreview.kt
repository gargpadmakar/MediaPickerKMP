package io.github.padmakargarg.mediapicker.compose.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.padmakargarg.mediapicker.domain.model.MediaPickerItem
import io.github.padmakargarg.mediapicker.domain.model.MediaType

/**
 * Card preview composable displaying metadata for a single [MediaPickerItem].
 */
@Composable
fun MediaPreview(
    item: MediaPickerItem,
    modifier: Modifier = Modifier,
    onRemove: (() -> Unit)? = null
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Media Type Badge
                SuggestionChip(
                    onClick = {},
                    label = {
                        Text(
                            text = item.mediaType.name,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = when (item.mediaType) {
                            MediaType.IMAGE -> MaterialTheme.colorScheme.primaryContainer
                            MediaType.VIDEO -> MaterialTheme.colorScheme.secondaryContainer
                            MediaType.AUDIO -> MaterialTheme.colorScheme.tertiaryContainer
                            MediaType.DOCUMENT -> MaterialTheme.colorScheme.surfaceContainerHigh
                            MediaType.UNKNOWN -> MaterialTheme.colorScheme.surface
                        }
                    )
                )

                if (onRemove != null) {
                    TextButton(onClick = onRemove) {
                        Text("Remove", color = MaterialTheme.colorScheme.error)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // File Name
            Text(
                text = item.name ?: "Unknown Media",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            // MIME & Size Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item.mimeType?.let { mime ->
                    Text(
                        text = mime,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                item.sizeInBytes?.let { bytes ->
                    Text(
                        text = formatFileSize(bytes),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Dimensions and Duration
            if (item.width != null && item.height != null || item.durationMs != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (item.width != null && item.height != null) {
                        AssistChip(
                            onClick = {},
                            label = { Text("${item.width} × ${item.height}") }
                        )
                    }
                    if (item.durationMs != null) {
                        AssistChip(
                            onClick = {},
                            label = { Text(formatDuration(item.durationMs!!)) }
                        )
                    }
                }
            }

            // Path/URI info
            val displayPath = item.localPath ?: item.uri
            if (displayPath.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = displayPath,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

private fun formatFileSize(bytes: Long): String {
    if (bytes < 1024) return "$bytes B"
    val kb = bytes / 1024.0
    if (kb < 1024) return "${(kb * 10).toLong() / 10.0} KB"
    val mb = kb / 1024.0
    if (mb < 1024) return "${(mb * 10).toLong() / 10.0} MB"
    val gb = mb / 1024.0
    return "${(gb * 100).toLong() / 100.0} GB"
}

private fun formatDuration(durationMs: Long): String {
    val totalSeconds = durationMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "$minutes:${seconds.toString().padStart(2, '0')}"
}
