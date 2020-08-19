package org.sotap.MissionTap.Utils;

import org.bukkit.inventory.ItemStack;
import org.sotap.MissionTap.MissionTap;
import org.sotap.MissionTap.Classes.ItemStackIdentifier;

public final class Identifiers {
    public static ItemStackIdentifier identifier;

    public static void init(MissionTap plugin) {
        identifier = new ItemStackIdentifier(plugin);
    }

    /**
     * 为指定 stack 加上 UUID 标识
     * @param stack 指定 stack
     */
    public static void identify(ItemStack stack) {
        identifier.addIdentifier(stack);
    }
    
    /**
     * 判断 stack 是否包含有效标识
     * 
     * @param stack
     * @return 是否包含有效标识
     */
    public static boolean isValidIdentified(ItemStack stack) {
        return identifier.isIdentified(stack, true);
    }
    
    /**
     * 判断 stack 是否包含无效标识
     * 
     * @param stack
     * @return 是否包含无效标识
     */
    public static boolean isInvalidIdentified(ItemStack stack) {
        return identifier.isIdentified(stack, false);
    }

    /**
     * 标记 stack 的标识为无效
     * @param stack
     */
    public static void setInvalid(ItemStack stack) {
        identifier.setInvalid(stack);
    }
}