package org.sotap.MissionTap.Events;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.sotap.MissionTap.MissionTap;
import org.sotap.MissionTap.Classes.Mission;
import org.sotap.MissionTap.Utils.Files;
import org.sotap.MissionTap.Utils.Functions;

public final class MissionEvents implements Listener {
    private List<UUID> droppedItem;

    public MissionEvents(MissionTap plugin) {
        this.droppedItem = new ArrayList<>();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public static void updateData(UUID u, String missionType, String dataName) {
        FileConfiguration playerdata = Files.loadPlayer(u);
        for (String type : new String[] { "daily", "weekly", "special"} ) {
            ConfigurationSection section = playerdata.getConfigurationSection(type);
            if (section == null || Files.isEmptyConfiguration(section)) continue;
            Map<String,Object> data = section.getValues(false);
            String dest = "";
            for (String key : data.keySet()) {
                dest = missionType + "-data" + "." + dataName;
                ConfigurationSection object = (ConfigurationSection) data.get(key);
                object.set(dest, object.getInt(dest) + 1);
            }
        }
        Files.savePlayer(playerdata, u);
    }

    public static void handleAutoSubmittion(Player p) {
        if (Files.config.getBoolean("require-submittion")) return;
        UUID u = p.getUniqueId();
        FileConfiguration playerdata = Files.loadPlayer(u);
        for (String type : new String[] { "daily", "weekly", "special"} ) {
            ConfigurationSection section = playerdata.getConfigurationSection(type);
            if (section == null || Files.isEmptyConfiguration(section)) continue;
            for (String key : section.getKeys(false)) {
                Mission m = new Mission(type, key);
                if (m.isFinished(u)) {
                    Functions.finishMission(m, p);
                }
            }
        }
    }

    // cheating action filter
    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent e) {
        droppedItem.add(e.getItemDrop().getUniqueId());
    }

    @EventHandler
    public void onPlayerPickupItem(EntityPickupItemEvent e) {
        if (e.getEntityType() != EntityType.PLAYER) return;
        if (droppedItem.contains(e.getItem().getUniqueId())) return;
        Player p = (Player) e.getEntity();
        updateData(p.getUniqueId(), "collecting", e.getItem().getItemStack().getType().toString());
        handleAutoSubmittion(p);
    }

    @EventHandler
    public void onEntityBreeding(EntityBreedEvent e) {
        if (e.getBreeder().getType() != EntityType.PLAYER) return;
        Player p = (Player) e.getBreeder();
        updateData(p.getUniqueId(), "breeding", e.getEntity().getType().toString());
        handleAutoSubmittion(p);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        Player p = e.getPlayer();
        updateData(p.getUniqueId(), "blockbreak", e.getBlock().getType().toString());
        handleAutoSubmittion(p);
    }
}