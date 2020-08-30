package org.sotap.MissionTap.Utils;

import org.bukkit.inventory.ItemStack;
import org.sotap.MissionTap.Classes.ItemStackIdentifier;

import java.util.UUID;

public final class Identifiers {
    public static ItemStackIdentifier identifier;

    public static void init() {
        identifier = new ItemStackIdentifier();
    }

    /**
     * 将指定 stack 加入 List
     * @param stack 指定 stack
     */
    public static void identify(ItemStack stack, UUID u) {
        setUniqueId(u);
        identifier.addIdentifier(stack);
    }
    
    /**
     * 判断 stack 是否存在于 List 中
     * 
     * @param stack
     * @return 是否包含有效标识
     */
    public static boolean isIdentified(ItemStack stack, UUID u) {
        setUniqueId(u);
        return identifier.isIdentified(stack);
    }

    /**
     * 从 List 中删除该 stack
     * @param stack
     */
    public static void remove(ItemStack stack, UUID u) {
        setUniqueId(u);
        identifier.remove(stack);
    }

    /**
     * 设置 Identifier 的目标玩家 UUID 以执行针对性初始化
     * @param u 目标玩家 UUID
     */
    public static void setUniqueId(UUID u) {
        identifier.setUUID(u);
    }

    /**
     * 清除指定玩家的数据
     * @param u 指定玩家 UUID
     */
    public static void clearDataFor(UUID u) {
        ItemStackIdentifier.clearDataFor(u);
    }
}