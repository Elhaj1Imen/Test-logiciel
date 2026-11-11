package com.celectronix.utils;

import java.io.InputStream;
import java.util.Properties;

/** Lecture de config.properties (surchargeable par -Dcle=valeur). */
public final class Config {

    private static final Properties PROPS = new Properties();

    static {
        try (InputStream in = Config.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (in != null) {
                PROPS.load(in);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Impossible de lire config.properties", e);
        }
    }

    private Config() { }

    public static String get(String key) {
        String fromSystem = System.getProperty(key);
        return fromSystem != null ? fromSystem : PROPS.getProperty(key);
    }

    public static String baseUrl() {
        String url = get("base.url");
        return url.endsWith("/") ? url : url + "/";
    }

    public static String browser() {
        return get("browser").trim().toLowerCase();
    }

    public static boolean headless() {
        return Boolean.parseBoolean(get("headless"));
    }

    public static int timeout() {
        return Integer.parseInt(get("timeout").trim());
    }
}
