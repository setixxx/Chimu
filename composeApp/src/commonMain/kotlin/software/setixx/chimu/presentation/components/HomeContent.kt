package software.setixx.chimu.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
fun HomeContent(state: MainState, onNavigateToTeam: (String) -> Unit) {
    if (state.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Text(
                text = "Активные Game Jams",
                style = MaterialTheme.typography.headlineMedium
            )
        }

        if (state.activeJams.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Default.Event,
                    title = "Нет активных джемов",
                    description = "Джемы появятся здесь, когда начнется регистрация"
                )
            }
        } else {
            items(state.activeJams.size) { index ->
                GameJamCard(jam = state.activeJams[index])
            }
        }

        if (state.userTeams.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Мои команды",
                    style = MaterialTheme.typography.headlineMedium
                )
            }

            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.userTeams.size) { index ->
                        TeamCard(team = state.userTeams[index], onClick = { onNavigateToTeam(state.userTeams[index].id) })
                    }
                }
            }
        }

        if (state.userProjects.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Мои проекты",
                    style = MaterialTheme.typography.headlineMedium
                )
            }

            items(state.userProjects.size) { index ->
                ProjectCard(project = state.userProjects[index])
            }
        }
    }
}