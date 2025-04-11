package net.asian.civiliansmod.gui.widgets;

import net.asian.civiliansmod.chat.NpcChat;
import net.asian.civiliansmod.gui.ConfirmScreen;
import net.asian.civiliansmod.gui.CustomChatScreen;
import net.asian.civiliansmod.gui.EditDialogueScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.MultilineText;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.util.ArrayList;

public class DialogueEntry extends AbstractDialogueEntry {
    DeleteWidget deleteWidget;
    String dialogue;
    int index;

    protected DialogueEntry(int x, int y, int width, int height, NpcChat.ChatReason chatReason, CustomChatScreen screen, String dialogue, int index) {
        super(x, y, width, height, chatReason, button -> {
            EditDialogueScreen editScreen= new EditDialogueScreen(dialogue, chatReason, index, screen);
            MinecraftClient.getInstance().setScreen(editScreen);
        });
        deleteWidget = new DeleteWidget(x + 40, y, 10, 10, button -> {
            ConfirmScreen confirmScreen = new ConfirmScreen(screen, button1 -> {
                NpcChat.dialogues.computeIfAbsent(chatReason, (o) -> new ArrayList<>()).remove(dialogue);
                screen.fullInit();
                MinecraftClient.getInstance().setScreen(screen);
            }, button1 -> {
                MinecraftClient.getInstance().setScreen(screen);
            }, MultilineText.create(MinecraftClient.getInstance().textRenderer, Text.translatable("gui.civilians.delete_dialogue")),
                    0xFFFF8000
            );

            MinecraftClient.getInstance().setScreen(confirmScreen);
        });
        this.dialogue = dialogue;
        this.index = index;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderWidget(context, mouseX, mouseY, delta);
    }

    @Override
    public void render(DrawContext context, int x, int y, int mouseX, int mouseY, boolean hovered, float delta) {
        super.render(context, x, y, mouseX, mouseY, hovered, delta);

        deleteWidget.setX(x + 100);
        deleteWidget.setY(y + 1);
        float scale = 0.5f;

        int maxWidth = (int) ((96) / scale);

        String textToDraw = MinecraftClient.getInstance().textRenderer.trimToWidth(dialogue, maxWidth - MinecraftClient.getInstance().textRenderer.getWidth("...")) + (MinecraftClient.getInstance().textRenderer.getWidth(dialogue) > maxWidth ? "..." : "");

        context.getMatrices().push();
        context.getMatrices().scale(scale, scale, 1.0F);
        context.drawText(MinecraftClient.getInstance().textRenderer, textToDraw, (int) ((x + 3) / scale), (int) ((y + 4) / scale), 0xFFFFFF, true);
        context.getMatrices().pop();

        deleteWidget.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if (deleteWidget.isMouseOver(mouseX, mouseY)) {
            deleteWidget.onClick(mouseX, mouseY);
        } else if (this.isMouseOver(mouseX, mouseY)) {
            this.onPress();
        }
    }
}
