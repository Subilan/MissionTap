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
    public static FileConfiguration speicalMissions;
    public static FileConfiguration dailyMissions;
    public static FileConfiguration weeklyMissions;
    public static FileConfiguration DailyMissions;
    public static FileConfiguration WeeklyMissions;

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
        return YamlConfiguration
                .loadConfiguration(getFile(new File(path.replace(path.length() == 1 ? "." : "./",
                        path.length() == 1 ? cwd : cwd + "/")), name));
    }

    public static boolean isEmptyConfiguration(ConfigurationSection config) {
        if (config == null)
            return true;
        return config.getKeys(false).size() == 0;
    }

    public static FileConfiguration loadPlayer(UUID u) {
        return load("./playerdata", u.toString() + ".yml");
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

    public static ConfigurationSection getGeneratedMissions(String type) {
        switch (type) {
            case "daily":
                return DailyMissions.getConfigurationSection("daily");
            case "weekly":
                return WeeklyMissions.getConfigurationSection("weekly");
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

    public static Map<String, FileConfiguration> getAllPlayerdata() {
        Map<String, FileConfiguration> result = new HashMap<>();
        for (File f : getSubfileList(new File(cwd + "/playerdata"))) {
            result.put(f.getName(), YamlConfiguration.loadConfiguration(f));
        }
        return result;
    }

    public static List<UUID> getAllPlayerUUID() {
        List<UUID> result = new ArrayList<>();
        for (File f : getSubfileList(new File(cwd + "/playerdata"))) {
            result.add(UUID.fromString(f.getName().replace(".yml", "")));
        }
        return result;
    }
}
