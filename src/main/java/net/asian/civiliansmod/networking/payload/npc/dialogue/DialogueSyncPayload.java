package net.asian.civiliansmod.networking.payload.npc.dialogue;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import net.asian.civiliansmod.CiviliansMod;
import net.asian.civiliansmod.chat.NpcChat;
import net.asian.civiliansmod.entity.NPCEntity;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.minecraft.client.MinecraftClient;
import net.asian.civiliansmod.gui.CustomChatScreen;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public record DialogueSyncPayload(int npcId, String info) implements CustomPayload {
    public static final CustomPayload.Id<DialogueSyncPayload> ID = new CustomPayload.Id<>(Identifier.of(CiviliansMod.MOD_ID, "dialogue_sync"));

    public static final PacketCodec<RegistryByteBuf, DialogueSyncPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER, DialogueSyncPayload::npcId,
            PacketCodecs.STRING, DialogueSyncPayload::info,
            DialogueSyncPayload::new
    );

    public DialogueSyncPayload(int npcUuid, Map<String, Map<NpcChat.ChatReason, List<String>>> info) throws IOException {
        this(
                npcUuid,
                compress(new Gson().toJson(info))
        );
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public void handlePacket(ClientPlayNetworking.Context context) {
        if (!(context.player().getWorld() instanceof World world)) return;
        if (!(world.getEntityById(this.npcId) instanceof NPCEntity entity)) {
            return;
        }
        var type = new TypeToken<Map<String, Map<NpcChat.ChatReason, List<String>>>>() {}.getType();
        try {
            Map<String, Map<NpcChat.ChatReason, List<String>>> dialogueMap = new Gson().fromJson(decompress(info), type);
            String clientLanguage = MinecraftClient.getInstance().getLanguageManager().getLanguage();
            Map<NpcChat.ChatReason, List<String>> dialoguesForLanguage = dialogueMap.get(clientLanguage);

            //fallback to en_us
            if (dialoguesForLanguage == null) {
                dialoguesForLanguage = dialogueMap.get("en_us");
                CiviliansMod.LOGGER.warn("[CiviliansMod] No dialogues for language {}, falling back to en_us", clientLanguage);
            }
            //fallback if en_us not available (only if error in the gen files)
            if (dialoguesForLanguage == null && !dialogueMap.isEmpty()) {
                dialoguesForLanguage = dialogueMap.values().iterator().next();
                CiviliansMod.LOGGER.warn("[CiviliansMod] No en_us dialogues, using first available language");
            }

            if (dialoguesForLanguage != null) {
                Map<String, Map<NpcChat.ChatReason, List<String>>> correctLanguage = new HashMap<>();
                correctLanguage.put(clientLanguage, dialoguesForLanguage);

                entity.getChatManager().setDialogues(correctLanguage);
                entity.dialoguesReceived = true;
                CiviliansMod.LOGGER.info("[CiviliansMod] Set {} dialogues for NPC {}", dialoguesForLanguage.size(), npcId);
            } else {
                CiviliansMod.LOGGER.error("[CiviliansMod] No dialogues available for NPC {}", npcId);
            }

            MinecraftClient client = MinecraftClient.getInstance();
            client.execute(() -> {
                // sync again later
                client.execute(() -> {
                    if (client.currentScreen instanceof CustomChatScreen screen) {
                        CiviliansMod.LOGGER.info("[CiviliansMod] Refreshing CustomChatScreen after dialogue sync for NPC " + npcId);
                        screen.fullInit();
                    }
                });
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String compress(String str) throws IOException {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        try (GZIPOutputStream gzip = new GZIPOutputStream(byteStream)) {
            gzip.write(str.getBytes(StandardCharsets.UTF_8));
        }
        return Base64.getEncoder().encodeToString(byteStream.toByteArray());
    }

    public static String decompress(String compressed) throws IOException {
        try {
        byte[] data = Base64.getDecoder().decode(compressed);
        try (GZIPInputStream gzip = new GZIPInputStream(new ByteArrayInputStream(data))) {
            return new String(gzip.readAllBytes(), StandardCharsets.UTF_8);
        }
        } catch (Exception e) {
            CiviliansMod.LOGGER.error("Decompression error", e);
            throw e;
        }
    }
}
