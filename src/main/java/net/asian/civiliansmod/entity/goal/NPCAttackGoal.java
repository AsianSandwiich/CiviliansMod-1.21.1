package net.asian.civiliansmod.entity.goal;

import net.asian.civiliansmod.entity.NPCEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;

public class NPCAttackGoal extends MeleeAttackGoal {
    private final NPCEntity npc;

    public NPCAttackGoal(NPCEntity npc, double speed, boolean pauseWhenMobIdle) {
        super(npc, speed, pauseWhenMobIdle);
        this.npc = npc;
    }

    @Override
    public boolean canStart() {
        // Only attack when in battle buddy mode and has a target
        if (!npc.isBattleBuddy()) {
            return false;
        }
        
        LivingEntity target = npc.getTarget();
        if (target == null || !target.isAlive()) {
            return false;
        }
        
        return super.canStart();
    }

    @Override
    public boolean shouldContinue() {
        return npc.isBattleBuddy() && super.shouldContinue();
    }

    @Override
    public void stop() {
        super.stop();
        npc.setTarget(null);
    }
}