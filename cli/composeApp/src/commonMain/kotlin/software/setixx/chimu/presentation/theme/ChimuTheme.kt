package software.setixx.chimu.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkPrimary = Color(0xFF97CBFF)
private val DarkOnPrimary = Color(0xFF003353)
private val DarkPrimaryContainer = Color(0xFF004A76)
private val DarkOnPrimaryContainer = Color(0xFFCEE5FF)
private val DarkInversePrimary = Color(0xFF00639B)
private val DarkSecondary = Color(0xFFB9C8DA)
private val DarkOnSecondary = Color(0xFF243240)
private val DarkSecondaryContainer = Color(0xFF3A4857)
private val DarkOnSecondaryContainer = Color(0xFFD5E4F7)
private val DarkTertiary = Color(0xFFD3BFE6)
private val DarkOnTertiary = Color(0xFF392A49)
private val DarkTertiaryContainer = Color(0xFF504061)
private val DarkOnTertiaryContainer = Color(0xFFEFDBFF)
private val DarkBackground = Color(0xFF131313)
private val DarkOnBackground = Color(0xFFE2E2E2)
private val DarkSurface = Color(0xFF131313)
private val DarkOnSurface = Color(0xFFE2E2E2)
private val DarkSurfaceVariant = Color(0xFF474747)
private val DarkOnSurfaceVariant = Color(0xFFC6C6C6)
private val DarkSurfaceTint = Color(0xFF97CBFF)
private val DarkInverseSurface = Color(0xFFF9F9F9)
private val DarkInverseOnSurface = Color(0xFF1B1B1B)
private val DarkError = Color(0xFFF2B8B5)
private val DarkOnError = Color(0xFF601410)
private val DarkErrorContainer = Color(0xFF8C1D18)
private val DarkOnErrorContainer = Color(0xFFF9DEDC)
private val DarkOutline = Color(0xFF919191)
private val DarkOutlineVariant = Color(0xFF474747)
private val DarkScrim = Color(0xFF000000)

private val LightPrimary = Color(0xFF00639B)
private val LightOnPrimary = Color(0xFFFFFFFF)
private val LightPrimaryContainer = Color(0xFFCEE5FF)
private val LightOnPrimaryContainer = Color(0xFF004A76)
private val LightInversePrimary = Color(0xFF97CBFF)
private val LightSecondary = Color(0xFF51606F)
private val LightOnSecondary = Color(0xFFFFFFFF)
private val LightSecondaryContainer = Color(0xFFD5E4F7)
private val LightOnSecondaryContainer = Color(0xFF3A4857)
private val LightTertiary = Color(0xFF68587A)
private val LightOnTertiary = Color(0xFFFFFFFF)
private val LightTertiaryContainer = Color(0xFFEFDBFF)
private val LightOnTertiaryContainer = Color(0xFF504061)
private val LightBackground = Color(0xFFF9F9F9)
private val LightOnBackground = Color(0xFF1B1B1B)
private val LightSurface = Color(0xFFF9F9F9)
private val LightOnSurface = Color(0xFF1B1B1B)
private val LightSurfaceVariant = Color(0xFFE2E2E2)
private val LightOnSurfaceVariant = Color(0xFF474747)
private val LightSurfaceTint = Color(0xFF00639B)
private val LightInverseSurface = Color(0xFF131313)
private val LightOnInverseSurface = Color(0xFFE2E2E2)
private val LightError = Color(0xFFB3261E)
private val LightOnError = Color(0xFFFFFFFF)
private val LightErrorContainer = Color(0xFFF9DEDC)
private val LightOnErrorContainer = Color(0xFF410E0B)
private val LightOutline = Color(0xFF777777)
private val LightOutlineVariant = Color(0xFFC6C6C6)
private val LightScrim = Color(0xFF000000)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    inversePrimary = DarkInversePrimary,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,
    tertiary = DarkTertiary,
    onTertiary = DarkOnTertiary,
    tertiaryContainer = DarkTertiaryContainer,
    onTertiaryContainer = DarkOnTertiaryContainer,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    surfaceTint = DarkSurfaceTint,
    inverseSurface = DarkInverseSurface,
    inverseOnSurface = DarkInverseOnSurface,
    error = DarkError,
    onError = DarkOnError,
    errorContainer = DarkErrorContainer,
    onErrorContainer = DarkOnErrorContainer,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,
    scrim = DarkScrim,
)

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    inversePrimary = LightInversePrimary,
    secondary = LightSecondary,
    onSecondary = LightOnSecondary,
    secondaryContainer = LightSecondaryContainer,
    onSecondaryContainer = LightOnSecondaryContainer,
    tertiary = LightTertiary,
    onTertiary = LightOnTertiary,
    tertiaryContainer = LightTertiaryContainer,
    onTertiaryContainer = LightOnTertiaryContainer,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    surfaceTint = LightSurfaceTint,
    inverseSurface = LightInverseSurface,
    inverseOnSurface = LightOnInverseSurface,
    error = LightError,
    onError = LightOnError,
    errorContainer = LightErrorContainer,
    onErrorContainer = LightOnErrorContainer,
    outline = LightOutline,
    outlineVariant = LightOutlineVariant,
    scrim = LightScrim,
)

@Composable
expect fun getDynamicColorScheme(darkTheme: Boolean): ColorScheme?

@Composable
fun ChimuTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (dynamicColor) {
        getDynamicColorScheme(darkTheme) ?: if (darkTheme) DarkColorScheme else LightColorScheme
    } else {
        if (darkTheme) DarkColorScheme else LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}