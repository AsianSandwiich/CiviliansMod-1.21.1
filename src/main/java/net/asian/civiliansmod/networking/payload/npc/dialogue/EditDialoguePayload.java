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

public record EditDialoguePayload(UUID npcUuid, String language, String chatReason, int index,
                                  String newDialogue) implements CustomPayload {
    public static final CustomPayload.Id<EditDialoguePayload> ID = new CustomPayload.Id<>(Identifier.of(CiviliansMod.MOD_ID, "npc_dialogue_edit"));

    public static final PacketCodec<RegistryByteBuf, EditDialoguePayload> CODEC = PacketCodec.tuple(
            Uuids.PACKET_CODEC, EditDialoguePayload::npcUuid,
            PacketCodecs.STRING, EditDialoguePayload::language,
            PacketCodecs.STRING, EditDialoguePayload::chatReason,
            PacketCodecs.INTEGER, EditDialoguePayload::index,
            PacketCodecs.STRING, EditDialoguePayload::newDialogue,
            EditDialoguePayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public void handlePacket(ServerPlayNetworking.Context context) {
        if (!(context.player().getWorld() instanceof ServerWorld world)) return;
        if (!(world.getEntity(this.npcUuid) instanceof NPCEntity entity)) return;
        entity.getChatHandler().getDialogues().computeIfAbsent(language, (i) -> new HashMap<>()).computeIfAbsent(NpcChat.ChatReason.valueOf(chatReason), (o) -> new ArrayList<>()).set(index, newDialogue);
        entity.getChatHandler().markDialoguesDirty(context.player().getUuid());
    }
}
