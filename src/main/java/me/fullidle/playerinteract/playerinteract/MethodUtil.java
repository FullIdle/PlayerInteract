package me.fullidle.playerinteract.playerinteract;

import lombok.Getter;
import lombok.SneakyThrows;
import me.clip.placeholderapi.PlaceholderAPI;
import me.fullidle.ficore.ficore.common.api.ineventory.ListenerInvHolder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.stream.Collectors;

public class MethodUtil {
    public static String getColorMsg(String msg){
        return msg.replace("&","§");
    }

    public static String colorPapi(String msg, OfflinePlayer player){
        return PlaceholderAPI.setPlaceholders(player,getColorMsg(msg));
    }

    public static Collection<String> colorPapi(Collection<String> content, OfflinePlayer player){
        return content.stream().map(s->colorPapi(s,player)).collect(Collectors.toList());
    }

    public static Integer getPerPriority(String permissionNode){
        String[] split = permissionNode.split("\\.");
        return Main.getInstance().getConfig().getInt(split[split.length-1] + ".priority");
    }

    /**
     * 获取从nms版本之后的类
     * @param path 从nms版本开始之后的内容
     */
    @SneakyThrows
    public static Class<?> getNMSClass(String path){
        return Class.forName("minecraft.server."+getNMSVersion()+"."+path);
    }

    public static String getNMSVersion(){
        String name = Bukkit.getServer().getClass().getPackage().getName();
        return name.substring(name.lastIndexOf(".")+1);
    }

    public static ItemStack getPlayerHeadItemStack(){
        ItemStack itemStack = new ItemStack(Material.getMaterial(isHighVersion()?"PLAYER_HEAD":"SKULL_ITEM"));
        if (!isHighVersion())itemStack.setDurability((short) 3);
        return itemStack;
    }

    public static ItemStack getGlassPane(){
        return new ItemStack(Material.getMaterial(isHighVersion()?"WHITE_STAINED_GLASS_PANE":"STAINED_GLASS_PANE"));
    }

    public static int getShotVersion(){
        String version = MethodUtil.getNMSVersion();
        return Integer.parseInt(version.split("_")[1]);
    }

    public static boolean isHighVersion(){
        return getShotVersion() > 12;
    }

    public static ListenerInvHolder newListenerHolder(String title,Integer slot){
        return new ListenerInvHolder() {
            @Getter
            private final Inventory inventory = Bukkit.createInventory(this, slot, title);
        };
    }

    public static void givePlayerInvItem(Player p, Inventory inv){
        Location location = p.getLocation();
        location.setY(location.getY()+0.5);
        for (ItemStack stack : inv.getContents()) {
            if (stack == null||stack.getType().equals(Material.AIR)) continue;
            Item item = p.getWorld().dropItem(location, stack);
            item.setPickupDelay(0);
            item.setGlowing(true);
            item.setGravity(false);
            item.setInvulnerable(true);
        }
    }

    public static boolean isOnlinePlayer(OfflinePlayer player){
        return player!=null&&player.isOnline();
    }

    public static String getUnavailablePlayerMsg(String playerName){
        return String.format("§c§lPlayer:§6§l %s §c§lis not online or does not exist!", playerName);
    }

    public static String getNotPlayerMsg(){
        return "§c§lYOU ARE NOT A PLAYER!";
    }
}
