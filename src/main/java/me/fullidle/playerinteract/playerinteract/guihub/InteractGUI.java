package me.fullidle.playerinteract.playerinteract.guihub;

import lombok.Getter;
import me.fullidle.ficore.ficore.common.api.ineventory.ListenerInvHolder;
import me.fullidle.playerinteract.playerinteract.Function;
import me.fullidle.playerinteract.playerinteract.Main;
import me.fullidle.playerinteract.playerinteract.MethodUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Getter
public class InteractGUI extends ListenerInvHolder {
    public static final Map<Player,ArrayList<Player>> cache = new HashMap<>();
    private static final List<Integer> functionSlot;
    static {
        functionSlot = Stream.concat(IntStream.rangeClosed(29,33).boxed(),
                IntStream.rangeClosed(38,42).boxed()).collect(Collectors.toList());
    }

    private final Inventory inventory;
    private final Player player;
    private final Player openPlayer;
    private String guiKey;
    private Function[][] groupFunctions = new Function[1][0];
    private Integer page;
    private final Map<ItemStack,Function> correspondMap = new HashMap<ItemStack,Function>(){
        @Override
        public Function get(Object key) {
            ItemStack i = (ItemStack) key;
            for (Entry<ItemStack, Function> entry : entrySet()) {
                if (entry.getKey().equals(i)||(entry.getKey().getAmount()==i.getAmount()&&entry.getKey().isSimilar(i))) {
                    return entry.getValue();
                }
            }
            return null;
        }
    };

    public InteractGUI(Player player,Player openPlayer) {
        this.openPlayer = openPlayer;
        this.player = player;
        this.inventory = Bukkit.getServer().createInventory(this,54, MethodUtil.colorPapi("%player_name%",player));
        Main plugin = Main.getInstance();
        //GUI布局
        {
            //平铺
            {
                ItemStack itemStack = MethodUtil.getGlassPane();
                ItemMeta itemMeta = itemStack.getItemMeta();
                itemMeta.setDisplayName(" ");
                itemMeta.setLore(new ArrayList<>());
                itemStack.setItemMeta(itemMeta);
                //填满,对功能去不填
                for (int i = 0; i < 54; i++) {
                    if (!functionSlot.contains(i))
                        inventory.setItem(i,itemStack);
                }
            }
            //玩家头
            {
                ItemStack itemStack = MethodUtil.getPlayerHeadItemStack();
                SkullMeta itemMeta = (SkullMeta) itemStack.getItemMeta();
                itemMeta.setDisplayName("§3§l"+MethodUtil.colorPapi("%player_name%",player));
                itemStack.setItemMeta(itemMeta);
                inventory.setItem(13,itemStack);
                //预判缓存卡主线程
                Bukkit.getScheduler().runTaskAsynchronously(plugin,()->{
                    itemMeta.setOwningPlayer(player);
                    itemStack.setItemMeta(itemMeta);
                    inventory.setItem(13,itemStack);
                });
            }
            //功能按钮{判断权限获取对于功能}计算保存功能页面
            {
                for (String permissionPriority : plugin.getPermissionPriority()) {
                    if (openPlayer.hasPermission(permissionPriority)) {
                        String[] split = permissionPriority.split("\\.");
                        guiKey = split[split.length-1];
                        break;
                    }
                }
                if (guiKey != null) {
                    List<String> functionNames = plugin.getConfig().getStringList(guiKey+".functions");
                    List<Function[]> groupedFunctions = new ArrayList<>();
                    List<Function> currentFunctions = new ArrayList<>();
                    for (String functionName : functionNames) {
                        Function function = plugin.getFunctions().get(functionName);
                        if (function == null) {
                            continue;
                        }
                        currentFunctions.add(function);
                        if (currentFunctions.size() == 10) {
                            groupedFunctions.add(currentFunctions.toArray(new Function[0]));
                            currentFunctions.clear();
                        }
                    }
                    if (!currentFunctions.isEmpty()) {
                        groupedFunctions.add(currentFunctions.toArray(new Function[0]));
                    }
                    groupFunctions = groupedFunctions.toArray(new Function[0][0]);
                }
                //更改页面到第一页
                changePage(0);
            }
            //翻页功能
            {
                if (groupFunctions.length > 1) {
                    ItemStack itemStack = new ItemStack(Material.ARROW);
                    ItemMeta itemMeta = itemStack.getItemMeta();
                    {
                        itemMeta.setDisplayName("§3§lPrevious page");
                        itemStack.setItemMeta(itemMeta);
                        inventory.setItem(18,itemStack);
                    }
                    {
                        itemMeta.setDisplayName("§3§lNext page");
                        itemStack.setItemMeta(itemMeta);
                        inventory.setItem(26,itemStack);
                    }
                }
            }
        }

        setEventHandler();
    }

    private void setEventHandler(){
        //打开的的时候记录缓存
        onOpen(e->{
            ArrayList<Player> players = cache.computeIfAbsent(player, k -> new ArrayList<>());
            players.add((Player) e.getPlayer());
        });
        //点击事件处理
        onClick(e->{
            e.setCancelled(true);
            ItemStack currentItem = e.getCurrentItem();
            if (currentItem == null||currentItem.getType().equals(Material.AIR)){
                return;
            }
            int slot = e.getSlot();

            //判断是否是功能区
            if (functionSlot.contains(slot)){
                Function function = correspondMap.get(currentItem);
                Player whoClicked = (Player) e.getWhoClicked();
                function.extScript(e, whoClicked,player);
                return;
            }

            //判断上下页按钮
            if (currentItem.getType().equals(Material.ARROW)) {
                int target = 0;
                if (slot == 18&&(target = page-1) < 0){
                    return;
                }
                if (slot == 26&&(target = page+1) == groupFunctions.length){
                    return;
                }
                changePage(target);
            }
        });
        //使无法拖拽
        onDrag(e->e.setCancelled(true));
        //关闭的时候去掉记录
        onClose(e->{
            if (cache.get(player) != null) {
                cache.get(player).remove(((Player) e.getPlayer()));
            }
        });
    }

    /**
     * 翻页
     * @param i 页码
     */
    public void changePage(int i){
        correspondMap.clear();
        Function[] functions = groupFunctions[i];
        for (int i1 = 0; i1 < functionSlot.size(); i1++) {
            Integer i2 = functionSlot.get(i1);
            if (i1 < functions.length){
                Function function = functions[i1];
                ItemStack itemStack = function.getItemStack(player);
                inventory.setItem(i2, itemStack);
                correspondMap.put(itemStack, function);
                continue;
            }
            inventory.setItem(i2,null);
        }
        this.page = i;
    }
}
