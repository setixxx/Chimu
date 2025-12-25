package software.setixx.chimu.presentation.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import software.setixx.chimu.domain.model.Specialization
import software.setixx.chimu.domain.model.Skill

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    viewModel: ProfileViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

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
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Назад")
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
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Сохранить")
                            }
                        }
                    } else {
                        IconButton(onClick = { viewModel.toggleEditMode() }) {
                            Icon(Icons.Default.Edit, "Редактировать")
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Person,
                            null,
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Text(
                    text = state.user?.email ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedTextField(
                            value = state.nickname,
                            onValueChange = { viewModel.updateNickname(it) },
                            label = { Text("Никнейм *") },
                            enabled = state.isEditing && !state.isSaving,
                            modifier = Modifier.fillMaxWidth(),
                            isError = state.nicknameError != null,
                            supportingText = state.nicknameError?.let { { Text(it) } },
                            leadingIcon = { Icon(Icons.Default.AccountCircle, null) }
                        )

                        OutlinedTextField(
                            value = state.firstName,
                            onValueChange = { viewModel.updateFirstName(it) },
                            label = { Text("Имя") },
                            enabled = state.isEditing && !state.isSaving,
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = { Icon(Icons.Default.Person, null) }
                        )

                        OutlinedTextField(
                            value = state.lastName,
                            onValueChange = { viewModel.updateLastName(it) },
                            label = { Text("Фамилия") },
                            enabled = state.isEditing && !state.isSaving,
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = { Icon(Icons.Default.Person, null) }
                        )

                        OutlinedTextField(
                            value = state.bio,
                            onValueChange = { viewModel.updateBio(it) },
                            label = { Text("О себе") },
                            enabled = state.isEditing && !state.isSaving,
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            maxLines = 5,
                            leadingIcon = { Icon(Icons.Default.Info, null) }
                        )

                        if (state.isEditing) {
                            var expandedSpec by remember { mutableStateOf(false) }

                            ExposedDropdownMenuBox(
                                expanded = expandedSpec,
                                onExpandedChange = { expandedSpec = it }
                            ) {
                                OutlinedTextField(
                                    value = state.selectedSpecialization?.name ?: "Не выбрано",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Специализация") },
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSpec)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(),
                                    leadingIcon = { Icon(Icons.Default.Work, null) }
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
                                            text = { Text(spec.name) },
                                            onClick = {
                                                viewModel.updateSpecialization(spec)
                                                expandedSpec = false
                                            }
                                        )
                                    }
                                }
                            }
                        } else {
                            OutlinedTextField(
                                value = state.selectedSpecialization?.name ?: "Не указана",
                                onValueChange = {},
                                label = { Text("Специализация") },
                                enabled = false,
                                modifier = Modifier.fillMaxWidth(),
                                leadingIcon = { Icon(Icons.Default.Work, null) }
                            )
                        }

                        OutlinedTextField(
                            value = state.githubUrl,
                            onValueChange = { viewModel.updateGithubUrl(it) },
                            label = { Text("GitHub") },
                            enabled = state.isEditing && !state.isSaving,
                            modifier = Modifier.fillMaxWidth(),
                            isError = state.githubError != null,
                            supportingText = state.githubError?.let { { Text(it) } },
                            leadingIcon = { Icon(Icons.Default.Code, null) },
                            placeholder = { Text("https://github.com/username") }
                        )

                        OutlinedTextField(
                            value = state.telegramUsername,
                            onValueChange = { viewModel.updateTelegramUsername(it) },
                            label = { Text("Telegram") },
                            enabled = state.isEditing && !state.isSaving,
                            modifier = Modifier.fillMaxWidth(),
                            isError = state.telegramError != null,
                            supportingText = state.telegramError?.let { { Text(it) } },
                            leadingIcon = { Icon(Icons.Default.Send, null) },
                            placeholder = { Text("username") },
                            prefix = { Text("@") }
                        )

                        if (state.isEditing) {
                            Text(
                                "Навыки",
                                style = MaterialTheme.typography.titleSmall
                            )

                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                state.availableSkills.forEach { skill ->
                                    val isSelected = state.selectedSkills.contains(skill)
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { viewModel.toggleSkill(skill) },
                                        label = { Text(skill.name) },
                                        enabled = !state.isSaving
                                    )
                                }
                            }
                        } else if (state.selectedSkills.isNotEmpty()) {
                            Text(
                                "Навыки",
                                style = MaterialTheme.typography.titleSmall
                            )

                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                state.selectedSkills.forEach { skill ->
                                    AssistChip(
                                        onClick = { },
                                        label = { Text(skill.name) }
                                    )
                                }
                            }
                        }
                    }
                }

                Text(
                    text = "Зарегистрирован: ${state.user?.createdAt ?: ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}