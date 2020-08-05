package org.sotap.MissionTap;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.sotap.MissionTap.Utils.G;

public final class Acceptance {
    public String key;
    public String type;
    public String name;
    public boolean finished;
    public long acceptanceTime;
    public long expirationTime;
    private ConfigurationSection acc;
    private FileConfiguration data;

    public Acceptance(String key, FileConfiguration data, String type, String name) {
        this.key = key;
        this.type = type;
        this.data = data;
        if (data != null) {
            this.acc = data.getConfigurationSection(type + "." + key);
            this.name = acc.getString("name");
            this.acceptanceTime = acc.getLong("acceptance-time");
            this.expirationTime = acc.getLong("expiration-time");
        } else {
            this.name = name;
        }
    }

    public Map<String, Object> getAcceptance() {
        Map<String, Object> acc = new HashMap<>();
        FileConfiguration missions = G.load("latest-missions.yml");
        Long nextUpdateTime = missions.getLong(type + "-next-regen");
        acc.put("name", name);
        acc.put("acceptance-time", new Date().getTime());
        acc.put("expiration-time", nextUpdateTime);
        acc.put("finished", false);
        return acc;
    }

    public void delete(UUID u) {
        data.set(type + "." + key, null);
        G.savePlayer(data, u);
    }

    public void updateData(FileConfiguration data) {
        this.data = data;
        this.acc = data.getConfigurationSection(type + "." + key);
    }

    public boolean isFinished() {
        for (String compareType : new String[] {"blockbreak", "collecting", "breeding", "trading"}) {
            Requirement requirement = new Requirement(acc, type, key, compareType);
            if (!requirement.met()) return false;
        }
        return true;
    }
}