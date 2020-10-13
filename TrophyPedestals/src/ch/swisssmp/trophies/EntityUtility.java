package ch.swisssmp.trophies;

import java.util.Optional;
import java.util.UUID;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

public class EntityUtility {

	protected static Optional<UUID> getTrophyPedestalId(Entity entity){
		PersistentDataContainer container = entity.getPersistentDataContainer();
		if(container.isEmpty()) {
			//Bukkit.getLogger().info("Container of "+entity.getType()+"["+entity.getUniqueId()+"]"+" is empty");
			return Optional.empty();
		}
		NamespacedKey key = getTrophyPedestalIdKey();
		String uuidString = container.get(key, PersistentDataType.STRING);
		if(uuidString==null) {
			//Bukkit.getLogger().info("UUID string not found in data container of "+entity.getType());
		}
		try {
			return uuidString!=null ? Optional.of(UUID.fromString(uuidString)) : Optional.empty();
		}
		catch(Exception e) {
			return Optional.empty();
		}
	}
	
	protected static boolean trophyPedestalIdMatches(Entity entity, UUID matchAgainst){
		Optional<UUID> uuidQuery = getTrophyPedestalId(entity);
		return uuidQuery.isPresent() && uuidQuery.get().equals(matchAgainst);
	}

	protected static void setTrophyPedestalId(Entity entity, UUID trophyPedestalId){
		PersistentDataContainer container = entity.getPersistentDataContainer();
		NamespacedKey key = getTrophyPedestalIdKey();
		container.set(key, PersistentDataType.STRING, trophyPedestalId.toString());
		//Bukkit.getLogger().info(entity.getType()+"["+entity.getUniqueId()+"]"+": "+container.get(key, PersistentDataType.STRING));
	}
	
	protected static void releaseDroppedItems(Chunk chunk) {
		for(Entity entity : chunk.getEntities()) {
			if(entity.getType()!=EntityType.DROPPED_ITEM) continue;
			Optional<UUID> pedestalId = EntityUtility.getTrophyPedestalId(entity);
			if(!pedestalId.isPresent()) continue;
			Optional<TrophyPedestal> pedestal = TrophyPedestal.get((Item) entity);
			if(!pedestal.isPresent()) {
				entity.getWorld().dropItem(entity.getLocation(), ((Item)entity).getItemStack());
				entity.remove();
			}
		}
	}
	
	protected static Optional<ArmorStand> getArmorStand(Block block) {
		World world = block.getWorld();
		return world
				.getNearbyEntities(BoundingBox.of(block).expand(BlockFace.DOWN, 0.1))
				.stream()
				.filter(e->e instanceof ArmorStand)
				.map(e->(ArmorStand) e)
				.filter(e->isTrophyPedestalStand(e))
				.findAny();
	}
	
	protected static Optional<Block> getBlock(Item item) {
		Block block = item.getLocation().subtract(0,0.8,0).getBlock();
		return block.getType()==Material.BARRIER ? Optional.of(block) : Optional.empty();
	}
	
	protected static Optional<Block> getBlock(ArmorStand armorStand) {
		if(!isTrophyPedestalStand(armorStand)) return Optional.empty();
		Block block = armorStand.getLocation().add(0,0.2,0).getBlock();
		return block.getType()==Material.BARRIER ? Optional.of(block) : Optional.empty();
	}
	
	protected static Optional<Item> getItem(Block block, UUID trophyPedestalId){
		World world = block.getWorld();
		return world
				.getNearbyEntities(BoundingBox.of(block.getRelative(BlockFace.UP)).expand(BlockFace.DOWN, 0.3))
				.stream()
				.filter(e->e instanceof Item)
				.map(e->(Item) e)
				.filter(e->EntityUtility.trophyPedestalIdMatches(e, trophyPedestalId))
				.findAny();
	}
	
	private static boolean isTrophyPedestalStand(ArmorStand armorStand) {
		return armorStand.isSmall() && 
				armorStand.getEquipment().getChestplate()!=null && 
				EntityUtility.getTrophyPedestalId(armorStand)!=null;
	}
	
	protected static NamespacedKey getTrophyPedestalIdKey() {
		return new NamespacedKey(TrophyPedestalsPlugin.getInstance(), "trophy_pedestal_id");
	}
	
	protected static Item spawnStaticItem(UUID trophyPedestalId, Location location, ItemStack itemStack) {
		Item item = location.getWorld().dropItem(location, itemStack);
		item.setGravity(false);
		item.setInvulnerable(true);
		item.setVelocity(new Vector());
		item.setPickupDelay(Integer.MAX_VALUE);
		setTrophyPedestalId(item, trophyPedestalId);
		return item;
	}
	
	protected static ArmorStand spawnInvisibleArmorStand(Location l){
	    //You can remove the net.minecraft.server.v1_8_R3 and just import the classes
	    //You need to change v1_8_R3 for your version.
	    net.minecraft.server.v1_16_R2.World w = ((org.bukkit.craftbukkit.v1_16_R2.CraftWorld)l.getWorld()).getHandle();
	    net.minecraft.server.v1_16_R2.EntityArmorStand nmsEntity = new net.minecraft.server.v1_16_R2.EntityArmorStand(w, l.getX(),  l.getY(), l.getZ());
	    nmsEntity.setLocation(l.getX(), l.getY(), l.getZ(), l.getYaw(), l.getPitch());
	    nmsEntity.setInvisible(true);
	    nmsEntity.setNoGravity(true);
	    /*You can make other changes like:
	    nmsEntity.setArms(true);
	    nmsEntity.setBasePlate(false);
	    The methods are very similiar to the ArmorStand ones in the API*/
	    w.addEntity(nmsEntity);
	    return (ArmorStand) nmsEntity.getBukkitEntity();
	}
}
