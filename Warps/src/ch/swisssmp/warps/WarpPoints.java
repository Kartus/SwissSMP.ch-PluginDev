package ch.swisssmp.warps;

import ch.swisssmp.utils.JsonUtil;
import ch.swisssmp.world.WorldManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class WarpPoints {

    private final static Set<WarpPoint> warpPoints = new HashSet<>();

    public static void addWarp(WarpPoint warp){
        warpPoints.add(warp);
    }

    public static Optional<WarpPoint> getWarp(String name){
        for(WarpPoint warp : warpPoints){
            if(!warp.getName().equalsIgnoreCase(name)) continue;
            return Optional.of(warp);
        }
        return Optional.empty();
    }

    public static Optional<WarpPoint> getWarp(String name, World world){
        for(WarpPoint warp : warpPoints){
            if(!warp.getName().equalsIgnoreCase(name) || !warp.getWorld().equals(world)) continue;
            return Optional.of(warp);
        }
        return Optional.empty();
    }

    public static Collection<WarpPoint> getAll(){
        return new ArrayList<>(warpPoints);
    }


    public static WarpPoint createWarp(String name, Location location){
        WarpPoint warp = WarpPoint.create(name, location);
        WarpPoints.addWarp(warp);
        save(location.getWorld());
        return warp;
    }

    public static File getWarpsFile(World world){
        return new File(WorldManager.getPluginDirectory(WarpsPlugin.getInstance(), world), "warps.json");
    }

    public static void loadWarps(World world){
        File dataFile = getWarpsFile(world);
        if(dataFile != null){
            WarpPoints.load(dataFile, world);
        } else{
            Bukkit.getLogger().info(WarpsPlugin.getPrefix() + " Couldn't load warps.json for world " + world.getName());
        }
    }

    public static void load(File dataFile, World world){
        JsonObject json = JsonUtil.parse(dataFile);
        if(json == null || !json.has("warps")) {
            Bukkit.getLogger().info(WarpsPlugin.getPrefix() + " Couldn't load warp-file for world " + world.getName());
            return;
        }
        JsonArray warpsSection = json.getAsJsonArray("warps");
        for(JsonElement element : warpsSection){
            if(!element.isJsonObject()) continue;
            JsonObject warpSection = element.getAsJsonObject();
            WarpPoint warp = WarpPoint.load(warpSection, world);
            if(warp == null) continue;
            warpPoints.add(warp);
        }
    }

    public static void remove(WarpPoint warp){
        World world = warp.getWorld();
        warpPoints.remove(warp);
        save(world);
    }

    public static void save(World world) {
        File dataFile = getWarpsFile(world);

        JsonArray warpsArray = new JsonArray();
        for(WarpPoint warp : warpPoints.stream().filter(w -> w.getWorld()==world).collect(Collectors.toList())) {
            JsonObject warpObject = warp.save();
            warpsArray.add(warpObject);
        }
        JsonObject json = new JsonObject();
        json.add("warps", warpsArray);
        JsonUtil.save(dataFile, json);
    }

    protected static void unloadWarps() {
        warpPoints.clear();
    }

    protected static void unloadWarps(World world){
        for(WarpPoint warp : warpPoints.stream().filter(w -> w.getWorld()==world).collect(Collectors.toList())){
            warpPoints.remove(warp);
        }
    }
}
