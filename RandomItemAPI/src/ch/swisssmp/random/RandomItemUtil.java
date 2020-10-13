package ch.swisssmp.random;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v1_16_R2.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import ch.swisssmp.utils.Random;
import net.minecraft.server.v1_16_R2.NBTBase;
import net.minecraft.server.v1_16_R2.NBTTagCompound;
import net.minecraft.server.v1_16_R2.NBTTagList;

public class RandomItemUtil {
	private static Random random = new Random();
	
	public static ItemStack buildItemStack(ItemStack template){
		return RandomItemUtil.buildItemStack(template, random);
	}
	public static ItemStack buildItemStack(ItemStack template, Random random){
		return RandomItemUtil.buildItemStack(template, random, -1);
	}
	public static ItemStack buildItemStack(ItemStack template, Random random, double chanceOverride){
		//extract nbt data from the template
		net.minecraft.server.v1_16_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(template);
		NBTTagCompound nbtTag = nmsStack.getTag();
		//if there is nothing to randomize simply return a copy of the template
		if(nbtTag==null || !nbtTag.hasKey("randomize")) return template.clone();
		//get randomize data
		NBTTagCompound randomizeData = nbtTag.getCompound("randomize");
		//cancel if this time there is no result
		if(chanceOverride>=0 || randomizeData.hasKey("chance")){
			if(random.nextDouble()<(chanceOverride>=0 ? chanceOverride : randomizeData.getDouble("chance"))) return null;
		}
		//clear the randomize data from the (copied) template data and then build a new itemstack
		nbtTag.remove("randomize");
		nmsStack.setTag(nbtTag);
		ItemStack result = CraftItemStack.asBukkitCopy(nmsStack);
		//randomize values that do not require nbt tag modification
		RandomItemUtil.randomizeBaseValues(result, random, randomizeData);
		//if this randomization resulted in an amount of zero items in the stack return nothing
		if(result.getAmount()==0) return null;
		//extract the nbt data from the resulting itemstack
		NBTTagCompound resultNBTData = RandomItemUtil.getNBTData(result);
		if(resultNBTData==null) resultNBTData = new NBTTagCompound();
		//extract any existing attributemodifiers or create a new compound for them
		NBTTagList attributeModifiers = (resultNBTData.hasKey("AttributeModifiers")) ? resultNBTData.getList("AttributeModifiers",10) : new NBTTagList();
		//randomize attribute modifiers
		RandomItemUtil.randomizeAttributeModifiers(attributeModifiers, random, randomizeData);
		//if any were created put them into the resulting nbt data
		if(attributeModifiers.size()>0){
			resultNBTData.set("AttributeModifiers", attributeModifiers);
		}
		//if nbt data was created add it to the resulting itemstack
		if(resultNBTData.getKeys().size()>0){
			RandomItemUtil.setNBTData(result, resultNBTData);
		}
		//remove randomizeDescription from result
		RandomItemUtil.removeRandomizeDescription(result);
		//return the result
		return result;
	}
	
	private static void randomizeBaseValues(ItemStack itemStack, Random random, NBTTagCompound randomizeData){
		if(randomizeData.hasKey("amount")){
			RandomItemUtil.randomizeAmount(itemStack, random, randomizeData.getCompound("amount"));
		}
		if(randomizeData.hasKey("durability")){
			RandomItemUtil.randomizeDurability(itemStack, random, randomizeData.getCompound("durability"));
		}
		if(randomizeData.hasKey("enchantments")){
			RandomItemUtil.randomizeEnchantments(itemStack, random, randomizeData.getCompound("enchantments"));
		}
	}
	
	private static void randomizeAttributeModifiers(NBTTagList attributeModifiers, Random random, NBTTagCompound randomizeData){
		if(randomizeData.hasKey("attack_damage")){
			RandomItemUtil.randomizeAttackDamage(attributeModifiers, random, randomizeData.getCompound("attack_damage"));
		}
		if(randomizeData.hasKey("attack_speed")){
			RandomItemUtil.randomizeAttackSpeed(attributeModifiers, random, randomizeData.getCompound("attack_speed"));
		}
		if(randomizeData.hasKey("armor")){
			RandomItemUtil.randomizeArmor(attributeModifiers, random, randomizeData.getCompound("armor"));
		}
		if(randomizeData.hasKey("toughness")){
			RandomItemUtil.randomizeToughness(attributeModifiers, random, randomizeData.getCompound("toughness"));
		}
	}
	
	private static void randomizeAmount(ItemStack itemStack, Random random, NBTTagCompound dataSection){
		int min = dataSection.getInt("min");
		int max = dataSection.getInt("max");
		itemStack.setAmount(random.nextInt(max-min)+min);
	}
	
	private static void randomizeDurability(ItemStack itemStack, Random random, NBTTagCompound dataSection){
		short min = dataSection.getShort("min");
		short max = dataSection.getShort("max");
		ItemMeta itemMeta = itemStack.getItemMeta();
		if(!(itemMeta instanceof Damageable)) return;
		((Damageable)itemMeta).setDamage(random.nextInt(max-min)+min);
	}
	
	private static void randomizeEnchantments(ItemStack itemStack, Random random, NBTTagCompound dataSection){
		ItemMeta itemMeta = itemStack.getItemMeta();
		NBTTagCompound enchantmentSection;
		Enchantment enchantment;
		int min;
		int max;
		for(String key : dataSection.getKeys()){
			enchantmentSection = dataSection.getCompound(key);
			enchantment = Enchantment.getByKey(NamespacedKey.minecraft(enchantmentSection.getString("enchantment")));
			if(enchantment==null) continue;
			min = enchantmentSection.getInt("min");
			max = enchantmentSection.getInt("max");
			itemMeta.addEnchant(enchantment, random.nextInt(max-min)+min, true);
		}
		itemStack.setItemMeta(itemMeta);
	}
	
	private static void randomizeAttackDamage(NBTTagList attributeModifiers, Random random, NBTTagCompound dataSection){
		RandomItemUtil.clearAttribute(attributeModifiers, "generic.attackDamage");
		double min = dataSection.getDouble("min");
		double max = dataSection.getDouble("max");
		NBTTagCompound attribute = RandomItemUtil.getNewAttributeModifier("generic.attackDamage", dataSection.getString("slot"));
		attribute.setDouble("Amount", random.nextDouble()*(max-min)+min);
		attribute.setString("Slot", "mainhand");
		attributeModifiers.add(attribute);
	}
	
	private static void randomizeAttackSpeed(NBTTagList attributeModifiers, Random random, NBTTagCompound dataSection){
		RandomItemUtil.clearAttribute(attributeModifiers, "generic.attackSpeed");
		double min = dataSection.getDouble("min");
		double max = dataSection.getDouble("max");
		NBTTagCompound attribute = RandomItemUtil.getNewAttributeModifier("generic.attackSpeed", dataSection.getString("slot"));
		attribute.setDouble("Amount", random.nextDouble()*(max-min)+min);
		attribute.setInt("Operation", 0);
		attribute.setString("Slot", dataSection.getString("slot"));
		attributeModifiers.add(attribute);
	}
	
	private static void randomizeArmor(NBTTagList attributeModifiers, Random random, NBTTagCompound dataSection){
		RandomItemUtil.clearAttribute(attributeModifiers, "generic.armor");
		double min = dataSection.getDouble("min");
		double max = dataSection.getDouble("max");
		NBTTagCompound attribute = RandomItemUtil.getNewAttributeModifier("generic.armor", dataSection.getString("slot"));
		attribute.setDouble("Amount", random.nextDouble()*(max-min)+min);
		attribute.setInt("Operation", 0);
		attributeModifiers.add(attribute);
	}
	
	private static void randomizeToughness(NBTTagList attributeModifiers, Random random, NBTTagCompound dataSection){
		RandomItemUtil.clearAttribute(attributeModifiers, "generic.armorToughness");
		double min = dataSection.getDouble("min");
		double max = dataSection.getDouble("max");
		NBTTagCompound attribute = RandomItemUtil.getNewAttributeModifier("generic.armorToughness", dataSection.getString("slot"));
		attribute.setDouble("Amount", random.nextDouble()*(max-min)+min);
		attribute.setInt("Operation", 0);
		attributeModifiers.add(attribute);
	}
	
	private static NBTTagCompound getNewAttributeModifier(String name, String slot){
		NBTTagCompound attribute = new NBTTagCompound();
		attribute.setString("AttributeName", name);
		attribute.setString("Name", name);
		attribute.setInt("UUIDLeast", 894654);
		attribute.setInt("UUIDMost", 2872);
		attribute.setString("Slot", slot);
		return attribute;
	}
	
	private static void clearAttribute(NBTTagList attributeModifiers, String attribute){
		List<Integer> removeIndexes = new ArrayList<Integer>();
		for(int i = 0; i < attributeModifiers.size(); i++){
			NBTBase baseTag = attributeModifiers.get(i);
			if(!(baseTag instanceof NBTTagCompound)) continue;
			NBTTagCompound tag = (NBTTagCompound)baseTag;
			if(!tag.hasKey("AttributeName")) continue;
			if(!tag.getString("AttributeName").equals(attribute)) continue;
			removeIndexes.add(i);
		}
		//remove indexes reversed, otherwise shifting will cause a mess
		Collections.reverse(removeIndexes);
		for(Integer index : removeIndexes){
			attributeModifiers.remove((int) index); // cast Integer to int for signature clarity
		}
	}
	
	private static NBTTagCompound getNBTData(ItemStack itemStack){
		net.minecraft.server.v1_16_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(itemStack);
		return nmsStack.getTag();
	}
	
	private static void setNBTData(ItemStack itemStack, NBTTagCompound nbtTag){
		net.minecraft.server.v1_16_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(itemStack);
		nmsStack.setTag(nbtTag);
		itemStack.setItemMeta(CraftItemStack.getItemMeta(nmsStack));
	}
	
	public static void addRandomizeDescription(ItemStack itemStack){
		//clear old data
		RandomItemUtil.removeRandomizeDescription(itemStack);
		//get randomize data
		NBTTagCompound nbtTag = RandomItemUtil.getNBTData(itemStack);
		//if there is no randomize data return
		if(nbtTag==null || !nbtTag.hasKey("randomize")) return;
		NBTTagCompound randomizeData = nbtTag.getCompound("randomize");
		//get the lore data from the stack
		ItemMeta itemMeta = itemStack.getItemMeta();
		List<String> lore = itemMeta.getLore();
		if(lore==null) lore = new ArrayList<String>();
		lore.add(ChatColor.LIGHT_PURPLE+"Zufällig:");
		if(randomizeData.hasKey("chance")){
			lore.add(ChatColor.GRAY+"Chance: "+ChatColor.GREEN+(randomizeData.getDouble("chance")*100+"%"));
		}
		if(randomizeData.hasKey("amount")){
			NBTTagCompound data = randomizeData.getCompound("amount");
			lore.add(ChatColor.GRAY+"Menge: "+ChatColor.WHITE+(data.getInt("min")+"-"+data.getInt("max")));
		}
		if(randomizeData.hasKey("durability")){
			NBTTagCompound data = randomizeData.getCompound("durability");
			lore.add(ChatColor.GRAY+"Beschädigung: "+ChatColor.WHITE+(data.getInt("min")+"-"+data.getInt("max")));
		}
		if(randomizeData.hasKey("enchantments")){
			lore.add("Verzauberungen:");
			NBTTagCompound enchantments = randomizeData.getCompound("enchantments");
			NBTTagCompound enchantmentData;
			for(String key : enchantments.getKeys()){
				enchantmentData = enchantments.getCompound(key);
				lore.add(ChatColor.GRAY+"- "+enchantmentData.getString("enchantment")+" ("+ChatColor.WHITE+(enchantmentData.getInt("min")+"-"+enchantmentData.getInt("max")+")"+ChatColor.GRAY+": "+ChatColor.GREEN+enchantmentData.getDouble("chance")+"%"));
			}
		}
		if(randomizeData.hasKey("attack_damage")){
			NBTTagCompound data = randomizeData.getCompound("attack_damage");
			lore.add(ChatColor.GRAY+"Schaden: "+ChatColor.WHITE+(data.getDouble("min")+"-"+data.getDouble("max")));
		}
		if(randomizeData.hasKey("attack_speed")){
			NBTTagCompound data = randomizeData.getCompound("attack_speed");
			lore.add(ChatColor.GRAY+"Angriffsgeschwindigkeit: "+ChatColor.WHITE+(data.getDouble("min")+"-"+data.getDouble("max")));
		}
		if(randomizeData.hasKey("armor")){
			NBTTagCompound data = randomizeData.getCompound("armor");
			lore.add(ChatColor.GRAY+"Rüstung: "+ChatColor.WHITE+(data.getDouble("min")+"-"+data.getDouble("max")));
		}
		if(randomizeData.hasKey("toughness")){
			NBTTagCompound data = randomizeData.getCompound("toughness");
			lore.add(ChatColor.GRAY+"Härte: "+ChatColor.WHITE+(data.getDouble("min")+"-"+data.getDouble("max")));
		}
		//apply the changed data to the item stack
		itemMeta.setLore(lore);
		itemStack.setItemMeta(itemMeta);
	}
	
	public static void removeRandomizeDescription(ItemStack itemStack){
		//get the lore data from the stack
		ItemMeta itemMeta = itemStack.getItemMeta();
		List<String> lore = itemMeta.getLore();
		//find out at what line the randomize description starts
		int randomizeDescriptionStart = RandomItemUtil.getRandomizeDescriptionStart(lore);
		//if no randomize description was found return
		if(randomizeDescriptionStart<0) return;
		//remove all lines of the randomize description
		while(lore.size()>randomizeDescriptionStart){
			lore.remove(randomizeDescriptionStart);
		}
		//apply the changed data to the item stack
		itemMeta.setLore(lore);
		itemStack.setItemMeta(itemMeta);
	}
	
	public static String getDescriptiveItemString(ItemStack itemStack){
		String result = ChatColor.GRAY+"";
		String name;
		if(itemStack.hasItemMeta() && itemStack.getItemMeta().hasDisplayName()){
			name = itemStack.getItemMeta().getDisplayName();
		}
		else{
			name = itemStack.getType().toString();
		}
		result+=name;
		NBTTagCompound nbtTag = RandomItemUtil.getNBTData(itemStack);
		if(nbtTag!=null && nbtTag.hasKey("randomize")){
			NBTTagCompound randomizeData = nbtTag.getCompound("randomize");
			if(randomizeData.hasKey("amount")){
				NBTTagCompound data = randomizeData.getCompound("amount");
				result+= " x("+data.getInt("min")+"-"+data.getInt("max")+")";
			}
			if(randomizeData.hasKey("chance")){
				result+=ChatColor.DARK_GRAY+" - "+(randomizeData.getDouble("chance")*100)+"%";
			}
		}
		else{
			result+=" x"+itemStack.getAmount();
		}
		return result;
	}
	
	public static String getDefaultSlot(ItemStack itemStack, String fallback){
		String type = itemStack.getType().toString().toLowerCase();
		if(type.contains("helmet")) return "head";
		if(type.contains("chestplate")) return "chest";
		if(type.contains("leggings")) return "legs";
		if(type.contains("boots")) return "feet";
		return fallback;
	}
	
	public static double getItemChance(ItemStack itemStack, double defaultChance){
		NBTTagCompound nbtTag = RandomItemUtil.getNBTData(itemStack);
		if(nbtTag==null || !nbtTag.hasKey("randomize")) return defaultChance;
		NBTTagCompound randomizeData = nbtTag.getCompound("randomize");
		if(!randomizeData.hasKey("chance")) return defaultChance;
		return randomizeData.getDouble("chance");
	}
	
	private static int getRandomizeDescriptionStart(List<String> lore){
		//if there is no lore return
		if(lore==null || lore.size()==0) return -1;
		for(int i = 0; i < lore.size(); i++){
			if(!lore.get(i).equals(ChatColor.LIGHT_PURPLE+"Zufällig:")) continue;
			return i;
		}
		return -1;
	}
}
