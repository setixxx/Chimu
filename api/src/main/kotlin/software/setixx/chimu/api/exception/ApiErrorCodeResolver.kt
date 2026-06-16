package software.setixx.chimu.api.exception

object ApiErrorCodeResolver {
    fun fromAuthentication(message: String?): ApiErrorCode = when (message) {
        "Refresh token expired" -> ApiErrorCode.REFRESH_TOKEN_EXPIRED
        "Invalid refresh token" -> ApiErrorCode.INVALID_REFRESH_TOKEN
        "Invalid token type. Expected a refresh token" -> ApiErrorCode.INVALID_TOKEN_TYPE
        else -> ApiErrorCode.AUTHENTICATION_FAILED
    }

    fun fromAccessDenied(message: String?): ApiErrorCode = when {
        message == null -> ApiErrorCode.ACCESS_DENIED
        message.startsWith("Only the team leader") -> ApiErrorCode.TEAM_LEADER_REQUIRED
        message.startsWith("Only team members") -> ApiErrorCode.TEAM_MEMBER_REQUIRED
        message.startsWith("Only the organizer or admin") -> ApiErrorCode.ORGANIZER_OR_ADMIN_REQUIRED
        message.startsWith("Only the jam organizer") -> ApiErrorCode.ORGANIZER_OR_ADMIN_REQUIRED
        else -> ApiErrorCode.ACCESS_DENIED
    }

    fun fromIllegalArgument(message: String?): ApiErrorCode = fromCommon(message) ?: when {
        message == null -> ApiErrorCode.BAD_REQUEST
        message.startsWith("Invalid UUID string:") -> ApiErrorCode.INVALID_ID_FORMAT
        message.startsWith("User with email ") -> ApiErrorCode.EMAIL_ALREADY_EXISTS
        message.startsWith("Game jam with name ") -> ApiErrorCode.JAM_NAME_ALREADY_IN_USE
        message.startsWith("Team with name ") -> ApiErrorCode.TEAM_NAME_ALREADY_IN_USE
        message.startsWith("Team is already registered for another active jam:") ->
            ApiErrorCode.TEAM_ALREADY_REGISTERED_FOR_ACTIVE_JAM
        message.startsWith("Cannot join team while it is registered for an active game jam:") ->
            ApiErrorCode.TEAM_HAS_ACTIVE_REGISTRATIONS
        message.startsWith("Team size (") -> ApiErrorCode.TEAM_SIZE_OUT_OF_RANGE
        message.startsWith("Cannot update registration with final status") -> ApiErrorCode.REGISTRATION_FINAL
        message.startsWith("Cannot force-transition from") -> ApiErrorCode.INVALID_STATUS_TRANSITION
        message.startsWith("Invalid status transition from") -> ApiErrorCode.INVALID_STATUS_TRANSITION
        message.startsWith("Unsupported file type") -> ApiErrorCode.UNSUPPORTED_FILE_TYPE
        message.startsWith("Unsupported MIME type") -> ApiErrorCode.UNSUPPORTED_FILE_TYPE
        message.startsWith("File size exceeds") -> ApiErrorCode.FILE_TOO_LARGE
        message.startsWith("Score must be between") -> ApiErrorCode.INVALID_SCORE
        message.startsWith("Score must be in increments") -> ApiErrorCode.INVALID_SCORE
        message.contains("must be before") -> ApiErrorCode.INVALID_DATE_RANGE
        message.contains("Registration period must be at least") -> ApiErrorCode.INVALID_DATE_RANGE
        message.startsWith("Minimum team size") -> ApiErrorCode.INVALID_TEAM_SIZE
        else -> ApiErrorCode.BAD_REQUEST
    }

    fun fromIllegalState(message: String?): ApiErrorCode = fromCommon(message) ?: when (message) {
        "Game jam was modified by another user. Please refresh and try again." -> ApiErrorCode.CONCURRENT_MODIFICATION
        "Team was modified by another user. Please refresh and try again." -> ApiErrorCode.CONCURRENT_MODIFICATION
        "Projects can only be uploaded at the in progress stage." -> ApiErrorCode.PROJECT_UPLOAD_CLOSED
        "Deleting a project is prohibited at the judging stage." -> ApiErrorCode.PROJECT_STATUS_NOT_DELETABLE
        "It is not possible to remove a judge from a completed jam." -> ApiErrorCode.JUDGE_REMOVAL_CLOSED
        "File storage unavailable, please try again later" -> ApiErrorCode.FILE_STORAGE_UNAVAILABLE
        "Files of the submitted project cannot be uploaded" -> ApiErrorCode.SUBMITTED_PROJECT_FILES_LOCKED
        "Cannot delete account: transfer leadership first." -> ApiErrorCode.ACCOUNT_DELETE_RESTRICTED
        "Cannot delete account: transfer game jam ownership first." -> ApiErrorCode.ACCOUNT_DELETE_RESTRICTED
        "Cannot delete account: active judge in an ongoing jam." -> ApiErrorCode.ACCOUNT_DELETE_RESTRICTED
        else -> when {
            message != null && message.startsWith("Files can only be uploaded while the jam is in progress") ->
                ApiErrorCode.PROJECT_UPLOAD_CLOSED
            message != null && message.startsWith("Maximum number of ") -> ApiErrorCode.PROJECT_FILE_LIMIT_EXCEEDED
            else -> ApiErrorCode.BAD_REQUEST
        }
    }

    private fun fromCommon(message: String?): ApiErrorCode? = when (message) {
        "User not found" -> ApiErrorCode.USER_NOT_FOUND
        "Admin not found" -> ApiErrorCode.ADMIN_NOT_FOUND
        "Game jam not found" -> ApiErrorCode.GAME_JAM_NOT_FOUND
        "Team not found" -> ApiErrorCode.TEAM_NOT_FOUND
        "Project not found" -> ApiErrorCode.PROJECT_NOT_FOUND
        "Project has no associated team" -> ApiErrorCode.PROJECT_TEAM_MISSING
        "File not found" -> ApiErrorCode.PROJECT_FILE_NOT_FOUND
        "Registration not found" -> ApiErrorCode.REGISTRATION_NOT_FOUND
        "Request not found" -> ApiErrorCode.ROLE_UPGRADE_REQUEST_NOT_FOUND
        "Transfer request not found" -> ApiErrorCode.TRANSFER_REQUEST_NOT_FOUND
        "No pending transfer request found for this jam" -> ApiErrorCode.TRANSFER_REQUEST_NOT_FOUND
        "Rating not found" -> ApiErrorCode.RATING_NOT_FOUND
        "Criteria not found" -> ApiErrorCode.CRITERIA_NOT_FOUND
        "Specialization not found" -> ApiErrorCode.SPECIALIZATION_NOT_FOUND
        "One or more skills not found" -> ApiErrorCode.SKILL_NOT_FOUND
        "Recipient not found" -> ApiErrorCode.RECIPIENT_NOT_FOUND
        "Judge user not found" -> ApiErrorCode.JUDGE_USER_NOT_FOUND
        "Token not found" -> ApiErrorCode.TOKEN_NOT_FOUND
        "This jam has no banner" -> ApiErrorCode.BANNER_NOT_FOUND

        "Old password is incorrect" -> ApiErrorCode.OLD_PASSWORD_INCORRECT
        "Nickname already taken" -> ApiErrorCode.NICKNAME_ALREADY_TAKEN
        "You cannot create more than 10 teams" -> ApiErrorCode.TEAM_LIMIT_EXCEEDED
        "Team is already registered for this game jam" -> ApiErrorCode.TEAM_ALREADY_REGISTERED
        "Team already has a rejected registration status for this game jam" -> ApiErrorCode.TEAM_REGISTRATION_REJECTED
        "User has already registered a team for this game jam" -> ApiErrorCode.USER_ALREADY_REGISTERED_TEAM
        "You are already a member of this team" -> ApiErrorCode.TEAM_MEMBER_ALREADY_EXISTS
        "Cannot join team while it is registered for an active game jam" -> ApiErrorCode.TEAM_HAS_ACTIVE_REGISTRATIONS
        "Cannot leave team while it has active jam registrations. Withdraw from jams first." ->
            ApiErrorCode.TEAM_HAS_ACTIVE_REGISTRATIONS
        "Cannot delete team while it has active jam registrations" -> ApiErrorCode.TEAM_HAS_ACTIVE_REGISTRATIONS
        "Cannot kick members while team has active jam registrations" -> ApiErrorCode.TEAM_HAS_ACTIVE_REGISTRATIONS
        "Team leader cannot leave the team. Transfer leadership or delete the team." ->
            ApiErrorCode.TEAM_LEADER_CANNOT_LEAVE
        "Team leader cannot kick themselves" -> ApiErrorCode.TEAM_LEADER_CANNOT_KICK_SELF
        "Your team already has a project for this jam" -> ApiErrorCode.PROJECT_ALREADY_EXISTS
        "User already has the requested role" -> ApiErrorCode.ROLE_ALREADY_ASSIGNED
        "A pending request for this role already exists" -> ApiErrorCode.ROLE_UPGRADE_REQUEST_ALREADY_EXISTS
        "A pending transfer request already exists for this jam" -> ApiErrorCode.TRANSFER_REQUEST_ALREADY_EXISTS
        "This judge is already assigned to this jam" -> ApiErrorCode.JUDGE_ALREADY_ASSIGNED
        "Criteria with this name already exists for this jam" -> ApiErrorCode.CRITERIA_NAME_ALREADY_IN_USE

        "Invalid invite token" -> ApiErrorCode.INVALID_INVITE_TOKEN
        "Can only request ORGANIZER or JUDGE role" -> ApiErrorCode.INVALID_ROLE_UPGRADE_TARGET
        "Status must be APPROVED or REJECTED" -> ApiErrorCode.INVALID_REVIEW_STATUS
        "Status must be ACCEPTED or REJECTED" -> ApiErrorCode.INVALID_REVIEW_STATUS
        "Organizer can only set APPROVED, REJECTED or DISQUALIFIED" -> ApiErrorCode.INVALID_REVIEW_STATUS
        "File is empty" -> ApiErrorCode.INVALID_FILE
        "Uploaded file is empty" -> ApiErrorCode.INVALID_FILE
        "Unsupported file type. Only JPEG, PNG and WebP are allowed" -> ApiErrorCode.UNSUPPORTED_FILE_TYPE
        "All team members must have a specialization assigned before registration" ->
            ApiErrorCode.TEAM_MEMBER_SPECIALIZATION_REQUIRED
        "Criteria does not belong to this jam" -> ApiErrorCode.CRITERIA_JAM_MISMATCH

        "Only organizers can create game jams" -> ApiErrorCode.ORGANIZER_REQUIRED
        "Only the organizer can initiate a transfer" -> ApiErrorCode.ORGANIZER_REQUIRED
        "Only the organizer can cancel a transfer" -> ApiErrorCode.ORGANIZER_REQUIRED
        "Only the organizer or admin can update this game jam" -> ApiErrorCode.ORGANIZER_OR_ADMIN_REQUIRED
        "Only the organizer or admin can update registration status" -> ApiErrorCode.ORGANIZER_OR_ADMIN_REQUIRED
        "Only organizer or admin can disqualify projects" -> ApiErrorCode.ORGANIZER_OR_ADMIN_REQUIRED
        "Only organizer or admin can view statistics" -> ApiErrorCode.ORGANIZER_OR_ADMIN_REQUIRED
        "Only the organizer or admin can add criteria" -> ApiErrorCode.ORGANIZER_OR_ADMIN_REQUIRED
        "Only the organizer or admin can update criteria" -> ApiErrorCode.ORGANIZER_OR_ADMIN_REQUIRED
        "Only the organizer or admin can delete criteria" -> ApiErrorCode.ORGANIZER_OR_ADMIN_REQUIRED
        "Only the organizer or admin can assign judges" -> ApiErrorCode.ORGANIZER_OR_ADMIN_REQUIRED
        "Only the organizer or admin can remove judges" -> ApiErrorCode.ORGANIZER_OR_ADMIN_REQUIRED
        "Only team leader can register the team" -> ApiErrorCode.TEAM_LEADER_REQUIRED
        "Only team leader can cancel registration" -> ApiErrorCode.TEAM_LEADER_REQUIRED
        "Only team leader can update team information" -> ApiErrorCode.TEAM_LEADER_REQUIRED
        "Only team leader can delete the team" -> ApiErrorCode.TEAM_LEADER_REQUIRED
        "Only team leader can kick members" -> ApiErrorCode.TEAM_LEADER_REQUIRED
        "Only team leader can regenerate invite token" -> ApiErrorCode.TEAM_LEADER_REQUIRED
        "Only team leader can update the project" -> ApiErrorCode.TEAM_LEADER_REQUIRED
        "Only team leader can submit the project" -> ApiErrorCode.TEAM_LEADER_REQUIRED
        "Only team leader can return project to draft" -> ApiErrorCode.TEAM_LEADER_REQUIRED
        "Only team leader can delete the project" -> ApiErrorCode.TEAM_LEADER_REQUIRED
        "You must be a team leader of an approved team for this jam" ->
            ApiErrorCode.TEAM_LEADER_APPROVED_TEAM_REQUIRED
        "You are not a member of this team" -> ApiErrorCode.TEAM_MEMBER_REQUIRED
        "User is not a member of this team" -> ApiErrorCode.TEAM_MEMBER_REQUIRED
        "You are not assigned as a judge for this jam" -> ApiErrorCode.JUDGE_REQUIRED
        "You are not a judge for this jam" -> ApiErrorCode.JUDGE_REQUIRED
        "User must have JUDGE or ADMIN role" -> ApiErrorCode.JUDGE_OR_ADMIN_REQUIRED
        "You can only update your own ratings" -> ApiErrorCode.OWN_RATING_REQUIRED
        "You can only delete your own ratings" -> ApiErrorCode.OWN_RATING_REQUIRED
        "Not authorized to review this transfer" -> ApiErrorCode.ACCESS_DENIED

        "Registration is temporarily unavailable." -> ApiErrorCode.REGISTRATION_UNAVAILABLE
        "Game jam is not open for registration" -> ApiErrorCode.JAM_REGISTRATION_NOT_OPEN
        "Cannot update game jam in current status" -> ApiErrorCode.GAME_JAM_STATUS_NOT_EDITABLE
        "Game jam can only be cancelled while announced or during registration" ->
            ApiErrorCode.GAME_JAM_NOT_CANCELLABLE
        "Only draft game jams can be deleted" -> ApiErrorCode.GAME_JAM_NOT_DRAFT
        "Only draft game jams can be published" -> ApiErrorCode.GAME_JAM_NOT_DRAFT
        "Game jam must have at least one rating criterion before publishing" ->
            ApiErrorCode.GAME_JAM_MISSING_CRITERIA
        "Game jam must have at least one judge" -> ApiErrorCode.GAME_JAM_MISSING_JUDGE
        "Game jam must have a banner url" -> ApiErrorCode.GAME_JAM_MISSING_BANNER
        "Cannot manage criteria in current jam status" -> ApiErrorCode.CRITERIA_STATUS_NOT_EDITABLE
        "Cannot add criteria after jam has started" -> ApiErrorCode.CRITERIA_STATUS_NOT_EDITABLE
        "Cannot delete criteria after judging has started" -> ApiErrorCode.CRITERIA_STATUS_NOT_EDITABLE
        "Cannot assign judge after jam started" -> ApiErrorCode.JUDGE_ASSIGNMENT_CLOSED
        "Only pending or approved registrations can be changed by the team" ->
            ApiErrorCode.REGISTRATION_STATUS_NOT_CHANGEABLE
        "Only approved teams can withdraw after the jam has started" ->
            ApiErrorCode.REGISTRATION_STATUS_NOT_CHANGEABLE
        "Registration cannot be cancelled or withdrawn in current jam status" ->
            ApiErrorCode.REGISTRATION_STATUS_NOT_CHANGEABLE
        "Registrations can only be approved or rejected before the jam starts" ->
            ApiErrorCode.REGISTRATION_STATUS_NOT_CHANGEABLE
        "Only pending registrations can be approved or rejected" -> ApiErrorCode.REGISTRATION_STATUS_NOT_CHANGEABLE
        "Teams can only be disqualified after the jam has started" -> ApiErrorCode.REGISTRATION_STATUS_NOT_CHANGEABLE
        "Only approved teams can be disqualified" -> ApiErrorCode.REGISTRATION_STATUS_NOT_CHANGEABLE
        "Only pending requests can be cancelled" -> ApiErrorCode.ROLE_UPGRADE_REQUEST_STATUS_NOT_CHANGEABLE
        "Only pending requests can be reviewed" -> ApiErrorCode.ROLE_UPGRADE_REQUEST_STATUS_NOT_CHANGEABLE
        "Projects can only be submitted during the jam period" -> ApiErrorCode.PROJECT_STATUS_NOT_SUBMITTABLE
        "Only draft projects can be edited" -> ApiErrorCode.PROJECT_STATUS_NOT_EDITABLE
        "Only draft projects can be deleted" -> ApiErrorCode.PROJECT_STATUS_NOT_DELETABLE
        "Project is already submitted" -> ApiErrorCode.PROJECT_ALREADY_SUBMITTED
        "Only submitted projects can be returned to draft" -> ApiErrorCode.PROJECT_STATUS_NOT_EDITABLE
        "Cannot return to draft after jam is completed" -> ApiErrorCode.PROJECT_RETURN_TO_DRAFT_CLOSED
        "Team is not registered for this jam" -> ApiErrorCode.TEAM_REGISTRATION_REQUIRED
        "Team is no longer approved for this jam" -> ApiErrorCode.TEAM_REGISTRATION_NOT_APPROVED
        "Leaderboard is only visible during judging (for organizers) or after completion" ->
            ApiErrorCode.LEADERBOARD_NOT_VISIBLE
        "Ratings can only be submitted during judging phase" -> ApiErrorCode.RATING_PHASE_CLOSED
        "Ratings can only be updated during judging phase" -> ApiErrorCode.RATING_PHASE_CLOSED
        "Ratings can only be deleted during judging phase" -> ApiErrorCode.RATING_PHASE_CLOSED
        "Full ratings are only visible after jam completion" -> ApiErrorCode.RATING_VISIBILITY_RESTRICTED
        "Only submitted projects can be rated" -> ApiErrorCode.PROJECT_NOT_RATEABLE
        "Transfer request has expired" -> ApiErrorCode.TRANSFER_REQUEST_EXPIRED
        "Cannot transfer jam to yourself" -> ApiErrorCode.TRANSFER_SELF_TARGET
        "Recipient must have ORGANIZER role" -> ApiErrorCode.RECIPIENT_ROLE_INVALID
        "Recipient cannot be a participant in this jam" -> ApiErrorCode.PARTICIPANT_CONFLICT
        "Judge cannot be a participant in this jam" -> ApiErrorCode.PARTICIPANT_CONFLICT
        "Organizer cannot be a judge for their own jam" -> ApiErrorCode.PARTICIPANT_CONFLICT
        "You can't downgrade your role" -> ApiErrorCode.ROLE_DOWNGRADE_FORBIDDEN
        "Files of the submitted project cannot be deleted" -> ApiErrorCode.SUBMITTED_PROJECT_FILES_LOCKED

        else -> null
    }
}
