package software.setixx.chimu.api.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.DefaultSecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
class SecurityConfig(
    private val userDetailsService: JwtUserDetailsService,
) {
    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun authenticationManager(config: AuthenticationConfiguration): AuthenticationManager =
        config.authenticationManager

    @Bean
    fun securityFilterChain(
        http: HttpSecurity,
        jwtAuthenticationFilter: JwtAuthorizationFilter,
    ): DefaultSecurityFilterChain {
        http
            .csrf { it.disable() }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers(
                        "/api/auth",
                        "/api/auth/register",
                        "/api/auth/refresh",
                        "/error",
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/swagger-ui.html"
                    ).permitAll()

                    .requestMatchers(HttpMethod.GET, "/api/jams", "/api/jams/**").permitAll()

                    .requestMatchers(HttpMethod.POST, "/api/jams").hasAnyRole("ORGANIZER", "ADMIN")

                    .requestMatchers(HttpMethod.PATCH, "/api/jams/**").authenticated()
                    .requestMatchers(HttpMethod.DELETE, "/api/jams/**").authenticated()
                    .requestMatchers(HttpMethod.POST, "/api/jams/*/status").authenticated()

                    .requestMatchers(HttpMethod.POST, "/api/jams/*/criteria").authenticated()
                    .requestMatchers(HttpMethod.PATCH, "/api/jams/*/criteria/**").authenticated()
                    .requestMatchers(HttpMethod.DELETE, "/api/jams/*/criteria/**").authenticated()

                    .requestMatchers(HttpMethod.POST, "/api/jams/*/judges").authenticated()
                    .requestMatchers(HttpMethod.DELETE, "/api/jams/*/judges/**").authenticated()

                    .requestMatchers("/api/jams/*/registrations/**").authenticated()

                    .requestMatchers(
                        "/api/users/me",
                        "/api/users/profile",
                        "/api/users/email",
                        "/api/users/skills/**",
                        "/api/users/change-password",
                        "/api/auth/logout",
                        "/api/teams/**",
                        "/api/specializations",
                        "/api/skills"
                    ).authenticated()

                    .anyRequest().fullyAuthenticated()
            }
            .userDetailsService(userDetailsService)
            .sessionManagement {
                it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
            .exceptionHandling {
                it.authenticationEntryPoint { _, response, _ ->
                    response.status = 401
                    response.contentType = "application/json"
                    response.writer.write("""{"error":"Unauthorized"}""")
                }
                it.accessDeniedHandler { _, response, _ ->
                    response.status = 403
                    response.contentType = "application/json"
                    response.writer.write("""{"error":"Forbidden"}""")
                }
            }
        return http.build()
    }
}