package net.asian.civiliansmod.gui;

import net.asian.civiliansmod.entity.NPCEntity;
import net.asian.civiliansmod.util.NPCUtil;
import net.asian.civiliansmod.util.SkinIdentifier;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.stream.IntStream;


/**
 * Class to display slim npc models
 */
public class SlimNPCScreen extends AbstratcNPCScreen {
    public SlimNPCScreen(NPCEntity npc) {
        super(npc);
    }

    public SlimNPCScreen(NPCEntity npc, int selected, int originalVariant) {
        super(npc, selected, originalVariant);
    }

    public SlimNPCScreen(NPCEntity npc, int selected, int defaultSkin, int selectedVariantIndex) {
        super(npc, selected, defaultSkin, selectedVariantIndex);
    }

    @Override
    protected List<Integer> getSkinsToRender() {
        return IntStream.range(0, NPCUtil.getSkins().size())
                .filter(i -> {
                    SkinIdentifier skin = NPCUtil.getSkins().get(i);
                    return skin.slim() && !skin.custom();
                })
                .boxed()
                .toList();
    }
}
