package org.sotap.MissionTap.Classes;

import org.bukkit.Bukkit;
import org.sotap.Ageing.API;
import org.sotap.Ageing.Ageing;

import java.util.Objects;

public final class AgeingAPI {
    public static Ageing ageing;
    public static API api;

    public static boolean isAvailable() {
        return ageing != null;
    }

    public static void load() {
        ageing = (Ageing) Objects.requireNonNull(Bukkit.getPluginManager().getPlugin("Ageing"));
        api = ageing.api;
    }
}