package io.github.padmakargarg.mediapicker.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import io.github.padmakargarg.mediapicker.AndroidMediaPicker
import io.github.padmakargarg.mediapicker.domain.model.*
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val picker by lazy { AndroidMediaPicker(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        Surface(
                            shadowElevation = 4.dp,
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .statusBarsPadding()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "MediaPickerKMP Sample",
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    SampleContent(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        onCapturePhoto = { onResult ->
                            lifecycleScope.launch {
                                val result = picker.capturePhoto(MediaPickerConfig.single())
                                onResult(result)
                            }
                        },
                        onPickImage = { onResult ->
                            lifecycleScope.launch {
                                val result = picker.pickImage(MediaPickerConfig.single(copyToCache = true))
                                onResult(result)
                            }
                        },
                        onPickMultipleImages = { onResult ->
                            lifecycleScope.launch {
                                val result = picker.pickMultipleImages(
                                    MediaPickerConfig.multiple(maxSelection = 5, copyToCache = true)
                                )
                                onResult(result)
                            }
                        },
                        onPickVideo = { onResult ->
                            lifecycleScope.launch {
                                val result = picker.pickVideo(MediaPickerConfig.single())
                                onResult(result)
                            }
                        },
                        onPickMultipleVideos = { onResult ->
                            lifecycleScope.launch {
                                val result = picker.pickMultipleVideos(MediaPickerConfig.multiple(maxSelection = 3))
                                onResult(result)
                            }
                        },
                        onPickFile = { onResult ->
                            lifecycleScope.launch {
                                val result = picker.pickFile(MediaPickerConfig.single(copyToCache = true))
                                onResult(result)
                            }
                        },
                        onPickMultipleFiles = { onResult ->
                            lifecycleScope.launch {
                                val result = picker.pickMultipleFiles(
                                    MediaPickerConfig.multiple(maxSelection = 5, copyToCache = true)
                                )
                                onResult(result)
                            }
                        },
                        onPickAudio = { onResult ->
                            lifecycleScope.launch {
                                val result = picker.pickAudio(MediaPickerConfig.single(copyToCache = true))
                                onResult(result)
                            }
                        },
                        onPickMultipleAudio = { onResult ->
                            lifecycleScope.launch {
                                val result = picker.pickMultipleAudio(
                                    MediaPickerConfig.multiple(maxSelection = 5, copyToCache = true)
                                )
                                onResult(result)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SampleContent(
    modifier: Modifier = Modifier,
    onCapturePhoto: ((MediaPickerResult) -> Unit) -> Unit,
    onPickImage: ((MediaPickerResult) -> Unit) -> Unit,
    onPickMultipleImages: ((MediaPickerResult) -> Unit) -> Unit,
    onPickVideo: ((MediaPickerResult) -> Unit) -> Unit,
    onPickMultipleVideos: ((MediaPickerResult) -> Unit) -> Unit,
    onPickFile: ((MediaPickerResult) -> Unit) -> Unit,
    onPickMultipleFiles: ((MediaPickerResult) -> Unit) -> Unit,
    onPickAudio: ((MediaPickerResult) -> Unit) -> Unit,
    onPickMultipleAudio: ((MediaPickerResult) -> Unit) -> Unit
) {
    var statusMessage by remember { mutableStateOf("Ready. Tap an action below.") }
    var statusColor by remember { mutableStateOf(Color.Gray) }
    var selectedItems by remember { mutableStateOf<List<MediaPickerItem>>(emptyList()) }

    fun handleResult(result: MediaPickerResult) {
        when (result) {
            is MediaPickerResult.Success -> {
                selectedItems = result.items
                statusMessage = "Success: Selected ${result.items.size} item(s)"
                statusColor = Color(0xFF2E7D32)
            }
            is MediaPickerResult.Cancelled -> {
                statusMessage = "Operation cancelled by user"
                statusColor = Color(0xFFE65100)
            }
            is MediaPickerResult.Error -> {
                statusMessage = "Error: ${result.error.message}"
                statusColor = Color(0xFFC62828)
            }
        }
    }

    LazyColumn(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Status",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = statusMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = statusColor
                    )
                }
            }
        }

        item {
            Text(
                text = "Camera Capture (Native)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Button(
                onClick = { onCapturePhoto { handleResult(it) } },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Capture Photo")
            }
        }

        item {
            Text(
                text = "Gallery Actions (Photo Picker)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onPickImage { handleResult(it) } },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Pick Image")
                }
                Button(
                    onClick = { onPickMultipleImages { handleResult(it) } },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Pick Images")
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onPickVideo { handleResult(it) } },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Pick Video")
                }
                Button(
                    onClick = { onPickMultipleVideos { handleResult(it) } },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Pick Videos")
                }
            }
        }

        item {
            Text(
                text = "Audio Actions (Storage Access Framework)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onPickAudio { handleResult(it) } },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Pick Audio")
                }
                Button(
                    onClick = { onPickMultipleAudio { handleResult(it) } },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Pick Audios")
                }
            }
        }

        item {
            Text(
                text = "Document Actions (Storage Access Framework)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { onPickFile { handleResult(it) } },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Pick File")
                }
                OutlinedButton(
                    onClick = { onPickMultipleFiles { handleResult(it) } },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Pick Files")
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Selected Items (${selectedItems.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        if (selectedItems.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No items selected yet.",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            items(selectedItems, key = { it.id }) { item ->
                ItemCard(item = item)
            }
        }
    }
}

@Composable
fun ItemCard(item: MediaPickerItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = item.name ?: "Unknown filename",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = item.mediaType.name,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            MetadataRow(label = "MIME Type", value = item.mimeType ?: "N/A")
            MetadataRow(label = "Extension", value = item.extension ?: "N/A")
            MetadataRow(label = "Size", value = formatFileSize(item.sizeInBytes))

            val w = item.width
            val h = item.height
            if (w != null && h != null) {
                MetadataRow(label = "Dimensions", value = "$w x $h px")
            }

            val duration = item.durationMs
            if (duration != null) {
                MetadataRow(label = "Duration", value = "${duration / 1000}s ($duration ms)")
            }

            val path = item.localPath
            if (path != null) {
                MetadataRow(label = "Cached Path", value = path)
            }

            MetadataRow(label = "URI", value = item.uri)
        }
    }
}

@Composable
fun MetadataRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp)
    ) {
        Text(
            text = "$label: ",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2
        )
    }
}

private fun formatFileSize(bytes: Long?): String {
    if (bytes == null || bytes <= 0) return "Unknown"
    return when {
        bytes >= 1024 * 1024 -> "%.2f MB".format(bytes / (1024.0 * 1024.0))
        bytes >= 1024 -> "%.1f KB".format(bytes / 1024.0)
        else -> "$bytes bytes"
    }
}
