package net.asian.civiliansmod.gui;

import net.asian.civiliansmod.entity.NPCEntity;
import net.asian.civiliansmod.util.NPCUtil;


/**
 * Class to display default npc models
 */
public class DefaultNPCScreen extends AbstratcNPCScreen {
    public DefaultNPCScreen(NPCEntity npc) {
        super(npc);
    }

    public DefaultNPCScreen(NPCEntity npc, int selected, int originalVariant) {
        super(npc, selected, originalVariant);
    }

    @Override
    protected int[] getStartAndEndIndexes() {
        return NPCUtil.getDefaultSkinIndexes();
    }
}
