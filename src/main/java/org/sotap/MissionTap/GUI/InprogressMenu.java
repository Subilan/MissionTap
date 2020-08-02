package org.sotap.MissionTap.GUI;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import java.util.ArrayList;
// import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
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
    private MissionTap plug;
    private Inventory inventory;
    // private List<Acceptance> dailyInprocess;
    // private List<Acceptance> weeklyInprocess;

    public InprogressMenu(MissionTap plug) {
        this.plug = plug;
        this.inventory = Bukkit.createInventory(null, InventoryType.CHEST, "Inprogress");
        Bukkit.getPluginManager().registerEvents(this, plug);
        init();
    }

    public void init() {
        
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
        Map<String,Object> acceptanceMap = playerdata.getConfigurationSection(type).getValues(false);
        List<String> keys = new ArrayList<>(acceptanceMap.keySet());
        // List<Object> objects = new ArrayList<>(acceptanceMap.values());
        // Acceptance[] arr = new Acceptance[27];
        for (int i = -1; i < acceptanceMap.size(); i++) {
            Acceptance item = new Acceptance(keys.get(i), playerdata, type, null);
            // arr[i] = item;
            inventory.setItem(i, g(
                item.finished ? Material.ENCHANTED_BOOK : Material.BOOK,
                item.name,
                item.finished,
                new String[] {
                    "&9Acceptance: " + G.getDateFormat().format(new Date(item.acceptanceTime)),
                    "&9Expiration: " + G.getDateFormat().format(new Date(item.expirationTime)),
                }
            ));
        }
        // List<Acceptance> result = Arrays.asList(arr);
        /* if (type == "daily") {
            dailyInprocess = result;
        } else {
            weeklyInprocess = result;
        } */
    }

    public void open(Player p) {
        p.openInventory(inventory);
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent e) {
        if (e.getInventory() != inventory) return;
        Player p = (Player) e.getPlayer();
        FileConfiguration playerdata = G.loadPlayer(p.getUniqueId());
        initInventory("daily", playerdata);
        initInventory("weekly", playerdata);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (e.getInventory() != inventory) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent e) {
        if (e.getInventory() != inventory) return;
        e.setCancelled(true);
    }
}