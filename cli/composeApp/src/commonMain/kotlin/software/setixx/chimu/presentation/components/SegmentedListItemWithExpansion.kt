package software.setixx.chimu.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp

/**
 * Expandable list item with animated chevron.
 *
 * @param headline    Primary text shown in the header.
 * @param supporting  Secondary (sub)text shown below the headline.
 * @param leadingContent Optional composable placed at the start of the header.
 * @param trailingContent Optional composable placed before the chevron (e.g. badge, button).
 * @param initiallyExpanded Whether the item starts in expanded state.
 * @param expandedContent Content rendered inside the expanded section.
 */
@Composable
fun SegmentedListItemWithExpansion(
    headline: String,
    supporting: String? = null,
    leadingContent: @Composable (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null,
    initiallyExpanded: Boolean = false,
    expandedContent: @Composable ColumnScope.() -> Unit
) {
    var expanded by remember { mutableStateOf(initiallyExpanded) }
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "chevron_rotation"
    )

    Column {
        ListItem(
            headlineContent = { Text(headline, style = MaterialTheme.typography.titleSmall) },
            supportingContent = supporting?.let {
                { Text(it, style = MaterialTheme.typography.bodySmall) }
            },
            leadingContent = leadingContent,
            trailingContent = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    trailingContent?.invoke()
                    Icon(
                        Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "Свернуть" else "Развернуть",
                        modifier = Modifier
                            .size(24.dp)
                            .rotate(rotation),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            modifier = Modifier.clickable { expanded = !expanded }
        )

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
                content = expandedContent
            )
        }
    }
}