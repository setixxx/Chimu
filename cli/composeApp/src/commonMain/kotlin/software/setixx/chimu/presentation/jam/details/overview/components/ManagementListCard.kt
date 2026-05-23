package software.setixx.chimu.presentation.jam.details.management.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun <T> ManagementListCard(
    title: String,
    titleIcon: ImageVector? = null,
    items: List<T>,
    emptyText: String,
    buttonText: String? = null,
    buttonIcon: ImageVector? = null,
    onButtonClick: () -> Unit,
    modifier: Modifier = Modifier,
    itemHeadline: @Composable (T) -> Unit,
    itemSupportingContent: @Composable ((T) -> Unit)? = null,
    itemTrailingContent: @Composable ((T) -> Unit)? = null,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .padding(end = 8.dp, top = 4.dp, bottom = 4.dp, start = 8.dp)
                ) {
                    if (titleIcon != null){
                        Icon(
                            titleIcon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = "$title (${items.size})",
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
                if (buttonIcon != null && buttonText != null){
                    FilledTonalButton(
                        onClick = onButtonClick,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = buttonIcon,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(buttonText)
                    }
                }
            }

            Column(
                modifier = Modifier.padding(top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
            ) {
                if (items.isEmpty()) {
                    Text(emptyText, style = MaterialTheme.typography.bodyMedium)
                } else {
                    items.forEachIndexed { index, item ->
                        SegmentedListItem(
                            colors = ListItemDefaults.colors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                            ),
                            selected = false,
                            onClick = {},
                            shapes = if (items.size == 1) ListItemDefaults.shapes(shape = MaterialTheme.shapes.medium)
                            else ListItemDefaults.segmentedShapes(index = index, items.size),
                            content = { itemHeadline(item) },
                            supportingContent = itemSupportingContent?.let { { it(item) } },
                            trailingContent = itemTrailingContent?.let { { it(item) } }
                        )
                    }
                }
            }
        }
    }
}