package org.sotap.MissionTap;

import org.bukkit.scheduler.BukkitRunnable;

public final class Timer extends BukkitRunnable {
    private final MissionTap plug;

    public Timer(MissionTap plug) {
        this.plug = plug;
    }

    @Override
    public void run() {
        plug.refreshMissions();
    }
}
