package org.sotap.MissionTap;

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class MissionTap extends JavaPlugin {

    public void log(String message) {
        getLogger().info(message);
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        load("special-missions.yml");
        load("daily-missions.yml");
        load("weekly-missions.yml");
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

    public FileConfiguration getMissions(String type) {
        List<String> validMissionTypes = List.of("special", "daily", "weekly");
        if (!validMissionTypes.contains(type)) {
            return null;
        }
        return load(type + "-missions.yml");
    }
}