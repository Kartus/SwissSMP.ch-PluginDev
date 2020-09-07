package ch.swisssmp.utils;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockIgniteEvent;

public class BlitzableiterListener implements Listener {

    static boolean cancelLightnings;

    @EventHandler
    private void onLightningIgnition(BlockIgniteEvent event){
        if(event.getIgnitingBlock() != null) return;
        if(event.getIgnitingEntity() != null) return;
        if(event.getCause() != BlockIgniteEvent.IgniteCause.LIGHTNING){
            return;
        }
        if(cancelLightnings) {
            event.setCancelled(true);
        }
    }

    protected static void reloadConfig(){
        Configuration config = UtilityCommandsPlugin.getInstance().getConfig();
        ConfigurationSection blitzableiterSection = config.getConfigurationSection("blitzableiter");
        cancelLightnings = blitzableiterSection.getBoolean("activated");
    }
}
