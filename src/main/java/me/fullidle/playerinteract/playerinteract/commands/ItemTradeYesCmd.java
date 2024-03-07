package me.fullidle.playerinteract.playerinteract.commands;

import me.fullidle.playerinteract.playerinteract.MethodUtil;
import me.fullidle.playerinteract.playerinteract.guihub.ItemTradeGui;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ItemTradeYesCmd implements TabExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)){
            sender.sendMessage(MethodUtil.getNotPlayerMsg());
            return true;
        }
        Player player = (Player) sender;
        ArrayList<Player> players = ItemTradeRequestCmd.request.get(player);
        if (players == null||players.isEmpty()) {
            sender.sendMessage("§6§lYou have no request!");
            return true;
        }
        //获取玩家并去除记录
        Player player1 = players.remove(players.size()-1);
        if (!MethodUtil.isOnlinePlayer(player1)){
            sender.sendMessage(MethodUtil.getUnavailablePlayerMsg(player1.getName()));
        }
        //打开交易界面
        ItemTradeGui gui = new ItemTradeGui(player, player1);
        player.closeInventory();
        player.openInventory(gui.getInventory());
        player.sendMessage(String.format("§a§lYou agree to %s item trading request",player1.getName()));
        player1.closeInventory();
        player1.openInventory(gui.getInventory());
        player1.sendMessage(String.format("§a§l%s has agreed to your item trade request!",player.getName()));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender,Command cmd,String label,String[] args) {
        return null;
    }
}
