package net.asian.civiliansmod.entity;

import net.asian.civiliansmod.entity.goal.CustomDoorGoal;
import net.asian.civiliansmod.gui.CustomNPCScreen;
import net.asian.civiliansmod.gui.DefaultNPCScreen;
import net.asian.civiliansmod.gui.SlimNPCScreen;
import net.asian.civiliansmod.util.NPCUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.client.MinecraftClient;

public class NPCEntity extends PathAwareEntity {

    private static final TrackedData<Integer> VARIANT = DataTracker.registerData(NPCEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Boolean> SLIM = DataTracker.registerData(NPCEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private float targetYaw = 0.0F; // The yaw to smoothly rotate towards
    private boolean isTurning = false; // Whether the NPC is currently in the process of turning
    private int lookAtPlayerTicks = 0;
    private static final TrackedData<Boolean> IS_PAUSED = DataTracker.registerData(NPCEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private int regenerationCooldown = 0;
    private static final TrackedData<Boolean> IS_FOLLOWING = DataTracker.registerData(NPCEntity.class, TrackedDataHandlerRegistry.BOOLEAN);


    public NPCEntity(EntityType<? extends PathAwareEntity> entityType, World world) {
        super(entityType, world);

        if (!this.getWorld().isClient) {

            int variant = this.random.nextInt(88);
            this.setVariant(variant);

            // Assign default model and slim model names to the entity
            String[] defaultModelNames = {"Charles", "Cade", "Henry", "Liam", "Rodney", "Nathaniel", "Elliot", "Julian", "Malcolm", "Tobias",
                    "Wesley", "Felix", "Desmond", "Simon", "Miles", "Everett", "Dorian", "Quentin", "Cedric", "Adrian", "Roman", "Marcus", "Gideon", "Levi", "Jasper"};
            String[] slimModelNames = {"Evelyn", "Sarah", "Olivia", "Emma", "Alexia", "Amelia", "Celeste", "Lillian", "Joleen", "Rosalie",
                    "Clara", "Vivienne", "Elena", "Margot", "Nora", "Daphne", "Fiona", "Genevieve", "Juliette", "Lucille", "Naomi", "Ivy", "Serena", "Vera", "Adelaide"};

            if (variant >= 0 && variant <= 43) {  // Default models: Variants 0, 1, 2
                String randomName = defaultModelNames[this.random.nextInt(defaultModelNames.length)];
                this.setCustomName(Text.literal(randomName));
                System.out.println("Assigned 'default' name: " + randomName + " to variant: " + variant);
            } else if (variant >= 44 && variant <= 87) {  // Slim models: Variants 3, 4, 5
                String randomName = slimModelNames[this.random.nextInt(slimModelNames.length)];
                this.setCustomName(Text.literal(randomName));
                System.out.println("Assigned 'slim' name: " + randomName + " to variant: " + variant);
            }

            this.setCustomNameVisible(true);  // Ensure name is visible
        }
    }


    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(VARIANT, 0);
        builder.add(SLIM, false);
        builder.add(IS_PAUSED, false);
        builder.add(IS_FOLLOWING, false);
    }

    public void setSlim(boolean slim) {
        this.dataTracker.set(SLIM, slim);
    }

    // Getter for the variant
    public int getVariant() {
        return this.dataTracker.get(VARIANT);
    }

    // Setter for the variant
    public void setVariant(int variant) {
        this.dataTracker.set(VARIANT, variant);
    }

    public void setCustomName(Text name) {
        super.setCustomName(name); // Call super to update name
        // This ensures the name change is tracked and saved
    }

    // Helper method to determine if the current variant is slim
    public boolean isSlim() {
        return this.dataTracker.get(SLIM);
    }

    @Override
    public boolean cannotDespawn() {
        return true;
    }

    public boolean isFollowing() {
        return this.dataTracker.get(IS_FOLLOWING);
    }

    public void setFollowing(boolean following) {
        this.dataTracker.set(IS_FOLLOWING, following);
    }

    public boolean isPaused() {
        return this.dataTracker.get(IS_PAUSED);
    }

    public void setPaused(boolean paused) {
        this.dataTracker.set(IS_PAUSED, paused);
    }

    public Identifier getSkinTexture() {
        return NPCUtil.getNPCTexture(getVariant());
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);

        // Save the variant to NBT
        nbt.putInt("Variant", this.getVariant());
        nbt.putBoolean("IsPaused", this.isPaused());
        nbt.putBoolean("IsFollowing", this.isFollowing());

    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt); // Call parent to load standard entity data
        if (nbt.contains("Variant")) {
            this.setVariant(nbt.getInt("Variant")); // Load custom variant from NBT

        }
        if (nbt.contains("IsPaused")) {
            this.setPaused(nbt.getBoolean("IsPaused")); // Load paused state
        }
        if (nbt.contains("IsFollowing")) {
            this.setFollowing(nbt.getBoolean("IsFollowing"));
        }

    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return PathAwareEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 20.0) // 20HP
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3); // Normal speed
    }

    @Override
    protected void initGoals() {
        super.initGoals();
        this.goalSelector.add(1, new WanderAroundFarGoal(this, 0.7));
        this.goalSelector.add(6, new CustomDoorGoal(this));
        this.goalSelector.add(4, new LookAroundGoal(this));
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        boolean hurt = super.damage(source, amount);

        if (hurt && source.getAttacker() != null) {
            if (!this.getWorld().isClient()) {
                Text nameText = this.getCustomName();
                String npcName = nameText != null ? nameText.getString() : "NPC";

                String[] hitDialogues = {
                        "Ouch! That hurt!",
                        "Hey, watch it!",
                        "Why would you do that?!",
                        "Stop hitting me!",
                        "What’s wrong with you?",
                        "Please, don’t hurt me!",
                        "What have I done to deserve this?!",
                        "Fight me fair and square!",
                        "Watch it pal, you don't know who you're messing with.",
                        "Ow!",
                        "GET AWAY FROM ME!",
                        "Why must this world cast unfortunate events upon me!",
                        "Hey...please stop I have already had a long day.",
                        "IF ONLY THERE WAS A HERO WHO COULD SAVE ME!",
                        "Lash your anger out on the sheep, not me!",
                        "I'm so sorry, I'm so sorry!",
                        "The prophecy foretold you would do this.",
                        "Friends shouldn't hurt other friends!",
                        "@$%#&!!"
                };

                String hitDialogue = hitDialogues[this.random.nextInt(hitDialogues.length)];

                if (source.getAttacker() instanceof PlayerEntity player) {
                    player.sendMessage(Text.literal(npcName + ": " + hitDialogue));
                }

                double dx = this.getX() - source.getAttacker().getX();
                double dz = this.getZ() - source.getAttacker().getZ();
                double fleeDistance = 12.0;

                this.getNavigation().startMovingTo(
                        this.getX() + dx * fleeDistance,
                        this.getY(),
                        this.getZ() + dz * fleeDistance,
                        1.2
                );
            }
        }

        return hurt;
    }

    @Override
    protected ActionResult interactMob(PlayerEntity player, Hand hand) {
        // Ensure the interaction is in the main hand
        if (hand == Hand.MAIN_HAND) {
            // Check if the player is holding a lead
            ItemStack heldItem = player.getStackInHand(hand);
            if (heldItem.isOf(Items.LEAD) && !this.hasPassengers()) {
                // Leash the NPC to the player if not already leashed
                if (!this.getWorld().isClient()) {
                    if (this.canBeLeashedBy(player)) {
                        this.attachLeash(player, true);
                        return ActionResult.SUCCESS;
                    }
                }
            }

            // Check if the player is sneaking
            if (player.isSneaking()) {
                if (!this.getWorld().isClient()) {
                    // Initiate turning to face the player
                    this.getNavigation().stop();

                    double dx = player.getX() - this.getX();
                    double dz = player.getZ() - this.getZ();
                    targetYaw = (float) (Math.atan2(dz, dx) * (180F / Math.PI)) - 90F;
                    isTurning = true;
                    this.lookAtPlayerTicks = 170; // NPC will look at the player for 5 seconds (170 ticks)

                    return ActionResult.SUCCESS;
                } else {
                    // Open the GUI on the client side
                    openCustomNPCScreen();
                }
                return ActionResult.SUCCESS; // Indicate the interaction was handled
            }

            // (Optional) Normal interaction behavior if not sneaking
            if (!this.getWorld().isClient()) {
                this.getNavigation().stop();

                double dx = player.getX() - this.getX();
                double dz = player.getZ() - this.getZ();
                targetYaw = (float) (Math.atan2(dz, dx) * (180F / Math.PI)) - 90F;
                isTurning = true;
                this.lookAtPlayerTicks = 60;
                Text nameText = this.getCustomName();
                String npcName = nameText != null ? nameText.getString() : "NPC";

                String[] dialogues = {
                        "Hello there, traveler! How can I help you?",
                        "I hope you're enjoying the day.",
                        "Stay safe—the world is dangerous.",
                        "There's treasure hidden nearby... or so I've heard.",
                        "Don't forget to stay out of trouble!",
                        "I'm here to help you, traveler.",
                        "What can I do for you?",
                        "I'm so hungry... Got any spare food?",
                        "I need to get my eyes checked, everything looks pixelated!",
                        "Sometimes it feels like I'm in a dream. I'm not sure what to do.",
                        "Hey! Can I help you something traveler?",
                        "Some would say the world is flat... can you believe that?",
                        "I don't have time to talk right now, I'm sorry!",
                        "Wow you look totally awesome, I might copy your look!",
                        "I need to find the hidden treasure, rumors have it that it's somewhere around here.",
                        "I love this place, it's a lot of fun to be here!",
                        "I hope someone got rid of that scary dragon... I'm sure it's not here anymore.",
                        "Want to go hunting with me?",
                        "Have you seen my friend? He's a little bit of a troublemaker.",
                        "Hopefully this place doesn't get too crowded...",
                        "I am surprised to see there are not more people here...",
                        "I'm so happy to see you, traveler!",
                        "When the birds sing, I can't help but sing along too.",
                        "I feel this unforgiving anger built up in my body! MUST... MUST... STOP!",
                        "Oop! Excuse me, let me just squeeze past ya",
                        "Darkness consumes me...",
                        "I AM SO HAPPY TO SEE YOU AGAIN! I LOVE YOU!",
                        "Hey, you're doing great"
                };

                String dialogue = dialogues[this.random.nextInt(dialogues.length)];
                player.sendMessage(Text.literal(npcName + ": " + dialogue));
            }
            return ActionResult.SUCCESS;
        }

        // Delegate to superclass for other interactions
        return super.interactMob(player, hand);
    }

    @Environment(EnvType.CLIENT)
    private void openCustomNPCScreen() {
        if(NPCUtil.hasDefaultSkin(this.getVariant())){
            MinecraftClient.getInstance().setScreen(new DefaultNPCScreen(this));
        } else if (NPCUtil.hasSlimSkin(this.getVariant())) {
            MinecraftClient.getInstance().setScreen(new SlimNPCScreen(this));
        }else {
            MinecraftClient.getInstance().setScreen(new CustomNPCScreen(this));
        }
    }

    @Override
    public Vec3d getLeashOffset() {
        // Adjusted offset to attach the leash to the NPC's hips
        return new Vec3d(0.0, 0.9, 0.0); // Adjust the Y-axis offset as needed.
    }

    public boolean canBeLeashedBy(PlayerEntity player) {
        // Allow leashing only if the player is in survival or adventure mode
        return !this.isLeashed() && !player.isSneaking();
    }

    @Override
    public void tickMovement() {
        // Handle paused state
        if (isPaused()) {
            // Ensure NPC does not move while paused
            this.getNavigation().stop();
            this.setVelocity(0.0, 0.0, 0.0);

            // Make the NPC look at the nearest player within 5 blocks
            PlayerEntity nearestPlayer = this.getWorld().getClosestPlayer(this, 5.0);
            if (nearestPlayer != null) {
                // Calculate direction for looking at the player
                double dx = nearestPlayer.getX() - this.getX();
                double dy = nearestPlayer.getEyeY() - this.getEyeY(); // Adjust for eye level
                double dz = nearestPlayer.getZ() - this.getZ();
                double distance = Math.sqrt(dx * dx + dz * dz);

                // Calculate yaw and pitch for the NPC to face the player
                float targetYaw = (float) (Math.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0F; // Horizontal rotation
                float targetPitch = (float) -(Math.atan2(dy, distance) * (180.0 / Math.PI)); // Vertical rotation

                // Smoothly adjust the headYaw and pitch towards the target
                this.headYaw = adjustTowards(this.headYaw, targetYaw);
                this.setPitch(adjustTowards(this.getPitch(), targetPitch));
            }
            return; // Skip additional logic while paused
        }

        // Handle "Follow" state
        if (isFollowing() && this.getWorld() != null && !this.getWorld().isClient) {
            PlayerEntity nearestPlayer = this.getWorld().getClosestPlayer(this, 15); // Follow within a 10-block radius

            if (nearestPlayer != null) {
                double distanceToPlayer = this.squaredDistanceTo(nearestPlayer);

                // Check the distance and adjust behavior dynamically
                if (distanceToPlayer > 4.0 && distanceToPlayer < 400.0) { // Follow if distance > 2 blocks but < 20 blocks
                    double deltaX = nearestPlayer.getX() - this.getX();
                    nearestPlayer.getEyeY();
                    this.getEyeY();
                    double deltaZ = nearestPlayer.getZ() - this.getZ();

                    // Dynamically adjust the speed based on distance (faster speed if farther away)
                    double distance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
                    double followSpeed = Math.min(3.5, 0.35 + (distance / 10.0)); // Max speed capped at 2.5

                    // Start moving towards the player
                    this.getNavigation().startMovingTo(nearestPlayer, followSpeed);


                    if (distance > 16.0) {
                        this.refreshPositionAndAngles(nearestPlayer.getX() - deltaX / 2, nearestPlayer.getY(), nearestPlayer.getZ() - deltaZ / 2, this.getYaw(), this.getPitch());
                        this.getNavigation().stop(); // Prevent glitches just after teleportation
                    }
                } else if (distanceToPlayer <= 4.0) {
                    this.getNavigation().stop(); // NPC is close enough; stop moving
                }
            } else {
                // Stop moving if no player is nearby
                this.getNavigation().stop();
            }
        }

        // Additional movement logic like smooth turning
        super.tickMovement();

        // Handle smooth turning (called every tick)
        if (isTurning) {
            smoothTurnToTargetYaw();
        }

        // Countdown for "lookAtPlayerTicks" behavior
        if (this.lookAtPlayerTicks > 0) {
            this.lookAtPlayerTicks--; // Decrease look timer
            this.getNavigation().stop(); // Ensure NPC does not move while focusing
            this.setVelocity(0.0, 0.0, 0.0);
        }

        // Handle regeneration logic
        if (this.isAlive() && this.getHealth() < this.getMaxHealth()) {
            if (regenerationCooldown <= 0) {
                this.heal(1.0F); // Regenerate 1 health every cooldown reset
                regenerationCooldown = 20; // Reset cooldown (20 ticks = 1 second)
            } else {
                regenerationCooldown--; // Decrease regeneration cooldown every tick
            }
        }
    }

    // Smoothly adjust the current angle towards a target angle
    private float adjustTowards(float current, float target) {
        float delta = MathHelper.wrapDegrees(target - current);
        if (delta > (float) 5.0) {
            delta = (float) 5.0; // Cap the increase
        } else if (delta < -(float) 5.0) {
            delta = -(float) 5.0; // Cap the decrease
        }
        return current + delta; // Adjust current angle
    }

    private void smoothTurnToTargetYaw() {
        float turnRate = 7.5F; // Amount to rotate per tick (increase for faster turning)
        float yawDifference = wrapDegrees(targetYaw - this.getYaw()); // Calculate the difference to the target yaw

        // Stop turning if we're very close to the target yaw
        if (Math.abs(yawDifference) < 1.0F) {
            this.setYaw(targetYaw); // Snap to the target
            this.bodyYaw = targetYaw;
            this.headYaw = targetYaw;
            isTurning = false; // We’re done turning
        } else {
            // Apply only part of the rotation to smooth out the turn
            float yawAdjustment = Math.min(turnRate, Math.max(-turnRate, yawDifference));
            this.setYaw(this.getYaw() + yawAdjustment);
            this.bodyYaw = this.getYaw();
            this.headYaw = this.getYaw();
        }
    }

    private float wrapDegrees(float degrees) {
        while (degrees >= 180.0F) {
            degrees -= 360.0F;
        }
        while (degrees < -180.0F) {
            degrees += 360.0F;
        }
        return degrees;
    }
}