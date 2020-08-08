package org.sotap.MissionTap.Utils;

import org.bukkit.ChatColor;

public final class Logger {
    public final static String SUCCESS = "&r[&aSUCCESS&r] ";
    public final static String WARN = "&r[&eWARN&r] ";
    public final static String FAILED = "&r[&cFAILED&r] ";
    public final static String INFO = "&r[&bINFO&r] ";
    
    public static String translateColor(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}