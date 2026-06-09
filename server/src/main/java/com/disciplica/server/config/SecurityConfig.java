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
 * Konfiguration für Spring Security.
 *
 * <p>Die eigentliche Zugriffsberechtigung wird hier bewusst <em>nicht</em>
 * geprüft. Nach mehreren erfolglosen Versuchen, die Pfad-Vergleicher von
 * Spring Security zusammen mit Spring MVC zum Laufen zu bringen, wurde die
 * Berechtigungsprüfung in den
 * {@link com.disciplica.server.security.AuthInterceptor} verlagert – einen
 * Spring-MVC-{@code HandlerInterceptor}, der auf Ebene des DispatcherServlets
 * arbeitet.
 *
 * <p>Spring Security übernimmt hier nur folgende Aufgaben:
 * <ol>
 *   <li>Prüfen des Bearer-JWT über den {@link JwtAuthFilter} und Befüllen des
 *       SecurityContext.</li>
 *   <li>Deaktivieren von CSRF (zustandslose API).</li>
 *   <li>Verwenden zustandsloser Sitzungen.</li>
 *   <li>Durchlassen ALLER Anfragen auf Security-Ebene
 *       ({@code anyRequest().permitAll()}), damit nichts blockiert wird,
 *       bevor der Interceptor greift.</li>
 * </ol>
 */
@Configuration
@EnableWebSecurity
@EnableConfigurationProperties({JwtProperties.class, GoogleProperties.class})
public class SecurityConfig {

    /**
     * Baut die Filterkette von Spring Security auf: JWT-Prüfung, kein CSRF,
     * zustandslose Sitzungen und freier Durchlass auf Security-Ebene.
     *
     * @param http       Baukasten zum Konfigurieren der Web-Sicherheit
     * @param jwtDecoder Komponente zum Prüfen und Auslesen von JWT-Tokens
     * @return die fertige Sicherheits-Filterkette
     * @throws Exception wenn der Aufbau der Filterkette fehlschlägt
     */
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, JwtDecoder jwtDecoder) throws Exception {
        JwtAuthFilter jwtAuthFilter = new JwtAuthFilter(jwtDecoder);
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // JWT-Tokens prüfen und den SecurityContext befüllen. Muss vor dem
                // UsernamePasswordAuthenticationFilter laufen, damit der Anmeldekontext
                // für nachfolgende Filter und den Interceptor verfügbar ist.
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                // Jede Anfrage auf Security-Ebene durchlassen.
                // Die Berechtigung wird im AuthInterceptor (Spring-MVC-Ebene) erzwungen.
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .build();
    }

    /**
     * Stellt den Passwort-Encoder bereit, mit dem Passwörter gehasht und
     * geprüft werden (Argon2).
     *
     * @return der konfigurierte Passwort-Encoder
     */
    @Bean
    PasswordEncoder passwordEncoder() {
        return Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
    }

    /**
     * Erzeugt den geheimen Schlüssel zum Signieren und Prüfen der JWT-Tokens
     * aus dem konfigurierten Geheimnis.
     *
     * @param properties die JWT-Konfiguration mit dem geheimen Schlüsselwort
     * @return der abgeleitete geheime Schlüssel (HMAC-SHA256)
     */
    @Bean
    SecretKey jwtSecretKey(JwtProperties properties) {
        byte[] keyBytes = properties.secret().getBytes(java.nio.charset.StandardCharsets.UTF_8);
        return new SecretKeySpec(keyBytes, "HmacSHA256");
    }

    /**
     * Stellt die Komponente zum Erzeugen (Signieren) von JWT-Tokens bereit.
     *
     * @param jwtSecretKey der geheime Signaturschlüssel
     * @return der JWT-Encoder
     */
    @Bean
    JwtEncoder jwtEncoder(SecretKey jwtSecretKey) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(jwtSecretKey));
    }

    /**
     * Stellt die Komponente zum Prüfen und Auslesen von JWT-Tokens bereit.
     *
     * @param jwtSecretKey der geheime Signaturschlüssel
     * @return der JWT-Decoder (HMAC-SHA256)
     */
    @Bean
    JwtDecoder jwtDecoder(SecretKey jwtSecretKey) {
        return NimbusJwtDecoder.withSecretKey(jwtSecretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }
}
