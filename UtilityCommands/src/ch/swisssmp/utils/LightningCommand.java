package ch.swisssmp.utils;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LightningCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)){
            sender.sendMessage("/lightning kann nur ingame verwendet werden");
            return true;
        }
        if(args.length != 0){
            Bukkit.getLogger().info("Arguments spotted");
            return false;
        }
        Player player = (Player) sender;
        Block target = player.getTargetBlock(null, 100);
        World world = target.getWorld();
        world.strikeLightning(target.getLocation());
        return true;
    }
}
