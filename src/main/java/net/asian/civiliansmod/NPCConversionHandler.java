package net.asian.civiliansmod;

import net.asian.civiliansmod.entity.ModEntities;
import net.asian.civiliansmod.entity.NPCEntity;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;
import java.awt.Color;

public class NPCConversionHandler {
    public static void register() {
        UseEntityCallback.EVENT.register(NPCConversionHandler::onEntityInteract);
    }

    private static ActionResult onEntityInteract(net.minecraft.entity.player.PlayerEntity player, World world, Hand hand, Entity entity, EntityHitResult hitResult) {
        if (!world.isClient && hand == Hand.MAIN_HAND) {
            if (entity.getType() == EntityType.VILLAGER && entity instanceof VillagerEntity villager) {
                if (!player.isSneaking()) {
                    return ActionResult.PASS;
                }
                ItemStack heldItem = player.getStackInHand(hand);

                if (villager.getVillagerData().profession().getIdAsString().equals("minecraft:none")) {
                    if (heldItem.isOf(ModItems.NPC_TOTEM)) {
                        if (world instanceof ServerWorld serverWorld) {
                            try {
                                convertVillagerToNPC(villager, serverWorld, player);
                                if (!player.isCreative()) {
                                    heldItem.decrement(1);
                                }
                            } catch (Exception e) {
                                System.err.println("Error during NPC conversion: " + e.getMessage());
                                e.printStackTrace();
                            }
                            return ActionResult.SUCCESS;
                        }
                    }
                } else {
                    player.sendMessage(
                            Text.literal("This Villager is connected to a job site and cannot be converted."),
                            true
                    );
                }
            }
        }
        return ActionResult.PASS;
    }

    private static void convertVillagerToNPC(VillagerEntity villager, ServerWorld world, net.minecraft.entity.player.PlayerEntity player) {
        NPCEntity npcEntity = ModEntities.NPC_ENTITY.create(world, null);

        if (npcEntity != null) {
            npcEntity.refreshPositionAndAngles(villager.getX(), villager.getY(), villager.getZ(), villager.getYaw(), villager.getPitch());

            if (villager.hasCustomName()) {
                npcEntity.setCustomName(villager.getCustomName());
                npcEntity.setCustomNameVisible(villager.isCustomNameVisible());
            }

            spawnRainbowParticles(villager.getX(), villager.getY() + 1, villager.getZ(), world);

            world.playSound(
                    null,
                    villager.getBlockPos(),
                    ModSounds.NPC_CONVERSION_SOUND,
                    net.minecraft.sound.SoundCategory.PLAYERS,
                    1.0f,
                    1.0f
            );

            villager.discard();
            world.spawnEntity(npcEntity);
            player.sendMessage(
                    Text.literal("Villager has been successfully converted into an NPC!"),
                    true
            );
        } else {
            System.err.println("Failed to convert Villager to NPCEntity!");
        }
    }

    private static void spawnRainbowParticles(double x, double y, double z, ServerWorld world) {
        for (float hue = 0; hue <= 1; hue += 0.1f) {
            int color = Color.HSBtoRGB(hue, 1.0F, 1.0F);
            DustParticleEffect rainbowParticle = new DustParticleEffect(
                    color,
                    1.0F
            );

            world.spawnParticles(
                    rainbowParticle,
                    x, y, z,
                    10,
                    0.5, 0.5, 0.5,
                    0.01
            );
        }
    }
}