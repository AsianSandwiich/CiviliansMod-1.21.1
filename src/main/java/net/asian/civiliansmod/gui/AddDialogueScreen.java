package net.asian.civiliansmod.gui;

import net.asian.civiliansmod.chat.NpcChat;
import net.asian.civiliansmod.entity.NPCEntity;
import net.asian.civiliansmod.gui.widgets.TextButtonWidget;
import net.asian.civiliansmod.networking.payload.npc.dialogue.AddDialoguePayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class AddDialogueScreen extends AbstractDialogueEditionScreen {

    private final boolean customMode;

    public AddDialogueScreen(NPCEntity npc, String text, NpcChat.ChatReason reason, CustomChatScreen parent, boolean customMode) {
        super(npc, text, reason, parent);
        this.customMode = customMode;
    }

    @Override
    protected void init() {
        int x = width / 2;
        int y = height / 2;
        super.init();
        TextButtonWidget addButton = new TextButtonWidget(x + 6, y + 30, 60, 15, Text.translatable("civilians.gui.add"), button -> {
            String language = MinecraftClient.getInstance().getLanguageManager().getLanguage();

            String input = this.textFieldWidget.getText().trim();
            if (input.isEmpty()) {
                return; // no space in input
            }

            if (customMode) {
                npc.getChatManager().getCustomDialogues().computeIfAbsent(reason, r -> new ArrayList<>()).add(input);
            } else {
                Map<NpcChat.ChatReason, List<String>> langMap = npc.getChatManager().getTranslatedDialogues(language);
                for (NpcChat.ChatReason r : NpcChat.ChatReason.values()) {
                    langMap.computeIfAbsent(r, o -> new ArrayList<>(Collections.singletonList("...")));
                }
                langMap.get(reason).add(input);
            }

            parent.fullInit();
            AddDialoguePayload payload = new AddDialoguePayload(npc.getUuid(), reason.toString(), language, input, customMode);
            ClientPlayNetworking.send(payload);
            MinecraftClient.getInstance().setScreen(parent);
        }, 0xFFFFFF, 0xFF00FF00);

        addDrawableChild(addButton);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        int x = width / 2;
        int y = height / 2;
        context.drawCenteredTextWithShadow(this.client.textRenderer, Text.translatable("civilians.gui.add_dialogue"), x, y - 15, 0xFFFFFF);
    }
}
