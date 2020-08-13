package org.sotap.MissionTap;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.sotap.MissionTap.Classes.AgeingAPI;
import org.sotap.MissionTap.Commands.CommandHandler;
import org.sotap.MissionTap.Commands.Tab;
import org.sotap.MissionTap.Utils.Events;
import org.sotap.MissionTap.Utils.Functions;
import org.sotap.MissionTap.Utils.LogUtil;
import org.sotap.MissionTap.Utils.Menus;

public final class MissionTap extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        Functions.reloadPlugin(this);
        Menus.init(this);
        Events.init(this);
        LogUtil.info("正在初始化任务...");
        Functions.handleMissionRefresh();
        LogUtil.success("初始化成功。");
        @SuppressWarnings("unused")
        BukkitTask timer = new GlobalTasks().runTaskTimer(this, 0, 20);
        Bukkit.getPluginCommand("missiontap").setExecutor(new CommandHandler(this));
        Bukkit.getPluginCommand("missiontap").setTabCompleter(new Tab());
        AgeingAPI.load();
        if (!AgeingAPI.isAvailable()) {
            LogUtil.failed("找不到必要的依赖 &eAgeing&r。");
        }
        LogUtil.success("插件已&a启用&r。");
    }

    @Override
    public void onDisable() {
        LogUtil.success("插件已&c禁用&r。");
    }
}
