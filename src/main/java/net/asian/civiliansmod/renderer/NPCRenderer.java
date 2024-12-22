package net.asian.civiliansmod.renderer;

import net.asian.civiliansmod.entity.NPCEntity;
import net.asian.civiliansmod.model.NPCModel;
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
        int variant = entity.getVariant();

        // Map variants to textures
        return switch (variant) {
            case 0 -> Identifier.of("civiliansmod", "textures/entity/npc/default/default_0.png");
            case 1 -> Identifier.of("civiliansmod", "textures/entity/npc/default/default_1.png");
            case 2 -> Identifier.of("civiliansmod", "textures/entity/npc/default/default_2.png");
            case 3 -> Identifier.of("civiliansmod", "textures/entity/npc/slim/slim_0.png");
            case 4 -> Identifier.of("civiliansmod", "textures/entity/npc/slim/slim_1.png");
            case 5 -> Identifier.of("civiliansmod", "textures/entity/npc/slim/slim_2.png");
            default -> Identifier.of("civiliansmod", "textures/entity/npc/default/default_0.png"); // Fallback
        };
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