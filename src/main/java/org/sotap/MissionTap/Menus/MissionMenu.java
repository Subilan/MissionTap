package org.sotap.MissionTap.Menus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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
import org.sotap.MissionTap.Main;
import org.sotap.MissionTap.Classes.Mission;
import org.sotap.MissionTap.Utils.Files;
import org.sotap.MissionTap.Utils.Logger;

public final class MissionMenu implements Listener {
    private final Inventory inventory;
    private final String type;
    private final Main plugin;
    private final FileConfiguration objects;
    private List<Mission> missions;

    public MissionMenu(String type, Main plugin) {
        this.type = type;
        this.plugin = plugin;
        this.objects = type != null ? (type == "daily" ? Files.DailyMissions : Files.WeeklyMissions)
                : null;
        this.inventory = Bukkit.createInventory(null, InventoryType.CHEST, "Missions");
        this.missions = new ArrayList<>();
        Bukkit.getPluginManager().registerEvents(this, plugin);
        init();
    }

    private void init() {
        if (objects == null) {
            plugin.log(Logger.WARN + "No &e" + type + "&r missions were found.");
            return;
        }
        Map<String, Object> missionObjects = objects.getValues(false);
        int index = 0;
        for (String key : missionObjects.keySet()) {
            Mission m = new Mission(type, key);
            inventory.setItem(index, m.getItemStack(null));
            missions.add(m);
            index++;
        }
    }

    public void open(final Player p) {
        if (!Files.config.getBoolean("require-acceptance")) {
            p.sendMessage(Logger.translateColor(
                    Logger.INFO + "You don't need to accept the missions manually now."));
            return;
        }
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
        final UUID u = p.getUniqueId();
        final Integer slot = e.getSlot();
        final Mission clickedMission = missions.get(slot);
        p.closeInventory();
        if (clickedMission.isAccepted(u)) {
            p.sendMessage(Logger.translateColor(
                    Logger.FAILED + "You cannot accept a mission that is already accepted!"));
            return;
        }
        if (clickedMission.isExpired(u)) {
            p.sendMessage(Logger.translateColor(Logger.WARN + "The mission is &cexpired&r now."));
            clickedMission.destory(u);
            return;
        }
        clickedMission.accept(u);
        p.sendMessage(Logger.translateColor(Logger.SUCCESS + "Successfully accepted the mission &a"
                + clickedMission.getName() + "&r!"));
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent e) {
        if (e.getInventory() == inventory)
            e.setCancelled(true);
    }
}
