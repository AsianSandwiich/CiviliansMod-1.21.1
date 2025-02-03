package net.asian.civiliansmod.util;

import net.asian.civiliansmod.CiviliansMod;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Environment(EnvType.CLIENT)
public class NPCUtil {
    /**
     * list of all npc textures
     */
    private static List<Identifier> skins = new ArrayList<>();

    /**
     * array representing limitations of the variants
     */
    static int[] indexes;

    public static boolean isSlim(int index) {
        if (index <= indexes[1])
            return false;
        if (index <= indexes[2])
            return true;
        return index > indexes[3];
    }

    public static int[] getDefaultSkinIndexes() {
        return new int[]{0, indexes[1]};
    }

    public static int[] getSlimSkinIndexes() {
        return new int[]{indexes[1] + 1, indexes[2]};
    }

    public static int[] getDefaultCustomSkinIndexes() {
        return new int[]{indexes[2] + 1, indexes[3]};
    }

    public static boolean hasDefaultSkin(int index) {
        return index < indexes[1];
    }

    public static boolean hasSlimSkin(int index) {
        return index > indexes[1] && index <= indexes[2];
    }

    public static int[] getSlimCustomSkinIndexes() {
        return new int[]{indexes[3] + 1, indexes[4]};
    }

    public static List<Identifier> getDefaultSkins() {
        List<Identifier> skins = new ArrayList<>();
        MinecraftClient.getInstance().getResourceManager().findResources("textures/entity/npc/wide", path -> path.toString().endsWith(".png")).forEach((id, resource) -> {
            skins.add(id);
        });
        return skins;
    }

    public static List<Identifier> getSlimSkins() {
        List<Identifier> skins = new ArrayList<>();
        MinecraftClient.getInstance().getResourceManager().findResources("textures/entity/npc/slim", path -> path.toString().endsWith(".png")).forEach((id, resource) -> {
            skins.add(id);
        });
        return skins;
    }

    public static List<Identifier> getDefaultCustomSkins() {
        try (var files = Files.list(MinecraftClient.getInstance().runDirectory.toPath().resolve("civiliansmod_skins_wide"))) {
            return searchAndConvertSkins(files);
        } catch (
                IOException e) {
            CiviliansMod.LOGGER.error("error while listing skin files");
            e.printStackTrace();
        }
        return null;
    }

    public static List<Identifier> getSlimCustomSkins() {
        try (var files = Files.list(MinecraftClient.getInstance().runDirectory.toPath().resolve("civiliansmod_skins_slim"))) {
            return searchAndConvertSkins(files);
        } catch (
                IOException e) {
            CiviliansMod.LOGGER.error("error while listing skin files");
            e.printStackTrace();
        }
        return null;
    }

    public static Identifier getNPCTexture(int texture) {
        if (texture > skins.size() - 1) {
            texture = Random.create().nextInt(skins.size() - 1);
        }
        return skins.get(texture);
    }


    /**
     * method to refresh all the npc textures.
     */
    public static void refreshTextures() {
        indexes = new int[5];
        skins = new ArrayList<>();
        indexes[0] = 0;
        skins.addAll(getDefaultSkins());

        indexes[1] = skins.size() - 1;
        skins.addAll(getSlimSkins());

        indexes[2] = skins.size() - 1;
        var customDefault = getDefaultCustomSkins();

        var customSlim = getSlimCustomSkins();

        if (customDefault != null) {
            skins.addAll(customDefault);
        }
        indexes[3] = skins.size() - 1;

        if (customSlim != null) {
            skins.addAll(customSlim);
        }
        indexes[4] = skins.size() - 1;
    }


    /**
     * Method to convert an image to an Identifier used to display skins.
     * The method will verify for each file that it is a png file and that has the good dimension({@code 64x64} pixels
     *
     * @param files the {@code Stream<Path>} that represents the files in the directory.
     */
    private static List<Identifier> searchAndConvertSkins(Stream<@NotNull Path> files) {
        List<Identifier> skins = new ArrayList<>();
        files.forEach((file) -> {
            if (file.getFileName().toString().endsWith(".png")) {
                try {
                    InputStream stream = Files.newInputStream(file);
                    try {
                        NativeImage image = NativeImage.read(stream);
                        if (image.getHeight() != 64 || image.getWidth() != 64) {
                            return;
                        }
                        NativeImageBackedTexture dynamicTexture = new NativeImageBackedTexture(image);

                        // Use registerTexture with a unique Identifier
                        Identifier textureId = Identifier.of(CiviliansMod.MOD_ID, "custom_skin_" + UUID.randomUUID());
                        MinecraftClient.getInstance().getTextureManager().registerTexture(textureId, dynamicTexture);

                        skins.add(textureId);
                        image.close();
                    } catch (Exception e) {
                        CiviliansMod.LOGGER.error("Error while converting skin files", e);
                    }

                } catch (IOException e) {
                    CiviliansMod.LOGGER.error("Error while converting skin files", e);
                }
            }

        });
        return skins;
    }

}
