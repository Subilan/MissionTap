package org.sotap.MissionTap;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public final class MissionTap extends JavaPlugin {
    public FileConfiguration specialMissions;
    public FileConfiguration dailyMissions;
    public FileConfiguration weeklyMissions;
    public FileConfiguration latestMissions;

    public void log(String message) {
        getLogger().info(message);
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.specialMissions = load("special-missions.yml");
        this.dailyMissions = load("daily-missions.yml");
        this.weeklyMissions = load("weekly-missions.yml");
        this.latestMissions = load("latest-missions.yml");
        updateMissions();
        @SuppressWarnings("unused")
        BukkitTask timer = new Timer(this).runTaskTimer(this, 0, 20);
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

    public void generateRandomMissions(String type) {
        if (!List.of("daily", "weekly").contains(type)) {
            return;
        }
        Random gen = new Random();
        Map<String,Object> missions;
        if (type == "daily") {
            missions = dailyMissions.getValues(true);
        } else {
            missions = weeklyMissions.getValues(true);
        }
        List<String> keys = new ArrayList<String>(missions.keySet());
        Map<String,Object> results = new HashMap<String,Object>();
        String randomKey;
        while (results.size() < 2) {
            randomKey = keys.get(gen.nextInt(keys.size()));
            if (results.containsKey(randomKey)) continue;
            results.put(randomKey, missions.get(randomKey));
        }
        latestMissions.set(type, null);    
        latestMissions.createSection(type, results);
    }

    public void updateMissions() {
        if (getHourNow(new Date()) >= getConfig().getInt("weekly_refresh_time")) {
            generateRandomMissions("daily");
        }
        if (getDayInWeekNow(new Date()) >= getConfig().getInt("daily_refresh_time")) {
            generateRandomMissions("weekly");
        }
    }

    private static int getHourNow(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        return c.get(Calendar.HOUR_OF_DAY);
    }

    private static int getDayInWeekNow(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        return c.get(Calendar.DAY_OF_WEEK);
    }
}