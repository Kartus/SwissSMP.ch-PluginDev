package ch.swisssmp.utils;

import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;

public class PluginSender implements ConsoleCommandSender {

	@Override
	public PermissionAttachment addAttachment(Plugin arg0) {
		return Bukkit.getConsoleSender().addAttachment(arg0);
	}

	@Override
	public PermissionAttachment addAttachment(Plugin arg0, int arg1) {
		return Bukkit.getConsoleSender().addAttachment(arg0, arg1);
	}

	@Override
	public PermissionAttachment addAttachment(Plugin arg0, String arg1, boolean arg2) {
		return Bukkit.getConsoleSender().addAttachment(arg0, arg1, arg2);
	}

	@Override
	public PermissionAttachment addAttachment(Plugin arg0, String arg1, boolean arg2, int arg3) {
		return Bukkit.getConsoleSender().addAttachment(arg0, arg1, arg2, arg3);
	}

	@Override
	public Set<PermissionAttachmentInfo> getEffectivePermissions() {
		return Bukkit.getConsoleSender().getEffectivePermissions();
	}

	@Override
	public boolean hasPermission(String arg0) {
		return Bukkit.getConsoleSender().hasPermission(arg0);
	}

	@Override
	public boolean hasPermission(Permission arg0) {
		return Bukkit.getConsoleSender().hasPermission(arg0);
	}

	@Override
	public boolean isPermissionSet(String arg0) {
		return Bukkit.getConsoleSender().isPermissionSet(arg0);
	}

	@Override
	public boolean isPermissionSet(Permission arg0) {
		return Bukkit.getConsoleSender().isPermissionSet(arg0);
	}

	@Override
	public void recalculatePermissions() {
		Bukkit.getConsoleSender().recalculatePermissions();
	}

	@Override
	public void removeAttachment(PermissionAttachment arg0) {
		Bukkit.getConsoleSender().removeAttachment(arg0);
	}

	@Override
	public boolean isOp() {
		return true;
	}

	@Override
	public void setOp(boolean arg0) {
		return;
	}

	@Override
	public String getName() {
		return "PluginSender";
	}

	@Override
	public Server getServer() {
		return Bukkit.getConsoleSender().getServer();
	}

	@Override
	public void sendMessage(String arg0) {
		//duly noted but ignored.
		return;
	}

	@Override
	public void sendMessage(String[] arg0) {
		//duly noted but ignored.
		return;
	}

	@Override
	public void sendMessage(UUID uuid, String s) {
		//duly noted but ignored.
		return;
	}

	@Override
	public void sendMessage(UUID uuid, String[] strings) {
		//duly noted but ignored.
		return;
	}

	@Override
	public Spigot spigot() {
		return Bukkit.getConsoleSender().spigot();
	}

	@Override
	public void abandonConversation(Conversation arg0) {
		Bukkit.getConsoleSender().abandonConversation(arg0);
	}

	@Override
	public void abandonConversation(Conversation arg0, ConversationAbandonedEvent arg1) {
		Bukkit.getConsoleSender().abandonConversation(arg0, arg1);
	}

	@Override
	public void acceptConversationInput(String arg0) {
		Bukkit.getConsoleSender().acceptConversationInput(arg0);
	}

	@Override
	public boolean beginConversation(Conversation arg0) {
		return Bukkit.getConsoleSender().beginConversation(arg0);
	}

	@Override
	public boolean isConversing() {
		return Bukkit.getConsoleSender().isConversing();
	}

	@Override
	public void sendRawMessage(String arg0) {
		//duly noted but ignored.
		return;
	}

	@Override
	public void sendRawMessage(UUID uuid, String s) {
		//duly noted but ignored.
		return;
	}

}
