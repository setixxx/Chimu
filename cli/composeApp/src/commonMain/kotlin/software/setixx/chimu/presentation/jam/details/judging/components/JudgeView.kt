package software.setixx.chimu.presentation.jam.details.judging.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import software.setixx.chimu.data.remote.dto.RatingCriteriaResponse
import software.setixx.chimu.domain.model.Project
import software.setixx.chimu.presentation.jam.details.judging.JudgingState

@Composable
fun JudgeView(
    state: JudgingState,
    jamCriteria: List<RatingCriteriaResponse>,
    onSelectProject: (Project) -> Unit,
    onBack: () -> Unit,
    onRate: (projectId: String, criteriaId: String, score: Int, comment: String?) -> Unit,
    onUpdate: (ratingId: String, projectId: String, score: Int, comment: String?) -> Unit,
    onDelete: (ratingId: String, projectId: String) -> Unit
) {
    val scrollState = rememberScrollState()

    val selectedProject = state.selectedProject
    if (selectedProject != null) {
        ProjectRatingPanel(
            project = selectedProject,
            criteria = jamCriteria,
            myRatings = state.myRatings,
            isRatingLoading = state.isRatingLoading,
            isActionLoading = state.isActionLoading,
            onBack = onBack,
            onRate = onRate,
            onUpdate = onUpdate,
            onDelete = onDelete
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            state.judgeProgress?.let { progress ->
                JudgeProgressCard(
                    ratedProjects = progress.ratedProjects,
                    totalProjects = progress.totalProjects
                )
            }

            state.judgeProgress?.let { progress ->
                if (progress.missingProjects.isNotEmpty()) {
                    MissingRatingsCard(missingCount = progress.missingProjects.size)
                }
            }

            Text(
                text = "Проекты для оценки (${state.projects.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            if (state.projects.isEmpty()) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Inbox,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Нет проектов для оценки",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                val ratedProjectIds = state.judgeProgress?.missingProjects
                    ?.map { it.projectId }?.toSet() ?: emptySet()

                state.projects.forEach { project ->
                    val isMissingRating = ratedProjectIds.contains(project.id)
                    ProjectCard(
                        project = project,
                        hasUnratedCriteria = isMissingRating,
                        onClick = { onSelectProject(project) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}