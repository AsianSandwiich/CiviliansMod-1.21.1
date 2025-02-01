package net.asian.civiliansmod.gui;

import net.asian.civiliansmod.entity.NPCEntity;
import net.asian.civiliansmod.util.NPCUtil;


/**
 * Class to display slim npc models
 */
public class SlimNPCScreen extends AbstratcNPCScreen {
    public SlimNPCScreen(NPCEntity npc) {
        super(npc);
    }

    public SlimNPCScreen(NPCEntity npc, int selected) {
        super(npc, selected);
    }

    @Override
    protected int[] getStartAndEndIndexes() {
        return NPCUtil.getSlimSkinIndexes();
    }
}
