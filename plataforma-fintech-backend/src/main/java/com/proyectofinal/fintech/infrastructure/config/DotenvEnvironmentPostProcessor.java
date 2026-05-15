package com.proyectofinal.fintech.infrastructure.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

/**
 * Loads a local {@code .env} file into the Spring {@link ConfigurableEnvironment}
 * before any bean is created, so {@code ${VAR}} placeholders in application.yml
 * resolve without the developer having to export the variables manually.
 *
 * <p>Resolution order (first existing file wins):
 * <ol>
 *   <li>{@code ./.env} — when running from the backend module directory</li>
 *   <li>{@code ./plataforma-fintech-backend/.env} — when running from the monorepo root</li>
 * </ol>
 *
 * <p>Real OS environment variables and JVM system properties keep priority:
 * this property source is added last, so it only fills values that are not
 * already defined elsewhere. The {@code .env} file is git-ignored and never shipped.
 */
public class DotenvEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final String PROPERTY_SOURCE_NAME = "dotenvFile";

    private static final List<String> CANDIDATE_PATHS = List.of(
            ".env",
            "plataforma-fintech-backend/.env");

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        for (String candidate : CANDIDATE_PATHS) {
            Path path = Path.of(candidate);
            if (Files.isRegularFile(path)) {
                Map<String, Object> values = parse(path);
                if (!values.isEmpty()) {
                    environment.getPropertySources()
                            .addLast(new MapPropertySource(PROPERTY_SOURCE_NAME, values));
                }
                return;
            }
        }
    }

    private Map<String, Object> parse(Path path) {
        Map<String, Object> values = new LinkedHashMap<>();
        try {
            for (String rawLine : Files.readAllLines(path, StandardCharsets.UTF_8)) {
                String line = rawLine.strip();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                int eq = line.indexOf('=');
                if (eq <= 0) {
                    continue;
                }
                String key = line.substring(0, eq).strip();
                String value = stripQuotes(line.substring(eq + 1).strip());
                if (!key.isEmpty()) {
                    values.put(key, value);
                }
            }
        } catch (IOException ex) {
            // A missing or unreadable .env is not fatal: defaults in application.yml apply.
            return Map.of();
        }
        return values;
    }

    private String stripQuotes(String value) {
        if (value.length() >= 2) {
            char first = value.charAt(0);
            char last = value.charAt(value.length() - 1);
            if ((first == '"' && last == '"') || (first == '\'' && last == '\'')) {
                return value.substring(1, value.length() - 1);
            }
        }
        return value;
    }
}
