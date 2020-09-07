package ch.swisssmp.utils;

import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;

public class BlitzableiterCommand implements CommandExecutor {
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 1) {
            sender.sendMessage("Provide on or off as argument");
            return true;
        }
        Configuration config = UtilityCommandsPlugin.getInstance().getConfig();
        ConfigurationSection blitzableiterSection = config.getConfigurationSection("blitzableiter");
        switch (args[0]) {
            case "on": {
                blitzableiterSection.set("activated", true);
                sender.sendMessage(ChatColor.GREEN + "Blitzableiter aktiviert!");
                BlitzableiterListener.reloadConfig();
                return true;
            }
            case "off": {
                blitzableiterSection.set("activated", false);
                sender.sendMessage(ChatColor.RED + "Blitzableiter deaktiviert!");
                BlitzableiterListener.reloadConfig();
                return true;
            }
            default:{
                sender.sendMessage("Provide on or off as argument");
                return true;
            }
        }
    }
}
