package org.sotap.MissionTap.Events;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.sotap.MissionTap.MissionTap;
import org.sotap.MissionTap.Utils.Functions;

public final class Preventers implements Listener {
    public List<UUID> manuallyDroppedItems;
    public Map<Location, Block> manuallyPlacedBlocks;

    public Preventers(MissionTap plugin) {
        this.manuallyDroppedItems = new ArrayList<>();
        this.manuallyPlacedBlocks = new HashMap<>();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDropItem(PlayerDropItemEvent e) {
        manuallyDroppedItems.add(e.getItemDrop().getUniqueId());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockDispense(BlockDispenseEvent e) {
        ItemStack item = e.getItem();
        e.setItem(Functions.addLore("dispensed", item));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e) {
        Block b = e.getBlock();
        BlockState state = b.getState();
        if (!(state instanceof InventoryHolder))
            return;
        InventoryHolder holder = (InventoryHolder) state;
        Inventory inventory = holder.getInventory();
        List<ItemStack> progressed = new ArrayList<>();
        for (ItemStack stack : inventory.getContents()) {
            if (Functions.isEmptyItemStack(stack))
                continue;
            progressed.add(Functions.addLore("from-container", stack));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent e) {
        Block b = e.getBlock();
        manuallyPlacedBlocks.put(b.getLocation(), b);
    }
}
