package net.asian.civiliansmod.model;

import net.minecraft.client.model.*;
import net.minecraft.client.render.entity.model.PlayerEntityModel;

public class NPCModel extends PlayerEntityModel {

    public NPCModel(ModelPart root, boolean slim) {
        super(root, slim); // Automatically uses the slim parameter for arms
    }


    public static TexturedModelData getTexturedModelData(boolean slim) {
        ModelData modelData = new ModelData();
        ModelPartData root = modelData.getRoot();

        // Head part (including hat as a sibling)
        root.addChild(
                "head",
                ModelPartBuilder.create()




                        .uv(0, 0)
                        .cuboid(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F),
                ModelTransform.pivot(0.0F, 0.0F, 0.0F)
        );

        root.addChild(
                "hat",
                ModelPartBuilder.create()
                        .uv(32, 0)
                        .cuboid(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new Dilation(0.5F)),
                ModelTransform.pivot(0.0F, 0.0F, 0.0F)
        );

        // Ear and Cloak parts (empty placeholders for now)
        root.addChild(
                "ear",
                ModelPartBuilder.create()
                        .uv(24, 0)
                        .cuboid(-6.0F, -10.0F, -1.0F, 2.0F, 4.0F, 1.0F), // Vanilla ear geometry
                ModelTransform.pivot(0.0F, 0.0F, 0.0F)
        );
        root.addChild(
                "cloak",
                ModelPartBuilder.create()
                        .uv(0, 0)
                        .cuboid(-5.0F, 0.0F, -1.0F, 10.0F, 16.0F, 1.0F), // Vanilla cloak dimensions
                ModelTransform.pivot(0.0F, 0.0F, 0.0F)
        );

        // Body
        root.addChild(
                "body",
                ModelPartBuilder.create()
                        .uv(16, 16)
                        .cuboid(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F),
                ModelTransform.pivot(0.0F, 0.0F, 0.0F)
        );

        // Right Arm
        root.addChild(
                "right_arm",
                slim
                        ? ModelPartBuilder.create()
                        .uv(40, 16)
                        .cuboid(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F)
                        : ModelPartBuilder.create()
                        .uv(40, 16)
                        .cuboid(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                ModelTransform.pivot(-5.0F, 2.0F, 0.0F)
        );

        // Left Arm
        root.addChild(
                "left_arm",
                slim
                        ? ModelPartBuilder.create()
                        .uv(32, 48)
                        .cuboid(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F)
                        : ModelPartBuilder.create()
                        .uv(40, 16)
                        .mirrored()
                        .cuboid(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                ModelTransform.pivot(5.0F, 2.0F, 0.0F)
        );

        // Right Leg
        root.addChild(
                "right_leg",
                ModelPartBuilder.create()
                        .uv(0, 16)
                        .cuboid(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                ModelTransform.pivot(-1.9F, 12.0F, 0.0F)
        );

        // Left Leg
        root.addChild(
                "left_leg",
                ModelPartBuilder.create()
                        .uv(0, 16)
                        .mirrored()
                        .cuboid(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                ModelTransform.pivot(1.9F, 12.0F, 0.0F)
        );

        // Right and Left Sleeves
        root.addChild(
                "right_sleeve",
                slim
                        ? ModelPartBuilder.create()
                        .uv(40, 32)
                        .cuboid(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, new Dilation(0.25F))
                        : ModelPartBuilder.create()
                        .uv(40, 32)
                        .cuboid(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new Dilation(0.25F)),
                ModelTransform.pivot(-5.0F, 2.0F, 0.0F)
        );

        root.addChild(
                "left_sleeve",
                slim
                        ? ModelPartBuilder.create()
                        .uv(48, 48)
                        .cuboid(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, new Dilation(0.25F))
                        : ModelPartBuilder.create()
                        .uv(40, 32)
                        .mirrored()
                        .cuboid(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new Dilation(0.25F)),
                ModelTransform.pivot(5.0F, 2.0F, 0.0F)
        );

        // Pants
        root.addChild(
                "right_pants",
                ModelPartBuilder.create()
                        .uv(0, 48)
                        .cuboid(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new Dilation(0.25F)),
                ModelTransform.pivot(-1.9F, 12.0F, 0.0F)
        );

        root.addChild(
                "left_pants",
                ModelPartBuilder.create()
                        .uv(0, 48)
                        .mirrored()
                        .cuboid(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new Dilation(0.25F)),
                ModelTransform.pivot(1.9F, 12.0F, 0.0F)
        );

        // Jacket
        root.addChild(
                "jacket",
                ModelPartBuilder.create()
                        .uv(16, 32)
                        .cuboid(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new Dilation(0.25F)),
                ModelTransform.pivot(0.0F, 0.0F, 0.0F)
        );

        return TexturedModelData.of(modelData, 64, 64);
    }
}