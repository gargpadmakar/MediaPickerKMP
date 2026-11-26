package io.github.padmakargarg.mediapicker.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
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
            MaterialTheme {
                val state = rememberMediaPickerState()
                var showBottomSheet by remember { mutableStateOf(false) }
                var showDialog by remember { mutableStateOf(false) }

                val pickerActions = remember(state) {
                    listOf(
                        MediaPickerActionItem(
                            id = "camera",
                            title = "Capture Photo",
                            subtitle = "Take a new photo with camera"
                        ) { state.capturePhoto(MediaPickerConfig.single()) },
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
                            subtitle = "Select up to 5 audio files"
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
                            shadowElevation = 4.dp,
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .statusBarsPadding()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "MediaPickerKMP Sample",
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                SuggestionChip(
                                    onClick = {},
                                    label = { Text(state.mediaPicker.platformName) },
                                    colors = SuggestionChipDefaults.suggestionChipColors(
                                        containerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f),
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
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Progress Indicator
                        if (state.isLoading) {
                            item {
                                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                            }
                        }

                        // Status Banner
                        item {
                            StatusCard(uiState = state.uiState, onClear = { state.clear() })
                        }

                        // Launcher Modals
                        item {
                            Text(
                                text = "Compose Multiplatform Components",
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
                                    onClick = { showBottomSheet = true },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Open Bottom Sheet")
                                }
                                OutlinedButton(
                                    onClick = { showDialog = true },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Open Dialog")
                                }
                            }
                        }

                        // Quick Action Buttons
                        item {
                            Text(
                                text = "Quick Triggers",
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
                                    onClick = { state.capturePhoto() },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Camera")
                                }
                                Button(
                                    onClick = { state.pickImage() },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Image")
                                }
                                Button(
                                    onClick = { state.pickMultipleImages(MediaPickerConfig.multiple(5)) },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Images (5)")
                                }
                            }
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { state.pickVideo() },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Video")
                                }
                                Button(
                                    onClick = { state.pickAudio() },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Audio")
                                }
                                Button(
                                    onClick = { state.pickFile() },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Document")
                                }
                            }
                        }

                        // Selected Media Items
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (uiState) {
                is MediaPickerUiState.Idle -> MaterialTheme.colorScheme.surfaceVariant
                is MediaPickerUiState.Loading -> MaterialTheme.colorScheme.primaryContainer
                is MediaPickerUiState.Success -> MaterialTheme.colorScheme.secondaryContainer
                is MediaPickerUiState.Cancelled -> MaterialTheme.colorScheme.tertiaryContainer
                is MediaPickerUiState.Error -> MaterialTheme.colorScheme.errorContainer
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "State: ${uiState::class.simpleName ?: "Idle"}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                val description = when (uiState) {
                    is MediaPickerUiState.Idle -> "Ready. Select a media source below."
                    is MediaPickerUiState.Loading -> "Media picker active..."
                    is MediaPickerUiState.Success -> "Successfully selected ${uiState.items.size} item(s)."
                    is MediaPickerUiState.Cancelled -> "Picker operation cancelled by user."
                    is MediaPickerUiState.Error -> "Error: ${uiState.error.message}"
                }
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (uiState !is MediaPickerUiState.Idle && uiState !is MediaPickerUiState.Loading) {
                TextButton(onClick = onClear) {
                    Text("Clear")
                }
            }
        }
    }
}
