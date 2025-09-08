package net.asian.civiliansmod.networking.payload.npc;

import net.asian.civiliansmod.CiviliansMod;
import net.asian.civiliansmod.chat.NpcChat;
import net.asian.civiliansmod.entity.NPCEntity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public record NpcRequest(UUID npcId) implements CustomPayload {
    public static final CustomPayload.Id<NpcRequest> ID = new CustomPayload.Id<>(Identifier.of(CiviliansMod.MOD_ID, "npc_request"));

    public static final PacketCodec<RegistryByteBuf, NpcRequest> CODEC = PacketCodec.tuple(
            Uuids.PACKET_CODEC, NpcRequest::npcId,
            NpcRequest::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public void handlePacket(ServerPlayNetworking.Context context) {
        if (!(context.player().getWorld() instanceof ServerWorld world)) return;
        if (!(world.getEntity(this.npcId) instanceof NPCEntity entity)) return;

    }
}
