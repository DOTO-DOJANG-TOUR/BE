package com.doto.global.config;

import com.doto.domain.member.entity.Authority;
import com.doto.global.security.JsonAuthenticationEntryPoint;
import com.doto.global.security.jwt.JwtAuthenticationFilter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    // 프론트엔드에서 직접 호출을 허용할 출처 목록
    private static final List<String> ALLOWED_ORIGINS = List.of(
        "https://doto-reward.netlify.app",
        "https://doto-app.cloud",
        "http://localhost:5173",
        "http://localhost:8080"
    );

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JsonAuthenticationEntryPoint jsonAuthenticationEntryPoint;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/v1/auth/**",
                                "/health",
                                "/actuator/health",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                // QR로 스캔한 방문객이 로그인 없이 바로 여는 보상 처리 화면에서 호출하는 API
                                "/api/v1/stamp-tours/reward"
                        ).permitAll()
                        .requestMatchers("/api/v1/admin/**").hasAuthority(Authority.ADMIN_ACCESS.name())
                        .anyRequest().authenticated())
                .exceptionHandling(exception ->
                        exception.authenticationEntryPoint(jsonAuthenticationEntryPoint))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        // QR로 스캔한 방문객이 어떤 화면/출처에서 열든 호출 가능해야 하는 보상 처리 API는 모든 출처를 허용
        CorsConfiguration rewardCorsConfiguration = new CorsConfiguration();
        rewardCorsConfiguration.setAllowedOrigins(List.of("*"));
        rewardCorsConfiguration.setAllowedMethods(List.of("GET", "POST", "OPTIONS"));
        rewardCorsConfiguration.setAllowedHeaders(List.of("*"));
        rewardCorsConfiguration.setAllowCredentials(false);
        source.registerCorsConfiguration("/api/v1/stamp-tours/reward", rewardCorsConfiguration);

        // 나머지 API는 등록된 프론트엔드 출처만 허용
        CorsConfiguration defaultCorsConfiguration = new CorsConfiguration();
        defaultCorsConfiguration.setAllowedOrigins(ALLOWED_ORIGINS);
        defaultCorsConfiguration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        defaultCorsConfiguration.setAllowedHeaders(List.of("*"));
        defaultCorsConfiguration.setAllowCredentials(true);
        source.registerCorsConfiguration("/**", defaultCorsConfiguration);

        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
