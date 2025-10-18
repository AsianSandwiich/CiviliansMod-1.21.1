package net.asian.civiliansmod.gui.widgets;

import net.asian.civiliansmod.chat.NpcChat;
import net.asian.civiliansmod.entity.NPCEntity;
import net.asian.civiliansmod.gui.CustomChatScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;

import java.util.ArrayList;
import java.util.List;

public class ChatReasonEntryScrollContainer extends ElementListWidget.Entry<ChatReasonEntryScrollContainer> {
    List<DialogueRowEntry> entries = new ArrayList<>();
    boolean customMode;
    boolean open = true;
    NpcChat.ChatReason chatReason;

    OpenWidget openWidget;

    public ChatReasonEntryScrollContainer(NPCEntity npc, final NpcChat.ChatReason chatReason, List<String> strings, CustomChatScreen screen, boolean customMode) {
        this.chatReason = chatReason;
        this.customMode = customMode;
        for (int i = 0; i < strings.size(); i += 2) {
            entries.add(new DialogueRowEntry(npc, chatReason, strings.subList(i, Math.min(i + 2, strings.size())), i, screen, customMode));
        }
        if (strings.size() % 2 == 0) {
            entries.add(new DialogueRowEntry(npc, chatReason, new ArrayList<>(), strings.size(), screen, customMode));
        }
        if (customMode && strings.isEmpty()) {
            entries.add(new DialogueRowEntry(npc, chatReason, new ArrayList<>(), 0, screen, true));
        }

        openWidget = new OpenWidget(0, 0, 10, 10, this, button -> open = !open);
    }

    @Override
    public List<? extends Selectable> selectableChildren() {
        return List.of();
    }

    @Override
    public List<? extends Element> children() {
        return List.of();
    }

    @Override
    public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
        openWidget.setX(x + 1);
        openWidget.setY(y);
        context.drawGuiTexture(RenderLayer::getGuiTextured, Identifier.ofVanilla("widget/button"), x, y, 145, 13, ColorHelper.fromFloats(1.0f, 0.5f, 0.5f, 1.0f));
        openWidget.render(context, mouseX, mouseY, tickDelta);
        context.drawText(MinecraftClient.getInstance().textRenderer, chatReason.getName(), x + 11, y + 2, 0xFFFFFF, true);
        if (open) {
            int i = 15;
            for (DialogueRowEntry entry : entries) {
                entry.render(context, 0, y + i, x, 110, 12, mouseX, mouseY, hovered, tickDelta);
                i += 15;
            }
        }
    }

    public int getHeight() {
        return 15 + (open ? (entries.size() * 15) : 0);
    }

    public boolean onClick(double mouseX, double mouseY) {
        //click on arrow
        if (openWidget.isMouseOver(mouseX, mouseY)) {
            openWidget.mouseClicked(mouseX, mouseY, 0);
            return true;
        }

        if (!open || entries.isEmpty()) return false;

        for (DialogueRowEntry row : entries) {
            if (row.mouseClicked(mouseX, mouseY, 0)) {
                return true;
            }
            for (AbstractDialogueEntry dialogue : row.dialogueEntryList) {
                if (dialogue.mouseClicked(mouseX, mouseY, 0)) {
                    return true;
                }
            }
        }
        return false;
    }
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return this.onClick(mouseX, mouseY);
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public boolean getOpen() {
        return open;
    }
}
