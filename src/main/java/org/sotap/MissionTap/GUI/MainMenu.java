package org.sotap.MissionTap.GUI;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.sotap.MissionTap.Utils.G;
import org.sotap.MissionTap.MissionTap;
import net.md_5.bungee.api.ChatColor;

public final class MainMenu implements Listener {
    private MissionTap plug;
    private Inventory inventory;

    public MainMenu(MissionTap plug) {
        this.plug = plug;
        Bukkit.getPluginManager().registerEvents(this, plug);
        init();
    }

    private void init() {
        FileConfiguration config = plug.getConfig();
        inventory = Bukkit.createInventory(null, InventoryType.CHEST, "MissionMenu");
        Integer dailyTime = config.getInt("daily_refresh_time");
        Integer weeklyTime = config.getInt("weekly_refresh_time");
        String weeklyDay = new String[]{
            "Sunday",
            "Monday",
            "Tuesday",
            "Wednesday",
            "Thursday",
            "Friday",
            "Saturday"
        }[weeklyTime];
        inventory.setItem(11, g(
            Material.ENCHANTED_BOOK,
            "&e&lDaily",
            new String[] {
                "Check the &edaily&f missions here.",
                "The missions update every day at &a" + dailyTime + ":00&f pm."
            }
        ));
        inventory.setItem(13, g(
            Material.ENCHANTED_BOOK,
            "&b&lWeekly",
            new String[] {
                "Check the &bweekly&f missions here.",
                "The missions update every &a" + weeklyDay + "&f.",
            }
        ));
        inventory.setItem(15, g(
            Material.GOLDEN_APPLE,
            "&cS&6p&ee&ac&3i&9a&1l",
            new String[] { 
                "&b&lSpecial &fmissions here!",
                "They will only appear on special days."
            }
        ));
    }

    private ItemStack g(Material mat, String name, String... lore) {
        final ItemStack item = new ItemStack(mat);
        final ItemMeta meta = item.getItemMeta();
        final List<String> finalLore = new ArrayList<>();
        for (String text : lore) {
            finalLore.add(ChatColor.RESET + G.translateColor("&f" + text));
        }
        meta.setDisplayName(G.translateColor(name));
        meta.setLore(finalLore);
        item.setItemMeta(meta);
        return item;
    }

    public void open(final Player p) {
        // if acceptance is not required
        if (!plug.getConfig().getBoolean("require_acceptance"))
            return;
        p.openInventory(inventory);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (e.getInventory() != inventory) return;
        e.setCancelled(true);
        final ItemStack clicked = e.getCurrentItem();
        if (clicked == null) return;
        if (clicked.getType() == Material.AIR) return;
        final Player p = (Player) e.getWhoClicked();
        final Integer slot = e.getSlot();
        p.closeInventory();
        switch (slot) {
            case 11: {
                plug.dailyMissionMenu.open(p);
                break;
            }

            case 13: {
                plug.weeklyMissionMenu.open(p);
                break;
            }

            case 15: {
                // TBC
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryDragEvent e) {
        if (e.getInventory() == inventory) e.setCancelled(true);
    }
}