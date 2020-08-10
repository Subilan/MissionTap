package org.sotap.MissionTap.Classes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.sotap.Ageing.Exception.AgeingAPIException;
import org.sotap.MissionTap.Utils.Calendars;
import org.sotap.MissionTap.Utils.Files;
import org.sotap.MissionTap.Utils.Functions;
import org.sotap.MissionTap.Utils.LogUtil;
import net.md_5.bungee.api.ChatColor;

public final class Mission {
    public String type;
    public String key;
    public final FileConfiguration missionFile;
    public final ConfigurationSection missions;
    public final ConfigurationSection object;
    public static String[] missionTypes = { "daily", "weekly", "special" };
    public static String[] missionDataTypes = { "blockbreak", "collecting", "breeding", "trading" };

    public Mission(String type, String key) {
        this.type = type;
        this.key = removeAllSuffix(key);
        this.missionFile = Files.getGeneratedMissionFile(type);
        this.missions = Files.getGeneratedMissions(type);
        this.object = missions.getConfigurationSection(key);
    }

    public ItemStack getItemStack(UUID u) {
        List<String> lore = object.getStringList("lore");
        List<String> finalLore = new ArrayList<>();
        Long expiration = 0L;
        Long refresh = missionFile.getLong("next-gen");
        if (u != null) {
            expiration = Files.loadPlayer(u).getLong(type + "." + key + ".expiration");
            finalLore.add(LogUtil.translateColor(isFinished(u) ? "&a&lFinished" : "&c&lUnfinished"));
            finalLore.add("");
        }
        for (String text : lore) {
            finalLore.add(ChatColor.WHITE + LogUtil.translateColor(text));
        }
        finalLore.add("");
        if (type != "special") {
            finalLore.add(LogUtil.translateColor(
                    "&8" + (u != null ? Calendars.stampToString(expiration) : Calendars.stampToString(refresh))));
        }
        return Functions.createItemStack(object.getString("name"),
                u != null ? (isFinished(u) ? Material.ENCHANTED_BOOK : Material.BOOK) : Material.BOOK, finalLore);
    }

    public void accept(UUID u) {
        FileConfiguration playerdata = Files.loadPlayer(u);
        Map<String, Object> missionContent = new HashMap<>();
        missionContent.put("name", object.getString("name"));
        missionContent.put("acceptance", Calendars.getNow());
        if (type != "special") {
            missionContent.put("expiration", Calendars.getMissionExpiration(type));
        }
        playerdata.createSection(type + "." + key + getDuplicatedNameSuffix(u, playerdata), missionContent);
        Files.savePlayer(playerdata, u);
    }

    public String getDuplicatedNameSuffix(UUID u, FileConfiguration playerdata) {
        ConfigurationSection section = playerdata.getConfigurationSection(type);
        List<String> match = new ArrayList<>();
        for (String objectKey : section.getKeys(false)) {
            if (!objectKey.startsWith(key))
                continue;
            match.add(objectKey);
        }
        String largestKey = Collections.max(match, Comparator.comparing(String::length));
        int underlineCount = StringUtils.countMatches(largestKey, "_");
        return "_".repeat(underlineCount + 1);
    }

    public String removeAllSuffix(String target) {
        return StringUtils.remove(target, "_");
    }

    public String getName() {
        return object.getString("name");
    }

    public boolean isAccepted(UUID u) {
        FileConfiguration playerdata = Files.loadPlayer(u);
        try {
            return playerdata.getConfigurationSection(type).getKeys(false).contains(key);
        } catch (NullPointerException e) {
            return false;
        }
    }

    public boolean isExpired(UUID u) {
        if (type == "special")
            return false;
        FileConfiguration playerdata = Files.loadPlayer(u);
        try {
            return playerdata.getLong(type + "." + key + ".expiration") <= Calendars.getNow();
        } catch (NullPointerException e) {
            return false;
        }
    }

    public boolean isFinished(UUID u) {
        FileConfiguration playerdata = Files.loadPlayer(u);
        for (String dataType : missionDataTypes) {
            if (object.getConfigurationSection(dataType) == null)
                continue;
            if (playerdata.getConfigurationSection(type + "." + key + "." + dataType + "-data") == null)
                return false;
            Map<String, Object> requirement = object.getConfigurationSection(dataType).getValues(false);
            Map<String, Object> progress = playerdata
                    .getConfigurationSection(type + "." + key + "." + dataType + "-data").getValues(false);
            for (String reqKey : requirement.keySet()) {
                if (progress.get(reqKey) == null)
                    return false;
                if ((Integer) progress.get(reqKey) < (Integer) requirement.get(reqKey))
                    return false;
            }
        }
        return true;
    }

    public void destory(UUID u) {
        FileConfiguration playerdata = Files.loadPlayer(u);
        playerdata.set(type + "." + key, null);
        Files.savePlayer(playerdata, u);
    }

    public boolean reward(Player p) {
        List<String> commands = object.getStringList("rewards");
        Integer ageExp = object.getInt("age-exp");
        if (commands.size() == 0 && ageExp == 0)
            return false;
        Functions.dispatchCommands(p, commands);
        if (ageExp > 0) {
            try {
                AgeingAPI.api.updateExperience(ageExp, p.getName());
                LogUtil.info("向您的 Ageing 账户中添加了 " + ageExp + " 点经验。", p);
            } catch (AgeingAPIException e) {
                LogUtil.warn("在更新 Ageing 数据时出现问题。", p);
            }
        }
        return true;
    }

    public void setSubmitted(UUID u) {
        FileConfiguration playerdata = Files.loadPlayer(u);
        List<String> submittedList = playerdata.getStringList("submitted-list." + type);
        submittedList = submittedList == null ? new ArrayList<>() : submittedList;
        submittedList.add(key);
        playerdata.set("submitted-list." + type, submittedList);
        Files.savePlayer(playerdata, u);
    }

    public boolean isSubmitted(UUID u) {
        FileConfiguration playerdata = Files.loadPlayer(u);
        List<String> submittedList = playerdata.getStringList("submitted-list." + type);
        if (submittedList != null) {
            return submittedList.contains(key);
        }
        return false;
    }

    public void clearData(UUID u) {
        FileConfiguration playerdata = Files.loadPlayer(u);
        for (String dataType : missionDataTypes) {
            playerdata.set(type + "." + key + "." + dataType + "-data", null);
        }
        Files.savePlayer(playerdata, u);
    }

    public void clearDataWithRequirement(UUID u) {
        FileConfiguration playerdata = Files.loadPlayer(u);
        Integer result;
        for (String dataType : missionDataTypes) {
            if (object.getConfigurationSection(dataType) == null
                    || playerdata.getConfigurationSection(type + "." + key + "." + dataType + "-data") == null)
                continue;
            Map<String, Object> requirement = object.getConfigurationSection(dataType).getValues(false);
            Map<String, Object> progress = playerdata
                    .getConfigurationSection(type + "." + key + "." + dataType + "-data").getValues(false);
            for (String reqKey : requirement.keySet()) {
                // if is finished, the former value should be greater than the latter value.
                result = (Integer) progress.get(reqKey) - (Integer) requirement.get(reqKey);
                playerdata.set(type + "." + key + "." + dataType + "-data." + reqKey, result < 0 ? 0 : result);
            }
        }
        Files.savePlayer(playerdata, u);
    }
}
