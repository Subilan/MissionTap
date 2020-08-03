package org.sotap.MissionTap.Utils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public final class G {
    public final static String SUCCESS = "&r[&aSUCCESS&r] ";
    public final static String WARN = "&r[&eWARN&r] ";
    public final static String FAILED = "&r[&cFAILED&r] ";
    public final static String INFO = "&r[&bINFO&r] ";
    public static String cwd;
    public static Boolean crawl;
    public static FileConfiguration config;

    /**
     * 调用 ChatColor 对 '&' 进行转义
     * 
     * @param message 要转义的字符串
     * @return 转义后的字符串
     */
    public static String translateColor(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static File getFile(File path, String name) {
        File folder = path == null ? new File(cwd) : path;
        File file = new File(folder, name);
        if (!folder.exists()) {
            folder.mkdir();
        }
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    public static FileConfiguration getYaml(String path, String name) {
        return YamlConfiguration.loadConfiguration(getFile(new File(path), name));
    }

    public static FileConfiguration load(String name) {
        return getYaml(cwd, name);
    }

    public static FileConfiguration loadPlayer(UUID uuid) {
        return getYaml(cwd + "/playerdata", uuid.toString() + ".yml");
    }

    public static void save(FileConfiguration config, String filename) {
        try {
            config.save(new File(cwd + "/" + filename + ".yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void savePlayer(FileConfiguration data, UUID uuid) {
        try {
            data.save(new File(cwd + "/playerdata/" + uuid.toString() + ".yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static SimpleDateFormat getDateFormat() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }

    public static String getDateString(Long stamp) {
        return getDateFormat().format(new Date(stamp));
    }

    public static boolean isOnlinePlayer(UUID uuid) {
        return Bukkit.getPlayer(uuid) != null;
    }
}
