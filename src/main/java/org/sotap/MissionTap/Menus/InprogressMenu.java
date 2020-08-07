package org.sotap.MissionTap.Menus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.sotap.MissionTap.MissionTap;
import org.sotap.MissionTap.Classes.Mission;
import org.sotap.MissionTap.Utils.Files;
import org.sotap.MissionTap.Utils.Logger;

public final class InprogressMenu implements Listener {
    private final Inventory inventory;
    private List<Mission> missions;
    public MissionTap plugin;

    public InprogressMenu(MissionTap plugin) {
        this.inventory = Bukkit.createInventory(null, InventoryType.CHEST, "Inprogress");
        this.missions = new ArrayList<>();
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void init(UUID u) {
        int index = 0;
        for (String type : new String[] {"daily", "weekly"}) {
            ConfigurationSection objects = Files.loadPlayer(u).getConfigurationSection(type);
            if (Files.isEmptyConfiguration(objects))
                continue;
            Map<String, Object> objectMap = objects.getValues(false);
            for (String key : objectMap.keySet()) {
                Mission m = new Mission(type, key);
                if (m.isExpired(u)) {
                    continue;
                }
                missions.add(m);
                inventory.setItem(index, m.getItemStack(u));
                index++;
            }
        }
    }

    public void open(final Player p) {
        init(p.getUniqueId());
        p.openInventory(inventory);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (e.getInventory() != inventory)
            return;
        e.setCancelled(true);
        ItemStack clicked = e.getCurrentItem();
        if (clicked == null)
            return;
        if (clicked.getType() != Material.BOOK && clicked.getType() != Material.ENCHANTED_BOOK)
            return;
        Player p = (Player) e.getWhoClicked();
        UUID u = p.getUniqueId();
        Integer slot = e.getSlot();
        Mission clickedMission = missions.get(slot);
        p.closeInventory();
        if (clickedMission.isExpired(u)) {
            p.sendMessage(
                    Logger.translateColor(Logger.WARN + "The mission is already &cexpired&r."));
            return;
        }
        if (e.getClick() == ClickType.SHIFT_LEFT) {
            if (!Files.config.getBoolean("allow-cancelling") || !Files.config.getBoolean("require-acceptance")) {
                p.sendMessage(
                        Logger.translateColor(Logger.FAILED + "You can't cancel the mission now."));
                return;
            }
            clickedMission.destory(u);
            p.sendMessage(Logger.translateColor(Logger.SUCCESS
                    + "Successfully removed the mission from your current working-on list."));
            return;
        }
        if (e.getClick() == ClickType.LEFT) {
            if (clickedMission.isFinished(u)) {
                clickedMission.reward(p);
                if (!Files.config.getBoolean("allow-multiple-acceptance"))
                    clickedMission.setSubmitted(u);
                clickedMission.destory(u);
                p.sendMessage(Logger.translateColor(
                        Logger.SUCCESS + "&bCongratulations!&r You've finished the mission \"&a"
                                + clickedMission.getName() + "&r\"!"));
            } else {
                p.sendMessage(Logger
                        .translateColor(Logger.FAILED + "You haven't finished the mission yet!"));
            }
            return;
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent e) {
        if (e.getInventory() == inventory)
            e.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        if (e.getInventory() != inventory)
            return;
        inventory.clear();
        missions = new ArrayList<>();
    }
}
