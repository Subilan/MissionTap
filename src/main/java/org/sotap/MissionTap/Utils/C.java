package org.sotap.MissionTap.Utils;

import java.util.Calendar;
import java.util.Date;
import org.bukkit.configuration.file.FileConfiguration;

public final class C {
    public static Date getNextWeeklyRefresh() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        Integer today = cal.get(Calendar.DAY_OF_WEEK);
        Integer refreshDay = G.config.getInt("weekly_refresh_time");
        Integer nextWeekdayOffset = today == (refreshDay - 1) ? 1 : refreshDay + 7 - today;
        cal.add(Calendar.DAY_OF_MONTH, nextWeekdayOffset);
        return cal.getTime();
    }

    public static Date getNextDailyRefresh() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        Integer now = cal.get(Calendar.HOUR_OF_DAY);
        Integer refreshHour = G.config.getInt("daily_refresh_time");
        if (now >= refreshHour) {
            cal.add(Calendar.DATE, 1);
        }
        cal.set(Calendar.HOUR_OF_DAY, refreshHour);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        return cal.getTime();
    }
}