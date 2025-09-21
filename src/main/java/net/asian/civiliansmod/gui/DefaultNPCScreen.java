package net.asian.civiliansmod.gui;

import net.asian.civiliansmod.entity.NPCEntity;
import net.asian.civiliansmod.util.NPCUtil;
import net.asian.civiliansmod.util.SkinIdentifier;

import java.util.List;
import java.util.stream.IntStream;


/**
 * Class to display default npc models
 */
public class DefaultNPCScreen extends AbstractNPCScreen {
    public DefaultNPCScreen(NPCEntity npc) {
        super(npc);
    }

    public DefaultNPCScreen(NPCEntity npc, int selected, int defaultSkin, int selectedVariantIndex) {
        super(npc, selected, defaultSkin, selectedVariantIndex);
    }

    @Override
    protected List<Integer> getSkinsToRender() {
        return IntStream.range(0, NPCUtil.getSkins().size())
                .filter(i -> {
                    SkinIdentifier skin = NPCUtil.getSkins().get(i);
                    return !skin.slim() && !skin.custom();
                })
                .boxed()
                .toList();
    }

    public DefaultNPCScreen(NPCEntity npc, int selected, int originalVariant) {
        super(npc, selected, originalVariant);
    }

}
