package net.asian.civiliansmod;

import net.asian.civiliansmod.custom_skins.SkinFolderManager;
import net.asian.civiliansmod.entity.ModEntities;
import net.asian.civiliansmod.util.NPCUtil;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.asian.civiliansmod.renderer.NPCRenderer;
import net.asian.civiliansmod.model.NPCModel;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;

public class CiviliansModClient implements ClientModInitializer {


    public static final EntityModelLayer WIDE_ENTITY_MODEL_LAYER =
            new EntityModelLayer(Identifier.of("civiliansmod", "npc_default"), "main");

    public static final EntityModelLayer SLIM_ENTITY_MODEL_LAYER =
            new EntityModelLayer(Identifier.of("civiliansmod", "npc_slim"), "main");

    @Override
    public void onInitializeClient() {
        SkinFolderManager.register();

        EntityRendererRegistry.register(ModEntities.NPC_ENTITY, NPCRenderer::new);

        EntityModelLayerRegistry.registerModelLayer(WIDE_ENTITY_MODEL_LAYER, () -> TexturedModelData.of(NPCModel.getTexturedModelData( Dilation.NONE, false), 64, 64));

        EntityModelLayerRegistry.registerModelLayer(SLIM_ENTITY_MODEL_LAYER, () -> TexturedModelData.of(NPCModel.getTexturedModelData( Dilation.NONE, true), 64, 64));


        //Since some libraries and minecraft methods are not registered during the client initializer,
        //we gather the textures when a client joins a server.
        ClientPlayConnectionEvents.INIT.register((phase, listener) -> {
                    NPCUtil.refreshTextures();
                }
        );
        CiviliansMod.LOGGER.info("[CiviliansMod] Model layers registered!");
    }
}