package org.sotap.MissionTap.Events;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.sotap.MissionTap.MissionTap;
import org.sotap.MissionTap.Utils.Functions;

public final class Preventers implements Listener {
    public List<UUID> manuallyDroppedItems;

    public Preventers(MissionTap plugin) {
        this.manuallyDroppedItems = new ArrayList<>();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDropItem(PlayerDropItemEvent e) {
        manuallyDroppedItems.add(e.getItemDrop().getUniqueId());
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockDispense(BlockDispenseEvent e) {
        ItemStack item = e.getItem();
        e.setItem(Functions.addLore("dispensed", item));
    }
}
