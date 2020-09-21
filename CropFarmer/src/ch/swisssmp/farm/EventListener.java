package ch.swisssmp.farm;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class EventListener implements Listener {
	
	private boolean active = true;
	
	@EventHandler
	private void onPlayerWorkSoil(PlayerInteractEvent event){
		if(!active) return;
		if(event.getAction()!=Action.RIGHT_CLICK_BLOCK) return;
		if(event.getItem()==null) return;
		if(event.getItem().getType()!=Material.GOLDEN_HOE && event.getItem().getType()!=Material.IRON_HOE) return;
		if(!SoilWorker.isSoil(event.getClickedBlock().getType()) || event.getClickedBlock().getRelative(BlockFace.UP).getType()!=Material.AIR) return;
		if(!event.getPlayer().hasPermission("cropfarm.use")) return;
		int radius = event.getItem().getType()==Material.GOLDEN_HOE ? 2 : 1;
		active = false; //<--this prevents the plugin from listening to its own BlockPlaceEvents
		SoilWorker.workGround(event.getPlayer(), event.getHand(), event.getClickedBlock(), radius);
		active = true;
	}

//	@EventHandler
//	private void BlockeruPlacedesu(BlockPlaceEvent event){
//		Bukkit.getLogger().info("Blockeru placedesu!");
//	}
	
	@EventHandler
	private void onBlockPlace(BlockPlaceEvent event){
//		Bukkit.getLogger().info("BlockPlaceEvent (cropfarmer)");
		if(!active) {
//			Bukkit.getLogger().info("!active");
			return;
		}
		if(!event.getPlayer().hasPermission("cropfarm.use")) return;
		if(!SeedPlacer.isCrop(event.getBlock().getType())) {
//			Bukkit.getLogger().info("Not a crop");
			return;
		}
		active = false; //<--this prevents the plugin from listening to its own BlockPlaceEvents
		SeedPlacer.placeSeeds(event.getPlayer(), event.getHand(), event.getBlock(), 1);
		active = true;
	}
}
