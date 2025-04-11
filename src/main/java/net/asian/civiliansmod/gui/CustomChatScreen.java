package net.asian.civiliansmod.gui;

import net.asian.civiliansmod.entity.NPCEntity;
import net.asian.civiliansmod.gui.widgets.GlobalChatScrollWidget;
import net.asian.civiliansmod.util.DebugUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class CustomChatScreen extends AbstractConfigScreen {
    GlobalChatScrollWidget chatScrollWidget;

    public CustomChatScreen(NPCEntity npc) {
        super(npc, Text.of("civilians.gui.chat_title"));
        chatScrollWidget = new GlobalChatScrollWidget(MinecraftClient.getInstance(), 236, 134, 0, 0, 10, this);
    }


    @Override
    public void init() {
        int x = width / 2;
        int y = height / 2;
        super.init();
        chatScrollWidget.setX(x - 114);
        chatScrollWidget.setY(y - 60);
        this.addDrawableChild(chatScrollWidget);
    }

    public void fullInit(){
        int x = width / 2;
        int y = height / 2;
        chatScrollWidget = new GlobalChatScrollWidget(MinecraftClient.getInstance(), 236, 134, x - 114, y - 60, 10, this);
        this.addDrawableChild(chatScrollWidget);
        super.init();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        DebugUtil.drawMouseInfo(context, width, height, mouseX, mouseY);

    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        int x = width / 2;
        int y = height / 2;
        Identifier guiTexture = Identifier.of("civiliansmod", "textures/gui/chat_gui.png");
        context.drawTexture(guiTexture, x - 128, y - 83, 0, 0, 0, 256, 166, 256, 166);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (chatScrollWidget.isMouseOver(mouseX, mouseY))
            chatScrollWidget.onClick(mouseX, mouseY);
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
