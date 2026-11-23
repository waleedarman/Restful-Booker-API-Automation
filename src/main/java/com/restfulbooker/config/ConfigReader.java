package com.restfulbooker.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class ConfigReader {
    private static final String CONFIG_FILE = "config.properties";
    private static final Properties PROPERTIES = loadProperties();

    private ConfigReader() {
    }

    public static String getRequiredProperty(String key) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Configuration property key must not be blank");
        }

        String value = PROPERTIES.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Required configuration property is missing or blank: " + key);
        }
        return value.trim();
    }

    private static Properties loadProperties() {
        Properties properties = new Properties();
        ClassLoader classLoader = ConfigReader.class.getClassLoader();
        try (InputStream input = classLoader.getResourceAsStream(CONFIG_FILE)) {
            if (input == null) {
                throw new IllegalStateException("Required configuration file is missing from the classpath: " + CONFIG_FILE);
            }
            properties.load(input);
            return properties;
        } catch (IOException exception) {
            throw new IllegalStateException("Could not load configuration file from the classpath: " + CONFIG_FILE, exception);
        }
    }
}
