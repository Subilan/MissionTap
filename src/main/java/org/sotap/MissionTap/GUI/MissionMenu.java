package org.sotap.MissionTap.GUI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
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
import org.sotap.MissionTap.Acceptance;
import org.sotap.MissionTap.Mission;
import org.sotap.MissionTap.MissionTap;
import net.md_5.bungee.api.ChatColor;

public final class MissionMenu implements Listener {
    private final Inventory inventory;
    private final String type;
    private final MissionTap plug;
    private List<Mission> inventoryContent;

    public MissionMenu(String type, MissionTap plug) {
        this.type = type;
        this.plug = plug;
        inventory = Bukkit.createInventory(null, InventoryType.CHEST, "Missions");
        Bukkit.getPluginManager().registerEvents(this, plug);
        init();
    }

    private void init() {
        FileConfiguration missionFile = G.load("latest-missions.yml");
        if (missionFile.getConfigurationSection(type) == null) {
            plug.log(G.translateColor(G.WARN + "No &e" + type
                    + "&r missions were found in latest-missions.yml, stopping the initialization of GUI."));
            return;
        }
        Long expirationTime = missionFile.getLong(type + "-next-regen");
        Map<String, Object> missionObjects = missionFile.getConfigurationSection(type).getValues(false);
        List<String> missionKeys = new ArrayList<>(missionObjects.keySet());
        List<Object> missions = new ArrayList<>(missionObjects.values());
        Mission[] inventorySlots = new Mission[27];
        int index = 0;
        for (; index < missions.size(); index++) {
            Mission m = new Mission(missionKeys.get(index), missions.get(index), type);
            plug.log(m.name);
            inventory.setItem(index, g(Material.BOOK, m.name, expirationTime, m.description.toArray(new String[0])));
            m.setPosition(index);
            inventorySlots[index] = m;
        }
        inventoryContent = Arrays.asList(inventorySlots);
    }

    public void reloadGUI() {
        inventory.clear();
        init();
    }

    private ItemStack g(final Material mat, final String name, final Long expiration, final String... lore) {
        final ItemStack item = new ItemStack(mat);
        final ItemMeta meta = item.getItemMeta();
        final List<String> finalLore = new ArrayList<>();
        for (String text : lore) {
            finalLore.add(ChatColor.RESET + G.translateColor("&f" + text));
        }
        finalLore.add("");
        finalLore.add(G.translateColor("&8" + G.getDateString(expiration)));
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
        if (e.getInventory() != inventory)
            return;
        e.setCancelled(true);
        final ItemStack clicked = e.getCurrentItem();
        if (clicked == null)
            return;
        if (clicked.getType() != Material.BOOK)
            return;
        final Player p = (Player) e.getWhoClicked();
        final Integer slot = e.getSlot();
        final Mission mission = inventoryContent.get(slot);
        final Acceptance acc = new Acceptance(mission.key, null, mission.type, mission.name);
        ConfigurationSection playerdata = G.loadPlayer(p.getUniqueId());
        if (playerdata.getInt(acc.type) == -1) {
            playerdata.set(acc.type, null);
        }
        // if the already-existing accpetance is expired
        if (playerdata.getLong(acc.type + "." + acc.key + ".expiration-time") <= new Date().getTime()) {
            playerdata.set(acc.type + "." + acc.key, null);
        } else {
            p.closeInventory();
            p.sendMessage(G.translateColor(G.FAILED + "You cannot accept a mission that is already accepted!"));
            return;
        }
        playerdata.createSection(acc.type + "." + acc.key, acc.getAcceptance());
        G.savePlayer((FileConfiguration) playerdata, p.getUniqueId());
        p.sendMessage(G.translateColor(G.SUCCESS + "Successfully accepted the mission &a" + acc.name + "&r!"));
        p.closeInventory();
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent e) {
        if (e.getInventory() == inventory)
            e.setCancelled(true);
    }
}
