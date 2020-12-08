package ch.swisssmp.resourcepack;

import java.util.UUID;

import net.minecraft.server.v1_16_R2.Resource;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PlayerCommand implements CommandExecutor{

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(args==null || args.length==0) return false;
		switch(args[0]){
		case "set":{
			if(args.length<3) return false;
			String playerString = args[1];
			Player player = Bukkit.getPlayer(playerString);
			if(player==null){
				player = Bukkit.getPlayer(UUID.fromString(playerString));
			}
			if(player==null){
				sender.sendMessage(ResourcepackManagerPlugin.getPrefix() + " " +playerString+" nicht gefunden.");
				return true;
			}
			String resourcepack = args[2];
			ResourcepackManagerPlugin.setResourcepack(player, resourcepack);
			break;
		}
		case "get":{
			if(args.length<2) return false;
			String playerString = args[1];
			Player player = Bukkit.getPlayer(playerString);
			if(player==null){
				player = Bukkit.getPlayer(UUID.fromString(playerString));
			}
			if(player==null){
				sender.sendMessage(ResourcepackManagerPlugin.getPrefix() + " §cSpieler "+playerString+" nicht gefunden.");
				return true;
			}
			if(!ResourcepackManagerPlugin.playerMap.containsKey(player)){
				sender.sendMessage(ResourcepackManagerPlugin.getPrefix() + " §7"+player.getName()+" hat aktuell kein Server-Resourcepack aktiv.");
				return true;
			}
			else{
				String resourcepack = ResourcepackManagerPlugin.getResourcepack(player);
				sender.sendMessage(ResourcepackManagerPlugin.getPrefix() + " §7"+player.getName()+" hat folgendes Server-Resourcepack aktiv: ");
				sender.sendMessage(resourcepack);
			}
			break;
		}
		case "reload":{
			if(args.length>1){
				String playerString = args[1];
				Player player = Bukkit.getPlayer(playerString);
				if(player==null){
					player = Bukkit.getPlayer(UUID.fromString(playerString));
				}
				if(player==null){
					sender.sendMessage(ResourcepackManagerPlugin.getPrefix() + " §cSpieler "+playerString+" nicht gefunden.");
					return true;
				}
				ResourcepackManagerPlugin.playerMap.remove(player);
				ResourcepackManagerPlugin.updateResourcepack(player);
				sender.sendMessage(ResourcepackManagerPlugin.getPrefix() + " §aResourcepack von "+player.getName()+" aktualisiert.");
			}
			else{
				for(Player player : Bukkit.getOnlinePlayers()){
					ResourcepackManagerPlugin.playerMap.remove(player);
					ResourcepackManagerPlugin.updateResourcepack(player);
				}
				sender.sendMessage(ResourcepackManagerPlugin.getPrefix() + " §aResourcepacks für alle Spieler aktualisiert.");
			}
			break;
		}
		}
		return true;
	}

}
