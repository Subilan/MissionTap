package org.sotap.MissionTap;

import java.util.List;
import org.bukkit.configuration.ConfigurationSection;

public final class Mission {
    public String name;
    public List<String> description;
    public ConfigurationSection contents;
    public String expires;
    private ConfigurationSection mission;
    
    public Mission(Object missionObject) {
        this.mission = (ConfigurationSection) missionObject;
        this.name = mission.getString("name");
        this.description = mission.getStringList("description");
        this.contents = mission.getConfigurationSection("contents");
        this.expires = mission.getString("expires");
    }
}