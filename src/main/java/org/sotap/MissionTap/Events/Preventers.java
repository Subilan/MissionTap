package org.sotap.MissionTap.Events;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
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

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e) {
        Block b = e.getBlock();
        BlockState state = b.getState();
        if (!(state instanceof InventoryHolder)) return;
        InventoryHolder holder = (InventoryHolder) state;
        Inventory inventory = holder.getInventory();
        List<ItemStack> progressed = new ArrayList<>();
        for (ItemStack stack : inventory.getContents()) {
            if (Functions.isEmptyItemStack(stack)) continue;
            progressed.add(Functions.addLore("from-container", stack));            
        }
    }
}
