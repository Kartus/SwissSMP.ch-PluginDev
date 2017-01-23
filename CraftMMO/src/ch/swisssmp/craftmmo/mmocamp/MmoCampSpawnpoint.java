package ch.swisssmp.craftmmo.mmocamp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import ch.swisssmp.craftmmo.Main;
import ch.swisssmp.craftmmo.mmoevent.MmoEvent;
import ch.swisssmp.craftmmo.mmoevent.MmoEventType;

public class MmoCampSpawnpoint {
	public static HashMap<Integer, MmoCampSpawnpoint> spawnpoints;
	
	public final int mmo_camp_spawnpoint_id;
	public final MmoCamp camp;
	public final ArrayList<Location> locations = new ArrayList<Location>();
	private final Random random;
	public ArrayList<MmoCampMob> mobs = new ArrayList<MmoCampMob>();
	
	public MmoCampSpawnpoint(MmoCamp camp, ConfigurationSection dataSection){
		this.mmo_camp_spawnpoint_id = Integer.parseInt(dataSection.getName());
		this.camp = camp;
		ConfigurationSection locationsSection = dataSection.getConfigurationSection("locations");
		World world = camp.world;
		for(String locationName : locationsSection.getKeys(false)){
			ConfigurationSection locationSection = locationsSection.getConfigurationSection(locationName);
			Integer x = locationSection.getInt("x");
			Integer y = locationSection.getInt("y");
			Integer z = locationSection.getInt("z");
			Location location = new Location(world, x, y, z);
			locations.add(location);
		}
		ConfigurationSection mobsSection = dataSection.getConfigurationSection("mobs");
		if(mobsSection!=null){
			for(String mobIndex : mobsSection.getKeys(false)){
				ConfigurationSection mobSection = mobsSection.getConfigurationSection(mobIndex);
				Integer mmo_mob_id = mobSection.getInt("mob_id");
				Integer mmo_camp_mob_id = Integer.parseInt(mobSection.getName());
				Integer count = mobSection.getInt("count");
				this.mobs.add(new MmoCampMob(this, mmo_camp_mob_id, mmo_mob_id, count));
			}
		}
		this.random = new Random();
		spawnpoints.put(this.mmo_camp_spawnpoint_id, this);
	}
	public MmoCampMob getIncomplete(){
		for(MmoCampMob campMob : mobs){
			if(campMob.live_entities.size() < campMob.max_count - campMob.prepared_count){
				return campMob;
			}
		}
		return null;
	}
	public void manageEntityDeath(MmoCampMob campMob, Entity entity, Player player){
		Main.info("Eine Einheit '"+entity.getCustomName()+"' ist gestorben.");
		campMob.live_entities.remove(entity);
		if(camp.getMobCount()==0){
			MmoEvent.fire(camp.events, MmoEventType.CAMP_TRIGGERED, player.getUniqueId());
		}
		if(!camp.isActive() || !camp.isSpawning())
			return;
		switch(camp.deathHandling){
		case "ANNIHILATE":
			if(camp.getMobCount()>0){
				return;
			}
			break;
		case "REGENERATE":
			break;
		default:
			return;
		}
		this.camp.attemptSpawning();
	}
	public void clear(){
		for(MmoCampMob campMob : mobs){
			campMob.despawn();
		}
	}
	public Location getSpawnLocation(){
		int index = random.nextInt(locations.size());
		return locations.get(index);
	}
	public static MmoCampSpawnpoint get(int mmo_camp_spawnpoint_id){
		if(spawnpoints==null){
			return null;
		}
		return spawnpoints.get(mmo_camp_spawnpoint_id);
	}
}
