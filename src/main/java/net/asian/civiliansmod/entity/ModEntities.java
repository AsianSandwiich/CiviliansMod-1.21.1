package net.asian.civiliansmod.entity;

import net.asian.civiliansmod.CiviliansMod;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public class ModEntities {
    public static final EntityType<NPCEntity> NPC_ENTITY = register("npc", NPCEntity::new, SpawnGroup.CREATURE, 0.6f, 1.8f);


    public static <T extends Entity> EntityType<T> register(String name, EntityType.EntityFactory<T> factory, SpawnGroup spawngroup, float width, float height) {
        RegistryKey<EntityType<?>> key = RegistryKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of(CiviliansMod.MOD_ID, name));
        return Registry.register(Registries.ENTITY_TYPE, key, EntityType.Builder.<T>create(factory, spawngroup).dimensions(width, height).build(key));
    }
}
