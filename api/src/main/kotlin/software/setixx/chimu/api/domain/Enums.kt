package software.setixx.chimu.api.domain

/**
 * Роли пользователей в системе.
 * Определяет уровень доступа к различным функциям платформы.
 */
enum class UserRole {
    PARTICIPANT, ORGANIZER, JUDGE, ADMIN, GUEST
}

/**
 * Возможные статусы Game Jam.
 * Отслеживает жизненный цикл мероприятия от черновика до завершения.
 */
enum class GameJamStatus {
    DRAFT, ANNOUNCED, REGISTRATION_OPEN, REGISTRATION_CLOSED, IN_PROGRESS, JUDGING, COMPLETED, CANCELLED
}

/**
 * Статусы регистрации команд на джем.
 */
enum class RegistrationStatus {
    PENDING, APPROVED, REJECTED, CANCELLED, WITHDRAWN, DISQUALIFIED
}

/**
 * Статусы проектов участников.
 */
enum class ProjectStatus {
    DRAFT, SUBMITTED, UNDER_REVIEW, DISQUALIFIED
}

/**
 * Типы файлов, загружаемых к проекту.
 */
enum class ProjectFileType {
    SCREENSHOT, BUILD, VIDEO, DOCUMENT, OTHER
}

/**
 * Статусы заявок на повышение роли.
 */
enum class RoleRequestStatus {
    PENDING,
    APPROVED,
    REJECTED,
    CANCELLED
}

/**
 * Статусы запросов на передачу прав владения джемом.
 */
enum class TransferStatus {
    PENDING,
    ACCEPTED,
    REJECTED,
    CANCELLED
}
