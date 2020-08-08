package org.sotap.MissionTap;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.sotap.MissionTap.Classes.AgeingAPI;
import org.sotap.MissionTap.Utils.Functions;
import org.sotap.MissionTap.Utils.Logger;

public final class MissionTap extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        Functions.initUtils(this);
        Functions.initMissions(this);
        Functions.initMenus(this);
        Functions.initEvents(this);
        Functions.refreshMissions(this);
        @SuppressWarnings("unused")
        BukkitTask timer = new Timer(this).runTaskTimer(this, 0, 20);
        Bukkit.getPluginCommand("missiontap").setExecutor(new CommandHandler(this));
        AgeingAPI.load();
        if (!AgeingAPI.isAvailable()) {
            log(Logger.FAILED + "找不到必要的依赖 &eAgeing&r。");
        }
        log(Logger.SUCCESS + "插件已&a启用&r。");
    }

    @Override
    public void onDisable() {
        log(Logger.SUCCESS + "插件已&c禁用&r。");
    }

    public void log(String message) {
        this.getLogger().info(Logger.translateColor(message));
    }

    public void reload() {
        Functions.initUtils(this);
    }
}
