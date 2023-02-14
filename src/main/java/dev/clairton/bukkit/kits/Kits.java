package dev.clairton.bukkit.kits;

import dev.clairton.bukkit.kits.command.KitCommand;
import dev.clairton.bukkit.kits.listener.CoreListener;
import dev.clairton.bukkit.kits.listener.MenuListener;
import dev.clairton.bukkit.kits.manager.AbilityManager;
import dev.clairton.bukkit.kits.util.EncodingUtil;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Kits extends JavaPlugin {

    @Getter
    private static Kits instance;

    @Getter
    private AbilityManager manager;

    private FileConfiguration config;

    @Override
    public void onEnable() {
        instance = this;

        try {
            config = EncodingUtil.saveDefaultConfig();
        } catch (Exception ex) {
            ex.printStackTrace();
            sendFatalLog( "Um erro ocorreu ao carregar a configuração inicial.");
            return;
        }

        saveDefaultConfig();
        manager = new AbilityManager();

        Bukkit.getPluginManager().registerEvents(new CoreListener(), this);
        Bukkit.getPluginManager().registerEvents(new MenuListener(), this);

        new KitCommand(this);
    }

    @Override
    public void onDisable() {
        if(manager == null) return;

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) manager.removeGamer(onlinePlayer);
    }

    public String Message(String key) {
        return ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages." + key));
    }

    public void log(ChatColor color, String message) {
        Bukkit.getConsoleSender().sendMessage(color + "[Kits] " + message);
    }

    public void sendFatalLog(String message) {
        log(ChatColor.DARK_RED, message);

        Bukkit.shutdown();
    }

    @Override
    public FileConfiguration getConfig() {
        return config;
    }

    @Override
    public void reloadConfig() {
        try {
            config = EncodingUtil.saveDefaultConfig();
            manager.reloadAll();
        } catch (Exception ex) {
            ex.printStackTrace();
            log(ChatColor.DARK_RED, "Um erro ocorreu ao carregar a configuração.");
        }
    }

}
