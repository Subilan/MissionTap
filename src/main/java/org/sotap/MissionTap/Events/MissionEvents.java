package org.sotap.MissionTap.Events;

import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.sotap.MissionTap.MissionTap;
import org.sotap.MissionTap.Classes.Mission;
import org.sotap.MissionTap.Utils.Files;
import org.sotap.MissionTap.Utils.Functions;

public final class MissionEvents implements Listener {
    public final Preventers prv;

    public MissionEvents(MissionTap plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        this.prv = new Preventers(plugin);
    }

    public static void updateData(Player p, String missionType, String dataName,
            Integer... addend) {
        UUID u = p.getUniqueId();
        final Integer finalAddend = addend.length == 0 ? 1 : addend[0];
        FileConfiguration playerdata = Files.loadPlayer(u);
        for (String type : Mission.missionTypes) {
            ConfigurationSection section = playerdata.getConfigurationSection(type);
            if (section == null || Files.isEmptyConfiguration(section))
                continue;
            Map<String, Object> data = section.getValues(false);
            String dest = "";
            for (String key : data.keySet()) {
                dest = missionType + "-data" + "." + dataName;
                ConfigurationSection object = (ConfigurationSection) data.get(key);
                object.set(dest, object.getInt(dest) + finalAddend);
            }
        }
        Files.savePlayer(playerdata, u);
        handleAutoSubmittion(p);
    }

    public static void handleAutoSubmittion(Player p) {
        if (Files.config.getBoolean("require-submittion"))
            return;
        UUID u = p.getUniqueId();
        FileConfiguration playerdata = Files.loadPlayer(u);
        for (String type : Mission.missionTypes) {
            ConfigurationSection section = playerdata.getConfigurationSection(type);
            if (section == null || Files.isEmptyConfiguration(section))
                continue;
            for (String key : section.getKeys(false)) {
                Mission m = new Mission(u, type, key);
                if (m.isFinished()) {
                    Functions.finishMission(m, p);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerPickupItem(EntityPickupItemEvent e) {
        if (e.getEntityType() != EntityType.PLAYER)
            return;
        ItemStack item = e.getItem().getItemStack();
        ItemMeta meta = item.getItemMeta();
        if (meta.hasLore()) {
            if (meta.getLore().contains("dispensed")) {
                e.setCancelled(true);
                Player p = (Player) e.getEntity();
                p.getInventory().addItem(Functions.removeLore("dispensed", item));
                e.getItem().remove();
                return;
            }
        }
        if (prv.manuallyDroppedItems.contains(e.getItem().getUniqueId()))
            return;
        Player p = (Player) e.getEntity();
        updateData(p, "collecting", e.getItem().getItemStack().getType().toString());
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityBreeding(EntityBreedEvent e) {
        if (e.getBreeder().getType() != EntityType.PLAYER)
            return;
        Player p = (Player) e.getBreeder();
        updateData(p, "breeding", e.getEntity().getType().toString());
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e) {
        Player p = e.getPlayer();
        BlockData data = e.getBlock().getBlockData();
        if (data instanceof Ageable) {
            Ageable age = (Ageable) data;
            if (age.getAge() != age.getMaximumAge())
                return;
        }
        updateData(p, "blockbreak", e.getBlock().getType().toString());
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent e) {
        if (e.getEntity().getKiller() == null)
            return;
        Player p = e.getEntity().getKiller();
        if (e.getEntityType() == EntityType.PLAYER)
            return;
        updateData(p, "combat", e.getEntity().getType().toString());
    }

    @EventHandler(ignoreCancelled = true)
    public void onItemCraft(CraftItemEvent e) {
        HumanEntity h = e.getView().getPlayer();
        if (!(h instanceof Player))
            return;
        Player p = (Player) h;
        if (e.isShiftClick()) {
            int checked = 0;
            int creation = 1;
            for (ItemStack item : e.getInventory().getMatrix()) {
                if (!Functions.isEmptyItemStack(item)) {
                    if (checked == 0) {
                        creation = item.getAmount();
                    } else {
                        creation = Math.min(creation, item.getAmount());
                    }
                    checked++;
                }
            }
            Integer rawResult = e.getRecipe().getResult().getAmount() * creation;
            Integer spaceLeft = 0;
            ItemStack[] contents = e.getView().getPlayer().getInventory().getContents();
            for (int i = 0; i < 36; i++) {
                ItemStack item = contents[i];
                if (Functions.isEmptyItemStack(item)) {
                    spaceLeft += e.getRecipe().getResult().getMaxStackSize();
                    continue;
                }
                if (item.isSimilar(e.getRecipe().getResult())) {
                    spaceLeft += item.getMaxStackSize() - item.getAmount();
                }
            }
            Integer realResult = spaceLeft >= rawResult ? rawResult : spaceLeft;
            updateData(p, "crafting", e.getInventory().getResult().getType().toString(),
                    realResult);
        } else {
            updateData(p, "crafting", e.getInventory().getResult().getType().toString(),
                    e.getInventory().getResult().getAmount());
        }
    }
}
