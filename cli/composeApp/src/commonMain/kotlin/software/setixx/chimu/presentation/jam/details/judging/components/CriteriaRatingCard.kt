package software.setixx.chimu.presentation.jam.details.judging.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import software.setixx.chimu.data.remote.dto.RatingCriteriaResponse
import software.setixx.chimu.domain.model.MyRating

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CriteriaRatingCard(
    criterion: RatingCriteriaResponse,
    existingRating: MyRating?,
    isActionLoading: Boolean,
    onRate: (score: Int, comment: String?) -> Unit,
    onUpdate: (score: Int, comment: String?) -> Unit,
    onDelete: () -> Unit
) {
    var isEditing by remember(existingRating) { mutableStateOf(existingRating == null) }
    var sliderValue by remember(existingRating, criterion) {
        mutableFloatStateOf(
            existingRating?.score?.toFloatOrNull() ?: 0f
        )
    }
    var comment by remember(existingRating) {
        mutableStateOf(existingRating?.comment ?: "")
    }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        criterion.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    criterion.description?.let {
                        Text(
                            it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "Макс: ${criterion.maxScore}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Вес: ${criterion.weight}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider()

            if (existingRating != null && !isEditing) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "${existingRating.score} / ${criterion.maxScore}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Row {
                        IconButton(
                            onClick = {
                                sliderValue = existingRating.score.toFloatOrNull() ?: 0f
                                comment = existingRating.comment ?: ""
                                isEditing = true
                            }
                        ) {
                            Icon(Icons.Default.Edit, "Изменить")
                        }
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(
                                Icons.Default.Delete,
                                "Удалить",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
                existingRating.comment?.let { c ->
                    Text(
                        "Комментарий: $c",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Оценка", style = MaterialTheme.typography.labelMedium)
                        Text(
                            "${sliderValue.toInt()} / ${criterion.maxScore}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Slider(
                        value = sliderValue,
                        onValueChange = { sliderValue = it },
                        valueRange = 0f..criterion.maxScore.toFloat(),
                        steps = criterion.maxScore - 1,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = comment,
                        onValueChange = { comment = it },
                        label = { Text("Комментарий (необязательно)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 4,
                        shape = MaterialTheme.shapes.largeIncreased
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (existingRating != null) {
                            OutlinedButton(
                                onClick = { isEditing = false },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(ButtonDefaults.MediumContainerHeight),
                            ) {
                                Text("Отмена")
                            }
                        }
                        Button(
                            onClick = {
                                val score = sliderValue.toInt()
                                if (existingRating != null) {
                                    onUpdate(score, comment)
                                } else {
                                    onRate(score, comment)
                                }
                                isEditing = false
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(ButtonDefaults.MediumContainerHeight),
                            enabled = !isActionLoading,
                        ) {
                            if (isActionLoading) {
                                LoadingIndicator()
                            } else {
                                Text(if (existingRating != null) "Обновить" else "Сохранить")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Удалить оценку?") },
            text = { Text("Оценка по критерию «${criterion.name}» будет удалена.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
                        showDeleteConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) { Text("Удалить") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Отмена") }
            }
        )
    }
}