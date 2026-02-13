package software.setixx.chimu.presentation.team

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import software.setixx.chimu.domain.model.Specialization
import software.setixx.chimu.domain.model.TeamMember
import software.setixx.chimu.presentation.components.DeleteTeamDialog
import software.setixx.chimu.presentation.components.InfoRow
import software.setixx.chimu.presentation.components.InviteTokenDialog
import software.setixx.chimu.presentation.components.KickMemberDialog
import software.setixx.chimu.presentation.components.LeaveTeamDialog
import software.setixx.chimu.presentation.components.MemberCard
import software.setixx.chimu.presentation.components.SpecializationDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamDetailsScreen(
    teamId: String,
    onBack: () -> Unit,
    viewModel: TeamDetailsViewModel = koinViewModel { parametersOf(teamId) }
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.loadTeamDetails(teamId)
    }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { error ->
            snackbarHostState.showSnackbar(error, duration = SnackbarDuration.Short)
            viewModel.clearError()
        }
    }

    LaunchedEffect(state.successMessage) {
        state.successMessage?.let { message ->
            snackbarHostState.showSnackbar(message, duration = SnackbarDuration.Short)
            viewModel.clearSuccess()
        }
    }

    if (state.showLeaveDialog) {
        LeaveTeamDialog(
            onConfirm = { viewModel.leaveTeam(onBack) },
            onDismiss = { viewModel.hideLeaveDialog() }
        )
    }

    if (state.showDeleteDialog) {
        DeleteTeamDialog(
            onConfirm = { viewModel.deleteTeam(onBack) },
            onDismiss = { viewModel.hideDeleteDialog() }
        )
    }

    if (state.showKickDialog && state.memberToKick != null) {
        KickMemberDialog(
            member = state.memberToKick!!,
            onConfirm = { viewModel.kickMember() },
            onDismiss = { viewModel.hideKickDialog() }
        )
    }

    if (state.showInviteDialog) {
        InviteTokenDialog(
            token = state.team?.inviteToken,
            onRegenerate = { viewModel.regenerateToken() },
            onDismiss = { viewModel.hideInviteDialog() }
        )
    }

    if (state.showSpecializationDialog) {
        SpecializationDialog(
            availableSpecializations = state.availableSpecializations,
            selectedSpecialization = state.selectedSpecialization,
            onSpecializationChange = { viewModel.updateSpecialization(it) },
            onConfirm = { viewModel.saveSpecialization() },
            onDismiss = { viewModel.hideSpecializationDialog() }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.team?.name ?: "Команда") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Назад")
                    }
                },
                actions = {
                    if (viewModel.isCurrentUserLeader() && !state.isEditing) {
                        IconButton(onClick = { viewModel.showInviteDialog() }) {
                            Icon(Icons.Default.Share, "Пригласить")
                        }
                        IconButton(onClick = { viewModel.toggleEditMode() }) {
                            Icon(Icons.Default.Edit, "Редактировать")
                        }
                    }

                    if (state.isEditing) {
                        TextButton(onClick = { viewModel.toggleEditMode() }) {
                            Text("Отмена")
                        }
                        TextButton(
                            onClick = { viewModel.saveTeam() },
                            enabled = !state.isSaving
                        ) {
                            if (state.isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Сохранить")
                            }
                        }
                    }

                    var showMenu by remember { mutableStateOf(false) }

                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, "Меню")
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            if (viewModel.isCurrentUserLeader()) {
                                DropdownMenuItem(
                                    text = { Text("Удалить команду") },
                                    onClick = {
                                        showMenu = false
                                        viewModel.showDeleteDialog()
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Delete,
                                            null,
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                )
                            } else {
                                DropdownMenuItem(
                                    text = { Text("Покинуть команду") },
                                    onClick = {
                                        showMenu = false
                                        viewModel.showLeaveDialog()
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.ExitToApp,
                                            null,
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            state.team?.let { team ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(8.dp)) }

                    item {
                        Card {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                    "Информация о команде",
                                    style = MaterialTheme.typography.titleMedium
                                )

                                if (state.isEditing) {
                                    OutlinedTextField(
                                        value = state.editName,
                                        onValueChange = { viewModel.updateName(it) },
                                        label = { Text("Название *") },
                                        enabled = !state.isSaving,
                                        modifier = Modifier.fillMaxWidth(),
                                        isError = state.nameError != null,
                                        supportingText = state.nameError?.let { { Text(it) } },
                                        singleLine = true
                                    )

                                    OutlinedTextField(
                                        value = state.editDescription,
                                        onValueChange = { viewModel.updateDescription(it) },
                                        label = { Text("Описание") },
                                        enabled = !state.isSaving,
                                        modifier = Modifier.fillMaxWidth(),
                                        minLines = 3,
                                        maxLines = 5
                                    )
                                } else {
                                    InfoRow(
                                        icon = Icons.Default.Group,
                                        label = "Название",
                                        value = team.name
                                    )

                                    team.description?.let { desc ->
                                        InfoRow(
                                            icon = Icons.Default.Description,
                                            label = "Описание",
                                            value = desc
                                        )
                                    }

                                    InfoRow(
                                        icon = Icons.Default.Event,
                                        label = "Создана",
                                        value = team.createdAt
                                    )

                                    InfoRow(
                                        icon = Icons.Default.Person,
                                        label = "Участников",
                                        value = "${team.members.size}"
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Участники",
                                style = MaterialTheme.typography.titleMedium
                            )

                            val currentMember = viewModel.getCurrentUserMember()
                            if (currentMember != null) {
                                TextButton(
                                    onClick = {
                                        viewModel.showSpecializationDialog(currentMember.specialization)
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.Work,
                                        null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Моя специализация")
                                }
                            }
                        }
                    }

                    items(team.members) { member ->
                        MemberCard(
                            member = member,
                            isLeader = viewModel.isCurrentUserLeader(),
                            currentUserId = viewModel.getCurrentUserMember()?.userId,
                            onKick = { viewModel.showKickDialog(member) }
                        )
                    }

                    item { Spacer(modifier = Modifier.height(8.dp)) }
                }
            }
        }
    }
}






