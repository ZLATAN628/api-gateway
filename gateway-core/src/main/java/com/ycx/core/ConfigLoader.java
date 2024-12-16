package com.ycx.core;

import com.ycx.common.utils.PropertiesUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

@Slf4j
public class ConfigLoader {
    private static final String CONFIG_FILE = "gateway.properties";
    private static final String ENV_PREFIX = "GATEWAY_";
    private static final String JVM_PREFIX = "gateway.";

    private static final ConfigLoader INSTANCE = new ConfigLoader();

    private ConfigLoader() {
    }

    public static ConfigLoader getInstance() {
        return INSTANCE;
    }

    private Config config;

    public static Config getConfig() {
        return INSTANCE.config;
    }

    public Config load(String[] args) {
        config = new Config();

        loadFromConfigFile();

        loadFromEnv();

        loadFromJvm();

        loadFromArgs(args);

        return config;
    }

    private void loadFromArgs(String[] args) {
        if (args != null && args.length > 0) {
            Properties properties = new Properties();
            for (String arg : args) {
                if (arg.startsWith("--") && arg.contains("=")) {
                    properties.put(arg.substring(2, arg.indexOf("=")), arg.substring(arg.indexOf("=") + 1));
                }
            }
            PropertiesUtils.properties2Object(properties, config);
        }
    }

    private void loadFromJvm() {
        Properties properties = System.getProperties();
        PropertiesUtils.properties2Object(properties, config, JVM_PREFIX);
    }

    private void loadFromEnv() {
        Map<String, String> env = System.getenv();
        Properties properties = new Properties();
        properties.putAll(env);
        PropertiesUtils.properties2Object(properties, config, ENV_PREFIX);
    }

    private void loadFromConfigFile() {
        try (InputStream resource = ConfigLoader.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (resource != null) {
                Properties properties = new Properties();
                properties.load(resource);
                PropertiesUtils.properties2Object(properties, config);
            }
        } catch (IOException e) {
            log.warn("load config file {} error", e.getMessage());
        }

    }
}
