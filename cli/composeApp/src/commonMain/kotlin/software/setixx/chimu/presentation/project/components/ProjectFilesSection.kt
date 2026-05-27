package software.setixx.chimu.presentation.project.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import software.setixx.chimu.domain.model.ProjectFile

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ProjectFilesSection(
    title: String,
    icon: ImageVector,
    files: List<ProjectFile>,
    maxCount: Int = 5,
    canUpload: Boolean,
    canDelete: Boolean,
    isReadOnly: Boolean,
    onUpload: () -> Unit,
    onDelete: (fileId: String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "$title (${files.size}/$maxCount)",
                    style = MaterialTheme.typography.titleSmall
                )
            }
            when {
                isReadOnly -> SuggestionChip(
                    onClick = {},
                    label = { Text("Недоступно", style = MaterialTheme.typography.labelSmall) }
                )
                canUpload && files.size < maxCount -> FilledTonalButton(
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

        if (files.isEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceContainerLow
            ) {
                Text(
                    "Файлы не загружены",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
                files.forEachIndexed { index, file ->
                    SegmentedListItem(
                        colors = ListItemDefaults.colors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                        selected = false,
                        onClick = {},
                        shapes = if (files.size == 1)
                            ListItemDefaults.shapes(shape = MaterialTheme.shapes.medium)
                        else
                            ListItemDefaults.segmentedShapes(index = index, count = files.size),
                        leadingContent = {
                            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        },
                        content = {
                            Text(file.fileName, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        },
                        supportingContent = {
                            Text(
                                formatFileSize(file.fileSize),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        trailingContent = if (canDelete) {
                            {
                                IconButton(onClick = { onDelete(file.id) }) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Удалить",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        } else null
                    )
                }
            }
        }
    }
}

private fun formatFileSize(bytes: Long): String = when {
    bytes < 1024L -> "$bytes Б"
    bytes < 1_048_576L -> "${bytes / 1024} КБ"
    bytes < 1_073_741_824L -> "${bytes / 1_048_576} МБ"
    else -> "${bytes / 1_073_741_824} ГБ"
}