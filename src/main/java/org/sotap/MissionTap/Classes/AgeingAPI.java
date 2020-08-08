package org.sotap.MissionTap.Classes;

import org.bukkit.Bukkit;
import org.sotap.Ageing.API;
import org.sotap.Ageing.Ageing;

public final class AgeingAPI {
    public static Ageing ageing;
    public static API api;

    public static boolean isAvailable() {
        return ageing != null;
    }

    public static void load() {
        ageing = (Ageing) Bukkit.getPluginManager().getPlugin("Ageing");
        api = ageing.api;
    }
}