package org.sotap.MissionTap.Classes;

import java.util.List;
import java.util.UUID;
import org.bukkit.configuration.ConfigurationSection;
import org.sotap.MissionTap.Utils.Calendars;
import org.sotap.MissionTap.Utils.Files;

public final class GlobalMission {
    public final String type;
    public final ConfigurationSection missions;

    public GlobalMission(String type) {
        this.type = type;
        this.missions = Files.getGeneratedMissions(type);
    }

    public void accept() {
        List<UUID> uuids = Files.getAllPlayerUUID();
        for (String key : missions.getValues(false).keySet()) {
            for (UUID u : uuids) {
                clearExpiredMissions(u);
                Mission m = new Mission(type, key);
                m.accept(u);
            }
        }
    }

    public void clearExpiredMissions(UUID u) {
        ConfigurationSection section = Files.loadPlayer(u).getConfigurationSection(type);
        if (Files.isEmptyConfiguration(section)) return;
        for (String key : section.getValues(false).keySet()) {
            if (section.getConfigurationSection(key).getLong("expiration") > Calendars.getNow()) continue;
            section.set(key, null);
        }
    }
}
