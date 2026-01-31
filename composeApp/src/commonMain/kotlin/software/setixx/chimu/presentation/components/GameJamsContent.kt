package software.setixx.chimu.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import software.setixx.chimu.presentation.main.MainState

@Composable
fun GameJamsContent(state: MainState) {
    if (state.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Все Game Jams",
                style = MaterialTheme.typography.headlineMedium
            )
        }

        if (state.activeJams.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Default.Event,
                    title = "Нет джемов"
                )
            }
        } else {
            items(state.activeJams.size) { index ->
                GameJamCard(jam = state.activeJams[index])
            }
        }
    }
}