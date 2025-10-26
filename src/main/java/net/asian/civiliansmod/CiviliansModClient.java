package net.asian.civiliansmod;

import net.asian.civiliansmod.chat.NpcChat;
import net.asian.civiliansmod.networking.CustomS2CNetworking;
import net.asian.civiliansmod.networking.PlayerLanguagePayload;
import net.asian.civiliansmod.util.FolderUtil;
import net.asian.civiliansmod.custom_skins.SkinFolderManager;
import net.asian.civiliansmod.entity.ModEntities;
import net.asian.civiliansmod.util.NPCUtil;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.asian.civiliansmod.renderer.NPCRenderer;
import net.asian.civiliansmod.model.NPCModel;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class CiviliansModClient implements ClientModInitializer {


    public static final EntityModelLayer WIDE_ENTITY_MODEL_LAYER =
            new EntityModelLayer(Identifier.of("civiliansmod", "npc_default"), "main");

    public static final EntityModelLayer SLIM_ENTITY_MODEL_LAYER =
            new EntityModelLayer(Identifier.of("civiliansmod", "npc_slim"), "main");

    @Override
    public void onInitializeClient() {
        // flashback compat
        try {
            // is flashback loaded?
            Class<?> flashbackClass = Class.forName("com.moulberry.flashback.Flashback");
            Object result = flashbackClass.getMethod("isInReplay").invoke(null);

            if (result instanceof Boolean && (Boolean) result) {
                CiviliansMod.LOGGER.info("[CiviliansMod] Flashback replay detected — initializing skins manually.");
                FolderUtil.init();
                SkinFolderManager.register();
                NPCUtil.refreshTextures();
            }
        } catch (ClassNotFoundException e) {
            // flashback is not given
        } catch (Throwable t) {
            CiviliansMod.LOGGER.warn("[CiviliansMod] Could not check Flashback replay state safely", t);
        }

        SkinFolderManager.register();

        EntityRendererRegistry.register(ModEntities.NPC_ENTITY, NPCRenderer::new);

        EntityModelLayerRegistry.registerModelLayer(WIDE_ENTITY_MODEL_LAYER, () -> TexturedModelData.of(NPCModel.getTexturedModelData( Dilation.NONE, false), 64, 64));

        EntityModelLayerRegistry.registerModelLayer(SLIM_ENTITY_MODEL_LAYER, () -> TexturedModelData.of(NPCModel.getTexturedModelData( Dilation.NONE, true), 64, 64));


        //Since some libraries and minecraft methods are not registered during the client initializer,
        //we gather the textures when a client joins a server.
        ClientPlayConnectionEvents.INIT.register((phase, listener) -> {
            FolderUtil.init();

            NPCUtil.refreshTextures();
            NpcChat.registerChat();
        });

        CustomS2CNetworking.intialize();

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            String lang = client.getLanguageManager().getLanguage();
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeString(lang);
            sender.sendPacket(new PlayerLanguagePayload(client.player.getUuid(), lang));
        });
        CiviliansMod.LOGGER.info("[CiviliansMod] Model layers registered!");
    }
}