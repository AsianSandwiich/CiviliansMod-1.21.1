package net.asian.civiliansmod.networking.payload.npc;

import com.google.gson.Gson;
import net.asian.civiliansmod.CiviliansMod;
import net.asian.civiliansmod.chat.NpcChat;
import net.asian.civiliansmod.entity.NPCEntity;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public record NpcSpawnPayload(int npcId, String dialogues, byte[] skin) implements CustomPayload {
    public static final CustomPayload.Id<NpcSpawnPayload> ID = new CustomPayload.Id<>(Identifier.of(CiviliansMod.MOD_ID, "spawn_payload"));

    public static final PacketCodec<RegistryByteBuf, NpcSpawnPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER, NpcSpawnPayload::npcId,
            PacketCodecs.STRING, NpcSpawnPayload::dialogues,
            PacketCodecs.BYTE_ARRAY, NpcSpawnPayload::skin,
            NpcSpawnPayload::new
    );

    public NpcSpawnPayload(int npcUuid, Map<String, Map<NpcChat.ChatReason, List<String>>> info, Identifier texture) {
        this(
                npcUuid,
                new Gson().toJson(info),
                getBytesFromIdentifier(texture)
        );
    }

    public static byte[] getBytesFromIdentifier(Identifier identifier) {
        try (InputStream input = MinecraftClient.getInstance()
                .getResourceManager()
                .getResource(identifier)
                .orElseThrow(() -> new IOException("Resource not found: " + identifier))
                .getInputStream()) {

            return input.readAllBytes(); // Java 9+

        } catch (IOException e) {
            e.printStackTrace();
            return new byte[0];
        }
    }

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }

    public void handlePacket(ClientPlayNetworking.Context context) {
        World world = context.player().getWorld();
        if (!(world.getEntityById(this.npcId) instanceof NPCEntity entity)) return;
        if (skin.length != 16384) return;
        entity.getSkinManager().setSkinByteArray(skin);
    }
}
