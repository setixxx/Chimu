package software.setixx.chimu.api.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import software.setixx.chimu.api.domain.GameJamStatus
import software.setixx.chimu.api.domain.RatingCriteria
import software.setixx.chimu.api.domain.UserRole
import software.setixx.chimu.api.dto.CreateCriteriaRequest
import software.setixx.chimu.api.dto.CriteriaResponse
import software.setixx.chimu.api.dto.UpdateCriteriaRequest
import software.setixx.chimu.api.repository.GameJamRepository
import software.setixx.chimu.api.repository.RatingCriteriaRepository
import software.setixx.chimu.api.repository.UserRepository
import java.math.BigDecimal
import java.util.UUID

@Service
class CriteriaService(
    private val ratingCriteriaRepository: RatingCriteriaRepository,
    private val gameJamRepository: GameJamRepository,
    private val userRepository: UserRepository
) {

    @Transactional
    fun createCriteria(jamId: String, userId: Long, request: CreateCriteriaRequest): CriteriaResponse {
        val jam = gameJamRepository.findByPublicIdAndDeletedAtIsNull(UUID.fromString(jamId))
            ?: throw IllegalArgumentException("Game jam not found")

        val user = userRepository.findById(userId).orElseThrow()

        if (jam.organizer.id != userId && user.role != UserRole.ADMIN) {
            throw IllegalArgumentException("Only the organizer or admin can add criteria")
        }

        if (jam.status !in listOf(
                GameJamStatus.DRAFT,
                GameJamStatus.ANNOUNCED,
                GameJamStatus.REGISTRATION_OPEN,
                GameJamStatus.REGISTRATION_CLOSED
            )
        ) {
            throw IllegalArgumentException("Cannot manage criteria in current jam status")
        }

        if (ratingCriteriaRepository.existsByGameJamIdAndNameAndDeletedAtIsNull(jam.id!!, request.name)) {
            throw IllegalArgumentException("Criteria with this name already exists for this jam")
        }

        val criteria = RatingCriteria(
            gameJam = jam,
            name = request.name,
            description = request.description,
            maxScore = request.maxScore,
            weight = BigDecimal.valueOf(request.weight),
            orderIndex = request.orderIndex
        )

        val saved = ratingCriteriaRepository.save(criteria)
        return toCriteriaResponse(saved)
    }

    @Transactional(readOnly = true)
    fun getJamCriteria(jamId: String): List<CriteriaResponse> {
        val jam = gameJamRepository.findByPublicIdAndDeletedAtIsNull(UUID.fromString(jamId))
            ?: throw IllegalArgumentException("Game jam not found")

        val criteria = ratingCriteriaRepository.findAllByJamIdOrderByOrderIndex(jam.id!!)
        return criteria.map { toCriteriaResponse(it) }
    }

    @Transactional
    fun updateCriteria(
        jamId: String,
        criteriaId: String,
        userId: Long,
        request: UpdateCriteriaRequest
    ): CriteriaResponse {
        val jam = gameJamRepository.findByPublicIdAndDeletedAtIsNull(UUID.fromString(jamId))
            ?: throw IllegalArgumentException("Game jam not found")

        val user = userRepository.findById(userId).orElseThrow()

        if (jam.organizer.id != userId && user.role != UserRole.ADMIN) {
            throw IllegalArgumentException("Only the organizer or admin can update criteria")
        }

        if (jam.status !in listOf(
                GameJamStatus.DRAFT,
                GameJamStatus.ANNOUNCED,
                GameJamStatus.REGISTRATION_OPEN,
                GameJamStatus.REGISTRATION_CLOSED
            )
        ) {
            throw IllegalArgumentException("Cannot add criteria after jam has started")
        }

        val criteria = ratingCriteriaRepository.findByPublicIdAndDeletedAtIsNull(UUID.fromString(criteriaId))
            ?: throw IllegalArgumentException("Criteria not found")

        if (criteria.gameJam.id != jam.id) {
            throw IllegalArgumentException("Criteria does not belong to this jam")
        }

        request.name?.let {
            if (it != criteria.name && ratingCriteriaRepository.existsByGameJamIdAndNameAndDeletedAtIsNull(jam.id!!, it)) {
                throw IllegalArgumentException("Criteria with this name already exists for this jam")
            }
            criteria.name = it
        }
        request.description?.let { criteria.description = it }
        request.maxScore?.let { criteria.maxScore = it }
        request.weight?.let { criteria.weight = BigDecimal.valueOf(it) }
        request.orderIndex?.let { criteria.orderIndex = it }

        ratingCriteriaRepository.save(criteria)
        return toCriteriaResponse(criteria)
    }

    @Transactional
    fun deleteCriteria(jamId: String, criteriaId: String, userId: Long) {
        val jam = gameJamRepository.findByPublicIdAndDeletedAtIsNull(UUID.fromString(jamId))
            ?: throw IllegalArgumentException("Game jam not found")

        val user = userRepository.findById(userId).orElseThrow()

        if (jam.organizer.id != userId && user.role != UserRole.ADMIN) {
            throw IllegalArgumentException("Only the organizer or admin can delete criteria")
        }

        if (jam.status in listOf(GameJamStatus.JUDGING, GameJamStatus.COMPLETED)) {
            throw IllegalArgumentException("Cannot delete criteria after judging has started")
        }

        val criteria = ratingCriteriaRepository.findByPublicIdAndDeletedAtIsNull(UUID.fromString(criteriaId))
            ?: throw IllegalArgumentException("Criteria not found")

        if (criteria.gameJam.id != jam.id) {
            throw IllegalArgumentException("Criteria does not belong to this jam")
        }

        ratingCriteriaRepository.softDeleteById(criteria.id!!)
    }

    private fun toCriteriaResponse(criteria: RatingCriteria): CriteriaResponse {
        return CriteriaResponse(
            id = criteria.publicId.toString(),
            name = criteria.name,
            description = criteria.description,
            maxScore = criteria.maxScore,
            weight = criteria.weight.toString(),
            orderIndex = criteria.orderIndex
        )
    }
}