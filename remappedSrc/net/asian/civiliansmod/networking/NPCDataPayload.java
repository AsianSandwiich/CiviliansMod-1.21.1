package net.asian.civiliansmod.networking;

import net.asian.civiliansmod.entity.NPCEntity;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;

import java.util.UUID;

public record NPCDataPayload(
        UUID npcUuid,
        String name,
        boolean paused,
        boolean following,
        boolean battleBuddy,
        float wanderRadius
) implements CustomPayload {
    
    public static final CustomPayload.Id<NPCDataPayload> ID = 
            new CustomPayload.Id<>(Identifier.of("civiliansmod", "npc_data"));
    
    public static final PacketCodec<PacketByteBuf, NPCDataPayload> CODEC = PacketCodec.tuple(
            Uuids.PACKET_CODEC, NPCDataPayload::npcUuid,
            PacketCodecs.STRING, NPCDataPayload::name,
            PacketCodecs.BOOL, NPCDataPayload::paused,
            PacketCodecs.BOOL, NPCDataPayload::following,
            PacketCodecs.BOOL, NPCDataPayload::battleBuddy,
            PacketCodecs.FLOAT, NPCDataPayload::wanderRadius,
            NPCDataPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public void handle(ServerPlayerEntity player) {
        Entity entity = player.getServerWorld().getEntity(this.npcUuid);
        if (entity instanceof NPCEntity npc) {
            npc.setCustomName(Text.literal(this.name));
            npc.setPaused(this.paused);
            npc.setFollowing(this.following);
            npc.setBattleBuddy(this.battleBuddy);
            if (this.battleBuddy) {
                npc.setOwner(player);
            }
            npc.setWanderRadius(this.wanderRadius);
        }
    }
}