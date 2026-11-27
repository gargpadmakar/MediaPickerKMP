package io.github.padmakargarg.mediapicker.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.padmakargarg.mediapicker.compose.MediaPickerUiState
import io.github.padmakargarg.mediapicker.compose.component.MediaPickerActionItem
import io.github.padmakargarg.mediapicker.compose.component.MediaPickerBottomSheet
import io.github.padmakargarg.mediapicker.compose.component.MediaPickerDialog
import io.github.padmakargarg.mediapicker.compose.component.SelectedMediaList
import io.github.padmakargarg.mediapicker.compose.rememberMediaPickerState
import io.github.padmakargarg.mediapicker.domain.model.MediaPickerConfig

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = MaterialTheme.colorScheme.primary,
                    secondary = MaterialTheme.colorScheme.secondary,
                    surface = MaterialTheme.colorScheme.surface
                )
            ) {
                val state = rememberMediaPickerState()
                var showBottomSheet by remember { mutableStateOf(false) }
                var showDialog by remember { mutableStateOf(false) }

                val pickerActions = remember(state) {
                    listOf(
                        MediaPickerActionItem(
                            id = "camera",
                            title = "Capture Photo",
                            subtitle = "Take a high-res photo using camera"
                        ) { state.capturePhoto(MediaPickerConfig.single()) },
                        MediaPickerActionItem(
                            id = "camera_video",
                            title = "Record Video",
                            subtitle = "Record a video using camera"
                        ) { state.captureVideo(MediaPickerConfig.single()) },
                        MediaPickerActionItem(
                            id = "compressed_image",
                            title = "Compressed Image (70%)",
                            subtitle = "Compresses to 70% quality, max 1080px"
                        ) {
                            state.pickImage(
                                MediaPickerConfig.single(
                                    copyToCache = true,
                                    compressionQuality = 70,
                                    maxImageWidth = 1080,
                                    maxImageHeight = 1080
                                )
                            )
                        },
                        MediaPickerActionItem(
                            id = "single_image",
                            title = "Pick Single Image",
                            subtitle = "Select 1 image from gallery"
                        ) { state.pickImage(MediaPickerConfig.single(copyToCache = true)) },
                        MediaPickerActionItem(
                            id = "multiple_images",
                            title = "Pick Multiple Images",
                            subtitle = "Select up to 5 images"
                        ) { state.pickMultipleImages(MediaPickerConfig.multiple(maxSelection = 5, copyToCache = true)) },
                        MediaPickerActionItem(
                            id = "single_video",
                            title = "Pick Single Video",
                            subtitle = "Select 1 video from gallery"
                        ) { state.pickVideo(MediaPickerConfig.single()) },
                        MediaPickerActionItem(
                            id = "multiple_videos",
                            title = "Pick Multiple Videos",
                            subtitle = "Select up to 3 videos"
                        ) { state.pickMultipleVideos(MediaPickerConfig.multiple(maxSelection = 3)) },
                        MediaPickerActionItem(
                            id = "single_audio",
                            title = "Pick Single Audio",
                            subtitle = "MP3, WAV, AAC, etc."
                        ) { state.pickAudio(MediaPickerConfig.single(copyToCache = true)) },
                        MediaPickerActionItem(
                            id = "multiple_audio",
                            title = "Pick Multiple Audio",
                            subtitle = "Select up to 5 audio tracks"
                        ) { state.pickMultipleAudio(MediaPickerConfig.multiple(maxSelection = 5, copyToCache = true)) },
                        MediaPickerActionItem(
                            id = "single_file",
                            title = "Pick Document",
                            subtitle = "PDF, DOCX, TXT, etc."
                        ) { state.pickFile(MediaPickerConfig.single(copyToCache = true)) },
                        MediaPickerActionItem(
                            id = "multiple_files",
                            title = "Pick Documents",
                            subtitle = "Select up to 5 documents"
                        ) { state.pickMultipleFiles(MediaPickerConfig.multiple(maxSelection = 5, copyToCache = true)) },
                        MediaPickerActionItem(
                            id = "combined_media",
                            title = "Combined Media",
                            subtitle = "Images and Videos together"
                        ) { state.pickMedia(MediaPickerConfig.multiple(maxSelection = 5)) }
                    )
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        Surface(
                            shadowElevation = 3.dp,
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .statusBarsPadding()
                                    .padding(horizontal = 20.dp, vertical = 14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "MediaPickerKMP",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = "Kotlin Multiplatform Media Library",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                                    )
                                }
                                SuggestionChip(
                                    onClick = {},
                                    label = {
                                        Text(
                                            text = state.mediaPicker.platformName,
                                            fontWeight = FontWeight.Bold
                                        )
                                    },
                                    colors = SuggestionChipDefaults.suggestionChipColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        labelColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Progress Indicator
                        if (state.isLoading) {
                            item {
                                LinearProgressIndicator(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        // State & Status Banner
                        item {
                            StatusCard(uiState = state.uiState, onClear = { state.clear() })
                        }

                        // Compose Prebuilt Modals
                        item {
                            Text(
                                text = "Prebuilt UI Components",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Button(
                                    onClick = { showBottomSheet = true },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(vertical = 12.dp)
                                ) {
                                    Text("Bottom Sheet", maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                                OutlinedButton(
                                    onClick = { showDialog = true },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(vertical = 12.dp)
                                ) {
                                    Text("Dialog Picker", maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }
                        }

                        // Direct Launch Actions (2-column layout to prevent text wrapping)
                        item {
                            Text(
                                text = "Direct Picker Actions",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Compression row
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                FilledTonalButton(
                                    onClick = {
                                        state.capturePhoto(
                                            MediaPickerConfig.single(
                                                compressionQuality = 70,
                                                maxImageWidth = 1280
                                            )
                                        )
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Camera (Comp 70%)", maxLines = 1)
                                }
                                FilledTonalButton(
                                    onClick = {
                                        state.pickImage(
                                            MediaPickerConfig.single(
                                                copyToCache = true,
                                                compressionQuality = 70,
                                                maxImageWidth = 1080
                                            )
                                        )
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Image (Comp 70%)", maxLines = 1)
                                }
                            }
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                FilledTonalButton(
                                    onClick = { state.capturePhoto() },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Photo Camera", maxLines = 1)
                                }
                                FilledTonalButton(
                                    onClick = { state.captureVideo() },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Record Video", maxLines = 1)
                                }
                            }
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                FilledTonalButton(
                                    onClick = { state.pickImage() },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Image (1)", maxLines = 1)
                                }
                                FilledTonalButton(
                                    onClick = { state.pickVideo() },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Video (1)", maxLines = 1)
                                }
                            }
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                FilledTonalButton(
                                    onClick = { state.pickMultipleImages(MediaPickerConfig.multiple(5)) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Images (Max 5)", maxLines = 1)
                                }
                                FilledTonalButton(
                                    onClick = { state.pickVideo() },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Video (1)", maxLines = 1)
                                }
                            }
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                FilledTonalButton(
                                    onClick = { state.pickAudio() },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Audio (1)", maxLines = 1)
                                }
                                FilledTonalButton(
                                    onClick = { state.pickMultipleAudio(MediaPickerConfig.multiple(5)) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Audio (Max 5)", maxLines = 1)
                                }
                            }
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                FilledTonalButton(
                                    onClick = { state.pickFile() },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Document (1)", maxLines = 1)
                                }
                                FilledTonalButton(
                                    onClick = { state.pickMultipleFiles(MediaPickerConfig.multiple(5)) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Documents (5)", maxLines = 1)
                                }
                            }
                        }

                        // Selected Media Items Section
                        item {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        }

                        item {
                            SelectedMediaList(
                                items = state.items,
                                onRemoveItem = { itemToRemove ->
                                    val updated = state.items.filter { it.id != itemToRemove.id }
                                    state.updateItems(updated)
                                }
                            )
                        }
                    }

                    // Bottom Sheet
                    if (showBottomSheet) {
                        MediaPickerBottomSheet(
                            onDismissRequest = { showBottomSheet = false },
                            actions = pickerActions
                        )
                    }

                    // Dialog
                    if (showDialog) {
                        MediaPickerDialog(
                            onDismissRequest = { showDialog = false },
                            actions = pickerActions
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatusCard(
    uiState: MediaPickerUiState,
    onClear: () -> Unit
) {
    val containerColor = when (uiState) {
        is MediaPickerUiState.Idle -> MaterialTheme.colorScheme.surfaceVariant
        is MediaPickerUiState.Loading -> MaterialTheme.colorScheme.primaryContainer
        is MediaPickerUiState.Success -> MaterialTheme.colorScheme.secondaryContainer
        is MediaPickerUiState.Cancelled -> MaterialTheme.colorScheme.tertiaryContainer
        is MediaPickerUiState.Error -> MaterialTheme.colorScheme.errorContainer
    }

    val contentColor = when (uiState) {
        is MediaPickerUiState.Idle -> MaterialTheme.colorScheme.onSurfaceVariant
        is MediaPickerUiState.Loading -> MaterialTheme.colorScheme.onPrimaryContainer
        is MediaPickerUiState.Success -> MaterialTheme.colorScheme.onSecondaryContainer
        is MediaPickerUiState.Cancelled -> MaterialTheme.colorScheme.onTertiaryContainer
        is MediaPickerUiState.Error -> MaterialTheme.colorScheme.onErrorContainer
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "State: ${uiState::class.simpleName ?: "Idle"}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                val description = when (uiState) {
                    is MediaPickerUiState.Idle -> "Ready. Select a media source below."
                    is MediaPickerUiState.Loading -> "Media picker is active..."
                    is MediaPickerUiState.Success -> "Successfully selected ${uiState.items.size} item(s)."
                    is MediaPickerUiState.Cancelled -> "Picker operation was cancelled."
                    is MediaPickerUiState.Error -> "Error: ${uiState.error.message}"
                }
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColor.copy(alpha = 0.9f)
                )
            }

            if (uiState !is MediaPickerUiState.Idle && uiState !is MediaPickerUiState.Loading) {
                OutlinedButton(
                    onClick = onClear,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, contentColor.copy(alpha = 0.5f))
                ) {
                    Text("Clear", color = contentColor)
                }
            }
        }
    }
}
