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
import org.bukkit.inventory.meta.ItemMeta;
import org.sotap.MissionTap.MissionTap;

public final class Preventers implements Listener {
    public List<UUID> droppedItem;

    public Preventers(MissionTap plugin) {
        this.droppedItem = new ArrayList<>();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent e) {
        droppedItem.add(e.getItemDrop().getUniqueId());
    }

    @EventHandler
    public void onBlockDispense(BlockDispenseEvent e) {
        ItemStack item = e.getItem();
        ItemMeta meta = item.getItemMeta();
        List<String> loreBefore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
        loreBefore.add("dispensed");
        meta.setLore(loreBefore);
        item.setItemMeta(meta);
        e.setItem(item);
    }
}