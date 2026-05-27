package software.setixx.chimu.presentation.utils

enum class PasswordStrength {
    WEAK, MEDIUM, STRONG
}

object PasswordUtils {
    fun calculatePasswordStrength(password: String): PasswordStrength {
        if (password.length < 8) return PasswordStrength.WEAK

        var strength = 0
        if (password.any { it.isUpperCase() }) strength++
        if (password.any { it.isLowerCase() }) strength++
        if (password.any { it.isDigit() }) strength++
        if (password.any { !it.isLetterOrDigit() }) strength++
        if (password.length >= 12) strength++

        return when {
            strength <= 2 -> PasswordStrength.WEAK
            strength <= 4 -> PasswordStrength.MEDIUM
            else -> PasswordStrength.STRONG
        }
    }
}
