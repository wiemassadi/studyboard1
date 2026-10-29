package com.wiem.studyboard;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * Version information read from app.properties.
 * Maven replaces the ${...} placeholders at build time; Jenkins provides the build number.
 */
public final class AppInfo {

    private static final Properties PROPS = load();

    private AppInfo() {
    }

    public static String version() {
        return PROPS.getProperty("app.version", "unknown");
    }

    public static String build() {
        return PROPS.getProperty("app.build", "local");
    }

    public static String builtAt() {
        return PROPS.getProperty("app.built", "unknown");
    }

    /** Machine name; inside Docker, this is the container id. */
    public static String hostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (IOException e) {
            return "unknown";
        }
    }

    private static Properties load() {
        Properties props = new Properties();
        try (InputStream in = AppInfo.class.getResourceAsStream("/app.properties")) {
            if (in != null) {
                props.load(new InputStreamReader(in, StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            // Keep the default values: the page must still render.
        }
        return props;
    }
}
