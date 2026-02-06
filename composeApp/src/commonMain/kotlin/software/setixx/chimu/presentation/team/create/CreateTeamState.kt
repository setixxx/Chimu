package software.setixx.chimu.presentation.team.create

data class CreateTeamState(
    val name: String = "",
    val description: String = "",
    val nameError: String? = null,
    val isCreating: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)