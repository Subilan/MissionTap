package org.sotap.MissionTap.Classes;

import java.util.*;

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
            {"blockbreak", "breeding", "combat", "crafting", "collecting"};

    public Mission(UUID u, String type, String key) {
        this.type = type;
        this.key = key;
        if (!Functions.eq(type, "special")) {
            this.missionFile = Files.getPlayerMissions(u);
            this.missions = missionFile.getConfigurationSection(type);
        } else {
            this.missionFile = Files.specialMissions;
            this.missions = Files.specialMissions;
        }
        this.object = Objects.requireNonNull(missions).getConfigurationSection(key);
        this.playerdata = Files.loadPlayer(u);
        this.u = u;
    }

    /**
     * 获取该任务的 ItemStack 图标，参数中的全局内容是指是否显示在 MainMenu 类菜单中，若不填则为 false
     *
     * @param global 是否为全局内容
     * @return
     */
    public ItemStack getItemStack(boolean global) {
        List<String> lore = object.getStringList("lore");
        List<String> finalLore = new ArrayList<>();
        long expiration = 0L;
        long refresh = Files.meta.getLong(type + ".next-regen");
        if (!global) {
            expiration = Files.loadPlayer(u).getLong(type + "." + key + ".expiration");
            if (Files.config.getBoolean("require-submittion")) {
                finalLore.add(LogUtil.translateColor(isFinished() ? "&a&l已完成" : "&c&l未完成"));
                finalLore.add("");
            }
        }
        if (Calendars.timeOffset != 0) {
            expiration -= Calendars.timeOffset * 3600000;
            refresh -= Calendars.timeOffset * 3600000;
        }
        final Description desc = new Description(u, type, key);
        List<String> descList = desc.getDescription(global);
        if (descList != null) {
            finalLore.addAll(descList);
        }
        finalLore.add("");
        for (String text : lore) {
            finalLore.add(ChatColor.WHITE + LogUtil.translateColor(text));
        }
        finalLore.add("");
        if (!Functions.eq(type, "special")) {
            finalLore.add(
                    LogUtil.translateColor("&8" + ((!global) ? Calendars.stampToString(expiration)
                            : Calendars.stampToString(refresh))));
        }
        return Functions.createItemStack(
                LogUtil.translateColor(
                        (Functions.eq(type, "special") ? "&l&6" : "") + getName()),
                (!global) ? (isFinished() ? Material.ENCHANTED_BOOK : Material.BOOK)
                        : Material.BOOK,
                finalLore);
    }

    public void accept() {
        Map<String, Object> missionContent = new HashMap<>();
        missionContent.put("name", getName());
        missionContent.put("acceptance", Calendars.getNow());
        if (!Functions.eq(type, "special")) {
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
            return Objects.requireNonNull(playerdata.getConfigurationSection(type)).getKeys(false).contains(key);
        } catch (NullPointerException e) {
            return false;
        }
    }

    public boolean isExpired() {
        if (Functions.eq(type, "special"))
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
                int anyRequirement = object.getInt(dataType);
                if (anyRequirement == 0) {
                    continue;
                }
                Map<String, Object> progress;
                try {
                    progress = Objects.requireNonNull(playerdata
                            .getConfigurationSection(type + "." + key + "." + dataType + "-data"))
                            .getValues(false);
                } catch (NullPointerException e) {
                    return false;
                }
                Integer total = 0;
                for (String progKey : progress.keySet()) {
                    total += (Integer) progress.get(progKey);
                }
                if (anyRequirement > total) {
                    return false;
                }
            } else {
                Map<String, Object> progress;
                Map<String, Object> requirement;
                try {
                    progress = Objects.requireNonNull(playerdata
                            .getConfigurationSection(type + "." + key + "." + dataType + "-data"))
                            .getValues(false);
                } catch (NullPointerException e) {
                    return false;
                }
                try {
                    requirement =
                            Objects.requireNonNull(object.getConfigurationSection(dataType)).getValues(false);
                } catch (NullPointerException e) {
                    return true;
                }
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
        int ageExp = object.getInt("age-exp");
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
        submittedList.add(key);
        playerdata.set("submitted-list." + type, submittedList);
        Files.savePlayer(playerdata, u);
    }

    public boolean isSubmitted() {
        List<String> submittedList = playerdata.getStringList("submitted-list." + type);
        if (submittedList.size() > 0) {
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
        int result;
        for (String dataType : missionDataTypes) {
            if (object.getConfigurationSection(dataType) == null || playerdata
                    .getConfigurationSection(type + "." + key + "." + dataType + "-data") == null)
                continue;
            Map<String, Object> requirement;
            Map<String, Object> progress;
            try {
                requirement = Objects.requireNonNull(object.getConfigurationSection(dataType)).getValues(false);
                progress = Objects.requireNonNull(playerdata.getConfigurationSection(type + "." + key + "." + dataType + "-data"))
                        .getValues(false);
            } catch (NullPointerException e) {
                return;
            }
            for (String reqKey : requirement.keySet()) {
                result = (Integer) progress.get(reqKey) - (Integer) requirement.get(reqKey);
                playerdata.set(type + "." + key + "." + dataType + "-data." + reqKey,
                        Math.max(result, 0));
            }
        }
        Files.savePlayer(playerdata, u);
    }
}
