package ch.swisssmp.camerastudio;

import net.minecraft.server.v1_16_R2.PacketPlayOutMapChunk;
import net.minecraft.server.v1_16_R2.PlayerConnection;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_16_R2.CraftChunk;
import org.bukkit.craftbukkit.v1_16_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.*;

public class CameraStudio {

    private static CameraStudio instance;

    private final CameraStudioPlugin plugin;

    private final HashSet<UUID> travelling = new HashSet<UUID>();
    private final HashSet<UUID> aborting = new HashSet<UUID>();

    private CameraStudio(CameraStudioPlugin plugin){
        this.plugin = plugin;
    }

    protected static CameraStudio init(CameraStudioPlugin plugin){
        instance = new CameraStudio(plugin);
        return instance;
    }

    public static CameraStudio inst(){
        return instance;
    }

    public Optional<CameraPath> getPath(UUID pathUid){
        return CameraStudioWorlds.getPath(pathUid);
    }

    public Optional<CameraPathSequence> getSequence(UUID sequenceUid){
        return CameraStudioWorlds.getSequence(sequenceUid);
    }

    public double round(double unrounded, int precision) {
        BigDecimal bd = new BigDecimal(unrounded);
        BigDecimal rounded = bd.setScale(precision, 4);
        return rounded.doubleValue();
    }

    public void travel(final Player player, UUID pathUid, int time, boolean stopViewingAfter){
        CameraPath path = CameraStudioWorlds.getPath(pathUid).orElse(null);
        if(path==null) return;
        travel(player,path,time,stopViewingAfter);
    }

    public void travel(final Player player, CameraPath path, int time, boolean stopViewingAfter){
        travel(player,path.getPoints(),time,stopViewingAfter);
    }

    public void travel(final Player player, CameraPath path, int time, boolean stopViewingAfter, Runnable callback){
        travel(player,path.getPoints(), time, stopViewingAfter, callback);
    }

    public void travel(final Player player, List<Location> locations, int time, boolean stopViewingAfter) {
        travel(player,locations,time, stopViewingAfter,null);
    }

    public void travel(final Player player, List<Location> locations, int time, boolean stopViewingAfter, final Runnable callback) {
        if(!player.isOnline()) return;
        aborting.remove(player.getUniqueId());
        final List<Location> motionPath = createMotionPath(player.getWorld(),locations,time);

        try {
            if(!travelling.contains(player.getUniqueId())) startViewing(player);
            Location start = motionPath.get(0);
            player.teleport(start);
            loadChunkArea(player,start.getBlockX()>>4,start.getBlockZ()>>4,10);
            hidePlayer(player);
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                private int ticks = 0;

                public void run() {
                    boolean abort = aborting.contains(player.getUniqueId());
                    if (!abort && this.ticks < motionPath.size()) {
                        player.teleport(motionPath.get(this.ticks));
                        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, this, 1L);
                        this.ticks += 1;
                    } else {
                        unhidePlayer(player);
                        if(stopViewingAfter || abort) stopViewing(player);
                        if(callback!=null){
                            callback.run();
                        }
                    }
                }
            }, 1L);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startViewing(Player player){
        travelling.add(player.getUniqueId());
        ViewerInfo info = ViewerInfo.of(player);
        info.save();
        player.setGameMode(GameMode.SPECTATOR);
    }

    private void stopViewing(Player player){
        travelling.remove(player.getUniqueId());
        aborting.remove(player.getUniqueId());
        ViewerInfo info = ViewerInfo.load(player).orElse(null);
        if(info==null){
            return;
        }
        info.apply(player, false);
        info.delete();
    }

    public void hidePlayer(Player player){
        for(Player otherPlayer : Bukkit.getOnlinePlayers()){
            otherPlayer.hidePlayer(CameraStudioPlugin.getInstance(), player);
        }
    }

    public void unhidePlayer(Player player){
        for(Player otherPlayer : Bukkit.getOnlinePlayers()){
            otherPlayer.showPlayer(CameraStudioPlugin.getInstance(), player);
        }
    }

    private List<Location> createMotionPath(World world, List<Location> locations, int time){
        List<Double> diffs = new ArrayList<Double>();
        List<Integer> travelTimes = new ArrayList<Integer>();
        List<Location> motionPath = new ArrayList<Location>();

        double totalDiff = 0.0D;

        for (int i = 0; i < locations.size() - 1; i++) {
            Location s = (Location) locations.get(i);
            Location n = (Location) locations.get(i + 1);
            double diff = positionDifference(s, n);
            totalDiff += diff;
            diffs.add(Double.valueOf(diff));
        }

        for (Iterator<Double> n = diffs.iterator(); n.hasNext();) {
            double d = ((Double) n.next()).doubleValue();
            travelTimes.add(Integer.valueOf((int) (d / totalDiff * time)));
        }
        for (int i = 0; i < locations.size() - 1; i++) {
            Location s = (Location) locations.get(i);
            Location n = (Location) locations.get(i + 1);
            int t = ((Integer) travelTimes.get(i)).intValue();

            double moveX = n.getX() - s.getX();
            double moveY = n.getY() - s.getY();
            double moveZ = n.getZ() - s.getZ();
            double movePitch = n.getPitch() - s.getPitch();

            double yawDiff = Math.abs(n.getYaw() - s.getYaw());
            double c = 0.0D;

            if (yawDiff <= 180.0D) {
                if (s.getYaw() < n.getYaw()) {
                    c = yawDiff;
                } else {
                    c = -yawDiff;
                }
            } else if (s.getYaw() < n.getYaw()) {
                c = -(360.0D - yawDiff);
            } else {
                c = 360.0D - yawDiff;
            }

            double d = c / t;

            for (int x = 0; x < t; x++) {
                Location l = new Location(world, s.getX() + moveX / t * x, s.getY() + moveY / t * x,
                        s.getZ() + moveZ / t * x, (float) (s.getYaw() + d * x),
                        (float) (s.getPitch() + movePitch / t * x));
                motionPath.add(l);
            }
        }
        return motionPath;
    }

    /**
     * Emergency eject button to prevent players from being stuck in spectator.
     * Teleports the player to the main world's spawn, sets their GameMode to SURVIVAl, disables flying and enables
     * gravity.
     */
    public static void resetPlayer(Player player){
        player.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
        player.setGameMode(GameMode.SURVIVAL);
        player.setFlying(false);
        player.setGravity(true);
    }

    public void loadChunkArea(Player player, int chunk_x, int chunk_z, int radius){
        World world = player.getWorld();
        chunk_x-=radius;
        chunk_z-=radius;
        PlayerConnection playerConnection = ((CraftPlayer)player).getHandle().playerConnection;
        Chunk chunk;
        for(int x = 0; x < radius*2+1; x++){
            for(int z = 0; z < radius*2+1; z++){
                chunk = world.getChunkAt(chunk_x+x, chunk_z+z);
                if(!chunk.isLoaded())chunk.load();
                playerConnection.sendPacket(new PacketPlayOutMapChunk(((CraftChunk)chunk).getHandle(),65535));
            }
        }
    }

    public double positionDifference(Location cLoc, Location eLoc) {
        double cX = cLoc.getX();
        double cY = cLoc.getY();
        double cZ = cLoc.getZ();

        double eX = eLoc.getX();
        double eY = eLoc.getY();
        double eZ = eLoc.getZ();

        double dX = eX - cX;
        if (dX < 0.0D) {
            dX = -dX;
        }
        double dZ = eZ - cZ;
        if (dZ < 0.0D) {
            dZ = -dZ;
        }
        double dXZ = Math.hypot(dX, dZ);

        double dY = eY - cY;
        if (dY < 0.0D) {
            dY = -dY;
        }
        double dXYZ = Math.hypot(dXZ, dY);

        return dXYZ;
    }

    public boolean isTravelling(UUID PlayerUUID) {
        return travelling.contains(PlayerUUID);
    }

    public void abortAll(){
        for(UUID player : new ArrayList<>(travelling)){
            abort(player);
        }
    }

    public void abort(final UUID playerUUID) {
        // Bukkit.getLogger().info(CameraStudioPlugin.getPrefix()+" Aborting camera motion for "+playerUUID);
        Player player = Bukkit.getPlayer(playerUUID);
        if(player!=null){
            ViewerInfo info = ViewerInfo.load(player).orElse(null);
            if(info!=null){
                info.apply(player);
                info.delete();
            }
        }
        aborting.add(playerUUID);
    }
}
