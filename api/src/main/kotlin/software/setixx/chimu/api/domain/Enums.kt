package software.setixx.chimu.api.domain

enum class UserRole {
    PARTICIPANT, ORGANIZER, JUDGE, ADMIN, GUEST
}

enum class GameJamStatus {
    DRAFT, ANNOUNCED, REGISTRATION_OPEN, REGISTRATION_CLOSED, IN_PROGRESS, JUDGING, COMPLETED, CANCELLED
}

enum class RegistrationStatus {
    PENDING, APPROVED, REJECTED, CANCELLED, WITHDRAWN, DISQUALIFIED
}

enum class ProjectStatus {
    DRAFT, SUBMITTED, UNDER_REVIEW, DISQUALIFIED
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
