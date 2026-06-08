package com.disciplica.server.config;

import jakarta.servlet.http.HttpServletResponse;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
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
 * Application security configuration.
 *
 * <p>Design decisions:
 * <ul>
 *   <li>No {@code oauth2ResourceServer()} — that DSL adds {@code BearerTokenAuthenticationFilter}
 *       which returns 401 before {@code AuthorizationFilter} can apply {@code permitAll()}.
 *       JWT validation is done by our own {@link JwtAuthFilter}.</li>
 *   <li>No path-matcher-based permit rules — in Spring Security 6.3 with Spring MVC on the
 *       classpath, {@code AntPathRequestMatcher} objects passed to
 *       {@code authorizeHttpRequests().requestMatchers()} may be silently wrapped by
 *       {@code MvcRequestHandlerProvider} and fail to match paths.  We use
 *       {@code anyRequest().access()} with a raw {@code getRequestURI()} string check instead.</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
@EnableConfigurationProperties({JwtProperties.class, GoogleProperties.class})
public class SecurityConfig {

    /**
     * Returns {@code true} for every URI that must be publicly accessible without a JWT.
     * This is evaluated against {@link jakarta.servlet.http.HttpServletRequest#getRequestURI()},
     * which is the raw path (no query string) relative to the web application root.
     */
    private static boolean isPublicUri(String uri) {
        return uri.equals("/")
                || uri.equals("/status")
                || uri.equals("/healthz")
                || uri.equals("/actuator/health")
                || uri.startsWith("/auth/")
                || uri.equals("/auth")
                || uri.startsWith("/ws/")
                || uri.equals("/ws");
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, JwtDecoder jwtDecoder) throws Exception {
        JwtAuthFilter jwtAuthFilter = new JwtAuthFilter(jwtDecoder);
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Run JWT validation before Spring Security's own auth filters.
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        // Use a raw URI string check to avoid AntPathMatcher / MvcRequestMatcher
                        // path-resolution issues with Spring MVC on the classpath.
                        .anyRequest().access((authentication, context) -> {
                            String uri = context.getRequest().getRequestURI();
                            if (isPublicUri(uri)) {
                                // Public endpoint — allow unconditionally.
                                return new org.springframework.security.authorization.AuthorizationDecision(true);
                            }
                            // Protected endpoint — must be authenticated (not anonymous).
                            var auth2 = authentication.get();
                            boolean authenticated = auth2 != null
                                    && auth2.isAuthenticated()
                                    && !(auth2 instanceof AnonymousAuthenticationToken);
                            return new org.springframework.security.authorization.AuthorizationDecision(authenticated);
                        }))
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
