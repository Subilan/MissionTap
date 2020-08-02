package org.sotap.MissionTap;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
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

    public Acceptance(String key, FileConfiguration data, String type, String name) {
        this.key = key;
        this.type = type;
        if (data != null) {
            this.acc = data.getConfigurationSection(type + "." + key);
            this.name = acc.getString("name");
            this.finished = acc.getBoolean("finished");
            this.acceptanceTime = acc.getLong("acceptance-time");
            this.expirationTime = acc.getLong("expiration-time");
        } else {
            this.name = name;
        }
    }

    public Map<String,Object> getAcceptance() {
        Map<String,Object> acc = new HashMap<>();
        FileConfiguration missions = G.load("latest-missions.yml");
        Long nextUpdateTime = missions.getLong(type + "-next-regen");
        acc.put("name", name);
        acc.put("acceptance-time", new Date().getTime());
        acc.put("expiration-time", nextUpdateTime);
        acc.put("finished", false);
        return acc;
    }
}