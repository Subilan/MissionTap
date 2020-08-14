package org.sotap.MissionTap.Classes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
    public final FileConfiguration playerdata;
    public final UUID u;
    public static String[] missionTypes = {"daily", "weekly", "special"};
    public static String[] missionDataTypes =
            {"blockbreak", "collecting", "breeding", "combat", "crafting"};

    public Mission(UUID u, String type, String key) {
        this.type = type;
        this.key = key;
        if (type != "special") {
            this.missionFile = Files.getPlayerMissions(u);
            this.missions = missionFile.getConfigurationSection(type);
        } else {
            this.missionFile = Files.specialMissions;
            this.missions = Files.specialMissions;
        }
        this.object = missions.getConfigurationSection(key);
        this.playerdata = Files.loadPlayer(u);
        this.u = u;
    }

    /**
     * 获取该任务的 ItemStack 图标，参数中的全局内容是指是否显示在 MainMenu 类菜单中，若不填则为 false
     * @param global 是否为全局内容
     * @return
     */
    public ItemStack getItemStack(boolean... global) {
        final boolean finalGlobal = global.length == 0 ? false : global[0];
        List<String> lore = object.getStringList("lore");
        List<String> finalLore = new ArrayList<>();
        Long expiration = 0L;
        Long refresh = Files.meta.getLong(type + ".next-regen");
        if (!finalGlobal) {
            expiration = Files.loadPlayer(u).getLong(type + "." + key + ".expiration");
            if (Files.config.getBoolean("require-submittion")) {
                finalLore.add(
                        LogUtil.translateColor(isFinished() ? "&a&lFinished" : "&c&lUnfinished"));
                finalLore.add("");
            }
        }
        final Description desc = new Description(u, type, key);
        finalLore.addAll(desc.getDescription());
        finalLore.add("");
        for (String text : lore) {
            finalLore.add(ChatColor.WHITE + LogUtil.translateColor(text));
        }
        finalLore.add("");
        if (type != "special") {
            finalLore.add(LogUtil
                    .translateColor("&8" + ((!finalGlobal) ? Calendars.stampToString(expiration)
                            : Calendars.stampToString(refresh))));
        }
        return Functions.createItemStack(object.getString("name"),
                (!finalGlobal) ? (isFinished() ? Material.ENCHANTED_BOOK : Material.BOOK)
                        : Material.BOOK,
                finalLore);
    }

    public void accept() {
        Map<String, Object> missionContent = new HashMap<>();
        missionContent.put("name", object.getString("name"));
        missionContent.put("acceptance", Calendars.getNow());
        if (type != "special") {
            missionContent.put("expiration", Calendars.getMissionExpiration(type));
        }
        playerdata.createSection(type + "." + key, missionContent);
        Files.savePlayer(playerdata, u);
    }

    public String getName() {
        return object.getString("name");
    }

    public boolean isAccepted() {
        try {
            return playerdata.getConfigurationSection(type).getKeys(false).contains(key);
        } catch (NullPointerException e) {
            return false;
        }
    }

    public boolean isExpired() {
        if (type == "special")
            return false;
        try {
            return playerdata.getLong(type + "." + key + ".expiration") <= Calendars.getNow();
        } catch (NullPointerException e) {
            return false;
        }
    }

    public boolean isFinished() {
        for (String dataType : missionDataTypes) {
            if (object.getConfigurationSection(dataType) == null) {
                Integer anyRequirement = object.getInt(dataType);
                if (anyRequirement == 0) {
                    continue;
                }
                if (playerdata.getConfigurationSection(
                        type + "." + key + "." + dataType + "-data") == null)
                    return false;
                Map<String, Object> progress = playerdata
                        .getConfigurationSection(type + "." + key + "." + dataType + "-data")
                        .getValues(false);
                Integer total = 0;
                for (String progKey : progress.keySet()) {
                    total += (Integer) progress.get(progKey);
                }
                if (anyRequirement > total) {
                    return false;
                }
            } else {
                if (playerdata.getConfigurationSection(
                        type + "." + key + "." + dataType + "-data") == null)
                    return false;
                Map<String, Object> progress = playerdata
                        .getConfigurationSection(type + "." + key + "." + dataType + "-data")
                        .getValues(false);
                Map<String, Object> requirement =
                        object.getConfigurationSection(dataType).getValues(false);
                for (String reqKey : requirement.keySet()) {
                    if (progress.get(reqKey) == null)
                        return false;
                    if ((Integer) progress.get(reqKey) < (Integer) requirement.get(reqKey))
                        return false;
                }
            }
        }
        return true;
    }

    public void destory() {
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

    public void setSubmitted() {
        List<String> submittedList = playerdata.getStringList("submitted-list." + type);
        submittedList = submittedList == null ? new ArrayList<>() : submittedList;
        submittedList.add(key);
        playerdata.set("submitted-list." + type, submittedList);
        Files.savePlayer(playerdata, u);
    }

    public boolean isSubmitted() {
        List<String> submittedList = playerdata.getStringList("submitted-list." + type);
        if (submittedList != null) {
            return submittedList.contains(key);
        }
        return false;
    }

    public void clearData() {
        for (String dataType : missionDataTypes) {
            playerdata.set(type + "." + key + "." + dataType + "-data", null);
        }
        Files.savePlayer(playerdata, u);
    }

    public void clearDataWithRequirement() {
        Integer result;
        for (String dataType : missionDataTypes) {
            if (object.getConfigurationSection(dataType) == null || playerdata
                    .getConfigurationSection(type + "." + key + "." + dataType + "-data") == null)
                continue;
            Map<String, Object> requirement =
                    object.getConfigurationSection(dataType).getValues(false);
            Map<String, Object> progress =
                    playerdata.getConfigurationSection(type + "." + key + "." + dataType + "-data")
                            .getValues(false);
            for (String reqKey : requirement.keySet()) {
                // if is finished, the former value should be greater than the latter value.
                result = (Integer) progress.get(reqKey) - (Integer) requirement.get(reqKey);
                playerdata.set(type + "." + key + "." + dataType + "-data." + reqKey,
                        result < 0 ? 0 : result);
            }
        }
        Files.savePlayer(playerdata, u);
    }
}
