package software.setixx.chimu.presentation.main.jam

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
import chimu.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import software.setixx.chimu.presentation.components.EmptyStateCard
import software.setixx.chimu.presentation.main.MainState

@Composable
fun GameJamsContent(
    state: MainState,
    onNavigateToCreateJam: () -> Unit = {},
    onNavigateToJamDetails: (String) -> Unit = {}
) {
    Box(modifier = Modifier.fillMaxSize()) {
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                LoadingIndicator()
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
            ) {
                Text(
                    text = stringResource(Res.string.jams_all_title),
                    style = MaterialTheme.typography.headlineMedium
                )

                if (state.activeJams.isEmpty()) {
                    EmptyStateCard(
                        icon = Icons.Default.Event,
                        title = stringResource(Res.string.home_empty_active_jams_title),
                        description = stringResource(Res.string.home_empty_active_jams_desc)
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize()) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize().padding(vertical = 24.dp),
                            verticalArrangement = Arrangement.spacedBy(24.dp),
                            contentPadding = PaddingValues(bottom = 80.dp)
                        ) {
                            items(state.activeJams.size) { index ->
                                GameJamCard(
                                    jam = state.activeJams[index],
                                    onDetailsClick = onNavigateToJamDetails,
                                )
                            }
                        }
                    }
                }
            }
            if (state.canCreateJam) {
                ExtendedFloatingActionButton(
                    onClick = onNavigateToCreateJam,
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text(stringResource(Res.string.create_jam_button)) },
                    modifier = Modifier
                        .padding(24.dp)
                        .align(Alignment.BottomEnd),
                )
            }
        }
    }
}