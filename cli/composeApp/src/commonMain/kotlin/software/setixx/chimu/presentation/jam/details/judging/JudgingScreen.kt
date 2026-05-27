package software.setixx.chimu.presentation.jam.details.judging

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import software.setixx.chimu.api.domain.GameJamStatus
import software.setixx.chimu.api.domain.UserRole
import software.setixx.chimu.data.remote.dto.RatingCriteriaResponse
import software.setixx.chimu.domain.model.GameJamDetails
import software.setixx.chimu.domain.model.MyRating
import software.setixx.chimu.domain.model.Project
import software.setixx.chimu.presentation.jam.details.judging.components.CriteriaRatingCard
import software.setixx.chimu.presentation.jam.details.judging.components.JudgeProgressCard
import software.setixx.chimu.presentation.jam.details.judging.components.JudgeView
import software.setixx.chimu.presentation.jam.details.judging.components.MissingRatingsCard
import software.setixx.chimu.presentation.jam.details.judging.components.ParticipantJudgingView
import software.setixx.chimu.presentation.jam.details.judging.components.ParticipantProjectCard
import software.setixx.chimu.presentation.jam.details.judging.components.ProjectCard
import software.setixx.chimu.presentation.jam.details.judging.components.ProjectInfoCard
import software.setixx.chimu.presentation.jam.details.judging.components.ProjectRatingPanel
import software.setixx.chimu.presentation.jam.details.judging.components.ProjectStatusBadge
import software.setixx.chimu.presentation.utils.DateTimeUtils

@Composable
fun JudgingScreen(
    jamId: String,
    jam: GameJamDetails,
    viewModel: JudgingViewModel = koinViewModel(),
    onNavigateToProject: ((String, String?, Boolean) -> Unit)? = null,
    paddingValues: PaddingValues
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(jamId) {
        viewModel.loadAsJudge(jamId)
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                LoadingIndicator()
            }
        } else if (jam.status != GameJamStatus.JUDGING){
            Text(
                modifier = Modifier
                    .align(Alignment.Center),
                text = "Этап судейства еще не начался. Дождитесь начала",
                textAlign = TextAlign.Center
            )
        } else {
            JudgeView(
                state = state,
                jamCriteria = jam.criteria,
                onSelectProject = { viewModel.selectProject(it) },
                onBack = { viewModel.clearSelectedProject() },
                onRate = { projectId, criteriaId, score, comment ->
                    viewModel.rateProject(projectId, criteriaId, score, comment)
                },
                onUpdate = { ratingId, projectId, score, comment ->
                    viewModel.updateRating(ratingId, projectId, score, comment)
                },
                onDelete = { ratingId, projectId ->
                    viewModel.deleteRating(ratingId, projectId)
                },
                onNavigateToProject = onNavigateToProject
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}