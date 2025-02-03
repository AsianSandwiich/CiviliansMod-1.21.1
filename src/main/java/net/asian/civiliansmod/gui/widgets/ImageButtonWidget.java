package net.asian.civiliansmod.gui.widgets;


import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class ImageButtonWidget extends ButtonWidget {
    private final Identifier image;

    public ImageButtonWidget(int x, int y, int width, int height, Identifier image, PressAction onPress) {
        super(x, y, width, height, Text.literal(""), onPress, DEFAULT_NARRATION_SUPPLIER);
        this.image = image;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderWidget(context, mouseX, mouseY, delta);
        context.drawTexture(
                RenderLayer::getText, // Provide the render layer function
                image,                // The texture Identifier
                this.getX() + 3,      // Destination X
                this.getY() + 3,      // Destination Y
                0,                    // U-coordinate (texture's X offset)
                0,                    // V-coordinate (texture's Y offset)
                this.width - 6,       // Draw width
                this.height - 6,      // Draw height
                this.width - 6,       // Texture width
                this.height - 6       // Texture height
        );
    }
}

