package net.asian.civiliansmod.gui;

import net.asian.civiliansmod.CiviliansMod;
import net.asian.civiliansmod.gui.widgets.TextButtonWidget;
import net.minecraft.client.font.MultilineText;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ConfirmScreen extends Screen {
    Screen parent;
    ButtonWidget.PressAction confirmAction;
    ButtonWidget.PressAction cancelAction;
    MultilineText text;
    int color;

    public ConfirmScreen(Screen parent, ButtonWidget.PressAction confirmAction, ButtonWidget.PressAction cancelAction, MultilineText text, int color) {
        super(Text.literal("confirm"));
        this.parent = parent;
        this.confirmAction = confirmAction;
        this.cancelAction = cancelAction;
        this.text = text;
        this.color = color;
    }

    @Override
    protected void init() {
        int x = width / 2;
        int y = height / 2;
        super.init();
        TextButtonWidget confirm = new TextButtonWidget(x + 6, y + 30, 60, 15, Text.translatable("civilians.gui.confirm"), confirmAction, 0xFFFFFFFF, 0xFF00FF00);
        TextButtonWidget cancel = new TextButtonWidget(x - 66, y + 30, 60, 15, Text.translatable("civilians.gui.cancel"), confirmAction, 0xFFFFFFFF, 0xFFFF0000);

        addDrawableChild(confirm);
        addDrawableChild(cancel);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        int x = width / 2;
        int y = height / 2;
        text.drawCenterWithShadow(context, x, y - 6, 9, color);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        int x = width / 2;
        int y = height / 2;
        super.renderBackground(context, mouseX, mouseY, delta);
        context.drawTexture(RenderLayer::getGui, Identifier.of(CiviliansMod.MOD_ID, "textures/gui/edit_dialogue_screen.png"), x - 150, y - 35, 0, 0, 300, 70, 300, 70);
    }
}
