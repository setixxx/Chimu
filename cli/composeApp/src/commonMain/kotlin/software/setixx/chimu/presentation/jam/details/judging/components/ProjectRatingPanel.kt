package software.setixx.chimu.presentation.jam.details.judging.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import software.setixx.chimu.data.remote.dto.RatingCriteriaResponse
import software.setixx.chimu.domain.model.MyRating
import software.setixx.chimu.domain.model.Project

@Composable
fun ProjectRatingPanel(
    project: Project,
    criteria: List<RatingCriteriaResponse>,
    myRatings: List<MyRating>,
    isRatingLoading: Boolean,
    isActionLoading: Boolean,
    onBack: () -> Unit,
    onRate: (projectId: String, criteriaId: String, score: Int, comment: String?) -> Unit,
    onUpdate: (ratingId: String, projectId: String, score: Int, comment: String?) -> Unit,
    onDelete: (ratingId: String, projectId: String) -> Unit
) {
    val scrollState = rememberScrollState()

    Column(modifier = Modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            tonalElevation = 3.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад")
                }
                Column(modifier = Modifier.padding(start = 8.dp)) {
                    Text(
                        project.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    project.teamName?.let {
                        Text(
                            it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (isRatingLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingIndicator()
                }
            } else {
                Text(
                    "Оценки по критериям",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                if (criteria.isEmpty()) {
                    Text(
                        "Критерии оценивания не заданы",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    criteria.sortedBy { it.orderIndex }.forEach { criterion ->
                        val existingRating = myRatings.find { it.criteriaId == criterion.id }
                        CriteriaRatingCard(
                            criterion = criterion,
                            existingRating = existingRating,
                            isActionLoading = isActionLoading,
                            onRate = { score, comment ->
                                onRate(project.id, criterion.id, score, comment)
                            },
                            onUpdate = { score, comment ->
                                existingRating?.let {
                                    onUpdate(it.id, project.id, score, comment)
                                }
                            },
                            onDelete = {
                                existingRating?.let {
                                    onDelete(it.id, project.id)
                                }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}