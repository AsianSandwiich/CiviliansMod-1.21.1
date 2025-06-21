package net.asian.civiliansmod.networking;

import net.asian.civiliansmod.networking.payload.npc.NpcSpawnPayload;
import net.asian.civiliansmod.networking.payload.npc.dialogue.*;
import net.asian.civiliansmod.networking.payload.npc.skin.ChangeBaseSkinPayload;
import net.asian.civiliansmod.networking.payload.npc.skin.ChangeSkinPayload;
import net.asian.civiliansmod.networking.payload.npc.skin.ClientNpcSkinPayload;
import net.asian.civiliansmod.networking.payload.npc.skin.SyncSkinPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class CustomS2CNetworking {
    static {
        ClientPlayNetworking.registerGlobalReceiver(OpenScreenDialoguesPayload.ID, OpenScreenDialoguesPayload::handlePacket);
        ClientPlayNetworking.registerGlobalReceiver(ClientNpcSkinPayload.ID, ClientNpcSkinPayload::handlePacket);
        ClientPlayNetworking.registerGlobalReceiver(SyncSkinPayload.ID, SyncSkinPayload::handlePacket);
        ClientPlayNetworking.registerGlobalReceiver(NpcSpawnPayload.ID, NpcSpawnPayload::handlePacket);
        ClientPlayNetworking.registerGlobalReceiver(DialogueSyncPayload.ID, DialogueSyncPayload::handlePacket);
    }
    public static void intialize() {
    }
}
