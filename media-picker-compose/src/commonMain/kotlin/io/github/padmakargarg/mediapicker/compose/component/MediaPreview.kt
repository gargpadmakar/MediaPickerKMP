package io.github.padmakargarg.mediapicker.compose.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.padmakargarg.mediapicker.domain.model.MediaPickerItem
import io.github.padmakargarg.mediapicker.domain.model.MediaType

/**
 * Card preview composable displaying detailed metadata and prominent file name for a [MediaPickerItem].
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
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row: Media Type Icon & Badge + Remove button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val typeIcon = when (item.mediaType) {
                        MediaType.IMAGE -> "🖼️"
                        MediaType.VIDEO -> "🎥"
                        MediaType.AUDIO -> "🎵"
                        MediaType.DOCUMENT -> "📄"
                        MediaType.UNKNOWN -> "📁"
                    }
                    Text(text = typeIcon, fontSize = 20.sp)

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
                                MediaType.DOCUMENT -> MaterialTheme.colorScheme.surfaceVariant
                                MediaType.UNKNOWN -> MaterialTheme.colorScheme.surface
                            }
                        )
                    )
                }

                if (onRemove != null) {
                    OutlinedButton(
                        onClick = onRemove,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Remove", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Prominent File Name Container
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Text(
                    text = "FILE NAME",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = item.name ?: "Unknown Media File",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Metadata Row: Size, MIME type, Duration / Dimensions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                item.sizeInBytes?.let { bytes ->
                    AssistChip(
                        onClick = {},
                        label = { Text("Size: ${formatFileSize(bytes)}", fontWeight = FontWeight.Medium) }
                    )
                }

                item.mimeType?.let { mime ->
                    AssistChip(
                        onClick = {},
                        label = { Text(mime.substringBefore(';'), maxLines = 1, overflow = TextOverflow.Ellipsis) }
                    )
                }
            }

            if (item.width != null && item.height != null || item.durationMs != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (item.width != null && item.height != null) {
                        AssistChip(
                            onClick = {},
                            label = { Text("Resolution: ${item.width} × ${item.height}") }
                        )
                    }
                    if (item.durationMs != null) {
                        AssistChip(
                            onClick = {},
                            label = { Text("Duration: ${formatDuration(item.durationMs!!)}") }
                        )
                    }
                }
            }

            // Storage Path Display
            val displayPath = item.localPath ?: item.uri
            if (displayPath.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (item.localPath != null) "Local Cache Path:" else "Content URI:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = displayPath,
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
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
