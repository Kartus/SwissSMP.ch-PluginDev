package ch.swisssmp.warehouse;

import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_16_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockVector;
import net.minecraft.server.v1_16_R2.BlockPosition;
import net.minecraft.server.v1_16_R2.Blocks;
import net.minecraft.server.v1_16_R2.PacketPlayOutBlockAction;

public class SlaveChestsAnimationRoutine implements Runnable {

	private static SlaveChestsAnimationRoutine instance;
	
	private HashSet<Slave> slaves = new HashSet<Slave>();
	
	@Override
	public void run() {
		for(Slave slave : slaves){
			playChestAnimation(slave);
		}
		slaves.clear();
	}
	
	private void playChestAnimation(Slave slave){
		World world = slave.getCollection().getWorld();
		for(BlockVector chest : slave.getChests()){
			playChestAnimation(world.getBlockAt(chest.getBlockX(), chest.getBlockY(), chest.getBlockZ()));
		}
	}

	private void playChestAnimation(Block chest){
		try{
			BlockPosition position = new BlockPosition(chest.getX(),chest.getY(),chest.getZ());
			net.minecraft.server.v1_16_R2.Block block = getNMSType(chest.getType());
			PacketPlayOutBlockAction openPacket = new PacketPlayOutBlockAction(position, block, (byte)1, (byte)1);
			for (Player player : chest.getWorld().getPlayers()) {
	            ((CraftPlayer)player).getHandle().playerConnection.sendPacket(openPacket);
	            player.playSound(chest.getLocation(), Sound.BLOCK_CHEST_OPEN, 0.8f, 1);
	        }
			Bukkit.getScheduler().runTaskLater(WarehousesPlugin.getInstance(), ()->{
				PacketPlayOutBlockAction closePacket = new PacketPlayOutBlockAction(position, block, (byte)1, (byte)0);
				for (Player player : chest.getWorld().getPlayers()) {
	                ((CraftPlayer)player).getHandle().playerConnection.sendPacket(closePacket);
	            }
			}, 10);
		}
		catch(Exception e){
			System.out.println(e);
		}
	}
	
	private static net.minecraft.server.v1_16_R2.Block getNMSType(Material material){
		switch(material){
		case CHEST: return Blocks.CHEST;
		case TRAPPED_CHEST: return Blocks.TRAPPED_CHEST;
		case BARREL: return Blocks.BARREL;
		default: return null;
		}
	}
	
	protected static void start(){
		SlaveChestsAnimationRoutine result = new SlaveChestsAnimationRoutine();
		instance = result;
		Bukkit.getScheduler().runTaskTimer(WarehousesPlugin.getInstance(), result, 0, 1);
	}

	public static void addSlave(Slave slave){
		instance.slaves.add(slave);
	}
}
