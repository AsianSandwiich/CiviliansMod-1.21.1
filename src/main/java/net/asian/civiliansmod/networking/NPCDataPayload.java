package net.asian.civiliansmod.networking;

import net.asian.civiliansmod.CiviliansMod;
import net.asian.civiliansmod.entity.NPCEntity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import java.util.UUID;
import java.util.logging.Logger;

public record NPCDataPayload(UUID entityUuid, String customName, int variant) implements CustomPayload {
    public static final CustomPayload.Id<NPCDataPayload> ID = new CustomPayload.Id<>(Identifier.of(CiviliansMod.MOD_ID, "npc_data"));
    public static final PacketCodec<RegistryByteBuf, NPCDataPayload> CODEC = PacketCodec.tuple(
            Uuids.PACKET_CODEC, NPCDataPayload::entityUuid,
            PacketCodecs.STRING, NPCDataPayload::customName,
            PacketCodecs.INTEGER, NPCDataPayload::variant,
            NPCDataPayload::new
    );


    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }


    public void handlePacket(ServerPlayNetworking.Context context) {
        if (!(context.player().getWorld() instanceof ServerWorld world)) return;
        if (!(world.getEntity(this.entityUuid) instanceof NPCEntity entity)) return;

        entity.setVariant(this.variant);
        entity.setCustomName(Text.of(this.customName));

    }

}