package org.sotap.MissionTap;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.sotap.MissionTap.Utils.G;

public final class CommandHandler implements CommandExecutor {
    public MissionTap plug;

    public CommandHandler(MissionTap plug) {
        this.plug = plug;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("missiontap")) {
            Player senderPlayer = Bukkit.getPlayer(sender.getName());
            if (args.length == 0) {
                // default behaviour
                plug.mainMenu.open(senderPlayer);
                return true;
            }

            switch (args[0]) {
                case "daily": {
                    plug.dailyMissionMenu.open(senderPlayer);
                    break;
                }

                case "weekly": {
                    plug.weeklyMissionMenu.open(senderPlayer);
                    break;
                }

                case "inprocess": {
                    plug.inprogMenu.open(senderPlayer);
                    break;
                }

                default: {
                    sender.sendMessage(G.translateColor(G.FAILED + "Invalid argument"));
                }
            }

            return true;
        }
        return false;
    }
}
