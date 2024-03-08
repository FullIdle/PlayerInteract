package me.fullidle.playerinteract.playerinteract;

import lombok.Getter;
import lombok.SneakyThrows;
import me.fullidle.ficore.ficore.common.api.util.FileUtil;
import me.fullidle.playerinteract.playerinteract.commands.PlayerInteractCmd;
import org.bukkit.Material;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

@Getter
public class Main extends JavaPlugin {
    private static Main main;
    private final Map<String,Function> functions = new HashMap<>();
    private final ArrayList<String> permissionPriority = new ArrayList<>();
    private String[] cmdHelp;
    @Override
    public void onEnable() {
        main = this;
        reloadConfig();
        {
            //指令
            setCommands();
        }
        getLogger().info("§3§lPlugin enabled!");
    }

    @SneakyThrows
    private void setCommands() {
        //获取所有的指令类的完整名去除CMD结尾
        Enumeration<JarEntry> entries = new JarFile(getFile()).entries();
        String mainPack = getClass().getPackage().getName();
        String cmdPackPath = mainPack.replace(".", "/") + "/commands/";
        getLogger().info("§3Setting cmd:↓");
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            String name = entry.getName();
            if (name.startsWith(cmdPackPath) && name.endsWith("Cmd.class") && !name.contains("$")) {
                String cmdName = name.replace(cmdPackPath, "").replace("Cmd.class", "");
                Class<?> aClass = Class.forName(mainPack + ".commands."+cmdName+"Cmd");
                TabExecutor tabE = (TabExecutor) aClass.getConstructor().newInstance();
                PluginCommand command = getCommand(cmdName.toLowerCase());
                command.setExecutor(tabE);
                command.setTabCompleter(tabE);
                if (!aClass.equals(PlayerInteractCmd.class))
                    command.setPermission(getDescription().getName().toLowerCase()+"."+cmdName.toLowerCase());
                getLogger().info("§3  - §6/"+command.getName()+"§r: "+command.getDescription());
            }
        }
        {
            ArrayList<String> list = new ArrayList<>();
            String name = getDescription().getName();
            list.add("§r§l"+ name);
            list.add("");
            list.add(String.format("  §6/%s:", name.toLowerCase()));
            list.add("    §6Args:↓");
            list.add("     §7 [help]                                                      §r-Show Help");//20
            list.add("     §7 [reload]                                  §r-Reload plugin configuration");//40
            list.add("     §7 [open] [player] [optional/otherPlayer] §r-Open the interactive interface");
            for (Map.Entry<String, Map<String, Object>> entry : getDescription().getCommands().entrySet()) {
                if (entry.getKey().equalsIgnoreCase(name)){
                    continue;
                }
                PluginCommand command = getCommand(entry.getKey());
                assert command != null;
                list.add("");
                list.add(String.format("  §6/%s:",command.getName()));
                list.add(String.format("    §6Description: §r%s",command.getDescription()));
                list.add(String.format("    §6Usage: §r%s",command.getUsage()));
                list.add(String.format("    §6Aliases: §r%s",command.getAliases().toArray()));
            }
            cmdHelp = list.toArray(new String[0]);
        }
    }

    @Override
    public void reloadConfig() {
        //载配置
        saveDefaultConfig();
        super.reloadConfig();
        FileUtil instance = FileUtil.getResourceInstance(this,"functions.yml",true,false);
        {
            //权限排优先级//权限以gui名起名
            String pluginName = getDescription().getName().toLowerCase();
            for (String key : getConfig().getKeys(false)) {
                permissionPriority.add(pluginName + "." + key);
            }
            //排序/待删除
            permissionPriority.sort(Comparator.comparingInt(MethodUtil::getPerPriority));
        }
        //缓存所有功能清理
        {
            for (Map.Entry<String, Function> entry : functions.entrySet()) {
                entry.getValue().getShell().resetLoadedClasses();
            }
        }
        functions.clear();
        //重新缓存所有功能
        {
            FileConfiguration config = instance.getConfiguration();
            for (String key : config.getKeys(false)) {
                String script = config.getString(key+".script");
                ItemStack itemStack = new ItemStack(
                        Material.getMaterial(config.getString(key+".item.material")),
                        config.getInt(key+".item.slot")
                );
                ItemMeta itemMeta = itemStack.getItemMeta();
                itemMeta.setDisplayName(config.getString(key+".item.name"));
                itemMeta.setLore(config.getStringList(key+".item.lore"));
                itemStack.setItemMeta(itemMeta);
                functions.put(key,new Function(script,itemStack,config.getBoolean(key+".isAsynchronous")));
            }
        }
        //监听器重注册
        HandlerList.unregisterAll(this);
        getServer().getPluginManager().registerEvents(new PlayerListener(),this);
    }

    public static Main getInstance() {
        return main;
    }
}