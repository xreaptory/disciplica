package com.disciplica.server.config;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.OrRequestMatcher;

import com.nimbusds.jose.jwk.source.ImmutableSecret;

@Configuration
@EnableWebSecurity
@EnableConfigurationProperties({JwtProperties.class, GoogleProperties.class})
public class SecurityConfig {
    @Bean
    @Order(0)
    SecurityFilterChain publicSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
                .securityMatcher(new OrRequestMatcher(
                        antMatcher("/"),
                        antMatcher("/status"),
                        antMatcher("/healthz"),
                        antMatcher("/auth/**"),
                        antMatcher("/ws/**"),
                        antMatcher("/actuator/health")
                ))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll())
                .build();
    }

    @Bean
    @Order(1)
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth -> oauth.jwt(jwt -> {
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
