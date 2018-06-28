package ch.swisssmp.playerstatues;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import ch.swisssmp.utils.ConfigurationSection;
import ch.swisssmp.utils.YamlConfiguration;
import ch.swisssmp.webcore.DataSource;
import net.minecraft.server.v1_12_R1.*;

import java.net.URLEncoder;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_12_R1.CraftServer;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.Location;

public class PlayerStatueOld {
	protected static boolean create(Location location, String playerName, String statueName){
		try {
			YamlConfiguration yamlConfiguration = DataSource.getYamlResponse("statues/create.php", new String[]{
					"player="+URLEncoder.encode(playerName, "utf-8"),
					"name="+URLEncoder.encode(statueName, "utf-8"),
					"x="+location.getX(),
					"y="+location.getY(),
					"z="+location.getZ(),
					"yaw="+location.getYaw(),
					"pitch="+location.getPitch(),
					"world="+URLEncoder.encode(location.getWorld().getName(),"utf-8")
			});
			if(yamlConfiguration==null || !yamlConfiguration.contains("statue")) return false;
			ConfigurationSection dataSection = yamlConfiguration.getConfigurationSection("statue");
			for(Player player : Bukkit.getOnlinePlayers()){
				place(player,dataSection);
			}
			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	protected static boolean update(Location location, String statueName, int range){
		try {
			DataSource.getResponse("statues/update.php", new String[]{
					"name="+URLEncoder.encode(statueName, "utf-8"),
					"range="+range,
					"x="+location.getX(),
					"y="+location.getY(),
					"z="+location.getZ(),
					"yaw="+location.getYaw(),
					"pitch="+location.getPitch(),
					"world="+URLEncoder.encode(location.getWorld().getName(),"utf-8")
			});
			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	protected static boolean remove(Location location, String statueName, int range){
		try {
			DataSource.getResponse("statues/remove.php", new String[]{
					"name="+URLEncoder.encode(statueName, "utf-8"),
					"range="+range,
					"x="+location.getX(),
					"y="+location.getY(),
					"z="+location.getZ(),
					"world="+URLEncoder.encode(location.getWorld().getName(),"utf-8")
			});
			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
    public static void place(Player player, ConfigurationSection dataSection) {
    	String name = dataSection.getString("name");
    	String texture = dataSection.getString("texture");
    	String signature = dataSection.getString("signature");
        Location location = dataSection.getLocation();
        if(location==null){
        	Bukkit.getLogger().info("[PlayerStatues] Could not place figure "+name+" for "+player.getName()+" because the location is null.");
        	return;
        }
        MinecraftServer nmsServer = ((CraftServer) Bukkit.getServer()).getServer();
        WorldServer nmsWorld = ((CraftWorld) player.getWorld()).getHandle();
        GameProfile gameProfile = new GameProfile(UUID.randomUUID(), name);
        if(texture!=null && signature!=null){
            gameProfile.getProperties().put("textures", new Property("textures",texture,signature));
        }

        EntityPlayer npc = new EntityPlayer(nmsServer, nmsWorld, gameProfile, new PlayerInteractManager(nmsWorld));
        Player npcPlayer = npc.getBukkitEntity().getPlayer();
        npcPlayer.setPlayerListName("");
        npc.setLocation(location.getX(), location.getY(), location.getZ(), player.getLocation().getYaw(), player.getLocation().getPitch());

        PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
        connection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, npc));
        connection.sendPacket(new PacketPlayOutNamedEntitySpawn(npc));
        
        Bukkit.getScheduler().runTaskLater(PlayerStatues.plugin, new Runnable(){
        	public void run(){
        		connection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, npc));
        	}
        }, 100L);
    }
}