package net.asian.civiliansmod.gui;

import net.asian.civiliansmod.chat.NpcChat;
import net.asian.civiliansmod.entity.NPCEntity;
import net.asian.civiliansmod.gui.widgets.TextButtonWidget;
import net.asian.civiliansmod.networking.payload.npc.dialogue.EditDialoguePayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.util.ArrayList;

public class EditDialogueScreen extends AbstractDialogueEditionScreen {
    int index;

    public EditDialogueScreen(NPCEntity npc, String text, NpcChat.ChatReason reason, int index, CustomChatScreen parent) {
        super(npc, text, reason, parent);
        this.index = index;
    }

    @Override
    protected void init() {
        int x = width / 2;
        int y = height / 2;
        super.init();
        TextButtonWidget saveButton = new TextButtonWidget(x + 6, y + 30, 60, 15, Text.translatable("civilians.gui.save"), button -> {
            String language = MinecraftClient.getInstance().getLanguageManager().getLanguage();
            npc.getChatHandler().getTranslatedDialogues(language).computeIfAbsent(reason, (o) -> new ArrayList<>()).set(index, this.textFieldWidget.getText());
            parent.fullInit();
            EditDialoguePayload payload = new EditDialoguePayload(npc.getUuid(), language, reason.toString(), index, this.textFieldWidget.getText());
            ClientPlayNetworking.send(payload);
            MinecraftClient.getInstance().setScreen(parent);
        }, 0xFFFFFF, 0xFF00FF00);

        addDrawableChild(saveButton);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        int x = width / 2;
        int y = height / 2;
        context.drawCenteredTextWithShadow(this.client.textRenderer, Text.translatable("civilians.gui.edit_dialogue"), x, y - 15, 0xFFFFFF);
    }
}
