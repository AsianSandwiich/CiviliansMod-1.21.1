package net.asian.civiliansmod.gui;

import net.asian.civiliansmod.CiviliansMod;
import net.asian.civiliansmod.chat.NpcChat;
import net.asian.civiliansmod.entity.NPCEntity;
import net.asian.civiliansmod.gui.widgets.ImageButtonWidget;
import net.asian.civiliansmod.util.NPCUtil;
import net.asian.civiliansmod.util.SkinIdentifier;
import net.minecraft.util.Identifier;

import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;


/**
 * Class to display custom models choose by the player
 */
public class CustomNPCScreen extends AbstractNPCScreen {
    public CustomNPCScreen(NPCEntity npc) {
        super(npc);
    }

    public CustomNPCScreen(NPCEntity npc, int selected, int originalVariant) {
        super(npc, selected, originalVariant);
    }

    public CustomNPCScreen(NPCEntity npc, int selected, int defaultSkin, int selectedVariantIndex) {
        super(npc, selected, defaultSkin, selectedVariantIndex);
    }

    @Override
    protected List<Integer> getSkinsToRender() {
        return IntStream.range(0, NPCUtil.getSkins().size())
                .filter(i -> {
                    SkinIdentifier skin = NPCUtil.getSkins().get(i);
                    return skin.custom();
                })
                .boxed()
                .sorted(Comparator.comparingInt(value -> {
                    SkinIdentifier skin = NPCUtil.getSkins().get(value);
                    return skin.slim() ? 1 : 0;
                }))
                .toList();
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
                containerX + 235, containerY + containerHeight - 160, 15, 15,
                Identifier.of(CiviliansMod.MOD_ID, "textures/gui/reset_button.png"),
                (press) -> {
                    NPCUtil.refreshTextures();
                    NpcChat.refresh();
                    this.init();
                }
        ));
    }
}
