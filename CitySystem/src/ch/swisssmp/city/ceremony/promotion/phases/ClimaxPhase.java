package ch.swisssmp.city.ceremony.promotion.phases;

import ch.swisssmp.ceremonies.Phase;
import ch.swisssmp.ceremonies.effects.FireBurstEffect;
import ch.swisssmp.city.CitySystemPlugin;
import ch.swisssmp.city.ceremony.promotion.CityPromotionCeremony;
import ch.swisssmp.city.ceremony.promotion.CityPromotionCeremonyMusic;
import ch.swisssmp.city.ceremony.promotion.PromotionCeremonyData;
import ch.swisssmp.utils.Random;
import net.minecraft.server.v1_16_R2.BlockPosition;
import net.minecraft.server.v1_16_R2.Blocks;
import net.minecraft.server.v1_16_R2.PacketPlayOutBlockAction;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.craftbukkit.v1_16_R2.entity.CraftPlayer;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

public class ClimaxPhase extends Phase {

    private final CityPromotionCeremony ceremony;
    private final Block chest;
    private final Random random;

    private BukkitTask musicTask;

    private ItemStack[] tribute;
    private long time;

    public ClimaxPhase(CityPromotionCeremony ceremony){
        this.ceremony = ceremony;
        this.chest = ceremony.getChest();
        this.random = ceremony.random;
    }

    @Override
    public void begin(){
        super.begin();
        tribute = ((Chest) chest.getState()).getBlockInventory().getContents();
        time = 0;
        startMusic();
        openChest(chest.getLocation());
    }

    private void startMusic(){
        for(Player player : ceremony.getPlayers()){
            player.stopSound(CityPromotionCeremonyMusic.shaker, SoundCategory.RECORDS);
            player.stopSound(CityPromotionCeremonyMusic.drums, SoundCategory.RECORDS);
        }
        musicTask = Bukkit.getScheduler().runTaskLater(CitySystemPlugin.getInstance(), () ->{
            ceremony.setMusic(chest.getLocation(), CityPromotionCeremonyMusic.finale, 932);
        }, 0L);
    }

    @Override
    public void run() {
        time++;
        if(!isEmpty(tribute)){
            int i = random.nextInt(tribute.length);
            while(tribute[i] == null || tribute[i].getType() == Material.AIR){
                i = random.nextInt(tribute.length);
            }
            Item droppedItem = chest.getWorld().dropItemNaturally(chest.getLocation().add(0,0.5,0), new ItemStack(tribute[i].getType()));

            droppedItem.setPickupDelay(Integer.MAX_VALUE);
            droppedItem.setGravity(false);
            double randomDouble = random.nextDouble();
            double x = random.nextInt(21)*0.01;
            double y = random.nextInt(21)*0.01;
            double z = random.nextInt(21)*0.01;
            if(randomDouble < 0.25) {
                x = -1 * x;
            }
            if(randomDouble < 0.5 && randomDouble >= 0.25) {
                z = -1 * z;
            }
            if(randomDouble < 0.75 && randomDouble >= 0.5) {
                x = -1 * x; z = -1 * z;}
            droppedItem.setVelocity(new Vector(x, y, z));

            BukkitRunnable runnable = new BukkitRunnable() {
                @Override
                public void run() {
                    if(!droppedItem.isValid()){
                        this.cancel();
                        return;
                    }
                    droppedItem.setVelocity(droppedItem.getVelocity().add(new Vector(0, 0.2, 0)));
                }
            };
            runnable.runTaskTimer(CitySystemPlugin.getInstance(), 10L, 5L);

            Bukkit.getScheduler().runTaskLater(CitySystemPlugin.getInstance(), droppedItem::remove, 60L);

            tribute[i].setAmount(tribute[i].getAmount() - 1);

            if(randomDouble < 0.5){
                Color primaryColor;
                Color secondaryColor;
                int randomInt = random.nextInt(3);
                switch(randomInt){
                    case 0: {
                        primaryColor = Color.fromRGB(192, 192, 192);
                        secondaryColor = Color.fromRGB(255,215,0);
                        break;
                    }
                    case 1: {
                        primaryColor = Color.fromRGB(255, 215, 0);
                        secondaryColor = Color.fromRGB(192, 192, 192);
                        break;
                    }
                    case 2: {
                        primaryColor = Color.fromRGB(185, 242, 255);
                        secondaryColor = Color.fromRGB(255, 215, 0);
                        break;
                    }
                    default: {
                        primaryColor = Color.fromRGB(255,115,0);
                        secondaryColor = Color.fromRGB(255, 10, 10);
                    }
                }
                if(time % 5 == 0) {
                    playFireBurst(chest.getLocation(), primaryColor, secondaryColor);
                }
            }
        } else {
            this.setCompleted();
        }
    }

    @Override
    public void finish(){
        super.finish();
        spawnExplosions(chest.getLocation(), ceremony.getCeremonyParameters());
        chest.setType(Material.AIR);
        if(musicTask != null || !musicTask.isCancelled()) musicTask.cancel();
    }

    private boolean isEmpty(ItemStack[] content){
        for(int i = 0; i < content.length; i++){
            if(content[i] == null || content[i].getType() == Material.AIR) continue;
            if(content[i].getAmount() > 0) return false;
        }
        return true;
    }

    /**
     * Uses suboptimal imports in order to open make the chest appear as opening when an item is thrown out.
     */
    private void openChest(Location loc){
        BlockPosition pos = new BlockPosition(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        PacketPlayOutBlockAction packet = new PacketPlayOutBlockAction(pos, Blocks.CHEST, (byte) 1, (byte) 1);
        for(Player player : Bukkit.getOnlinePlayers()) {
            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
        }
    }

    private void playFireBurst(Location location, Color primaryColor, Color secondaryColor){
        Location effectLocation = location.add(0,0.5,0);
        FireBurstEffect.play(CitySystemPlugin.getInstance(), effectLocation.getBlock(), 5, primaryColor, secondaryColor);
    }

    private void spawnExplosions(Location location, PromotionCeremonyData data){
        int cycles = data.getClimaxExplosionCycles();
        BukkitTask explosions = Bukkit.getScheduler().runTaskTimer(CitySystemPlugin.getInstance(), () ->{
            double x = random.nextDouble();
            double y = random.nextDouble();
            double z = random.nextDouble();
            Location loc = location.add(x - 0.5, y - 0.5, z - 0.5);
            loc.getWorld().strikeLightning(loc);
            loc.getWorld().createExplosion(loc, 1, false);
        }, 5L, 5L);
        Bukkit.getScheduler().runTaskLater(CitySystemPlugin.getInstance(), explosions::cancel, (5*cycles) + 1);
    }
}
