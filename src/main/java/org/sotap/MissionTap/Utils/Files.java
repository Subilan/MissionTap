package org.sotap.MissionTap.Utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public final class Files {
    public static String cwd;
    public static FileConfiguration config;
    public static FileConfiguration dailyMissions;
    public static FileConfiguration weeklyMissions;
    public static FileConfiguration DailyMissions;
    public static FileConfiguration WeeklyMissions;
    public static FileConfiguration SpecialMissions;
    public static FileConfiguration meta;

    public static File getFile(File folder, String name) {
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

    public static FileConfiguration load(String path, String name) {
        return YamlConfiguration.loadConfiguration(getFile(
                new File(path.replace(path.length() == 1 ? "." : "./", path.length() == 1 ? cwd : cwd + "/")), name));
    }

    public static boolean isEmptyConfiguration(ConfigurationSection config) {
        if (config == null)
            return true;
        return config.getKeys(false).size() == 0;
    }

    public static FileConfiguration loadPlayer(UUID u) {
        return load("./playerdata", u.toString() + ".yml");
    }

    public static FileConfiguration loadMissionFor(UUID u) {
        return load("./generated/player", u.toString() + ".yml");
    }

    public static void save(FileConfiguration data, String targetFile) {
        try {
            data.save(targetFile.replace("./", cwd + "/"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void savePlayer(FileConfiguration data, UUID u) {
        save(data, "./playerdata/" + u.toString() + ".yml");
    }

    public static void saveConfig() {
        save(config, "./config.yml");
    }

    public static void savePlayerMission(FileConfiguration data, UUID u) {
        save(data, "./generated/player/" + u.toString() + ".yml");
    }

    public static void saveMeta() {
        save(meta, "./generated/meta.yml");
    }

    /**
     * 获取指定类型的任务源文件
     * @param type 类型
     * @return 源文件的 FC 实例
     */
    public static FileConfiguration getMissions(String type) {
        switch (type) {
            case "daily":
                return dailyMissions;
            case "weekly":
                return weeklyMissions;
            case "special":
                return SpecialMissions;
            default:
                return null;
        }
    }
    
    public static ConfigurationSection getGeneratedMissions(String type) {
        switch (type) {
            case "daily":
                return DailyMissions.getConfigurationSection("daily");
            case "weekly":
                return WeeklyMissions.getConfigurationSection("weekly");
            case "special":
                return SpecialMissions;
            default:
                return null;
        }
    }

    public static FileConfiguration getGeneratedMissionFile(String type) {
        switch (type) {
            case "daily":
                return DailyMissions;
            case "weekly":
                return WeeklyMissions;
            case "special":
                return SpecialMissions;
            default:
                return null;
        }
    }

    public static List<File> getSubfileList(File directory) {
        if (directory == null)
            return null;
        if (!directory.isDirectory())
            return null;
        List<File> files = new ArrayList<>();
        for (File f : directory.listFiles()) {
            if (f.isFile()) {
                files.add(f);
            }
        }
        return files;
    }

    /**
     * 获取有记录的所有玩家的 UUID 和其对应的 FC 实例所组成的 Map
     * @return Map(String,FC)
     */
    public static Map<String, FileConfiguration> getAllPlayerdata() {
        Map<String, FileConfiguration> result = new HashMap<>();
        for (File f : getSubfileList(new File(cwd + "/playerdata"))) {
            result.put(f.getName().replace(".yml", ""), YamlConfiguration.loadConfiguration(f));
        }
        return result;
    }

    /**
     * 获取有记录的所有玩家的 UUID
     * @return UUID 的 List
     */
    public static List<UUID> getAllPlayerUUID() {
        List<UUID> result = new ArrayList<>();
        try {
            for (File f : getSubfileList(new File(cwd + "/playerdata"))) {
                result.add(UUID.fromString(f.getName().replace(".yml", "")));
            }
            return result;
        } catch (NullPointerException e) {
            return null;
        }
    }

    public static FileConfiguration getPlayerMissions(UUID u) {
        return load("./generated/player", u.toString() + ".yml");
    }

    public static Map<String, FileConfiguration> getAllPlayerMissions() {
        Map<String, FileConfiguration> result = new HashMap<>();
        for (File f : getSubfileList(new File(cwd + "/generated/player"))) {
            result.put(f.getName().replace(".yml", ""), YamlConfiguration.loadConfiguration(f));
        }
        return result;
    }
}
