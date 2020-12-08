package ch.swisssmp.resourcepack;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import ch.swisssmp.utils.URLEncoder;
import ch.swisssmp.webcore.DataSource;
import ch.swisssmp.webcore.HTTPRequest;

public class ResourcepackManagerPlugin extends JavaPlugin{
	private static PluginDescriptionFile pdfFile;
	private static ResourcepackManagerPlugin plugin;
	
	protected static HashMap<Player,String> playerMap = new HashMap<Player,String>();
	
	@Override
	public void onEnable() {
		plugin = this;
		pdfFile = getDescription();

		getConfig().options().copyDefaults();
		saveDefaultConfig();
		EventListener.reloadAdditionalResourcepacks();
		
		PlayerCommand playerCommand = new PlayerCommand();
		this.getCommand("resourcepack").setExecutor(playerCommand);
		
		Bukkit.getPluginManager().registerEvents(new EventListener(), this);
		
		Bukkit.getLogger().info(pdfFile.getName() + " has been enabled (Version: " + pdfFile.getVersion() + ")");
	}
	
	public static void setResourcepack(Player player, String resourcepack){
		if(player==null) return;
		if(playerMap.containsKey(player) && playerMap.get(player).equals(resourcepack))
			return;
		playerMap.remove(player);
		if(resourcepack==null) return;
		HTTPRequest request = DataSource.getResponse(ResourcepackManagerPlugin.getInstance(), "get_url.php", new String[]{
				"resourcepack="+URLEncoder.encode(resourcepack)
		});
		request.onFinish(()->{
			String url = request.getResponse();
			if(url==null || url.isEmpty()) return;
			playerMap.put(player, resourcepack);
			player.setResourcePack(url);
		});
	}
	
	public static String getResourcepack(Player player){
		return playerMap.get(player);
	}
	
	public static void updateResourcepack(Player player, long delay){
		Bukkit.getScheduler().runTaskLater(ResourcepackManagerPlugin.plugin, new Runnable(){
			public void run(){
				updateResourcepack(player);
			}
		}, delay);
	}
	
	public static void updateResourcepack(Player player){
		PlayerResourcePackUpdateEvent event = new PlayerResourcePackUpdateEvent(player);
		Bukkit.getPluginManager().callEvent(event);
		String resourcepack = String.join("+", event.getComponents());
		ResourcepackManagerPlugin.setResourcepack(player, resourcepack);
		
	}

	@Override
	public void onDisable() {
		HandlerList.unregisterAll(this);
		PluginDescriptionFile pdfFile = getDescription();
		Bukkit.getLogger().info(pdfFile.getName() + " has been disabled (Version: " + pdfFile.getVersion() + ")");
	}
	
	public static ResourcepackManagerPlugin getInstance(){
		return plugin;
	}

	public static String getPrefix(){
		return "[§1ResourcepackManager§r]";
	}
}
