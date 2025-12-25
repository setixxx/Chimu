package software.setixx.chimu.data.repository

import kotlin.time.Clock
import kotlin.time.Instant
import software.setixx.chimu.data.local.TokenStorage
import software.setixx.chimu.data.remote.GameJamApi
import software.setixx.chimu.domain.model.*
import software.setixx.chimu.domain.repository.GameJamRepository
import kotlin.time.ExperimentalTime

class GameJamRepositoryImpl(
    private val api: GameJamApi,
    private val tokenStorage: TokenStorage
) : GameJamRepository {

    override suspend fun getAllJams(): Result<List<GameJam>> {
        return try {
            val token = tokenStorage.getAccessToken()
                ?: return Result.failure(Exception("Not authenticated"))

            val response = api.getAllJams(token)
            val jams = response.map { dto ->
                GameJam(
                    id = dto.id,
                    name = dto.name,
                    description = dto.description,
                    theme = dto.theme,
                    status = GameJamStatus.valueOf(dto.status),
                    organizerNickname = dto.organizerNickname,
                    registeredTeamsCount = dto.registeredTeamsCount,
                    registrationEnd = dto.registrationEnd,
                    jamEnd = dto.jamEnd,
                    daysRemaining = calculateDaysRemaining(dto.jamEnd, dto.status)
                )
            }
            Result.success(jams)
        } catch (e: Exception) {
            println("Error loading all jams: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun getActiveJams(): Result<List<GameJam>> {
        return try {
            val token = tokenStorage.getAccessToken()
                ?: return Result.failure(Exception("Not authenticated"))

            val statuses = listOf("IN_PROGRESS", "REGISTRATION_OPEN", "JUDGING")
            val allJams = mutableListOf<GameJam>()

            for (status in statuses) {
                try {
                    val response = api.getJamsByStatus(status, token)
                    val jams = response.map { dto ->
                        GameJam(
                            id = dto.id,
                            name = dto.name,
                            description = dto.description,
                            theme = dto.theme,
                            status = GameJamStatus.valueOf(dto.status),
                            organizerNickname = dto.organizerNickname,
                            registeredTeamsCount = dto.registeredTeamsCount,
                            registrationEnd = dto.registrationEnd,
                            jamEnd = dto.jamEnd,
                            daysRemaining = calculateDaysRemaining(dto.jamEnd, dto.status)
                        )
                    }
                    allJams.addAll(jams)
                } catch (e: Exception) {
                    println("⚠️ Error loading jams with status $status: ${e.message}")
                }
            }

            Result.success(allJams)
        } catch (e: Exception) {
            println("Error loading active jams: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun calculateDaysRemaining(endDate: String, status: String): Int? {
        return try {
            if (status in listOf("COMPLETED", "CANCELLED")) return null

            val end = Instant.parse(endDate)
            val now = Clock.System.now()
            val days = (end - now).inWholeDays

            if (days < 0) null else days.toInt()
        } catch (e: Exception) {
            println("Error calculating days remaining: ${e.message}")
            null
        }
    }
}
