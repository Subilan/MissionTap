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
    private static final String[] BASE = { "daily", "weekly", "inprogress", "special", "about", "player",
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
        StringUtil.copyPartialMatches(arg, commands, result);
        Collections.sort(result);
        return result;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> result = new ArrayList<>();
        if (command.getName().equalsIgnoreCase("missiontap")) {
            switch (args[0]) {
                case "enable":
                case "disable": {
                    result = getResult(args[1], Arrays.asList(ENABLE_DISABLE_OPTIONS));
                    break;
                }

                case "player": {
                    result = getResult(args[2], Arrays.asList(PLAYER_OPTIONS));
                    break;
                }

                default: {
                    if (args.length == 1) {
                        result = getResult(args[0], getAvailableCommands((Player) sender));
                    } else{
                        result = null;
                    }
                }
            }
        }
        return result;
    }
}
