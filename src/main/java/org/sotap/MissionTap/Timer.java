package org.sotap.MissionTap;

import java.sql.Date;
import java.util.Calendar;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

public final class Timer extends BukkitRunnable {
    private final MissionTap plug;
    private final FileConfiguration config;
    private final FileConfiguration latestMissions;
    private final Integer weeklyRefresh;
    private final Integer dailyRefresh;

    public Timer(MissionTap plug) {
        this.plug = plug;
        this.latestMissions = plug.latestMissions;
        this.config = plug.getConfig();
        this.weeklyRefresh = config.getInt("weekly_refresh_time");
        this.dailyRefresh = config.getInt("daily_refresh_time");
    }

    @Override
    public void run() {
        // check expiration and recreate missions.
    }

    private int getHourNow(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        return c.get(Calendar.HOUR_OF_DAY);
    }

    private int getDayInWeekNow(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        return c.get(Calendar.DAY_OF_WEEK);
    }
}