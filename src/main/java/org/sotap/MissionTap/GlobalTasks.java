package org.sotap.MissionTap;

import org.bukkit.scheduler.BukkitRunnable;
import org.sotap.MissionTap.Utils.Functions;

public final class GlobalTasks extends BukkitRunnable {

    public GlobalTasks() {
    }

    @Override
    public void run() {
        for (String type : new String[]{"daily", "weekly"}) {
            if (Functions.isTimeForRefreshFor(type)) {
                Functions.handleMissionRefresh(type);
            }
        }
    }
}
