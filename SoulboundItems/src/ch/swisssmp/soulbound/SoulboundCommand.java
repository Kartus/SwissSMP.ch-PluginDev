package ch.swisssmp.soulbound;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class SoulboundCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)) {
            sender.sendMessage(SoulboundItemsPlugin.getPrefix() + "Soulbinding kann nur ingame verwendet werden.");
            return true;
        }
        if(args.length != 1){
            sender.sendMessage(SoulboundItemsPlugin.getPrefix() + "Gib die UUID des Spielers an den das Item gebunden werden soll ein.");
            return false;
        }

        UUID uuid = null;
        try {
            uuid = UUID.fromString(args[0]);
        } catch (IllegalArgumentException e){
            sender.sendMessage(SoulboundItemsPlugin.getPrefix() + "Konnte Spieler mit UUID " + args[0] + " nicht finden.");
            return true;
        }

        if(uuid.equals(null)) {
            sender.sendMessage(SoulboundItemsPlugin.getPrefix() + "Konnte Spieler mit UUID " + args[0] + " nicht finden.");
            return true;
        }
        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
        if(player == null) {
            sender.sendMessage(SoulboundItemsPlugin.getPrefix() + "Konnte Spieler nicht finden.");
            return true;
        }
        Player admin = (Player) sender;
        ItemStack itemStack = admin.getInventory().getItemInMainHand();
        if(itemStack == null || itemStack.getType() == Material.AIR) {
            sender.sendMessage(SoulboundItemsPlugin.getPrefix() + "Ungültiges Item.");
            return true;
        }
        Soulbinder.bind(itemStack, uuid, player.getName());
        return true;
    }
}
