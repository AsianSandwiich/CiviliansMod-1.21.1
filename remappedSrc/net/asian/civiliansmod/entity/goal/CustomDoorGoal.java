package net.asian.civiliansmod.entity.goal;

import net.minecraft.block.BlockState;
import net.minecraft.block.DoorBlock;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;


public class CustomDoorGoal extends Goal {
    private final PathAwareEntity npc;           // NPC instance
    private BlockPos targetDoorPos;              // Store the location of the door
    private boolean isDoorOpened = false;        // Whether the door is currently open
    private boolean isNavigating = false;        // Whether NPC is in navigation mode (through door)
    private int stuckTimer = 0;                  // Timer to handle NPC being stuck
    private int interactionCooldown = 0;         // Cooldown between door interactions

    public CustomDoorGoal(PathAwareEntity npc) {
        this.npc = npc;
    }

    @Override
    public boolean canStart() {
        // Prevent restarting if there's a cooldown
        if (interactionCooldown > 0) {
            interactionCooldown--;
            return false;
        }

        // Find doors near NPC that block the way
        targetDoorPos = findNearbyDoor();
        return targetDoorPos != null; // Start the goal if a door was found
    }

    @Override
    public boolean shouldContinue() {
        // Continue as long as the door is detected, and NPC hasn't given up
        return targetDoorPos != null && !hasReachedPosition(getTargetPosition());
    }

    @Override
    public void start() {
        if (targetDoorPos == null) return;

        ServerWorld world = (ServerWorld) npc.getWorld();
        BlockState doorState = world.getBlockState(targetDoorPos);

        // Open the door if it's closed
        if (isClosedDoor(doorState)) {
            toggleDoor(world, doorState, true); // Open door
            isDoorOpened = true;
        }

        // Navigate through the door position
        Vec3d targetPosition = getTargetPosition();
        npc.getNavigation().startMovingTo(targetPosition.x, targetPosition.y, targetPosition.z, 1.0);
        isNavigating = true;
    }

    @Override
    public void tick() {
        if (npc.getNavigation().isIdle()) {
            stuckTimer++;
        } else {
            stuckTimer = 0;
        }

        // Close door after passing
        if (isDoorOpened && !isNavigating && stuckTimer >= 20) {
            closeDoor();
        }
    }

    @Override
    public void stop() {
        // Reset states when goal is stopped
        targetDoorPos = null;
        isDoorOpened = false;
        isNavigating = false;
        stuckTimer = 0;
        interactionCooldown = 100; // Cooldown after interaction
    }

    private BlockPos findNearbyDoor() {
        // Find doors within a radius of NPC
        BlockPos npcPos = npc.getBlockPos();
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                BlockPos pos = npcPos.add(dx, 0, dz);
                BlockState state = npc.getWorld().getBlockState(pos);
                if (state.getBlock() instanceof DoorBlock && state.contains(DoorBlock.FACING)) {
                    return pos; // Return the first detected valid door with FACING property
                }
            }
        }
        return null;
    }

    private boolean isClosedDoor(BlockState state) {
        // Check if the block at door position is a closed door
        return state.getBlock() instanceof DoorBlock && !state.get(DoorBlock.OPEN);
    }

    private void toggleDoor(ServerWorld world, BlockState doorState, boolean open) {
        // Open or close the door
        world.setBlockState(targetDoorPos, doorState.with(DoorBlock.OPEN, open));
        BlockPos upperPart = targetDoorPos.up();
        BlockState upperDoor = world.getBlockState(upperPart);

        if (upperDoor.getBlock() instanceof DoorBlock) {
            world.setBlockState(upperPart, upperDoor.with(DoorBlock.OPEN, open));
        }
    }

    private Vec3d getTargetPosition() {
        // Ensure the targetDoorPos and corresponding BlockState are valid
        if (targetDoorPos == null) {
            return npc.getPos(); // Fallback to current NPC position
        }

        BlockState doorState = npc.getWorld().getBlockState(targetDoorPos);

        // Check if the block state is a DoorBlock and supports the 'FACING' property
        if (!(doorState.getBlock() instanceof DoorBlock) || !doorState.contains(DoorBlock.FACING)) {
            return npc.getPos(); // Fallback to NPC's current position
        }

        Direction facing = doorState.get(DoorBlock.FACING);

        if (isNavigating) {
            // Navigate to the other side of the door
            return new Vec3d(
                    targetDoorPos.getX() + 0.5 + facing.getOffsetX() * 2,
                    targetDoorPos.getY(),
                    targetDoorPos.getZ() + 0.5 + facing.getOffsetZ() * 2
            );
        } else {
            // Move towards just inside the door
            return new Vec3d(
                    targetDoorPos.getX() + 0.5,
                    targetDoorPos.getY(),
                    targetDoorPos.getZ() + 0.5
            );
        }
    }

    private boolean hasReachedPosition(Vec3d targetPosition) {
        // Check if the NPC has reached the target position near the door
        return npc.getPos().isInRange(targetPosition, 1.0);
    }

    private void closeDoor() {
        // Close the door after NPC has passed through
        ServerWorld world = (ServerWorld) npc.getWorld();
        BlockState doorState = world.getBlockState(targetDoorPos);
        if (doorState.getBlock() instanceof DoorBlock && doorState.get(DoorBlock.OPEN)) {
            toggleDoor(world, doorState, false);
            isDoorOpened = false;
            isNavigating = false;
        }
    }
}