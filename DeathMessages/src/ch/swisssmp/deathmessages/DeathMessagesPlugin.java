package ch.swisssmp.deathmessages;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

public class DeathMessagesPlugin extends JavaPlugin {
    protected static PluginDescriptionFile pdfFile;
    protected static DeathMessagesPlugin plugin;

    @Override
    public void onEnable() {
        plugin = this;
        saveDefaultConfig();
        FileConfiguration config = getConfig();
        pdfFile = getDescription();
        Bukkit.getPluginManager().registerEvents(new EventListener(config), this);
        DeathMessages.reload();
        Bukkit.getLogger().info(pdfFile.getName() + " has been enabled (Version: " + pdfFile.getVersion() + ")");
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
        Bukkit.getScheduler().cancelTasks(this);
        PluginDescriptionFile pdfFile = getDescription();
        Bukkit.getLogger().info(pdfFile.getName() + " has been disabled (Version: " + pdfFile.getVersion() + ")");
    }

    public static DeathMessagesPlugin getInstance() {
        return plugin;
    }
}
