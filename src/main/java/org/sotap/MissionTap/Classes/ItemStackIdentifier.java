package org.sotap.MissionTap.Classes;

import java.util.*;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.inventory.ItemStack;
import org.sotap.MissionTap.Utils.Files;

public final class ItemStackIdentifier {
    public static MemoryConfiguration mc;
    public UUID u;
    public List<ItemStack> stacks;
    public ConfigurationSection data;

    public ItemStackIdentifier() {
        mc = new MemoryConfiguration();
    }

    public void setUUID(UUID u) {
        this.u = u;
        data = mc.getConfigurationSection(u.toString());
    }

    public void addIdentifier(ItemStack stack) {
        data.set(UUID.randomUUID().toString(), stack);
    }

    public boolean isIdentified(ItemStack stack) {
        if (Files.isEmptyConfiguration(data)) return false;
        for (String key : data.getKeys(false)) {
            if (Objects.equals(data.getItemStack(key), stack)) return true;
        }
        return false;
    }

    public void remove(ItemStack stack) {
        if (Files.isEmptyConfiguration(data)) return;
        for (String key : data.getKeys(false)) {
            if (Objects.equals(data.getItemStack(key), stack)) {
                data.set(key, null);
                break;
            }
        }
    }

    public static void clearDataFor(UUID u) {
        mc.set(u.toString(), null);
    }
}
