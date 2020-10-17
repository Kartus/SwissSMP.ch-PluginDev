package ch.swisssmp.fortressassault;

import java.io.IOException;

import org.bukkit.craftbukkit.v1_16_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;

import com.google.gson.JsonObject;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import net.minecraft.server.v1_16_R2.IChatBaseComponent;
import net.minecraft.server.v1_16_R2.PacketDataSerializer;
import net.minecraft.server.v1_16_R2.PacketPlayOutPlayerListHeaderFooter;
import net.minecraft.server.v1_16_R2.PlayerConnection;
import net.minecraft.server.v1_16_R2.IChatBaseComponent.ChatSerializer;

public class TabList {
	
	static void setHeaderFooter(Player player, String header_text, String footer_text) {
	     
        CraftPlayer craftplayer = (CraftPlayer) player;
        PlayerConnection connection = craftplayer.getHandle().playerConnection;
        JsonObject jsonHeader = new JsonObject();
        JsonObject jsonFooter = new JsonObject();
        jsonHeader.addProperty("text", header_text);
        jsonFooter.addProperty("text", footer_text);
        IChatBaseComponent header = ChatSerializer.a(jsonHeader.toString());
        IChatBaseComponent footer = ChatSerializer.a(jsonFooter.toString());
        ByteBuf byteBuffer = ByteBufAllocator.DEFAULT.buffer(header_text.getBytes().length + footer_text.getBytes().length);

        PacketDataSerializer packetDataSerializer = new PacketDataSerializer(byteBuffer);

        PacketPlayOutPlayerListHeaderFooter packet = new PacketPlayOutPlayerListHeaderFooter();

        try {
            packetDataSerializer.a(header);
            packetDataSerializer.a(footer);
            packet.a(packetDataSerializer);

        } catch (IOException e) {
            e.printStackTrace();
        }
        connection.sendPacket(packet);
    }
}
