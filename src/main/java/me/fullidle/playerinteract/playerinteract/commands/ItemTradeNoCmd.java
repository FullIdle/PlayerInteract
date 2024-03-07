package me.fullidle.playerinteract.playerinteract.commands;

import me.fullidle.playerinteract.playerinteract.MethodUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ItemTradeNoCmd implements TabExecutor {
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
        ItemTradeRequestCmd.request.remove(player);
        for (Player p : players) {
            if (MethodUtil.isOnlinePlayer(p)) {
                p.sendMessage(String.format("§6§l%s has rejected your item trade request",player.getName()));
            }
        }
        player.sendMessage("§6§lYou have rejected all item trade requests");
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        return null;
    }
}

