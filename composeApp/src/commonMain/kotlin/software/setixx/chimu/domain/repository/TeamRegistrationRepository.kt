package software.setixx.chimu.domain.repository

import software.setixx.chimu.domain.model.ApiResult
import software.setixx.chimu.domain.model.RegisterTeam
import software.setixx.chimu.domain.model.Registration
import software.setixx.chimu.domain.model.UpdateRegistrationStatus

interface TeamRegistrationRepository {
    suspend fun getJamRegistrations(jamId: String): ApiResult<List<Registration>>
    suspend fun registerTeam(teamId: RegisterTeam): ApiResult<Registration>
    suspend fun withdrawTeam(jamId: String, teamId: String): ApiResult<Unit>
    suspend fun updateRegistrationStatus(jamId: String, teamId: String, status: UpdateRegistrationStatus): ApiResult<Unit>
}