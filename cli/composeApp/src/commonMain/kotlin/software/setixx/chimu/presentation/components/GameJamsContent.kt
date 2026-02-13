package software.setixx.chimu.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import software.setixx.chimu.presentation.main.MainState

@Composable
fun GameJamsContent(
    state: MainState,
    onNavigateToCreateJam: () -> Unit = {},
    onNavigateToJamDetails: (String) -> Unit = {}
) {
    if (state.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val canCreateJam = state.user?.role == "ADMIN" || state.user?.role == "ORGANIZER"

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 24.dp, bottom = 80.dp)
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
                    GameJamCard(
                        jam = state.activeJams[index],
                        onDetailsClick = onNavigateToJamDetails
                    )
                }
            }
        }

        if (canCreateJam) {
            ExtendedFloatingActionButton(
                onClick = onNavigateToCreateJam,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Создать джем") },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp)
            )
        }
    }
}