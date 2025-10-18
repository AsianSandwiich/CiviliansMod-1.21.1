package net.asian.civiliansmod.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.asian.civiliansmod.CiviliansMod;
import net.asian.civiliansmod.chat.DefaultChat;
import net.asian.civiliansmod.chat.NpcChat;
import net.asian.civiliansmod.entity.goal.CustomDoorGoal;
import net.asian.civiliansmod.gui.CustomNPCScreen;
import net.asian.civiliansmod.gui.DefaultNPCScreen;
import net.asian.civiliansmod.gui.SlimNPCScreen;
import java.util.Arrays;
import net.asian.civiliansmod.networking.payload.npc.dialogue.ClientDialogueSyncPayload;
import net.asian.civiliansmod.networking.payload.npc.dialogue.DialogueSyncPayload;
import net.asian.civiliansmod.networking.payload.npc.dialogue.OpenScreenDialoguesPayload;
import net.asian.civiliansmod.networking.payload.npc.skin.ClientNpcSkinPayload;
import net.asian.civiliansmod.networking.payload.npc.skin.SyncSkinPayload;
import net.asian.civiliansmod.util.NPCUtil;
import net.asian.civiliansmod.util.SkinIdentifier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.*;
import java.util.function.BiConsumer;

public class NPCEntity extends PathAwareEntity {
    private float targetYaw = 0.0F; // The yaw to smoothly rotate towards
    private boolean isTurning = false; // Whether the NPC is currently in the process of turning
    private int lookAtPlayerTicks = 0;
    private static final TrackedData<Boolean> IS_PAUSED = DataTracker.registerData(NPCEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private int regenerationCooldown = 0;
    private static final TrackedData<Boolean> IS_FOLLOWING = DataTracker.registerData(NPCEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    int updateDialoguesTicks = 0;

    //@Environment(EnvType.SERVER)
    Set<UUID> sent;

    @Environment(EnvType.CLIENT)
    public boolean dialoguesReceived;

    public NameManager getNameManager() {
        return nameManager;
    }
    public SkinManager getSkinManager() {
        return skinManager;
    }
    public ChatManager getChatManager() { return chatManager; }

    NameManager nameManager = new NameManager(this);
    SkinManager skinManager = new SkinManager(this);
    ChatManager chatManager = new ChatManager(this);

    @Override
    public void onSpawnPacket(EntitySpawnS2CPacket packet) {
        super.onSpawnPacket(packet);
        SkinIdentifier skinIdentifier = NPCUtil.waitingSync.get(this.getId());
        if (skinIdentifier != null) {
            this.skinManager.setIdSkin(skinIdentifier);
        }

        updateDialoguesTicks = 10;
    }

    @Override
    public Packet<ClientPlayPacketListener> createSpawnPacket(EntityTrackerEntry entityTrackerEntry) {
        if (this.skinManager.skinByteArray == null) {
            for (ServerPlayerEntity player : Objects.requireNonNull(this.getWorld().getServer()).getPlayerManager().getPlayerList()) {
                ServerPlayNetworking.send(player, new SyncSkinPayload(this.getId(), this.skinManager.baseVariant));
            }
        } else {
            for (ServerPlayerEntity player : Objects.requireNonNull(this.getWorld().getServer()).getPlayerManager().getPlayerList()) {
                ServerPlayNetworking.send(player, new ClientNpcSkinPayload(this.getId(), this.skinManager.slim, this.skinManager.skinByteArray));
            }
        }
        return super.createSpawnPacket(entityTrackerEntry);
    }

    public NPCEntity(EntityType<? extends PathAwareEntity> entityType, World world) {
        super(entityType, world);


        if (!this.getWorld().isClient) {
            this.setCustomNameVisible(true);
            this.sent = new HashSet<>();
        } else {
            this.dialoguesReceived = false;
        }
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        this.chatManager = new ChatManager(this);
        super.initDataTracker(builder);
        builder.add(IS_PAUSED, false);
        builder.add(IS_FOLLOWING, false);
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

    @Override
    protected void writeCustomData(WriteView writeView) {
        super.writeCustomData(writeView);

        // Save the variant to NBT
        writeView.putBoolean("IsPaused", this.isPaused());
        writeView.putBoolean("IsFollowing", this.isFollowing());
        writeView.put("dialogues", Dialogues.CODEC, Dialogues.fromMap(chatManager.getDialogues()));
        this.skinManager.writeView(writeView);
    }

    @Override
    protected void readCustomData(ReadView readView) {
        super.readCustomData(readView);
        this.setPaused(readView.getBoolean("IsPaused", false));
        this.setFollowing(readView.getBoolean("IsFollowing", false));

        this.chatManager.setFromReadView(readView);

        this.skinManager.readNbt(readView);
    }


    public static DefaultAttributeContainer.Builder createAttributes() {
        return PathAwareEntity.createMobAttributes()
                .add(EntityAttributes.MAX_HEALTH, 20.0) // 20HP
                .add(EntityAttributes.MOVEMENT_SPEED, 0.3); // Adjusted speed
    }

    @Override
    protected void initGoals() {
        super.initGoals();
        this.goalSelector.add(1, new WanderAroundFarGoal(this, 0.7));
        this.goalSelector.add(6, new CustomDoorGoal(this));
        this.goalSelector.add(4, new LookAroundGoal(this));
    }

    @Override
    public boolean damage(ServerWorld world, DamageSource source, float amount) {
        // Check if the entity is in a "paused" state (custom logic)
        if (isPaused()) {
            return false; // Prevent damage and stop further processing
        }

        // Call the new `super.damage` method with the correct parameters
        boolean hurt = super.damage(world, source, amount);

        // If the entity was damaged and there is an attacker
        if (hurt && source.getAttacker() != null) {
            if (!world.isClient()) {
                Text nameText = this.getCustomName();
                String npcName = (nameText != null) ? nameText.getString() : "NPC";


                if (source.getAttacker() instanceof PlayerEntity player) {
                    String hitDialogue = chatManager.getRandomChat(CiviliansMod.playerLanguages.get(player.getUuid()), NpcChat.ChatReason.HURT);
                    player.sendMessage(Text.literal(npcName + ": " + hitDialogue), true);

                }

                // Define flee behavior: Calculate direction vector for fleeing
                double dx = this.getX() - source.getAttacker().getX();
                double dz = this.getZ() - source.getAttacker().getZ();
                double fleeDistance = 12.0;

                // Start moving the entity away from the attacker
                this.getNavigation().startMovingTo(
                        this.getX() + dx * fleeDistance,
                        this.getY(),
                        this.getZ() + dz * fleeDistance,
                        1.2 // Movement speed when fleeing
                );
            }
        }

        return hurt; // Return whether the entity was successfully damaged
    }

    @Override
    protected ActionResult interactMob(PlayerEntity player, Hand hand) {
        CiviliansMod.LOGGER.info("[CiviliansMod] This is the NPC: {}", this.getId());

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
                    // SERVER-SIDE: Handle dialogue payload and NPC behavior
                    this.getNavigation().stop();

                    double dx = player.getX() - this.getX();
                    double dz = player.getZ() - this.getZ();
                    targetYaw = (float) (Math.atan2(dz, dx) * (180F / Math.PI)) - 90F;
                    isTurning = true;
                    this.lookAtPlayerTicks = 60;

                    // Only send dialogue payload if not already sent
                    if (player instanceof ServerPlayerEntity serverPlayer && !hasSentTo(player.getUuid())) {
                        markSentTo(player.getUuid());
                        OpenScreenDialoguesPayload openScreenDialoguesPayload = new OpenScreenDialoguesPayload(this.getId(), this.chatManager.getDialogues());
                        ServerPlayNetworking.send(serverPlayer, openScreenDialoguesPayload);
                        CiviliansMod.LOGGER.info("[CiviliansMod] Sent dialogues for NPC {}", this.getId());
                    }

                    return ActionResult.SUCCESS;
                } else {
                    // CLIENT-SIDE: Only handle GUI opening when dialogues are received
                    // Remove the chat message here - it doesn't belong in sneak interaction
                    if (this.dialoguesReceived) {
                        CiviliansMod.LOGGER.info("[CiviliansMod] Opening GUI for NPC {}", this.getId());
                        openCustomNPCScreen();
                    } else {
                        CiviliansMod.LOGGER.warn("[CiviliansMod] No dialogues yet for NPC {}", this.getId());
                    }
                    return ActionResult.SUCCESS;
                }
            } else {
                // NORMAL INTERACTION (not sneaking) - Send chat message
                if (!this.getWorld().isClient()) {
                    this.getNavigation().stop();

                    double dx = player.getX() - this.getX();
                    double dz = player.getZ() - this.getZ();
                    targetYaw = (float) (Math.atan2(dz, dx) * (180F / Math.PI)) - 90F;
                    isTurning = true;
                    this.lookAtPlayerTicks = 60;

                    Text nameText = this.getCustomName();
                    String npcName = nameText != null ? nameText.getString() : "NPC";
                    String dialogue = chatManager.getRandomChat(CiviliansMod.playerLanguages.get(player.getUuid()), NpcChat.ChatReason.INTERACT);
                    player.sendMessage(Text.literal(npcName + ": " + dialogue), true);
                }
                return ActionResult.SUCCESS;
            }
        }
        // Delegate to superclass for other interactions
        return super.interactMob(player, hand);
    }

    @Environment(EnvType.CLIENT)
    public void openCustomNPCScreen() {
        if (skinManager.slim && skinManager.defaultSkin) {
            MinecraftClient.getInstance().setScreen(new SlimNPCScreen(this));
        } else if (skinManager.defaultSkin) {
            MinecraftClient.getInstance().setScreen(new DefaultNPCScreen(this));
        } else {
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
    public void tick() {
        super.tick();
        if (getWorld().isClient) {
            if (--updateDialoguesTicks == 0) {
                ClientPlayNetworking.send(new ClientDialogueSyncPayload(this.getUuid()));
            }
        }
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

    public boolean hasSentTo(UUID playerId) {
        return sent.contains(playerId);
    }

    public void markSentTo(UUID playerId) {
        sent.add(playerId);
    }

    public static class ChatManager {
        NPCEntity npc;
        Map<String, Map<NpcChat.ChatReason, List<String>>> dialogues;
        Map<NpcChat.ChatReason, List<String>> customDialogues = new EnumMap<>(NpcChat.ChatReason.class);

        public ChatManager(NPCEntity npc) {
            this.npc = npc;
            this.dialogues = new HashMap<>(DefaultChat.getDefaultChat());
        }

        public String getRandomChat(String language, NpcChat.ChatReason reason) {
            List<String> custom = customDialogues.get(reason);
            if (custom != null && !custom.isEmpty()) {
                return custom.get(Random.create().nextInt(custom.size()));
            }

            Map<NpcChat.ChatReason, List<String>> langDialogues = dialogues.get(language);
            if (langDialogues == null || langDialogues.isEmpty()) {
                CiviliansMod.LOGGER.warn("[CiviliansMod] No dialogues for language {}, falling back to en_us (NPC ID: {})",language , npc.getId());
                langDialogues = DefaultChat.getDefaultChat().get("en_us");
            }

            if (langDialogues == null && !dialogues.isEmpty()) {
                langDialogues = dialogues.values().iterator().next();
                CiviliansMod.LOGGER.warn("[CiviliansMod] No en_us dialogues, using first available language for NPC {}", npc.getId());
            }

            if (langDialogues == null) {
                CiviliansMod.LOGGER.warn("[CiviliansMod] No dialogues available at all, using placeholder for NPC {}", npc.getId());
                langDialogues = new EnumMap<>(NpcChat.ChatReason.class);
            }

            if (dialogues.isEmpty()) {
                CiviliansMod.LOGGER.error("[CiviliansMod] Dialogue map empty, loading default dialogues for NPC {}", npc.getId());
                dialogues.putAll(DefaultChat.getDefaultChat());
            }

            List<String> messages = langDialogues.getOrDefault(reason, Collections.singletonList("..."));
            if (messages.isEmpty()) {
                messages = Collections.singletonList("...");
            }
            return messages.get(Random.create().nextInt(messages.size()));
        }

        public Map<NpcChat.ChatReason, List<String>> getDialoguesForLanguage(String language) {
            //map for language
            Map<NpcChat.ChatReason, List<String>> languageMap = dialogues.get(language);

            // fallback if no lang
            if (languageMap == null || languageMap.isEmpty()) {
                CiviliansMod.LOGGER.warn("[CiviliansMod] No dialogues for language {}, falling back to en_us (NPC ID: {})", language, npc.getId());
                languageMap = DefaultChat.getDefaultChat().get("en_us");
            }
            //fallback if en_us is not available on error, use first other language
            if (languageMap == null && !dialogues.isEmpty()) {
                languageMap = dialogues.values().iterator().next();
                CiviliansMod.LOGGER.warn("[CiviliansMod] No en_us dialogues, using first available language for NPC {}", npc.getId());
            }
            //fallback if nothing works to DefaultChat
            if (languageMap == null) {
                CiviliansMod.LOGGER.warn("[CiviliansMod] No dialogues available at all, creating default placeholder map for NPC {}", npc.getId());
                languageMap = new EnumMap<>(NpcChat.ChatReason.class);
            }
            // get allways a placeholder list for reasons
            for (NpcChat.ChatReason reason : NpcChat.ChatReason.values()) {
                languageMap.computeIfAbsent(reason, r -> new ArrayList<>(Collections.singletonList("...")));
            }
            return languageMap;
        }

        public Map<NpcChat.ChatReason, List<String>> getTranslatedDialogues(String language) {
            return getDialoguesForLanguage(language);
        }

        public NbtCompound saveDialogues() {
            NbtCompound main = new NbtCompound();

            //save language dialogue
            NbtCompound langs = new NbtCompound();
            for (Map.Entry<String, Map<NpcChat.ChatReason, List<String>>> langEntry : dialogues.entrySet()) {
                NbtCompound langCompound = new NbtCompound();
                for (Map.Entry<NpcChat.ChatReason, List<String>> reasonEntry : langEntry.getValue().entrySet()) {
                    NbtList list = new NbtList();
                    for (String msg : reasonEntry.getValue()) {
                        list.add(NbtString.of(msg));
                    }
                    langCompound.put(reasonEntry.getKey().getName(), list);
                }
                langs.put(langEntry.getKey(), langCompound);
            }
            main.put("Languages", langs);

            //save custom dialogue
            NbtCompound customCompound = new NbtCompound();
            for (Map.Entry<NpcChat.ChatReason, List<String>> entry : customDialogues.entrySet()) {
                NbtList list = new NbtList();
                for (String msg : entry.getValue()) {
                    list.add(NbtString.of(msg));
                }
                customCompound.put(entry.getKey().getName(), list);
            }
            main.put("CustomDialogues", customCompound);

            return main;
        }

        public void setFromNbt(Optional<NbtCompound> nbtOptional) {
            if (nbtOptional.isEmpty()) {
                return;
            }

            dialogues.clear();
            customDialogues.clear();

            NbtCompound nbt = nbtOptional.get();
            Optional<NbtCompound> langsOpt = nbt.getCompound("Languages");
            if (langsOpt.isPresent()) {
                NbtCompound langs = langsOpt.get();

                for (String language : langs.getKeys()) {
                    Optional<NbtCompound> langCompoundOpt = langs.getCompound(language);
                    if (langCompoundOpt.isEmpty()) continue;
                    NbtCompound langCompound = langCompoundOpt.get();
                    Map<NpcChat.ChatReason, List<String>> reasonMap = new EnumMap<>(NpcChat.ChatReason.class);

                    for (NpcChat.ChatReason reason : NpcChat.ChatReason.values()) {
                        if (langCompound.contains(reason.getName())) {
                            Optional<NbtList> listOpt = langCompound.getList(reason.getName());
                            if (listOpt.isEmpty()) continue;
                            NbtList list = listOpt.get();
                            List<String> messages = new ArrayList<>();
                            list.forEach(e -> messages.add(e.asString().orElse("")));

                            reasonMap.put(reason, messages);
                        }
                    }

                    dialogues.put(language, reasonMap);
                }
            }


            Optional<NbtCompound> customOpt = nbt.getCompound("CustomDialogues");
            if (customOpt.isPresent()) {
                NbtCompound custom = customOpt.get();


                for (NpcChat.ChatReason reason : NpcChat.ChatReason.values()) {
                    if (custom.contains(reason.getName())) {
                        Optional<NbtList> listOpt = custom.getList(reason.getName());
                        if (listOpt.isEmpty()) continue;

                        NbtList list = listOpt.get();
                        List<String> messages = new ArrayList<>();
                        list.forEach(e -> messages.add(e.asString().orElse("")));
                        customDialogues.put(reason, messages);
                    }
                }
            }

            if (dialogues.isEmpty()) {
                dialogues.putAll(DefaultChat.getDefaultChat());
            }
        }

        public void setFromReadView(ReadView readView) {
            Optional<Dialogues> dialoguesOptional = readView.read("dialogues", Dialogues.CODEC);
            if (dialoguesOptional.isEmpty()) {
                // Fallback, falls nichts gespeichert war
                if (dialogues.isEmpty()) dialogues.putAll(DefaultChat.getDefaultChat());
                return;
            }

            Dialogues d = dialoguesOptional.get();
            Map<String, Map<NpcChat.ChatReason, List<String>>> chats = new HashMap<>();
            d.dialogues().forEach((lang, languageDialogue) -> {
                Map<NpcChat.ChatReason, List<String>> perReason = new EnumMap<>(NpcChat.ChatReason.class);
                languageDialogue.languageDialogue().forEach((reason, chatReasonDialogue) -> {
                    perReason.put(reason, new ArrayList<>(chatReasonDialogue.sayings()));
                });
                chats.put(lang, perReason);
            });

            this.dialogues.clear();
            this.dialogues.putAll(chats);

            if (this.dialogues.isEmpty()) this.dialogues.putAll(DefaultChat.getDefaultChat());
        }

        public Map<String, Map<NpcChat.ChatReason, List<String>>> getDialogues() {
            return dialogues;
        }

        public void setDialogues(Map<String, Map<NpcChat.ChatReason, List<String>>> newDialogues) {
            dialogues.clear();
            dialogues.putAll(newDialogues);
        }

        public Map<NpcChat.ChatReason, List<String>> getCustomDialogues() {
            return customDialogues;
        }

        public void setCustomDialogues(Map<NpcChat.ChatReason, List<String>> map) {
            customDialogues.clear();
            customDialogues.putAll(map);
        }

        public void markDialoguesDirty(UUID avoid) {
            if (!(npc.getWorld() instanceof ServerWorld serverWorld)) return;

            for (ServerPlayerEntity player : serverWorld.getPlayers(p -> !p.getUuid().equals(avoid))) {
                try {
                    ServerPlayNetworking.send(player, new DialogueSyncPayload(npc.getId(), dialogues, customDialogues));
                } catch (IOException e) {
                    CiviliansMod.LOGGER.error("[CiviliansMod] Failed to sync dialogues to player {}", player.getGameProfile().getName(), e);
                }
            }
        }

        //TODO ADD Language entrys....
        public void updateDialoguesForLanguage(String language, Map<NpcChat.ChatReason, List<String>> newDialogues) {
            this.dialogues.put(language, new HashMap<>(newDialogues));
            markDialoguesDirty(null); // Sync to all players
        }

        public void updateDialogueForLanguageAndReason(String language, NpcChat.ChatReason reason, List<String> messages) {
            this.dialogues.computeIfAbsent(language, k -> new EnumMap<>(NpcChat.ChatReason.class))
                    .put(reason, new ArrayList<>(messages));
            markDialoguesDirty(null);
        }

        public void addLanguage(String language, Map<NpcChat.ChatReason, List<String>> dialogues) {
            this.dialogues.put(language, new HashMap<>(dialogues));
            markDialoguesDirty(null);
        }

        public void removeLanguage(String language) {
            this.dialogues.remove(language);
            markDialoguesDirty(null);
        }

        public Set<String> getAvailableLanguages() {
            return dialogues.keySet();
        }

        public boolean hasLanguage(String language) {
            return dialogues.containsKey(language) && !dialogues.get(language).isEmpty();
        }

        public void setLanguageMap(Map<String, Map<NpcChat.ChatReason, List<String>>> newLanguageMap) {
            this.dialogues.clear();
            this.dialogues.putAll(newLanguageMap);
            markDialoguesDirty(null);
        }


        static @NotNull BiConsumer<String, Map<NpcChat.ChatReason, List<String>>> getManageCompoundSave(NbtCompound mainCompound) {
            return (language, reasonToMessagesMap) -> {
                NbtCompound languageCompound = new NbtCompound();

                reasonToMessagesMap.forEach((reason, messages) -> {
                    NbtList messageList = new NbtList();
                    messages.forEach(message -> messageList.add(NbtString.of(message)));

                    // Ajout de la liste des messages pour une raison donnée
                    languageCompound.put(reason.getName(), messageList);
                });

                // Ajout du compound pour la langue
                mainCompound.put(language, languageCompound);
            };
        }
    }

    public static class SkinManager {
        byte[] skinByteArray;

        SkinIdentifier skinIdentifier;

        public void setBaseVariant(int baseVariant) {
            this.baseVariant = baseVariant;
        }

        int baseVariant;

        boolean slim;

        NPCEntity npcEntity;

        boolean defaultSkin;

        public SkinManager(NPCEntity npcEntity) {
            this.npcEntity = npcEntity;
            this.defaultSkin = true;
            this.baseVariant = npcEntity.random.nextInt(88);
            if (baseVariant <= 43) {
                this.slim = false;
            } else {
                this.slim = true;
            }
            if (npcEntity.nameManager != null) {
                npcEntity.nameManager.setRandomName(this.slim);
            } else {
                CiviliansMod.LOGGER.warn("[CiviliansMod] nameManager is null in SkinManager constructor");
            }
        }

        public boolean isSlim() {
            return slim;
        }

        public void setSkinByteArray(byte[] skinByteArray) {
            this.skinByteArray = skinByteArray;
        }

        @Environment(EnvType.CLIENT)
        public void setIdSkin(SkinIdentifier skin) {
            this.skinIdentifier = skin;
        }

        @Environment(EnvType.CLIENT)
        public SkinIdentifier getIdSkin() {
            if (this.skinIdentifier == null) {
                return NPCUtil.getNPCTexture(baseVariant);
            }
            return this.skinIdentifier;
        }

        void writeView(WriteView writeView) {
            writeView.putInt("basevariat", baseVariant);
            if (skinByteArray != null) {
                writeView.put("skin", Skin.CODEC, new Skin(skinByteArray));
            }
        }

        void readNbt(ReadView readView) {
            this.baseVariant = readView.getInt("basevariat", 0);
            Optional<Skin> skin = readView.read("skin", Skin.CODEC);
            skin.ifPresent(skin1 -> this.skinByteArray = skin1.skin);
        }



        public void setSlim(boolean slim) {
            this.slim = slim;
        }
    }

    public static class NameManager {
        String[] defaultModelNames = {"Charles", "Cade", "Henry", "Liam", "Rodney", "Nathaniel", "Elliot", "Julian", "Malcolm", "Tobias",
                "Wesley", "Felix", "Desmond", "Simon", "Miles", "Everett", "Dorian", "Quentin", "Cedric", "Adrian", "Roman", "Marcus", "Gideon", "Levi", "Jasper"};
        String[] slimModelNames = {"Evelyn", "Sarah", "Olivia", "Emma", "Alexia", "Amelia", "Celeste", "Lillian", "Joleen", "Rosalie",
                "Clara", "Vivienne", "Elena", "Margot", "Nora", "Daphne", "Fiona", "Genevieve", "Juliette", "Lucille", "Naomi", "Ivy", "Serena", "Vera", "Adelaide"};

        NPCEntity npcEntity;

        public NameManager(NPCEntity npcEntity) {
            this.npcEntity = npcEntity;
        }

        public void setRandomName(boolean slim) {
            if (slim)
                this.npcEntity.setCustomName(Text.literal(slimModelNames[Random.create().nextInt(slimModelNames.length)]));
            else
                this.npcEntity.setCustomName(Text.literal(defaultModelNames[Random.create().nextInt(defaultModelNames.length)]));

        }
    }

    public record Dialogues(Map<String, LanguageDialogue> dialogues) {
        public static final Codec<Dialogues> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.unboundedMap(Codec.STRING, LanguageDialogue.CODEC)
                        .fieldOf("dialogues")
                        .forGetter(o -> o.dialogues)
        ).apply(instance, Dialogues::new));

        public static Dialogues fromMap(Map<String, Map<NpcChat.ChatReason, List<String>>> dialogues) {
            Dialogues dialogues1 = new Dialogues(new HashMap<>());
            dialogues.forEach((s, chatReasonListMap) -> {
                dialogues1.dialogues.put(s, LanguageDialogue.fromMap(chatReasonListMap));
            });
            return dialogues1;
        }
    }

    record LanguageDialogue(Map<NpcChat.ChatReason, ChatReasonDialogue> languageDialogue) {
        public static final Codec<LanguageDialogue> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.unboundedMap(NpcChat.ChatReason.CODEC, ChatReasonDialogue.CODEC)
                        .fieldOf("languague_dialogues")
                        .forGetter(o -> o.languageDialogue)
        ).apply(instance, LanguageDialogue::new));

        public static LanguageDialogue fromMap(Map<NpcChat.ChatReason, List<String>> languageDialogue) {
            LanguageDialogue languageDialogue1 = new LanguageDialogue(new HashMap<>());
            languageDialogue.forEach((chatReason, chatReasonDialogue) -> {
                languageDialogue1.languageDialogue.put(chatReason, new ChatReasonDialogue(chatReasonDialogue));
            });
            return languageDialogue1;
        }
    }

    record ChatReasonDialogue(List<String> sayings) {
        public static final Codec<ChatReasonDialogue> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.list(Codec.STRING).fieldOf("sayings").forGetter(o -> o.sayings)
        ).apply(instance, ChatReasonDialogue::new));
    }

    record Skin(byte[] skin) {
        public static final Codec<Skin> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.BYTE.listOf().fieldOf("skin").forGetter(o -> {
                    List<Byte> byteList = new ArrayList<>();
                    for (byte b : o.skin) {
                        byteList.add(b);
                    }
                    return byteList;
                })
        ).apply(instance, bytes -> {
            byte[] array = new byte[bytes.size()];
            for (int i = 0; i < bytes.size(); i++) array[i] = bytes.get(i);
            return new Skin(array);
        }));
    }
}