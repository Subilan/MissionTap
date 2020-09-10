package org.sotap.MissionTap.Events;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;
import org.sotap.MissionTap.MissionTap;
import org.sotap.MissionTap.Classes.Mission;
import org.sotap.MissionTap.Utils.Files;
import org.sotap.MissionTap.Utils.Functions;
import org.sotap.MissionTap.Utils.Identifiers;

@SuppressWarnings("unused")
public final class MissionEvents implements Listener {
    public final Preventers prv;

    public MissionEvents(MissionTap plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        this.prv = new Preventers(plugin);
    }

    public static void updateData(Player p, String missionType, String dataName,
                                  Integer... addend) {
        UUID u = p.getUniqueId();
        final int finalAddend = addend.length == 0 ? 1 : addend[0];
        FileConfiguration playerdata = Files.loadPlayer(u);
        for (String type : Mission.missionTypes) {
            ConfigurationSection section = playerdata.getConfigurationSection(type);
            if (section == null || Files.isEmptyConfiguration(section))
                continue;
            Map<String, Object> data = section.getValues(false);
            String dest;
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

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityBreeding(EntityBreedEvent e) {
        if (e.getBreeder() == null)
            return;
        if (e.getBreeder().getType() != EntityType.PLAYER)
            return;
        Player p = (Player) e.getBreeder();
        updateData(p, "breeding", e.getEntity().getType().toString());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e) {
        Block b = e.getBlock();
        if (prv.isManuallyPlaced(b)) {
            prv.clearManuallyPlaced(b);
            return;
        }
        /* if (!b.getDrops(p.getInventory().getItemInMainHand()).isEmpty()) {
            identifyAll(b.getDrops(), p.getUniqueId());
        } */
        updateData(e.getPlayer(), "blockbreak", b.getType().toString());
    }

    /* @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerPickupItem(EntityPickupItemEvent e) {
        if (e.getEntityType() != EntityType.PLAYER)
            return;
        ItemStack pickedUp = e.getItem().getItemStack();
        Player p = (Player) e.getEntity();
        if (Identifiers.isIdentified(pickedUp, p.getUniqueId())) {
            Identifiers.remove(pickedUp, p.getUniqueId());
            updateData(p, "collecting", pickedUp.getType().toString(), pickedUp.getAmount());
        }
    } */

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent e) {
        if (e.getEntity().getKiller() == null)
            return;
        Player p = e.getEntity().getKiller();
        if (e.getEntityType() == EntityType.PLAYER)
            return;
        updateData(p, "combat", e.getEntity().getType().toString());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
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
            int rawResult = e.getRecipe().getResult().getAmount() * creation;
            int spaceLeft = 0;
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
            Integer realResult = Math.min(spaceLeft, rawResult);
            updateData(p, "crafting", Objects.requireNonNull(e.getInventory().getResult()).getType().toString(),
                    realResult);
        } else {
            updateData(p, "crafting", Objects.requireNonNull(e.getInventory().getResult()).getType().toString(),
                    e.getInventory().getResult().getAmount());
        }
    }

    public void identifyAll(Iterable<ItemStack> stacks, UUID u) {
        for (ItemStack i : stacks) {
            Identifiers.identify(i, u);
        }
    }
}
