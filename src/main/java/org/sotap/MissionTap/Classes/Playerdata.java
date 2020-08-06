package org.sotap.MissionTap.Classes;

import java.util.UUID;
import org.bukkit.configuration.file.FileConfiguration;
import org.sotap.MissionTap.Utils.Files;

public final class Playerdata {
    public UUID uuid;
    public FileConfiguration data;

    public Playerdata(UUID u) {
        this.data = Files.load("./playerdata", u.toString());
        this.uuid = u;
    }

    public void save() {
        Files.save(data, "./playerdata/" + uuid.toString() + ".yml");
    }
}
