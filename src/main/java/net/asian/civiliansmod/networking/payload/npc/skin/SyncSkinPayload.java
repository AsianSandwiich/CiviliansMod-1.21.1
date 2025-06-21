package net.asian.civiliansmod.networking.payload.npc.skin;

import net.asian.civiliansmod.CiviliansMod;
import net.asian.civiliansmod.entity.NPCEntity;
import net.asian.civiliansmod.util.NPCUtil;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record SyncSkinPayload(int npcId, int id) implements CustomPayload {
    public static final CustomPayload.Id<SyncSkinPayload> ID = new CustomPayload.Id<>(Identifier.of(CiviliansMod.MOD_ID, "sync_skin_payload"));

    public static final PacketCodec<RegistryByteBuf, SyncSkinPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER, SyncSkinPayload::npcId,
            PacketCodecs.INTEGER, SyncSkinPayload::id,
            SyncSkinPayload::new
    );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }

    public void handlePacket(ClientPlayNetworking.Context context) {
        ClientWorld clientWorld = context.player().clientWorld;
        Entity entityById = clientWorld.getEntityById(this.npcId);

        if (entityById == null) {
            System.out.println(id);
            NPCUtil.waitingSync.put(npcId, NPCUtil.getNPCTexture(id));
            System.out.println("a");
            return;
        }
        if(entityById instanceof NPCEntity npcEntity) {
            npcEntity.getSkinManager().setIdSkin(NPCUtil.getNPCTexture(id));
            System.out.println("o");
        }
    }
}
