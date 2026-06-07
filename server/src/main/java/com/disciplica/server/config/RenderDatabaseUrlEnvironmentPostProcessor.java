package com.disciplica.server.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class RenderDatabaseUrlEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final String PROPERTY_SOURCE_NAME = "renderDatabaseUrl";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String databaseUrl = firstPresent(
                environment.getProperty("DATABASE_URL"),
                environment.getProperty("SPRING_DATASOURCE_URL")
        );

        if (databaseUrl == null || !isPostgresUrl(databaseUrl)) {
            return;
        }

        URI uri = URI.create(databaseUrl);
        Map<String, Object> properties = new HashMap<>();
        properties.put("spring.datasource.url", toJdbcUrl(uri));

        Credentials credentials = credentials(uri);
        if (credentials.username() != null && environment.getProperty("DATABASE_USERNAME") == null) {
            properties.put("spring.datasource.username", credentials.username());
        }
        if (credentials.password() != null && environment.getProperty("DATABASE_PASSWORD") == null) {
            properties.put("spring.datasource.password", credentials.password());
        }

        environment.getPropertySources().addFirst(new MapPropertySource(PROPERTY_SOURCE_NAME, properties));
    }

    private static boolean isPostgresUrl(String value) {
        return value.startsWith("postgres://") || value.startsWith("postgresql://");
    }

    private static String toJdbcUrl(URI uri) {
        StringBuilder jdbcUrl = new StringBuilder("jdbc:postgresql://");
        jdbcUrl.append(uri.getHost());
        if (uri.getPort() > 0) {
            jdbcUrl.append(':').append(uri.getPort());
        }
        jdbcUrl.append(uri.getRawPath() == null || uri.getRawPath().isBlank() ? "/" : uri.getRawPath());
        if (uri.getRawQuery() != null && !uri.getRawQuery().isBlank()) {
            jdbcUrl.append('?').append(uri.getRawQuery());
        }
        return jdbcUrl.toString();
    }

    private static Credentials credentials(URI uri) {
        String userInfo = uri.getRawUserInfo();
        if (userInfo == null || userInfo.isBlank()) {
            return new Credentials(null, null);
        }

        String[] parts = userInfo.split(":", 2);
        String username = decode(parts[0]);
        String password = parts.length > 1 ? decode(parts[1]) : null;
        return new Credentials(username, password);
    }

    private static String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    private static String firstPresent(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first;
        }
        if (second != null && !second.isBlank()) {
            return second;
        }
        return null;
    }

    private record Credentials(String username, String password) {
    }
}
