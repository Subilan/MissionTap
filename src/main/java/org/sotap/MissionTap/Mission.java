package org.sotap.MissionTap;

import java.util.List;
import org.bukkit.configuration.ConfigurationSection;

public final class Mission {
    public String key;
    public String name;
    public List<String> description;
    public ConfigurationSection contents;
    public Integer pos;
    private ConfigurationSection mission;
    public Object missionObject;
    
    public Mission(String key, Object missionObject) {
        this.mission = (ConfigurationSection) missionObject;
        this.missionObject = missionObject;
        this.name = mission.getString("name");
        this.description = mission.getStringList("description");
        this.contents = mission.getConfigurationSection("contents");
        this.key = key;
    }

    public void setPosition(Integer pos) {
        this.pos = pos;
    }
    
    // Purpose: beautify the codes to be more human-readable.
    public ConfigurationSection getBlockbreak() {
        return contents.getConfigurationSection("blockbreak");
    }

    public ConfigurationSection getBreeding() {
        return contents.getConfigurationSection("breeding");
    }

    public ConfigurationSection getCollecting() {
        return contents.getConfigurationSection("collecting");
    }

    public ConfigurationSection getTrading() {
        return contents.getConfigurationSection("trading");
    }
}