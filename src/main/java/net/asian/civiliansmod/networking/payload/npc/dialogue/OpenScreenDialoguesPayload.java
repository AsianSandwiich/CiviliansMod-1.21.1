package net.asian.civiliansmod.networking.payload.npc.dialogue;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import net.asian.civiliansmod.CiviliansMod;
import net.asian.civiliansmod.chat.NpcChat;
import net.asian.civiliansmod.entity.NPCEntity;
import net.asian.civiliansmod.gui.CustomChatScreen;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.*;

public record OpenScreenDialoguesPayload(int npcId, String dialogues) implements CustomPayload {
    public static final CustomPayload.Id<OpenScreenDialoguesPayload> ID = new CustomPayload.Id<>(Identifier.of(CiviliansMod.MOD_ID, "open_screen_dialogues"));

    public static final PacketCodec<RegistryByteBuf, OpenScreenDialoguesPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER, OpenScreenDialoguesPayload::npcId,
            PacketCodecs.STRING, OpenScreenDialoguesPayload::dialogues,
            OpenScreenDialoguesPayload::new
    );

    public OpenScreenDialoguesPayload(int npcId, Map<String, Map<NpcChat.ChatReason, List<String>>> dialogues) {
        this(
                npcId,
                new Gson().toJson(dialogues)
        );
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public void handlePacket(ClientPlayNetworking.Context context) {
        if (!(context.player().getWorld() instanceof World world)) return;
        Entity entity = world.getEntityById(this.npcId);
        if (!(entity instanceof NPCEntity npc)) {
            CiviliansMod.LOGGER.warn("[CiviliansMod] Entity with id {} is not an NPCEntity!", npcId);
            return;
        }

        var type = new TypeToken<Map<String, Map<NpcChat.ChatReason, List<String>>>>() {}.getType();
        Map<String, Map<NpcChat.ChatReason, List<String>>> dialogueMap = new Gson().fromJson(dialogues, type);

        npc.getChatManager().setDialogues(dialogueMap);
        npc.dialoguesReceived = true;

        CiviliansMod.LOGGER.info("[CiviliansMod] Dialogues received for NPC {}", npcId);

        MinecraftClient.getInstance().execute(() -> {
            if (MinecraftClient.getInstance().currentScreen instanceof CustomChatScreen screen) {
                CiviliansMod.LOGGER.info("[CiviliansMod] Initializing CustomChatScreen after dialogue sync for NPC {}", npcId);
                screen.fullInit(); // refresh entries
            } else {
                CiviliansMod.LOGGER.info("[CiviliansMod] Dialogues received but CustomChatScreen not open yet for NPC {}", npcId);
            }
        });
    }
}
