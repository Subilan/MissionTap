package org.sotap.MissionTap;

import java.io.File;
import java.io.IOException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class MissionTap extends JavaPlugin {
    public FileConfiguration specialMissions;
    public FileConfiguration dailyMissions;
    public FileConfiguration weeklyMissions;

    public void log(String message) {
        getLogger().info(message);
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        reloadMissions();
        log(G.translateColor(G.SUCCESS + "The plugin has been &aenabled&r."));
    }

    @Override
    public void onDisable() {
        log(G.translateColor(G.SUCCESS + "The plugin has been &cdisabled&r."));
    }

    public FileConfiguration load(String filename) {
        File folder = getDataFolder();
        File file = new File(folder, filename);
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
        return YamlConfiguration.loadConfiguration(file);
    }

    public void reloadMissions() {
        specialMissions = load("special-missions.yml");
        dailyMissions = load("daily-missions.yml");
        weeklyMissions = load("weekly-missions.yml");
    }
}