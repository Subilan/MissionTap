package org.sotap.MissionTap.Events;

import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityBreedEvent;
import org.sotap.MissionTap.MissionTap;
import org.sotap.MissionTap.Utils.G;

public final class MissionEvents implements Listener {
    public MissionTap plug;
    private MemoryConfiguration temp;

    public MissionEvents(MissionTap plug) {
        this.plug = plug;
        Bukkit.getPluginManager().registerEvents(this, plug);
    }

    // for BLOCKBREAK
    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        Player p = e.getPlayer();
        FileConfiguration playerdata = G.loadPlayer(p.getUniqueId());
        String blockname = e.getBlock().getType().toString();
        String dest;
        for (String type : new String[] {"daily", "weekly"}) {
            Set<String> keys = playerdata.getConfigurationSection(type).getKeys(false);
            if (keys.size() == 0)
                continue;
            for (String key : keys) {
                dest = type + "." + key + ".blockbreak-data." + blockname;
                playerdata.set(dest, playerdata.getInt(dest) + 1);
            }
        }
        G.savePlayer(playerdata, p.getUniqueId());
    }

    // for BREEDING
    @EventHandler
    public void onEntityBreed(EntityBreedEvent e) {
        UUID u = e.getBreeder().getUniqueId();
        if (G.isOnlinePlayer(u)) {
            FileConfiguration playerdata = G.loadPlayer(u);
            String dest;
            String entityName = e.getEntityType().toString();
            for (String type : new String[] {"daily", "weekly"}) {
                Set<String> keys = playerdata.getConfigurationSection(type).getKeys(false);
                if (keys.size() == 0)
                    continue;
                for (String key : keys) {
                    dest = type + "." + key + ".breeding-data." + entityName;
                    playerdata.set(dest, playerdata.getInt(dest) + 1);
                }
            }
            G.savePlayer(playerdata, u);
        }
    }
}
