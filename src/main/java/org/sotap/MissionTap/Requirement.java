package org.sotap.MissionTap;

import java.util.Map;
import org.bukkit.configuration.ConfigurationSection;
import org.sotap.MissionTap.Utils.G;

public final class Requirement {
    public String type;
    public String key;
    // NOTE: The compareType needs '-data' suffix to work.
    public String compareType;
    public ConfigurationSection contents;
    public ConfigurationSection toCompareCS;

    public Requirement(ConfigurationSection playerdata, String type, String key, String compareType) {
        this.toCompareCS = playerdata.getConfigurationSection(compareType + "-data");
        this.compareType = compareType;
        this.contents = G.load("latest-missions.yml")
                .getConfigurationSection(type + "." + key + ".contents");
    }

    public boolean met() {
        // if the requirements does not exists, return true.
        if (contents == null) return true;
        ConfigurationSection actualContents = contents.getConfigurationSection(compareType);
        if (toCompareCS == null && actualContents == null)
            return true;
        if (toCompareCS == null && actualContents != null)
            return false;
        if (toCompareCS != null && actualContents == null)
            return true;
        if (toCompareCS != null && actualContents != null) {
            // compare as requirements of the mission.
            Map<String, Integer> compare = cast(actualContents.getValues(false));
            // toCompare as the actual progress of the player.
            Map<String, Integer> toCompare = cast(toCompareCS.getValues(false));
            for (String k : compare.keySet()) {
                // if one's actual progress is less than the requirement or the progress is null, return false.
                if (toCompare.get(k) == null)
                    return false;
                if (compare.get(k) > toCompare.get(k))
                    return false;
            }
            // otherwise, return true.
            return true;
        }
        return false;
    }

    /**
     * Forcefully convert Map(String,Object) to Map(String,Integer) in case that the type of values is confirmed as Integer.
     * 
     * It's unnecessary to use a `for` loop here, and it's smarter to cheat the compiler.
     * 
     * @param object Map(String,Object) to convert
     * @return Map(String,Integer)
     */
    public Map<String, Integer> cast(Map<String, Object> object) {
        @SuppressWarnings({"unchecked", "rawtypes"})
        Map<String, Integer> hack = (Map) object;
        return hack;
    }
}
