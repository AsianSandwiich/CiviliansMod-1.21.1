package net.asian.civiliansmod.gui.widgets;

import net.asian.civiliansmod.chat.NpcChat;
import net.asian.civiliansmod.entity.NPCEntity;
import net.asian.civiliansmod.gui.CustomChatScreen;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ElementListWidget;

import java.util.ArrayList;
import java.util.List;

public class DialogueRowEntry extends ElementListWidget.Entry<DialogueRowEntry> {
    List<AbstractDialogueEntry> dialogueEntryList = new ArrayList<>();
    boolean customMode;

    public DialogueRowEntry(NPCEntity npc, NpcChat.ChatReason chatReason, List<String> strings, int base, CustomChatScreen screen, boolean customMode) {
        this.customMode = customMode;
        for (int i = 0; i < strings.size(); i++) {
            String s = strings.get(i);
            dialogueEntryList.add(new DialogueEntry( npc,0, 0, 112, 12, chatReason, screen, s, base + i, customMode));
        }

        if (strings.isEmpty() || strings.size() == 1) {
            dialogueEntryList.add(new AddDialogueEntry(npc,0, 0, 112, 12, chatReason, screen, customMode));
        }
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
        int i = 0;
        for (AbstractDialogueEntry entry : dialogueEntryList) {
            entry.setX(x + i);
            entry.setY(y);
            entry.render(context, x + i, y, mouseX, mouseY, entry.isMouseOver(mouseX, mouseY), tickDelta);
            i += 117;
        }
    }
}
