package software.setixx.chimu.presentation.jam.details.project

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import software.setixx.chimu.api.domain.UserRole
import software.setixx.chimu.domain.model.GameJamDetails
import software.setixx.chimu.domain.model.Project
import software.setixx.chimu.presentation.jam.details.overview.components.ManagementListCard
import software.setixx.chimu.presentation.project.components.ProjectDetailsContent
import software.setixx.chimu.presentation.project.components.ProjectEditDialog

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ProjectScreen(
    jamId: String,
    jam: GameJamDetails,
    userRole: UserRole?,
    userId: String?,
    onNavigateToProject: (projectId: String, roleStr: String?, isAdminOrOrganizer: Boolean) -> Unit,
    viewModel: ProjectViewModel = koinViewModel(),
    paddingValues: PaddingValues
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()
    val isAdminOrOrganizer = userRole == UserRole.ADMIN ||
            (userRole == UserRole.ORGANIZER && jam.organizerId == userId)
    val isParticipant = userRole == UserRole.PARTICIPANT

    var showCreateDialog by remember { mutableStateOf(false) }
    var dialogTitle by remember { mutableStateOf("") }
    var dialogDescription by remember { mutableStateOf("") }
    var dialogGameUrl by remember { mutableStateOf("") }

    LaunchedEffect(jamId, isAdminOrOrganizer) {
        viewModel.load(jamId, isAdminOrOrganizer)
    }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            state.isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    LoadingIndicator()
                }
            }
            isAdminOrOrganizer -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    AdminProjectListSection(
                        projects = state.allProjects,
                        userRole = userRole,
                        onNavigateToProject = onNavigateToProject
                    )
                    Spacer(Modifier.height(80.dp))
                }
            }
            isParticipant -> {
                val project = state.userProject
                val isLeader = state.isUserLeaderOfRegisteredTeam()

                if (project == null) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(paddingValues)
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CreateProjectCard(
                            isLeader = isLeader,
                            onCreate = {
                                dialogTitle = ""
                                dialogDescription = ""
                                dialogGameUrl = ""
                                showCreateDialog = true
                            }
                        )
                        Spacer(Modifier.height(80.dp))
                    }
                } else {
                    ProjectDetailsContent(
                        project = project,
                        projectFiles = state.projectFiles,
                        userRole = userRole,
                        isAdminOrOrganizer = false,
                        isLeader = isLeader,
                        isActionLoading = state.isActionLoading,
                        viewModel = viewModel,
                        modifier = Modifier.padding(paddingValues)
                    )
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    if (showCreateDialog) {
        ProjectEditDialog(
            title = "Создание проекта",
            projectTitle = dialogTitle,
            projectDescription = dialogDescription,
            projectGameUrl = dialogGameUrl,
            onTitleChange = { dialogTitle = it },
            onDescriptionChange = { dialogDescription = it },
            onGameUrlChange = { dialogGameUrl = it },
            onConfirm = {
                viewModel.createProject(
                    jamId,
                    dialogTitle,
                    dialogDescription.takeIf { it.isNotBlank() }
                )
                showCreateDialog = false
            },
            onDismiss = { showCreateDialog = false }
        )
    }
}

@Composable
private fun CreateProjectCard(
    isLeader: Boolean,
    onCreate: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Проект", style = MaterialTheme.typography.titleLarge)
            Text(
                text = if (isLeader)
                    "Проект ещё не создан. Создайте его, чтобы начать работу."
                else
                    "Проект ещё не создан. Обратитесь к лидеру команды.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (isLeader) {
                Button(
                    onClick = onCreate,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Создать проект")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun AdminProjectListSection(
    projects: List<Project>,
    userRole: UserRole?,
    onNavigateToProject: (String, String?, Boolean) -> Unit
) {
    ManagementListCard(
        title = "Проекты участников",
        titleIcon = Icons.Default.Gamepad,
        items = projects,
        emptyText = "Проекты ещё не созданы",
        onButtonClick = {},
        onItemClick = { onNavigateToProject(it.id, userRole?.name, true) },
        itemHeadline = { Text(it.title) },
        itemSupportingContent = {
            Text(
                "Команда: ${it.teamName ?: "—"}  •  ${it.status.name}",
                style = MaterialTheme.typography.bodySmall
            )
        },
        itemTrailingContent = {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    )
}
