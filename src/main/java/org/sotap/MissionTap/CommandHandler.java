package org.sotap.MissionTap;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public final class CommandHandler implements CommandExecutor {
    public MissionTap plug;

    public CommandHandler(MissionTap plug) {
        this.plug = plug;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("missiontap")) {
            Player senderPlayer = Bukkit.getPlayer(sender.getName());
            switch (args[0]) {
                case "daily": {
                    plug.dailyMissionGUI.open(senderPlayer);
                    break;
                }

                case "weekly": {
                    plug.weeklyMissionGUI.open(senderPlayer);
                    break;
                }

                default: {
                    sender.sendMessage(G.translateColor(G.FAILED + "Invalid command"));
                }
            }
            return true;
        }
        return false;
    }
}
