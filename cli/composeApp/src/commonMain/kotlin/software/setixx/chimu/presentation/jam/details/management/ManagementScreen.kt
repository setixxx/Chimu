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
import chimu.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
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
import software.setixx.chimu.presentation.jam.details.overview.components.ManagementListCard
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
    onNavigateToAlienProfile: (String) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val state by viewModel.state.collectAsState()
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
                        onDeleteBanner = { state.jam?.id?.let { viewModel.deleteBanner(it) } },
                        isEditable = jam.status in editableStates,
                        isBannerUploading = state.isBannerUploading,
                        isBannerDeleting = state.isBannerDeleting
                    )

                    state.statistics?.let { stats ->
                        JamStatisticsCard(statistics = stats)
                    }

                    ManagementListCard(
                        title = stringResource(Res.string.management_judges_title),
                        titleIcon = Icons.Default.Gavel,
                        items = state.judges,
                        emptyText = stringResource(Res.string.management_judges_empty),
                        buttonText = if (jam.status in editableStates) stringResource(Res.string.management_assign_judge_button) else null,
                        buttonIcon = if (jam.status in editableStates) Icons.Default.PersonAdd else null,
                        onButtonClick = { showAssignJudgeDialog = true },
                        itemHeadline = { judge -> Text(judge.nickname) },
                        itemSupportingContent = { judge ->
                            Text(stringResource(Res.string.jam_details_judge_assigned_at, DateTimeUtils.formatDateTime(judge.assignedAt)))
                        },
                        onItemClick = { judge ->
                            onNavigateToAlienProfile(judge.userId)
                        },
                        itemTrailingContent = { judge ->
                            if (jam.status in editableStates){
                                IconButton(
                                    onClick = {
                                        selectedJudge = judge
                                        showUnassignJudgeDialog = true
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PersonRemove,
                                        contentDescription = stringResource(Res.string.management_unassign_judge_desc),
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    )

                    ManagementListCard(
                        title = stringResource(Res.string.jam_details_criteria_title),
                        titleIcon = Icons.AutoMirrored.Filled.Rule,
                        items = state.criteria.sortedBy { it.orderIndex },
                        emptyText = stringResource(Res.string.jam_details_criteria_empty),
                        buttonText = if (jam.status in editableStates) stringResource(Res.string.management_add_button) else null,
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
                            Text(
                                stringResource(
                                    Res.string.max_score_and_weight_format,
                                    stringResource(Res.string.judging_max_score, criteria.maxScore),
                                    stringResource(Res.string.judging_weight, criteria.weight)
                                )
                            )
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
                                        contentDescription = stringResource(Res.string.delete),
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
                        isActionsVisible = jam.status in editableStates
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
                                    Text(stringResource(Res.string.management_publish_jam_button))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
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
            title = { Text(stringResource(Res.string.management_unassign_judge_title)) },
            text = { Text(stringResource(Res.string.management_unassign_judge_message, selectedJudge?.nickname ?: "")) },
            confirmButton = {
                Button(
                    onClick = {
                        selectedJudge?.let { viewModel.unassignJudge(jam.id, it.userId) }
                        showUnassignJudgeDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) { Text(stringResource(Res.string.management_unassign_judge_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { showUnassignJudgeDialog = false }) { Text(stringResource(Res.string.cancel)) }
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
            title = { Text(stringResource(Res.string.management_delete_criteria_title)) },
            text = { Text(stringResource(Res.string.management_delete_criteria_message, selectedCriteria?.name ?: "")) },
            confirmButton = {
                Button(
                    onClick = {
                        selectedCriteria?.let { viewModel.deleteCriteria(jam.id, it.id)}
                        showDeleteCriteriaDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) { Text(stringResource(Res.string.delete)) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteCriteriaDialog = false }) { Text(stringResource(Res.string.cancel)) }
            }
        )
    }


    if (showAddCriteriaDialog) {
        AlertDialog(
            onDismissRequest = { showAddCriteriaDialog = false },
            title = { Text(stringResource(Res.string.management_new_criteria_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = criteriaName,
                        onValueChange = { criteriaName = it },
                        label = { Text(stringResource(Res.string.name)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.largeIncreased
                    )
                    OutlinedTextField(
                        value = criteriaDesc,
                        onValueChange = { criteriaDesc = it },
                        label = { Text(stringResource(Res.string.description)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.largeIncreased
                    )
                    OutlinedTextField(
                        value = criteriaMaxScore,
                        onValueChange = { criteriaMaxScore = it },
                        label = { Text(stringResource(Res.string.management_max_score_label)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.largeIncreased
                    )
                    OutlinedTextField(
                        value = criteriaWeight,
                        onValueChange = { criteriaWeight = it },
                        label = { Text(stringResource(Res.string.management_weight_label)) },
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
                }) { Text(stringResource(Res.string.management_add_button)) }
            },
            dismissButton = {
                TextButton(onClick = { showAddCriteriaDialog = false }) { Text(stringResource(Res.string.cancel)) }
            }
        )
    }

    if (showUpdateCriteriaDialog) {
        AlertDialog(
            onDismissRequest = {
                showUpdateCriteriaDialog = false
                selectedCriteriaId = null
            },
            title = { Text(stringResource(Res.string.management_edit_criteria_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = criteriaName,
                        onValueChange = { criteriaName = it },
                        label = { Text(stringResource(Res.string.name)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.largeIncreased
                    )
                    OutlinedTextField(
                        value = criteriaDesc,
                        onValueChange = { criteriaDesc = it },
                        label = { Text(stringResource(Res.string.description)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.largeIncreased
                    )
                    OutlinedTextField(
                        value = criteriaMaxScore,
                        onValueChange = { criteriaMaxScore = it },
                        label = { Text(stringResource(Res.string.management_max_score_label)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.largeIncreased
                    )
                    OutlinedTextField(
                        value = criteriaWeight,
                        onValueChange = { criteriaWeight = it },
                        label = { Text(stringResource(Res.string.management_weight_label)) },
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
                }) { Text(stringResource(Res.string.save)) }
            },
            dismissButton = {
                TextButton(onClick = {
                    showUpdateCriteriaDialog = false
                    selectedCriteriaId = null
                }) { Text(stringResource(Res.string.cancel)) }
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
                DialogTitle(stringResource(Res.string.management_assign_judge_title))
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(Res.string.management_assign_judge_message),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(20.dp))

                OutlinedTextField(
                    value = state.judgeSearchQuery,
                    onValueChange = onQueryChange,
                    label = { Text(stringResource(Res.string.management_nickname_label)) },
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
                                Icon(Icons.Default.Search, stringResource(Res.string.management_search_desc))
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
                    confirmText = stringResource(Res.string.management_assign_judge_button),
                    confirmEnabled = state.foundJudge != null && !state.isActionLoading,
                    isLoading = state.isActionLoading,
                    onConfirm = onConfirm
                )
            }
        }
    }
}
