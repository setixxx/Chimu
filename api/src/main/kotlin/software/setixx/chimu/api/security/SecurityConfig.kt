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
                    .requestMatchers(HttpMethod.PATCH, "/api/jams/**").hasAnyRole("ORGANIZER", "ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/api/jams/**").hasAnyRole("ORGANIZER", "ADMIN")
                    .requestMatchers(HttpMethod.POST, "/api/jams/*/status").hasAnyRole("ORGANIZER", "ADMIN")

                    .requestMatchers(HttpMethod.POST, "/api/jams/*/criteria").hasAnyRole("ORGANIZER", "ADMIN")
                    .requestMatchers(HttpMethod.PATCH, "/api/jams/*/criteria/**").hasAnyRole("ORGANIZER", "ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/api/jams/*/criteria/**").hasAnyRole("ORGANIZER", "ADMIN")

                    .requestMatchers(HttpMethod.POST, "/api/jams/*/judges").hasAnyRole("ORGANIZER", "ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/api/jams/*/judges/**").hasAnyRole("ORGANIZER", "ADMIN")

                    .requestMatchers(HttpMethod.GET, "/api/jams/*/registrations").authenticated()
                    .requestMatchers(HttpMethod.POST, "/api/jams/*/registrations").hasAnyRole("PARTICIPANT", "ADMIN")
                    .requestMatchers(HttpMethod.PATCH, "/api/jams/*/registrations/**").hasAnyRole("ORGANIZER", "ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/api/jams/*/registrations/**").hasAnyRole("PARTICIPANT", "ADMIN")

                    .requestMatchers(HttpMethod.POST, "/api/jams/*/projects").authenticated()
                    .requestMatchers(HttpMethod.GET, "/api/projects/**").permitAll()
                    .requestMatchers(HttpMethod.PATCH, "/api/projects/**").authenticated()
                    .requestMatchers(HttpMethod.DELETE, "/api/projects/**").authenticated()
                    .requestMatchers(HttpMethod.POST, "/api/projects/*/submit").authenticated()
                    .requestMatchers(HttpMethod.POST, "/api/projects/*/return-draft").hasAnyRole("ORGANIZER", "ADMIN")
                    .requestMatchers(HttpMethod.POST, "/api/projects/*/publish").hasAnyRole("ORGANIZER", "ADMIN")
                    .requestMatchers(HttpMethod.POST, "/api/projects/*/disqualify").hasAnyRole("ORGANIZER", "ADMIN")

                    .requestMatchers(HttpMethod.POST, "/api/projects/*/ratings").hasAnyRole("JUDGE", "ADMIN")
                    .requestMatchers(HttpMethod.PATCH, "/api/ratings/**").hasAnyRole("JUDGE", "ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/api/ratings/**").hasAnyRole("JUDGE", "ADMIN")
                    .requestMatchers(HttpMethod.GET, "/api/projects/*/ratings").authenticated()
                    .requestMatchers(HttpMethod.GET, "/api/projects/*/my-ratings").hasAnyRole("JUDGE", "ADMIN")
                    .requestMatchers(HttpMethod.GET, "/api/jams/*/my-progress").hasAnyRole("JUDGE", "ADMIN")

                    .requestMatchers(HttpMethod.GET, "/api/jams/*/leaderboard").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/jams/*/statistics").hasAnyRole("ORGANIZER", "ADMIN")

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