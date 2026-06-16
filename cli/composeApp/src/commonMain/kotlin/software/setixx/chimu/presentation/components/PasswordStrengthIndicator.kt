package software.setixx.chimu.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import chimu.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import software.setixx.chimu.presentation.utils.PasswordStrength

@Composable
fun PasswordStrengthIndicator(strength: PasswordStrength) {
    val (color, textRes) = when (strength) {
        PasswordStrength.WEAK -> Color(0xFFEF5350) to Res.string.weak_password_label
        PasswordStrength.MEDIUM -> Color(0xFFFFA726) to Res.string.medium_password_label
        PasswordStrength.STRONG -> Color(0xFF66BB6A) to Res.string.strong_password_label
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        LinearProgressIndicator(
            progress = { when (strength) {
                PasswordStrength.WEAK -> 0.33f
                PasswordStrength.MEDIUM -> 0.66f
                PasswordStrength.STRONG -> 1f
            } },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp),
            color = color,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(textRes),
            style = MaterialTheme.typography.bodySmall,
            color = color
        )
    }
}
