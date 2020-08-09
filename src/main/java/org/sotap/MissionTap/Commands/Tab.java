package org.sotap.MissionTap.Commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;

public final class Tab implements TabCompleter {
    private final List<String> ITERABLE_BASE;

    public Tab() {
        this.ITERABLE_BASE = List.of("daily", "weekly", "inprogress", "special", "d", "w", "i", "s",
                "about", "init", "player", "reload");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] arguments) {
        List<String> result = new ArrayList<>();
        if (command.getName().equalsIgnoreCase("missiontap")) {
            result = StringUtil.copyPartialMatches(arguments[0], ITERABLE_BASE, result);
            Collections.sort(result);
        }
        return result;
    }
}
