package org.sotap.MissionTap;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.apache.commons.lang.time.DateUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.sotap.MissionTap.GUI.MainMenu;
import org.sotap.MissionTap.GUI.MissionMenu;
import org.sotap.MissionTap.Utils.G;

public final class MissionTap extends JavaPlugin {
    public FileConfiguration specialMissions;
    public FileConfiguration dailyMissions;
    public FileConfiguration weeklyMissions;
    public FileConfiguration latestMissions;
    public MissionMenu weeklyMissionMenu;
    public MissionMenu dailyMissionMenu;
    public MainMenu mainMenu;

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
        if (latestMissions.getConfigurationSection("daily") == null) {
            log(G.translateColor(G.INFO + "No &edaily&r missions were found, trying to regenerate them..."));
            generateRandomMissions("daily");
        }
        if (latestMissions.getConfigurationSection("weekly") == null) {
            log(G.translateColor(G.INFO + "No &eweekly&r missions were found, trying to regenerate them..."));
            generateRandomMissions("weekly");
        }
        updateMissions();
        dailyMissionMenu = new MissionMenu("daily", this);
        weeklyMissionMenu = new MissionMenu("weekly", this);
        mainMenu = new MainMenu(this);
        Bukkit.getPluginCommand("missiontap").setExecutor(new CommandHandler(this));
        //@SuppressWarnings("unused")
        //BukkitTask timer = new Timer(this).runTaskTimer(this, 0, 20);
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
        if (dailyMissions == null || weeklyMissions == null) return;
        FileConfiguration missions = type == "daily" ? dailyMissions : weeklyMissions;
        List<String> keys = new ArrayList<String>(dailyMissions.getKeys(false));
        Map<String,Object> results = new HashMap<String,Object>();
        String randomKey;
        while (results.size() < (type == "daily" ? 2 : 4)) {
            randomKey = keys.get(gen.nextInt(keys.size()));
            if (results.containsKey(randomKey)) continue;
            results.put(randomKey, missions.get(randomKey));
        }
        latestMissions.set(type, null);    
        latestMissions.createSection(type, results);
        saveMissions();
    }

    public void saveMissions() {
        try {
            dailyMissions.save(new File(getDataFolder(), "daily-missions.yml"));
            weeklyMissions.save(new File(getDataFolder(), "weekly-missions.yml"));
            specialMissions.save(new File(getDataFolder(), "special-missions.yml"));
            latestMissions.save(new File(getDataFolder(), "latest-missions.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateMissions() {
        if (getHourNow(new Date()) >= getConfig().getInt("weekly_refresh_time")) {
            log(G.translateColor(G.SUCCESS + "It's time to refresh now! Regenerating the missions..."));
            generateRandomMissions("daily");
            log(G.translateColor(G.SUCCESS + "Successfully regenerated the missions."));
        }
        if (getDayInWeekNow(new Date()) >= getConfig().getInt("daily_refresh_time")) {
            log(G.translateColor(G.SUCCESS + "It's time to refresh now! Regenerating the missions..."));
            generateRandomMissions("weekly");
            log(G.translateColor(G.SUCCESS + "Successfully regenerated the missions."));
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