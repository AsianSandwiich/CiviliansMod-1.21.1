package net.asian.civiliansmod.util;

import net.asian.civiliansmod.CiviliansMod;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

@Environment(EnvType.CLIENT)
public class NPCUtil {
    public static final Map<Integer, SkinIdentifier> waitingSync = new HashMap<>();


    static final List<SkinIdentifier> skins = new ArrayList<>();

    public static List<SkinIdentifier> getSkins() {
        return skins;
    }

    public static Map<SkinIdentifier, byte[]> images = new HashMap<>();

    public static boolean isSlim(int index) {
        return skins.get(index).slim();
    }


    private static void registerDefaultSkins() {
        MinecraftClient.getInstance().getResourceManager().findResources("textures/entity/npc/wide", path -> path.toString().endsWith(".png")).forEach((id, resource) -> {
            skins.add(new SkinIdentifier(id, false, false));
        });
    }

    private static void registerSlimSkins() {
        MinecraftClient.getInstance().getResourceManager().findResources("textures/entity/npc/slim", path -> path.toString().endsWith(".png")).forEach((id, resource) -> {
            skins.add(new SkinIdentifier(id, true, false));
        });
    }

    private static void registerDefaultCustomSkins() {
        try (var files = Files.list(FolderUtil.WIDE_SKIN_PATH)) {
            searchAndConvertSkins(files, false);
        } catch (
                IOException e) {
            CiviliansMod.LOGGER.error("error while listing skin files");
            e.printStackTrace();
        }
    }

    private static void registerSlimCustomSkins() {
        try (var files = Files.list(FolderUtil.SLIM_SKIN_PATH)) {
            searchAndConvertSkins(files, true);
        } catch (
                IOException e) {
            CiviliansMod.LOGGER.error("error while listing skin files");
            e.printStackTrace();
        }
    }

    public static SkinIdentifier getNPCTexture(int texture) {
        if (skins.isEmpty()) {
            throw new IllegalStateException("No NPC skins are registered!");
        }
        if (texture < 0 || texture >= skins.size()) {
            texture = Random.create().nextInt(skins.size());
        }
        return skins.get(texture);
    }


    /**
     * method to refresh all the npc textures.
     */
    public static void refreshTextures() {
        skins.clear();
        registerDefaultSkins();
        registerSlimSkins();

        registerDefaultCustomSkins();
        registerSlimCustomSkins();
    }


    /**
     * Method to convert an image to an Identifier used to display skins.
     * The method will verify for each file that it is a png file and that has the good dimension({@code 64x64} pixels
     *
     * @param files the {@code Stream<Path>} that represents the files in the directory.
     */
    private static void searchAndConvertSkins(Stream<@NotNull Path> files, boolean slim) {
        AtomicInteger i = new AtomicInteger();
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
                        skins.add(new SkinIdentifier(MinecraftClient.getInstance().getTextureManager().registerDynamicTexture(CiviliansMod.MOD_ID + "_custom_skin", dynamicTexture), slim, true));
                        images.put(skins.getLast(), image.getBytes());
                        System.out.println(i.getAndIncrement());
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
    }

    public static void registerSkin(){

    }


}
