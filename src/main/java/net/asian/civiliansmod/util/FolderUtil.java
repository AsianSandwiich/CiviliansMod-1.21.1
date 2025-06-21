package net.asian.civiliansmod.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.asian.civiliansmod.CiviliansMod;
import net.asian.civiliansmod.chat.DefaultChat;
import net.asian.civiliansmod.chat.NpcChat;
import net.fabricmc.loader.api.FabricLoader;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FolderUtil {
    public static final Path CIVILIANS_PATH = FabricLoader.getInstance().getGameDir().resolve("civiliansmod");

    public static final Path DIALOGUES_PATH = CIVILIANS_PATH.resolve("dialogues");

    public static final Path SKIN_PATH = CIVILIANS_PATH.resolve("skins");
    public static final Path WIDE_SKIN_PATH = SKIN_PATH.resolve("wide");
    public static final Path SLIM_SKIN_PATH = SKIN_PATH.resolve("slim");

    public static void init() {
        CiviliansMod.LOGGER.info("Initializing folders");
        if (!CIVILIANS_PATH.toFile().exists()) {
            CIVILIANS_PATH.toFile().mkdirs();
        }
        if (!DIALOGUES_PATH.toFile().exists()) {
            DIALOGUES_PATH.toFile().mkdirs();
            Map<String, Map<NpcChat.ChatReason, List<String>>> defaultDialogues = DefaultChat.getDefaultChat();
            Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();

            defaultDialogues.forEach((language, content) -> {
                Path filePath = DIALOGUES_PATH.resolve(language + ".json");

                try (FileWriter writer = new FileWriter(filePath.toFile())) {
                    Map<String, List<String>> simplifiedContent = new HashMap<>();
                    content.forEach((chatReason, messages) -> simplifiedContent.put(chatReason.getName(), messages));

                    gson.toJson(simplifiedContent, writer);
                } catch (IOException e) {
                    e.fillInStackTrace();
                }
            });
        }
        if (!SKIN_PATH.toFile().exists()) {
            SKIN_PATH.toFile().mkdirs();
        }
        if (!WIDE_SKIN_PATH.toFile().exists()) {
            WIDE_SKIN_PATH.toFile().mkdirs();
        }
        if (!SLIM_SKIN_PATH.toFile().exists()) {
            SLIM_SKIN_PATH.toFile().mkdirs();
        }
    }

}
