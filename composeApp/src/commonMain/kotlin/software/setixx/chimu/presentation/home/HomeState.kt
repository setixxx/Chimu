package software.setixx.chimu.presentation.home

import software.setixx.chimu.domain.model.User

data class HomeState(
    val user: User? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)