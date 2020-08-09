package org.sotap.MissionTap.Utils;

import java.util.logging.Logger;

import org.bukkit.ChatColor;

public final class LogUtil {
    public final static String SUCCESS = "&r[&a成功&r] ";
    public final static String WARN = "&r[&e警告&r] ";
    public final static String FAILED = "&r[&c失败&r] ";
    public final static String INFO = "&r[&b提示&r] ";
    public static Logger origin;
    
    public static String translateColor(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static void success(String message) {
        origin.info(translateColor(SUCCESS + message));
    }

    public static void warn(String message) {
        origin.info(translateColor(WARN + message));
    }

    public static void failed(String message) {
        origin.info(translateColor(FAILED + message));
    }

    public static void info(String message) {
        origin.info(translateColor(INFO + message));
    }
}