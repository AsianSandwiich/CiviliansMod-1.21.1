package net.asian.civiliansmod.gui;

import net.asian.civiliansmod.CiviliansMod;
import net.asian.civiliansmod.entity.NPCEntity;
import net.asian.civiliansmod.gui.widgets.ImageButtonWidget;
import net.asian.civiliansmod.util.NPCUtil;
import net.minecraft.util.Identifier;


/**
 * Class to display custom models choose by the player
 */
public class CustomNPCScreen extends AbstratcNPCScreen {
    public CustomNPCScreen(NPCEntity npc) {
        super(npc);
    }

    public CustomNPCScreen(NPCEntity npc, int selected) {
        super(npc, selected);
    }

    @Override
    protected int[] getStartAndEndIndexes() {
        return new int[]{NPCUtil.getDefaultCustomSkinIndexes()[0], NPCUtil.getSlimCustomSkinIndexes()[1]};
    }


    /**
     * We add the reset button to collect skins.
     * Texture from <a href="https://github.com/McMellonTeam/easierworldcreator">EWC mod</a>
     */
    @Override
    protected void init() {
        super.init();
        int containerWidth = 256;
        int containerHeight = 166;
        int containerX = (this.width - containerWidth) / 2;
        int containerY = (this.height - containerHeight) / 2;

        this.addDrawableChild(new ImageButtonWidget(
                containerX + 220, containerY + containerHeight - 140, 15, 15,
                Identifier.of(CiviliansMod.MOD_ID, "textures/gui/reset_button.png"),
                (press) -> {
                    NPCUtil.refreshTextures();
                    renitIndexes();
                    this.init();
                }
        ));
    }
}
