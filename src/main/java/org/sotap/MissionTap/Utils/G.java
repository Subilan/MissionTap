package org.sotap.MissionTap.Utils;

import org.bukkit.ChatColor;

public final class G {
    public final static String SUCCESS = "&r[&aSUCCESS&r] ";
    public final static String WARN = "&r[&eWARN&r] ";
    public final static String FAILED = "&r[&cFAILED&r] ";
    public final static String INFO = "&r[&bINFO&r] ";

    /**
     * 调用 ChatColor 对 '&' 进行转义
     * 
     * @param message 要转义的字符串
     * @return 转义后的字符串
     */
    public static String translateColor(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
