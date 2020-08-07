package org.sotap.MissionTap.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.sotap.MissionTap.MissionTap;
import net.md_5.bungee.api.ChatColor;

public final class Functions {

    public static void dispatchCommands(Player p, List<String> commands) {
        CommandSender sender = Bukkit.getConsoleSender();
        for (String cmd : commands) {
            Bukkit.dispatchCommand(sender, cmd.replace("%playername%", p.getName())
                    .replace("%uuid%", p.getUniqueId().toString()));
        }
    }

    public static void initUtils(MissionTap plugin) {
        Files.cwd = plugin.getDataFolder().getPath();
        Files.config = plugin.getConfig();
        Files.dailyMissions = Files.load(".", "daily-missions.yml");
        Files.weeklyMissions = Files.load(".", "weekly-missions.yml");
        Files.speicalMissions = Files.load(".", "special-missions.yml");
        Files.DailyMissions = Files.load("./generated", "daily-missions.yml");
        Files.WeeklyMissions = Files.load("./generated", "weekly-missions.yml");
    }

    public static void initMenus(MissionTap plugin) {
        Menus.refresh(plugin);
    }

    public static void initEvents(MissionTap plugin) {
        Events.refresh(plugin);
    }

    public static void initMissions(MissionTap plugin) {
        if (Files.isEmptyConfiguration(Files.DailyMissions)) {
            plugin.log(
                    Logger.INFO + "No &edaily&r missions were found, trying to regenerate them...");
            generateMissions("daily", plugin);
        }
        if (Files.isEmptyConfiguration(Files.WeeklyMissions)) {
            plugin.log(Logger.INFO
                    + "No &eweekly&r missions were found, trying to regenerate them...");
            generateMissions("weekly", plugin);
        }
    }

    public static void refreshMissions(MissionTap plugin) {
        if (!Files.isEmptyConfiguration(Files.DailyMissions)) {
            if (Files.DailyMissions.getLong("next-gen") <= Calendars.getNow()) {
                plugin.log(Logger.INFO + "Regenerating &edaily&r missions...");
                generateMissions("daily", plugin);
            }
        }
        if (!Files.isEmptyConfiguration(Files.WeeklyMissions)) {
            if (Files.WeeklyMissions.getLong("next-gen") <= Calendars.getNow()) {
                plugin.log(Logger.INFO + "Regenerating &eweekly&r missions...");
                generateMissions("weekly", plugin);
            }
        }
    }

    public static void generateMissions(String type, MissionTap plugin) {
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
        FileConfiguration target = Files.getGeneratedMissionFile(type);
        long nextRefresh = Calendars.getNextRefresh(type);
        target.createSection(type, results);
        target.set("last-gen", Calendars.getNow());
        target.set("next-gen", nextRefresh);
        Files.save(target, "./generated/" + type + "-missions.yml");
        plugin.log(Logger.SUCCESS + "Regeneration done. The next regeneration will be on &a"
                + Calendars.stampToString(nextRefresh) + "&r.");
    }

    public static ItemStack createItemStack(final String name, final Material material,
            final List<String> lore) {
        final ItemStack item = new ItemStack(material);
        final ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.AQUA + name);
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    public static void reloadPlugin(MissionTap plugin) {
        plugin.reloadConfig();
        initUtils(plugin);
        initMissions(plugin);
        initMenus(plugin);
        initEvents(plugin);
    }

    public static void resetSubmittedList(UUID u) {
        FileConfiguration playerdata = Files.loadPlayer(u);
        playerdata.set("submitted-list", null);
    }

    public static void resetSubmittedListForAll() {
        // to be continued
    }

    public static void initDataForPlayer(UUID u) {
        @SuppressWarnings("unused")
        FileConfiguration playerdata = Files.loadPlayer(u);
    }
}
