package ch.swisssmp.flyday;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import ch.swisssmp.utils.YamlConfiguration;
import ch.swisssmp.webcore.DataSource;
import ch.swisssmp.webcore.HTTPRequest;

public class FlyDay extends JavaPlugin{

	public static FlyDay plugin;
	public static Logger logger;
	public static Server server;
	public static PluginDescriptionFile pdfFile;
	
	private static List<String> permittedWorlds = new ArrayList<String>();
	
	@Override
	public void onEnable() {
		plugin = this;
		pdfFile = getDescription();
		logger = Logger.getLogger("Minecraft");
		logger.info(pdfFile.getName() + " has been enabled (Version: " + pdfFile.getVersion() + ")");
		
		FlyDayCommand flyDayCommand = new FlyDayCommand();
		this.getCommand("FlyDay").setExecutor(flyDayCommand);
		
		Bukkit.getPluginManager().registerEvents(new EventListener(), this);
		if(Bukkit.getPluginManager().isPluginEnabled("Lift")){
			Bukkit.getPluginManager().registerEvents(new LiftHandler(), this);
		}
		updateState();
	}
	
	public static boolean isFlightPermitted(World world){
		return isFlightPermitted(world.getName());
	}
	
	public static boolean isFlightPermitted(String worldName){
		return permittedWorlds.contains(worldName);
	}
	
	public static void updateState(){
		HTTPRequest request = DataSource.getResponse(plugin, "isnow.php");
		request.onFinish(()->{
			updateState(request.getYamlResponse());
		});
	}
	
	private static void updateState(YamlConfiguration yamlConfiguration){
		if(yamlConfiguration==null || !yamlConfiguration.contains("global_flight")){
			Bukkit.getLogger().info("[FlyDay] Kein Flyday");
			permittedWorlds.clear();
		}
		else{
			permittedWorlds = yamlConfiguration.getStringList("global_flight");
			Bukkit.getLogger().info("[FlyDay] Flyday auf "+String.join(", ", permittedWorlds));
		}
		updatePlayers();
	}
	
	public static void updatePlayers(){
		for(Player player : Bukkit.getOnlinePlayers()){
			updatePlayer(player);
		}
	}
	
	public static void updatePlayer(Player player, UpdateFlag... flags){
		if(player==null) return;
		if(player.hasPermission("flyday.bypass")) return;
		boolean flightPermitted = isFlightPermitted(player.getWorld());
		if(flightPermitted==player.getAllowFlight()) return;
		boolean gracePeriod = !Arrays.stream(flags).anyMatch(flag->flag==UpdateFlag.INSTANT||flag==UpdateFlag.QUIET);
		if(!player.isOnGround() && !flightPermitted && gracePeriod){
			player.sendMessage("[§cWarnung§r] §cDeine Flugrechte werden in 30 Sekunden deaktiviert.");
			Bukkit.getScheduler().runTaskLater(FlyDay.plugin, new Runnable(){
				public void run(){
					updatePlayer(player, UpdateFlag.INSTANT);
				}
			}, 30*20L);
		}
		else{
			setFlightPermissions(player, flightPermitted, flags);
		}
	}
	
	private static void setFlightPermissions(Player player, boolean flightPermitted, UpdateFlag... flags){
		player.setAllowFlight(flightPermitted);
		boolean quiet = Arrays.stream(flags).anyMatch(flag->flag==UpdateFlag.QUIET);
		if(quiet){
			if(!flightPermitted) player.setFlying(false);
			return;
		}
		player.setFlying(flightPermitted);
		if(flightPermitted){
			player.sendMessage("[§EFlyDay§r] §aFlug-Rechte aktiviert.");
		}
		else{
			player.sendMessage("[§EFlyDay§r] §cFlug-Rechte deaktiviert.");
		}
	}
	
	protected static Collection<String> getActiveWorlds(){
		return permittedWorlds;
	}
    
	@Override
	public void onDisable() {
		HandlerList.unregisterAll(this);
		PluginDescriptionFile pdfFile = getDescription();
		logger.info(pdfFile.getName() + " has been disabled (Version: " + pdfFile.getVersion() + ")");
	}
	
	public static FlyDay getInstance(){
		return plugin;
	}
}
