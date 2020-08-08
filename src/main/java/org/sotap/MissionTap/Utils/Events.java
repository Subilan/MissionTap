package org.sotap.MissionTap.Utils;

import org.sotap.MissionTap.MissionTap;
import org.sotap.MissionTap.Events.GlobalEvents;
import org.sotap.MissionTap.Events.MissionEvents;

public final class Events {
    public static GlobalEvents global;
    public static MissionEvents mission;

    public static void refresh(MissionTap plugin) {
        global = new GlobalEvents(plugin);
        mission = new MissionEvents(plugin);
    }
}