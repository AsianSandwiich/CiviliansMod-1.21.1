package net.asian.civiliansmod.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public class DebugUtil {
    public static void drawGrid(DrawContext context, int width, int height) {
        int centerX = width / 2;
        int centerY = height / 2;
        int gridSize = 10;
        int gridColor = 0x50A9A9A9; // Bleu (ARGB)

        for (int x = centerX; x < width; x += gridSize) {
            context.fill(x, 0, x + 1, height, gridColor); // Lignes verticales à droite
        }
        for (int x = centerX; x > 0; x -= gridSize) {
            context.fill(x, 0, x - 1, height, gridColor); // Lignes verticales à gauche
        }
        for (int y = centerY; y < height; y += gridSize) {
            context.fill(0, y, width, y + 1, gridColor); // Lignes horizontales en bas
        }
        for (int y = centerY; y > 0; y -= gridSize) {
            context.fill(0, y, width, y - 1, gridColor); // Lignes horizontales en haut
        }
    }

    public static void drawMouseInfo(DrawContext context, int width, int height, int mouseX, int mouseY) {
        int centerX = width / 2;
        int centerY = height / 2;

        int relativeX = mouseX - centerX;
        int relativeY = mouseY - centerY;

        String absoluteCoords = "Mouse: " + mouseX + ", " + mouseY;
        String relativeCoords = "Relative: " + relativeX + ", " + relativeY;

        int color = 0xFFFFFF; // Blanc

        context.drawText(MinecraftClient.getInstance().textRenderer, absoluteCoords, 5, 5, color, true);
        context.drawText(MinecraftClient.getInstance().textRenderer, relativeCoords, 5, 15, color, true);
    }
}
