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

public record AddDialoguePayload(UUID npcUuid, String chatReason, String language, String dialogue, boolean customMode) implements CustomPayload {
    public static final CustomPayload.Id<AddDialoguePayload> ID = new CustomPayload.Id<>(Identifier.of(CiviliansMod.MOD_ID, "npc_dialogue_add"));

    public static final PacketCodec<RegistryByteBuf, AddDialoguePayload> CODEC = PacketCodec.tuple(
            Uuids.PACKET_CODEC, AddDialoguePayload::npcUuid,
            PacketCodecs.STRING, AddDialoguePayload::chatReason,
            PacketCodecs.STRING, AddDialoguePayload::language,
            PacketCodecs.STRING, AddDialoguePayload::dialogue,
            PacketCodecs.BOOLEAN, AddDialoguePayload::customMode,
            AddDialoguePayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public void handlePacket(ServerPlayNetworking.Context context) {
        if (!(context.player().getWorld() instanceof ServerWorld world)) return;
        if (!(world.getEntity(this.npcUuid) instanceof NPCEntity entity)) return;

        var chatManager = entity.getChatManager();
        NpcChat.ChatReason reason;

        try {
            reason = NpcChat.ChatReason.valueOf(chatReason);
        } catch (IllegalArgumentException e) {
            CiviliansMod.LOGGER.error("[CiviliansMod] Invalid ChatReason '{}' for NPC {}", chatReason, npcUuid, e);
            return;
        }

        if (customMode) {
            // Add to custom dialogues
            chatManager.getCustomDialogues()
                    .computeIfAbsent(reason, r -> new ArrayList<>())
                    .add(dialogue);
            CiviliansMod.LOGGER.info("[CiviliansMod] Added custom dialogue '{}' for NPC {} [{}]", dialogue, npcUuid, reason);

        } else {
            // Add to normal dialogues for the given language
            chatManager.getDialogues()
                    .computeIfAbsent(language, l -> new HashMap<>())
                    .computeIfAbsent(reason, r -> new ArrayList<>())
                    .add(dialogue);
            CiviliansMod.LOGGER.info("[CiviliansMod] Added dialogue '{}' for NPC {} [{} | lang={}]", dialogue, npcUuid, reason, language);

        }
        entity.getChatManager().markDialoguesDirty(context.player().getUuid());
    }
}