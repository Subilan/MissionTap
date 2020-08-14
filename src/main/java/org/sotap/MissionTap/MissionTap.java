package org.sotap.MissionTap;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.sotap.MissionTap.Classes.AgeingAPI;
import org.sotap.MissionTap.Commands.CommandHandler;
import org.sotap.MissionTap.Commands.Tab;
import org.sotap.MissionTap.Utils.Events;
import org.sotap.MissionTap.Utils.Files;
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
        handleMissionGeneration();
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

    public void handleMissionGeneration() {
        if (Files.isEmptyConfiguration(Files.dailyMissions)) {
            LogUtil.warn("找不到每日任务池的内容。");
        }
        if (Files.isEmptyConfiguration(Files.weeklyMissions)) {
            LogUtil.warn("找不到每周任务池的内容。");
        }
        if (Files.isEmptyConfiguration(Files.dailyMissions) && Files.isEmptyConfiguration(Files.weeklyMissions)) {
            LogUtil.warn("请在任务编写好后输入 &b/mt reload&r 来重载任务，在没写好并重载前请&c不要&r让玩家进入服务器。");
            LogUtil.warn("若已经有玩家进入服务器，则应当让玩家退出后重新加入，否则任务数据为空。");
        }
        if (!Files.isEmptyConfiguration(Files.dailyMissions) || !Files.isEmptyConfiguration(Files.weeklyMissions)) {
            LogUtil.info("刷新玩家任务中...");
            Functions.handleMissionRefresh();
            LogUtil.success("刷新成功。");
        }
    }
}
