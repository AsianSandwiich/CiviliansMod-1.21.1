package net.asian.civiliansmod.util;

import net.asian.civiliansmod.CiviliansMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class NPCUtil {
    /**
     * list of all npc textures
     */
    private static List<Identifier> skins = new ArrayList<>();
    private static int defaultSkins = 0;
    private static int slimSkins = 0;
    private static int customDefaultSkins = 0;
    private static int customSlimSkins = 0;

    public static int getDefaultSkinsCount() {
        return defaultSkins;
    }

    public static int getSlimSkinsCount() {
        return slimSkins;
    }

    public static int getCustomDefaultSkinsCount() {
        return customDefaultSkins;
    }

    public static int getCustomSlimSkinsCount() {
        return customSlimSkins;
    }


    public static List<Identifier> getDefaultSkins() {
        defaultSkins = 0;
        List<Identifier> skins = new ArrayList<>();
        MinecraftClient.getInstance().getResourceManager().findResources("textures/entity/npc/default", path -> path.toString().endsWith(".png")).forEach((id, resource) -> {
            skins.add(id);
            defaultSkins++;
        });
        return skins;
    }

    public static List<Identifier> getSlimSkins() {
        slimSkins = 0;
        MinecraftClient.getInstance().getResourceManager().findResources("textures/entity/npc/slim", path -> path.toString().endsWith(".png")).forEach((id, resource) -> {
            skins.add(id);
            slimSkins++;
        });
        return skins;
    }

    public static List<Identifier> getDefaultCustomSkins() {
        try (var files = Files.list(MinecraftClient.getInstance().runDirectory.toPath().resolve("civiliansmod_skins_default"))) {
            return searchAndConvertSkins(files, true);
        } catch (
                IOException e) {
            CiviliansMod.LOGGER.error("error while listing skin files");
            e.printStackTrace();
        }
        return null;
    }

    public static List<Identifier> getSlimCustomSkins() {
        try (var files = Files.list(MinecraftClient.getInstance().runDirectory.toPath().resolve("civiliansmod_skins_slim"))) {
            return searchAndConvertSkins(files, false);
        } catch (
                IOException e) {
            CiviliansMod.LOGGER.error("error while listing skin files");
            e.printStackTrace();
        }
        return null;
    }

    public static Identifier getNPCTexture(int texture) {
        return skins.get(texture);
    }


    /**
     * method to refresh all the npc textures
     */
    public static void refreshTextures() {
        skins = new ArrayList<>();
        skins.addAll(getDefaultSkins());
        skins.addAll(getSlimSkins());
        skins.addAll(Objects.requireNonNull(getDefaultCustomSkins()));
        skins.addAll(Objects.requireNonNull(getSlimCustomSkins()));

        System.out.println(skins);
        System.out.println(skins.size());
    }


    /**
     * method to convert an image to an Identifier used to display skins.
     * The method will verify for each file that it is a png file and that has the good dimension({@code 64x64} pixels
     *
     * @param files the {@code Stream<Path>} that represents the files in the directory.
     */
    private static List<Identifier> searchAndConvertSkins(Stream<@NotNull Path> files, boolean defaultSkin) {
        if (defaultSkin) {
            customDefaultSkins = 0;
        } else {
            customSlimSkins = 0;
        }
        List<Identifier> skins = new ArrayList<>();
        files.forEach((file) -> {
            System.out.println(file.getFileName());
            if (file.getFileName().toString().endsWith(".png")) {
                try {

                    InputStream stream = Files.newInputStream(file);
                    byte[] fileBytes = Files.readAllBytes(file);
                    try {
                        //NativeImage image = loadTexture(stream);
                        NativeImage nativeImage = new NativeImage(64, 64, false);
                        NativeImage image = nativeImage.read(fileBytes);
                        if (image.getHeight() != 64 || image.getWidth() != 64) {
                            return;
                        }
                        if (defaultSkin) {
                            customDefaultSkins++;
                        } else {
                            customSlimSkins++;
                        }
                        NativeImageBackedTexture dynamicTexture = new NativeImageBackedTexture(64, 64, false/*"image"*/);
                        dynamicTexture.setImage(image);
                        skins.add(MinecraftClient.getInstance().getTextureManager().registerDynamicTexture("custom_skin", dynamicTexture));
                        dynamicTexture.close();
                        image.close();
                    } catch (Exception e) {
                        CiviliansMod.LOGGER.error("error while converting skin files");
                        e.printStackTrace();
                    }

                } catch (IOException e) {
                    CiviliansMod.LOGGER.error("error while converting skin files");
                    e.printStackTrace();
                }
            }

        });
        return skins;
    }

}
