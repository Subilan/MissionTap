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
import org.sotap.MissionTap.Utils.Logger;
import net.md_5.bungee.api.ChatColor;

public final class Mission {
    public String type;
    public String key;
    public final FileConfiguration missionFile;
    public final ConfigurationSection missions;
    public final ConfigurationSection object;

    public Mission(String type, String key) {
        this.type = type;
        this.key = key;
        this.missionFile = Files.getGeneratedMissionFile(type);
        this.missions = missionFile.getConfigurationSection(type);
        this.object = missions.getConfigurationSection(key);
    }

    public ItemStack getItemStack(UUID u) {
        List<String> lore = object.getStringList("lore");
        List<String> finalLore = new ArrayList<>();
        if (u != null) {
            finalLore.add(Logger.translateColor(isFinished(u) ? "&a&lFinished" : "&c&lUnfinished"));
            finalLore.add("");
        }
        for (String text : lore) {
            finalLore.add(ChatColor.WHITE + Logger.translateColor(text));
        }
        finalLore.add("");
        finalLore.add(Logger
                .translateColor("&8" + Calendars.stampToString(Calendars.getNextRefresh(type))));
        return Functions.createItemStack(object.getString("name"),
                u != null ? (isFinished(u) ? Material.ENCHANTED_BOOK : Material.BOOK)
                        : Material.BOOK,
                finalLore);
    }

    public void accept(UUID u) {
        FileConfiguration playerdata = Files.loadPlayer(u);
        Map<String, Object> missionContent = new HashMap<>();
        missionContent.put("name", object.getString("name"));
        missionContent.put("acceptance", Calendars.getNow());
        missionContent.put("expiration", missionFile.getLong("next-gen"));
        playerdata.createSection(type + "." + key, missionContent);
        Files.savePlayer(playerdata, u);
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
        FileConfiguration playerdata = Files.loadPlayer(u);
        try {
            return playerdata.getLong(type + "." + key + ".expiration") <= Calendars.getNow();
        } catch (NullPointerException e) {
            return false;
        }
    }

    public boolean isFinished(UUID u) {
        FileConfiguration playerdata = Files.loadPlayer(u);
        for (String missionType : new String[] {"blockbreak", "collecting", "breeding",
                "trading"}) {
            if (object.getConfigurationSection(missionType) == null)
                continue;
            if (playerdata.getConfigurationSection(
                    type + "." + key + "." + missionType + "-data") == null)
                return false;
            Map<String, Object> requirement =
                    object.getConfigurationSection(missionType).getValues(false);
            Map<String, Object> progress = playerdata
                    .getConfigurationSection(type + "." + key + "." + missionType + "-data")
                    .getValues(false);
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
        if (commands.size() == 0) return false;
        Functions.dispatchCommands(p, commands);
        try {
            AgeingAPI.api.updateExperience(ageExp, p.getName());
            p.sendMessage(Logger.translateColor(Logger.INFO + "向您的 Ageing 账户中添加了 " + ageExp + " 点经验。"));
        } catch (AgeingAPIException e) {
            p.sendMessage(Logger.translateColor(Logger.WARN + "在更新 Ageing 数据时出现问题。"));
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
        for (String missionType : new String[] {"blockbreak", "collecting", "breeding",
                "trading"}) {
            playerdata.set(type + "." + key + "." + missionType + "-data", null);
        }
    }

    public void clearDataWithRequirement(UUID u) {
        FileConfiguration playerdata = Files.loadPlayer(u);
        Integer result;
        for (String missionType : new String[] {"blockbreak", "collecting", "breeding",
                "trading"}) {
            if (object.getConfigurationSection(missionType) == null
                    || playerdata.getConfigurationSection(
                            type + "." + key + "." + missionType + "-data") == null)
                continue;
            Map<String, Object> requirement =
                    object.getConfigurationSection(missionType).getValues(false);
            Map<String, Object> progress = playerdata
                    .getConfigurationSection(type + "." + key + "." + missionType + "-data")
                    .getValues(false);
            for (String reqKey : requirement.keySet()) {
                // if is finished, the former value should be greater than the latter value.
                result = (Integer) progress.get(reqKey) - (Integer) requirement.get(reqKey);
                playerdata.set(type + "." + key + "." + missionType + "-data." + reqKey, result < 0 ? 0 : result);
            }
        }
        Files.savePlayer(playerdata, u);
    }
}
