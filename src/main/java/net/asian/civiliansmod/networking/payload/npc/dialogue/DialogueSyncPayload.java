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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
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
        if (!(world.getEntityById(this.npcId) instanceof NPCEntity)) {
            return;
        }
        NPCEntity entity = (NPCEntity) world.getEntityById(npcId);
        var type = new TypeToken<Map<String, Map<NpcChat.ChatReason, List<String>>>>() {}.getType();
        try {
            Map<String, Map<NpcChat.ChatReason, List<String>>> dialogueMap = new Gson().fromJson(decompress(info), type);
            entity.getChatManager().setDialogue(dialogueMap);
            entity.dialoguesReceived = true;
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
        byte[] data = Base64.getDecoder().decode(compressed);
        try (GZIPInputStream gzip = new GZIPInputStream(new ByteArrayInputStream(data))) {
            return new String(gzip.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
