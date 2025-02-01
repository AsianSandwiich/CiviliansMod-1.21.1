package net.asian.civiliansmod.renderer;

import net.asian.civiliansmod.custom_skins.SkinFolderManager;
import net.asian.civiliansmod.entity.NPCEntity;
import net.asian.civiliansmod.model.NPCModel;
import net.asian.civiliansmod.util.NPCUtil;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class NPCRenderer extends MobEntityRenderer<NPCEntity, NPCModel<NPCEntity>> {
    // Entity model layers for default and slim models
    public static final EntityModelLayer DEFAULT_ENTITY_MODEL_LAYER =
            new EntityModelLayer(Identifier.of("civiliansmod", "npc_default"), "main");
    public static final EntityModelLayer SLIM_ENTITY_MODEL_LAYER =
            new EntityModelLayer(Identifier.of("civiliansmod", "npc_slim"), "main");

    // Cached models for performance
    private final NPCModel<NPCEntity> defaultModel;
    private final NPCModel<NPCEntity> slimModel;

    // Constructor
    public NPCRenderer(EntityRendererFactory.Context context) {
        // Set the default model and shadow size
        super(context, new NPCModel<>(context.getPart(DEFAULT_ENTITY_MODEL_LAYER), false), 0.5F);

        // Cache both default and slim models for reuse
        this.defaultModel = new NPCModel<>(context.getPart(DEFAULT_ENTITY_MODEL_LAYER), false);
        this.slimModel = new NPCModel<>(context.getPart(SLIM_ENTITY_MODEL_LAYER), true);
    }

    /**
     * Dynamically assigns the appropriate texture based on the NPC's variant.
     */
    @Override
    public Identifier getTexture(NPCEntity entity) {
        /*int variant = entity.getVariant();
        String textureType;

        if (variant < 44) {
            textureType = "default";
        } else if (variant < 88) {
            textureType = "slim";
        } else {
            textureType = "custom";
        }
/*
        if (variant >= 88) {
            return SkinFolderManager.getCustomSkinTexture(variant);
        }

        return Identifier.of("civiliansmod", "textures/entity/npc/" + textureType + "/"
                + textureType + "_" + (variant % 44) + ".png")*/
        return entity.getSkinTexture();
    }

    /**
     * Adjusts the rendering model (default vs slim) dynamically based on the entity's variant.
     */
    @Override
    public void render(
            NPCEntity entity,
            float entityYaw,
            float partialTicks,
            MatrixStack matrices,
            net.minecraft.client.render.VertexConsumerProvider vertexConsumers,
            int light) {
        // Determine the model to use based on variant (slim = variants 3–5)
        this.model = entity.isSlim() ? slimModel : defaultModel;

        // Render the entity using the selected model
        super.render(entity, entityYaw, partialTicks, matrices, vertexConsumers, light);
    }

    /**
     * Scales the NPC entity slightly for both slim and default models.
     */
    @Override
    protected void scale(NPCEntity entity, MatrixStack matrices, float amount) {
        float scale = 0.945F; // Uniform scaling for consistency
        matrices.scale(scale, scale, scale);
        super.scale(entity, matrices, amount);
    }
}