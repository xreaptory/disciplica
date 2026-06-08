package com.disciplica.server.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Spring MVC interceptor that enforces authentication on protected endpoints.
 *
 * <p>This runs AFTER the {@code DispatcherServlet} receives the request, completely
 * outside Spring Security's filter chain.  Public paths are whitelisted by URI prefix;
 * all other paths require a valid JWT (placed in the SecurityContext by
 * {@link JwtAuthFilter}).
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(AuthInterceptor.class);

    private static boolean isPublicUri(String uri) {
        return uri == null
                || uri.equals("/")
                || uri.equals("/status")
                || uri.equals("/healthz")
                || uri.equals("/actuator/health")
                || uri.equals("/auth")
                || uri.startsWith("/auth/")
                || uri.equals("/ws")
                || uri.startsWith("/ws/");
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        String requestUri  = request.getRequestURI();
        String servletPath = request.getServletPath();
        String pathInfo    = request.getPathInfo();
        String contextPath = request.getContextPath();

        // Determine the effective path for matching.
        // Prefer servletPath because it strips the context-path prefix.
        String effectivePath = (servletPath != null && !servletPath.isEmpty()) ? servletPath : requestUri;

        log.info("[AuthInterceptor] method={} requestURI={} servletPath={} pathInfo={} contextPath={} effectivePath={}",
                request.getMethod(), requestUri, servletPath, pathInfo, contextPath, effectivePath);

        if (isPublicUri(requestUri) || isPublicUri(effectivePath)) {
            log.info("[AuthInterceptor] PUBLIC path — allowing through");
            return true;
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean authenticated = auth != null
                && auth.isAuthenticated()
                && !(auth instanceof AnonymousAuthenticationToken);

        log.info("[AuthInterceptor] PROTECTED path — authenticated={} principal={}",
                authenticated, auth == null ? "null" : auth.getClass().getSimpleName());

        if (!authenticated) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"error\":\"Unauthorized\",\"path\":\"" + requestUri + "\"}");
            return false;
        }
        return true;
    }
}
