package software.setixx.chimu.presentation.project.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import software.setixx.chimu.domain.model.ProjectFile

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ImagePlaceholderGallery(
    screenshotFiles: List<ProjectFile>,
    maxCount: Int = 5,
    canUpload: Boolean,
    canDelete: Boolean,
    isReadOnly: Boolean,
    onUpload: () -> Unit,
    onDelete: (fileId: String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Image,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Скриншоты (${screenshotFiles.size}/$maxCount)",
                    style = MaterialTheme.typography.titleSmall
                )
            }
            when {
                isReadOnly -> SuggestionChip(
                    onClick = {},
                    label = { Text("Недоступно", style = MaterialTheme.typography.labelSmall) }
                )
                canUpload && screenshotFiles.size < maxCount -> FilledTonalButton(
                    onClick = onUpload,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Загрузить", style = MaterialTheme.typography.labelMedium)
                }
                !canUpload && !isReadOnly -> FilledTonalButton(
                    onClick = {},
                    enabled = false,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Загрузить", style = MaterialTheme.typography.labelMedium)
                }
            }
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(screenshotFiles) { file ->
                FilledPlaceholderTile(
                    fileName = file.fileName,
                    canDelete = canDelete,
                    onDelete = { onDelete(file.id) }
                )
            }
            val emptySlots = (maxCount - screenshotFiles.size).coerceAtLeast(0)
            items(emptySlots) { EmptyPlaceholderTile() }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun FilledPlaceholderTile(
    fileName: String,
    canDelete: Boolean,
    onDelete: () -> Unit
) {
    Box(modifier = Modifier.size(100.dp)) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 2.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.Image,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    fileName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
            }
        }
        if (canDelete) {
            FilledIconButton(
                onClick = onDelete,
                modifier = Modifier
                    .size(22.dp)
                    .align(Alignment.TopEnd),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(12.dp))
            }
        }
    }
}

@Composable
private fun EmptyPlaceholderTile() {
    Box(
        modifier = Modifier
            .size(100.dp)
            .clip(MaterialTheme.shapes.medium)
            .border(
                width = 1.dp,
                brush = SolidColor(MaterialTheme.colorScheme.outlineVariant),
                shape = MaterialTheme.shapes.medium
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Default.Image,
            contentDescription = null,
            modifier = Modifier.size(28.dp),
            tint = MaterialTheme.colorScheme.outlineVariant
        )
    }
}