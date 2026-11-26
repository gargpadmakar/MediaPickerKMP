package io.github.padmakargarg.mediapicker.compose.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Material 3 Bottom Sheet displaying media picker options with scroll support.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaPickerBottomSheet(
    onDismissRequest: () -> Unit,
    actions: List<MediaPickerActionItem>,
    modifier: Modifier = Modifier,
    title: String = "Select Media",
    sheetState: SheetState = rememberModalBottomSheetState()
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                actions.forEach { action ->
                    ListItem(
                        headlineContent = {
                            Text(
                                text = action.title,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                        },
                        supportingContent = action.subtitle?.let {
                            { Text(text = it, style = MaterialTheme.typography.bodySmall) }
                        },
                        leadingContent = action.icon?.let { icon ->
                            { Icon(imageVector = icon, contentDescription = action.title) }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onDismissRequest()
                                action.onClick()
                            }
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
