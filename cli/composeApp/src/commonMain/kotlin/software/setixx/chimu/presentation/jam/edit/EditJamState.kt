package software.setixx.chimu.presentation.jam.edit

import software.setixx.chimu.domain.model.GameJam
import software.setixx.chimu.domain.model.GameJamDetails

data class EditJamState(
    val jam: GameJamDetails? = null,
    val name: String = "",
    val description: String = "",
    val theme: String = "",
    val rules: String = "",
    val registrationStart: String = "",
    val registrationEnd: String = "",
    val jamStart: String = "",
    val jamEnd: String = "",
    val judgingStart: String = "",
    val judgingEnd: String = "",
    val minTeamSize: String = "",
    val maxTeamSize: String = "",
    
    val nameError: String? = null,
    val dateError: String? = null,
    val teamSizeError: String? = null,
    
    val isLoading: Boolean = false,
    val isUpdating: Boolean = false,
    val isActionLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)