package org.sotap.MissionTap.Events;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.ItemMergeEvent;
import org.bukkit.inventory.ItemStack;
import org.sotap.MissionTap.MissionTap;
import org.sotap.MissionTap.Utils.Identifiers;

public final class Preventers implements Listener {
    public Map<Location, Block> manuallyPlacedBlocks;

    public Preventers(MissionTap plugin) {
        this.manuallyPlacedBlocks = new HashMap<>();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent e) {
        Block b = e.getBlock();
        manuallyPlacedBlocks.put(b.getLocation(), b);
    }

    @EventHandler(ignoreCancelled = true)
    public void onItemMerge(ItemMergeEvent e) {
        ItemStack aStack = e.getEntity().getItemStack();
        ItemStack bStack = e.getTarget().getItemStack();
        if (Identifiers.isValidIdentified(aStack) || Identifiers.isValidIdentified(bStack)) {
            e.setCancelled(true);
        }
    }
}
