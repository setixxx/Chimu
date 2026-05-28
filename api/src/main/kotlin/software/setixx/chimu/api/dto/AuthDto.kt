package software.setixx.chimu.api.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

/**
 * Запрос на аутентификацию пользователя.
 * Содержит email и пароль для входа в систему.
 */
data class AuthenticationRequest(
    @field:NotBlank(message = "Email must not be blank")
    @field:Email(message = "Email must be a valid email address")
    val email: String,
    @field:NotBlank(message = "Password must not be blank")
    val password: String
)

/**
 * Ответ на успешную аутентификацию.
 * Возвращает пару JWT-токенов: Access и Refresh.
 */
data class AuthenticationResponse(
    val accessToken: String,
    val refreshToken: String
)

/**
 * Запрос на регистрацию нового пользователя.
 * Содержит необходимые данные для создания аккаунта.
 */
data class RegisterRequest(
    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Invalid email format")
    val email: String,
    /*    @field:NotBlank(message = "Password is required")
        @field:Size(min = 8, max = 128, message = "Password must be between 8 and 128 characters")
        @field:Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$",
            message = "Password must contain at least one uppercase letter, one lowercase letter, and one digit"
        )*/
    val password: String,
)

/**
 * Ответ на успешную регистрацию.
 * Возвращает публичный идентификатор созданного пользователя.
 */
data class RegisterResponse(
    val publicId: String
)


/**
 * Запрос на обновление пары токенов.
 * Использует Refresh-токен для получения нового Access-токена.
 */
data class RefreshTokenRequest(
    @field:NotBlank(message = "Token is required")
    val token: String
)

/**
 * Ответ с новой парой токенов после обновления.
 */
data class RefreshTokenResponse(
    val accessToken: String,
    val refreshToken: String
)
