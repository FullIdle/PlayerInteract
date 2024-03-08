package me.fullidle.playerinteract.playerinteract.commands;

import com.google.common.collect.Lists;
import me.fullidle.playerinteract.playerinteract.Main;
import me.fullidle.playerinteract.playerinteract.MethodUtil;
import me.fullidle.playerinteract.playerinteract.guihub.InteractGUI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PlayerInteractCmd implements TabExecutor {
    public Main plugin = Main.getInstance();
    public static ArrayList<String> sub = Lists.newArrayList(
            "help","reload","open"
    );
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length >= 1) {
            if (sub.contains(args[0].toLowerCase())) {
                switch (args[0]) {
                    case "help": {
                        //发送所有指令帮助
                        sender.sendMessage(plugin.getCmdHelp());
                        return true;
                    }
                    case "reload": {
                        String per = getSubPermission("reload");
                        if (!sender.hasPermission(per)){
                            sender.sendMessage("§c§lMissing permissions:§r§3"+per);
                        }

                        //重载
                        plugin.reloadConfig();
                        sender.sendMessage("§6§lReload successful!");
                        return true;
                    }
                    case "open": {
                        String per = getSubPermission("open");
                        if (!sender.hasPermission(per)){
                            sender.sendMessage("§c§lMissing permissions:§r§3"+per);
                        }

                        //打开交互gui
                        if (args.length >= 2){
                            boolean b = args.length>=3;
                            String forPer = getSubPermission("for");
                            if (b&&!sender.hasPermission(forPer)){
                                sender.sendMessage("§c§lMissing permissions:§r§3"+forPer);
                                return true;
                            }
                            String otherName = b?args[2]:args[1];
                            Player other = Bukkit.getPlayer(otherName);
                            if (!MethodUtil.isOnlinePlayer(other)) {
                                sender.sendMessage(MethodUtil.getUnavailablePlayerMsg(otherName));
                                return true;
                            }
                            Player player;
                            if (b){
                                player = Bukkit.getPlayer(args[1]);
                                if (!MethodUtil.isOnlinePlayer(player)) {
                                    sender.sendMessage(MethodUtil.getUnavailablePlayerMsg(args[1]));
                                    return true;
                                }
                            }else {
                                if (!(sender instanceof Player)){
                                    sender.sendMessage(MethodUtil.getNotPlayerMsg());
                                    return true;
                                }
                                player = (Player) sender;
                            }
                            //判断是否自己请求的自己
                            if (player.equals(other)) {
                                sender.sendMessage("§c§lCannot send to yourself!");
                                return true;
                            }

                            InteractGUI gui = new InteractGUI(other,player);
                            player.openInventory(gui.getInventory());
                            return true;
                        }
                        break;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender,Command cmd,String label,String[] args) {
        if (args.length < 1) return sub;
        if (args.length == 1) return sub.stream().filter(s->s.startsWith(args[0])).collect(Collectors.toList());
        return null;
    }

    public String getSubPermission(String sub){
        String lowerCasePlugin = plugin.getName().toLowerCase();
        return lowerCasePlugin + "." + lowerCasePlugin + "." + sub;
    }
}
