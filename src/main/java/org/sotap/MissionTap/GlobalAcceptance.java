package org.sotap.MissionTap;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.sotap.MissionTap.Utils.G;

public final class GlobalAcceptance {
    public String key;
    public String type;
    public String name;
    public boolean finished;
    public long acceptanceTime;
    public long expirationTime;
    private ConfigurationSection gacc;
    private ConfigurationSection globalData;
    private FileConfiguration data;
    private FileConfiguration playerdata;

    public GlobalAcceptance(String key, FileConfiguration playerdata, String type) {
        this.key = key;
        this.type = type;
        this.data = G.load("latest-missions.yml");
        this.gacc = data.getConfigurationSection(type + "." + key);
        this.playerdata = playerdata;
        this.globalData = playerdata.getConfigurationSection("global");
        this.name = gacc.getString("name");
        this.acceptanceTime = data.getLong(type + "-last-regen");
        this.expirationTime = data.getLong(type + "-next-regen");
    }

    public void updateData(FileConfiguration playerdata) {
        this.globalData = playerdata.getConfigurationSection("global");
    }

    public boolean isFinished() {
        for (String compareType : new String[] { "blockbreak", "collecting", "breeding", "trading" }) {
            if (globalData == null)
                return false;
            Requirement requirement = new Requirement(globalData, type, key, compareType);
            if (!requirement.met())
                return false;
        }
        return true;
    }

    public boolean isReceived() {
        List<String> received = playerdata.getStringList("global-received-list." + type);
        return received.contains(key);
    }

    public void setReceived(UUID u) {
        List<String> received = playerdata.getStringList("global-received-list." + type);
        received = received == null ? new ArrayList<>() : received;
        received.add(key);
        playerdata.set("global-received-list." + type, received);
        G.savePlayer(playerdata, u);
    }
}