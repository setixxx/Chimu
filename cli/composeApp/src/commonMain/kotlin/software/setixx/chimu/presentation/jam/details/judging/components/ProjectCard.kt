package software.setixx.chimu.presentation.jam.details.judging.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import software.setixx.chimu.domain.model.Project

@Composable
fun ProjectCard(
    project: Project,
    hasUnratedCriteria: Boolean,
    onClick: () -> Unit,
    onViewDetails: (() -> Unit)? = null
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (hasUnratedCriteria)
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = MaterialTheme.shapes.extraLarge,
        modifier = Modifier
            .fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    project.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                project.teamName?.let {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Команда: $it",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (onViewDetails != null) {
                IconButton(onClick = onViewDetails) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = "Подробнее о проекте",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}