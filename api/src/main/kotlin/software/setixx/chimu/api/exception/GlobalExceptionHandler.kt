package software.setixx.chimu.api.exception

import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.authentication.AuthenticationServiceException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

/**
 * Глобальный обработчик исключений.
 * Перехватывает ошибки валидации, аутентификации и бизнес-логики для возврата стандартизированных JSON-ответов.
 */
@ControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(ApiException::class)
    fun handleApi(ex: ApiException): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(ex.httpStatus)
            .contentType(MediaType.APPLICATION_JSON)
            .body(ErrorResponse.of(ex.code, ex.message ?: "Unexpected error"))
    }

    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDenied(ex: AccessDeniedException): ResponseEntity<ErrorResponse> {
        val code = ApiErrorCodeResolver.fromAccessDenied(ex.message)
        return buildResponse(code, ex.message ?: "Access denied")
    }

    @ExceptionHandler(SecurityException::class)
    fun handleSecurity(ex: SecurityException): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .contentType(MediaType.APPLICATION_JSON)
            .body(ErrorResponse.of(ApiErrorCode.UNAUTHORIZED, ex.message ?: "Unauthorized"))
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val errors = ex.bindingResult.fieldErrors.associate {
            it.field to (it.defaultMessage ?: "Invalid value")
        }
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .contentType(MediaType.APPLICATION_JSON)
            .body(ErrorResponse.of(ApiErrorCode.VALIDATION_FAILED, "Validation failed", errors))
    }

    @ExceptionHandler(AuthenticationServiceException::class)
    fun handleAuthenticationService(ex: AuthenticationServiceException): ResponseEntity<ErrorResponse> {
        val code = ApiErrorCodeResolver.fromAuthentication(ex.message)
        return buildResponse(code, ex.message ?: "Authentication failed")
    }

    @ExceptionHandler(EmailAlreadyExistsException::class)
    fun handleEmailAlreadyExists(ex: EmailAlreadyExistsException): ResponseEntity<ErrorResponse> {
        return buildResponse(ApiErrorCode.EMAIL_ALREADY_EXISTS, ex.message ?: "The email is already in use")
    }

    @ExceptionHandler(JamNameAlreadyInUseException::class)
    fun handleJamUniqueConstraintViolation(ex: JamNameAlreadyInUseException): ResponseEntity<ErrorResponse> {
        return buildResponse(ApiErrorCode.JAM_NAME_ALREADY_IN_USE, ex.message ?: "Jam name is already in use")
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(ex: IllegalArgumentException): ResponseEntity<ErrorResponse> {
        val code = ApiErrorCodeResolver.fromIllegalArgument(ex.message)
        return buildResponse(code, ex.message ?: "Invalid request")
    }

    @ExceptionHandler(IllegalStateException::class)
    fun handleIllegalState(ex: IllegalStateException): ResponseEntity<ErrorResponse> {
        val code = ApiErrorCodeResolver.fromIllegalState(ex.message)
        return buildResponse(code, ex.message ?: "Invalid state")
    }

    @ExceptionHandler(RuntimeException::class)
    fun handleRuntime(ex: RuntimeException): ResponseEntity<ErrorResponse> {
        return buildResponse(ApiErrorCode.INTERNAL_ERROR, "Unexpected error")
    }

    private fun buildResponse(code: ApiErrorCode, message: String): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(code.status)
            .contentType(MediaType.APPLICATION_JSON)
            .body(ErrorResponse.of(code, message))
    }
}
