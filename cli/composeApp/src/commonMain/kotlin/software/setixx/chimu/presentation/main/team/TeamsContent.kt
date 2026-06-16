package software.setixx.chimu.presentation.main.team

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import chimu.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import software.setixx.chimu.presentation.components.EmptyStateCard
import software.setixx.chimu.presentation.main.MainState

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TeamsContent(
    state: MainState,
    onNavigateToCreateTeam: () -> Unit,
    onNavigateToJoinTeam: () -> Unit,
    onNavigateToTeam: (String) -> Unit
) {
    var fabExpanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                LoadingIndicator()
            }
        } else {
            Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
                if (state.userTeams.isEmpty()) {
                    EmptyStateCard(
                        icon = Icons.Default.Group,
                        title = stringResource(Res.string.teams_empty_title),
                        description = stringResource(Res.string.teams_empty_desc)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(state.userTeams.size) { index ->
                            TeamCard(
                                modifier = Modifier.fillMaxWidth(),
                                team = state.userTeams[index],
                                onClick = { onNavigateToTeam(state.userTeams[index].id) }
                            )
                        }
                    }
                }
            }
        }

        FloatingActionButtonMenu(
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp),
            expanded = fabExpanded,
            button = {
                FloatingActionButton(
                    onClick = { fabExpanded = !fabExpanded },
                    containerColor = if (!fabExpanded) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.onPrimaryContainer,
                    contentColor = if (!fabExpanded) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.primaryContainer,
                    shape = if (!fabExpanded) FloatingActionButtonDefaults.extendedFabShape else FloatingActionButtonDefaults.largeShape
                ) {
                    Icon(
                        if (fabExpanded) Icons.Default.Close else Icons.Default.Add,
                        contentDescription = stringResource(Res.string.teams_fab_desc)
                    )
                }
            }
        ) {
            FloatingActionButtonMenuItem(
                onClick = {
                    fabExpanded = false
                    onNavigateToJoinTeam()
                },
                icon = { Icon(Icons.AutoMirrored.Filled.Login, null) },
                text = { Text(stringResource(Res.string.teams_join)) }
            )
            FloatingActionButtonMenuItem(
                onClick = {
                    fabExpanded = false
                    onNavigateToCreateTeam()
                },
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text(stringResource(Res.string.teams_create)) }
            )
        }
    }
}
