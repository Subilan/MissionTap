package org.sotap.MissionTap.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.sotap.MissionTap.Main;

public final class Functions {

    public static void dispatchCommands(Player p, List<String> commands) {
        CommandSender sender = Bukkit.getConsoleSender();
        for (String cmd : commands) {
            Bukkit.dispatchCommand(sender, cmd.replace("%playername%", p.getName())
                    .replace("%uuid%", p.getUniqueId().toString()));
        }
    }

    public static void initUtils(Main plugin) {
        Files.cwd = plugin.getDataFolder().getPath();
        Files.config = plugin.getConfig();
        Files.dailyMissions = Files.load(".", "daily-missions.yml");
        Files.weeklyMissions = Files.load(".", "weekly-missions.yml");
        Files.speicalMissions = Files.load(".", "special-missions.yml");
        Files.DailyMissions = Files.load("./generated", "daily-missions.yml");
        Files.WeeklyMissions = Files.load("./generated", "weekly-missions.yml");
    }

    public static void initMissions(Main plugin) {
        if (Files.DailyMissions == null) {
            plugin.log(
                    Logger.INFO + "No &edaily&r missions were found, trying to regenerate them...");
            generateMissions("daily", plugin);
        }
        if (Files.WeeklyMissions == null) {
            plugin.log(Logger.INFO
                    + "No &eweekly&r missions were found, trying to regenerate them...");
            generateMissions("weekly", plugin);
        }
    }

    public static void refreshMissions(Main plugin) {
        if (Files.DailyMissions.getLong("next-gen") <= Calendars.getNow()) {
            plugin.log(Logger.INFO + "Regenerating &edaily&r missions...");
            generateMissions("daily", plugin);
        }
        if (Files.WeeklyMissions.getLong("next-gen") <= Calendars.getNow()) {
            plugin.log(Logger.INFO + "Regenerating &eweekly&r missions...");
            generateMissions("weekly", plugin);
        }
    }

    public static void generateMissions(String type, Main plugin) {
        if (!List.of("daily", "weekly").contains(type)) {
            return;
        }
        Random gen = new Random();
        if (Files.dailyMissions == null || Files.weeklyMissions == null)
            return;
        FileConfiguration missions = type == "daily" ? Files.dailyMissions : Files.weeklyMissions;
        List<String> keys = new ArrayList<>(missions.getKeys(false));
        Map<String, Object> results = new HashMap<>();
        String randomKey;
        while (results.size() < (type == "daily" ? (keys.size() >= 2 ? 2 : keys.size())
                : (keys.size() >= 4 ? 4 : keys.size()))) {
            randomKey = keys.get(gen.nextInt(keys.size()));
            if (results.containsKey(randomKey))
                continue;
            results.put(randomKey, missions.get(randomKey));
        }
        FileConfiguration target = Files.getGeneratedMissions(type);
        long nextRefresh = Calendars.getNextRefresh(type);
        target.createSection(type, results);
        target.set("last-gen", Calendars.getNow());
        target.set("next-gen", nextRefresh);
        plugin.log(Logger.SUCCESS + "Regeneration done. The next regeneration will be on &a"
                + Calendars.stampToString(nextRefresh) + "&r.");
        Files.save(target, "./generated/" + type + "-missions.yml");
    }
}
