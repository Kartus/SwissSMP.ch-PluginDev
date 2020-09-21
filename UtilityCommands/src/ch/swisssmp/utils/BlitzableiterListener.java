package ch.swisssmp.utils;

import org.bukkit.Bukkit;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockIgniteEvent;

public class BlitzableiterListener implements Listener {

    static boolean cancelLightnings;

    @EventHandler
    private void onLightningIgnition(BlockIgniteEvent event){
//        if(event.getIgnitingBlock() != null) {
//            Bukkit.getLogger().info("igniting block: " + event.getIgnitingBlock().toString());
//            Bukkit.getLogger().info(("cancel lightnings: " + cancelLightnings));
//            return;
//        }
//        if(event.getIgnitingEntity() != null) {
//            Bukkit.getLogger().info("there is an  igniting entity: " + event.getIgnitingEntity().toString());
//            Bukkit.getLogger().info(("cancel lightnings: " + cancelLightnings));
//            return;
//        }
        if(event.getCause() != BlockIgniteEvent.IgniteCause.LIGHTNING){
//            Bukkit.getLogger().info("cause != lightning");
//            Bukkit.getLogger().info(("cancel lightnings: " + cancelLightnings));
            return;
        }
        if(cancelLightnings) {
//            Bukkit.getLogger().info("Cacnelled lightning");
//            Bukkit.getLogger().info(("cancel lightnings: " + cancelLightnings));
            event.setCancelled(true);
        }
    }

    protected static void reloadConfig(){
        Configuration config = UtilityCommandsPlugin.getInstance().getConfig();
        ConfigurationSection blitzableiterSection = config.getConfigurationSection("blitzableiter");
        cancelLightnings = blitzableiterSection.getBoolean("activated");
        UtilityCommandsPlugin.getInstance().saveConfig();
    }
}
