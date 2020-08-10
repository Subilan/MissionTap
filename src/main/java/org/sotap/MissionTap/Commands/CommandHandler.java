package org.sotap.MissionTap.Commands;

import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.sotap.MissionTap.MissionTap;
import org.sotap.MissionTap.Classes.GlobalMission;
import org.sotap.MissionTap.Utils.Files;
import org.sotap.MissionTap.Utils.Functions;
import org.sotap.MissionTap.Utils.LogUtil;
import org.sotap.MissionTap.Utils.Menus;

public final class CommandHandler implements CommandExecutor {
    public final MissionTap plugin;

    public CommandHandler(MissionTap plugin) {
        this.plugin = plugin;
    }

    public static void noPermission(Player p) {
        LogUtil.warn("你没有执行该指令的权限。", p);
    }

    public static String getPermissionNode(String arg) {
        if (arg == null)
            return "";
        switch (arg) {
            case "d":
                return "daily";
            case "w":
                return "weekly";
            case "s":
                return "special";
            case "i":
                return "inprogress";
            default:
                return arg;
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("missiontap")) {
            Player p = Bukkit.getPlayer(sender.getName());
            if (args.length == 0) {
                Menus.mainMenu.open(p);
                return true;
            }

            if (!args[0].equalsIgnoreCase("about")) {
                if (!p.hasPermission("missiontap." + getPermissionNode(args[0]))) {
                    noPermission(p);
                    return true;
                }
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

                case "special":
                case "s": {
                    Menus.specialMissionMenu.open(p);
                    break;
                }

                case "init": {
                    LogUtil.info("正在初始化当前设定所需数据...", p);
                    Functions.reloadPlugin(plugin);
                    if (!Files.config.getBoolean("require-acceptance")) {
                        GlobalMission globalDailyMission = new GlobalMission("daily");
                        GlobalMission globalWeeklyMission = new GlobalMission("weekly");
                        globalDailyMission.accept();
                        globalWeeklyMission.accept();
                    }
                    LogUtil.success("初始化完成。", p);
                    break;
                }

                case "player": {
                    if (args.length < 3) {
                        LogUtil.failed("参数不足。", p);
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
                                        Functions.clearSubmittion(u, args[3]);
                                        LogUtil.success("成功清除 &a" + pl.getName() + " &r该类型的任务提交记录。",
                                                p);
                                    } else {
                                        LogUtil.failed("无效参数。", p);
                                    }
                                } else {
                                    Functions.clearAllSubmittions(u);
                                    LogUtil.success("成功清除 &a" + pl.getName() + "&r 的&e所有&r任务提交记录。",
                                            p);
                                }
                                Files.savePlayer(playerdata, u);

                                break;
                            }

                            default: {
                                LogUtil.failed("无效参数。", p);
                            }
                        }
                    } else {
                        LogUtil.failed("指定的玩家&c不在线&r或者&c不存在&r。", p);
                    }
                    break;
                }

                case "reload": {
                    Functions.reloadPlugin(plugin);
                    LogUtil.success("成功重载配置文件。", p);
                    break;
                }

                case "about": {
                    sender.sendMessage(LogUtil.translateColor("&e# 关于 MissionTap"));
                    sender.sendMessage(LogUtil
                            .translateColor("&bMissionTap&r 是 SoTap 独立开发的第一个玩法性插件，如有问题请见谅 >_<!"));
                    sender.sendMessage(
                            LogUtil.translateColor("如果您对 MissionTap 有什么想说的，可以在讨论群内联系管理组，畅所欲言~"));
                    sender.sendMessage(LogUtil
                            .translateColor("GitHub: &a&nhttps://github.com/sotapmc/MissionTap"));
                    break;
                }

                default: {
                    LogUtil.failed("无效参数。", p);
                }
            }
        }
        return true;
    }
}
