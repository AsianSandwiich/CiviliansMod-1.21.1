package net.asian.civiliansmod;

import net.asian.civiliansmod.entity.ModEntities;
import net.asian.civiliansmod.entity.NPCEntity;
import net.asian.civiliansmod.networking.CustomC2SNetworking;
import net.asian.civiliansmod.networking.NetworkPayloads;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class CiviliansMod implements ModInitializer {

    public static final String MOD_ID = "civiliansmod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {


        FabricDefaultAttributeRegistry.register(ModEntities.NPC_ENTITY, NPCEntity.createAttributes());

        ModItems.registerModItems();

        NPCConversionHandler.register();

        NetworkPayloads.intialize();

        CustomC2SNetworking.intialize();





    }
}