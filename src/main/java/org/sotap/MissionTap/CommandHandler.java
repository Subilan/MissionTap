package org.sotap.MissionTap;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.sotap.MissionTap.Utils.Functions;
import org.sotap.MissionTap.Utils.Logger;
import org.sotap.MissionTap.Utils.Menus;

public final class CommandHandler implements CommandExecutor {
    public final MissionTap plugin;

    public CommandHandler(MissionTap plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("missiontap")) {
            Player p = Bukkit.getPlayer(sender.getName());
            if (args.length == 0) {
                Menus.mainMenu.open(p);
                return true;
            }

            switch (args[0]) {
                case "daily":
                case "d": {
                    Menus.dailyMissionMenu.open(p);
                    break;
                }

                case "weekly":
                case "w": {
                    Menus.weeklyMissionMenu.open(p);
                    break;
                }

                case "inprogress":
                case "i": {
                    Menus.inprogressMenu.open(p);
                    break;
                }

                case "reload": {
                    Functions.reloadPlugin(plugin);
                    sender.sendMessage(Logger.translateColor(Logger.SUCCESS + "Successfully reloaded the plugin."));
                    break;
                }

                default: {
                    sender.sendMessage(Logger.translateColor(Logger.WARN + "Invalid argument."));
                }
            }

            return true;
        }
        return false;
    }
}
