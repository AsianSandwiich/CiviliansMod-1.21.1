package net.asian.civiliansmod.gui.widgets;

import net.asian.civiliansmod.chat.NpcChat;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class AbstractDialogueEntry extends ButtonWidget {
    NpcChat.ChatReason chatReason;

    protected AbstractDialogueEntry(int x, int y, int width, int height,  NpcChat.ChatReason chatReason, PressAction onPress) {
        super(x, y, width, height, Text.empty(), onPress, DEFAULT_NARRATION_SUPPLIER);
        this.chatReason = chatReason;
    }

    public void render(DrawContext context, int x, int y, int mouseX, int mouseY, boolean hovered, float delta) {
        if (isMouseOver(mouseX, mouseY)) {
            context.setShaderColor(0.75f, 0.75f, 0.75f, 0.75f);
        }
        context.drawGuiTexture(Identifier.ofVanilla("widget/button"), x, y, 112, 12);
        context.setShaderColor(1f, 1f, 1f, 1f);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.active && this.visible && this.isMouseOver(mouseX, mouseY)) {
            this.onPress.onPress(this);
            return true;
        }
        return false;
    }
}
