package com.disciplica.server.config;

import jakarta.servlet.http.HttpServletResponse;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.disciplica.server.security.JwtAuthFilter;
import com.nimbusds.jose.jwk.source.ImmutableSecret;

/**
 * Application security configuration.
 *
 * <p>We intentionally do NOT use {@code oauth2ResourceServer()} here.  That DSL adds
 * {@code BearerTokenAuthenticationFilter} to the chain, which eagerly returns 401 for any
 * request that lacks a bearer token — including public endpoints — before
 * {@code AuthorizationFilter} has a chance to apply {@code permitAll()}.
 *
 * <p>Instead, JWT validation is handled by {@link JwtAuthFilter}, a lightweight
 * {@code OncePerRequestFilter} that runs before Spring Security's authorization checks.
 * Public paths receive {@code permitAll()} and are never touched by JWT logic.
 */
@Configuration
@EnableWebSecurity
@EnableConfigurationProperties({JwtProperties.class, GoogleProperties.class})
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, JwtDecoder jwtDecoder) throws Exception {
        JwtAuthFilter jwtAuthFilter = new JwtAuthFilter(jwtDecoder);
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Run our custom JWT filter before Spring's own auth filters.
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        // Public paths — no token needed.
                        .requestMatchers(
                                new AntPathRequestMatcher("/"),
                                new AntPathRequestMatcher("/status"),
                                new AntPathRequestMatcher("/healthz"),
                                new AntPathRequestMatcher("/actuator/health"),
                                new AntPathRequestMatcher("/auth/**"),
                                new AntPathRequestMatcher("/ws/**")
                        ).permitAll()
                        // Everything else requires a valid JWT.
                        .anyRequest().authenticated())
                // Return a plain 401 JSON body instead of redirecting to /login.
                .exceptionHandling(e -> e
                        .authenticationEntryPoint((req, res, ex) -> {
                            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            res.setContentType("application/json");
                            res.getWriter().write("{\"error\":\"Unauthorized\"}");
                        }))
                .build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
    }

    @Bean
    SecretKey jwtSecretKey(JwtProperties properties) {
        byte[] keyBytes = properties.secret().getBytes(java.nio.charset.StandardCharsets.UTF_8);
        return new SecretKeySpec(keyBytes, "HmacSHA256");
    }

    @Bean
    JwtEncoder jwtEncoder(SecretKey jwtSecretKey) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(jwtSecretKey));
    }

    @Bean
    JwtDecoder jwtDecoder(SecretKey jwtSecretKey) {
        return NimbusJwtDecoder.withSecretKey(jwtSecretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }
}
