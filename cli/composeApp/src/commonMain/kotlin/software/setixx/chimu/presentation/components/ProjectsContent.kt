package software.setixx.chimu.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import software.setixx.chimu.presentation.main.MainState

@Composable
fun ProjectsContent(state: MainState) {
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
                text = "Мои проекты",
                style = MaterialTheme.typography.headlineMedium
            )
        }

        if (state.userProjects.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Default.Gamepad,
                    title = "У вас пока нет проектов",
                    description = "Зарегистрируйтесь на джем и создайте проект"
                )
            }
        } else {
            items(state.userProjects.size) { index ->
                ProjectCard(project = state.userProjects[index])
            }
        }
    }
}