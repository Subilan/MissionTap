package org.sotap.MissionTap.Utils;

import org.bukkit.ChatColor;

public final class Logger {
    public final static String SUCCESS = "&r[&a成功&r] ";
    public final static String WARN = "&r[&e警告&r] ";
    public final static String FAILED = "&r[&c失败&r] ";
    public final static String INFO = "&r[&b提示&r] ";
    
    public static String translateColor(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}