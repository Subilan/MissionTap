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
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.sotap.MissionTap.MissionTap;
import org.sotap.MissionTap.Classes.Mission;
import net.md_5.bungee.api.ChatColor;

public final class Functions {

    public static void dispatchCommands(Player p, List<String> commands) {
        CommandSender sender = Bukkit.getConsoleSender();
        for (String cmd : commands) {
            Bukkit.dispatchCommand(sender,
                    LogUtil.translateColor(cmd.replace("%playername%", p.getName())
                            .replace("%uuid%", p.getUniqueId().toString())));
        }
    }

    /**
     * 获取一份随机生成的任务，返回的值是 Map，需要通过 createConfigurationSection 使用
     * @param type 任务类型
     * @return 随机生成的任务
     */
    public static Map<String, Object> getRandomMissions(String type) {
        if (!List.of("daily", "weekly").contains(type)) {
            return null;
        }
        Random random = new Random();
        FileConfiguration pool = Files.getMissions(type);
        if (pool == null)
            return null;
        Map<String, Object> result = new HashMap<>();
        List<String> keys = new ArrayList<>(pool.getKeys(false));
        Integer amount = Files.config.getInt(type + "-mission-amount");
        final Integer finalAmount = amount == 0 ? (type == "daily" ? 2 : 4)
                : (keys.size() >= amount ? amount : keys.size());
        String randomKey;
        while (result.size() < finalAmount) {
            randomKey = keys.get(random.nextInt(keys.size()));
            if (result.containsKey(randomKey))
                continue;
            result.put(randomKey, pool.get(randomKey));
        }
        return result;
    }

    /**
     * 为所有有记录玩家重新生成一份任务
     * @param type 任务类型
     */
    public static void generateMissionsForAll(String type) {
        Map<String, FileConfiguration> playermissions = Files.getAllPlayerMissions();
        FileConfiguration playermission;
        for (String key : playermissions.keySet()) {
            playermission = playermissions.get(key);
            playermission.createSection(type, getRandomMissions(type));
            Files.savePlayerMission(playermission, UUID.fromString(key));
        }
        Files.meta.set(type + ".last-regen", Calendars.getNow());
        Files.meta.set(type + ".next-regen", Calendars.getNextRefresh(type));
        Files.saveMeta();
    }

    /**
     * 为单一玩家单独生成新的任务，本项不属于刷新操作，故不会更新 {@code}last-regen{@code} 或者 {@code}next-regen{@code} 时间。
     * @param u 玩家 UUID
     * @param type 任务类型
     */
    public static void generateMissionsFor(UUID u, String type) {
        FileConfiguration playermission = Files.getPlayerMissions(u);
        playermission.createSection(type, getRandomMissions(type));
        Files.savePlayerMission(playermission, u);
    }

    /**
     * 根据所提供的信息创建一个 ItemStack
     * @param name 物品名称
     * @param material 物品材质
     * @param lore 介绍部分（lore）
     * @return 所求 ItemStack
     */
    public static ItemStack createItemStack(final String name, final Material material,
            final List<String> lore) {
        final ItemStack item = new ItemStack(material);
        final ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.AQUA + name);
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    /**
     * 重载插件，本项不会重载 Menu 和 Events
     * @param plugin
     */
    public static void reloadPlugin(MissionTap plugin) {
        plugin.reloadConfig();
        Files.init(plugin);
        LogUtil.init(plugin);
        if (Files.config.getBoolean("special-missions")) {
            Mission.missionTypes = new String[] {"daily", "weekly", "special"};
        } else {
            Mission.missionTypes = new String[] {"daily", "weekly"};
        }
    }

    /**
     * 处理玩家 {@code}p{@code} 对任务 {@code}m{@code} 的完成操作
     * @param m
     * @param p
     */
    public static void finishMission(Mission m, Player p) {
        if (Files.config.getBoolean("require-acceptance")
                && !Files.config.getBoolean("allow-multiple-acceptance")) {
            m.setSubmitted();
            m.destory();
        } else if (!Files.config.getBoolean("require-acceptance")
                && Files.config.getBoolean("allow-multiple-acceptance")) {
            m.clearData();
        } else {
            m.destory();
        }
        LogUtil.success("&e恭喜！ &r你成功完成了任务 &a" + m.getName() + "&r！", p);
        if (!m.reward(p)) {
            LogUtil.warn("这个任务&c没有给予任何奖励&r。", p);
        }
    }

    /**
     * 删除指定玩家的任务提交记录
     * 
     * @param u    UUID
     * @param type 类型
     */
    public static void clearSubmittion(UUID u, String type) {
        if (!List.of("weekly", "daily").contains(type))
            return;
        FileConfiguration playerdata = Files.loadPlayer(u);
        playerdata.set("submittion-list." + type, null);
        Files.savePlayer(playerdata, u);
    }

    /**
     * 删除指定玩家的所有任务提交记录
     * 
     * @param u UUID
     */
    public static void clearAllSubmittions(UUID u) {
        FileConfiguration playerdata = Files.loadPlayer(u);
        playerdata.set("submittion-list", null);
        Files.savePlayer(playerdata, u);
    }

    /**
     * 删除所有玩家的指定类型的提交记录，若类型为空，则删除所有玩家的所有任务提交记录
     * 
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
     * 
     * @param u    UUID
     * @param type 任务类型
     */
    public static void clearMission(UUID u, String type) {
        FileConfiguration playerdata = Files.loadPlayer(u);
        playerdata.set(type, null);
        Files.savePlayer(playerdata, u);
    }

    /**
     * 删除所有玩家数据中指定类型的所有任务
     * 
     * @param type 任务类型
     */
    public static void clearAllMissions(String type) {
        for (UUID u : Files.getAllPlayerUUID()) {
            clearMission(u, type);
        }
    }

    /**
     * 清除所有玩家存在的任何类型的过期任务
     */
    public static void clearAllExpiredMissions() {
        for (String type : new String[] {"daily", "weekly"}) {
            Map<UUID,FileConfiguration> playerdatas = Files.getAllPlayerdata();
            FileConfiguration playerdata;
            ConfigurationSection inprogMissions;
            for (UUID u : playerdatas.keySet()) {
                playerdata = playerdatas.get(u);
                inprogMissions = playerdata.getConfigurationSection(type);
                if (inprogMissions == null) continue;
                for (String key : inprogMissions.getKeys(false)) {
                    Mission m = new Mission(u, type, key);
                    if (m.isExpired()) {
                        m.destory();
                    }
                }
            }
        }
    }

    /**
     * 删除指定玩家数据中的所有任务
     * 
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

    public static boolean isEmptyItemStack(ItemStack i) {
        return i == null || i.getType().equals(Material.AIR);
    }

    public static boolean isTimeForRefreshFor(String type) {
        return Calendars.getNow() >= Files.meta.getLong(type + ".next-gen");
    }

    public static void handleMissionRefresh() {
        for (String type : new String[] {"daily", "weekly"}) {
            if (isTimeForRefreshFor(type)) {
                clearAllExpiredMissions();
                generateMissionsForAll(type);
                if (!Files.config.getBoolean("require-acceptance")) {
                    acceptMissionsForAll(type);
                }
            }
        }
    }

    public static void initPlayer(Player p) {
        if (!p.hasPlayedBefore()) {
            generateMissionsFor(p.getUniqueId(), "daily");
            generateMissionsFor(p.getUniqueId(), "weekly");
            if (!Files.config.getBoolean("require-acceptance")) {
                acceptMissionsFor("daily", p.getUniqueId()); 
                acceptMissionsFor("weekly", p.getUniqueId());
            }
        }
    }

    public static void acceptMissionsFor(String type, UUID u) {
        if (!List.of("weekly", "daily").contains(type)) return;
        FileConfiguration playermission = Files.getPlayerMissions(u);
        ConfigurationSection missions = playermission.getConfigurationSection(type);
        if (missions == null) {
            LogUtil.warn("未找到 &e" + u.toString() + "&r 的任务列表。");
            return;
        }
        for (String key : missions.getKeys(false)) {
            Mission m = new Mission(u, type, key);
            m.accept();
        }
    }

    public static void acceptMissionsForAll(String type) {
        if (!List.of("weekly", "daily").contains(type)) return;
        for (UUID u : Files.getAllPlayerUUID()) {
            acceptMissionsFor(type, u);
        }
    }
}
