package software.setixx.chimu.presentation.team.create

/**
 * Состояние экрана создания команды.
 * Хранит временные данные полей ввода и статус процесса создания.
 */
data class CreateTeamState(
    val name: String = "",
    val description: String = "",
    val nameError: String? = null,
    val isCreating: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val createdTeamId: String? = null
)