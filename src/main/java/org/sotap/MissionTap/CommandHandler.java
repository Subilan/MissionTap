package org.sotap.MissionTap;

import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.sotap.MissionTap.Utils.Files;
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

                case "player": {
                    if (args.length < 3) {
                        sender.sendMessage(Logger.translateColor(Logger.FAILED + "Not enough arguments."));
                        break;
                    }
                    Player pl = Bukkit.getPlayer(args[1]);
                    if (pl != null) {
                        UUID u = pl.getUniqueId();
                        FileConfiguration playerdata = Files.loadPlayer(u);
                        switch (args[2]) {
                            case "clear-submittion": {
                                if (args.length >= 4) {
                                    if (List.of("daily", "weekly").contains(args[3])) {
                                        playerdata.set("submitted-list." + args[3], null);
                                        sender.sendMessage(Logger.translateColor(Logger.SUCCESS + "Successfully cleared &a" + pl.getName() + "&r's &e" + args[3] + " &rmission submittion history."));
                                    } else {
                                        sender.sendMessage(Logger.translateColor(Logger.FAILED + "Invalid argument."));
                                    }
                                } else {
                                    for (String type : new String[] {"daily", "weekly"}) {
                                        playerdata.set("submitted-list." + type, null);
                                    }
                                    sender.sendMessage(Logger.translateColor(Logger.SUCCESS + "Successfully cleared all of &a" + pl.getName() + "&r's mission submittion history."));
                                }
                                Files.savePlayer(playerdata, u);
                                
                                break;
                            }

                            default: {
                                sender.sendMessage(Logger.translateColor(Logger.FAILED + "Invalid option."));
                            }
                        }
                    } else {
                        sender.sendMessage(Logger.translateColor(Logger.FAILED + "The player specified is not &conline&r or &cdoes not exist&r."));
                    }
                    break;
                }

                case "reload": {
                    Functions.reloadPlugin(plugin);
                    sender.sendMessage(Logger.translateColor(Logger.SUCCESS + "Successfully reloaded the plugin."));
                    break;
                }

                default: {
                    sender.sendMessage(Logger.translateColor(Logger.FAILED + "Invalid argument."));
                }
            }

            return true;
        }
        return false;
    }
}
