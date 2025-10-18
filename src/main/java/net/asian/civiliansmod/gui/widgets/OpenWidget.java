package net.asian.civiliansmod.gui.widgets;

import net.asian.civiliansmod.CiviliansMod;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class OpenWidget extends ButtonWidget {
    int baseY;
    final Identifier OPEN = Identifier.of(CiviliansMod.MOD_ID, "textures/gui/open_arrow.png");
    final Identifier OPEN_HOVERED = Identifier.of(CiviliansMod.MOD_ID, "textures/gui/open_arrow_hover.png");
    final Identifier CLOSE = Identifier.of(CiviliansMod.MOD_ID, "textures/gui/close_arrow.png");
    final Identifier CLOSE_HOVERED = Identifier.of(CiviliansMod.MOD_ID, "textures/gui/close_arrow_hover.png");
    ChatReasonEntryScrollContainer scrollWidget;

    protected OpenWidget(int x, int y, int width, int height, ChatReasonEntryScrollContainer scrollWidget, PressAction onPress) {
        super(x, y, width, height, Text.empty(), onPress, DEFAULT_NARRATION_SUPPLIER);
        this.scrollWidget = scrollWidget;
        this.baseY = y;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        this.baseY = this.getY();
        if(!scrollWidget.open)
            this.setY(baseY + 1);

        if (isMouseOver(mouseX, mouseY)) {
            context.drawTexture(scrollWidget.open ? OPEN_HOVERED : CLOSE_HOVERED, this.getX(), this.getY() , 0, 0, 0, this.width, this.height, this.width, this.height);
        } else {
            context.drawTexture(scrollWidget.open ? OPEN : CLOSE, this.getX(), this.getY(), 0, 0, 0, this.width, this.height, this.width, this.height);
        }
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return this.active
                && this.visible
                && mouseX >= (double)this.getX() + 2
                && mouseY >= (double)this.getY() + 2
                && mouseX < (double)(this.getX() + this.width) - 2
                && mouseY < (double)(this.getY() + this.height) - 2;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.isMouseOver(mouseX, mouseY)) {
            this.onPress.onPress(this);
            return true;
        }
        return false;
    }
}
