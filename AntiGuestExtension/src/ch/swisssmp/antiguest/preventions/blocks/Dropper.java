package ch.swisssmp.antiguest.preventions.blocks;

import org.bukkit.Material;
import org.bukkit.block.Block;

public class Dropper extends BlockInteractPrevention {

	@Override
	protected boolean isMatch(Block block) {
		return block.getState() instanceof org.bukkit.block.Dropper;
	}

	@Override
	protected String getSubPermission() {
		return "dropper";
	}
}
