package net.asian.civiliansmod.networking.payload.npc.skin;

import net.asian.civiliansmod.CiviliansMod;
import net.asian.civiliansmod.entity.NPCEntity;
import net.asian.civiliansmod.util.NPCUtil;
import net.asian.civiliansmod.util.SkinIdentifier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ClientNpcSkinPayload(int npcId, boolean slim, byte[] skin) implements CustomPayload {
    public static final CustomPayload.Id<ClientNpcSkinPayload> ID = new CustomPayload.Id<>(Identifier.of(CiviliansMod.MOD_ID, "client_npc_skin_update"));

    public static final PacketCodec<RegistryByteBuf, ClientNpcSkinPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER, ClientNpcSkinPayload::npcId,
            PacketCodecs.BOOL, ClientNpcSkinPayload::slim,
            PacketCodecs.BYTE_ARRAY, ClientNpcSkinPayload::skin,
            ClientNpcSkinPayload::new
    );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }

    @Environment(EnvType.CLIENT)
    public void handlePacket(ClientPlayNetworking.Context context) {
        //if (skin.length != 16384) return;

        ClientWorld clientWorld = context.player().clientWorld;

        Entity entityById = clientWorld.getEntityById(this.npcId);
        System.out.println("p");

        try {
            NativeImage image = NativeImage.read(skin);
            if (image.getHeight() != 64 || image.getWidth() != 64) {
                return;
            }
            NativeImageBackedTexture dynamicTexture = new NativeImageBackedTexture(image);
            Identifier skinIdentifier = MinecraftClient.getInstance().getTextureManager().registerDynamicTexture(CiviliansMod.MOD_ID + "_custom_skin", dynamicTexture);

            image.close();
            SkinIdentifier skin1 = new SkinIdentifier(skinIdentifier, slim, true);

            if (entityById == null) {
                NPCUtil.waitingSync.put(npcId, skin1);
                return;
            }

            if (entityById instanceof NPCEntity npcEntity) {
                npcEntity.getSkinManager().setIdSkin(skin1);
            }
        } catch (Exception e) {
            CiviliansMod.LOGGER.error("error while converting skin files");
            e.printStackTrace();
        }
    }
}
