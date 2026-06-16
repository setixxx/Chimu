package software.setixx.chimu.presentation.main.judge

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import chimu.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import software.setixx.chimu.presentation.components.EmptyStateCard
import software.setixx.chimu.presentation.main.MainState
import software.setixx.chimu.presentation.main.jam.GameJamCard

@Composable
fun JudgingContent(
    state: MainState,
    onNavigateToJamJudging: (String) -> Unit = {}
) {
    if (state.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            LoadingIndicator()
        }
    } else {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
        ) {
            Text(
                text = stringResource(Res.string.judging_title),
                style = MaterialTheme.typography.headlineMedium
            )

            if (state.judgingJams.isEmpty()) {
                EmptyStateCard(
                    icon = Icons.Filled.Gavel,
                    title = stringResource(Res.string.judging_empty_title),
                    description = stringResource(Res.string.judging_empty_desc)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(vertical = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(state.judgingJams.size) { index ->
                        GameJamCard(
                            jam = state.judgingJams[index],
                            onDetailsClick = onNavigateToJamJudging
                        )
                    }
                }
            }
        }
    }
}
