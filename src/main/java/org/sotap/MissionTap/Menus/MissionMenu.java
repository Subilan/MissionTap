package org.sotap.MissionTap.Menus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.sotap.MissionTap.MissionTap;
import org.sotap.MissionTap.Classes.Mission;
import org.sotap.MissionTap.Utils.Files;
import org.sotap.MissionTap.Utils.LogUtil;

public final class MissionMenu implements Listener {
    private final Inventory inventory;
    private final String type;
    private ConfigurationSection objects;
    private List<Mission> missions;

    public MissionMenu(String type, MissionTap plugin) {
        this.type = type;
        this.inventory = Bukkit.createInventory(null, InventoryType.CHEST, "任务列表");
        this.missions = new ArrayList<>();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    private void init(UUID u) {
        this.objects = type != null ? Files.getPlayerMissions(u).getConfigurationSection(type) : null;
        if (Files.isEmptyConfiguration(objects)) {
            LogUtil.warn("由于数据为空，无法加载 GUI。");
            return;
        }
        Map<String, Object> missionObjects = objects.getValues(false);
        int index = 0;
        for (String key : missionObjects.keySet()) {
            Mission m = new Mission(u, type, key);
            inventory.setItem(index, m.getItemStack(null));
            missions.add(m);
            index++;
        }
    }

    public void open(final Player p) {
        if (!Files.config.getBoolean("require-acceptance")) {
            LogUtil.info("你现在不需要手动接受任务。", p);
            return;
        }
        if (!Files.config.getBoolean("special-missions") && type == "special") {
            LogUtil.info("当前特殊任务尚未开放。", p);
            return;
        }
        init(p.getUniqueId());
        p.openInventory(inventory);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (e.getInventory() != inventory)
            return;
        e.setCancelled(true);
        final ItemStack clicked = e.getCurrentItem();
        if (clicked == null)
            return;
        if (clicked.getType() == Material.AIR)
            return;
        final Player p = (Player) e.getWhoClicked();
        final UUID u = p.getUniqueId();
        final Integer slot = e.getSlot();
        final Mission clickedMission = missions.get(slot);
        p.closeInventory();
        if (clickedMission.isAccepted(u)) {
            LogUtil.failed("你不能接受进行中的任务。", p);
            return;
        }
        if (!Files.config.getBoolean("allow-multiple-acceptance")) {
            if (clickedMission.isSubmitted(u)) {
                LogUtil.warn("你不能接受先前&e已完成的&r任务！", p);
                clickedMission.destory(u);
                return;
            }
        }
        clickedMission.accept(u);
        LogUtil.success("成功接受任务 &a" + clickedMission.getName() + "&r！", p);
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent e) {
        if (e.getInventory() == inventory)
            e.setCancelled(true);
    }
}
