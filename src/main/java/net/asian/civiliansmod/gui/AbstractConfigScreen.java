package net.asian.civiliansmod.gui;

import net.asian.civiliansmod.entity.NPCEntity;
import net.asian.civiliansmod.gui.widgets.TextButtonWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class AbstractConfigScreen extends Screen {
    NPCEntity npc;

    protected AbstractConfigScreen(NPCEntity npc, Text title) {
        super(title);
        this.npc = npc;
    }


    @Override
    protected void init() {
        int x = width / 2;
        int y = height / 2;

        int skinSelectionColor = this instanceof AbstractNPCScreen ? 0x00FF00 : 0xFFFFFFFF;
        TextButtonWidget skinSelection = new TextButtonWidget(x - 88, y - 78, 85, 13, Text.translatable("civilians.gui.skin"), (button) -> {
            MinecraftClient.getInstance().setScreen(new DefaultNPCScreen(npc));
        }, 0xFFFFFFFF, skinSelectionColor);

        int chatSelectionColor = this instanceof CustomChatScreen ? 0x00FF00 : 0xFFFFFFFF;
        TextButtonWidget chatSelection = new TextButtonWidget(x + 3, y - 78, 85, 13, Text.translatable("civilians.gui.chat"), (button) -> {
            MinecraftClient.getInstance().setScreen(new CustomChatScreen(npc));
        }, 0xFFFFFFFF, chatSelectionColor);

        this.addDrawableChild(skinSelection);
        this.addDrawableChild(chatSelection);

        super.init();
    }
}
