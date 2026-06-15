package software.setixx.chimu.presentation.main.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import software.setixx.chimu.presentation.components.EmptyStateCard
import software.setixx.chimu.presentation.main.MainState
import software.setixx.chimu.presentation.main.jam.GameJamCard
import software.setixx.chimu.presentation.main.project.ProjectCard
import software.setixx.chimu.presentation.main.team.TeamCard

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HomeContent(
    state: MainState,
    onNavigateToTeam: (String) -> Unit,
    onNavigateToJamDetails: (String) -> Unit,
    onNavigateToProject: (String, String?, Boolean) -> Unit,
) {
    val teamScrollState = rememberScrollState()

    if (state.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            LoadingIndicator()
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize().padding(24.dp),
    ) {
        Text(
            text = "Активные Game Jams",
            style = MaterialTheme.typography.headlineMedium
        )

        if (state.activeJams.isEmpty()) {
            EmptyStateCard(
                icon = Icons.Default.Event,
                title = "Нет активных джемов",
                description = "Джемы появятся здесь, когда начнется регистрация"
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                items(state.activeJams.size) { index ->
                    GameJamCard(
                        jam = state.activeJams[index],
                        onDetailsClick = onNavigateToJamDetails
                    )
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
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            items(state.userTeams.size) { index ->
                                TeamCard(
                                    modifier = Modifier.width(280.dp),
                                    team = state.userTeams[index],
                                    onClick = { onNavigateToTeam(state.userTeams[index].id) }
                                )
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

                    item {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            items(state.userProjects.size) { index ->
                                ProjectCard(
                                    modifier = Modifier.width(280.dp),
                                    project = state.userProjects[index],
                                    onClick = {
                                        onNavigateToProject(
                                            state.userProjects[index].id,
                                            state.user?.role?.name, false
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}