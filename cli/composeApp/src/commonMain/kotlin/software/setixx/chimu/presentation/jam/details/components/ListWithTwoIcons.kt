package software.setixx.chimu.presentation.jam.details.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun ListWithTwoIcons(
    icon: ImageVector? = null,
    contentDescription: String,
    header: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    trailingIcon: ImageVector? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        icon?.let {
            Icon(
                modifier = Modifier
                    .padding(end = 16.dp)
                    .size(24.dp),
                imageVector = icon,
                contentDescription = contentDescription
            )
        }

        Text(
            text = header,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier
                .weight(1f)
                .padding(end = 16.dp)
                .padding(vertical = 8.dp)
        )

        trailingIcon?.let {
            Icon(
                modifier = Modifier
                    .size(24.dp),
                imageVector = it,
                contentDescription = null
            )
        }
    }
}