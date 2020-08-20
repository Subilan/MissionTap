package org.sotap.MissionTap.Classes;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.bukkit.inventory.ItemStack;

public final class ItemStackIdentifier {
    public Map<UUID, Map<ItemStack, Boolean>> identified;

    public ItemStackIdentifier() {
        this.identified = new HashMap<>();
    }

    public void addIdentifier(ItemStack stack) {
        UUID random = UUID.randomUUID();
        identified.put(random, createState(stack, true));
    }

    public boolean isIdentified(ItemStack stack, boolean requireValid) {
        return identified.containsValue(createState(stack, requireValid));
    }

    public void setInvalid(ItemStack stack) {
        for (UUID u : identified.keySet()) {
            if (Objects.equals(identified.get(u), createState(stack, true))) {
                identified.remove(u);
                identified.put(u, createState(stack, false));
                break;
            }
        }
    }

    private Map<ItemStack, Boolean> createState(ItemStack stack, Boolean bool) {
        Map<ItemStack, Boolean> map = new HashMap<>();
        map.put(stack, bool);
        return map;
    }
}
