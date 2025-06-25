package net.asian.civiliansmod.model;

import net.asian.civiliansmod.renderer.NPCRenderState;
import net.minecraft.client.model.*;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.util.Util;
import net.minecraft.util.math.random.Random;

import java.util.List;

public class NPCModel extends BipedEntityModel<NPCRenderState> {


    private final List<ModelPart> parts;
    public final ModelPart leftSleeve;
    public final ModelPart rightSleeve;
    public final ModelPart leftPants;
    public final ModelPart rightPants;
    public final ModelPart jacket;
    private final boolean thinArms;

    public NPCModel(ModelPart modelPart, boolean bl) {
        super(modelPart, RenderLayer::getEntityTranslucent);
        this.thinArms = bl;
        this.leftSleeve = this.leftArm.getChild("left_sleeve");
        this.rightSleeve = this.rightArm.getChild("right_sleeve");
        this.leftPants = this.leftLeg.getChild("left_pants");
        this.rightPants = this.rightLeg.getChild("right_pants");
        this.jacket = this.body.getChild("jacket");
        this.parts = List.of(this.head, this.body, this.leftArm, this.rightArm, this.leftLeg, this.rightLeg);
    }

    public static ModelData getTexturedModelData(Dilation dilation, boolean bl) {
        ModelData modelData = BipedEntityModel.getModelData(dilation, 0.0F);
        ModelPartData modelPartData = modelData.getRoot();

        if (bl) { // Thin arms
            ModelPartData leftArm = modelPartData.addChild("left_arm",
                    ModelPartBuilder.create().uv(32, 48)
                            .cuboid(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, dilation),
                    ModelTransform.of(5.0F, 2.0F, 0.0F, 0.0F, 0.0F, 0.0F));

            ModelPartData rightArm = modelPartData.addChild("right_arm",
                    ModelPartBuilder.create().uv(40, 16)
                            .cuboid(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, dilation),
                    ModelTransform.of(-5.0F, 2.0F, 0.0F, 0.0F, 0.0F, 0.0F));

            leftArm.addChild("left_sleeve",
                    ModelPartBuilder.create().uv(48, 48)
                            .cuboid(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, dilation.add(0.25F)),
                    ModelTransform.NONE);

            rightArm.addChild("right_sleeve",
                    ModelPartBuilder.create().uv(40, 32)
                            .cuboid(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, dilation.add(0.25F)),
                    ModelTransform.NONE);
        } else { // Wide arms
            ModelPartData leftArm = modelPartData.addChild("left_arm",
                    ModelPartBuilder.create().uv(32, 48)
                            .cuboid(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, dilation),
                    ModelTransform.of(5.0F, 2.0F, 0.0F, 0.0F, 0.0F, 0.0F));

            ModelPartData rightArm = modelPartData.addChild("right_arm",
                    ModelPartBuilder.create().uv(40, 16)
                            .cuboid(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, dilation),
                    ModelTransform.of(-5.0F, 2.0F, 0.0F, 0.0F, 0.0F, 0.0F));

            leftArm.addChild("left_sleeve",
                    ModelPartBuilder.create().uv(48, 48)
                            .cuboid(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, dilation.add(0.25F)),
                    ModelTransform.NONE);

            rightArm.addChild("right_sleeve",
                    ModelPartBuilder.create().uv(40, 32)
                            .cuboid(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, dilation.add(0.25F)),
                    ModelTransform.NONE);
        }

        ModelPartData leftLeg = modelPartData.addChild("left_leg",
                ModelPartBuilder.create().uv(16, 48)
                        .cuboid(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, dilation),
                ModelTransform.of(1.9F, 12.0F, 0.0F, 0.0F, 0.0F, 0.0F));

        ModelPartData rightLeg = modelPartData.addChild("right_leg",
                ModelPartBuilder.create().uv(0, 16)
                        .cuboid(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, dilation),
                ModelTransform.of(-1.9F, 12.0F, 0.0F, 0.0F, 0.0F, 0.0F));

        leftLeg.addChild("left_pants",
                ModelPartBuilder.create().uv(0, 48)
                        .cuboid(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, dilation.add(0.25F)),
                ModelTransform.NONE);

        rightLeg.addChild("right_pants",
                ModelPartBuilder.create().uv(0, 32)
                        .cuboid(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, dilation.add(0.25F)),
                ModelTransform.NONE);

        ModelPartData body = modelPartData.getChild("body");
        body.addChild("jacket",
                ModelPartBuilder.create().uv(16, 32)
                        .cuboid(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, dilation.add(0.25F)),
                ModelTransform.NONE);

        return modelData;
    }

    public void setAngles(NPCRenderState playerEntityRenderState) {
        boolean bl = !playerEntityRenderState.spectator;
        this.body.visible = bl;
        this.rightArm.visible = bl;
        this.leftArm.visible = bl;
        this.rightLeg.visible = bl;
        this.leftLeg.visible = bl;
        this.hat.visible = playerEntityRenderState.hatVisible;
        this.jacket.visible = playerEntityRenderState.jacketVisible;
        this.leftPants.visible = playerEntityRenderState.leftPantsLegVisible;
        this.rightPants.visible = playerEntityRenderState.rightPantsLegVisible;
        this.leftSleeve.visible = playerEntityRenderState.leftSleeveVisible;
        this.rightSleeve.visible = playerEntityRenderState.rightSleeveVisible;
        super.setAngles(playerEntityRenderState);
    }

    public void setVisible(boolean bl) {
        super.setVisible(bl);
        this.leftSleeve.visible = bl;
        this.rightSleeve.visible = bl;
        this.leftPants.visible = bl;
        this.rightPants.visible = bl;
        this.jacket.visible = bl;
    }

    public ModelPart getRandomPart(Random random) {
        return (ModelPart) Util.getRandom(this.parts, random);
    }
}