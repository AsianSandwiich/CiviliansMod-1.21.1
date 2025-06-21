package net.asian.civiliansmod.networking;

import net.asian.civiliansmod.networking.payload.npc.NpcSpawnPayload;
import net.asian.civiliansmod.networking.payload.npc.dialogue.*;
import net.asian.civiliansmod.networking.payload.npc.skin.ChangeBaseSkinPayload;
import net.asian.civiliansmod.networking.payload.npc.skin.ChangeSkinPayload;
import net.asian.civiliansmod.networking.payload.npc.skin.ClientNpcSkinPayload;
import net.asian.civiliansmod.networking.payload.npc.skin.SyncSkinPayload;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.codec.PacketCodec;

public class NetworkPayloads {


    static {
        registerC2S(NPCDataPayload.ID, NPCDataPayload.CODEC);
        registerC2S(AddDialoguePayload.ID, AddDialoguePayload.CODEC);
        registerC2S(EditDialoguePayload.ID, EditDialoguePayload.CODEC);
        registerC2S(RemoveDialoguePayload.ID, RemoveDialoguePayload.CODEC);
        registerC2S(ChangeSkinPayload.ID, ChangeSkinPayload.CODEC);
        registerC2S(PlayerLanguagePayload.ID, PlayerLanguagePayload.CODEC);
        registerC2S(CilentDialogueSyncPayload.ID, CilentDialogueSyncPayload.CODEC);
        registerC2S(ChangeBaseSkinPayload.ID, ChangeBaseSkinPayload.CODEC);


        registerS2C(OpenScreenDialoguesPayload.ID, OpenScreenDialoguesPayload.CODEC);
        registerS2C(ClientNpcSkinPayload.ID, ClientNpcSkinPayload.CODEC);
        registerS2C(NpcSpawnPayload.ID, NpcSpawnPayload.CODEC);
        registerS2C(DialogueSyncPayload.ID, DialogueSyncPayload.CODEC);
        registerS2C(SyncSkinPayload.ID, SyncSkinPayload.CODEC);
    }


    private static <T extends CustomPayload> void registerC2S(CustomPayload.Id<T> packetIdentifier, PacketCodec<RegistryByteBuf, T> codec) {
        PayloadTypeRegistry.playC2S().register(packetIdentifier, codec);
    }
    private static <T extends CustomPayload> void registerS2C(CustomPayload.Id<T> packetIdentifier, PacketCodec<RegistryByteBuf, T> codec) {
        PayloadTypeRegistry.playS2C().register(packetIdentifier, codec);
    }

    public static void intialize() {

    }
}
