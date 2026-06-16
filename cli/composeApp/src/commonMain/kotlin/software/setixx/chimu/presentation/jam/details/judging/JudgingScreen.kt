package software.setixx.chimu.presentation.jam.details.judging

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import chimu.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import software.setixx.chimu.api.domain.GameJamStatus
import software.setixx.chimu.domain.model.GameJamDetails
import software.setixx.chimu.presentation.jam.details.judging.components.JudgeView

@Composable
fun JudgingScreen(
    jamId: String,
    jam: GameJamDetails,
    viewModel: JudgingViewModel = koinViewModel(),
    onNavigateToProject: ((String, String?, Boolean) -> Unit)? = null,
    onNavigateToProjectRating: (String) -> Unit,
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
                text = stringResource(Res.string.judging_not_started),
                textAlign = TextAlign.Center
            )
        } else {
            JudgeView(
                state = state,
                onSelectProject = { onNavigateToProjectRating(it.id) },
                onNavigateToProject = onNavigateToProject
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
