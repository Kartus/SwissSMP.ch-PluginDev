package ch.swisssmp.world;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import ch.swisssmp.utils.ConfigurationSection;
import ch.swisssmp.utils.FileUtil;
import ch.swisssmp.utils.URLEncoder;
import ch.swisssmp.utils.YamlConfiguration;
import ch.swisssmp.webcore.DataSource;
import ch.swisssmp.webcore.FTPConnection;

public class WorldManager extends JavaPlugin{
	protected static Logger logger;
	protected static PluginDescriptionFile pdfFile;
	protected static WorldManager plugin;
	protected static HashMap<String,WorldBorder> worldBorders = new HashMap<String,WorldBorder>();
	
	@Override
	public void onEnable() {
		plugin = this;
		pdfFile = getDescription();
		logger = Logger.getLogger("Minecraft");
		
		logger.info(pdfFile.getName() + " has been enabled (Version: " + pdfFile.getVersion() + ")");
		
		Bukkit.getPluginCommand("worldmanager").setExecutor(new PlayerCommand());
		Bukkit.getPluginCommand("worlds").setExecutor(new WorldsCommand());
		
		Bukkit.getPluginManager().registerEvents(new EventListener(), this);
		if(Bukkit.getPluginManager().getPlugin("WorldGuard")!=null) Bukkit.getPluginManager().registerEvents(new WorldGuardHandler(), this);
		
		WorldManager.loadWorlds();
		WorldBorderChecker worldBorder = new WorldBorderChecker();
		worldBorder.runTaskTimer(this, 0, 100L);
	}

	@Override
	public void onDisable() {
		Bukkit.getScheduler().cancelTasks(this);
		HandlerList.unregisterAll(this);
		PluginDescriptionFile pdfFile = getDescription();
		logger.info(pdfFile.getName() + " has been disabled (Version: " + pdfFile.getVersion() + ")");
	}
	
	protected static void loadWorlds(){
		File worldContainer = Bukkit.getWorldContainer();
		File levelFile;
		for(File file : worldContainer.listFiles()){
			if(!file.isDirectory()) continue;
			levelFile = new File(file.getPath(), "level.dat");
			if(!levelFile.exists()) continue;
			try{
				WorldManager.loadWorld(file.getName());
			}
			catch(Exception e){
				//Skip this World Directory (there is no settings.yml inside to tell WorldManager how to load the World)
			}
		}
	}

	/**
	 * Loads a Minecraft World
	 * @param worldName - The name of the World
	 * @return <code>World</code> if successfully loaded;
	 *         <code>null</code> otherwise
	 * @throws NullPointerException If settings.yml within World Directory is missing
	 */
	public static World loadWorld(String worldName) throws NullPointerException{
		Bukkit.getLogger().info("[WorldManager] Lade Welt "+worldName);
		//Load World Settings
		YamlConfiguration yamlConfiguration = WorldManager.getWorldSettings(worldName);
		if(yamlConfiguration==null){
			throw new NullPointerException("[WorldManager] Kann Welt "+worldName+" nicht laden, fehlende Konfigurationsdatei settings.yml im Welt-Ordner.");
		}
		if(yamlConfiguration.contains("load") && !yamlConfiguration.getBoolean("load")){
			Bukkit.unloadWorld(worldName, true);
			return null;
		}
		return WorldLoader.load(worldName, yamlConfiguration.getConfigurationSection("world"));
	}
	
	/**
	 * Unloads a Minecraft World
	 * @param worldName - The name of the World
	 * @return <code>true</code> if the World was unloaded;
	 *         <code>false</code> otherwise
	 */
	public static boolean unloadWorld(String worldName){
		Bukkit.getLogger().info("[WorldManager] Deaktiviere Welt "+worldName);
		if(Bukkit.unloadWorld(worldName, false)){
			DataSource.getResponse("world/unload.php", new String[]{
					"world="+URLEncoder.encode(worldName)
			});
			return true;
		}
		else return false;
	}
	
	/**
	 * Uploads a World to the FTP Server
	 * @param sender - The responsible Entity for this transaction
	 * @param worldName - The local World Folder to be uploaded
	 * @return A <code>WorldTransferTask</code> reporting the status of the upload to the sender
	 */
	public static WorldTransferObserver uploadWorld(CommandSender sender, String worldName){
		return WorldManager.uploadWorld(sender, worldName, worldName);
	}

	/**
	 * Uploads a World to the FTP Server
	 * @param sender - The responsible Entity for this transaction
	 * @param worldName - The local World Folder to be uploaded
	 * @param overrideWorldName - The name of the World Folder on the remote Server
	 * @return A <code>WorldTransferTask</code> reporting the status of the upload to the sender
	 */
	public static WorldTransferObserver uploadWorld(CommandSender sender, String worldName, String overrideWorldName){
		WorldUpload worldUpload = new WorldUpload(worldName,overrideWorldName);
		WorldTransferObserver result = WorldTransferObserver.run(sender, worldName, worldUpload);
		Thread uploadThread = new Thread(worldUpload);
		uploadThread.start();
		return result;
	}
	
	/**
	 * Downloads a World from the FTP Server
	 * @param sender - The responsible Entity for this transaction
	 * @param worldName - The remote World Folder to be downloaded
	 * @return A <code>WorldTransferTask</code> reporting the status of the upload to the sender
	 */
	public static WorldTransferObserver downloadWorld(CommandSender sender, String worldName){
		return WorldManager.downloadWorld(sender, worldName, worldName);
	}

	/**
	 * Downloads a World from the FTP Server
	 * @param sender - The responsible Entity for this transaction
	 * @param worldName - The remote World Folder to be downloaded
	 * @param overrideWorldName - The name of the World Folder on the local Server
	 * @return A <code>WorldTransferTask</code> reporting the status of the upload to the sender
	 */
	public static WorldTransferObserver downloadWorld(CommandSender sender, String worldName, String overrideWorldName){
		WorldDownload worldDownload = new WorldDownload(worldName,overrideWorldName);
		File packedDirectory = new File(WorldManager.getTempFolder(), overrideWorldName);
		WorldTransferObserver result = WorldTransferObserver.run(sender, overrideWorldName, worldDownload);
		result.addImmediateOnFinishListener(()->{
			WorldManager.unpackWorld(worldName, overrideWorldName, packedDirectory);
		});
		Thread downloadThread = new Thread(worldDownload);
		downloadThread.start();
		return result;
	}
	
	/**
	 * Returns <code>true</code> if a World with the given worldName exists
	 * @param worldName - The worldName to look for
	 * @return <code>true</code> if a World on the local Server exists;
	 *         <code>false</code> otherwise
	 */
	public static boolean localWorldExists(String worldName){
		File worldDirectory = new File(Bukkit.getWorldContainer(), worldName);
		return worldDirectory.exists();
	}

	/**
	 * Returns <code>true</code> if a World with the given worldName exists
	 * @param worldName - The worldName to look for
	 * @return <code>true</code> if a World on the FTP Server exists;
	 *         <code>false</code> otherwise
	 */
	public static boolean remoteWorldExists(String worldName){
		return FTPConnection.fileExists("worlds/"+worldName+".zip");
	}
	
	/**
	 * Gets the WorldBorder associated with a World
	 * @param worldName - The name of the World to look for
	 * @return A <code>WorldBorder</code> if found;
	 *         <code>null</code> otherwise
	 */
	public static WorldBorder getWorldBorder(String worldName){
		return WorldManager.worldBorders.get(worldName);
	}
	
	protected static File getTempFolder(){
		return new File(WorldManager.plugin.getDataFolder(), "temp");
	}
	
	protected static YamlConfiguration getWorldSettings(String worldName){
		File localSettingsFile = new File(Bukkit.getWorldContainer(), worldName+"/settings.yml");
		YamlConfiguration yamlConfiguration = null;
		if(localSettingsFile.exists()){
			yamlConfiguration = YamlConfiguration.loadConfiguration(localSettingsFile);
		}
		if(yamlConfiguration==null || !yamlConfiguration.contains("world")){
			yamlConfiguration = DataSource.getYamlResponse("world/load.php", new String[]{
					"world="+URLEncoder.encode(worldName)
			});
		}
		if(yamlConfiguration==null || !yamlConfiguration.contains("world")) return null;
		return yamlConfiguration;
	}
	
	/**
	 * Creates a settings.yml in the World Directory (This is done for convenience's sake when the World Folder is transferred between servers)
	 * @param world - The World to save the settings for
	 */
	protected static void saveWorldSettings(World world){
		String spawnRadius = world.getGameRuleValue("spawnRadius");
		world.setGameRuleValue("spawnRadius", "0");
		Location spawnLocation = world.getSpawnLocation();
		world.setGameRuleValue("spawnRadius", spawnRadius);
		YamlConfiguration yamlConfiguration = new YamlConfiguration();
		ConfigurationSection dataSection = yamlConfiguration.createSection("world");
		dataSection.set("environment", world.getEnvironment().toString());
		dataSection.set("generate_structures", world.canGenerateStructures());
		dataSection.set("seed", world.getSeed());
		dataSection.set("world_type", world.getWorldType().toString());
		dataSection.set("spawn_x", spawnLocation.getX());
		dataSection.set("spawn_y", spawnLocation.getY());
		dataSection.set("spawn_z", spawnLocation.getZ());
		dataSection.set("time", world.getTime());
		ConfigurationSection gamerulesSection = dataSection.createSection("gamerules");
		for(String gamerule : world.getGameRules()){
			gamerulesSection.set(gamerule, world.getGameRuleValue(gamerule));
		}
		WorldBorder worldBorder = WorldManager.worldBorders.get("world_border");
		if(worldBorder!=null){
			ConfigurationSection worldBorderSection = dataSection.createSection("world_border");
			worldBorderSection.set("center_x", worldBorder.getCenterX());
			worldBorderSection.set("center_z", worldBorder.getCenterZ());
			worldBorderSection.set("radius", worldBorder.getRadius());
			worldBorderSection.set("wrap", worldBorder.doWrap());
			worldBorderSection.set("margin", worldBorder.getMargin());
		}
		yamlConfiguration.save(new File(world.getWorldFolder(),"settings.yml"));
	}
	
	/**
	 * Packs a World Directory into a package containing the World Directory and associated Plugin Files (e.g. WorldGuard region data)
	 * @param worldName - The name of the World to be packed
	 * @param overrideWorldName - The name of the packed World
	 * @param packedDirectory - The Directory to pack into
	 */
	protected static void packWorld(String worldName, String overrideWorldName, File packedDirectory){
		File worldDirectory = new File(Bukkit.getWorldContainer(),overrideWorldName);
		File packedWorldDirectory = new File(packedDirectory,"World/"+worldName);
		packedWorldDirectory.mkdirs();
		FileUtil.copyDirectory(worldDirectory, packedWorldDirectory, new ArrayList<String>(Arrays.asList("session.lock")));
		Bukkit.getPluginManager().callEvent(new WorldPackEvent(worldName,packedDirectory));
	}
	
	/**
	 * Delete a World Directory and all of its associated Plugin Files (e.g. WorldGuard region data)
	 * @param worldName - The name of the World to be deleted
	 */
	public static void deleteWorld(String worldName){
		World loaded = Bukkit.getWorld(worldName);
		if(loaded!=null){
			
			if(!Bukkit.unloadWorld(loaded, true)){
				Bukkit.getLogger().info("[WorldManager] Konnte Welt "+worldName+" nicht deaktivieren und löschen.");
				return;
			}
			Bukkit.getLogger().info("[WorldManager] Welt "+worldName+" deaktiviert.");
			Bukkit.getScheduler().runTaskLater(WorldManager.plugin, ()->{
				WorldManager.deleteWorld(worldName);
			}, 100L);
			return;
		}
		File worldDirectory = new File(Bukkit.getWorldContainer(),worldName);
		FileUtil.deleteRecursive(worldDirectory);
		Bukkit.getLogger().info("[WorldManager] Lösche Ordner "+worldDirectory.getPath());
		//continue deleting files until this stuff is gone
		if(worldDirectory.exists()){
			Bukkit.getLogger().info("[WorldManager] Ordner "+worldDirectory.getPath()+" konnte nicht vollständig gelöscht werden.");
			Bukkit.getLogger().info("[WorldManager] Löschung der Welt "+worldName+" wird im nächsten Tick fortgesetzt.");
			Bukkit.getScheduler().runTaskLater(WorldManager.plugin, ()->{
				WorldManager.deleteWorld(worldName);
			}, 1l);
			return;
		}
		Bukkit.getPluginManager().callEvent(new WorldDeleteEvent(worldName));
		Bukkit.getLogger().info("[WorldManager] Welt "+worldName+" gelöscht.");
	}
	
	/**
	 * Unpacks a World package containing the World Directory and associated Plugin Files (e.g. WorldGuard region data)
	 * @param worldName - The name of the World to be unpacked
	 * @param overrideWorldName - The name of the World after unpacking
	 * @param packedDirectory - The Directory to unpack from
	 */
	private static void unpackWorld(String worldName, String overrideWorldName, File packedDirectory){
		File packedWorldDirectory = new File(packedDirectory,"World/"+worldName);
		File worldDirectory = new File(Bukkit.getWorldContainer(),overrideWorldName);
		FileUtil.copyDirectory(packedWorldDirectory, worldDirectory, new ArrayList<String>(Arrays.asList("session.lock")));
		try{
			Bukkit.getPluginManager().callEvent(new WorldUnpackEvent(overrideWorldName,packedDirectory));
		}
		catch(Exception e){
			e.printStackTrace();
			return;
		}
		Bukkit.getLogger().info("[WorldManager] Unpacking of World "+worldName+" finished.");
		Bukkit.getScheduler().runTaskLater(WorldManager.plugin, ()->{
			FileUtil.deleteRecursive(packedDirectory);
		}, 5L);
	}
}
