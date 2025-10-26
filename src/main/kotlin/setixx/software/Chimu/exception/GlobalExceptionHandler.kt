package setixx.software.Chimu.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException::class)
    fun handleRuntime(ex: RuntimeException): ResponseEntity<Map<String, String>> {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(mapOf("error" to (ex.message ?: "Unexpected error")))
    }

    @ExceptionHandler(SecurityException::class)
    fun handleSecurity(ex: SecurityException): ResponseEntity<Map<String, String>> {
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(mapOf("error" to (ex.message ?: "Unauthorized")))
    }
}
