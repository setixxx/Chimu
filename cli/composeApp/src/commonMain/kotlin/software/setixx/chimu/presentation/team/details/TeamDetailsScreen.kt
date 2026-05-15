package software.setixx.chimu.presentation.team.details

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import software.setixx.chimu.presentation.team.details.components.DeleteTeamDialog
import software.setixx.chimu.presentation.components.InfoRow
import software.setixx.chimu.presentation.team.details.components.InviteTokenDialog
import software.setixx.chimu.presentation.team.details.components.KickMemberDialog
import software.setixx.chimu.presentation.team.details.components.LeaveTeamDialog
import software.setixx.chimu.presentation.team.details.components.SpecializationDialog
import software.setixx.chimu.presentation.utils.DateTimeUtils

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TeamDetailsScreen(
    teamId: String,
    onBack: () -> Unit,
    onNavigateToUserProfile: (String) -> Unit,
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
                    if (state.isCurrentUserLeader() && !state.isEditing) {
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
                                LoadingIndicator(
                                    modifier = Modifier.size(24.dp),
                                )
                            } else {
                                Text("Сохранить")
                            }
                        }
                    }

                    if (state.isCurrentUserLeader()) {
                        IconButton(onClick = { viewModel.showDeleteDialog() }) {
                            Icon(
                                Icons.Default.Delete,
                                "Удалить команду",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    } else {
                        IconButton(onClick = { viewModel.showLeaveDialog() }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ExitToApp,
                                "Покинуть команду",
                                tint = MaterialTheme.colorScheme.error
                            )
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
                LoadingIndicator()
            }
        } else {
            state.team?.let { team ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 24.dp),
                ) {
                    item { Spacer(modifier = Modifier.height(8.dp)) }

                    item {
                        Card(
                            shape = MaterialTheme.shapes.largeIncreased
                        ) {
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
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(
                                            keyboardType = KeyboardType.Text,
                                            imeAction = ImeAction.Next
                                        ),
                                        shape = MaterialTheme.shapes.largeIncreased
                                    )

                                    OutlinedTextField(
                                        value = state.editDescription,
                                        onValueChange = { viewModel.updateDescription(it) },
                                        label = { Text("Описание") },
                                        enabled = !state.isSaving,
                                        modifier = Modifier.fillMaxWidth(),
                                        minLines = 3,
                                        maxLines = 5,
                                        shape = MaterialTheme.shapes.largeIncreased
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
                                        value = DateTimeUtils.formatDateTime(team.createdAt)
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

                    item { Spacer(modifier = Modifier.height(16.dp)) }

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

                            val currentMember = state.getCurrentUserMember()
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
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
                            team.members.forEachIndexed { index, member ->
                                val colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.onSecondary)
                                SegmentedListItem(
                                    colors = colors,
                                    selected = false,
                                    onClick = { onNavigateToUserProfile(member.userId) },
                                    shapes = ListItemDefaults.segmentedShapes(
                                        index = index,
                                        count = team.members.size
                                    ),
                                    content = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                member.nickname,
                                                style = MaterialTheme.typography.titleMedium
                                            )
                                            if (member.isLeader) {
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Icon(
                                                    Icons.Default.Star,
                                                    "Лидер",
                                                    modifier = Modifier.size(16.dp),
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    },
                                    supportingContent = {
                                        Column {
                                            member.specialization?.let { spec ->
                                                Text(
                                                    spec.name,
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                            }
                                            Text(
                                                "Присоединился: ${DateTimeUtils.formatDateTime(member.joinedAt)}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    },
                                    leadingContent = {
                                        Surface(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape),
                                            color = MaterialTheme.colorScheme.primaryContainer
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    Icons.Default.Person,
                                                    null,
                                                    tint = if (member.isLeader) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimaryContainer
                                                )
                                            }
                                        }
                                    },
                                    trailingContent = {
                                        val currentUserId = state.getCurrentUserMember()?.userId
                                        if (state.isCurrentUserLeader() && !member.isLeader && member.userId != currentUserId) {
                                            IconButton(onClick = { viewModel.showKickDialog(member) }) {
                                                Icon(
                                                    Icons.Default.PersonRemove,
                                                    "Исключить",
                                                    tint = MaterialTheme.colorScheme.error
                                                )
                                            }
                                        }
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






