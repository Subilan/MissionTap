package org.sotap.MissionTap;

import org.bukkit.scheduler.BukkitRunnable;
import org.sotap.MissionTap.Utils.Functions;

public final class Timer extends BukkitRunnable {

    public Timer() {}

    @Override
    public void run() {
        Functions.handleMissionRefresh();
    }
}