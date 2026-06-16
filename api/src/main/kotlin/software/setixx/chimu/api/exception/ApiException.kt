package software.setixx.chimu.api.exception

import org.springframework.http.HttpStatus

open class ApiException(
    val code: ApiErrorCode,
    message: String,
    val httpStatus: HttpStatus = code.status,
    cause: Throwable? = null
) : RuntimeException(message, cause)
