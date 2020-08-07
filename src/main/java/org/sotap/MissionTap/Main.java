package org.sotap.MissionTap;

import org.bukkit.plugin.java.JavaPlugin;
// import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.sotap.MissionTap.Utils.Functions;
import org.sotap.MissionTap.Utils.Logger;

public final class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        Functions.initUtils(this);
        Functions.initMissions(this);
        Functions.initMenus(this);
        Functions.refreshMissions(this);
        @SuppressWarnings("unused")
        BukkitTask timer = new Timer(this).runTaskTimer(this, 0, 20);
        log(Logger.SUCCESS + "The plugin has been &aenabled&r.");
    }

    @Override
    public void onDisable() {
        log(Logger.SUCCESS + "The plugin has been &cdisabled&r.");
    }

    public void log(String message) {
        this.getLogger().info(Logger.translateColor(message));
    }

    public void reload() {
        Functions.initUtils(this);
    }
}
