package org.sotap.MissionTap.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
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
     * 
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
     * 
     * @param type 任务类型
     */
    public static void generateMissionsForAll(String type) {
        Map<String, FileConfiguration> playermissions = Files.getAllPlayerMissions();
        if (playermissions == null) {
            return;
        }
        FileConfiguration playermission;
        ConfigurationSection missionsBefore;
        Map<String, Object> missionBeforeMap;
        Map<String, Object> resultMap;
        for (String key : playermissions.keySet()) {
            playermission = playermissions.get(key);
            resultMap = getRandomMissions(type);
            if (type == "weekly") {
                missionsBefore = playermission.getConfigurationSection(type);
                if (missionsBefore != null) {
                    missionBeforeMap = missionsBefore.getValues(false);
                    Map<String, Object> mergedMap = new HashMap<>();
                    for (String keyBefore : missionBeforeMap.keySet()) {
                        if (resultMap.containsKey(keyBefore)) {
                            resultMap.remove(keyBefore);
                        }
                    }
                    mergedMap.putAll(missionBeforeMap);
                    mergedMap.putAll(resultMap);
                    resultMap = mergedMap;
                }
            }
            playermission.createSection(type, resultMap);
            Files.savePlayerMission(playermission, UUID.fromString(key));
        }
        updateNextRefreshTime(type);
    }

    /**
     * 为单一玩家单独生成新的任务，本项不属于刷新操作，故不会更新 {@code}last-regen{@code} 或者 {@code}next-regen{@code} 时间。
     * 
     * @param u    玩家 UUID
     * @param type 任务类型
     */
    public static void generateMissionsFor(UUID u, String type) {
        FileConfiguration playermission = Files.getPlayerMissions(u);
        playermission.createSection(type, getRandomMissions(type));
        Files.savePlayerMission(playermission, u);
    }

    /**
     * 根据所提供的信息创建一个 ItemStack
     * 
     * @param name     物品名称
     * @param material 物品材质
     * @param lore     介绍部分（lore）
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
     * 
     * @param plugin
     */
    public static void reloadPlugin(MissionTap plugin) {
        LogUtil.init(plugin);
        Files.init(plugin);
        Calendars.init();
        plugin.reloadConfig();
        handleMissionGeneration();
        if (Files.config.getBoolean("special-missions")) {
            Mission.missionTypes = new String[] {"daily", "weekly", "special"};
        } else {
            clearAllMissions("special");
            Mission.missionTypes = new String[] {"daily", "weekly"};
        }
    }

    /**
     * 处理玩家 {@code}p{@code} 对任务 {@code}m{@code} 的完成操作
     * 
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
        p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 10, 1);
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
        List<UUID> uuids = Files.getAllPlayerUUID();
        if (uuids == null)
            return;
        for (UUID u : uuids) {
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
        List<UUID> uuids = Files.getAllPlayerUUID();
        if (uuids == null)
            return;
        for (UUID u : uuids) {
            clearMission(u, type);
        }
    }

    /**
     * 清除所有玩家存在的任何类型的过期任务
     */
    public static void clearAllExpiredMissions() {
        for (String type : new String[] {"daily", "weekly"}) {
            Map<UUID, FileConfiguration> playerdatas = Files.getAllPlayerdata();
            if (playerdatas == null)
                continue;
            FileConfiguration playerdata;
            ConfigurationSection inprogMissions;
            for (UUID u : playerdatas.keySet()) {
                playerdata = playerdatas.get(u);
                inprogMissions = playerdata.getConfigurationSection(type);
                if (inprogMissions == null)
                    continue;
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

    /**
     * 判断指定 ItemStack 是否为空，即是否为 NULL 或者空气
     * 
     * @param i ItemStack
     * @return
     */
    public static boolean isEmptyItemStack(ItemStack i) {
        return i == null || i.getType().equals(Material.AIR);
    }

    /**
     * 判断当前时间是否已经为或超过指定类型任务的下次刷新时间
     * 
     * @param type 任务类型
     * @return
     */
    public static boolean isTimeForRefreshFor(String type) {
        return Calendars.getNow() >= Files.meta.getLong(type + ".next-regen");
    }

    /**
     * 处理指定类型任务刷新的相关逻辑。 先判断是不是满足了刷新时间，如果不是则不执行。 如果是，先清除所有玩家的指定类型的过期任务，然后再生成一份到玩家的任务列表中。
     * 最后判断如果不需要手动接受就帮玩家自动接受。
     * 
     * @param type 任务类型
     */
    public static void handleMissionRefresh(String type) {
        if (!List.of("daily", "weekly").contains(type))
            return;
        LogUtil.info("正在刷新" + (type == "daily" ? "每日" : "每周") + "任务。");
        clearAllExpiredMissions();
        generateMissionsForAll(type);
        if (!Files.config.getBoolean("require-acceptance")) {
            acceptMissionsForAll(type);
        }
        LogUtil.success("刷新成功。");
    }

    /**
     * 用于服务器开启或重载时的一个函数。其中包含日志输出、空文件处理和调用 {@code}handleMissionRefresh(){@code}。
     */
    public static void handleMissionGeneration() {
        if (Files.isEmptyConfiguration(Files.dailyMissions)) {
            LogUtil.warn("找不到每日任务池的内容。");
        }
        if (Files.isEmptyConfiguration(Files.weeklyMissions)) {
            LogUtil.warn("找不到每周任务池的内容。");
        }
        if (Files.isEmptyConfiguration(Files.dailyMissions)
                && Files.isEmptyConfiguration(Files.weeklyMissions)) {
            LogUtil.warn("请在任务编写好后输入 &b/mt reload&r 来重载任务，在没写好并重载前请&c不要&r让玩家进入服务器。");
            LogUtil.warn("若已经有玩家进入服务器，则应当让玩家退出后重新加入，否则任务数据为空。");
        }
        if (!Files.isEmptyConfiguration(Files.dailyMissions)
                || !Files.isEmptyConfiguration(Files.weeklyMissions)) {
            if (Files.isEmptyConfiguration(Files.meta)) {
                LogUtil.info("初始化任务刷新时间...");
                updateNextRefreshTime("daily");
                updateNextRefreshTime("weekly");
                LogUtil.success("初始化成功。");
            }
            for (String type : new String[] {"daily", "weekly"}) {
                if (isTimeForRefreshFor(type)) {
                    handleMissionRefresh(type);
                }
            }
        }
    }

    /**
     * 初始化一名玩家
     * 
     * @param p 玩家对象
     */
    public static void initPlayer(UUID u) {
        if (isMissionEmpty(u)) {
            LogUtil.info("检测到该玩家没有任务数据，初始化中...");
            generateMissionsFor(u, "daily");
            generateMissionsFor(u, "weekly");
            if (!Files.config.getBoolean("require-acceptance")) {
                acceptMissionsFor("daily", u);
                acceptMissionsFor("weekly", u);
                if (Files.config.getBoolean("special-missions")) {
                    acceptMissionsFor("special", u);
                }
            }
            LogUtil.success("初始化成功。");
        }
    }

    public static boolean isMissionEmpty(UUID u) {
        return Files.isEmptyConfiguration(Files.getPlayerMissions(u));
    }

    /**
     * 为指定玩家接受所有指定类型的任务，用于 {@code}require-acceptance=false{@code} 的情形
     * 
     * @param type 任务类型
     * @param u    UUID
     */
    public static void acceptMissionsFor(String type, UUID u) {
        if (!List.of("weekly", "daily", "special").contains(type))
            return;
        if (type == "special") {
            if (Files.specialMissions == null) {
                LogUtil.warn("特殊任务已启用，但未找到特殊任务。");
                return;
            }
            for (String key : Files.specialMissions.getKeys(false)) {
                Mission m = new Mission(u, type, key);
                m.accept();
            }
        } else {
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
    }

    /**
     * 为所有玩家接受指定类型的任务，用于 {@code}require-acceptance=false{@code} 的情形
     * 
     * @param type
     */
    public static void acceptMissionsForAll(String type) {
        if (!List.of("weekly", "daily", "special").contains(type))
            return;
        List<UUID> uuids = Files.getAllPlayerUUID();
        if (uuids == null)
            return;
        for (UUID u : uuids) {
            acceptMissionsFor(type, u);
        }
    }

    /**
     * 更新指定类型的下次刷新时间，该函数将一并写入执行该函数的时间到 last-regen 项内
     * 
     * @param type 任务类型
     */
    public static void updateNextRefreshTime(String type) {
        if (!List.of("daily", "weekly").contains(type))
            return;
        Long nextRegen = Calendars.getNextRefresh(type);
        Files.meta.set(type + ".last-regen", Calendars.getNow());
        Files.meta.set(type + ".next-regen", Calendars.getNextRefresh(type));
        Long nextRegenReal = 0l;
        if (Calendars.timeOffset != 0) {
            nextRegenReal = nextRegen - Calendars.timeOffset * 3600000;
        }
        LogUtil.info("下次" + (type == "daily" ? "每日" : "每周") + "任务刷新时间： &a"
                + Calendars.stampToString(nextRegen)
                + (nextRegenReal != 0L
                        ? "&r（真实时间 &a" + Calendars.stampToString(nextRegenReal) + "&r）"
                        : ""));
        Files.saveMeta();
    }

    /**
     * 为指定的 IS 添加一行 lore
     * 
     * @param line  要添加的 lore
     * @param stack 要添加的 IS
     * @return 添加后的 IS
     */
    public static ItemStack addLore(String line, ItemStack stack) {
        ItemMeta meta = stack.getItemMeta();
        List<String> loreBefore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
        loreBefore.add(line);
        meta.setLore(loreBefore);
        stack.setItemMeta(meta);
        return stack;
    }

    /**
     * 移除指定 IS 的某一行 lore
     * 
     * @param line  要移除的 lore
     * @param stack 要移除的 IS
     * @return 移除后的 IS
     */
    public static ItemStack removeLore(String line, ItemStack stack) {
        ItemMeta meta = stack.getItemMeta();
        if (!meta.hasLore())
            return stack;
        List<String> lore = meta.getLore();
        lore.remove(line);
        meta.setLore(lore);
        stack.setItemMeta(meta);
        return stack;
    }

    /**
     * 清空指定 IS 的 lore
     * 
     * @param stack 要清空的 IS
     * @return 清空后的 IS
     */
    public static ItemStack clearLore(ItemStack stack) {
        ItemMeta meta = stack.getItemMeta();
        if (!meta.hasLore())
            return stack;
        meta.getLore().clear();
        stack.setItemMeta(meta);
        return stack;
    }

    /**
     * 将指定 IS 的 lore 设置为指定值
     * 
     * @param lore  指定值
     * @param stack 指定 IS
     * @return 设置后的 IS
     */
    public static ItemStack setLore(List<String> lore, ItemStack stack) {
        ItemMeta meta = stack.getItemMeta();
        meta.setLore(lore);
        stack.setItemMeta(meta);
        return stack;
    }

    /**
     * 判断指定方块是否属于两格高整体植株，海带会返回 {@code}false{@code}
     * 
     * @param b 指定方块
     * @return
     */
    public static boolean isTallPlant(Block b) {
        Material type = b.getType();
        switch (type) {
            case ROSE_BUSH:
            case TALL_GRASS:
            case LILAC:
            case PEONY:
            case LARGE_FERN:
                return true;

            default:
                return false;
        }
    }
}
