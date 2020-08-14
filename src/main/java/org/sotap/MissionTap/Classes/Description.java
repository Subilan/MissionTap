package org.sotap.MissionTap.Classes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.sotap.MissionTap.Utils.Files;
import org.sotap.MissionTap.Utils.LogUtil;
import net.md_5.bungee.api.ChatColor;

public final class Description {
    public final ConfigurationSection playerObject;
    public final ConfigurationSection missionObject;

    public Description(UUID u, String type, String key) {
        FileConfiguration playerdata = Files.loadPlayer(u);
        FileConfiguration playermission = Files.getPlayerMissions(u);
        playerObject = playerdata.getConfigurationSection(type + "." + key);
        missionObject = playermission.getConfigurationSection(type + "." + key);
    }

    /**
     * 根据 dataType 获取对应的中文动词
     * 
     * @param dataType 要求类型
     * @return
     */
    public static String getDataTypeName(String dataType) {
        Map<String, String> map = new HashMap<>();
        map.put("collecting", "收集");
        map.put("blockbreak", "破坏");
        map.put("combat", "击杀");
        map.put("breeding", "繁殖");
        map.put("crafting", "合成");
        return map.get(dataType);
    }

    /**
     * 根据 dataType 获取对应的中文量词（部分不准）
     * 
     * @param dataType 要求类型
     * @return
     */
    public static String getQuantifier(String dataType) {
        Map<String, String> map = new HashMap<>();
        map.put("collecting", "个");
        map.put("blockbreak", "个");
        map.put("combat", "只");
        map.put("breeding", "对");
        map.put("crafting", "个");
        return map.get(dataType);
    }

    public static String getObjectName(String dataType) {
        Map<String, String> map = new HashMap<>();
        map.put("collecting", "物品");
        map.put("blockbreak", "方块");
        map.put("combat", "生物");
        map.put("breeding", "动物");
        map.put("crafting", "物品");
        return map.get(dataType);
    }

    /**
     * 获取介绍正文，返回的是一个 {@code}List(String){@code}
     * 
     * @param type 要求类型，选填、一个或多个字符串
     * @return
     */
    public List<String> getDescription(boolean... global) {
        if (missionObject == null)
            return null;
        final boolean finalGlobal = global.length == 0 ? false : global[0];
        ConfigurationSection data;
        List<String> result = new ArrayList<>();
        Integer amountLeft;
        Integer requirement;
        for (String dataType : Mission.missionDataTypes) {
            data = missionObject.getConfigurationSection(dataType);
            if (data == null) {
                requirement = missionObject.getInt(dataType);
                if (requirement == 0)
                    continue;
                amountLeft = getAnyAmountLeft(dataType, requirement);
                result.add(ChatColor.WHITE + LogUtil
                        .translateColor((finalGlobal ? "" : (amountLeft == requirement ? "" : "还需"))
                                + (amountLeft == 0 ? "&8&m&o" : "") + getDataTypeName(dataType)
                                + (amountLeft == 0 ? " " : " &e")
                                + (finalGlobal ? requirement : amountLeft)
                                + (amountLeft == 0 ? " " : "&r&f ") + getQuantifier(dataType) + "任意"
                                + getObjectName(dataType)));
            } else {
                for (String itemKey : data.getKeys(false)) {
                    requirement = data.getInt(itemKey);
                    amountLeft = getAmountLeft(dataType, itemKey, data);
                    result.add(ChatColor.WHITE + LogUtil.translateColor(
                            (finalGlobal ? "" : (amountLeft == requirement ? "" : "还需"))
                                    + (amountLeft == 0 ? "&8&m&o" : "") + getDataTypeName(dataType)
                                    + (amountLeft == 0 ? " " : " &e")
                                    + (finalGlobal ? requirement : amountLeft)
                                    + (amountLeft == 0 ? " " : "&r&f ") + getQuantifier(dataType)
                                    + Files.translations.getString(itemKey)));
                }
            }
        }
        return result;
    }

    /**
     * 获取用户剩余未完成的数值
     * 
     * @param type            要求类型
     * @param itemKey         要求项目名称
     * @param requirementData 要求的 CS 实例
     * @return
     */
    public Integer getAmountLeft(String type, String itemKey,
            ConfigurationSection requirementData) {
        ConfigurationSection playerdata = playerObject.getConfigurationSection(type + "-data");
        Integer requirement = requirementData.getInt(itemKey);
        if (playerdata == null)
            return requirement;
        Integer progress = playerdata.getInt(itemKey);
        return progress > requirement ? 0 : requirement - progress;
    }

    /**
     * 获取用户任意项未完成的数值
     * 
     * @param type      要求类型
     * @param anyAmount 任意项要求数值（需要任意项多少个）
     * @return
     */
    public Integer getAnyAmountLeft(String type, Integer anyAmount) {
        ConfigurationSection playerdata = playerObject.getConfigurationSection(type + "-data");
        if (playerdata == null)
            return anyAmount;
        Integer progress = 0;
        for (String itemKey : playerdata.getKeys(false)) {
            progress += playerdata.getInt(itemKey);
        }
        return progress > anyAmount ? 0 : anyAmount - progress;
    }
}
