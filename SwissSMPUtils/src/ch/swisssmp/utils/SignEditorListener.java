package ch.swisssmp.utils;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import net.minecraft.server.v1_16_R2.BlockPosition;

public final class SignEditorListener implements Listener{

	@EventHandler
	private void onPlayerInteract(PlayerInteractEvent event){
		if(event.getAction()!=Action.RIGHT_CLICK_BLOCK) return;
		if(event.getPlayer().getGameMode()!=GameMode.CREATIVE) return;
		if(event.getItem()==null || event.getItem().getType()!=Material.FEATHER) return;
		if(!(event.getClickedBlock().getState() instanceof Sign)) return;
		Sign s = (Sign) event.getClickedBlock().getState();
		Player p = event.getPlayer();
		Object tileEntity = Reflections.getDeclaredField(s,  "sign");
		Reflections.setDeclaredField(tileEntity, "isEditable", true);
		Reflections.setDeclaredField(tileEntity, Reflections.ver().startsWith("v1_7") ? "k" : "h", Reflections.getHandle(p));  
		Reflections.sendPacket(p,  Reflections.getPacket("PacketPlayOutOpenSignEditor", new BlockPosition(s.getX(), s.getY(), s.getZ())));
	}
}
