package org.sotap.MissionTap.Events;

import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.sotap.MissionTap.MissionTap;
import org.sotap.MissionTap.Utils.Functions;

public final class GlobalEvents implements Listener {
    public MissionTap plugin;

    public GlobalEvents(MissionTap plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        UUID u = p.getUniqueId();
        Functions.initDataForPlayer(u);
    }
}