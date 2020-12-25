package ch.swisssmp.nightskipper;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.world.TimeSkipEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.stream.Collectors;

public class SleepChecker extends BukkitRunnable {

    private final World world;
    private final long delay;
    private final float percentage;

    private long t;

    private SleepChecker(World world, long delay, float percentage){
        this.world = world;
        this.delay = delay;
        this.percentage = percentage;
    }

    @Override
    public void run() {
        t++;
        if(t>=delay){
            complete();
        }
    }

    public void complete(){
        finish();
        List<Player> players = world.getPlayers().stream()
                .filter(p->
                        (!p.isSleepingIgnored() || p.isSleeping()) &&
                        (!p.hasPermission("nightskipper.ignore")) &&
                        (p.getGameMode()== GameMode.SURVIVAL || p.getGameMode()== GameMode.ADVENTURE))
                .collect(Collectors.toList());
        int playerCount = players.size();
        int sleepingCount = (int) players.stream().filter(p->p.isSleeping()).count();
        if(playerCount==0 || sleepingCount/(float) playerCount < percentage){
            if(sleepingCount!=0) {
                for (Player player : players) {
                    player.sendMessage(ChatColor.GOLD + "[NightSkipper] "
                            + ChatColor.DARK_RED + sleepingCount + "/" + playerCount + ChatColor.DARK_GRAY + " Spieler im Bett");
                }
            }
            return;
        }

        long skipAmount = 24000-world.getTime();
        TimeSkipEvent event = new TimeSkipEvent(world, TimeSkipEvent.SkipReason.NIGHT_SKIP, skipAmount);
        Bukkit.getPluginManager().callEvent(event);
        if(event.isCancelled()){
            return;
        }
        world.setFullTime(world.getFullTime()+skipAmount);
    }

    @Override
    public void cancel(){
        finish();
    }

    private void finish(){
        super.cancel();
    }

    protected static SleepChecker start(World world, long delay, float percentage){
        SleepChecker result = new SleepChecker(world, delay, percentage);
        result.runTaskTimer(NightSkipperPlugin.getInstance(), 0, 1);
        return result;
    }
}
