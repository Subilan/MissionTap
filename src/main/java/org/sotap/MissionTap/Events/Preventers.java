package org.sotap.MissionTap.Events;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Bisected.Half;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.sotap.MissionTap.MissionTap;

public final class Preventers implements Listener {
    private Map<String, Material> manuallyPlacedBlocks;

    public Preventers(MissionTap plugin) {
        this.manuallyPlacedBlocks = new HashMap<>();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent e) {
        Block b = e.getBlock();
        manuallyPlacedBlocks.put(locationKey(b), b.getType());
    }

    public boolean isManuallyPlaced(Block b) {
        b = findBottomOfBisectedBlock(b);
        if (isSpecialBlock(b)) {
            return !isEligibleSpecialBlock(b);
        }
        Material expectedType = manuallyPlacedBlocks.get(locationKey(b));
        return b.getType() == expectedType;
    }

    public void clearManuallyPlaced(Block b) {
        b = findBottomOfBisectedBlock(b);
        manuallyPlacedBlocks.remove(locationKey(b));
    }

    private Block findBottomOfBisectedBlock(Block b) {
        BlockData data = b.getBlockData();
        if (!(data instanceof Bisected)) {
            return b;
        }
        Bisected bi = (Bisected) data;
        if (bi.getHalf() == Half.TOP) {
            b = b.getRelative(BlockFace.DOWN);
        }
        return b;
    }

    private boolean isSpecialBlock(Block b) {
        BlockData data = b.getBlockData();
        return data instanceof Ageable;
    }

    private boolean isEligibleSpecialBlock(Block b) {
        BlockData data = b.getBlockData();
        if (data instanceof Ageable) {
            Ageable age = (Ageable) data;
            return age.getAge() == age.getMaximumAge();
        }
        return false;
    }

    private String locationKey(Block b) {
        // To ignore pitch and yaw of location.
        Location l = b.getLocation();
        return l.getWorld().getName() + ":" + l.getBlockX() + "," + l.getBlockY() + "," + l.getBlockZ();
    }
}
