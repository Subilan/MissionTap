package org.sotap.MissionTap.GUI;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
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
import org.sotap.MissionTap.MissionTap;
import org.sotap.MissionTap.Utils.G;
import net.md_5.bungee.api.ChatColor;

public final class InprogressMenu implements Listener {
    private Inventory inventory;
    private List<Acceptance> accList;

    public InprogressMenu(MissionTap plug) {
        this.inventory = Bukkit.createInventory(null, InventoryType.CHEST, "Inprogress");
        this.accList = new ArrayList<>();
        Bukkit.getPluginManager().registerEvents(this, plug);
    }

    private ItemStack g(final Material mat, final String name, final boolean finished, final String... lore) {
        final ItemStack item = new ItemStack(mat);
        final ItemMeta meta = item.getItemMeta();
        final List<String> finalLore = new ArrayList<>();
        finalLore.add("");
        finalLore.add(G.translateColor( finished ? "&a&lFinished" : "&c&lUnfinished"));
        finalLore.add("");
        for (String text : lore) {
            finalLore.add(ChatColor.RESET + G.translateColor("&f" + text));
        }
        meta.setDisplayName(ChatColor.AQUA + name);
        meta.setLore(finalLore);
        item.setItemMeta(meta);
        return item;
    }

    public void initInventory(String type, FileConfiguration playerdata) {
        if (!List.of("daily", "weekly").contains(type)) return;
        if (playerdata.getInt(type) == -1) return;
        Map<String,Object> acceptanceMap = playerdata.getConfigurationSection(type).getValues(false);
        List<String> keys = new ArrayList<>(acceptanceMap.keySet());
        ItemStack[] inventoryContent = new ItemStack[27];
        for (int i = 0; i < acceptanceMap.size(); i++) {
            Acceptance acc = new Acceptance(keys.get(i), playerdata, type, null);
            accList.add(acc);
            ItemStack item = g(
                acc.finished ? Material.ENCHANTED_BOOK : Material.BOOK,
                acc.name,
                acc.finished,
                new String[] {
                    "&fAcceptance: &a" + G.getDateFormat().format(new Date(acc.acceptanceTime)),
                    "&fExpiration: &c" + G.getDateFormat().format(new Date(acc.expirationTime)),
                }
            );
            inventory.addItem(item);
            inventoryContent[i] = item;
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
        if (e.getInventory() != inventory) return;
        Player p = (Player) e.getPlayer();
        FileConfiguration playerdata = G.loadPlayer(p.getUniqueId());
        if (playerdata.getInt("daily") == -1 && playerdata.getInt("weekly") == -1) {
            p.sendMessage(G.translateColor(G.WARN + "You haven't accepted any mission now."));
            e.setCancelled(true);
            return;
        }
        initInventory("daily", playerdata);
        initInventory("weekly", playerdata);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (e.getInventory() != inventory) return;
        e.setCancelled(true);
        ItemStack item = e.getCurrentItem();
        if (item == null) return;
        if (item.getType() != Material.BOOK && item.getType() != Material.ENCHANTED_BOOK) return;
        Player p = (Player) e.getWhoClicked();
        Integer slot = e.getSlot();
        Acceptance currentAcc = accList.get(slot);
        // DELETE
        if (e.getClick() == ClickType.SHIFT_LEFT) {
            currentAcc.delete(p.getUniqueId());
            removeSlot(slot);
            p.closeInventory();
            p.sendMessage(G.translateColor(G.SUCCESS + "Successfully removed the mission from your current working-on list."));
            return;
        }
        // SUBMIT
        if (e.getClick() == ClickType.LEFT) {
            if (currentAcc.finished) {
                // reward code goes here...
                currentAcc.delete(p.getUniqueId());
                removeSlot(slot);
                p.closeInventory();
                p.sendMessage(G.translateColor(G.SUCCESS + "&eCongratulations!&r You've finished the mission &a" + currentAcc.name + "&r!"));
            } else {
                p.closeInventory();
                p.sendMessage(G.translateColor(G.WARN + "You haven't finished the mission &c" + currentAcc.name + " &ryet!"));
            }
            return;
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent e) {
        if (e.getInventory() != inventory) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        if (e.getInventory() != inventory) return;
        inventory.clear();
        accList = new ArrayList<>();
    }
}