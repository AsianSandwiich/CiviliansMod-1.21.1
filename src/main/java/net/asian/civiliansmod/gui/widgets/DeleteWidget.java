package net.asian.civiliansmod.gui.widgets;

import net.asian.civiliansmod.CiviliansMod;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class DeleteWidget extends ButtonWidget {
    Identifier BUTTON = Identifier.of(CiviliansMod.MOD_ID, "textures/gui/delete_button.png");
    Identifier BUTTON_HOVERED = Identifier.of(CiviliansMod.MOD_ID, "textures/gui/delete_button_hover.png");

    protected DeleteWidget(int x, int y, int width, int height, PressAction onPress) {
        super(x, y, width, height, Text.empty(), onPress, DEFAULT_NARRATION_SUPPLIER);
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        if (isMouseOver(mouseX, mouseY)) {
            context.setShaderColor(1.0f, 0, 0, 1.0f);
            context.drawTexture(BUTTON_HOVERED, this.getX(), this.getY(), 0, 0, 0, this.width, this.height, this.width, this.height);
            context.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        } else {
            context.drawTexture(BUTTON, this.getX(), this.getY(), 0, 0, 0, this.width, this.height, this.width, this.height);
        }
    }
}
