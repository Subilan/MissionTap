package org.sotap.MissionTap;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.sotap.MissionTap.Events.GlobalEvents;
import org.sotap.MissionTap.GUI.InprogressMenu;
import org.sotap.MissionTap.GUI.MainMenu;
import org.sotap.MissionTap.GUI.MissionMenu;
import org.sotap.MissionTap.Utils.C;
import org.sotap.MissionTap.Utils.G;

public final class MissionTap extends JavaPlugin {
    public FileConfiguration specialMissions;
    public FileConfiguration dailyMissions;
    public FileConfiguration weeklyMissions;
    public FileConfiguration latestMissions;
    public MissionMenu weeklyMissionMenu;
    public MissionMenu dailyMissionMenu;
    public MainMenu mainMenu;
    public InprogressMenu inprogMenu;
    public GlobalEvents events;

    public void log(String message) {
        getLogger().info(message);
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        G.cwd = getDataFolderPath();
        G.crawl = getConfig().getBoolean("crawl_stars_mode");
        this.specialMissions = G.load("special-missions.yml");
        this.dailyMissions = G.load("daily-missions.yml");
        this.weeklyMissions = G.load("weekly-missions.yml");
        this.latestMissions = G.load("latest-missions.yml");
        initMissions();
        refreshMissions();
        dailyMissionMenu = new MissionMenu("daily", this);
        weeklyMissionMenu = new MissionMenu("weekly", this);
        mainMenu = new MainMenu(this);
        inprogMenu = new InprogressMenu(this);
        events = new GlobalEvents(this);
        Bukkit.getPluginCommand("missiontap").setExecutor(new CommandHandler(this));
        // @SuppressWarnings("unused")
        // BukkitTask timer = new Timer(this).runTaskTimer(this, 0, 20);
        log(G.translateColor(G.SUCCESS + "The plugin has been &aenabled&r."));
    }

    @Override
    public void onDisable() {
        log(G.translateColor(G.SUCCESS + "The plugin has been &cdisabled&r."));
    }

    public String getDataFolderPath() {
        return getDataFolder().getPath();
    }

    public void initMissions() {
        // initial generation
        if (latestMissions.getConfigurationSection("daily") == null) {
            log(G.translateColor(
                    G.INFO + "No &edaily&r missions were found, trying to regenerate them..."));
            generateRandomMissions("daily");
        }
        if (latestMissions.getConfigurationSection("weekly") == null) {
            log(G.translateColor(
                    G.INFO + "No &eweekly&r missions were found, trying to regenerate them..."));
            generateRandomMissions("weekly");
        }

    }

    public void refreshMissions() {
        // refresh generation
        if (latestMissions.getLong("daily-next-regen") <= new Date().getTime()) {
            log(G.translateColor(G.INFO + "Regenerating &edaily&r missions..."));
            generateRandomMissions("daily");
        }
        if (latestMissions.getLong("weekly-next-regen") <= new Date().getTime()) {
            log(G.translateColor(G.INFO + "Regenerating &bweekly&r missions..."));
            generateRandomMissions("weekly");
        }
    }

    public void generateRandomMissions(String type) {
        if (!List.of("daily", "weekly").contains(type)) {
            return;
        }
        Random gen = new Random();
        if (dailyMissions == null || weeklyMissions == null)
            return;
        FileConfiguration missions = type == "daily" ? dailyMissions : weeklyMissions;
        List<String> keys = new ArrayList<>(dailyMissions.getKeys(false));
        Map<String, Object> results = new HashMap<>();
        String randomKey;
        while (results.size() < (type == "daily" ? 2 : 4)) {
            randomKey = keys.get(gen.nextInt(keys.size()));
            if (results.containsKey(randomKey))
                continue;
            results.put(randomKey, missions.get(randomKey));
        }
        latestMissions.set(type, null);
        latestMissions.createSection(type, results);
        Date date = new Date();
        latestMissions.set(type + "-last-regen", date.getTime());
        latestMissions.set(type + "-next-regen", getNextRegenerationTime(type).getTime());
        log(G.translateColor(G.SUCCESS + "Regeneration done. The next regeneration will be on &a"
                + G.getDateFormat().format(getNextRegenerationTime(type))
                + "&r."));
        saveMissions();
    }

    public Date getNextRegenerationTime(String type) {
        return type == "daily" ? C.getNextDailyRefresh(getConfig())
                : C.getNextWeeklyRefresh(getConfig());
    }

    public void saveMissions() {
        G.save(dailyMissions, "daily-missions");
        G.save(weeklyMissions, "weekly-missions");
        G.save(specialMissions, "special-missions");
        G.save(latestMissions, "latest-missions");
    }
}
