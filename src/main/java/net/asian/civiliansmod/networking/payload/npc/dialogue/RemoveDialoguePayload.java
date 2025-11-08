package net.asian.civiliansmod.networking.payload.npc.dialogue;

import net.asian.civiliansmod.CiviliansMod;
import net.asian.civiliansmod.chat.NpcChat;
import net.asian.civiliansmod.entity.NPCEntity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record RemoveDialoguePayload(UUID npcUuid, String language, String reason, String dialogue, boolean customMode) implements CustomPayload {
    public static final CustomPayload.Id<RemoveDialoguePayload> ID = new CustomPayload.Id<>(Identifier.of(CiviliansMod.MOD_ID, "remove_dialogue"));

    public static final PacketCodec<RegistryByteBuf, RemoveDialoguePayload> CODEC = PacketCodec.tuple(
            Uuids.PACKET_CODEC, RemoveDialoguePayload::npcUuid,
            PacketCodecs.STRING, RemoveDialoguePayload::language,
            PacketCodecs.STRING, RemoveDialoguePayload::reason,
            PacketCodecs.STRING, RemoveDialoguePayload::dialogue,
            PacketCodecs.BOOLEAN, RemoveDialoguePayload::customMode,
            RemoveDialoguePayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static void handlePacket(RemoveDialoguePayload payload, ServerPlayNetworking.Context context) {
        ServerPlayerEntity player = context.player();
        Entity entity = player.getWorld().getEntity(payload.npcUuid);
        if (!(entity instanceof NPCEntity npc)) return;

        NpcChat.ChatReason chatReason = null;
        for (NpcChat.ChatReason reason : NpcChat.ChatReason.values()) {
            if (reason.getName().equalsIgnoreCase(payload.reason)) {
                chatReason = reason;
                break;
            }
        }
        if (chatReason == null) {
            CiviliansMod.LOGGER.warn("[CiviliansMod] Unknown ChatReason '{}'", payload.reason);
            return;
        }

            if (payload.customMode) {
                // Remove from custom dialogues
                List<String> dialogues = npc.getChatManager()
                        .getCustomDialogues()
                        .computeIfAbsent(chatReason, (o) -> new ArrayList<>());
                dialogues.remove(payload.dialogue);
            } else {
                // Remove from language dialogues
                List<String> dialogues = npc.getChatManager()
                        .getTranslatedDialogues(payload.language)
                        .computeIfAbsent(chatReason, (o) -> new ArrayList<>());
                dialogues.remove(payload.dialogue);
            }

            npc.getChatManager().markDialoguesDirty(player.getUuid());
        }
    }