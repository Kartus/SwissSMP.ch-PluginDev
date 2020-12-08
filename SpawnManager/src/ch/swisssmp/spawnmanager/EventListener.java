package ch.swisssmp.spawnmanager;

import java.util.Random;

import ch.swisssmp.utils.JsonUtil;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.Statistic;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Bed;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.potion.PotionEffect;

import ch.swisssmp.utils.SwissSMPler;
import ch.swisssmp.utils.URLEncoder;
import ch.swisssmp.utils.YamlConfiguration;
import ch.swisssmp.webcore.DataSource;
import ch.swisssmp.webcore.HTTPRequest;
import ch.swisssmp.webcore.RequestMethod;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

public class EventListener implements Listener {

	private static final boolean debug = false;
	private Random random = new Random();

	private static void debug(String s){
		debug(s, false);
	}

	private static void debug(String s, boolean force){
		if(!debug && !force) return;
		Bukkit.getLogger().info(SpawnManager.getPrefix()+" "+s);
	}
	
	@EventHandler
	private void onPlayerJoin(PlayerJoinEvent event){
		onReturningPlayerJoin(event);
	}

	private void onReturningPlayerJoin(PlayerJoinEvent event){
		HTTPRequest request = DataSource.getResponse(SpawnManager.getInstance(), "last_world.php", new String[]{
				"player="+event.getPlayer().getUniqueId()
		});
		request.onFinish(()->{
			String worldName = request.getResponse();
			if(worldName==null || worldName.isEmpty()) return;
			World world = Bukkit.getWorld(worldName);
			if(world==null){
				event.getPlayer().teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
				DataSource.getResponse(SpawnManager.getInstance(), "world_change.php", new String[]{
						"player="+event.getPlayer().getUniqueId(),
						"world="+event.getPlayer().getWorld().getName()
				});
			}
		});
	}

	@EventHandler
	private void onPlayerSpawnLocation(PlayerSpawnLocationEvent event){
		if(!event.getPlayer().hasPlayedBefore()){
			onFirstPlayerJoin(event);
		}
	}

	private void onFirstPlayerJoin(PlayerSpawnLocationEvent event){
		HTTPRequest request = DataSource.getResponse(SpawnManager.getInstance(), "first_join.php", new String[]{
				"player="+event.getPlayer().getUniqueId()
		}, RequestMethod.POST_SYNC);

		JsonObject json = request.getJsonResponse();
		if(json==null || !json.has("spawn")) return;
		JsonObject spawnSection = json.get("spawn").getAsJsonObject();
		World world = Bukkit.getWorld(spawnSection.get("world").getAsString());
		if(world==null){
			debug("Spawn world for "+event.getPlayer().getName()+" not found: "+json.toString());
			return;
		}
		Location spawnLocation = JsonUtil.getLocation("spawn", world, json);
		if(spawnLocation==null){
			debug("Spawn location for "+event.getPlayer().getName()+" not found: '"+json.toString());
			return;
		}
		event.setSpawnLocation(spawnLocation);
	}
	
	@EventHandler(priority=EventPriority.HIGHEST,ignoreCancelled=true)
	private void onPlayerInteractBlock(PlayerInteractEvent event){
		if(event.getPlayer().getGameMode()!=GameMode.SURVIVAL || event.getAction()!=Action.RIGHT_CLICK_BLOCK) return;
		Block block = event.getClickedBlock();
		if(!(block.getBlockData() instanceof Bed)) return;
		World world = block.getWorld();
		if(world.getEnvironment() != World.Environment.NORMAL) return;
		long time = world.getTime();
		boolean isDay = time < 12541 && !world.hasStorm();
		Location previousBed = event.getPlayer().getBedSpawnLocation();
		event.getPlayer().setBedSpawnLocation(event.getPlayer().getLocation(), true);
		if(!isDay) return;
		
		event.setCancelled(true);
		Bukkit.getScheduler().runTaskLater(SpawnManager.getInstance(), ()->{
			event.getPlayer().setStatistic(Statistic.TIME_SINCE_REST, 0);
		},10L);
		
		double distanceToPreviousBed = previousBed!=null && block!=null && previousBed.getWorld()==world ? previousBed.distanceSquared(block.getLocation()) : Double.MAX_VALUE;
		if(distanceToPreviousBed>36){
			SwissSMPler.get(event.getPlayer()).sendActionBar(ChatColor.GREEN+"Spawnpunkt gesetzt!");
		}
		else if(isDay){
			SwissSMPler.get(event.getPlayer()).sendActionBar(ChatColor.YELLOW+"Du fühlst dich erholt.");
		}
	}
	
	@EventHandler
	private void onPlayerChangedWorld(PlayerChangedWorldEvent event){
		DataSource.getResponse(SpawnManager.getInstance(), "world_change.php", new String[]{
			"player="+event.getPlayer().getUniqueId(),
			"world="+event.getPlayer().getWorld().getName()
		});
	}
	
	@EventHandler(ignoreCancelled=true)
	private void onPlayerRespawn(PlayerRespawnEvent event){
		Player player = event.getPlayer();
		if(event.isBedSpawn()){
			return;
		}
		HTTPRequest request = DataSource.getResponse(SpawnManager.getInstance(), "spawn.php", new String[]{
				"player="+player.getUniqueId().toString(),
				"x="+(int)Math.round(player.getLocation().getX()),
				"y="+(int)Math.round(player.getLocation().getY()),
				"z="+(int)Math.round(player.getLocation().getZ()),
				"world="+URLEncoder.encode(player.getWorld().getName()),
				"flag=respawn"
		}, RequestMethod.POST_SYNC);
		YamlConfiguration yamlConfiguration = request.getYamlResponse();
		if(yamlConfiguration==null) return;
		if(yamlConfiguration.contains("spawnpoint")){
			Location location = yamlConfiguration.getLocation("spawnpoint");
			if(location!=null){
				event.setRespawnLocation(location);
			}
		}
		if(yamlConfiguration.contains("message")){
			player.sendMessage(yamlConfiguration.getString("message"));
		}
		if(yamlConfiguration.contains("actionbar") || yamlConfiguration.contains("effect") || yamlConfiguration.contains("sound")){
			Bukkit.getScheduler().runTaskLater(SpawnManager.plugin, new Runnable(){
				public void run(){
					if(yamlConfiguration.contains("actionbar")){
						SwissSMPler swisssmpler = SwissSMPler.get(player);
						swisssmpler.sendActionBar(yamlConfiguration.getString("actionbar"));
					}
					if(yamlConfiguration.contains("effect")){
						PotionEffect potionEffect = yamlConfiguration.getPotionEffect("effect");
						if(potionEffect!=null) player.addPotionEffect(potionEffect);
					}
					if(yamlConfiguration.contains("sound")){
						Sound sound = Sound.valueOf(yamlConfiguration.getString("sound"));
						if(sound!=null){
							player.getWorld().playSound(player.getLocation(), sound, 5f, 0.8f+random.nextFloat()*0.4f);
						}
					}
				}
			}, 1l);
		}
	}
}
