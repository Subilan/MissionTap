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
import org.sotap.MissionTap.Classes.GlobalMission;
import org.sotap.MissionTap.Classes.Mission;
import net.md_5.bungee.api.ChatColor;

public final class Functions {

    public static void dispatchCommands(Player p, List<String> commands) {
        CommandSender sender = Bukkit.getConsoleSender();
        for (String cmd : commands) {
            Bukkit.dispatchCommand(sender, LogUtil.translateColor(
                    cmd.replace("%playername%", p.getName()).replace("%uuid%", p.getUniqueId().toString())));
        }
    }

    public static void initUtils(MissionTap plugin) {
        Files.cwd = plugin.getDataFolder().getPath();
        Files.config = plugin.getConfig();
        Files.dailyMissions = Files.load(".", "daily-missions.yml");
        Files.weeklyMissions = Files.load(".", "weekly-missions.yml");
        Files.SpecialMissions = Files.load(".", "special-missions.yml");
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
            plugin.log(LogUtil.INFO + "找不到存在的&e每日&r任务，正在尝试重新生成...");
            generateMissions("daily", plugin);
        }
        if (Files.isEmptyConfiguration(Files.WeeklyMissions)) {
            plugin.log(LogUtil.INFO + "找不到存在的&e每周&r任务，正在尝试重新生成...");
            generateMissions("weekly", plugin);
        }
        if (Files.isEmptyConfiguration(Files.SpecialMissions) && Files.config.getBoolean("special-missions")) {
            plugin.log(LogUtil.WARN + "特殊任务已被设置为启用状态，但找不到存在的&e特殊&r任务。");
        }
    }

    public static void refreshMissions(MissionTap plugin) {
        if (!Files.isEmptyConfiguration(Files.DailyMissions)) {
            if (Files.DailyMissions.getLong("next-gen") <= Calendars.getNow()) {
                plugin.log(LogUtil.INFO + "正在刷新&e每日&r任务...");
                generateMissions("daily", plugin);
            }
        }
        if (!Files.isEmptyConfiguration(Files.WeeklyMissions)) {
            if (Files.WeeklyMissions.getLong("next-gen") <= Calendars.getNow()) {
                plugin.log(LogUtil.INFO + "正在刷新&e每周&r任务...");
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
        Integer amount = Files.config.getInt(type + "-mission-amount");
        while (results
                .size() < (amount == 0 ? (type == "daily" ? 2 : 4) : (keys.size() >= amount ? amount : keys.size()))) {
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
        plugin.log(LogUtil.SUCCESS + "刷新成功。下次刷新日期为 &a" + Calendars.stampToString(nextRefresh) + "&r。");
        if (!Files.config.getBoolean("require-acceptance")) {
            plugin.log(LogUtil.INFO + "正在向玩家档案写入任务数据...");
            acceptGlobalMission(type);
            plugin.log(LogUtil.SUCCESS + "写入成功。");
        }
    }

    public static ItemStack createItemStack(final String name, final Material material, final List<String> lore) {
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
        if (Files.config.getBoolean("special-missions")) {
            Mission.missionTypes = new String[] { "daily", "weekly", "special" };
        } else {
            Mission.missionTypes = new String[] { "daily", "weekly" };
        }
    }

    public static void acceptGlobalMission(String type) {
        GlobalMission mission = new GlobalMission(type);
        mission.accept();
    }

    public static void initDataForPlayer(UUID u) {
        FileConfiguration playerdata = Files.loadPlayer(u);
        if (!Files.config.getBoolean("require-acceptance")) {
            if (Files.isEmptyConfiguration(playerdata)) {
                GlobalMission dailyGlobalMission = new GlobalMission("daily");
                GlobalMission weeklyGlobalMission = new GlobalMission("weekly");
                dailyGlobalMission.acceptAllFor(u);
                weeklyGlobalMission.acceptAllFor(u);
            }
        }
    }

    public static void finishMission(Mission m, Player p) {
        UUID u = p.getUniqueId();
        if (Files.config.getBoolean("require-acceptance") && !Files.config.getBoolean("allow-multiple-acceptance")) {
            m.setSubmitted(u);
            m.destory(u);
        } else if (!Files.config.getBoolean("require-acceptance")
                && Files.config.getBoolean("allow-multiple-acceptance")) {
            m.clearData(u);
        } else {
            m.destory(u);
        }
        p.sendMessage(LogUtil.translateColor(LogUtil.SUCCESS + "&e恭喜！ &r你成功完成了任务 &a" + m.getName() + "&r！"));
        if (!m.reward(p)) {
            p.sendMessage(LogUtil.translateColor(LogUtil.WARN + "这个任务&c没有给予任何奖励&r。"));
        }
    }
}
