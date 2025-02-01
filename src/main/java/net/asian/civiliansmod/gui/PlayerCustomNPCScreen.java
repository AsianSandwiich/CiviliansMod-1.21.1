package net.asian.civiliansmod.gui;

import net.asian.civiliansmod.CiviliansMod;
import net.asian.civiliansmod.entity.NPCEntity;
import net.asian.civiliansmod.util.NPCUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Class to display custom models choose by the player
 */
public class PlayerCustomNPCScreen extends CustomNPCScreen {
    public PlayerCustomNPCScreen(NPCEntity npc) {
        super(npc);
    }

    @Override
    protected List<Identifier> getDefaultSkins() {
        return NPCUtil.getDefaultCustomSkins();
    }

    @Override
    protected List<Identifier> getSlimSkins() {
        return NPCUtil.getSlimCustomSkins();
    }


    /**
     * method to convert an image to an Identifier used to display skins.
     * The method will verify for each file that it is a png file and that has the good dimension({@code 64x64} pixels
     * @param files the {@code Stream<Path>} that represents the files in the directory.
     * @param skins the {@code Set<Identifier>} that list the skins
     */
    private static void searchAndConvertSkins(Stream<@NotNull Path> files, List<Identifier> skins) {
        files.forEach((file) -> {
            if (file.endsWith(".png")) {
                try {
                    byte[] fileBytes = Files.readAllBytes(file);
                    NativeImage image = NativeImage.read(fileBytes);
                    if (image.getHeight() != 64 || image.getWidth() != 64) {
                        return;
                    }
                    NativeImageBackedTexture dynamicTexture = new NativeImageBackedTexture(image);
                    skins.add(MinecraftClient.getInstance().getTextureManager().registerDynamicTexture("custom_skin", dynamicTexture));
                } catch (IOException e) {
                    CiviliansMod.LOGGER.error("error while converting skin files");
                    e.printStackTrace();
                }
            }

        });
    }


}
