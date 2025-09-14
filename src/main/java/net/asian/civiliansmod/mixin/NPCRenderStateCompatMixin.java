package net.asian.civiliansmod.mixin;

import net.asian.civiliansmod.renderer.NPCRenderState;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(NPCRenderState.class)
public class NPCRenderStateCompatMixin {
    public ItemStack getMainHandItem() {
        return ItemStack.EMPTY; // Civilians don't hold something as default
    }

    public ItemStack getOffhandItem() {
        return ItemStack.EMPTY;
    }
}
