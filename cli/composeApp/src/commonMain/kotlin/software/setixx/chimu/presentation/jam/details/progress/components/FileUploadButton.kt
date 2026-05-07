package software.setixx.chimu.presentation.jam.details.progress.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Badge
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun FileUploadButton(
    icon: ImageVector,
    label: String,
    currentCount: Int,
    maxCount: Int,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center) {
            FilledIconButton(
                onClick = onClick,
                enabled = enabled && currentCount < maxCount,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(icon, contentDescription = null)
            }
            if (currentCount > 0) {
                Badge(modifier = Modifier.align(Alignment.TopEnd)) {
                    Text(currentCount.toString())
                }
            }
        }
        Text(label, style = MaterialTheme.typography.labelSmall)
        Text(
            "$currentCount/$maxCount",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline
        )
    }
}