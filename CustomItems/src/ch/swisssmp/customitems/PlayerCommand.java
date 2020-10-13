package ch.swisssmp.customitems;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.NamespacedKey;
import org.bukkit.command.TabExecutor;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_16_R2.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;

import net.minecraft.server.v1_16_R2.NBTBase;
import net.minecraft.server.v1_16_R2.NBTTagByteArray;
import net.minecraft.server.v1_16_R2.NBTTagCompound;
import net.minecraft.server.v1_16_R2.NBTTagIntArray;
import net.minecraft.server.v1_16_R2.NBTTagList;
import org.bukkit.util.StringUtil;

public class PlayerCommand implements TabExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(args==null || args.length==0) return false;
		switch(args[0]){
		case "summon":{
			if(args.length<2) return false;
			if(!(sender instanceof Player)){
				sender.sendMessage("[CustomItems] Kann nur ingame verwendet werden.");
				return true;
			}
			CustomItemBuilder customItemBuilder;
			Player player = (Player) sender;
			if(args.length>2 && StringUtils.isNumeric(args[2])){
				customItemBuilder = CustomItems.getCustomItemBuilder(args[1], Integer.parseInt(args[2]));
			}
			else{
				customItemBuilder = CustomItems.getCustomItemBuilder(args[1]);
			}
			if(customItemBuilder==null){
				sender.sendMessage("[CustomItems] Konnte den ItemBuilder nicht generieren.");
				return true;
			}
			ItemStack itemStack = customItemBuilder.build();
			if(itemStack==null){
				sender.sendMessage("[CustomItems] Konnte den ItemStack nicht generieren.");
				return true;
			}
			sender.sendMessage("[CustomItems] "+itemStack.getAmount()+"x "+itemStack.getItemMeta().getDisplayName()+"§r generiert!");
			player.getWorld().dropItem(player.getEyeLocation(), itemStack);
			break;
		}
		case "inspect":{
			if(!(sender instanceof Player)){
				sender.sendMessage("[CustomItems] Kann nur ingame verwendet werden.");
				return true;
			}
			Player player = (Player) sender;
			ItemStack itemStack = player.getInventory().getItemInMainHand();
			if(itemStack==null) itemStack = player.getInventory().getItemInOffHand();
			if(itemStack==null) return true;
			ItemMeta itemMeta = itemStack.getItemMeta();
			String name;
			if(itemMeta!=null){
				name = itemMeta.getDisplayName();
				if(name==null) name = itemMeta.getLocalizedName();
			}
			else{
				name = itemStack.getType().name();
			}
			sender.sendMessage("[CustomItems] Analysiere "+name);
			net.minecraft.server.v1_16_R2.ItemStack craftItemStack = CraftItemStack.asNMSCopy(itemStack);
			if(craftItemStack.hasTag()){
				NBTTagCompound nbtTags = craftItemStack.getTag();
				this.displayNBTTagCompound(nbtTags, player, 0);
			}
			else{
				sender.sendMessage("Keine NBT Daten gefunden.");
			}
			break;
		}
		case "craft":
		case "crafting":
		case "recipe":
		case "recipes":{
			if(!(sender instanceof Player)){
				sender.sendMessage("[CustomItems] Kann nur ingame verwendet werden.");
				return true;
			}
			Player player = (Player) sender;
			ItemStack itemStack = player.getInventory().getItemInMainHand();
			if(itemStack==null) itemStack = player.getInventory().getItemInOffHand();
			if(itemStack==null) return true;
			String name = this.getItemDisplayName(itemStack);
			sender.sendMessage("[CustomItems] Rezepte für "+name);
			for(Recipe recipe : Bukkit.getRecipesFor(itemStack)){
				if(!recipe.getResult().isSimilar(itemStack)) continue;
				if(recipe instanceof ShapelessRecipe){
					ShapelessRecipe shapeless = (ShapelessRecipe)recipe;
					sender.sendMessage("Formloses Rezept:");
					for(ItemStack ingredient : shapeless.getIngredientList()){
						sender.sendMessage("- "+this.getItemDisplayName(ingredient));
					}
				}
				else if(recipe instanceof ShapedRecipe){
					ShapedRecipe shaped = (ShapedRecipe) recipe;
					sender.sendMessage("Formfestes Rezept:");
					sender.sendMessage(this.getOffset(1)+"Form:");
					for(int i = 0; i < shaped.getShape().length; i++){
						sender.sendMessage(this.getOffset(2)+shaped.getShape()[i]);
					}
					sender.sendMessage(this.getOffset(1)+"Zutaten:");
					for(Entry<Character, ItemStack> ingredient : shaped.getIngredientMap().entrySet()){
						sender.sendMessage(this.getOffset(2)+ingredient.getKey()+": "+this.getItemDisplayName(ingredient.getValue()));
					}
				}
				else if(recipe instanceof FurnaceRecipe){
					FurnaceRecipe furnace = (FurnaceRecipe) recipe;
					sender.sendMessage("Schmelz Rezept:");
					sender.sendMessage(this.getOffset(1)+"Zutat: "+this.getItemDisplayName(furnace.getInput()));
				}
			}
			return true;
		}
		case "uploaddata":{
			CustomItems.uploadData();
			return true;
		}
		case "reload":{
			CustomItems.reload();
			sender.sendMessage("[CustomItems] Items & Materialien aktualisiert.");
			return true;
		}
		default:
			return false;
		}
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		if(args.length<=1){
			List<String> options = Arrays.asList("reload", "summon", "inspect", "recipes", "uploaddata", "view");
			String current = args.length>0 ? args[0] : "";
			return StringUtil.copyPartialMatches(current, options, new ArrayList<>());
		}
		switch(args[0]){
			case "summon":{
				List<String> options = Stream.concat(
						CustomItemTemplates.getKeys().stream(),
						CustomMaterialTemplates.getKeys().stream()
				).distinct().map(NamespacedKey::toString).collect(Collectors.toList());
				String current = args.length>1 ? args[1] : "";
				return StringUtil.copyPartialMatches(current, options, new ArrayList<>());
			}
			case "reload":
			case "inspect":
			case "recipes":
			case "uploaddata":
			case "view":
			default:
				return Collections.emptyList();
		}
	}
	
	private void displayNBTTag(String label, NBTBase tag, Player player, int depth){
		if(tag instanceof NBTTagCompound){
			if(!label.trim().isEmpty()) player.sendMessage(this.getOffset(depth)+label+":");
			this.displayNBTTagCompound((NBTTagCompound)tag, player, depth+1);
		}
		else if(tag instanceof NBTTagList){
			if(!label.trim().isEmpty()) player.sendMessage(this.getOffset(depth)+label+":");
			this.displayNBTTagList((NBTTagList)tag, player, depth+1);
		}
		else if(tag instanceof NBTTagByteArray){
			this.displayNBTTagByteArray(label, (NBTTagByteArray)tag, player, depth+1);
		}
		else if(tag instanceof NBTTagIntArray){
			this.displayNBTTagIntArray(label, (NBTTagIntArray)tag, player, depth+1);
		}
		else{
			String variableName = this.getOffset(depth)+(!label.trim().isEmpty()?label+": ":"- ");
			player.sendMessage(variableName+tag.asString());
		}
	}
	
	private void displayNBTTagCompound(NBTTagCompound compound, Player player, int depth){
		NBTBase tag;
		for(String key : compound.getKeys()){
			tag = compound.get(key);
			this.displayNBTTag(key,tag, player, depth+1);
		}
	}
	
	private void displayNBTTagList(NBTTagList list, Player player, int depth){
		NBTBase tag;
		for(int i = 0; i < list.size(); i++){
			tag = list.get(i);
			this.displayNBTTag(i+"", tag, player, depth+1);
		}
	}
	
	private void displayNBTTagByteArray(String label, NBTTagByteArray array, Player player, int depth){
		player.sendMessage(this.getOffset(depth)+label+": ["+StringUtils.join(array.getBytes(), ",")+"]");
	}
	
	private void displayNBTTagIntArray(String label, NBTTagIntArray array, Player player, int depth){
		player.sendMessage(this.getOffset(depth)+label+": ["+StringUtils.join(array.getInts(), ",")+"]");
	}
	
	private String getOffset(int depth){
		String result = "";
		for(int i = 0; i < depth; i++){
			result+= "  ";
		}
		return result;
	}
	
	private String getItemDisplayName(ItemStack itemStack){
		if(itemStack==null) return "AIR";
		String name;
		if(itemStack.hasItemMeta() && itemStack.getItemMeta().hasDisplayName()) name = itemStack.getItemMeta().getDisplayName();
		else name = itemStack.getType().name();
		if(itemStack.getAmount()>1) name+=" x"+itemStack.getAmount();
		return name;
	}
}
