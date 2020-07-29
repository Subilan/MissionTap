package org.sotap.MissionTap;

import java.util.Arrays;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class GUI implements Listener {
    private final Inventory inventory;
    private final String type;
    private final MissionTap plug;

    public GUI(String type, MissionTap plug) {
        this.type = type;
        this.plug = plug;
        inventory = Bukkit.createInventory(null, InventoryType.CHEST, "Missions");
        init();
    }

    private void init() {
        // inventory.add(g(...))
    }

    private ItemStack g(final Material mat, final String name, final String... lore) {
        final ItemStack item = new ItemStack(mat);
        final ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        item.setItemMeta(meta);
        return item;
    }

    public void open(final Player p) {
        p.openInventory(inventory);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (e.getInventory() != inventory) return;
        e.setCancelled(true);
        final ItemStack clicked = e.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;
        final Player p = (Player) e.getWhoClicked();
        // ...
    }

    @EventHandler
    public void onInventoryClick(InventoryDragEvent e) {
        if (e.getInventory() == inventory) e.setCancelled(true);
    }
}
