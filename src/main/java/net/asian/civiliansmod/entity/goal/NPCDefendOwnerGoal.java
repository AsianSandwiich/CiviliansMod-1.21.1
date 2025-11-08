package net.asian.civiliansmod.entity.goal;

import net.asian.civiliansmod.entity.NPCEntity;
import net.minecraft.entity.ai.goal.Goal;

public class NPCDefendOwnerGoal extends Goal {

    private final NPCEntity npc;

    // dummy constructor..... need todo
    public NPCDefendOwnerGoal(NPCEntity npc) {
        this.npc = npc;
    }

    @Override
    public boolean canStart() {
        // no logic yet, need todo
        return false;
    }
}