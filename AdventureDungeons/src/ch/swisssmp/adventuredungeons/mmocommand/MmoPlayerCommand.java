package ch.swisssmp.adventuredungeons.mmocommand;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import ch.swisssmp.adventuredungeons.Main;
import ch.swisssmp.adventuredungeons.mmoplayer.MmoRequest;
import ch.swisssmp.adventuredungeons.mmoworld.MmoDungeon;
import ch.swisssmp.adventuredungeons.mmoworld.MmoDungeonInstance;
import net.md_5.bungee.api.ChatColor;

public class MmoPlayerCommand implements CommandExecutor{

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		switch(label){
		case "join":{
			if(!(sender instanceof Player)){
				Main.debug("Nur ingame verwendbar.");
				return true;
			}
			if(args==null) {
				sender.sendMessage("Keinen Spieler definiert.");
				return false;
			}
			if(args.length<1) {
				sender.sendMessage("Keinen Spieler definiert.");
				return false;
			}
			String otherPlayerName = args[0];
			Player otherPlayer = Bukkit.getPlayer(otherPlayerName);
			if(otherPlayer==null){
				sender.sendMessage(ChatColor.RED+otherPlayerName+" nicht gefunden.");
				return true;
			}
			MmoDungeonInstance targetInstance = MmoDungeon.getInstance(otherPlayer);
			if(targetInstance==null) {
				sender.sendMessage(otherPlayerName+" ist momentan nicht in einem Dungeon.");
				return true;
			}
			if(targetInstance.running){
				sender.sendMessage(ChatColor.RED+"Diese Instanz wurde bereits gestartet und kann nicht mehr betreten werden.");
				break;
			}
			Player player = (Player) sender;
			if(targetInstance.player_uuids.contains(player.getUniqueId())){
				sender.sendMessage(ChatColor.YELLOW+"Du bist bereits in dieser Instanz.");
				break;
			}
			if(!targetInstance.isInvitedPlayer(player.getUniqueId())){
				sender.sendMessage(ChatColor.RED+"Du bist nicht in diese Instanz eingeladen worden.");
				sender.sendMessage(ChatColor.YELLOW+"Ein Mitglied dieser Instanz kann dich mit '/invite "+player.getName()+"' einladen.");
				break;
			}
			MmoDungeon mmoDungeon = MmoDungeon.get(targetInstance.mmo_dungeon_id);
			int maxDistance = 50;
			Location entryLocation = mmoDungeon.lobby_leave;
			if(player.getLocation().getWorld()!=entryLocation.getWorld()){
				player.sendMessage(ChatColor.RED+"Du bist nicht in der N�he des Dungeon-Eingangs.");
				break;
			}
			if(player.getLocation().distance(entryLocation)>maxDistance){
				player.sendMessage(ChatColor.RED+"Du bist nicht in der N�he des Dungeon-Eingangs.");
				break;
			}
			mmoDungeon.join(player.getUniqueId(), targetInstance);
			break;
		}
		case "refuse":{
			if(!(sender instanceof Player)) return true;
			if(args==null) return true;
			if(args.length<1) return true;
			Player player = (Player) sender;
			String otherPlayerName = args[0];
			Player otherPlayer = Bukkit.getPlayer(otherPlayerName);
			if(otherPlayer==null) return true;
			otherPlayer.sendMessage(player.getDisplayName()+ChatColor.RED+" hat deine Anfrage abgelehnt.");
			break;
		}
		case "inv":
		case "invite":{
			if(!(sender instanceof Player)) return true;
			if(args==null) return false;
			if(args.length<1) return false;
			Player player = (Player) sender;
			MmoDungeon playerDungeon = MmoDungeon.get(player);
			MmoDungeonInstance dungeonInstance = MmoDungeon.getInstance(player);
			if(playerDungeon==null || dungeonInstance==null){
				return true;
			}
			if(dungeonInstance.running){
				sender.sendMessage(ChatColor.RED+"Die Instanz wurde bereits gestartet und es k�nnen keine neuen Spieler eingeladen werden.");
				break;
			}
			String otherPlayerName = args[0];
			Player otherPlayer = Bukkit.getPlayer(otherPlayerName);
			if(otherPlayer==null){
				sender.sendMessage(ChatColor.RED+otherPlayerName+" nicht gefunden.");
				return true;
			}
			int maxDistance = 50;
			Location entryLocation = playerDungeon.lobby_leave;
			if(otherPlayer.getLocation().getWorld()!=entryLocation.getWorld()){
				player.sendMessage(ChatColor.RED+otherPlayer.getDisplayName()+ChatColor.RED+" ist nicht in "+entryLocation.getWorld().getName()+".");
				break;
			}
			if(otherPlayer.getLocation().distance(entryLocation)>maxDistance){
				player.sendMessage(ChatColor.RED+otherPlayer.getDisplayName()+ChatColor.RED+" ist zu weit weg.");
				break;
			}
			dungeonInstance.addInvitedPlayer(otherPlayer.getUniqueId());
		    MmoRequest mmoRequest = new MmoRequest(ChatColor.YELLOW+"M�chtest du der Gruppe von "+player.getDisplayName()+ChatColor.YELLOW+" beitreten?");
		    mmoRequest.addOption("Beitreten", "join "+player.getName());
		    mmoRequest.addOption("Ablehnen", "refuse "+player.getName());
		    mmoRequest.send(otherPlayer.getUniqueId());
		    dungeonInstance.addInvitedPlayer(otherPlayer.getUniqueId());
			break;
		}
		case "choose":{
			if(!(sender instanceof Player)) return true;
			if(args==null) return true;
			if(args.length<2) return true;
			Player player = (Player) sender;
			String requestIDString = args[0];
			MmoRequest request = MmoRequest.get(requestIDString);
			if(request==null) return true;
			String key = args[1];
			request.choose(player, key);
			break;
		}
		default:{
			break;
		}
		}
		return true;
	}

}
