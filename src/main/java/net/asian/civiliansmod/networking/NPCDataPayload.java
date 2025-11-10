package net.asian.civiliansmod.networking;

import net.asian.civiliansmod.entity.NPCEntity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;

import java.util.UUID;

public record NPCDataPayload(UUID npcUuid, String name, boolean paused, boolean following, boolean battleBuddy, float wanderRadius) implements CustomPayload {
    public static final CustomPayload.Id<NPCDataPayload> ID = new CustomPayload.Id<>(Identifier.of("civiliansmod", "npc_data"));

    public static final PacketCodec<RegistryByteBuf, NPCDataPayload> CODEC = PacketCodec.tuple(
            Uuids.PACKET_CODEC, NPCDataPayload::npcUuid,
            PacketCodecs.STRING, NPCDataPayload::name,
            PacketCodecs.BOOLEAN, NPCDataPayload::paused,
            PacketCodecs.BOOLEAN, NPCDataPayload::following,
            PacketCodecs.BOOLEAN, NPCDataPayload::battleBuddy,
            PacketCodecs.FLOAT, NPCDataPayload::wanderRadius,
            NPCDataPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static void handlePacket(NPCDataPayload payload, ServerPlayNetworking.Context context) {
        ServerPlayerEntity player = context.player();
        Entity entity = player.getWorld().getEntity(payload.npcUuid);
        if (entity instanceof NPCEntity npc) {
            npc.setCustomName(Text.literal(payload.name));
            npc.setPaused(payload.paused);
            npc.setFollowing(payload.following);
            npc.setBattleBuddy(payload.battleBuddy);
            if (payload.battleBuddy) {
                npc.setOwner(player);
            }
            npc.setWanderRadius(payload.wanderRadius);
        }
    }
}