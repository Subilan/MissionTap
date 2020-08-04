package org.sotap.MissionTap;

import java.util.Map;
import java.util.UUID;
import org.bukkit.configuration.ConfigurationSection;
import org.sotap.MissionTap.Utils.G;

public final class Requirement {
    public String type;
    public String key;
    public String compareType;
    public ConfigurationSection contents;
    public ConfigurationSection toCompareCS;

    public Requirement(UUID u, String type, String key, String compareType) {
        this.toCompareCS = G.loadPlayer(u).getConfigurationSection(type + "."  + key + "." + compareType + "-data");
        System.out.println(type + "."  + key + "." + compareType + "-data");
        this.compareType = compareType;
        this.contents = G.load("latest-missions.yml")
                .getConfigurationSection(type + "." + key + ".contents");
    }

    public boolean met() {
        ConfigurationSection actualContents = contents.getConfigurationSection(compareType);
        if (toCompareCS == null && actualContents == null) return true;
        if (toCompareCS == null && actualContents != null) return false;
        if (toCompareCS != null && actualContents == null) return true;
        if (toCompareCS != null && actualContents != null) {
            Map<String, Integer> compare = cast(actualContents.getValues(false));
            Map<String, Integer> toCompare = cast(toCompareCS.getValues(false));
            for (String k : compare.keySet()) {
                if (toCompare.get(k) == null) continue;
                if (compare.get(k) > toCompare.get(k))
                    return false;
            }
            return true;
        }
        return false;
    }

    /**
     * 强行让编译器将 Map(String,Object) 作为 Map(String,Integer) 来处理 这种方法有效解决了性能上的无谓损失，因为已经确定了其所有值必为 Integer
     * 若通过循环来创建一个新的 Map 则显得没有必要。因此通过这种欺骗编译器的方式来解决。
     * 
     * @param object 要强行识别的 Map(String,Object)
     * @return Map(String,Integer)
     */
    public Map<String, Integer> cast(Map<String, Object> object) {
        @SuppressWarnings({"unchecked", "rawtypes"})
        Map<String, Integer> hack = (Map) object;
        return hack;
    }
}
