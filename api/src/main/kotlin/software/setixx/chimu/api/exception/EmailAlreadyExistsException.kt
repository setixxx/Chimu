package software.setixx.chimu.api.exception

class EmailAlreadyExistsException(message: String) : ApiException(ApiErrorCode.EMAIL_ALREADY_EXISTS, message)
