package org.sotap.MissionTap;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.configuration.file.FileConfiguration;
import org.sotap.MissionTap.Utils.G;

public final class Acceptance {
    public Mission proto;
    public String key;

    public Acceptance(Mission m) {
        this.proto = m;
        this.key = m.key;    
    }

    public Map<String,Object> getAcceptance() {
        Map<String,Object> acc = new HashMap<>();
        FileConfiguration missions = G.loadYaml(G.cwd, "latest-missions.yml");
        Long nextUpdateTime = missions.getLong(proto.type + "-next-regen");
        acc.put("name", proto.name);
        acc.put("acceptance-time", new Date().getTime());
        acc.put("expiration-time", nextUpdateTime);
        acc.put("finished", false);
        return acc;
    }
}