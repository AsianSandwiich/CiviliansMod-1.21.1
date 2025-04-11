package net.asian.civiliansmod.gui;

import net.asian.civiliansmod.CiviliansMod;
import net.asian.civiliansmod.chat.NpcChat;
import net.asian.civiliansmod.gui.widgets.TextButtonWidget;
import net.asian.civiliansmod.mixin.TextFieldWidgetAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class AbstractDialogueEditionScreen extends Screen {
    String text;
    TextFieldWidget textFieldWidget;
    CustomChatScreen parent;
    NpcChat.ChatReason reason;

    protected AbstractDialogueEditionScreen(String text, NpcChat.ChatReason reason, CustomChatScreen parent) {
        super(Text.literal("dialoguescreen"));
        this.text = text;
        this.parent = parent;
        this.reason = reason;
    }

    @Override
    protected void init() {
        int x = width / 2;
        int y = height / 2;
        super.init();
        this.textFieldWidget = new TextFieldWidget(this.client.textRenderer, x - 125, y, 250, 15, Text.literal(text));
        ((TextFieldWidgetAccessor)textFieldWidget).setMaxLength(256);
        this.textFieldWidget.setText(text);
        TextButtonWidget cancelButton = new TextButtonWidget(x - 66, y + 30, 60, 15, Text.translatable("civilians.gui.cancel"), button -> MinecraftClient.getInstance().setScreen(parent), 0xFFFFFF, 0xFFFF0000);

        addDrawableChild(textFieldWidget);
        addDrawableChild(cancelButton);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        int x = width / 2;
        int y = height / 2;
        super.renderBackground(context, mouseX, mouseY, delta);
        context.drawTexture(Identifier.of(CiviliansMod.MOD_ID, "textures/gui/edit_dialogue_screen.png"), x - 150, y - 35, 0, 0, 300, 70, 300, 70);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        textFieldWidget.mouseClicked(mouseX, mouseY, button);
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
    }
}
