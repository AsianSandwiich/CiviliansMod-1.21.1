package net.asian.civiliansmod;

import net.asian.civiliansmod.entity.NPCEntity;
import net.asian.civiliansmod.networking.CustomC2SNetworking;
import net.asian.civiliansmod.networking.NetworkPayloads;
import net.asian.civiliansmod.networking.payload.npc.dialogue.OpenScreenDialoguesPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class CiviliansMod implements ModInitializer {

    public static final String MOD_ID = "civiliansmod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static Map<UUID, String> playerLanguages = new HashMap<>();

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
        LOGGER.info("Initializing CiviliansMod");

        FabricDefaultAttributeRegistry.register(NPC_ENTITY, NPCEntity.createAttributes());

        ModItems.registerModItems();

        NPCConversionHandler.register();

        NetworkPayloads.intialize();

        CustomC2SNetworking.intialize();

        EntityTrackingEvents.START_TRACKING.register((entity, player) -> {
            if (entity instanceof NPCEntity npc) {
                UUID playerId = player.getUuid();
                if (!npc.hasSentTo(playerId)) {
                    npc.markSentTo(playerId);
                    ServerPlayNetworking.send(player, new OpenScreenDialoguesPayload(npc.getId(), npc.getChatManager().getDialogues()));
                }
            }
        });

    }
}