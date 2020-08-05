package org.sotap.MissionTap.GUI;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.sotap.MissionTap.Acceptance;
import org.sotap.MissionTap.Mission;
import org.sotap.MissionTap.MissionTap;
import org.sotap.MissionTap.Utils.G;
import net.md_5.bungee.api.ChatColor;

public final class InprogressMenu implements Listener {
    private Inventory inventory;
    private List<Acceptance> accList;
    private List<Mission> misList;
    private MissionTap plug;

    public InprogressMenu(MissionTap plug) {
        this.inventory = Bukkit.createInventory(null, InventoryType.CHEST, "Inprogress");
        this.accList = new ArrayList<>();
        this.misList = new ArrayList<>();
        this.plug = plug;
        Bukkit.getPluginManager().registerEvents(this, plug);
    }

    private ItemStack g(final String name, final long expirationTime, final boolean finished) {
        final ItemStack item = new ItemStack(finished ? Material.ENCHANTED_BOOK : Material.BOOK);
        final ItemMeta meta = item.getItemMeta();
        final List<String> finalLore = new ArrayList<>();
        finalLore.add(G.translateColor(finished ? "&a&lFinished" : "&c&lUnfinished"));
        finalLore.add("");
        finalLore.add(G.translateColor("&8" + G.getDateFormat().format(new Date(expirationTime))));
        meta.setDisplayName(ChatColor.AQUA + name);
        meta.setLore(finalLore);
        item.setItemMeta(meta);
        return item;
    }

    public void initInventory(String type, FileConfiguration playerdata) {
        if (!List.of("daily", "weekly").contains(type))
            return;
        if (playerdata.getInt(type) == -1)
            return;
        Map<String, Object> acceptanceMap = playerdata.getConfigurationSection(type).getValues(false);
        for (String key : acceptanceMap.keySet()) {
            Acceptance acc = new Acceptance(key, playerdata, type, null);
            if (acc.expirationTime <= new Date().getTime())
                continue;
            accList.add(acc);
            inventory.addItem(g(acc.name, acc.expirationTime, acc.isFinished()));
        }
    }

    public void initGlobalInventory(FileConfiguration playerdata) {
        for (String type : new String[] { "daily", "weekly" }) {
            if (plug.latestMissions.getLong(type + "-next-regen") <= new Date().getTime())
                continue;
            Map<String, Object> missionMap = plug.latestMissions.getConfigurationSection(type).getValues(false);
            for (String key : missionMap.keySet()) {
                Mission m = new Mission(key, missionMap.get(key), type);
                misList.add(m);
                inventory.addItem(g(m.name, plug.latestMissions.getLong(type + "-next-regen"), false));
            }
        }
    }

    public void open(Player p) {
        p.openInventory(inventory);
    }

    private void removeSlot(Integer slot) {
        inventory.setItem(slot, new ItemStack(Material.AIR));
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent e) {
        if (e.getInventory() != inventory)
            return;
        Player p = (Player) e.getPlayer();
        FileConfiguration playerdata = G.loadPlayer(p.getUniqueId());
        if (playerdata.getInt("daily") == -1 && playerdata.getInt("weekly") == -1) {
            p.sendMessage(G.translateColor(G.WARN + "You haven't accepted any mission now."));
            e.setCancelled(true);
            return;
        }
        if (plug.getConfig().getBoolean("require_acceptance")) {
            initInventory("daily", playerdata);
            initInventory("weekly", playerdata);
        } else {
            initGlobalInventory(playerdata);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (e.getInventory() != inventory)
            return;
        e.setCancelled(true);
        ItemStack item = e.getCurrentItem();
        if (item == null)
            return;
        if (item.getType() != Material.BOOK && item.getType() != Material.ENCHANTED_BOOK)
            return;
        Player p = (Player) e.getWhoClicked();
        UUID u = p.getUniqueId();
        Integer slot = e.getSlot();
        Acceptance currentAcc = accList.get(slot);
        currentAcc.updateData(G.loadPlayer(u));
        if (currentAcc.expirationTime <= new Date().getTime()) {
            p.closeInventory();
            p.sendMessage(G.translateColor(G.WARN + "The mission is already &cexpired&r now!"));
            return;
        }
        // DELETE
        if (e.getClick() == ClickType.SHIFT_LEFT) {
            if (!G.config.getBoolean("allow_cancelling")) {
                p.closeInventory();
                p.sendMessage(G.translateColor(G.WARN + "You &ccan't&r cancel the mission now."));
                return;
            }
            currentAcc.delete(u);
            removeSlot(slot);
            p.closeInventory();
            p.sendMessage(G
                    .translateColor(G.SUCCESS + "Successfully removed the mission from your current working-on list."));
            return;
        }
        // SUBMIT
        if (e.getClick() == ClickType.LEFT) {
            if (currentAcc.isFinished()) {
                List<String> commands = G.load("latest-missions.yml")
                        .getStringList(currentAcc.type + "." + currentAcc.key + ".rewards");
                G.dispatchCommands(p, commands);
                currentAcc.delete(u);
                removeSlot(slot);
                p.closeInventory();
                p.sendMessage(G.translateColor(
                        G.SUCCESS + "&eCongratulations!&r You've finished the mission &a" + currentAcc.name + "&r!"));
            } else {
                p.closeInventory();
                p.sendMessage(
                        G.translateColor(G.WARN + "You haven't finished the mission &c" + currentAcc.name + " &ryet!"));
            }
            return;
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent e) {
        if (e.getInventory() != inventory)
            return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        if (e.getInventory() != inventory)
            return;
        inventory.clear();
        accList = new ArrayList<>();
    }
}