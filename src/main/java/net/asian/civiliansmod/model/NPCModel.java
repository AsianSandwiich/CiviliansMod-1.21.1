package net.asian.civiliansmod.model;

import net.minecraft.client.model.*;
import net.minecraft.client.render.entity.model.PlayerEntityModel;

public class NPCModel extends PlayerEntityModel {

    public NPCModel(ModelPart root, boolean slim) {
        super(root, slim); // Automatically uses the slim parameter for arms
    }
}