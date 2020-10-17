package ch.swisssmp.utils;

import ch.swisssmp.utils.nbt.NBTUtil;
import net.querz.nbt.tag.CompoundTag;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.libs.org.apache.commons.codec.binary.Base64;
import org.bukkit.craftbukkit.v1_16_R2.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;
import java.util.function.Function;

public class ItemUtil {
	
	public static String serialize(ItemStack itemStack) {
		org.bukkit.configuration.file.YamlConfiguration yamlConfiguration = new YamlConfiguration();
		yamlConfiguration.set("item", itemStack);
		return Base64.encodeBase64String(yamlConfiguration.saveToString().getBytes());
	}
	
	public static ItemStack deserialize(String s) {
		if(Base64.isBase64(s)){
			org.bukkit.configuration.file.YamlConfiguration yamlConfiguration = new YamlConfiguration();
			try {
				yamlConfiguration.loadFromString(new String(Base64.decodeBase64(s)));
				return yamlConfiguration.getItemStack("item");
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
		return null;
	}
	
	public static boolean isHelmet(ItemStack itemStack){
		if(itemStack==null)return false;
		Material material = itemStack.getType();
		return 
				material==Material.LEATHER_HELMET||
				material==Material.CHAINMAIL_HELMET||
				material==Material.IRON_HELMET||
				material==Material.GOLDEN_HELMET||
				material==Material.DIAMOND_HELMET;
	}
	
	public static boolean isChestplate(ItemStack itemStack){
		if(itemStack==null)return false;
		Material material = itemStack.getType();
		return 
				material==Material.LEATHER_CHESTPLATE||
				material==Material.CHAINMAIL_CHESTPLATE||
				material==Material.IRON_CHESTPLATE||
				material==Material.GOLDEN_CHESTPLATE||
				material==Material.DIAMOND_CHESTPLATE;
	}
	
	public static boolean isLeggings(ItemStack itemStack){
		if(itemStack==null)return false;
		Material material = itemStack.getType();
		return 
				material==Material.LEATHER_LEGGINGS||
				material==Material.CHAINMAIL_LEGGINGS||
				material==Material.IRON_LEGGINGS||
				material==Material.GOLDEN_LEGGINGS||
				material==Material.DIAMOND_LEGGINGS;
	}
	
	public static boolean isBoots(ItemStack itemStack){
		if(itemStack==null)return false;
		Material material = itemStack.getType();
		return 
				material==Material.LEATHER_BOOTS||
				material==Material.CHAINMAIL_BOOTS||
				material==Material.IRON_BOOTS||
				material==Material.GOLDEN_BOOTS||
				material==Material.DIAMOND_BOOTS;
	}
	
	public static Position getPosition(ItemStack itemStack, String key){
		net.minecraft.server.v1_16_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(itemStack);
		if(!nmsStack.hasTag()) return null;
		CompoundTag tag = NBTUtil.fromNMS(nmsStack.getTag());
		return tag!=null ? NBTUtil.getPosition(key, tag) : null;
	}
	
	public static void setPosition(ItemStack itemStack, String key, Position position){
		net.minecraft.server.v1_16_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(itemStack);
		CompoundTag tag = NBTUtil.fromNMS(nmsStack.getTag());
		if(tag==null) tag = new CompoundTag();
		NBTUtil.set(key, position, tag);
		nmsStack.setTag(NBTUtil.toNMS(tag));
		itemStack.setItemMeta(CraftItemStack.getItemMeta(nmsStack));
	}
	
	public static String getString(ItemStack itemStack, String key){
		net.minecraft.server.v1_16_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(itemStack);
		if(!nmsStack.hasTag()) return null;
		CompoundTag tag = NBTUtil.fromNMS(nmsStack.getTag());
		return tag!=null && tag.containsKey(key) ? tag.getString(key) : null;
	}
	
	public static void setString(ItemStack itemStack, String key, String value){
		net.minecraft.server.v1_16_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(itemStack);
		CompoundTag tag = NBTUtil.fromNMS(nmsStack.getTag());
		if(tag==null) tag = new CompoundTag();
		tag.putString(key, value);
		nmsStack.setTag(NBTUtil.toNMS(tag));
		itemStack.setItemMeta(CraftItemStack.getItemMeta(nmsStack));
	}
	
	public static int getInt(ItemStack itemStack, String key){
		return ItemUtil.getInt(itemStack, key, 0);
	}
	
	public static int getInt(ItemStack itemStack, String key, int fallback){
		net.minecraft.server.v1_16_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(itemStack);
		if(!nmsStack.hasTag()) return fallback;
		CompoundTag tag = NBTUtil.fromNMS(nmsStack.getTag());
		return tag!=null && tag.containsKey(key) ? tag.getInt(key) : fallback;
	}
	
	public static void setInt(ItemStack itemStack, String key, int value){
		net.minecraft.server.v1_16_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(itemStack);
		CompoundTag tag = NBTUtil.fromNMS(nmsStack.getTag());
		if(tag==null) tag = new CompoundTag();
		tag.putInt(key, value);
		nmsStack.setTag(NBTUtil.toNMS(tag));
		itemStack.setItemMeta(CraftItemStack.getItemMeta(nmsStack));
	}
	
	public static double getDouble(ItemStack itemStack, String key){
		return ItemUtil.getDouble(itemStack, key, 0);
	}
	
	public static double getDouble(ItemStack itemStack, String key, double fallback){
		net.minecraft.server.v1_16_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(itemStack);
		if(!nmsStack.hasTag()) return fallback;
		CompoundTag tag = NBTUtil.fromNMS(nmsStack.getTag());
		return tag!=null && tag.containsKey(key) ? tag.getDouble(key) : fallback;
	}
	
	public static void setDouble(ItemStack itemStack, String key, double value){
		net.minecraft.server.v1_16_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(itemStack);
		CompoundTag tag = NBTUtil.fromNMS(nmsStack.getTag());
		if(tag==null) tag = new CompoundTag();
		tag.putDouble(key, value);
		nmsStack.setTag(NBTUtil.toNMS(tag));
		itemStack.setItemMeta(CraftItemStack.getItemMeta(nmsStack));
	}
	
	public static boolean getBoolean(ItemStack itemStack, String key){
		return ItemUtil.getBoolean(itemStack, key, false);
	}
	
	public static boolean getBoolean(ItemStack itemStack, String key, boolean fallback){
		net.minecraft.server.v1_16_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(itemStack);
		if(!nmsStack.hasTag()) return fallback;
		CompoundTag tag = NBTUtil.fromNMS(nmsStack.getTag());
		return tag!=null && tag.containsKey(key) ? tag.getBoolean(key) : fallback;
	}
	
	public static void setBoolean(ItemStack itemStack, String key, boolean value){
		net.minecraft.server.v1_16_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(itemStack);
		CompoundTag tag = NBTUtil.fromNMS(nmsStack.getTag());
		if(tag==null) tag = new CompoundTag();
		tag.putBoolean(key, value);
		nmsStack.setTag(NBTUtil.toNMS(tag));
		itemStack.setItemMeta(CraftItemStack.getItemMeta(nmsStack));
	}
	
	public static void setData(ItemStack itemStack, CompoundTag tag){
		net.minecraft.server.v1_16_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(itemStack);
		nmsStack.setTag(NBTUtil.toNMS(tag!=null ? tag : new CompoundTag()));
		itemStack.setItemMeta(CraftItemStack.getItemMeta(nmsStack));
	}
	
	public static CompoundTag getData(ItemStack itemStack){
		net.minecraft.server.v1_16_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(itemStack);
		if(!nmsStack.hasTag()) return null;
		return NBTUtil.fromNMS(nmsStack.getTag());
	}

	public static void updateItemsGlobal(Function<ItemStack,Boolean> filter, Consumer<ItemStack> callback){
		for(Player player : Bukkit.getOnlinePlayers()){
			updateItems(player.getInventory(), filter, callback);
			if(player.getOpenInventory()!=null){
				updateItems(player.getOpenInventory().getTopInventory(), filter, callback);
			}
		}
	}

	public static void updateItems(Inventory inventory, Function<ItemStack,Boolean> filter, Consumer<ItemStack> callback){
		for(ItemStack itemStack : inventory){
			if(itemStack==null || itemStack.getType()==Material.AIR) continue;
			if(!filter.apply(itemStack)) continue;
			callback.accept(itemStack);
		}
	}
}
