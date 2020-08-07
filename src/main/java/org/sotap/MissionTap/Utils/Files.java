package org.sotap.MissionTap.Utils;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
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
        return YamlConfiguration.loadConfiguration(getFile(new File(path.replace(".", cwd)), name));
    }

    public static FileConfiguration loadPlayer(UUID u) {
        return load("./playerdata", u.toString() + ".yml");
    }

    public static void save(FileConfiguration data, String targetFile) {
        try {
            data.save(new File(targetFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void savePlayer(FileConfiguration data, UUID u) {
        save(data, "./playerdata/" + u.toString() + ".yml");
    }

    public static FileConfiguration getGeneratedMissions(String type) {
        switch (type) {
            case "daily":
                return DailyMissions;
            case "weekly":
                return WeeklyMissions;
            default:
                return null;
        }
    }
}
