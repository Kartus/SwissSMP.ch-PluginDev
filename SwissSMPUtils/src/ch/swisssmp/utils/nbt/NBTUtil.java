package ch.swisssmp.utils.nbt;

import ch.swisssmp.utils.ItemUtil;
import ch.swisssmp.utils.Position;
import net.minecraft.server.v1_16_R2.NBTTagCompound;
import net.querz.nbt.io.NamedTag;
import net.querz.nbt.tag.*;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class NBTUtil {

    public static CompoundTag fromNMS(NBTTagCompound nms){
        return BukkitNBTAdapter.adapt(nms);
    }

    public static NBTTagCompound toNMS(CompoundTag tag){
        return BukkitNBTAdapter.adapt(tag);
    }

    public static String toString(Tag<?> tag){
        try {
            return net.querz.nbt.io.SNBTUtil.toSNBT(tag);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static NamedTag parse(File file){
        try {
            return net.querz.nbt.io.NBTUtil.read(file);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean save(File file, NamedTag tag){
        try {
            net.querz.nbt.io.NBTUtil.write(tag, file);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static UUID getUUID(String key, CompoundTag tag){
        Tag<?> element = tag.containsKey(key) ? tag.get(key) : null;
        String idString = element instanceof StringTag ? ((StringTag) element).getValue() : null;
        try{
            return idString!=null ? UUID.fromString(idString) : null;
        }
        catch(Exception e){
            return null;
        }
    }

    public static void set(String key, UUID uuid, CompoundTag tag){
        tag.putString(key, uuid.toString());
    }

    public static Color getColor(String key, CompoundTag tag){
        return Color.fromRGB(tag.getInt(key));
    }

    public static void set(String key, Color value, CompoundTag tag){
        tag.putInt(key, value.asRGB());
    }

    public static Block getBlock(String key, World world, CompoundTag tag){
        Tag<?> locationElement = tag.containsKey(key) ? tag.get(key) : null;
        if(!(locationElement instanceof CompoundTag)) return null;
        CompoundTag locationData = (CompoundTag) locationElement;
        int x = locationData.getInt("x");
        int y = locationData.getInt("y");
        int z = locationData.getInt("z");
        return world.getBlockAt(x,y,z);
    }

    public static void set(String key, Block block, CompoundTag tag){
        tag.put(key, toCompoundTag(block));
    }

    public static CompoundTag toCompoundTag(Block b){
        CompoundTag tag = new CompoundTag();
        tag.putInt("x", b.getX());
        tag.putInt("y", b.getY());
        tag.putInt("z", b.getZ());
        return tag;
    }

    public static Location getLocation(World world, CompoundTag tag){
        if(tag==null) return null;
        double x = tag.getDouble("x");
        double y = tag.getDouble("y");
        double z = tag.getDouble("z");
        if(tag.containsKey("pitch") && tag.containsKey("yaw")){
            float pitch = tag.getFloat("pitch");
            float yaw = tag.getFloat("yaw");
            return new Location(world, x, y, z, yaw, pitch);
        }

        return new Location(world, x, y, z);
    }

    public static Location getLocation(String key, World world, CompoundTag tag){
        Tag<?> locationElement = tag.containsKey(key) ? tag.get(key) : null;
        if(!(locationElement instanceof CompoundTag)) return null;
        return getLocation(world, (CompoundTag) locationElement);
    }

    public static void set(String key, Location location, CompoundTag tag){
        tag.put(key, toCompoundTag(location));
    }

    public static CompoundTag toCompoundTag(Location l){
        CompoundTag tag = new CompoundTag();
        tag.putDouble("x", l.getX());
        tag.putDouble("y", l.getY());
        tag.putDouble("z", l.getZ());
        tag.putFloat("yaw", l.getYaw());
        tag.putFloat("pitch", l.getPitch());
        return tag;
    }

    public static Position getPosition(CompoundTag tag){
        if(tag==null) return null;
        double x = tag.getDouble("x");
        double y = tag.getDouble("y");
        double z = tag.getDouble("z");
        if(tag.containsKey("pitch") && tag.containsKey("yaw")){
            float pitch = tag.getFloat("pitch");
            float yaw = tag.getFloat("yaw");
            return new Position(x, y, z, yaw, pitch);
        }

        return new Position(x, y, z);
    }

    public static Position getPosition(String key, CompoundTag tag){
        Tag<?> positionElement = tag.containsKey(key) ? tag.get(key) : null;
        if(!(positionElement instanceof CompoundTag)) return null;
        return getPosition((CompoundTag) positionElement);
    }

    public static void set(String key, Position position, CompoundTag tag){
        tag.put(key, toCompoundTag(position));
    }

    public static CompoundTag toCompoundTag(Position p){
        CompoundTag tag = new CompoundTag();
        tag.putDouble("x", p.getX());
        tag.putDouble("y", p.getY());
        tag.putDouble("z", p.getZ());
        tag.putFloat("yaw", p.getYaw());
        tag.putFloat("pitch", p.getPitch());
        return tag;
    }

    public static Vector getVector(CompoundTag tag){
        if(tag==null) return null;
        double x = tag.getDouble("x");
        double y = tag.getDouble("y");
        double z = tag.getDouble("z");

        return new Vector(x, y, z);
    }

    public static Vector getVector(String key, CompoundTag tag){
        Tag<?> vectorElement = tag.containsKey(key) ? tag.get(key) : null;
        if(!(vectorElement instanceof CompoundTag)) return null;
        return getVector((CompoundTag) vectorElement);
    }

    public static void set(String key, Vector vector, CompoundTag tag){
        tag.put(key, toCompoundTag(vector));
    }

    public static CompoundTag toCompoundTag(Vector v){
        CompoundTag tag = new CompoundTag();
        tag.putDouble("x", v.getX());
        tag.putDouble("y", v.getY());
        tag.putDouble("z", v.getZ());
        return tag;
    }

    public static BlockVector getBlockVector(CompoundTag tag){
        if(tag==null) return null;
        int x = tag.getInt("x");
        int y = tag.getInt("y");
        int z = tag.getInt("z");

        return new BlockVector(x, y, z);
    }

    public static BlockVector getBlockVector(String key, CompoundTag tag){
        Tag<?> element = tag.containsKey(key) ? tag.get(key) : null;
        if(!(element instanceof CompoundTag)) return null;
        return getBlockVector((CompoundTag) element);
    }

    public static void set(String key, BlockVector vector, CompoundTag tag){
        tag.put(key, toCompoundTag(vector));
    }

    public static CompoundTag toCompoundTag(BlockVector v){
        CompoundTag tag = new CompoundTag();
        tag.putInt("x", v.getBlockX());
        tag.putInt("y", v.getBlockY());
        tag.putInt("z", v.getBlockZ());
        return tag;
    }

    public static ItemStack getItemStack(String key, CompoundTag tag){
        String serialized = tag.getString(key);
        return serialized!=null ? ItemUtil.deserialize(serialized) : null;
    }

    public static void set(String key, ItemStack itemStack, CompoundTag tag){
        if(itemStack==null){
            if(tag.containsKey(key)) tag.remove(key);
            return;
        }
        tag.putString(key, ItemUtil.serialize(itemStack));
    }
}
