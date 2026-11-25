package com.restfulbooker.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class ConfigReader {
    private static final String CONFIG_FILE = "config.properties";
    private static final String EXAMPLE_CONFIG_FILE = "config.example.properties";
    private static final Properties PROPERTIES = loadProperties();

    private ConfigReader() {
    }

    public static String getRequiredProperty(String key) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Configuration property key must not be blank");
        }

        String environmentValue = System.getenv(environmentVariableFor(key));
        if (environmentValue != null && !environmentValue.isBlank()) {
            return environmentValue.trim();
        }

        String value = PROPERTIES.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Required configuration property is missing or blank: " + key
                    + " (or environment variable " + environmentVariableFor(key) + ")");
        }
        if (value.startsWith("your-")) {
            throw new IllegalStateException("Set " + environmentVariableFor(key)
                    + " or provide a local value for configuration property: " + key);
        }
        return value.trim();
    }

    private static String environmentVariableFor(String key) {
        return switch (key) {
            case "base.url" -> "RESTFULBOOKER_BASE_URL";
            case "auth.username" -> "RESTFULBOOKER_USERNAME";
            case "auth.password" -> "RESTFULBOOKER_PASSWORD";
            default -> throw new IllegalArgumentException("Unsupported configuration property: " + key);
        };
    }

    private static Properties loadProperties() {
        Properties properties = new Properties();
        ClassLoader classLoader = ConfigReader.class.getClassLoader();
        InputStream configInput = classLoader.getResourceAsStream(CONFIG_FILE);
        String selectedConfig = CONFIG_FILE;
        if (configInput == null) {
            configInput = classLoader.getResourceAsStream(EXAMPLE_CONFIG_FILE);
            selectedConfig = EXAMPLE_CONFIG_FILE;
        }

        try (InputStream input = configInput) {
            if (input == null) {
                throw new IllegalStateException("Required configuration file is missing from the classpath: "
                        + CONFIG_FILE + " and " + EXAMPLE_CONFIG_FILE);
            }
            properties.load(input);
            return properties;
        } catch (IOException exception) {
            throw new IllegalStateException("Could not load configuration file from the classpath: "
                    + selectedConfig, exception);
        }
    }
}
