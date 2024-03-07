package me.fullidle.playerinteract.playerinteract.commands;

import me.fullidle.playerinteract.playerinteract.Main;
import me.fullidle.playerinteract.playerinteract.MethodUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ItemTradeRequestCmd implements TabExecutor {
    public static final Map<Player, ArrayList<Player>> request = new HashMap<>();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)){
            sender.sendMessage(MethodUtil.getNotPlayerMsg());
            return true;
        }
        if (args.length > 0){
            Player player = ((Player) sender);
            Player other = Bukkit.getPlayer(args[0]);
            if (!MethodUtil.isOnlinePlayer(other)) {
                player.sendMessage(MethodUtil.getUnavailablePlayerMsg(args[0]));
                return true;
            }
            //判断是两个玩家是同一个
            if (player.equals(other)) {
                sender.sendMessage("§c§lCannot send to yourself!");
                return true;
            }
            //获取请求记录,已经发送过就提示一下,反之添加请求并加个延迟执行
            ArrayList<Player> requestPlayers = request.computeIfAbsent(other, k -> new ArrayList<>());
            if (!requestPlayers.contains(player)) {
                requestPlayers.add(player);
                other.sendMessage(String.format("§6§l%s has sent you an item transaction request",player.getName()));
                other.sendMessage("§c§l/itryes §6§lAccept request");
                other.sendMessage("§c§l/itrno §6§lDeny request");
                player.sendMessage("§6§lThe request will timeout after 30 seconds!");
                Bukkit.getScheduler().runTaskLater(Main.getInstance(),()->{
                    if (request.containsKey(other)) {
                        ArrayList<Player> list = request.get(other);
                        if (!list.contains(player)) {
                            return;
                        }
                        list.remove(player);
                    }
                    try {
                        other.sendMessage(String.format("§6§l%s item trade request has expired!",player.getName()));
                        player.sendMessage(String.format("§6§l%s Didn't respond to you",args[0]));
                    } catch (Exception ignored) {
                    }
                },600);
            }
            player.sendMessage(String.format("§6§lRequest sent to %s",other.getName()));
            return true;
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender,Command cmd,String label,String[] args) {
        if (args.length <= 1){
            return Bukkit.getOnlinePlayers().stream().map(OfflinePlayer::getName).collect(Collectors.toList());
        }
        return null;
    }
}
