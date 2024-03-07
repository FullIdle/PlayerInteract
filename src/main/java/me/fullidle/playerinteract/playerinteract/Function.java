package me.fullidle.playerinteract.playerinteract;

import groovy.lang.GroovyShell;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class Function{
    @Getter
    private final String script;
    @Getter
    private final GroovyShell shell = new GroovyShell();
    private final ItemStack itemStack;
    public final boolean isAsynchronous;

    public Function(String script, ItemStack itemStack, boolean isAsynchronous){
        this.script = script;
        this.itemStack = itemStack;
        this.isAsynchronous = isAsynchronous;
    }

    public void extScript(InventoryClickEvent e,Player player, Player other){
        shell.setProperty("player",player);
        shell.setProperty("other",other);
        shell.setProperty("event",e);
        if (!isAsynchronous){
            shell.evaluate(script);
            return;
        }
        Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(),()-> shell.evaluate(script));
    }

    public ItemStack getItemStack(OfflinePlayer player) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(MethodUtil.colorPapi(itemMeta.getDisplayName(),player));
        itemMeta.setLore(((List<String>) MethodUtil.colorPapi(itemMeta.getLore(), player)));
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }
}
