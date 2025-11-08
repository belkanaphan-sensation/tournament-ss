package org.bn.sensation.security;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
/**
 * Про настройку для прода читай sensation/docs/security.md
 * */
public class SecurityConfiguration {

    @Value("${server.servlet.session.cookie.name:JSESSIONID}")
    private String sessionCookieName;

    // todo проверить какие именно эндпоинты здесь действительно нужны
    private static final String[] WHITE_LIST_URL = {
            "/api/v1/auth/**",
            "/v2/api-docs",
            "/v3/api-docs",
            "/v3/api-docs/**",
            "/swagger-resources",
            "/swagger-resources/**",
            "/configuration/ui",
            "/configuration/security",
            "/swagger-ui/**",
            "/webjars/**",
            "/swagger-ui.html",
            "/public/**",
            "/api/v1/auth/login"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                //todo: рассмотреть включение на проде
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(
                        session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(WHITE_LIST_URL).permitAll()
                        .requestMatchers(HttpMethod.GET, "/actuator/health", "/actuator/info").permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) -> {
                            // Вместо редиректа возвращаем чистый 401
                            response.setStatus(HttpStatus.UNAUTHORIZED.value());
                            response.setContentType("application/json");
                            response.getWriter().write("{\"error\": \"Unauthorized\"}");
                        })
                )
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .formLogin(f -> f
                        .loginProcessingUrl("/api/v1/auth/login") // Spring создаст endpoint
                        .successHandler((request, response, authentication) -> {
                            // Успешный логин - возвращаем JSON
                            response.setStatus(HttpStatus.OK.value());
                            response.setContentType("application/json");
                            response.getWriter().write("{\"message\": \"Login successful\"}");
                        })
                        .failureHandler((request, response, exception) -> {
                            // Ошибка логина - возвращаем JSON ошибку
                            response.setStatus(HttpStatus.UNAUTHORIZED.value());
                            response.setContentType("application/json");
                            response.getWriter().write("{\"error\": \"Login failed: " + exception.getMessage() + "\"}");
                        })
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/api/v1/auth/logout")
                        .clearAuthentication(true)
                        .deleteCookies(sessionCookieName)
                        .invalidateHttpSession(true)
                        .permitAll()
                        // это надо чтобы после logout он не пытался перейти на login который не рабочий
                        .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler())
                )
                .headers(headers -> headers
                        .httpStrictTransportSecurity(hsts -> hsts.includeSubDomains(true).preload(true))
                        .contentSecurityPolicy(csp -> csp.policyDirectives(
                                "default-src 'self'; " +
                                        "script-src 'self' 'unsafe-inline' 'unsafe-eval'; " +
                                        "style-src 'self' 'unsafe-inline'; " +
                                        "img-src 'self' data: blob:; " +
                                        "font-src 'self' data:; " +
                                        "connect-src 'self'; " +
                                        "frame-ancestors 'self'"
                        ))
                        .frameOptions(frame -> frame.sameOrigin())
                        .referrerPolicy(rp -> rp.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER))
                );
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of(
                "http://localhost:*",
                // это адрес компа вместо localhost, можно его глянуть в ipconfig (на винде). Что бы при перезагрузке роутера
                //он оставался тем же, надо его в роутере задать как статический для MAC адреса компа, на котором запускается сервер
                //его надо будет вынести в отдельную переменную в application.properties. Нужен конкретный адрес UI сервера, чтобы cors работал
                //ifconfig строчка с inet
                "http://192.168.0.103:5173",
                "http://192.168.31.65:5173",
                "http://192.168.64.1:5173",
                "http://192.168.31.60:5173"
                //hamachi
                //, "http://25.3.135.219:5173"
        ));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AesPasswordEncoder passwordEncoder() {
        return new AesPasswordEncoder("1234567890ABCDEF"); // ключ длиной 16, 24 или 32 символа
    }
//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder(12);
//    }
}
