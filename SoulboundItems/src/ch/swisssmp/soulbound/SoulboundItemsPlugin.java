package ch.swisssmp.soulbound;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

public class SoulboundItemsPlugin extends JavaPlugin {
	
	private static SoulboundItemsPlugin plugin;
	
	@Override
	public void onEnable() {
		plugin = this;
		Bukkit.getPluginManager().registerEvents(new EventListener(), this);

		this.getCommand("soulbind").setExecutor(new SoulboundCommand());

		Bukkit.getLogger().info(getDescription().getName() + " has been enabled (Version: " + getDescription().getVersion() + ")");
	}
    
	@Override
	public void onDisable() {
		PluginDescriptionFile pdfFile = getDescription();
		HandlerList.unregisterAll(this);
		Bukkit.getLogger().info(pdfFile.getName() + " has been disabled (Version: " + pdfFile.getVersion() + ")");
	}
	
	public static String getPrefix(){
		return "["+ChatColor.AQUA+plugin.getName()+ChatColor.RESET+"] ";
	}
	
	public static SoulboundItemsPlugin getInstance(){
		return plugin;
	}
}
