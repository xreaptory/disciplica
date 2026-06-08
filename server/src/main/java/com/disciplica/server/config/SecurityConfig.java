package com.disciplica.server.config;

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

import com.disciplica.server.security.JwtAuthFilter;
import com.nimbusds.jose.jwk.source.ImmutableSecret;

/**
 * Spring Security configuration.
 *
 * <p>Authorization is intentionally NOT handled here.  After multiple failed attempts to
 * make Spring Security path matchers work with Spring MVC on the classpath
 * ({@code AntPathRequestMatcher}, {@code web.ignoring()}, {@code access()} lambda), we moved
 * authorization to {@link com.disciplica.server.security.AuthInterceptor} — a Spring MVC
 * {@code HandlerInterceptor} that runs at the DispatcherServlet level.
 *
 * <p>Spring Security's role here is only:
 * <ol>
 *   <li>Validate bearer JWT via {@link JwtAuthFilter} and populate the SecurityContext.</li>
 *   <li>Disable CSRF (stateless API).</li>
 *   <li>Use stateless sessions.</li>
 *   <li>Permit ALL requests at the Spring Security layer ({@code anyRequest().permitAll()}) so
 *       nothing is blocked before reaching the interceptor.</li>
 * </ol>
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
                // Validate JWT tokens and populate SecurityContext.  Must run before
                // UsernamePasswordAuthenticationFilter so that the auth context is available
                // to downstream filters and the interceptor.
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                // Allow every request through Spring Security.
                // Authorization is enforced in AuthInterceptor (Spring MVC layer).
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
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
