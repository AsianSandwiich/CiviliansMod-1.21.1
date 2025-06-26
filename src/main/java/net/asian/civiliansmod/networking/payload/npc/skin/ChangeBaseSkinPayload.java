package net.asian.civiliansmod.networking.payload.npc.skin;

import net.asian.civiliansmod.CiviliansMod;
import net.asian.civiliansmod.entity.NPCEntity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;

import java.util.UUID;

public record ChangeBaseSkinPayload(UUID npcUuid, int baseVariant) implements CustomPayload {
    public static final CustomPayload.Id<ChangeBaseSkinPayload> ID = new CustomPayload.Id<>(Identifier.of(CiviliansMod.MOD_ID, "npc_base_skin_edit"));

    public static final PacketCodec<RegistryByteBuf, ChangeBaseSkinPayload> CODEC = PacketCodec.tuple(
            Uuids.PACKET_CODEC, ChangeBaseSkinPayload::npcUuid,
            PacketCodecs.INTEGER, ChangeBaseSkinPayload::baseVariant,
            ChangeBaseSkinPayload::new
    );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }

    public void handlePacket(ServerPlayNetworking.Context context) {
        if (!(context.player().getWorld() instanceof ServerWorld world)) return;
        if (!(world.getEntity(this.npcUuid) instanceof NPCEntity entity)) return;
        entity.getSkinManager().setBaseVariant(this.baseVariant);
        for (ServerPlayerEntity player : world.getPlayers()) {
            if (player.getUuid().equals(context.player().getUuid())) continue;

            ServerPlayNetworking.send(player, new SyncSkinPayload(entity.getId(),baseVariant));
        }
    }
}
