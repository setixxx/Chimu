package software.setixx.chimu.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import software.setixx.chimu.domain.model.TeamMember

@Composable
fun KickMemberDialog(
    member: TeamMember,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.PersonRemove, null) },
        title = { Text("Исключить участника") },
        text = { Text("Вы уверены, что хотите исключить ${member.nickname} из команды?") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Исключить", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}