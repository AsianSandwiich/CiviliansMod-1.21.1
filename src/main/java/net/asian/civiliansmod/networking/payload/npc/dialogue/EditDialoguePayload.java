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
import java.util.List;
import java.util.UUID;

public record EditDialoguePayload(UUID npcUuid, String language, String chatReason, int index,
                                  String newDialogue, boolean customMode) implements CustomPayload {
    public static final CustomPayload.Id<EditDialoguePayload> ID = new CustomPayload.Id<>(Identifier.of(CiviliansMod.MOD_ID, "npc_dialogue_edit"));

    public static final PacketCodec<RegistryByteBuf, EditDialoguePayload> CODEC = PacketCodec.tuple(
            Uuids.PACKET_CODEC, EditDialoguePayload::npcUuid,
            PacketCodecs.STRING, EditDialoguePayload::language,
            PacketCodecs.STRING, EditDialoguePayload::chatReason,
            PacketCodecs.INTEGER, EditDialoguePayload::index,
            PacketCodecs.STRING, EditDialoguePayload::newDialogue,
            PacketCodecs.BOOLEAN, EditDialoguePayload::customMode,
            EditDialoguePayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public void handlePacket(ServerPlayNetworking.Context context) {
        if (!(context.player().getWorld() instanceof ServerWorld world)) return;
        if (!(world.getEntity(this.npcUuid) instanceof NPCEntity entity)) return;

        String lang = this.language;
        NpcChat.ChatReason reason = NpcChat.ChatReason.valueOf(chatReason);
        if (customMode) {
            List<String> list = entity.getChatManager()
                    .getCustomDialogues()
                    .computeIfAbsent(reason, r -> new ArrayList<>());
            while (list.size() <= index) {
                list.add("...");
            }
            list.set(index, newDialogue);
        } else {
            entity.getChatManager()
                    .getDialogues()
                    .computeIfAbsent(lang, i -> new HashMap<>())
                    .computeIfAbsent(reason, o -> new ArrayList<>());

            List<String> list = entity.getChatManager()
                    .getDialogues()
                    .get(lang)
                    .get(reason);

            while (list.size() <= index) {
                list.add("...");
            }
            list.set(index, newDialogue);
        }

        entity.getChatManager().markDialoguesDirty(context.player().getUuid());
    }
}
