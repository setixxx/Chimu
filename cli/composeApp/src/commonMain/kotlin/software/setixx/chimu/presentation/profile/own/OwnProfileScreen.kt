package software.setixx.chimu.presentation.profile.own

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import software.setixx.chimu.api.domain.UserRole
import software.setixx.chimu.domain.model.Skill
import software.setixx.chimu.presentation.profile.components.EditableProfileField
import software.setixx.chimu.presentation.profile.components.ProfileHeader
import software.setixx.chimu.presentation.profile.components.ProfileSkillsView
import software.setixx.chimu.presentation.profile.roleupgrade.RoleUpgradeDialog
import software.setixx.chimu.presentation.profile.roleupgrade.RoleUpgradeViewModel
import software.setixx.chimu.presentation.utils.DateTimeUtils

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onDeleteAccount: () -> Unit,
    viewModel: OwnProfileViewModel = koinViewModel(),
    roleUpgradeViewModel: RoleUpgradeViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showRoleUpgradeDialog by remember { mutableStateOf(false) }

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Профиль") },
                navigationIcon = {
                    FilledTonalIconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад")
                    }
                },
                actions = {
                    if (state.isEditing) {
                        TextButton(onClick = { viewModel.toggleEditMode() }) {
                            Text("Отмена")
                        }
                        TextButton(
                            onClick = { viewModel.saveProfile() },
                            enabled = !state.isSaving
                        ) {
                            if (state.isSaving) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            } else {
                                Text("Сохранить")
                            }
                        }
                    } else {
                        FilledTonalIconButton(onClick = { viewModel.toggleEditMode() }) {
                            Icon(Icons.Default.Edit, "Редактировать")
                        }

                        if (state.user?.role != UserRole.GUEST) {
                            FilledTonalIconButton(
                                onClick = { showRoleUpgradeDialog = true }
                            ) {
                                Icon(
                                    Icons.Default.AdminPanelSettings,
                                    contentDescription = "Повышение роли"
                                )
                            }
                        }

                        FilledTonalIconButton(
                            onClick = { showDeleteDialog = true },
                            colors = IconButtonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.error,
                                disabledContainerColor = MaterialTheme.colorScheme.errorContainer,
                                disabledContentColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                "Удалить аккаунт",
                                tint = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            if (state.isLoading) {
                LoadingIndicator()
            } else {
                Column(
                    modifier = Modifier
                        .widthIn(max = 1500.dp)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    state.user?.let {
                        ProfileHeader(
                            primaryText = it.email,
                            secondaryText = state.role
                        )
                    }

                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
                    ) {
                        val totalFields = 8

                        EditableProfileField(
                            value = state.nickname,
                            onValueChange = { viewModel.updateNickname(it) },
                            label = "Никнейм",
                            leadingIcon = Icons.Default.AccountCircle,
                            isEditing = state.isEditing,
                            enabled = !state.isSaving,
                            isError = state.nicknameError != null,
                            supportingText = state.nicknameError,
                            itemIndex = 0,
                            listCount = totalFields
                        )

                        EditableProfileField(
                            value = state.firstName,
                            onValueChange = { viewModel.updateFirstName(it) },
                            label = "Имя",
                            leadingIcon = Icons.Default.Person,
                            isEditing = state.isEditing,
                            enabled = !state.isSaving,
                            itemIndex = 1,
                            listCount = totalFields
                        )

                        EditableProfileField(
                            value = state.lastName,
                            onValueChange = { viewModel.updateLastName(it) },
                            label = "Фамилия",
                            leadingIcon = Icons.Default.Person,
                            isEditing = state.isEditing,
                            enabled = !state.isSaving,
                            itemIndex = 2,
                            listCount = totalFields
                        )

                        EditableProfileField(
                            value = state.bio,
                            onValueChange = { viewModel.updateBio(it) },
                            label = "О себе",
                            leadingIcon = Icons.Default.Info,
                            isEditing = state.isEditing,
                            enabled = !state.isSaving,
                            minLines = 3,
                            itemIndex = 3,
                            listCount = totalFields
                        )

                        if (state.isEditing) {
                            var expandedSpec by remember { mutableStateOf(false) }

                            ExposedDropdownMenuBox(
                                expanded = expandedSpec,
                                onExpandedChange = { expandedSpec = !state.isSaving && it }
                            ) {
                                EditableProfileField(
                                    value = state.selectedSpecialization?.name ?: "Не выбрано",
                                    onValueChange = {},
                                    label = "Специализация",
                                    leadingIcon = Icons.Default.Work,
                                    isEditing = true,
                                    readOnly = true,
                                    enabled = !state.isSaving,
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSpec)
                                    },
                                    modifier = Modifier.menuAnchor(),
                                    itemIndex = 4,
                                    listCount = totalFields
                                )

                                ExposedDropdownMenu(
                                    expanded = expandedSpec,
                                    onDismissRequest = { expandedSpec = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Не выбрано") },
                                        onClick = {
                                            viewModel.updateSpecialization(null)
                                            expandedSpec = false
                                        }
                                    )
                                    state.availableSpecializations.forEach { spec ->
                                        DropdownMenuItem(
                                            text = {
                                                Column {
                                                    Text(spec.name)
                                                    spec.description?.let {
                                                        Text(
                                                            it,
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                }
                                            },
                                            onClick = {
                                                viewModel.updateSpecialization(spec)
                                                expandedSpec = false
                                            }
                                        )
                                    }
                                }
                            }
                        } else {
                            EditableProfileField(
                                value = state.selectedSpecialization?.name ?: "",
                                onValueChange = {},
                                label = "Специализация",
                                leadingIcon = Icons.Default.Work,
                                isEditing = false,
                                itemIndex = 4,
                                listCount = totalFields
                            )
                        }

                        EditableProfileField(
                            value = state.githubUrl,
                            onValueChange = { viewModel.updateGithubUrl(it) },
                            label = "GitHub",
                            leadingIcon = Icons.Default.Code,
                            isEditing = state.isEditing,
                            enabled = !state.isSaving,
                            isError = state.githubError != null,
                            supportingText = state.githubError,
                            placeholder = "https://github.com/username",
                            itemIndex = 5,
                            listCount = totalFields
                        )

                        EditableProfileField(
                            value = state.telegramUsername,
                            onValueChange = { viewModel.updateTelegramUsername(it) },
                            label = "Telegram",
                            leadingIcon = Icons.Default.Send,
                            isEditing = state.isEditing,
                            enabled = !state.isSaving,
                            isError = state.telegramError != null,
                            supportingText = state.telegramError,
                            placeholder = "username",
                            prefix = { Text("@") },
                            itemIndex = 6,
                            listCount = totalFields
                        )

                        if (state.isEditing) {
                            Spacer(modifier = Modifier.height(8.dp))
                            SkillsSelector(
                                availableSkills = state.availableSkills,
                                selectedSkills = state.selectedSkills,
                                onSkillToggle = { viewModel.toggleSkill(it) },
                                enabled = !state.isSaving
                            )
                        } else {
                            ProfileSkillsView(
                                skills = state.selectedSkills.map { it.name },
                                itemIndex = 7,
                                listCount = totalFields
                            )
                        }
                    }

                    Text(
                        text = "Зарегистрирован: ${DateTimeUtils.formatDateTime(state.user?.createdAt)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Удалить аккаунт?") },
            text = { Text("Все данные будут безвозвратно удалены.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteProfile(onDeleteAccount)
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) { Text("Удалить") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Отмена") }
            }
        )
    }

    if (showRoleUpgradeDialog) {
        state.user?.role?.let {
            RoleUpgradeDialog(
                currentRole = it,
                isAdmin = it == UserRole.ADMIN,
                viewModel = roleUpgradeViewModel,
                onDismiss = { showRoleUpgradeDialog = false }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkillsSelector(
    availableSkills: List<Skill>,
    selectedSkills: List<Skill>,
    onSkillToggle: (Skill) -> Unit,
    enabled: Boolean
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            "Навыки",
            style = MaterialTheme.typography.titleSmall
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = enabled && it }
        ) {
            OutlinedButton(
                onClick = { if (enabled) expanded = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                enabled = enabled
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (selectedSkills.isEmpty())
                            "Выберите навыки"
                        else
                            "${selectedSkills.size} выбрано"
                    )
                    Icon(
                        if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null
                    )
                }
            }

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.heightIn(max = 400.dp)
            ) {
                availableSkills.forEach { skill ->
                    val isSelected = selectedSkills.contains(skill)
                    DropdownMenuItem(
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = null
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(skill.name)
                            }
                        },
                        onClick = { onSkillToggle(skill) }
                    )
                }
            }
        }

        if (selectedSkills.isNotEmpty()) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                selectedSkills.forEach { skill ->
                    InputChip(
                        selected = true,
                        onClick = { if (enabled) onSkillToggle(skill) },
                        label = { Text(skill.name) },
                        trailingIcon = {
                            if (enabled) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Удалить",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        },
                        enabled = enabled
                    )
                }
            }
        }
    }
}