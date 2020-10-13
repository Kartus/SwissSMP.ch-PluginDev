package ch.swisssmp.random;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_16_R2.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import ch.swisssmp.utils.Mathf;
import net.minecraft.server.v1_16_R2.NBTTagCompound;

public class RandomItemCommand implements CommandExecutor{

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(args==null || args.length==0) return false;
		if(!(sender instanceof Player)){
			sender.sendMessage("[RandomItemAPI] Kann nur ingame verwendet werden.");
			return true;
		}
		Player player = (Player)sender;
		ItemStack itemStack = player.getInventory().getItemInMainHand();
		if(itemStack==null){
			sender.sendMessage("[RandomItemAPI] Nimm zuerst ein Item in deine Haupthand.");
		}
		net.minecraft.server.v1_16_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(itemStack);
		NBTTagCompound nbtTag = nmsStack.getTag();
		if(nbtTag==null) nbtTag = new NBTTagCompound();
		NBTTagCompound randomizeData = nbtTag.hasKey("randomize") ? nbtTag.getCompound("randomize") : new NBTTagCompound();
		try{
		switch(args[0].toLowerCase()){
		case "menge":
		case "count":
		case "amount":{
			if(args.length<3){
				if(randomizeData.hasKey("amount")) randomizeData.remove("amount");
				break;
			}
			int min = Math.max(1, Integer.parseInt(args[1]));
			int max = Math.max(1, Integer.parseInt(args[2]));
			if(min==max){
				if(randomizeData.hasKey("amount")) randomizeData.remove("amount");
			}
			else{
				NBTTagCompound randomizeAmount = new NBTTagCompound();
				randomizeAmount.setInt("min", Math.min(min,max));
				randomizeAmount.setInt("max", Math.max(min,max));
				randomizeData.set("amount", randomizeAmount);
			}
			break;
		}
		case "probability":
		case "wahrscheinlichkeit":
		case "chance":{
			if(args.length<2){
				if(randomizeData.hasKey("chance")) randomizeData.remove("chance");
				break;
			}
			String chanceString = args[1];
			if(chanceString.contains("%")) chanceString.replace("%","");
			double chance = Double.parseDouble(chanceString)*0.01;
			if(chance>=1){
				if(randomizeData.hasKey("chance")) randomizeData.remove("chance");
			}
			else{
				randomizeData.setDouble("chance", Mathf.clamp01(chance));
			}
			break;
		}
		case "haltbarkeit":
		case "durability":{
			if(args.length<3){
				if(randomizeData.hasKey("durability")) randomizeData.remove("durability");
				break;
			}
			int min = Math.max(0, Integer.parseInt(args[1]));
			int max = Math.max(0, Integer.parseInt(args[2]));
			if(min==max){
				if(randomizeData.hasKey("durability")) randomizeData.remove("durability");
			}
			else{
				NBTTagCompound randomizeDurability = new NBTTagCompound();
				randomizeDurability.setInt("min", Math.min(min,max));
				randomizeDurability.setInt("max", Math.max(min,max));
				randomizeData.set("durability", randomizeDurability);
			}
			break;
		}
		case "verzauberung":
		case "enchantment":
		case "enchantments":{
			Enchantment enchantment;
			if(args.length<3){
				if(randomizeData.hasKey("enchantments")) randomizeData.remove("enchantments");
				break;
			}
			else if(args.length<4){
				if(randomizeData.hasKey("enchantments")){
					enchantment = Enchantment.getByKey(NamespacedKey.minecraft(args[1]));
					if(enchantment==null){
						sender.sendMessage("[RandomItemAPI] Verzauberung '"+args[1]+"' nicht gefunden.");
						return true;
					}
					NBTTagCompound enchantmentsSection = randomizeData.getCompound("enchantments");
					NBTTagCompound enchantmentSection;
					List<String> indexes = new ArrayList<String>(enchantmentsSection.getKeys());
					Collections.reverse(indexes);
					for(String key : indexes){
						enchantmentSection = enchantmentsSection.getCompound(key);
						if(!enchantmentSection.getString("enchantment").equals(enchantment.getKey().getKey())) continue;
						enchantmentsSection.remove(key);
					}
					if(enchantmentsSection.getKeys().size()>0){
						randomizeData.set("enchantments", enchantmentsSection);
					}
					else{
						randomizeData.remove("enchantments");
					}
				}
				break;
			}
			int min;
			int max;
			double chance;
			if(args.length>4){
				min = Integer.parseInt(args[2]);
				max = Integer.parseInt(args[3]);
				chance = Double.parseDouble(args[4])*0.01;
				enchantment = Enchantment.getByKey(NamespacedKey.minecraft(args[1]));
				if(enchantment==null){
					sender.sendMessage("[RandomItemAPI] Verzauberung '"+args[1]+"' nicht gefunden.");
					return true;
				}
			}
			else{
				min = Integer.parseInt(args[1]);
				max = min;
				chance = Double.parseDouble(args[3]);
				enchantment = Enchantment.getByKey(NamespacedKey.minecraft(args[2]));
				if(enchantment==null){
					sender.sendMessage("[RandomItemAPI] Verzauberung '"+args[2]+"' nicht gefunden.");
					return true;
				}
			}
			NBTTagCompound enchantmentsSection = randomizeData.hasKey("enchantments") ? randomizeData.getCompound("enchantments") : new NBTTagCompound();
			if((min<=0 && max<= 0) || (min==max && chance>=1)){
				NBTTagCompound enchantmentSection;
				List<String> indexes = new ArrayList<String>(enchantmentsSection.getKeys());
				Collections.reverse(indexes);
				for(String key : indexes){
					enchantmentSection = enchantmentsSection.getCompound(key);
					if(!enchantmentSection.getString("enchantment").equals(enchantment.getKey().getKey())) continue;
					enchantmentsSection.remove(key);
				}
			}
			else{
				NBTTagCompound randomizeEnchantment = new NBTTagCompound();
				randomizeEnchantment.setInt("min", Math.max(1, Math.min(min,max)));
				randomizeEnchantment.setInt("max", Math.max(1, Math.max(min,max)));
				randomizeEnchantment.setDouble("chance", Mathf.clamp01(chance));
				randomizeEnchantment.setString("enchantment", enchantment.getKey().getKey());
				enchantmentsSection.set("enchantment_"+enchantmentsSection.getKeys().size(), randomizeEnchantment);
			}
			if(enchantmentsSection.getKeys().size()>0){
				randomizeData.set("enchantments", enchantmentsSection);
			}
			else if(randomizeData.hasKey("enchantments")) randomizeData.remove("enchantments");
			break;
		}
		case "damage":
		case "schaden":
		case "attack_damage":{
			if(args.length<3){
				if(randomizeData.hasKey("attack_damage")) randomizeData.remove("attack_damage");
				break;
			}
			double min = Math.max(0, Double.parseDouble(args[1]));
			double max = Math.max(0, Double.parseDouble(args[2]));
			if(min==max){
				if(randomizeData.hasKey("attack_damage")) randomizeData.remove("attack_damage");
			}
			else{
				String slot = args.length>3 ? args[3] : RandomItemUtil.getDefaultSlot(itemStack, "mainhand");
				if(!isValidSlot(slot)){
					sender.sendMessage("[RandomItemAPI] Ungültiger Slot '"+slot+"'");
					return true;
				}
				NBTTagCompound randomizeAttackDamage = new NBTTagCompound();
				randomizeAttackDamage.setDouble("min", Math.min(min,max));
				randomizeAttackDamage.setDouble("max", Math.max(min,max));
				randomizeAttackDamage.setString("slot", slot);
				randomizeData.set("attack_damage", randomizeAttackDamage);
			}
			break;
		}
		case "geschwindigkeit":
		case "speed":
		case "attack_speed":{
			if(args.length<3){
				if(randomizeData.hasKey("attack_speed")) randomizeData.remove("attack_speed");
				break;
			}
			double min = Math.max(0, Double.parseDouble(args[1]));
			double max = Math.max(0, Double.parseDouble(args[2]));
			if(min==max){
				if(randomizeData.hasKey("attack_speed")) randomizeData.remove("attack_speed");
			}
			else{
				String slot = args.length>3 ? args[3] : RandomItemUtil.getDefaultSlot(itemStack, "mainhand");
				if(!isValidSlot(slot)){
					sender.sendMessage("[RandomItemAPI] Ungültiger Slot '"+slot+"'");
					return true;
				}
				NBTTagCompound randomizeAttackSpeed = new NBTTagCompound();
				randomizeAttackSpeed.setDouble("min", Math.min(min,max));
				randomizeAttackSpeed.setDouble("max", Math.max(min,max));
				randomizeAttackSpeed.setString("slot", slot);
				randomizeData.set("attack_speed", randomizeAttackSpeed);
			}
			break;
		}
		case "rüstung":
		case "verteidigung":
		case "defense":
		case "armor":{
			if(args.length<3){
				if(randomizeData.hasKey("armor")) randomizeData.remove("armor");
				break;
			}
			double min = Math.max(0, Double.parseDouble(args[1]));
			double max = Math.max(0, Double.parseDouble(args[2]));
			if(min==max){
				if(randomizeData.hasKey("armor")) randomizeData.remove("armor");
			}
			else{
				String slot = args.length>3 ? args[3] : RandomItemUtil.getDefaultSlot(itemStack, "head");
				if(!isValidSlot(slot)){
					sender.sendMessage("[RandomItemAPI] Ungültiger Slot '"+slot+"'");
					return true;
				}
				NBTTagCompound randomizeArmor = new NBTTagCompound();
				randomizeArmor.setDouble("min", Math.min(min,max));
				randomizeArmor.setDouble("max", Math.max(min,max));
				randomizeArmor.setString("slot", slot);
				randomizeData.set("armor", randomizeArmor);
			}
			break;
		}
		case "härte":
		case "toughness":{
			if(args.length<3){
				if(randomizeData.hasKey("toughness")) randomizeData.remove("toughness");
				break;
			}
			double min = Math.max(0, Double.parseDouble(args[1]));
			double max = Math.max(0, Double.parseDouble(args[2]));
			if(min==max){
				if(randomizeData.hasKey("toughness")) randomizeData.remove("toughness");
			}
			else{
				String slot = args.length>3 ? args[3] : RandomItemUtil.getDefaultSlot(itemStack, "head");
				if(!isValidSlot(slot)){
					sender.sendMessage("[RandomItemAPI] Ungültiger Slot '"+slot+"'");
					return true;
				}
				NBTTagCompound randomizeToughness = new NBTTagCompound();
				randomizeToughness.setDouble("min", Math.min(min,max));
				randomizeToughness.setDouble("max", Math.max(min,max));
				randomizeToughness.setString("slot", slot);
				randomizeData.set("toughness", randomizeToughness);
			}
			break;
		}
		case "nichts":
		case "nothing":
		case "reset":
		case "clear":{
			for(String key : randomizeData.getKeys()){
				randomizeData.remove(key);
			}
			break;
		}
		default: return false;
		}
		}
		catch(Exception e){
			return false;
		}
		if(randomizeData.getKeys().size()>0){
			nbtTag.set("randomize", randomizeData);
		}
		else if(nbtTag.hasKey("randomize")){
			nbtTag.remove("randomize");
		}
		if(nbtTag.getKeys().size()>0){
			nmsStack.setTag(nbtTag);
		}
		else if(nmsStack.hasTag()) nmsStack.setTag(null);
		itemStack.setItemMeta(CraftItemStack.getItemMeta(nmsStack));
		RandomItemUtil.addRandomizeDescription(itemStack);
		return true;
	}
	private boolean isValidSlot(String slot){
		switch(slot){
		case "mainhand":
		case "offhand":
		case "head":
		case "chest":
		case "legs":
		case "feet": return true;
		default: return false;
		}
	}
}
