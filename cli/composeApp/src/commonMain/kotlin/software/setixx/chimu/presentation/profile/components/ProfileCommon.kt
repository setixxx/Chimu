package software.setixx.chimu.presentation.profile.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import chimu.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun ProfileHeader(
    primaryText: String,
    secondaryText: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
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

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = primaryText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = secondaryText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ProfileSkillsView(
    skills: List<String>,
    itemIndex: Int,
    listCount: Int,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val isEmpty = skills.isEmpty()

    SegmentedListItem(
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        selected = false,
        onClick = { if (!isEmpty) expanded = !expanded },
        shapes = ListItemDefaults.segmentedShapes(
            index = itemIndex,
            count = listCount
        ),
        content = { Text(stringResource(Res.string.profile_skills_title)) },
        supportingContent = {
            if (expanded && !isEmpty) {
                Column(modifier = Modifier.padding(top = 8.dp).animateContentSize()) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth().animateContentSize(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        skills.forEach { skill ->
                            AssistChip(onClick = {}, label = { Text(skill) })
                        }
                    }
                }
            } else {
                Text(
                    text = if (isEmpty) stringResource(Res.string.profile_not_specified) else stringResource(Res.string.profile_selected_count_format, skills.size),
                    color = if (isEmpty) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        leadingContent = { Icon(Icons.Default.Star, contentDescription = stringResource(Res.string.profile_skills_title)) },
        trailingContent = {
            if (!isEmpty) {
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null
                )
            }
        },
        modifier = modifier.animateContentSize()
    )
}
