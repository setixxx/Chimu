package software.setixx.chimu.presentation.user

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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import software.setixx.chimu.domain.model.PublicUserProfile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    userId: String,
    onBack: () -> Unit,
    viewModel: UserProfileViewModel = koinViewModel { parametersOf(userId) }
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
                title = {
                    val title = when {
                        state.isLoading -> "Профиль"
                        state.profile?.isDeleted == true -> "Удалённый аккаунт"
                        state.profile != null -> "@${state.profile!!.nickname}"
                        else -> "Профиль"
                    }
                    Text(title)
                },
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
                .padding(paddingValues)
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                state.profile == null -> {
                    Text(
                        text = state.errorMessage ?: "Не удалось загрузить профиль",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center).padding(24.dp)
                    )
                }

                state.profile!!.isDeleted -> {
                    DeletedAccountContent(
                        nickname = state.profile!!.nickname,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                else -> {
                    ActiveProfileContent(
                        profile = state.profile!!,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

// ── Удалённый аккаунт ─────────────────────────────────────────────────────────

@Composable
private fun DeletedAccountContent(nickname: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = Icons.Default.PersonOff,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(nickname, style = MaterialTheme.typography.titleLarge)
        Text(
            text = "Аккаунт удалён",
            style = MaterialTheme.typography.bodyMedium,
            fontStyle = FontStyle.Italic,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ── Активный аккаунт ──────────────────────────────────────────────────────────

@Composable
private fun ActiveProfileContent(profile: PublicUserProfile, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Аватар + имя
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (profile.avatarUrl != null) {
                AsyncImage(
                    model = profile.avatarUrl,
                    contentDescription = "Аватар ${profile.nickname}",
                    modifier = Modifier.size(96.dp).clip(CircleShape)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(96.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text("@${profile.nickname}", style = MaterialTheme.typography.headlineSmall)

            val displayName = listOfNotNull(profile.firstName, profile.lastName)
                .joinToString(" ").takeIf { it.isNotBlank() }
            if (displayName != null) {
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            profile.specialization?.let { spec ->
                AssistChip(
                    onClick = {},
                    label = { Text(spec.name) },
                    leadingIcon = {
                        Icon(Icons.Default.Work, null, modifier = Modifier.size(16.dp))
                    }
                )
            }
        }

        HorizontalDivider()

        // О себе
        if (!profile.bio.isNullOrBlank()) {
            ProfileInfoCard {
                ProfileRow(icon = Icons.Default.Notes, label = "О себе", value = profile.bio)
            }
        }

        // Навыки
        if (profile.skills.isNotEmpty()) {
            ProfileInfoCard {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Star,
                            null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Навыки",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        profile.skills.forEach { skill ->
                            SuggestionChip(onClick = {}, label = { Text(skill) })
                        }
                    }
                }
            }
        }

        // Ссылки
        val hasLinks = !profile.githubUrl.isNullOrBlank() || !profile.telegramUrl.isNullOrBlank()
        if (hasLinks) {
            ProfileInfoCard {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (!profile.githubUrl.isNullOrBlank()) {
                        ProfileRow(icon = Icons.Default.Code, label = "GitHub", value = profile.githubUrl)
                    }
                    if (!profile.telegramUrl.isNullOrBlank()) {
                        ProfileRow(
                            icon = Icons.Default.Send,
                            label = "Telegram",
                            value = "@${profile.telegramUrl}"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileInfoCard(content: @Composable ColumnScope.() -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), content = content)
    }
}

@Composable
private fun ProfileRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp).padding(top = 2.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(text = value, style = MaterialTheme.typography.bodyMedium)
        }
    }
}