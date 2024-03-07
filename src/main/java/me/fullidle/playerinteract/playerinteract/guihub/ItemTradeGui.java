package me.fullidle.playerinteract.playerinteract.guihub;

import lombok.Getter;
import lombok.Setter;
import me.fullidle.ficore.ficore.common.api.ineventory.ListenerInvHolder;
import me.fullidle.playerinteract.playerinteract.Main;
import me.fullidle.playerinteract.playerinteract.MethodUtil;
import net.minecraft.advancements.critereon.UsedEnderEyeTrigger;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

@Getter
public class ItemTradeGui extends ListenerInvHolder {
    private final Inventory inventory;
    private final UploadHolder uploadHolder;
    private final UploadHolder uploadHolder1;
    private Player readyPlayer = null;
    private boolean complete = false;

    public ItemTradeGui(Player player, Player player1) {
        //初始化
        Main plugin = Main.getInstance();
        this.inventory = Bukkit.createInventory(this, 9, "§3§lItemTrade");
        this.uploadHolder = new UploadHolder(player, this);
        this.uploadHolder1 = new UploadHolder(player1, this);
        //GUI布局
        {
            //分界线
            {
                ItemStack itemStack = MethodUtil.getGlassPane();
                ItemMeta itemMeta = itemStack.getItemMeta();
                itemMeta.setDisplayName(" ");
                itemStack.setItemMeta(itemMeta);
                for (int i = 0; i < this.inventory.getSize(); i++) {
                    this.inventory.setItem(i, itemStack);
                }
            }
            //玩家头
            {
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                    this.inventory.setItem(2, this.uploadHolder.head);
                    this.inventory.setItem(6, this.uploadHolder1.head);
                });
            }
            //中间的同意按钮(它有4个状态)
            {
                ItemStack itemStack = new ItemStack(Material.ENDER_PEARL);
                ItemMeta itemMeta = itemStack.getItemMeta();
                itemMeta.setDisplayName("§a§lConfirm");
                itemMeta.setLore(Collections.singletonList("§a§l[Click Confirm]"));
                itemStack.setItemMeta(itemMeta);
                this.inventory.setItem(4, itemStack);
            }
        }
        //Event Handler
        onClick(e -> {
            ItemStack item = e.getCurrentItem();
            e.setCancelled(true);
            Player whoClicked = (Player) e.getWhoClicked();
            if (item == null || item.getType().equals(Material.AIR)) {
                return;
            }
            //如果是确认按钮 [4个状态]
            if (e.getSlot() == 4){
                if (item.getType().equals(Material.NETHER_STAR) && item.containsEnchantment(Enchantment.LUCK)){
                    //完成交易
                    this.complete = true;
                    //交换物品
                    MethodUtil.givePlayerInvItem(player,uploadHolder1.inventory);
                    MethodUtil.givePlayerInvItem(player1,uploadHolder.inventory);
                    //发送完成并关闭界面
                    player.sendMessage("§6§lTransaction complete!");
                    player1.sendMessage("§6§lTransaction complete!");
                    player.closeInventory();
                    player1.closeInventory();
                    return;
                }
                //其他情况全部进行阶段变更
                if (this.readyPlayer != whoClicked) nextState(whoClicked);
                return;
            }
            //不是按状态按钮的时候
            //为了安全
            resetState();
            //玩家头
            if (item.getItemMeta() instanceof SkullMeta){
                UploadHolder upHolder = e.getSlot() == 2?uploadHolder:uploadHolder1;
                whoClicked.openInventory(upHolder.inventory);
                return;
            }
        });
        onClose(e-> Bukkit.getScheduler().runTask(plugin,()->{
            //如果已经被结束了那么就不处理了
            if (this.complete) {
                return;
            }

            //玩家跳转不处理
            Player p = (Player) e.getPlayer();
            if (uploadHolder.beingUsed == p || uploadHolder1.beingUsed == p) {
                return;
            }

            //一人关闭等同放弃
            this.complete = true;
            Player opposite = (uploadHolder.player == p ? uploadHolder1 : uploadHolder).player;
            opposite.closeInventory();
            //返还物品
            MethodUtil.givePlayerInvItem(uploadHolder.player,uploadHolder.inventory);
            MethodUtil.givePlayerInvItem(uploadHolder1.player,uploadHolder1.inventory);

            //发送放弃消息
            opposite.sendMessage(String.format("§6§l%s abandoned the transaction",p.getName()));
            p.sendMessage("§6§lYou gave up on the deal");
        }));
        onDrag(e->e.setCancelled(true));
    }

    private void resetState(){
        this.inventory.getItem(4).setType(Material.ENDER_PEARL);
        this.readyPlayer = null;
    }
    private void nextState(Player whoClick){
        ItemStack item = this.inventory.getItem(4);
        Material type = item.getType();
        Material eye = Material.getMaterial(MethodUtil.isHighVersion() ? "ENDER_EYE" : "EYE_OF_ENDER");
        assert eye != null;
        if (type.equals(Material.NETHER_STAR)){
            if (item.containsEnchantment(Enchantment.LUCK))return;
            item.addUnsafeEnchantment(Enchantment.LUCK,1);
            this.readyPlayer = whoClick;
        }
        if (type.equals(eye)){
            item.setType(Material.NETHER_STAR);
        }
        if (type.equals(Material.ENDER_PEARL)) {
            item.setType(eye);
            this.readyPlayer = whoClick;
        }
    }

    @Getter
    private static class UploadHolder extends ListenerInvHolder {
        private final Inventory inventory;
        private final Player player;
        private final ItemStack head;
        private final ItemTradeGui up;
        @Setter
        private Player beingUsed;

        public UploadHolder(Player player, ItemTradeGui up) {
            Main plugin = Main.getInstance();
            this.player = player;
            this.inventory = Bukkit.createInventory(this, 54, "§3§l" + player.getName());
            this.up = up;
            this.head = MethodUtil.getPlayerHeadItemStack();
            SkullMeta itemMeta = (SkullMeta) this.head.getItemMeta();
            assert itemMeta != null;
            itemMeta.setDisplayName("§3§lItems uploaded by " + player.getName());
            itemMeta.setOwningPlayer(player);
            this.head.setItemMeta(itemMeta);
            //sub EventHandler
            onOpen(e ->{
                this.beingUsed = (Player) e.getPlayer();
            });
            onClick(e -> {
                if (e.getWhoClicked() != this.player) e.setCancelled(true);
            });
            onClose(e -> {
                this.beingUsed = null;
                Bukkit.getScheduler().runTask(plugin, () -> {
                    //上个界面已经结束了不返回
                    if (up.complete)return;
                    e.getPlayer().openInventory(up.getInventory());
                });
            });
        }
    }
}