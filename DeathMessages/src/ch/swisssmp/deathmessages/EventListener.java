package ch.swisssmp.deathmessages;

import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.projectiles.BlockProjectileSource;
import org.bukkit.projectiles.ProjectileSource;

import java.util.HashMap;
import java.util.UUID;

public class EventListener implements Listener {
    private FileConfiguration config;
    private HashMap<UUID, Long> cooldowns = new HashMap<>();

    public EventListener(FileConfiguration config) {
        this.config = config;
    }

    @EventHandler
    private void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        UUID playerId = player.getUniqueId();
        String deathMessage = event.getDeathMessage();
        String entity = null;
        String cause = null;
        String block = null;
        String killer = null;
        Boolean cooldownsActive = config.getBoolean("pvpCooldown");
        Long cooldownTime = config.getLong("pvpCooldownTime");
        if (cooldownsActive && (cooldowns.containsKey(playerId) && cooldowns.get(playerId) > System.currentTimeMillis())) {
            event.setDeathMessage("");
            return;
        }

        if (player.getKiller() != null) {
            killer = player.getKiller().getDisplayName();
        }
        EntityDamageEvent lastDamage = player.getLastDamageCause();
        if (lastDamage != null) {
            cause = lastDamage.getCause().toString();
            if (lastDamage instanceof EntityDamageByEntityEvent) {
                EntityDamageByEntityEvent damageByEntity = (EntityDamageByEntityEvent) lastDamage;
                Entity e = damageByEntity.getDamager();
                if (e != null) {
                    if (e instanceof Projectile) {
                        Projectile projectile = (Projectile) e;
                        ProjectileSource projectileSource = projectile.getShooter();
                        if (projectileSource instanceof Entity) {
                            Entity shooter = (Entity) projectileSource;
                            entity = shooter.getType().toString();
                            if (shooter.getCustomName() != null)
                                killer = shooter.getCustomName();
                        } else if (projectileSource instanceof BlockProjectileSource) {
                            BlockProjectileSource blockSource = (BlockProjectileSource) projectileSource;
                            block = blockSource.getBlock().getType().toString();
                        }

                    } else {
                        if (e instanceof Zombie && !((Zombie) e).isAdult()) {
                            entity = "BABY_" + e.getType();
                        } else {
                            entity = e.getType().toString();
                        }
                        if (e.getCustomName() != null)
                            killer = e.getCustomName();
                    }
                }
            } else if (lastDamage instanceof EntityDamageByBlockEvent) {
                EntityDamageByBlockEvent damageByBlock = (EntityDamageByBlockEvent) lastDamage;
                Block b = damageByBlock.getDamager();
                if (b != null)
                    block = b.getType().toString();
            }
        }
        event.setDeathMessage(DeathMessages.GetCustomDeathMessage(player, deathMessage, cause, entity, block, killer));
        if (cooldownsActive && "PLAYER".equals(entity)) {
            cooldowns.put(playerId, System.currentTimeMillis() + cooldownTime);
        }
    }
}
