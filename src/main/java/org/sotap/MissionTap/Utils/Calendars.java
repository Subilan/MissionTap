package org.sotap.MissionTap.Utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public final class Calendars {

    public static SimpleDateFormat getFormatter() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }

    public static String stampToString(Long stamp) {
        return getFormatter().format(new Date(stamp));
    }

    public static Long getNow() {
        return new Date().getTime();
    }

    public static Date getNextWeeklyRefresh() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        Integer today = cal.get(Calendar.DAY_OF_WEEK);
        Integer refreshDay = Files.config.getInt("weekly-refresh-time");
        Integer nextWeekdayOffset = today == (refreshDay - 1) ? 1 : refreshDay + 7 - today;
        cal.add(Calendar.DAY_OF_MONTH, nextWeekdayOffset);
        return cal.getTime();
    }

    public static Date getNextDailyRefresh() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        Integer now = cal.get(Calendar.HOUR_OF_DAY);
        Integer refreshHour = Files.config.getInt("daily-refresh-time");
        if (now >= refreshHour) {
            cal.add(Calendar.DATE, 1);
        }
        cal.set(Calendar.HOUR_OF_DAY, refreshHour);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        return cal.getTime();
    }

    public static long getMissionExpiration(String type) {
        if (!List.of("daily", "weekly").contains(type))
            return 0;
        if (type == "daily") {
            return getNextDailyRefresh().getTime();
        } else {
            Date weeklyRefresh = getNextWeeklyRefresh();
            if (Files.config.getBoolean("allow-tarriance")) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(new Date());
                cal.add(Calendar.MONTH, 1);
                weeklyRefresh = cal.getTime();
            }
            return weeklyRefresh.getTime();
        }
    }

    public static long getNextRefresh(String type) {
        if (!List.of("daily", "weekly").contains(type))
            return 0;
        return type == "daily" ? getNextDailyRefresh().getTime() : getNextWeeklyRefresh().getTime();
    }

    // public static boolean isNowTheFirstWeekOfTheMonth() {
    // Calendar cal = Calendar.getInstance();
    // cal.setTime(new Date());
    // Integer now = cal.get(Calendar.WEEK_OF_MONTH);
    // return now == 1;
    // }
}
