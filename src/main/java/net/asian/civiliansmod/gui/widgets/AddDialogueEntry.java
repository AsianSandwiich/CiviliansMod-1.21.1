package net.asian.civiliansmod.gui.widgets;

import net.asian.civiliansmod.CiviliansMod;
import net.asian.civiliansmod.chat.NpcChat;
import net.asian.civiliansmod.entity.NPCEntity;
import net.asian.civiliansmod.gui.AddDialogueScreen;
import net.asian.civiliansmod.gui.CustomChatScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;

public class AddDialogueEntry extends AbstractDialogueEntry {
    Identifier TEXTURE = Identifier.of(CiviliansMod.MOD_ID, "textures/gui/add_button.png");
    boolean customMode;

    protected AddDialogueEntry(NPCEntity npc, int x, int y, int width, int height, NpcChat.ChatReason chatReason, CustomChatScreen screen, boolean customMode) {
        super(x, y, width, height, chatReason, button -> {
            MinecraftClient.getInstance().setScreen(new AddDialogueScreen(npc, "", chatReason, screen, customMode));
        });
    }

    @Override
    public void render(DrawContext context, int x, int y, int mouseX, int mouseY, boolean hovered, float delta) {
        super.render(context, x, y, mouseX, mouseY, hovered, delta);
        context.drawTexture(RenderPipelines.GUI_TEXTURED, TEXTURE, x + width / 2 - 4, y + 2, 0, 0, 8, 8, 8, 8);
    }
}
