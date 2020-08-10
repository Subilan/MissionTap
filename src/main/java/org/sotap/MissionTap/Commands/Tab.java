package org.sotap.MissionTap.Commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

public final class Tab implements TabCompleter {
    private static final String[] BASE = { "daily", "weekly", "inprogress", "special", "about", "init", "player",
            "reload", "enable", "disable" };
    private static final String[] PLAYER_OPTIONS = { "clear-submittions", "clear-missions" };
    private static final String[] ENABLE_DISABLE_OPTIONS = { "special" };

    public Tab() {
    }

    public List<String> getAvailableCommands(Player p) {
        List<String> available = new ArrayList<>();
        for (String cmd : BASE) {
            if (p.hasPermission("missiontap." + cmd)) {
                available.add(cmd);
            }
        }
        return available;
    }

    public List<String> getResult(String arg, List<String> commands) {
        List<String> result = new ArrayList<>();
        result = StringUtil.copyPartialMatches(arg, commands, result);
        Collections.sort(result);
        return result;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] arguments) {
        List<String> result = new ArrayList<>();
        if (command.getName().equalsIgnoreCase("missiontap")) {
            switch (arguments.length) {
                case 1: {
                    result = getResult(arguments[0], getAvailableCommands((Player) sender));
                    break;
                }

                case 2: {
                    if (arguments[0] == "enable" || arguments[0] == "disable") {
                        result = getResult(arguments[1], Arrays.asList(ENABLE_DISABLE_OPTIONS));
                    }
                    break;
                }

                case 3: {
                    if (arguments[0] == "player") {
                        result = getResult(arguments[0], Arrays.asList(PLAYER_OPTIONS));
                    }
                    break;
                }
            }
            if (result.size() == 0)
                result = null;
        }
        return result;
    }
}
