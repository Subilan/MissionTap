package org.sotap.MissionTap;

import org.bukkit.scheduler.BukkitRunnable;
import org.sotap.MissionTap.Utils.Functions;

public final class GlobalTasks extends BukkitRunnable {

    public GlobalTasks() {}

    @Override
    public void run() {
        Functions.handleMissionGeneration();
    }
}