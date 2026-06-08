package com.disciplica.server.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Lightweight JWT validation filter.
 *
 * <p>This replaces Spring Security's {@code BearerTokenAuthenticationFilter} (which is added by
 * {@code oauth2ResourceServer()}) so that we can apply {@code permitAll()} in
 * {@code authorizeHttpRequests} without the bearer-token filter intercepting the request first
 * and returning 401 before path-level permits are evaluated.
 *
 * <p>Behaviour:
 * <ul>
 *   <li>If the request contains a valid {@code Authorization: Bearer …} header, the JWT is decoded
 *       and an authenticated {@link JwtAuthenticationToken} is stored in the
 *       {@link SecurityContextHolder}.</li>
 *   <li>If the header is absent or the token is invalid/expired the filter does nothing — the
 *       {@link org.springframework.security.web.access.intercept.AuthorizationFilter} will
 *       reject the request if the endpoint requires authentication.</li>
 * </ul>
 */
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtDecoder jwtDecoder;

    public JwtAuthFilter(JwtDecoder jwtDecoder) {
        this.jwtDecoder = jwtDecoder;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                Jwt jwt = jwtDecoder.decode(token);
                // Pass an empty authority list — the app uses JWT claims directly.
                // Two-arg constructor calls setAuthenticated(true) internally.
                JwtAuthenticationToken auth = new JwtAuthenticationToken(jwt, List.of());
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (Exception ignored) {
                // Invalid / expired token — proceed without authentication.
                // AuthorizationFilter will reject the request if the endpoint requires auth.
            }
        }
        chain.doFilter(request, response);
    }
}
