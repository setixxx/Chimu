package software.setixx.chimu.api.dto

import jakarta.validation.constraints.*
import software.setixx.chimu.api.domain.GameJamStatus
import java.time.Instant

data class CreateGameJamRequest(
    @field:NotBlank(message = "Game jam name is required")
    @field:Size(min = 3, max = 200, message = "Name must be between 3 and 200 characters")
    val name: String,

    @field:Size(max = 5000, message = "Description must not exceed 5000 characters")
    val description: String? = null,

    @field:Size(max = 200, message = "Theme must not exceed 200 characters")
    val theme: String? = null,

    @field:Size(max = 5000, message = "Rules must not exceed 5000 characters")
    val rules: String? = null,

    @field:NotNull(message = "Registration start date is required")
    val registrationStart: Instant,

    @field:NotNull(message = "Registration end date is required")
    val registrationEnd: Instant,

    @field:NotNull(message = "Jam start date is required")
    val jamStart: Instant,

    @field:NotNull(message = "Jam end date is required")
    val jamEnd: Instant,

    @field:NotNull(message = "Judging start date is required")
    val judgingStart: Instant,

    @field:NotNull(message = "Judging end date is required")
    val judgingEnd: Instant,

    @field:Min(value = 1, message = "Minimum team size must be at least 1")
    @field:Max(value = 100, message = "Minimum team size must not exceed 100")
    val minTeamSize: Int = 1,

    @field:Min(value = 1, message = "Maximum team size must be at least 1")
    @field:Max(value = 100, message = "Maximum team size must not exceed 100")
    val maxTeamSize: Int = 10
)

data class UpdateGameJamRequest(
    @field:Size(min = 3, max = 200, message = "Name must be between 3 and 200 characters")
    val name: String? = null,

    @field:Size(max = 5000, message = "Description must not exceed 5000 characters")
    val description: String? = null,

    @field:Size(max = 200, message = "Theme must not exceed 200 characters")
    val theme: String? = null,

    @field:Size(max = 5000, message = "Rules must not exceed 5000 characters")
    val rules: String? = null,

    val registrationStart: Instant? = null,

    val registrationEnd: Instant? = null,

    val jamStart: Instant? = null,

    val jamEnd: Instant? = null,

    val judgingStart: Instant? = null,

    val judgingEnd: Instant? = null,

    @field:Min(value = 1, message = "Minimum team size must be at least 1")
    @field:Max(value = 100, message = "Minimum team size must not exceed 100")
    val minTeamSize: Int? = null,

    @field:Min(value = 1, message = "Maximum team size must be at least 1")
    @field:Max(value = 100, message = "Maximum team size must not exceed 100")
    val maxTeamSize: Int? = null
)

data class ChangeGameJamStatusRequest(
    @field:NotNull(message = "Status is required")
    val status: GameJamStatus
)

data class GameJamResponse(
    val id: String,
    val name: String,
    val description: String?,
    val theme: String?,
    val registrationStart: String,
    val registrationEnd: String,
    val jamStart: String,
    val jamEnd: String,
    val judgingStart: String,
    val judgingEnd: String,
    val status: GameJamStatus,
    val organizerId: String,
    val organizerNickname: String,
    val registeredTeamsCount: Int,
    val maxTeamSize: Int,
    val minTeamSize: Int,
    val createdAt: String
)

data class GameJamDetailsResponse(
    val id: String,
    val name: String,
    val description: String?,
    val theme: String?,
    val rules: String?,
    val registrationStart: String,
    val registrationEnd: String,
    val jamStart: String,
    val jamEnd: String,
    val judgingStart: String,
    val judgingEnd: String,
    val status: GameJamStatus,
    val organizerId: String,
    val organizerNickname: String,
    val minTeamSize: Int,
    val maxTeamSize: Int,
    val createdAt: String,
    val updatedAt: String,
    val criteria: List<CriteriaResponse>,
    val judges: List<JudgeResponse>,
    val registeredTeamsCount: Int,
    val submittedProjectsCount: Int
)

data class CriteriaResponse(
    val id: Long,
    val name: String,
    val description: String?,
    val maxScore: Int,
    val weight: String,
    val orderIndex: Int
)

data class JudgeResponse(
    val userId: String,
    val nickname: String,
    val avatarUrl: String?,
    val assignedAt: String
)

data class CreateCriteriaRequest(
    @field:NotBlank(message = "Criteria name is required")
    @field:Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    val name: String,

    @field:Size(max = 500, message = "Description must not exceed 500 characters")
    val description: String? = null,

    @field:Min(value = 1, message = "Max score must be at least 1")
    @field:Max(value = 100, message = "Max score must not exceed 100")
    val maxScore: Int = 10,

    @field:DecimalMin(value = "0.1", message = "Weight must be at least 0.1")
    @field:DecimalMax(value = "10.0", message = "Weight must not exceed 10.0")
    val weight: Double = 1.0,

    @field:Min(value = 0, message = "Order index must be non-negative")
    val orderIndex: Int = 0
)

data class UpdateCriteriaRequest(
    @field:Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    val name: String? = null,

    @field:Size(max = 500, message = "Description must not exceed 500 characters")
    val description: String? = null,

    @field:Min(value = 1, message = "Max score must be at least 1")
    @field:Max(value = 100, message = "Max score must not exceed 100")
    val maxScore: Int? = null,

    @field:DecimalMin(value = "0.1", message = "Weight must be at least 0.1")
    @field:DecimalMax(value = "10.0", message = "Weight must not exceed 10.0")
    val weight: Double? = null,

    @field:Min(value = 0, message = "Order index must be non-negative")
    val orderIndex: Int? = null
)

data class AssignJudgeRequest(
    @field:NotBlank(message = "Judge user ID is required")
    val judgeUserId: String
)