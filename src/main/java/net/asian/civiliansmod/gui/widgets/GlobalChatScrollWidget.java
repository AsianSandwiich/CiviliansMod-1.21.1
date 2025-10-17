package net.asian.civiliansmod.gui.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import net.asian.civiliansmod.chat.NpcChat;
import net.asian.civiliansmod.entity.NPCEntity;
import net.asian.civiliansmod.gui.CustomChatScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;

public class GlobalChatScrollWidget extends ElementListWidget<ChatReasonEntryScrollContainer> {
    NPCEntity npc;
    CustomChatScreen screen;
    boolean customMode;

    public GlobalChatScrollWidget(NPCEntity npc, MinecraftClient minecraftClient, int width, int height, int x, int y, int itemHeight, CustomChatScreen screen, boolean customMode) {
        super(minecraftClient, width, height, y, itemHeight);
        this.npc = npc;
        this.screen = screen;
        this.customMode = customMode;
        this.setRenderHeader(false, 0);
        npc.getChatHandler().getTranslatedDialogues(minecraftClient.getLanguageManager().getLanguage()).forEach((chatReason, strings) -> {
            this.children().add(new ChatReasonEntryScrollContainer(npc, chatReason, strings, screen));
        });

        this.setPosition(x, y);
        refreshChildren();
    }

    public boolean isCustomMode() {
        return customMode;
    }

    public void refreshChildren() {
        this.children().clear();
        String language = MinecraftClient.getInstance().getLanguageManager().getLanguage();

        var dialoguesMap = customMode
                ? npc.getChatManager().getCustomDialogues()
                : npc.getChatManager().getTranslatedDialogues(language);

        dialoguesMap.forEach((chatReason, strings) -> {
            // add allways
            if (strings == null) strings = new ArrayList<>();
            this.children().add(new ChatReasonEntryScrollContainer(npc, chatReason, strings, screen, customMode));
        });

        // add placeholder
        if (dialoguesMap.isEmpty() && customMode) {
            for (NpcChat.ChatReason reason : NpcChat.ChatReason.values()) {
                this.children().add(new ChatReasonEntryScrollContainer(npc, reason, new ArrayList<>(), screen, true));
            }
        }
    }

    @Override
    public int getRowLeft() {
        return this.getX();
    }

    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        this.enableScissor(context);
        this.renderList(context, mouseX, mouseY, delta);
        context.disableScissor();
        renderScrollBar(context);
    }

    protected void renderScrollBar(DrawContext context) {
        if (this.visible) {
            int contentHeight = getTotalContentHeight();
            int visibleHeight = this.height;

            int scrollbarHeight = (int) ((float) visibleHeight * visibleHeight / (float) contentHeight);
            scrollbarHeight = MathHelper.clamp(scrollbarHeight, 32, visibleHeight - 8);

            int scrollY = (int) (this.getScrollY() * (visibleHeight - scrollbarHeight) / (float) getMaxScrollY()) + this.getY();
            scrollY = Math.max(scrollY, this.getY());

            int scrollbarX = this.getScrollbarX();
            // No need for blend states in modern versions as DrawContext handles it
            context.fill(scrollbarX, scrollY - 2, scrollbarX + 3, scrollY + scrollbarHeight, 0xFFAAAAAA);
        }
    }


    protected int getEntryTop(int index) {
        int y = this.getY() - (int) this.getScrollY();
        for (int i = 0; i < index; i++) {
            y += this.children().get(i).getHeight();
        }
        return y;
    }

    protected int getTotalContentHeight() {
        return this.children().stream().mapToInt(ChatReasonEntryScrollContainer::getHeight).sum();
    }

    protected void renderList(DrawContext context, int mouseX, int mouseY, float delta) {
        int rowLeft = this.getRowLeft();
        int rowWidth = this.getRowWidth();
        int entryCount = this.getEntryCount();

        int y = this.getY() - (int) this.getScrollY();
        for (int i = 0; i < entryCount; i++) {
            ChatReasonEntryScrollContainer entry = this.children().get(i);
            int entryHeight = entry.getHeight();

            if (y + entryHeight >= this.getY() && y <= this.getBottom()) {
                this.renderEntry(context, mouseX, mouseY, delta, i, rowLeft, y, rowWidth, entryHeight);
            }

            y += entryHeight;
        }
    }

    @Override
    public boolean mouseScrolled(double d, double e, double f, double g) {
        if (!this.visible) {
            return false;
        } else {
            this.setScrollY(this.getScrollY() - g * this.getDeltaYPerScroll());
            return true;
        }
    }

    @Override
    public int getMaxScrollY() {
        return getTotalContentHeight() - this.getHeight();
    }

    @Override
    protected int getScrollbarX() {
        return this.getX() + this.width - 3;
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        this.children().forEach(dialogueEntryScrollContainer -> {
            if (dialogueEntryScrollContainer.onClick(mouseX, mouseY)) {
                return;
            }
            if (dialogueEntryScrollContainer.open) {
                dialogueEntryScrollContainer.entries.forEach(entry -> {
                    entry.dialogueEntryList.forEach(dialogueEntry -> {
                        dialogueEntry.onClick(mouseX, mouseY);
                    });
                });
            }
        });
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!this.isMouseOver(mouseX, mouseY)) return false;

        for (ChatReasonEntryScrollContainer container : this.children()) {
            if (container.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }

        return false;
    }
}
