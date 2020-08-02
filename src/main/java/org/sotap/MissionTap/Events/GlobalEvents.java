package org.sotap.MissionTap.Events;

import java.io.File;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.sotap.MissionTap.MissionTap;
import org.sotap.MissionTap.Utils.G;

public final class GlobalEvents implements Listener {
    public final MissionTap plug;

    public GlobalEvents(MissionTap plug) {
        this.plug = plug;
        Bukkit.getPluginManager().registerEvents(this, plug);
    }

    public boolean playerdataExists(UUID uuid) {
        File f = new File(plug.getDataFolder().getPath() + "/playerdata/" + uuid.toString() + ".yml");
        return !f.isDirectory() && f.exists();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        UUID u = p.getUniqueId();
        if (!p.hasPlayedBefore() || !playerdataExists(u)) {
            FileConfiguration playerdata = G.loadPlayer(u);
            playerdata.set("daily", -1);
            playerdata.set("weekly", -1);
            G.savePlayer(playerdata, u);
        }
    }
}