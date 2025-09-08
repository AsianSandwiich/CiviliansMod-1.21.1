package net.asian.civiliansmod.mixin;

import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;
import net.minecraft.client.gui.widget.TextFieldWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Environment(EnvType.CLIENT)
@Mixin(TextFieldWidget.class)
public interface TextFieldWidgetAccessor {
    @Accessor
    void setMaxLength(int maxLength);
}
