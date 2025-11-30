package config;

import java.io.*;
import java.util.Properties;

/**
 * Класс для работы с настройками приложения, загружаемыми из файла config.properties.
 * Настройки включают в себя имя пользователя, пароль, группу пользователя, режим отладки и режим автотестов.
 */
public class Settings {
     private String username;
    private String password;
    private String userGroup;
    private boolean debugMode;
    private boolean autoTestMode;
    private final Properties properties;
    private final String configFile = "config.properties";

    public Settings() {
        properties = new Properties();
        loadSettings();
    }

    /**
     * Загружает настройки из файла config.properties.
     */
    private void loadSettings() {
        try (InputStream input = new FileInputStream(configFile)) {
            properties.load(input);
            username = properties.getProperty("user.name");
            password = properties.getProperty("user.password");
            userGroup = properties.getProperty("user.group");
            debugMode = Boolean.parseBoolean(properties.getProperty("debug.mode"));
            autoTestMode = Boolean.parseBoolean(properties.getProperty("auto.test.mode"));
        } catch (IOException e) {
            username = "guest";
            password = "password";
            userGroup = "user";
            debugMode = false;
            autoTestMode = false;
        }
    }

    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getUserGroup() { return userGroup; }
    public boolean isDebugMode() { return debugMode; }
    public boolean isAutoTestMode() { return autoTestMode; }
    public boolean isRoot() { return "root".equals(userGroup); }
}