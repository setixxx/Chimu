package software.setixx.chimu.presentation.jam.details.judging

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import software.setixx.chimu.presentation.jam.details.judging.components.ProjectRatingPanel

@Composable
fun ProjectRatingScreen(
    jamId: String,
    projectId: String,
    onBack: () -> Unit,
    viewModel: JudgingViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(jamId, projectId) {
        viewModel.loadProjectRating(jamId, projectId)
    }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    LaunchedEffect(state.successMessage) {
        state.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSuccess()
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            val project = state.selectedProject
            when {
                state.isLoading -> {
                    LoadingIndicator(modifier = Modifier.align(Alignment.Center))
                }
                project != null -> {
                    ProjectRatingPanel(
                        project = project,
                        criteria = state.jamCriteria,
                        myRatings = state.myRatings,
                        isRatingLoading = state.isRatingLoading,
                        isActionLoading = state.isActionLoading,
                        onBack = onBack,
                        onRate = { ratedProjectId, criteriaId, score, comment ->
                            viewModel.rateProject(ratedProjectId, criteriaId, score, comment)
                        },
                        onUpdate = { ratingId, ratedProjectId, score, comment ->
                            viewModel.updateRating(ratingId, ratedProjectId, score, comment)
                        },
                        onDelete = { ratingId, ratedProjectId ->
                            viewModel.deleteRating(ratingId, ratedProjectId)
                        }
                    )
                }
                else -> {
                    Text(
                        text = "Проект не найден",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}
