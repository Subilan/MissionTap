package org.sotap.MissionTap.Utils;

import org.bukkit.inventory.ItemStack;
import org.sotap.MissionTap.MissionTap;
import org.sotap.MissionTap.Classes.ItemStackIdentifier;

public final class Identifiers {
    public static ItemStackIdentifier identifier;

    public static void init(MissionTap plugin) {
        identifier = new ItemStackIdentifier(plugin);
    }

    public static void identify(ItemStack stack) {
        identifier.addIdentifier(stack);
    }

    public static boolean isValidIdentified(ItemStack stack) {
        return identifier.isIdentified(stack, true);
    }
    
    public static boolean isIdentified(ItemStack stack) {
        return identifier.isIdentified(stack, false);
    }

    public static void setInvalid(ItemStack stack) {
        identifier.setInvalid(stack);
    }
}