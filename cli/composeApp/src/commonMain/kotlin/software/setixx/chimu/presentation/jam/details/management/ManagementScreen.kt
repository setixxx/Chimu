package software.setixx.chimu.presentation.jam.details.management

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Rule
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import org.koin.compose.viewmodel.koinViewModel
import software.setixx.chimu.api.domain.GameJamStatus
import software.setixx.chimu.api.domain.RegistrationStatus
import software.setixx.chimu.data.picker.rememberFilePicker
import software.setixx.chimu.domain.model.CreateRatingCriteria
import software.setixx.chimu.domain.model.GameJamDetails
import software.setixx.chimu.domain.model.JamStatistics
import software.setixx.chimu.domain.model.Leaderboard
import software.setixx.chimu.domain.model.Project
import software.setixx.chimu.domain.model.Registration
import software.setixx.chimu.presentation.components.SegmentedListItemWithExpansion
import software.setixx.chimu.presentation.components.localizeStatus
import software.setixx.chimu.presentation.jam.details.components.ListWithTwoIcons
import software.setixx.chimu.presentation.jam.details.management.components.BannerCard
import software.setixx.chimu.presentation.jam.details.management.components.JamStatisticsCard
import software.setixx.chimu.presentation.jam.details.management.components.LeaderboardCard
import software.setixx.chimu.presentation.jam.details.management.components.ManagementListCard
import software.setixx.chimu.presentation.jam.details.management.components.TeamsWithProjectsSection
import software.setixx.chimu.presentation.main.components.JamBanner
import software.setixx.chimu.presentation.main.components.StatusChip
import software.setixx.chimu.presentation.utils.DateTimeUtils

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ManagementScreen(
    jam: GameJamDetails,
    viewModel: ManagementViewModel = koinViewModel(),
    paddingValues: PaddingValues
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()

    var showAssignJudgeDialog by remember { mutableStateOf(false) }
    var showAddCriteriaDialog by remember { mutableStateOf(false) }

    var judgeUserIdInput by remember { mutableStateOf("") }
    var criteriaName by remember { mutableStateOf("") }
    var criteriaDesc by remember { mutableStateOf("") }
    var criteriaMaxScore by remember { mutableStateOf("10") }
    var criteriaWeight by remember { mutableStateOf("1.0") }

    var isJudgesExpanded by remember { mutableStateOf(true) }
    
    val currentJam = state.jam ?: jam
    val isDraft = state.jam?.status == GameJamStatus.DRAFT && !state.isPublished

    val bannerUrl = currentJam.bannerUrl
    val bannerPicker = rememberFilePicker { fileUpload ->
        fileUpload?.let {
            currentJam.id.let { id -> viewModel.uploadBanner(id, it) }
        }
    }

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

                    state.leaderboard?.let { lb ->
                        if (lb.rankings.isNotEmpty()) {
                            LeaderboardCard(leaderboard = lb)
                        }
                    }

/*                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.extraLarge,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.onSecondary
                        ),
                        onClick = { isJudgesExpanded = !isJudgesExpanded }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .padding(end = 8.dp, top = 4.dp, bottom = 4.dp, start = 8.dp)
                                ) {
                                    Text(
                                        text = "Судьи (${state.judges.size})",
                                    )
                                    Icon(
                                        imageVector = if (isJudgesExpanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                                        contentDescription = "Развернуть список судей",
                                        modifier = Modifier.padding(start = 4.dp)
                                    )
                                }

                                FilledTonalButton(
                                    onClick = { showAssignJudgeDialog = true },
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PersonAdd,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Назначить")
                                }
                            }

                            AnimatedVisibility(
                                visible = isJudgesExpanded,
                                enter = expandVertically() + fadeIn(),
                                exit = shrinkVertically() + fadeOut()
                            ) {
                                Column(
                                    modifier = Modifier.padding(top = 16.dp),
                                    verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
                                ) {
                                    if (state.judges.isEmpty()) {
                                        Text(
                                            text = "Судьи не назначены.",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(vertical = 8.dp)
                                        )
                                    } else {
                                        val judges = state.judges
                                        judges.forEachIndexed { index, judge ->
                                            SegmentedListItem(
                                                colors = ListItemDefaults.colors(
                                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                                ),
                                                selected = false,
                                                onClick = {},
                                                shapes = ListItemDefaults.segmentedShapes(
                                                    index = index,
                                                    count = judges.size
                                                ),
                                                content = { Text(judge.nickname) },
                                                supportingContent = {
                                                    Text("Назначен: ${DateTimeUtils.formatDateTime(judge.assignedAt)}")
                                                },
                                                trailingContent = {
                                                    IconButton(
                                                        onClick = { viewModel.unassignJudge(jam.id, judge.userId) }
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.PersonRemove,
                                                            contentDescription = "Снять",
                                                            tint = MaterialTheme.colorScheme.error
                                                        )
                                                    }
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }*/

                    ManagementListCard(
                        title = "Судьи",
                        titleIcon = Icons.Default.Gavel,
                        items = state.judges,
                        emptyText = "Судьи не назначены.",
                        buttonText = "Назначить",
                        buttonIcon = Icons.Default.PersonAdd,
                        onButtonClick = { showAssignJudgeDialog = true },
                        itemHeadline = { judge -> Text(judge.nickname) },
                        itemSupportingContent = { judge ->
                            Text("Назначен: ${DateTimeUtils.formatDateTime(judge.assignedAt)}")
                        },
                        itemTrailingContent = { judge ->
                            IconButton(
                                onClick = { viewModel.unassignJudge(jam.id, judge.userId) }
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
                        title = "Критерии\nоценивания",
                        titleIcon = Icons.AutoMirrored.Filled.Rule,
                        items = state.criteria.sortedBy { it.orderIndex },
                        emptyText = "Критерии не добавлены.",
                        buttonText = "Добавить",
                        buttonIcon = Icons.Default.Add,
                        onButtonClick = { showAddCriteriaDialog = true },
                        itemHeadline = { criteria -> Text(criteria.name) },
                        itemSupportingContent = { criteria ->
                            Text("Макс: ${criteria.maxScore} • Вес: ${criteria.weight}")
                        },
                        itemTrailingContent = { criteria ->
                            IconButton(
                                onClick = { viewModel.deleteCriteria(jam.id, criteria.id) }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Удалить",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    )



                    TeamsWithProjectsSection(
                        registrations = state.registrations,
                        projectsByTeam = state.projectsByTeam,
                        isActionLoading = state.isActionLoading,
                        onApprove = { teamId ->
                            viewModel.updateRegistrationStatus(jam.id, teamId, RegistrationStatus.APPROVED)
                        },
                        onReject = { teamId ->
                            viewModel.updateRegistrationStatus(jam.id, teamId, RegistrationStatus.REJECTED)
                        },
                        onDisqualify = { teamId ->
                            viewModel.updateRegistrationStatus(jam.id, teamId, RegistrationStatus.DISQUALIFIED)
                        }
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
        AlertDialog(
            onDismissRequest = { showAssignJudgeDialog = false },
            title = { Text("Назначить судью") },
            text = {
                OutlinedTextField(
                    value = judgeUserIdInput,
                    onValueChange = { judgeUserIdInput = it },
                    label = { Text("ID пользователя") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.assignJudge(jam.id, judgeUserIdInput)
                    judgeUserIdInput = ""
                    showAssignJudgeDialog = false
                }) { Text("Назначить") }
            },
            dismissButton = {
                TextButton(onClick = { showAssignJudgeDialog = false }) { Text("Отмена") }
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
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = criteriaDesc,
                        onValueChange = { criteriaDesc = it },
                        label = { Text("Описание") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = criteriaMaxScore,
                        onValueChange = { criteriaMaxScore = it },
                        label = { Text("Макс. балл") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = criteriaWeight,
                        onValueChange = { criteriaWeight = it },
                        label = { Text("Вес") },
                        modifier = Modifier.fillMaxWidth()
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
}
