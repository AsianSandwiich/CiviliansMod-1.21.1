package net.asian.civiliansmod.gui;

import net.asian.civiliansmod.CiviliansMod;
import net.asian.civiliansmod.entity.NPCEntity;
import net.asian.civiliansmod.util.NPCUtil;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Class to display default npc models
 */
public class DefaultNPCScreen extends CustomNPCScreen {
    public DefaultNPCScreen(NPCEntity npc) {
        super(npc);
    }


    @Override
    protected List<Identifier> getDefaultSkins() {
        return NPCUtil.getDefaultSkins();
    }

    @Override
    protected List<Identifier> getSlimSkins() {
        return List.of();
    }

}
