package org.sotap.MissionTap;

import java.util.List;
import org.bukkit.configuration.ConfigurationSection;

public final class Mission {
    public String name;
    public List<String> description;
    public ConfigurationSection contents;
    public Integer pos;
    private ConfigurationSection mission;
    
    public Mission(Object missionObject) {
        this.mission = (ConfigurationSection) missionObject;
        this.name = mission.getString("name");
        this.description = mission.getStringList("description");
        this.contents = mission.getConfigurationSection("contents");
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