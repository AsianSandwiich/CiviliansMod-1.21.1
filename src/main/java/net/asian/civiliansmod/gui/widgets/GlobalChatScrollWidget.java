package net.asian.civiliansmod.gui.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import net.asian.civiliansmod.entity.NPCEntity;
import net.asian.civiliansmod.gui.CustomChatScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.util.math.MathHelper;

public class GlobalChatScrollWidget extends ElementListWidget<ChatReasonEntryScrollContainer> {
    NPCEntity npc;
    CustomChatScreen screen;

    public GlobalChatScrollWidget(NPCEntity npc, MinecraftClient minecraftClient, int width, int height, int x, int y, int itemHeight, CustomChatScreen screen) {
        super(minecraftClient, width, height, y, itemHeight);
        this.npc = npc;
        this.screen = screen;
        this.setRenderHeader(false, 0);
        this.setPosition(x, y);
        refreshChildren();
    }

    public void refreshChildren() {
        this.children().clear();
        npc.getChatHandler().getTranslatedDialogues(MinecraftClient.getInstance().getLanguageManager().getLanguage())
                .forEach((chatReason, strings) -> {
                    System.out.println("[CiviliansMod] Loading " + (strings != null ? strings.size() : 0) + " entries for reason: " + chatReason);
                    if (strings != null && !strings.isEmpty()) {
                        this.children().add(new ChatReasonEntryScrollContainer(npc, chatReason, strings, screen));
                    }
                });
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
        if (this.isScrollbarVisible()) {
            int contentHeight = getTotalContentHeight();
            int visibleHeight = this.height;

            int scrollbarHeight = (int) ((float) visibleHeight * visibleHeight / (float) contentHeight);
            scrollbarHeight = MathHelper.clamp(scrollbarHeight, 32, visibleHeight - 8);

            int scrollY = (int) (this.getScrollAmount() * (visibleHeight - scrollbarHeight) / (float) getMaxScroll()) + this.getY();
            scrollY = Math.max(scrollY, this.getY());

            int scrollbarX = this.getScrollbarX();
            RenderSystem.enableBlend();
            context.fill(scrollbarX, scrollY - 2, scrollbarX + 3, scrollY + scrollbarHeight, 0xFFAAAAAA);
            RenderSystem.disableBlend();
        }
    }

    protected int getEntryTop(int index) {
        int y = this.getY() - (int) this.getScrollAmount();
        for (int i = 0; i < index; i++) {
            y += this.children().get(i).getHeight();
        }
        return y;
    }

    protected int getTotalContentHeight() {
        return this.children().stream().mapToInt(ChatReasonEntryScrollContainer::getHeight).sum();
    }

    @Override
    protected int getMaxPosition() {
        return getTotalContentHeight();
    }

    @Override
    public int getMaxScroll() {
        return Math.max(0, getTotalContentHeight() - this.height);
    }

    protected void renderList(DrawContext context, int mouseX, int mouseY, float delta) {
        int rowLeft = this.getRowLeft();
        int rowWidth = this.getRowWidth();
        int entryCount = this.getEntryCount();

        int y = this.getY() - (int) this.getScrollAmount();
        for (int i = 0; i < entryCount; i++) {
            ChatReasonEntryScrollContainer entry = this.children().get(i);
            System.out.println("[CiviliansMod] Rendering entry " + i + " with height " + entry.getHeight());
            int entryHeight = entry.getHeight();

            if (y + entryHeight >= this.getY() && y <= this.getBottom()) {
                this.renderEntry(context, mouseX, mouseY, delta, i, rowLeft, y, rowWidth, entryHeight);
            }

            y += entryHeight;
        }
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
            dialogueEntryScrollContainer.entries.forEach(entry -> {
                entry.dialogueEntryList.forEach(dialogueEntry -> {
                    dialogueEntry.onClick(mouseX, mouseY);
                });
            });
        });
    }
}
