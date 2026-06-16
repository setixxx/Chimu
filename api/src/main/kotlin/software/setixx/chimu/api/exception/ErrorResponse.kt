package software.setixx.chimu.api.exception

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ErrorResponse(
    val code: String,
    val message: String,
    val details: Map<String, String>? = null
) {
    companion object {
        fun of(
            code: ApiErrorCode,
            message: String,
            details: Map<String, String>? = null
        ): ErrorResponse = ErrorResponse(
            code = code.name,
            message = message,
            details = details
        )
    }
}
