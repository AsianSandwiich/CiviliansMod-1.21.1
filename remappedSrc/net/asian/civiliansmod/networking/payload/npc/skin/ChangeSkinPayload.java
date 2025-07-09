package net.asian.civiliansmod.networking.payload.npc.skin;

import net.asian.civiliansmod.CiviliansMod;
import net.asian.civiliansmod.entity.NPCEntity;
import net.asian.civiliansmod.util.NPCUtil;
import net.asian.civiliansmod.util.SkinIdentifier;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public record ChangeSkinPayload(UUID npcUuid, boolean slim, byte[] skin) implements CustomPayload {
    public static final CustomPayload.Id<ChangeSkinPayload> ID = new CustomPayload.Id<>(Identifier.of(CiviliansMod.MOD_ID, "npc_skin_edit"));

    public static final PacketCodec<RegistryByteBuf, ChangeSkinPayload> CODEC = PacketCodec.tuple(
            Uuids.PACKET_CODEC, ChangeSkinPayload::npcUuid,
            PacketCodecs.BOOLEAN, ChangeSkinPayload::slim,
            PacketCodecs.BYTE_ARRAY, ChangeSkinPayload::skin,
            ChangeSkinPayload::new
    );

    public ChangeSkinPayload(UUID npcUuid, boolean slim, SkinIdentifier skinIdentifier) {
        this(
                npcUuid,
                slim,
                NPCUtil.images.get(skinIdentifier)
        );
    }

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }

    public void handlePacket(ServerPlayNetworking.Context context) {
        if (!(context.player().getWorld() instanceof ServerWorld world)) return;
        if (!(world.getEntity(this.npcUuid) instanceof NPCEntity entity)) return;
        //if (skin.length != 16384) return;
        entity.getSkinManager().setSkinByteArray(skin);
        for (ServerPlayerEntity player : world.getPlayers()) {
            if (player.getUuid().equals(context.player().getUuid())) continue;

            ServerPlayNetworking.send(player, new ClientNpcSkinPayload(entity.getId(), slim, skin));
        }
    }
}