package ch.swisssmp.resourcepack;

import org.bukkit.Bukkit;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent.Status;

import ch.swisssmp.utils.URLEncoder;
import ch.swisssmp.utils.YamlConfiguration;
import ch.swisssmp.webcore.DataSource;
import ch.swisssmp.webcore.HTTPRequest;

import java.util.ArrayList;
import java.util.List;

public class EventListener implements Listener{

	protected static List<String> defaultResourcepacks = new ArrayList<String>();

	@EventHandler(ignoreCancelled=true)
	private void onPlayerJoin(PlayerJoinEvent event){
		ResourcepackManagerPlugin.updateResourcepack(event.getPlayer(), 5L);
	}
	@EventHandler
	private void onPlayerQuit(PlayerQuitEvent event){
		ResourcepackManagerPlugin.playerMap.remove(event.getPlayer());
	}
	@EventHandler(ignoreCancelled=true)
	private void onPlayerChangedWorld(PlayerChangedWorldEvent event){
		ResourcepackManagerPlugin.updateResourcepack(event.getPlayer(), 20L);
	}
	@EventHandler(ignoreCancelled=true)
	private void onResourcepackChange(PlayerResourcePackStatusEvent event){
		if(event.getStatus()==Status.ACCEPTED){
			event.getPlayer().setInvulnerable(true);
		}
		else if(event.getStatus()==Status.DECLINED){
			event.getPlayer().setInvulnerable(false);
			ResourcepackManagerPlugin.playerMap.remove(event.getPlayer());
			HTTPRequest request = DataSource.getResponse(ResourcepackManagerPlugin.getInstance(), "declined.php", new String[]{
				"player="+event.getPlayer().getUniqueId()	
			});
			request.onFinish(()->{
				YamlConfiguration yamlConfiguration = request.getYamlResponse();
				if(yamlConfiguration.contains("message")){
					event.getPlayer().sendMessage(yamlConfiguration.getString("message"));
				}
			});
		}
		else if(event.getStatus()==Status.SUCCESSFULLY_LOADED || event.getStatus()==Status.FAILED_DOWNLOAD){
			event.getPlayer().setInvulnerable(false);
			if(event.getStatus()==Status.FAILED_DOWNLOAD){
				Bukkit.getLogger().info(ResourcepackManagerPlugin.getPrefix() + " Resourcepack "+URLEncoder.encode(ResourcepackManagerPlugin.playerMap.get(event.getPlayer()))+" konnte bei "+event.getPlayer().getName()+" nicht geladen werden.");
				ResourcepackManagerPlugin.playerMap.remove(event.getPlayer());
			}
		}
	}

	protected static void reloadAdditionalResourcepacks(){
		Configuration config = ResourcepackManagerPlugin.getInstance().getConfig();
		ConfigurationSection resourcepacks = config.getConfigurationSection("resourcepacks");
		for(String resourcepack : resourcepacks.getKeys(false)){
			if(!resourcepacks.isSet(resourcepack) || resourcepacks.getString(resourcepack).equals("")) continue;
			defaultResourcepacks.add(resourcepack);
		}
	}

	@EventHandler
	private void onResourcepackUpdate(PlayerResourcePackUpdateEvent event){
		for(String resourcepack : defaultResourcepacks){
			event.addComponent(resourcepack);
		}
	}
}
