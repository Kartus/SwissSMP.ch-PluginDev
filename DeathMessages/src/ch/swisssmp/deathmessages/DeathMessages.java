package ch.swisssmp.deathmessages;

import ch.swisssmp.utils.JsonUtil;
import ch.swisssmp.utils.Random;
import ch.swisssmp.webcore.DataSource;
import ch.swisssmp.webcore.HTTPRequest;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class DeathMessages {
    private static final ArrayList<VanillaDeathMessage> vanillaDethMessages = new ArrayList<>();
    private static final ArrayList<CustomDeathMessage> customDeathMessages = new ArrayList<>();
    private static final String using = "using";

    public static void reload() {
        reload(null);
    }

    public static void reload(Consumer<String> sendResult) {
        HTTPRequest request = DataSource.getResponse(DeathMessagesPlugin.getInstance(), "messages.php");
        request.onFinish(() -> {
            JsonObject jsonResponse = request.getJsonResponse();
            if (jsonResponse == null || jsonResponse.isJsonNull()) {
                if (sendResult != null) {
                    sendResult.accept("Aktualisierung der Todesnachrichten fehlgeschlagen.");
                }
                return;
            }
            reload(sendResult, jsonResponse);
        });
    }

    public static String GetCustomDeathMessage(Player player, String oldMessage, String cause, String entity, String block, String killer) {
        Optional<VanillaDeathMessage> vanillaDeathMessageOpt = vanillaDethMessages.stream()
                .filter(msg -> oldMessage.contains(msg.signature))
                .filter(msg -> oldMessage.contains(using) == msg.message.contains(using))
                .sorted(Comparator.comparingInt(msg -> msg.message.length()))
                .findFirst();
        if (!vanillaDeathMessageOpt.isPresent())
            return oldMessage;
        VanillaDeathMessage vanillaDeathMessage = vanillaDeathMessageOpt.get();

        CustomDeathMessage randomMessage = getRandomMatchingCustomDeathMessage(cause, entity, block, vanillaDeathMessage);
        if (randomMessage == null)
            return oldMessage;

        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(vanillaDeathMessage.mask);
        if (element == null)
            return oldMessage;
        JsonArray array = element.getAsJsonArray();
        if (array == null)
            return oldMessage;

        String vanillaMessage = vanillaDeathMessage.message;
        String customMessage = randomMessage.message;

        ArrayList<String> masks = new ArrayList<>();
        for (JsonElement maskElement : array) {
            masks.add(maskElement.getAsString());
        }
        if (masks.isEmpty())
            return oldMessage;

        for (String mask : masks) {
            vanillaMessage = vanillaMessage.replace(mask, ";");
        }
        String oldCopy = oldMessage;
        String[] splitVanillaMessage = vanillaMessage.split(";");
        for (String splitVanillaMessagePart : splitVanillaMessage) {
            if (splitVanillaMessagePart.isEmpty())
                continue;
            oldCopy = oldCopy.replace(splitVanillaMessagePart, "\n");
        }
        String[] splitOldMessage = oldCopy.split("\n");
        for (String maskElement : masks) {
            String maskInsert;
            if ("{Player}".equals(maskElement)) {
                maskInsert = player.getDisplayName();
            } else if (("{Mob}".equals(maskElement) || "{Killer}".equals(maskElement)) && killer != null && !killer.isEmpty()) {
                maskInsert = killer;
            } else {
                maskInsert = splitOldMessage[masks.indexOf(maskElement)];
            }
            customMessage = customMessage.replace(maskElement, maskInsert);
        }
        return customMessage;
    }

    private static CustomDeathMessage getRandomMatchingCustomDeathMessage(String cause, String entity, String block, VanillaDeathMessage vanillaDeathMessage) {
        Random r = new Random();
        List<CustomDeathMessage> matchingCustomDeathMessages = customDeathMessages.stream()
                .filter(msg -> msg.vanillaId == vanillaDeathMessage.id)
                .filter(msg -> msg.cause == null || (msg.cause == null && cause == null) || (msg.cause != null && msg.cause.equals(cause)))
                .filter(msg -> msg.entity == null || (msg.entity == null && entity == null) || (msg.entity != null && msg.entity.equals(entity)))
                .filter(msg -> msg.block == null || (msg.block == null && block == null) || (msg.block != null && msg.block.equals(block)))
                .collect(Collectors.toList());
        if (matchingCustomDeathMessages == null || matchingCustomDeathMessages.isEmpty()) {
            Bukkit.getLogger().info("Keine passende Todesnachricht gefunden für Id:" + vanillaDeathMessage.id + ", cause:" + cause + ", entity:" + entity + ", block:" + block);
            return null;
        }
        for (CustomDeathMessage msg : matchingCustomDeathMessages) {
        }
        CustomDeathMessage message = matchingCustomDeathMessages.get(r.nextInt(matchingCustomDeathMessages.size()));
        return message;
    }

    private static void reload(Consumer<String> sendResult, JsonObject deathMessageData) {
        JsonArray rawVanillaDeathMessages = deathMessageData.getAsJsonArray("vanilla_messages");
        JsonArray rawCustomDeathMessages = deathMessageData.getAsJsonArray("messages");
        if (rawVanillaDeathMessages == null || rawCustomDeathMessages == null) {
            return;
        }

        vanillaDethMessages.clear();
        int vanillaCounter = 0;
        for (JsonElement rawVanillaDeathMessage : rawVanillaDeathMessages) {
            if (rawVanillaDeathMessage.isJsonObject()) {
                vanillaDethMessages.add(loadVanillaDeathMessage(rawVanillaDeathMessage.getAsJsonObject()));
                vanillaCounter++;
            }
        }
        customDeathMessages.clear();
        int customCounter = 0;
        for (JsonElement rawCustomDeathMessage : rawCustomDeathMessages) {
            if (rawCustomDeathMessage.isJsonObject()) {
                customDeathMessages.add(loadCustomDeathMessage(rawCustomDeathMessage.getAsJsonObject()));
                customCounter++;
            }
        }
        Bukkit.getLogger().info("Anzahl vanilla Todesnachrichten geladen: " + vanillaCounter);
        Bukkit.getLogger().info("Anzahl custom Todesnachrichten geladen: " + customCounter);
    }

    private static VanillaDeathMessage loadVanillaDeathMessage(JsonObject rawVanillaDeathMessage) {
        return new VanillaDeathMessage(
                JsonUtil.getInt("id", rawVanillaDeathMessage),
                JsonUtil.getString("message", rawVanillaDeathMessage),
                JsonUtil.getString("signature", rawVanillaDeathMessage),
                JsonUtil.getString("mask", rawVanillaDeathMessage));
    }

    private static CustomDeathMessage loadCustomDeathMessage(JsonObject rawCustomDeathMessage) {
        return new CustomDeathMessage(
                JsonUtil.getInt("deathmessage_id", rawCustomDeathMessage),
                JsonUtil.getString("entity", rawCustomDeathMessage),
                JsonUtil.getString("message", rawCustomDeathMessage),
                JsonUtil.getString("cause", rawCustomDeathMessage),
                JsonUtil.getString("block", rawCustomDeathMessage));
    }

    public static class VanillaDeathMessage {
        public int id;
        public String message;
        public String signature;
        public String mask;

        public VanillaDeathMessage(int id, String message, String signature, String mask) {
            this.id = id;
            this.message = message;
            this.signature = signature;
            this.mask = mask;
        }
    }

    public static class CustomDeathMessage {
        public int vanillaId;
        public String entity;
        public String message;
        public String cause;
        public String block;

        public CustomDeathMessage(int vanillaId, String entity, String message, String cause, String block) {
            this.vanillaId = vanillaId;
            this.entity = entity;
            this.message = message;
            this.cause = cause;
            this.block = block;
        }

        public String getCause() {
            return this.cause;
        }

        public String getMessage() {
            return this.message;
        }

        public String getEntity() {
            return this.entity;
        }

        public String getBlock() {
            return this.block;
        }

        public int getVanillaId() {
            return this.vanillaId;
        }

    }
}
