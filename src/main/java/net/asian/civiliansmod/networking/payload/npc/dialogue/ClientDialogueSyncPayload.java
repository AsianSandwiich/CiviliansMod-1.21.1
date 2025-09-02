package net.asian.civiliansmod.networking.payload.npc.dialogue;

import net.asian.civiliansmod.CiviliansMod;
import net.asian.civiliansmod.entity.NPCEntity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;

import java.util.UUID;

public record ClientDialogueSyncPayload(UUID npcUuid) implements CustomPayload {
    public static final Id<ClientDialogueSyncPayload> ID = new Id<>(Identifier.of(CiviliansMod.MOD_ID, "client_dialogue_sync_request"));

    public static final PacketCodec<RegistryByteBuf, ClientDialogueSyncPayload> CODEC = PacketCodec.tuple(
            Uuids.PACKET_CODEC, ClientDialogueSyncPayload::npcUuid,
            ClientDialogueSyncPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public void handlePacket(ServerPlayNetworking.Context context) {
        if (!(context.player().getWorld() instanceof ServerWorld world)) return;
        if (!(world.getEntity(this.npcUuid) instanceof NPCEntity entity)) return;
        try {
            ServerPlayNetworking.send(
                    context.player(),
                    new DialogueSyncPayload(
                            entity.getId(),
                            entity.getChatManager().getDialogues()
                    ));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
