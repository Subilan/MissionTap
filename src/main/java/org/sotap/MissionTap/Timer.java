package org.sotap.MissionTap;

import org.bukkit.scheduler.BukkitRunnable;
import org.sotap.MissionTap.Utils.Functions;

public final class Timer extends BukkitRunnable {
    private final MissionTap plugin;

    public Timer(MissionTap plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        Functions.refreshMissions(plugin);
    }
}