package net.asian.civiliansmod.gui;

import net.asian.civiliansmod.CiviliansMod;
import net.asian.civiliansmod.entity.NPCEntity;
import net.asian.civiliansmod.gui.widgets.ChatReasonEntryScrollContainer;
import net.asian.civiliansmod.gui.widgets.GlobalChatScrollWidget;
import net.asian.civiliansmod.gui.widgets.TextButtonWidget;
import net.asian.civiliansmod.util.DebugUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class CustomChatScreen extends AbstractConfigScreen {
    GlobalChatScrollWidget chatScrollWidget;
    private boolean screenInitialized = false; // screen is initialized? true if yes

    public CustomChatScreen(NPCEntity npc) {
        super(npc, Text.of("civilians.gui.chat_title"));
        chatScrollWidget = new GlobalChatScrollWidget(npc, MinecraftClient.getInstance(), 236, 134, 0, 0, 10, this, false);
    }

    @Override
    public void init() {
        super.init();
        int x = width / 2;
        int y = height / 2;

        chatScrollWidget.setX(x - 114);
        chatScrollWidget.setY(y - 60);
        chatScrollWidget.refreshChildren();
        this.addDrawableChild(chatScrollWidget);

        screenInitialized = true; //now it should be initialized

        //Add toggleButton
        int buttonWidth = 80;
        int buttonHeight = 15;
        int buttonX = x - (buttonWidth / 2);
        int buttonY = y + 85;
        TextButtonWidget toggleButton = new TextButtonWidget(buttonX, buttonY, buttonWidth, buttonHeight,  Text.literal(chatScrollWidget.isCustomMode() ? "Default" : "Custom"), button -> {
            boolean nextMode = !chatScrollWidget.isCustomMode();
            this.remove(chatScrollWidget);

            chatScrollWidget = new GlobalChatScrollWidget(npc, MinecraftClient.getInstance(), 236, 134, 0, 0, 10, this, nextMode);

            int newX = width / 2 - 114;
            int newY = height / 2 - 60;
            chatScrollWidget.setX(newX);
            chatScrollWidget.setY(newY);
            chatScrollWidget.refreshChildren();
            chatScrollWidget.refreshScroll();

            this.addDrawableChild(chatScrollWidget);
            button.setMessage(Text.literal(nextMode ? "Default" : "Custom"));

            screenInitialized = true;
        });
        addDrawableChild(toggleButton);
    }

    public void fullInit() {
        List<Boolean> openList = new ArrayList<>();
        double offsetY = chatScrollWidget.getScrollAmount();

        chatScrollWidget.children().forEach(container -> openList.add(container.getOpen()));
        chatScrollWidget.refreshChildren();
        chatScrollWidget.setScrollAmount(Math.min(offsetY, chatScrollWidget.getMaxScroll()));
        chatScrollWidget.refreshScroll();

        for (int i = 0; i < chatScrollWidget.children().size() && i < openList.size(); i++) {
            chatScrollWidget.children().get(i).setOpen(openList.get(i));
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        if (chatScrollWidget != null && screenInitialized) {
            chatScrollWidget.renderWidget(context, mouseX, mouseY, delta);
        }
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderBackground(context, mouseX, mouseY, delta);
        int x = width / 2;
        int y = height / 2;
        Identifier guiTexture = Identifier.of("civiliansmod", "textures/gui/chat_gui.png");
        context.drawTexture(guiTexture, x - 128, y - 83, 0, 0, 0, 256, 166, 256, 166);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (chatScrollWidget.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    //after close reset the screen is initialized
    @Override
    public void close() {
        screenInitialized = false;
        super.close();
    }
}
