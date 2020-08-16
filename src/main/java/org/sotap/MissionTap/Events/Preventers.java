package org.sotap.MissionTap.Events;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.sotap.MissionTap.MissionTap;
import org.sotap.MissionTap.Utils.Functions;

public final class Preventers implements Listener {
    public List<UUID> droppedItem;

    public Preventers(MissionTap plugin) {
        this.droppedItem = new ArrayList<>();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDropItem(PlayerDropItemEvent e) {
        droppedItem.add(e.getItemDrop().getUniqueId());
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockDispense(BlockDispenseEvent e) {
        ItemStack item = e.getItem();
        e.setItem(Functions.addLore("dispensed", item));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e) {
        Block b = e.getBlock();
        BlockState state = b.getState();
        if (!(state instanceof InventoryHolder)) return;
        World world = b.getWorld();
        Collection<ItemStack> items = b.getDrops();
        b.setType(Material.AIR);
        for (ItemStack item : items) {
            world.dropItemNaturally(b.getLocation(), Functions.addLore("from-container", item));
        }
    }
}