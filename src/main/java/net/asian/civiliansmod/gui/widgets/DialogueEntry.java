package net.asian.civiliansmod.gui.widgets;

import net.asian.civiliansmod.chat.NpcChat;
import net.asian.civiliansmod.entity.NPCEntity;
import net.asian.civiliansmod.gui.ConfirmScreen;
import net.asian.civiliansmod.gui.CustomChatScreen;
import net.asian.civiliansmod.gui.EditDialogueScreen;
import net.asian.civiliansmod.networking.payload.npc.dialogue.RemoveDialoguePayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.MultilineText;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.util.ArrayList;

public class DialogueEntry extends AbstractDialogueEntry {
    DeleteWidget deleteWidget;
    String dialogue;
    int index;
    boolean customMode;

    protected DialogueEntry(NPCEntity npc, int x, int y, int width, int height, NpcChat.ChatReason chatReason, CustomChatScreen screen, String dialogue, int index, boolean customMode) {
        super(x, y, width, height, chatReason, button -> {
            EditDialogueScreen editScreen = new EditDialogueScreen(npc, dialogue, chatReason, index, screen, customMode);
            MinecraftClient.getInstance().setScreen(editScreen);
        });
        deleteWidget = new DeleteWidget(x + 40, y, 10, 10, button -> {
            ConfirmScreen confirmScreen = new ConfirmScreen(screen, button1 -> {
                String language = MinecraftClient.getInstance().getLanguageManager().getLanguage();
                npc.getChatManager().getTranslatedDialogues(language).computeIfAbsent(chatReason, (o) -> new ArrayList<>()).remove(dialogue);
                RemoveDialoguePayload payload = new RemoveDialoguePayload(npc.getUuid(), language, chatReason.toString(), dialogue, customMode);
                ClientPlayNetworking.send(payload);
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

        //draw layour
        super.render(context, x, y, mouseX, mouseY, hovered, delta);

        //delete button
        deleteWidget.setX(x + width - 12);
        deleteWidget.setY(y + 1);

        //get text
        MinecraftClient client = MinecraftClient.getInstance();
        int maxWidth = width - 16;
        String textToDraw = client.textRenderer.trimToWidth(dialogue, maxWidth - client.textRenderer.getWidth("..."))
                + (client.textRenderer.getWidth(dialogue) > maxWidth ? "..." : "");

        //draw dialouge
        context.drawTextWithShadow(client.textRenderer, textToDraw, x + 4, y + 3, 0xFFFFFFFF);

        deleteWidget.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (deleteWidget.isMouseOver(mouseX, mouseY)) {
            deleteWidget.mouseClicked(mouseX, mouseY, button);
            return true;
        }
        if (this.isMouseOver(mouseX, mouseY)) {
            this.onPress.onPress(this);
            return true;
        }
        return false;
    }
}
