package org.sotap.MissionTap.Events;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.sotap.MissionTap.MissionTap;

import java.util.HashMap;
import java.util.Map;

public final class Identifiers implements Listener {
    private Map<String, Integer> identified;

    public Identifiers(MissionTap plugin) {
        this.identified = new HashMap<>();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent e) {
        String prefix = keyPrefix(e.getPlayer());
        for (String key : this.identified.keySet()) {
            if (key.startsWith(prefix)) {
                this.identified.remove(key);
            }
        }
    }

    public void queueAll(Iterable<ItemStack> stacks, Player p) {
        for (ItemStack stack : stacks) {
            queue(stack, p);
        }
    }

    public void queue(ItemStack stack, Player p) {
        String key = itemKey(stack, p);
        int existing = this.identified.getOrDefault(key, 0);
        int amount = stack.getAmount();
        this.identified.put(key, existing + amount);
    }

    public int dequeue(ItemStack stack, Player p) {
        String key = itemKey(stack, p);
        int existing = this.identified.getOrDefault(key, 0);
        int amount = stack.getAmount();
        if (existing > amount) {
            this.identified.put(key, existing - amount);
            return amount;
        } else {
            this.identified.remove(key);
            return existing;
        }
    }

    private String keyPrefix(Player p) {
        return p.getUniqueId().toString() + ".";
    }

    private String itemKey(ItemStack stack, Player p) {
        return keyPrefix(p) + stack.getType().toString();
    }

}
