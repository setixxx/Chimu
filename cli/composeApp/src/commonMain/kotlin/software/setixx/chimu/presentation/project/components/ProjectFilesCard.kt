package software.setixx.chimu.presentation.project.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import software.setixx.chimu.domain.model.ProjectFile
import software.setixx.chimu.presentation.jam.details.overview.components.ManagementListCard
import software.setixx.chimu.presentation.project.formatFileSize

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ProjectFilesCard(
    title: String,
    icon: ImageVector,
    files: List<ProjectFile>,
    canUpload: Boolean,
    canDelete: Boolean,
    isReadOnly: Boolean,
    onUpload: () -> Unit,
    onDelete: (fileId: String) -> Unit,
    onDownload: (fileId: String) -> Unit
) {
    ManagementListCard(
        title = title,
        titleIcon = icon,
        items = files,
        emptyText = "Файлы не загружены",
        buttonText = if (canUpload && files.size < 5 && !isReadOnly) "Загрузить" else null,
        buttonIcon = if (canUpload && files.size < 5 && !isReadOnly) Icons.Default.Add else null,
        onButtonClick = onUpload,
        onItemClick = { onDownload(it.id) },
        itemHeadline = {
            Text(
                it.fileName,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        itemSupportingContent = {
            Text(
                formatFileSize(it.fileSize),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        itemTrailingContent = {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = { onDownload(it.id) }) {
                    Icon(
                        Icons.Default.Download,
                        contentDescription = "Скачать",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                if (canDelete) {
                    IconButton(onClick = { onDelete(it.id) }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Удалить",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    )
}