package org.sotap.MissionTap.Events;

import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.sotap.MissionTap.MissionTap;
import org.sotap.MissionTap.Utils.G;

public final class MissionEvents implements Listener {
    public MissionTap plug;

    public MissionEvents(MissionTap plug) {
        this.plug = plug;
        Bukkit.getPluginManager().registerEvents(this, plug);
    }
}