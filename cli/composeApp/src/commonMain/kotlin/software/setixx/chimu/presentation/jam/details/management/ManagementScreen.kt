package software.setixx.chimu.presentation.jam.details.management

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Rule
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import software.setixx.chimu.api.domain.GameJamStatus
import software.setixx.chimu.api.domain.RegistrationStatus
import software.setixx.chimu.data.picker.rememberFilePicker
import software.setixx.chimu.domain.model.CreateRatingCriteria
import software.setixx.chimu.domain.model.GameJamDetails
import software.setixx.chimu.domain.model.Judge
import software.setixx.chimu.domain.model.RatingCriteria
import software.setixx.chimu.domain.model.UpdateRatingCriteria
import software.setixx.chimu.presentation.jam.details.management.components.BannerCard
import software.setixx.chimu.presentation.jam.details.management.components.JamStatisticsCard
import software.setixx.chimu.presentation.jam.details.management.components.ManagementListCard
import software.setixx.chimu.presentation.jam.details.management.components.TeamCard
import software.setixx.chimu.presentation.jam.details.transfer.DialogActions
import software.setixx.chimu.presentation.jam.details.transfer.DialogIcon
import software.setixx.chimu.presentation.jam.details.transfer.DialogTitle
import software.setixx.chimu.presentation.jam.details.transfer.RecipientCard
import software.setixx.chimu.presentation.utils.DateTimeUtils

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ManagementScreen(
    jam: GameJamDetails,
    viewModel: ManagementViewModel = koinViewModel(),
    paddingValues: PaddingValues,
    onNavigateToAlienProfile: (String) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()

    var showUnassignJudgeDialog by remember { mutableStateOf(false) }
    var selectedJudge by remember { mutableStateOf<Judge?>(null) }
    var showDeleteCriteriaDialog by remember { mutableStateOf(false) }
    var selectedCriteria by remember { mutableStateOf<RatingCriteria?>(null) }
    var showAssignJudgeDialog by remember { mutableStateOf(false) }
    var showAddCriteriaDialog by remember { mutableStateOf(false) }
    var showUpdateCriteriaDialog by remember { mutableStateOf(false) }
    var selectedCriteriaId by remember { mutableStateOf<String?>(null) }

    var criteriaName by remember { mutableStateOf("") }
    var criteriaDesc by remember { mutableStateOf("") }
    var criteriaMaxScore by remember { mutableStateOf("10") }
    var criteriaWeight by remember { mutableStateOf("1.0") }
    
    val currentJam = state.jam ?: jam
    val isDraft = state.jam?.status == GameJamStatus.DRAFT && !state.isPublished

    val bannerUrl = currentJam.bannerUrl
    val bannerPicker = rememberFilePicker { fileUpload ->
        fileUpload?.let {
            currentJam.id.let { id -> viewModel.uploadBanner(id, it) }
        }
    }

    val editableStates = setOf(
        GameJamStatus.DRAFT,
        GameJamStatus.ANNOUNCED,
        GameJamStatus.REGISTRATION_OPEN,
        GameJamStatus.REGISTRATION_CLOSED
    )

    LaunchedEffect(jam.id) {
        viewModel.load(jam.id)
    }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    LaunchedEffect(state.successMessage) {
        state.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSuccess()
        }
    }

    if (state.isLoading){
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ){
            LoadingIndicator()
        }
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    BannerCard(
                        bannerUrl = bannerUrl,
                        onOpenBannerPicker = bannerPicker,
                        onDeleteBanner = { state.jam?.id?.let { viewModel.deleteBanner(it) } }
                    )

                    state.statistics?.let { stats ->
                        JamStatisticsCard(statistics = stats)
                    }

                    ManagementListCard(
                        title = "Судьи",
                        titleIcon = Icons.Default.Gavel,
                        items = state.judges,
                        emptyText = "Судьи не назначены.",
                        buttonText = if (jam.status in editableStates) "Назначить" else null,
                        buttonIcon = if (jam.status in editableStates) Icons.Default.PersonAdd else null,
                        onButtonClick = { showAssignJudgeDialog = true },
                        itemHeadline = { judge -> Text(judge.nickname) },
                        itemSupportingContent = { judge ->
                            Text("Назначен: ${DateTimeUtils.formatDateTime(judge.assignedAt)}")
                        },
                        onItemClick = { judge ->
                            onNavigateToAlienProfile(judge.userId)
                        },
                        itemTrailingContent = { judge ->
                            IconButton(
                                onClick = {
                                    selectedJudge = judge
                                    showUnassignJudgeDialog = true
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PersonRemove,
                                    contentDescription = "Снять",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    )

                    ManagementListCard(
                        title = "Критерии оценивания",
                        titleIcon = Icons.AutoMirrored.Filled.Rule,
                        items = state.criteria.sortedBy { it.orderIndex },
                        emptyText = "Критерии не добавлены.",
                        buttonText = if (jam.status in editableStates) "Добавить" else null,
                        buttonIcon = if (jam.status in editableStates) Icons.Default.Add else null,
                        onButtonClick = { showAddCriteriaDialog = true },
                        onItemClick = { criteria ->
                            selectedCriteriaId = criteria.id
                            criteriaName = criteria.name
                            criteriaDesc = criteria.description ?: ""
                            criteriaMaxScore = criteria.maxScore.toString()
                            criteriaWeight = criteria.weight
                            showUpdateCriteriaDialog = true
                        },
                        itemHeadline = { criteria -> Text(criteria.name) },
                        itemSupportingContent = { criteria ->
                            Text("Макс: ${criteria.maxScore} • Вес: ${criteria.weight}")
                        },
                        itemTrailingContent = { criteria ->
                            if (jam.status in editableStates){
                                IconButton(
                                    onClick = {
                                        selectedCriteria = criteria
                                        showDeleteCriteriaDialog = true
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Удалить",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    )

                    TeamCard(
                        registrations = state.registrations,
                        onApprove = { teamId ->
                            viewModel.updateRegistrationStatus(jam.id, teamId, RegistrationStatus.APPROVED)
                        },
                        onReject = { teamId ->
                            viewModel.updateRegistrationStatus(jam.id, teamId, RegistrationStatus.REJECTED)
                        },
                        onDisqualify = { teamId ->
                            viewModel.updateRegistrationStatus(jam.id, teamId, RegistrationStatus.DISQUALIFIED)
                        },
                        isActionsVisible = true
                    )

                    if (isDraft) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = { viewModel.publishJam(jam.id) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                enabled = !state.isLoading
                            ) {
                                if (state.isActionLoading) {
                                    LoadingIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                } else {
                                    Text("Опубликовать джем")
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .padding(paddingValues)
                    .align(Alignment.BottomCenter)
            )
        }
    }


    if (showAssignJudgeDialog) {
        AssignJudgeDialog(
            state = state,
            onDismiss = { showAssignJudgeDialog = false },
            onQueryChange = { viewModel.onJudgeSearchQueryChange(it) },
            onSearch = { viewModel.searchJudge() },
            onConfirm = {
                viewModel.assignJudge(jam.id)
                showAssignJudgeDialog = false
            }
        )
    }

    if (showUnassignJudgeDialog) {
        AlertDialog(
            onDismissRequest = { showUnassignJudgeDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.PersonRemove,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("Снять судью с судейства?") },
            text = { Text("Cудья ${selectedJudge?.nickname} будет отстранен от судейства.") },
            confirmButton = {
                Button(
                    onClick = {
                        selectedJudge?.let { viewModel.unassignJudge(jam.id, it.userId) }
                        showUnassignJudgeDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) { Text("Снять") }
            },
            dismissButton = {
                TextButton(onClick = { showUnassignJudgeDialog = false }) { Text("Отмена") }
            }
        )
    }

    if (showDeleteCriteriaDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteCriteriaDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("Удалить критерий оценивания?") },
            text = { Text("Критерий «${selectedCriteria?.name}» будет безвозратно удален.") },
            confirmButton = {
                Button(
                    onClick = {
                        selectedCriteria?.let { viewModel.deleteCriteria(jam.id, it.id)}
                        showDeleteCriteriaDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) { Text("Удалить") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteCriteriaDialog = false }) { Text("Отмена") }
            }
        )
    }


    if (showAddCriteriaDialog) {
        AlertDialog(
            onDismissRequest = { showAddCriteriaDialog = false },
            title = { Text("Новый критерий") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = criteriaName,
                        onValueChange = { criteriaName = it },
                        label = { Text("Название") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.largeIncreased
                    )
                    OutlinedTextField(
                        value = criteriaDesc,
                        onValueChange = { criteriaDesc = it },
                        label = { Text("Описание") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.largeIncreased
                    )
                    OutlinedTextField(
                        value = criteriaMaxScore,
                        onValueChange = { criteriaMaxScore = it },
                        label = { Text("Макс. балл") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.largeIncreased
                    )
                    OutlinedTextField(
                        value = criteriaWeight,
                        onValueChange = { criteriaWeight = it },
                        label = { Text("Вес") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.largeIncreased
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.createCriteria(
                        jam.id,
                        CreateRatingCriteria(
                            name = criteriaName,
                            description = criteriaDesc.takeIf { it.isNotBlank() },
                            maxScore = criteriaMaxScore.toIntOrNull() ?: 10,
                            weight = criteriaWeight.toDoubleOrNull() ?: 1.0,
                            orderIndex = state.criteria.size
                        )
                    )
                    criteriaName = ""
                    criteriaDesc = ""
                    criteriaMaxScore = "10"
                    criteriaWeight = "1.0"
                    showAddCriteriaDialog = false
                }) { Text("Добавить") }
            },
            dismissButton = {
                TextButton(onClick = { showAddCriteriaDialog = false }) { Text("Отмена") }
            }
        )
    }

    if (showUpdateCriteriaDialog) {
        AlertDialog(
            onDismissRequest = {
                showUpdateCriteriaDialog = false
                selectedCriteriaId = null
            },
            title = { Text("Редактировать критерий") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = criteriaName,
                        onValueChange = { criteriaName = it },
                        label = { Text("Название") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.largeIncreased
                    )
                    OutlinedTextField(
                        value = criteriaDesc,
                        onValueChange = { criteriaDesc = it },
                        label = { Text("Описание") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.largeIncreased
                    )
                    OutlinedTextField(
                        value = criteriaMaxScore,
                        onValueChange = { criteriaMaxScore = it },
                        label = { Text("Макс. балл") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.largeIncreased
                    )
                    OutlinedTextField(
                        value = criteriaWeight,
                        onValueChange = { criteriaWeight = it },
                        label = { Text("Вес") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.largeIncreased
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    selectedCriteriaId?.let { id ->
                        viewModel.updateCriteria(
                            jam.id,
                            id,
                            UpdateRatingCriteria(
                                name = criteriaName,
                                description = criteriaDesc.takeIf { it.isNotBlank() },
                                maxScore = criteriaMaxScore.toIntOrNull() ?: 10,
                                weight = criteriaWeight.toDoubleOrNull() ?: 1.0,
                                orderIndex = null
                            )
                        )
                    }
                    showUpdateCriteriaDialog = false
                    selectedCriteriaId = null
                    criteriaName = ""
                    criteriaDesc = ""
                    criteriaMaxScore = "10"
                    criteriaWeight = "1.0"
                }) { Text("Сохранить") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showUpdateCriteriaDialog = false
                    selectedCriteriaId = null
                }) { Text("Отмена") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun AssignJudgeDialog(
    state: ManagementState,
    onDismiss: () -> Unit,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onConfirm: () -> Unit
) {
    BasicAlertDialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp).animateContentSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                DialogIcon(Icons.Default.PersonAdd, MaterialTheme.colorScheme.secondary)
                Spacer(Modifier.height(12.dp))
                DialogTitle("Назначить судью")
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Введите никнейм пользователя, которого хотите назначить судьей.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(20.dp))

                OutlinedTextField(
                    value = state.judgeSearchQuery,
                    onValueChange = onQueryChange,
                    label = { Text("Никнейм пользователя") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        if (state.isSearchingJudge) {
                            LoadingIndicator(modifier = Modifier.size(24.dp))
                        } else {
                            IconButton(
                                onClick = onSearch,
                                enabled = state.judgeSearchQuery.isNotBlank()
                            ) {
                                Icon(Icons.Default.Search, "Найти")
                            }
                        }
                    },
                    isError = state.judgeSearchError != null,
                    supportingText = state.judgeSearchError?.let { err -> { Text(err) } },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { onSearch() }),
                    shape = MaterialTheme.shapes.largeIncreased
                )

                state.foundJudge?.let { user ->
                    Spacer(Modifier.height(12.dp))
                    RecipientCard(user)
                }

                Spacer(Modifier.height(24.dp))
                DialogActions(
                    onDismiss = onDismiss,
                    confirmText = "Назначить",
                    confirmEnabled = state.foundJudge != null && !state.isActionLoading,
                    isLoading = state.isActionLoading,
                    onConfirm = onConfirm
                )
            }
        }
    }
}
