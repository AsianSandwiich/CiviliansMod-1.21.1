package net.asian.civiliansmod;

import net.asian.civiliansmod.util.NPCUtil;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.asian.civiliansmod.renderer.NPCRenderer;
import net.asian.civiliansmod.model.NPCModel;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;

public class CiviliansModClient implements ClientModInitializer {


    public static final EntityModelLayer WIDE_ENTITY_MODEL_LAYER =
            new EntityModelLayer(Identifier.of("civiliansmod", "npc_default"), "main");

    public static final EntityModelLayer SLIM_ENTITY_MODEL_LAYER =
            new EntityModelLayer(Identifier.of("civiliansmod", "npc_slim"), "main");

    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(CiviliansMod.NPC_ENTITY, NPCRenderer::new);

        EntityModelLayerRegistry.registerModelLayer(WIDE_ENTITY_MODEL_LAYER, () -> NPCModel.getTexturedModelData(false));

        EntityModelLayerRegistry.registerModelLayer(SLIM_ENTITY_MODEL_LAYER, () -> NPCModel.getTexturedModelData(true));


        //Since some libraries and minecraft methods are not registered during the client initializer,
        //we gather the textures when a client joins a server.
        ClientPlayConnectionEvents.INIT.register((phase, listener) -> {
                    NPCUtil.refreshTextures();
                }
        );
        CiviliansMod.LOGGER.info("[CiviliansMod] Model layers registered!");
    }
}