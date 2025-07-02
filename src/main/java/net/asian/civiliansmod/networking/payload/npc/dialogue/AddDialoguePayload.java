package net.asian.civiliansmod.networking.payload.npc.dialogue;

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

public record AddDialoguePayload(UUID npcUuid, String chatReason, String language,
                                 String dialogue) implements CustomPayload {
    public static final CustomPayload.Id<AddDialoguePayload> ID = new CustomPayload.Id<>(Identifier.of(CiviliansMod.MOD_ID, "npc_dialogue_add"));

    public static final PacketCodec<RegistryByteBuf, AddDialoguePayload> CODEC = PacketCodec.tuple(
            Uuids.PACKET_CODEC, AddDialoguePayload::npcUuid,
            PacketCodecs.STRING, AddDialoguePayload::chatReason,
            PacketCodecs.STRING, AddDialoguePayload::language,
            PacketCodecs.STRING, AddDialoguePayload::dialogue,
            AddDialoguePayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public void handlePacket(ServerPlayNetworking.Context context) {
        if (!(context.player().getWorld() instanceof ServerWorld world)) return;
        if (!(world.getEntity(this.npcUuid) instanceof NPCEntity entity)) return;
        entity.getChatManager().getDialogues().computeIfAbsent(language, (i) -> new HashMap<>()).computeIfAbsent(NpcChat.ChatReason.valueOf(chatReason), (o) -> new ArrayList<>()).add(dialogue);
        entity.getChatManager().markDialoguesDirty(context.player().getUuid());
    }
}
