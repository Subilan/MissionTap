package org.sotap.MissionTap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import net.md_5.bungee.api.ChatColor;

public final class GUI implements Listener {
    private final Inventory inventory;
    private final String type;
    private final MissionTap plug;
    private List<Mission> inventoryContent;

    public GUI(String type, MissionTap plug) {
        this.type = type;
        this.plug = plug;
        inventory = Bukkit.createInventory(null, InventoryType.CHEST, "Missions");
        Bukkit.getPluginManager().registerEvents(this, plug);
        init();
    }

    private void init() {
        ConfigurationSection missionSection = plug.load("latest-missions.yml").getConfigurationSection(type);
        if (missionSection == null) {
            plug.log(G.translateColor(G.WARN + "No &e" + type + "&r missions were found in latest-missions.yml, stopping the initialization of GUI."));
            return;
        }
        Map<String,Object> missions = missionSection.getValues(false);
        Mission[] inventorySlots = new Mission[27];
        // next value: daily -> 10, 12; weekly -> 10, 12, 14, 16; 
        int index = type == "daily" ? 10 : 8;
        for (Object item : missions.values()) {
            index += 2;
            Mission m = new Mission(item);
            plug.log(m.name);
            inventory.setItem(index, g(Material.BOOK, m.name, m.description.toArray(new String[0])));
            m.setPosition(index);
            inventorySlots[index] = m;
        }
        inventoryContent = Arrays.asList(inventorySlots);
    }

    public void reloadGUI() {
        inventory.clear();
        init();
    }

    private ItemStack g(final Material mat, final String name, final String... lore) {
        final ItemStack item = new ItemStack(mat);
        final ItemMeta meta = item.getItemMeta();
        final List<String> finalLore = new ArrayList<>();
        for (String text : lore) {
            finalLore.add(G.translateColor(text));
        }
        meta.setDisplayName(ChatColor.AQUA + name);
        meta.setLore(finalLore);
        item.setItemMeta(meta);
        return item;
    }

    public void open(final Player p) {
        p.openInventory(inventory);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        e.setCancelled(true);
        if (e.getInventory() != inventory) return;
        final ItemStack clicked = e.getCurrentItem();
        if (clicked == null) return;
        if (clicked.getType() != Material.BOOK) return;
        final Player p = (Player) e.getWhoClicked();
        final Integer slot = e.getSlot();
        final Mission mission = inventoryContent.get(slot);
        p.sendMessage("The mission you clicked is: " + slot);
        // 玩家接受任务后
    }

    @EventHandler
    public void onInventoryClick(InventoryDragEvent e) {
        if (e.getInventory() == inventory) e.setCancelled(true);
    }
}
