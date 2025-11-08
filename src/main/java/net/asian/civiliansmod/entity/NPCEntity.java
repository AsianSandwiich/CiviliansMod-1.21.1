package net.asian.civiliansmod.entity;

import net.asian.civiliansmod.CiviliansMod;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.asian.civiliansmod.CiviliansMod;
import net.asian.civiliansmod.chat.DefaultChat;
import net.asian.civiliansmod.chat.NpcChat;
import net.asian.civiliansmod.entity.goal.CustomDoorGoal;
import net.asian.civiliansmod.entity.goal.NPCAttackGoal;
import net.asian.civiliansmod.entity.goal.NPCDefendOwnerGoal;
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
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.*;
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
import net.minecraft.util.Uuids;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.*;
import java.util.function.BiConsumer;

public class NPCEntity extends PathAwareEntity {
    private float targetYaw = 0.0F;
    private boolean isTurning = false;
    private int lookAtPlayerTicks = 0;
    private static final TrackedData<Boolean> IS_PAUSED = DataTracker.registerData(NPCEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private int regenerationCooldown = 0;
    private static final TrackedData<Boolean> IS_FOLLOWING = DataTracker.registerData(NPCEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

    // NEW: Battle Buddy Mode
    private static final TrackedData<Boolean> IS_BATTLE_BUDDY = DataTracker.registerData(NPCEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private float originalMaxHealth = 20.0F;
    private PlayerEntity owner = null;

    // NEW: Wander Radius
    private static final TrackedData<Float> WANDER_RADIUS = DataTracker.registerData(NPCEntity.class, TrackedDataHandlerRegistry.FLOAT);

    // NEW: Trade Preset
    private static final TrackedData<String> TRADE_PRESET = DataTracker.registerData(NPCEntity.class, TrackedDataHandlerRegistry.STRING);

    int updateDialoguesTicks = 0;

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
        if (this.skinManager.getSkinByteArray() == null) {
            for (ServerPlayerEntity player : Objects.requireNonNull(this.getWorld().getServer()).getPlayerManager().getPlayerList()) {
                ServerPlayNetworking.send(player, new SyncSkinPayload(this.getId(), this.skinManager.getBaseVariant()));
            }
        } else {
            for (ServerPlayerEntity player : Objects.requireNonNull(this.getWorld().getServer()).getPlayerManager().getPlayerList()) {
                ServerPlayNetworking.send(player, new ClientNpcSkinPayload(this.getId(), this.skinManager.isSlimModel(), this.skinManager.getSkinByteArray()));
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
        builder.add(IS_BATTLE_BUDDY, false);
        builder.add(WANDER_RADIUS, 16.0F); // Default 16 block radius
        builder.add(TRADE_PRESET, "none"); // Default no trade preset
    }

    @Override
    public boolean cannotDespawn() {
        return true;
    }

    // NEW: Battle Buddy getters/setters
    public boolean isBattleBuddy() {
        return this.dataTracker.get(IS_BATTLE_BUDDY);
    }

    public void setBattleBuddy(boolean battleBuddy) {
        this.dataTracker.set(IS_BATTLE_BUDDY, battleBuddy);
        if (battleBuddy) {
            // Store original health
            this.originalMaxHealth = this.getMaxHealth();
            // Boost HP for battle mode
            this.getAttributeInstance(EntityAttributes.MAX_HEALTH).setBaseValue(40.0);
            this.setHealth(40.0F);
        } else {
            // Restore original health
            this.getAttributeInstance(EntityAttributes.MAX_HEALTH).setBaseValue(this.originalMaxHealth);
            if (this.getHealth() > this.originalMaxHealth) {
                this.setHealth(this.originalMaxHealth);
            }
        }
    }

    public void setOwner(PlayerEntity player) {
        this.owner = player;
    }

    public PlayerEntity getOwner() {
        return this.owner;
    }

    // NEW: Wander Radius getters/setters
    public float getWanderRadius() {
        return this.dataTracker.get(WANDER_RADIUS);
    }

    public void setWanderRadius(float radius) {
        this.dataTracker.set(WANDER_RADIUS, Math.max(1.0F, Math.min(radius, 64.0F))); // Clamp between 1-64
    }

    // NEW: Trade Preset getters/setters
    public String getTradePreset() {
        return this.dataTracker.get(TRADE_PRESET);
    }

    public void setTradePreset(String preset) {
        this.dataTracker.set(TRADE_PRESET, preset);
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

        writeView.putBoolean("IsPaused", this.isPaused());
        writeView.putBoolean("IsFollowing", this.isFollowing());
        writeView.putBoolean("IsBattleBuddy", this.isBattleBuddy());
        writeView.putFloat("WanderRadius", this.getWanderRadius());
        writeView.putString("TradePreset", this.getTradePreset());
        writeView.put("dialogues", Dialogue.Dialogues.CODEC, Dialogue.Dialogues.fromMap(chatManager.getDialogues()));
        this.skinManager.writeView(writeView);

        if (this.owner != null) {
            writeView.put("OwnerUUID", Uuids.CODEC, this.owner.getUuid());
        }
    }

    @Override
    protected void readCustomData(ReadView readView) {
        super.readCustomData(readView);
        this.setPaused(readView.getBoolean("IsPaused", false));
        this.setFollowing(readView.getBoolean("IsFollowing", false));
        this.setBattleBuddy(readView.getBoolean("IsBattleBuddy", false));
        this.setWanderRadius(readView.getFloat("WanderRadius", 16.0F));
        this.setTradePreset(readView.getString("TradePreset", "none"));

        this.chatManager.setFromReadView(readView);
        this.skinManager.readNbt(readView);

        UUID ownerUuid = readView.read("OwnerUUID", Uuids.CODEC).orElse(null);
        if (ownerUuid != null && this.getWorld() != null && !this.getWorld().isClient) {
            PlayerEntity player = this.getWorld().getPlayerByUuid(ownerUuid);
            if (player != null) {
                this.owner = player;
            }
        }
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return PathAwareEntity.createMobAttributes()
                .add(EntityAttributes.MAX_HEALTH, 20.0)
                .add(EntityAttributes.MOVEMENT_SPEED, 0.3)
                .add(EntityAttributes.ATTACK_DAMAGE, 2.0); // NEW: For battle buddy mode
    }

    @Override
    protected void initGoals() {
        super.initGoals();

        // Battle Buddy goals (highest priority when active)
        this.goalSelector.add(1, new NPCDefendOwnerGoal(this));
        this.goalSelector.add(2, new NPCAttackGoal(this, 1.0, false));

        // Regular goals
        this.goalSelector.add(3, new WanderAroundFarGoal(this, 0.7) {
            @Override
            public boolean canStart() {
                // Don't wander if paused, following, or in battle buddy mode
                if (isPaused() || isFollowing() || isBattleBuddy()) {
                    return false;
                }
                return super.canStart();
            }

            @Override
            protected Vec3d getWanderTarget() {
                // Respect wander radius
                float radius = getWanderRadius();
                Vec3d currentPos = NPCEntity.this.getPos();

                // Get random position within radius
                Random random = NPCEntity.this.getRandom();
                double angle = random.nextDouble() * 2 * Math.PI;
                double distance = random.nextDouble() * radius;

                double x = currentPos.x + Math.cos(angle) * distance;
                double z = currentPos.z + Math.sin(angle) * distance;

                return new Vec3d(x, currentPos.y, z);
            }
        });

        this.goalSelector.add(6, new CustomDoorGoal(this));
        this.goalSelector.add(4, new LookAroundGoal(this));

        // Target selector for battle buddy
        this.targetSelector.add(1, new RevengeGoal(this) {
            @Override
            public boolean canStart() {
                return isBattleBuddy() && super.canStart();
            }
        });
    }

    public boolean hasSentTo(UUID playerId) {
        return sent != null && sent.contains(playerId);
    }

    public void markSentTo(UUID playerId) {
        if (sent != null) {
            sent.add(playerId);
        }
    }
}