package software.setixx.chimu.presentation.main.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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
                        title = "У вас пока нет команд",
                        description = "Создайте команду или присоединитесь к существующей"
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(state.userTeams.size) { index ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onNavigateToTeam(state.userTeams[index].id) },
                                shape = MaterialTheme.shapes.largeIncreased,
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = state.userTeams[index].name,
                                            style = MaterialTheme.typography.titleMedium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                        if (state.userTeams[index].isLeader) {
                                            Icon(
                                                Icons.Default.Star,
                                                "Лидер",
                                                modifier = Modifier.size(20.dp),
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }

                                    state.userTeams[index].description?.let { desc ->
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = desc,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.Person,
                                            null,
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "${state.userTeams[index].memberCount} участников",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
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
                        contentDescription = "Действия с командами"
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
                text = { Text("Присоединиться") }
            )
            FloatingActionButtonMenuItem(
                onClick = {
                    fabExpanded = false
                    onNavigateToCreateTeam()
                },
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("Создать") }
            )
        }
    }
}
