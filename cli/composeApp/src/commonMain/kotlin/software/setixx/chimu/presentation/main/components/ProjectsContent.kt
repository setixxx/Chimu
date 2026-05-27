package software.setixx.chimu.presentation.main.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
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
fun ProjectsContent(
    state: MainState,
    onNavigateToProject: (String, String?, Boolean) -> Unit,
) {
    if (state.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            LoadingIndicator()
        }
        return
    }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        if (state.userProjects.isEmpty()) {
            EmptyStateCard(
                icon = Icons.Default.Gamepad,
                title = "У вас пока нет проектов",
                description = "Зарегистрируйтесь на джем и создайте проект"
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(state.userProjects.size) { index ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(),
                        onClick = { onNavigateToProject(state.userProjects[index].id, state.user?.role?.name, false) },
                        shape = MaterialTheme.shapes.extraLarge
                    ){
                        ProjectCard(project = state.userProjects[index])
                    }
                }
            }
        }
    }
}