package net.asian.civiliansmod;

import net.asian.civiliansmod.entity.NPCEntity;
import net.asian.civiliansmod.networking.CustomC2SNetworking;
import net.asian.civiliansmod.networking.NetworkPayloads;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class CiviliansMod implements ModInitializer {

    public static final String MOD_ID = "civiliansmod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final EntityType<NPCEntity> NPC_ENTITY = Registry.register(
            Registries.ENTITY_TYPE,
            Identifier.of(MOD_ID, "npc"),
            EntityType.Builder
                    .create(NPCEntity::new, SpawnGroup.CREATURE)
                    .dimensions(0.6f, 1.8f)
                    .build()
    );

    @Override
    public void onInitialize() {


        FabricDefaultAttributeRegistry.register(NPC_ENTITY, NPCEntity.createAttributes());

        ModItems.registerModItems();

        NPCConversionHandler.register();

        NetworkPayloads.intialize();

        CustomC2SNetworking.intialize();





    }
}