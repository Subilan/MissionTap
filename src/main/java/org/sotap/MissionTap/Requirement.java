package org.sotap.MissionTap;

import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.configuration.ConfigurationSection;
import org.sotap.MissionTap.Utils.G;

public final class Requirement {
    public String type;
    public String key;
    public ConfigurationSection contents;
    public Map<String, Integer> toCompare;

    public Requirement(String type, String key, Map<String, Object> toCompare) {
        this.toCompare = cast(toCompare);
        this.contents = G.load("latest-missions").getConfigurationSection(type + "." + key + ".contents");
    }

    public boolean met() {
        Map<String, Integer> compare = cast(contents.getValues(false));
        for (String key : compare.keySet()) {
            if (compare.get(key) > toCompare.get(key))
                return false;
        }
        return true;
    }

    public Map<String,Integer> cast(Map<String,Object> object) {
        return object.entrySet().stream()
        .collect(Collectors.toMap(Map.Entry::getKey, e -> (Integer) e.getValue()));
    }
}