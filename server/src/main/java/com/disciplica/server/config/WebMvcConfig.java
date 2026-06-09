package com.disciplica.server.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.disciplica.server.security.AuthInterceptor;

/**
 * Spring-MVC-Konfiguration, die den {@link AuthInterceptor} registriert.
 * <p>
 * Über diesen Interceptor wird die Zugriffsberechtigung für eingehende
 * Anfragen geprüft (siehe {@link SecurityConfig}).
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    /**
     * Erzeugt die Konfiguration mit dem zu registrierenden Interceptor.
     *
     * @param authInterceptor der Interceptor zur Berechtigungsprüfung
     */
    public WebMvcConfig(AuthInterceptor authInterceptor) {
        this.authInterceptor = authInterceptor;
    }

    /**
     * Fügt den {@link AuthInterceptor} in die Verarbeitungskette aller
     * Anfragen ein.
     *
     * @param registry die Registrierung, in die der Interceptor eingetragen
     *                 wird
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor);
    }
}
