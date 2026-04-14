package com.exampleinyection.jwtgft.config;

import com.exampleinyection.jwtgft.security.BookstoreAccessDeniedHandler;
import com.exampleinyection.jwtgft.security.BookstoreAuthenticationEntryPoint;
import com.exampleinyection.jwtgft.security.JwtAuthenticationFilter;
import com.exampleinyection.jwtgft.security.LoginRateLimitFilter;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
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
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfig {

    private final boolean requireHttps;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final LoginRateLimitFilter loginRateLimitFilter;
    private final BookstoreAuthenticationEntryPoint authEntryPoint;
    private final BookstoreAccessDeniedHandler accessDeniedHandler;

    public SecurityConfig(
        @Value("${app.security.require-https:false}") boolean requireHttps,
        JwtAuthenticationFilter jwtAuthenticationFilter,
        LoginRateLimitFilter loginRateLimitFilter,
        BookstoreAuthenticationEntryPoint authEntryPoint,
        BookstoreAccessDeniedHandler accessDeniedHandler
    ) {
        this.requireHttps = requireHttps;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.loginRateLimitFilter = loginRateLimitFilter;
        this.authEntryPoint = authEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)
            .headers(headers -> headers.frameOptions(frame -> frame.disable()))
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(auth ->
                auth
                    .requestMatchers("/api/auth/login", "/api/auth/register", "/api/auth/refresh").permitAll()
                    .requestMatchers("/h2-console/**", "/actuator/health").permitAll()
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/books/**").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/books").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/api/books/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/api/books/**").hasRole("ADMIN")
                    .requestMatchers("/api/admin/**").hasRole("ADMIN")
                    .requestMatchers("/actuator/**").hasRole("ADMIN")
                    .requestMatchers("/api/orders/**").authenticated()
                    .requestMatchers("/api/users/**").authenticated()
                    .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(authEntryPoint)
                .accessDeniedHandler(accessDeniedHandler)
            )
            .addFilterBefore(loginRateLimitFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        if (requireHttps) {
            http.requiresChannel(channel -> channel.anyRequest().requiresSecure());
            http.headers(headers -> headers.httpStrictTransportSecurity(hsts ->
                hsts.includeSubDomains(true).maxAgeInSeconds(31_536_000)
            ));
        }

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:3000"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(false);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}

