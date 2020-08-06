package org.sotap.MissionTap;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
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
    private boolean allowMultiple;

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
        this.allowMultiple = G.config.getInt("multiple-acceptance-cooldown") != -1;
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
        for (String compareType : new String[] { "blockbreak", "collecting", "breeding", "trading" }) {
            Requirement requirement = new Requirement(acc, type, key, compareType);
            if (!requirement.met())
                return false;
        }
        return true;
    }

    public boolean isReceived() {
        // If the multiple acceptance is allowed
        if (allowMultiple)
            return false;
        List<String> received = data.getStringList("received-list." + type);
        return received.contains(key);
    }

    public void setReceived(UUID u) {
        if (allowMultiple)
            return;
        List<String> received = data.getStringList("received-list." + type);
        received = received == null ? new ArrayList<>() : received;
        received.add(key);
        data.set("received-list." + type, received);
        G.savePlayer(data, u);
    }

    public void setNotReceived(UUID u) {
        if (allowMultiple)
            return;
        List<String> received = data.getStringList("received-list." + type);
        if (received == null)
            return;
        received.remove(key);
        data.set("received-list", received);
        G.savePlayer(data, u);
    }
}