package ch.swisssmp.hotchocolate;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import ch.swisssmp.customitems.CustomItemBuilder;
import ch.swisssmp.customitems.CustomItems;

public enum CustomMaterial {
	CHOCOLATE_POWDER,
	HOT_CHOCOLATE;
	
	public ItemStack getItemStack() {
		CustomItemBuilder itemBuilder = CustomItems.getCustomItemBuilder(this.toString());
		return itemBuilder.build();
	}
	
	public static CustomMaterial of(ItemStack itemStack) {
		String customEnum = CustomItems.getCustomEnum(itemStack);
		if(customEnum==null){
			return null;
		}
		try {
			return CustomMaterial.valueOf(customEnum.toUpperCase());
		}
		catch(Exception e) {
			return null;
		}
	}
}
