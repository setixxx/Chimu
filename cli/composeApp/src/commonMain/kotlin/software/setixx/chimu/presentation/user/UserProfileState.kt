package software.setixx.chimu.presentation.user

import software.setixx.chimu.domain.model.PublicUserProfile

data class UserProfileState(
    val profile: PublicUserProfile? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)