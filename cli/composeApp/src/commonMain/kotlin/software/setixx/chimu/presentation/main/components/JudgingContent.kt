package software.setixx.chimu.presentation.main.components

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
import software.setixx.chimu.presentation.components.EmptyStateCard
import software.setixx.chimu.presentation.main.MainState

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
                text = "Оценивание",
                style = MaterialTheme.typography.headlineMedium
            )

            if (state.judgingJams.isEmpty()) {
                EmptyStateCard(
                    icon = Icons.Filled.Gavel,
                    title = "Нет джемов для оценивания",
                    description = "Вы пока не назначены судьей ни на один джем"
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
