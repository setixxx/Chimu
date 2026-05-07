package software.setixx.chimu.presentation.jam.details.judging

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import software.setixx.chimu.domain.model.GameJamDetails
import software.setixx.chimu.presentation.components.StagePlaceholder

@Composable
fun JudgingScreen(
    jamId: String,
    jam: GameJamDetails,
    viewModel: JudgingViewModel = koinViewModel()
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        StagePlaceholder("Этап оценивания и завершенный джем пока не реализованы.")
    }
}
