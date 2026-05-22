package software.setixx.chimu.presentation.profile.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun EditableProfileField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: ImageVector,
    isEditing: Boolean,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isError: Boolean = false,
    supportingText: String? = null,
    placeholder: String? = null,
    prefix: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    minLines: Int = 1,
    maxLines: Int = 5,
    readOnly: Boolean = false,
    itemIndex: Int = 0,
    listCount: Int = 1
) {
    if (isEditing) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            enabled = enabled,
            readOnly = readOnly,
            modifier = modifier.fillMaxWidth(),
            isError = isError,
            supportingText = supportingText?.let { { Text(it) } },
            leadingIcon = { Icon(leadingIcon, contentDescription = label) },
            trailingIcon = trailingIcon,
            placeholder = placeholder?.let { { Text(it) } },
            prefix = prefix,
            minLines = minLines,
            maxLines = maxLines,
            shape = MaterialTheme.shapes.largeIncreased
        )
    } else {
        SegmentedListItem(
            colors = ListItemDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            selected = false,
            onClick = {},
            shapes = ListItemDefaults.segmentedShapes(
                index = itemIndex,
                count = listCount
            ),
            content = { Text(label) },
            supportingContent = {
                Text(
                    text = value.ifBlank { "Не указано" },
                    color = if (value.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            leadingContent = { Icon(leadingIcon, contentDescription = label) },
            trailingContent = trailingIcon
        )
    }
}