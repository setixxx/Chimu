package software.setixx.chimu.api.exception

class JamNameAlreadyInUseException(message: String) : ApiException(ApiErrorCode.JAM_NAME_ALREADY_IN_USE, message)
