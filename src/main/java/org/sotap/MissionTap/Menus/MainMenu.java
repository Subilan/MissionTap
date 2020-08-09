package org.sotap.MissionTap.Menus;

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
import org.sotap.MissionTap.MissionTap;
import org.sotap.MissionTap.Utils.Files;
import org.sotap.MissionTap.Utils.Functions;
import org.sotap.MissionTap.Utils.Menus;

public final class MainMenu implements Listener {
    private final Inventory inventory;

    public MainMenu(MissionTap plugin) {
        this.inventory = Bukkit.createInventory(null, InventoryType.CHEST, "主菜单");
        Bukkit.getPluginManager().registerEvents(this, plugin);
        init();
    }

    private void init() {
        inventory.setItem(10,
                Functions.createItemStack("&e&l每日任务", Material.ENCHANTED_BOOK, null));
        inventory.setItem(12,
                Functions.createItemStack("&a&l每周任务", Material.ENCHANTED_BOOK, null));
        inventory.setItem(14,
                Functions.createItemStack("&c特&6别&e任&a务&3哦&9！&1！", Material.ENCHANTED_BOOK, null));
        inventory.setItem(16, Functions.createItemStack("&b&lInprogress", Material.FEATHER, null));
    }

    public void open(final Player p) {
        if (!Files.config.getBoolean("require-acceptance"))
            return;
        p.openInventory(inventory);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (e.getInventory() != inventory)
            return;
        e.setCancelled(true);
        final ItemStack clicked = e.getCurrentItem();
        if (clicked == null)
            return;
        if (clicked.getType() == Material.AIR)
            return;
        final Player p = (Player) e.getWhoClicked();
        final Integer slot = e.getSlot();
        p.closeInventory();
        switch (slot) {
            case 10: {
                Menus.dailyMissionMenu.open(p);
                break;
            }

            case 12: {
                Menus.weeklyMissionMenu.open(p);
                break;
            }

            case 14: {
                Menus.specialMissionMenu.open(p);
                return;
            }

            case 16: {
                Menus.inprogressMenu.open(p);
                break;
            }

            default: {
                return;
            }
        }
    }

    @EventHandler
    public void onInventorDrag(InventoryDragEvent e) {
        if (e.getInventory() == inventory)
            e.setCancelled(true);
    }
}
