package com.disciplica.server.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Konfiguration für die WebSocket-Kommunikation (STOMP).
 * <p>
 * Aktiviert einen einfachen Nachrichten-Broker, über den z.&nbsp;B. der
 * Gruppen-Chat in Echtzeit verteilt wird.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * Richtet den Nachrichten-Broker ein: Nachrichten an {@code /topic}
     * werden an die Abonnenten verteilt, Nachrichten der Clients laufen über
     * das Präfix {@code /app} an die Anwendung.
     *
     * @param registry die Broker-Registrierung
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");
        registry.setApplicationDestinationPrefixes("/app");
    }

    /**
     * Registriert den WebSocket-Endpunkt {@code /ws}, über den sich Clients
     * verbinden.
     *
     * @param registry die Endpunkt-Registrierung
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws").setAllowedOriginPatterns("*");
    }
}
