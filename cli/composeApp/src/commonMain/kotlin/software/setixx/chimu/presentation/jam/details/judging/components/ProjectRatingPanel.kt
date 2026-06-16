package software.setixx.chimu.presentation.jam.details.judging.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
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
import chimu.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
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
    onRate: (projectId: String, criteriaId: String, score: Int, comment: String?) -> Unit,
    onUpdate: (ratingId: String, projectId: String, score: Int, comment: String?) -> Unit,
    onDelete: (ratingId: String, projectId: String) -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 1500.dp)
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isRatingLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingIndicator()
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(Res.string.judging_criteria_ratings_title),
                        style = MaterialTheme.typography.titleMedium,
                    )
                }

                if (criteria.isEmpty()) {
                    Text(
                        stringResource(Res.string.judging_criteria_empty),
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