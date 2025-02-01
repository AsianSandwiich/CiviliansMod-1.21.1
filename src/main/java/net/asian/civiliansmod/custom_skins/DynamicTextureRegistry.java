package net.asian.civiliansmod.custom_skins;

import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.Identifier;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class DynamicTextureRegistry {

    // Registers a texture dynamically
    public static Identifier registerTexture(File textureFile, String namespace, String textureName) {
        try {
            // Read the image file using NativeImage
            NativeImage skinImage = NativeImage.read(new FileInputStream(textureFile));
            NativeImageBackedTexture dynamicTexture = new NativeImageBackedTexture(skinImage);

            // Create a unique Identifier for this texture
            Identifier textureId = Identifier.of(namespace, textureName);

            // Get the TextureManager instance
            TextureManager textureManager = net.minecraft.client.MinecraftClient.getInstance().getTextureManager();

            // Register the texture with Minecraft's TextureManager
            textureManager.registerTexture(textureId, dynamicTexture);

            System.out.println("Successfully registered texture: " + textureId);
            return textureId;
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to load texture from file: " + textureFile.getAbsolutePath());
        }
        return null;
    }

}