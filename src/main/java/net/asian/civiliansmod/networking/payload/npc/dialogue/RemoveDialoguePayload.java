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

public record RemoveDialoguePayload(UUID npcUuid, String language, String chatReason,
                                    String dialogue) implements CustomPayload {
    public static final CustomPayload.Id<RemoveDialoguePayload> ID = new CustomPayload.Id<>(Identifier.of(CiviliansMod.MOD_ID, "npc_dialogue_remove"));

    public static final PacketCodec<RegistryByteBuf, RemoveDialoguePayload> CODEC = PacketCodec.tuple(
            Uuids.PACKET_CODEC, RemoveDialoguePayload::npcUuid,
            PacketCodecs.STRING, RemoveDialoguePayload::language,
            PacketCodecs.STRING, RemoveDialoguePayload::chatReason,
            PacketCodecs.STRING, RemoveDialoguePayload::dialogue,
            RemoveDialoguePayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public void handlePacket(ServerPlayNetworking.Context context) {
        if (!(context.player().getWorld() instanceof ServerWorld world)) return;
        if (!(world.getEntity(this.npcUuid) instanceof NPCEntity entity)) return;
        entity.getChatHandler().getDialogues().computeIfAbsent(language, (i) -> new HashMap<>()).computeIfAbsent(NpcChat.ChatReason.valueOf(chatReason), (o) -> new ArrayList<>()).remove(dialogue);
        entity.getChatHandler().markDialoguesDirty(context.player().getUuid());
    }
}
