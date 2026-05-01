package software.setixx.chimu.api.domain

enum class UserRole {
    PARTICIPANT, ORGANIZER, JUDGE, ADMIN, GUEST
}

enum class GameJamStatus {
    ANNOUNCED, REGISTRATION_OPEN, REGISTRATION_CLOSED, IN_PROGRESS, JUDGING, COMPLETED, CANCELLED
}

enum class RegistrationStatus {
    PENDING, APPROVED, REJECTED, WITHDRAWN, CANCELLED
}

enum class ProjectStatus {
    DRAFT, SUBMITTED, UNDER_REVIEW, PUBLISHED, DISQUALIFIED
}

enum class ProjectFileType {
    SCREENSHOT, BUILD, VIDEO, DOCUMENT, OTHER
}

enum class RoleRequestStatus {
    PENDING,
    APPROVED,
    REJECTED,
    CANCELLED
}

enum class TransferStatus {
    PENDING,
    ACCEPTED,
    REJECTED,
    CANCELLED
}