package software.setixx.chimu.presentation.profile.alien

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import software.setixx.chimu.presentation.profile.components.EditableProfileField
import software.setixx.chimu.presentation.profile.components.ProfileHeader
import software.setixx.chimu.presentation.profile.components.ProfileSkillsView
import software.setixx.chimu.presentation.utils.DateTimeUtils

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun UserProfileScreen(
    userId: String,
    onBack: () -> Unit,
    viewModel: AlienProfileViewModel = koinViewModel { parametersOf(userId) }
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it, duration = SnackbarDuration.Short)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Профиль") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Назад")
                    }
                }
            )
        }
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
                state.profile?.let { profile ->
                    Column(
                        modifier = Modifier
                            .widthIn(max = 1500.dp)
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        ProfileHeader(
                            primaryText = profile.nickname,
                            secondaryText = state.role
                        )

                        if (profile.isDeleted) {
                            Spacer(modifier = Modifier.height(32.dp))
                            Text(
                                text = "Аккаунт удалён",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
                            ) {
                                val totalFields = 7

                                EditableProfileField(
                                    value = profile.firstName ?: "",
                                    onValueChange = {},
                                    label = "Имя",
                                    leadingIcon = Icons.Default.Person,
                                    isEditing = false,
                                    itemIndex = 0,
                                    listCount = totalFields
                                )

                                EditableProfileField(
                                    value = profile.lastName ?: "",
                                    onValueChange = {},
                                    label = "Фамилия",
                                    leadingIcon = Icons.Default.Person,
                                    isEditing = false,
                                    itemIndex = 1,
                                    listCount = totalFields
                                )

                                EditableProfileField(
                                    value = profile.bio ?: "",
                                    onValueChange = {},
                                    label = "О себе",
                                    leadingIcon = Icons.Default.Info,
                                    isEditing = false,
                                    minLines = 3,
                                    itemIndex = 2,
                                    listCount = totalFields
                                )

                                EditableProfileField(
                                    value = profile.specialization?.name ?: "",
                                    onValueChange = {},
                                    label = "Специализация",
                                    leadingIcon = Icons.Default.Work,
                                    isEditing = false,
                                    itemIndex = 3,
                                    listCount = totalFields
                                )

                                EditableProfileField(
                                    value = profile.githubUrl ?: "",
                                    onValueChange = {},
                                    label = "GitHub",
                                    leadingIcon = Icons.Default.Code,
                                    isEditing = false,
                                    placeholder = "https://github.com/username",
                                    itemIndex = 4,
                                    listCount = totalFields
                                )

                                EditableProfileField(
                                    value = profile.telegramUrl ?: "",
                                    onValueChange = {},
                                    label = "Telegram",
                                    leadingIcon = Icons.Default.Send,
                                    isEditing = false,
                                    placeholder = "username",
                                    prefix = { Text("@") },
                                    itemIndex = 5,
                                    listCount = totalFields
                                )

                                ProfileSkillsView(
                                    skills = profile.skills,
                                    itemIndex = 6,
                                    listCount = totalFields
                                )
                            }
                        }

                        profile.createdAt?.let {
                            Text(
                                text = "Зарегистрирован: ${DateTimeUtils.formatDateTime(it)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } ?: run {
                    Text(
                        text = state.errorMessage ?: "Не удалось загрузить профиль",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(24.dp)
                    )
                }
            }
        }
    }
}
