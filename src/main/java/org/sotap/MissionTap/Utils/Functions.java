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
        LogUtil.origin = plugin.getLogger();
    }

    public static void initMenus(MissionTap plugin) {
        Menus.refresh(plugin);
    }

    public static void initEvents(MissionTap plugin) {
        Events.refresh(plugin);
    }

    public static void initMissions() {
        if (Files.isEmptyConfiguration(Files.DailyMissions)) {
            LogUtil.info("找不到存在的&e每日&r任务，正在尝试重新生成...");
            generateMissions("daily");
        }
        if (Files.isEmptyConfiguration(Files.WeeklyMissions)) {
            LogUtil.info("找不到存在的&e每周&r任务，正在尝试重新生成...");
            generateMissions("weekly");
        }
        if (Files.isEmptyConfiguration(Files.SpecialMissions) && Files.config.getBoolean("special-missions")) {
            LogUtil.warn("特殊任务已被设置为启用状态，但找不到存在的&e特殊&r任务。");
        }
    }

    public static void refreshMissions() {
        if (!Files.isEmptyConfiguration(Files.DailyMissions)) {
            if (Files.DailyMissions.getLong("next-gen") <= Calendars.getNow()) {
                clearAllSubmittionsForAll("daily");
                LogUtil.info("正在刷新&e每日&r任务...");
                generateMissions("daily");
            }
        }
        if (!Files.isEmptyConfiguration(Files.WeeklyMissions)) {
            if (Files.WeeklyMissions.getLong("next-gen") <= Calendars.getNow()) {
                clearAllSubmittionsForAll("weekly");
                LogUtil.info("正在刷新&e每周&r任务...");
                generateMissions("weekly");
            }
        }
    }

    public static void generateMissions(String type) {
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
        LogUtil.success("刷新成功。下次刷新日期为 &a" + Calendars.stampToString(nextRefresh) + "&r。");
        if (!Files.config.getBoolean("require-acceptance")) {
            LogUtil.info("正在向玩家档案写入任务数据...");
            acceptGlobalMission(type);
            LogUtil.success("写入成功。");
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
        initMissions();
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
        LogUtil.success("&e恭喜！ &r你成功完成了任务 &a" + m.getName() + "&r！", p);
        if (!m.reward(p)) {
            LogUtil.warn("这个任务&c没有给予任何奖励&r。", p);
        }
    }

    /**
     * 删除指定玩家的任务提交记录
     * @param u UUID
     * @param type 类型
     */
    public static void clearSubmittion(UUID u, String type) {
        if (!List.of("weekly", "daily").contains(type)) return;
        FileConfiguration playerdata = Files.loadPlayer(u);
        playerdata.set("submittion-list." + type, null);
        Files.savePlayer(playerdata, u);
    }

    /**
     * 删除指定玩家的所有任务提交记录
     * @param u UUID
     */
    public static void clearAllSubmittions(UUID u) {
        FileConfiguration playerdata = Files.loadPlayer(u);
        playerdata.set("submittion-list", null);
        Files.savePlayer(playerdata, u);
    }

    /**
     * 删除所有玩家的指定类型的提交记录，若类型为空，则删除所有玩家的所有任务提交记录
     * @param type
     */
    public static void clearAllSubmittionsForAll(String... type) {
        String typeStr = type.length > 0 ? type[0] : null;
        for (UUID u : Files.getAllPlayerUUID()) {
            if (typeStr != null) {
                clearSubmittion(u, typeStr);
            } else {
                clearAllSubmittions(u);
            }
        }
    }

    /**
     * 删除指定玩家数据中指定类型的所有任务
     * @param u UUID
     * @param type 任务类型
     */
    public static void clearMission(UUID u, String type) {
        FileConfiguration playerdata = Files.loadPlayer(u);
        playerdata.set(type, null);
        Files.savePlayer(playerdata, u);
    }

    /**
     * 删除所有玩家数据中指定类型的所有任务
     * @param type 任务类型
     */
    public static void clearAllMissions(String type) {
        for (UUID u : Files.getAllPlayerUUID()) {
            clearMission(u, type);
        }
    }

    /**
     * 删除指定玩家数据中的所有任务
     * @param u UUID
     */
    public static void clearAllMissionsFor(UUID u) {
        FileConfiguration playerdata = Files.loadPlayer(u);
        // NOTE: There must be three elements to work.
        for (String type : new String[] {"daily", "weekly", "special"}) {
            playerdata.set(type, null);
        }
        Files.savePlayer(playerdata, u);
    }
}
