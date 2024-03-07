package me.fullidle.playerinteract.playerinteract;

import me.fullidle.playerinteract.playerinteract.commands.ItemTradeRequestCmd;
import me.fullidle.playerinteract.playerinteract.guihub.InteractGUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.List;

import static me.fullidle.playerinteract.playerinteract.guihub.InteractGUI.cache;

public class PlayerListener implements Listener {
    @EventHandler
    public void interact(PlayerInteractEntityEvent e){
        if (e.getHand().equals(EquipmentSlot.OFF_HAND)) return;
        if (!(e.getRightClicked() instanceof Player)) return;
        Player other = ((Player) e.getRightClicked());
        Player player = e.getPlayer();
        PlayerInventory pInv = player.getInventory();
        if (player.isSneaking()) {
            return;
        }
        //空手
        if (!((pInv.getItemInMainHand() == null || pInv.getItemInMainHand().getType().equals(Material.AIR))
                && (pInv.getItemInOffHand() == null || pInv.getItemInOffHand().getType().equals(Material.AIR)))) {
            return;
        }
        //开
        InteractGUI gui = new InteractGUI(other,player);
        Inventory inv = gui.getInventory();
        player.openInventory(inv);
    }
    @EventHandler
    public void quit(PlayerQuitEvent e){
        Player player = e.getPlayer();
        ArrayList<Player> list = cache.get(player);
        if (list == null||list.isEmpty()) {
            return;
        }
        //将所有打开了这个玩家界面的都关掉
        for (Player openPlayer : ((List<Player>) list.clone())) {
            if (openPlayer.getOpenInventory().getTopInventory().getHolder() instanceof InteractGUI){
                openPlayer.closeInventory();
                openPlayer.sendMessage("§6§lThe player is offline!");
            }
        }
        cache.remove(player);
        //清楚物品交易请求缓存
        ItemTradeRequestCmd.request.remove(player);
    }
}
