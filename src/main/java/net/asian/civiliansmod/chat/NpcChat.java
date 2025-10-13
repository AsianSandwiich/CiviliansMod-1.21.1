package net.asian.civiliansmod.chat;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.asian.civiliansmod.CiviliansMod;
import net.asian.civiliansmod.util.FolderUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.random.Random;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class NpcChat {
    public static Map<ChatReason, List<String>> dialogues = new LinkedHashMap<>();


    public static String getRandomChat(ChatReason reason, String language) {
        List<String> chat = dialogues.get(reason);
        if (chat == null || chat.isEmpty()) {
            // fallback to DefaultChat
            Map<NpcChat.ChatReason, List<String>> defaultLang = DefaultChat.getDefaultChat()
                    .getOrDefault(language, DefaultChat.getDefaultChat().get("en_us"));
            chat = defaultLang.getOrDefault(reason, Collections.singletonList("..."));
        }
        return chat.get(Random.create().nextInt(chat.size()));
    }

    public static void registerChat() {
        CiviliansMod.LOGGER.info("Registering dialogues");
        collect();
    }

    public static void refresh(){
        collect();
    }

    private static void collect() {
        dialogues.clear(); //build allways

        String languageCode = MinecraftClient.getInstance().getLanguageManager().getLanguage();
        Path customDialogue = FolderUtil.DIALOGUES_PATH.resolve(languageCode + ".json");
        JsonObject customcontent = null;

        try {
            if(Files.exists(customDialogue)) {
                CiviliansMod.LOGGER.info("Loading dialogues for language {}", languageCode);
                String CustomJsonDialogue = Files.readString(customDialogue);
                customcontent = JsonParser.parseString(CustomJsonDialogue).getAsJsonObject();

            } else {
                //Default Dialouge Fallback to en_us.json
                Path defaultFallbackDialogue = FolderUtil.DIALOGUES_PATH.resolve("en_us.json");
                if (Files.exists(defaultFallbackDialogue)) {
                    CiviliansMod.LOGGER.info("Falling back to default en_us dialogues");
                    String CustomJsonDialouge = Files.readString(defaultFallbackDialogue);
                    customcontent = JsonParser.parseString(CustomJsonDialouge).getAsJsonObject();
                } else {
                    CiviliansMod.LOGGER.warn("No custom dialogues found – using built-in DefaultChat");
                }
            }
        } catch (IOException e) {
            CiviliansMod.LOGGER.error("Couldn't read default dialogue JSON", e);
        }

        if (customcontent == null) {
                Map<String, Map<NpcChat.ChatReason, List<String>>> fallback = DefaultChat.getDefaultChat();
                Map<NpcChat.ChatReason, List<String>> englishFallback = fallback.getOrDefault("en_us", Collections.emptyMap());
                for (ChatReason reason : ChatReason.values()) {
                    List<String> list = new ArrayList<>(englishFallback.getOrDefault(reason, Collections.singletonList("...")));
                    dialogues.put(reason, list);
                }
                return;
            }

        // read jsons
        for (ChatReason reason : ChatReason.values()) {
            List<String> reasons = new ArrayList<>();

            if (customcontent.has(reason.getName())) {
                JsonArray jsonArray = customcontent.get(reason.getName()).getAsJsonArray();
                for (JsonElement element : jsonArray) {
                    reasons.add(element.getAsString());
                }
            }

            // if json no entrys, add default
            if (reasons.isEmpty()) {
                Map<String, Map<NpcChat.ChatReason, List<String>>> defaults = DefaultChat.getDefaultChat();
                Map<NpcChat.ChatReason, List<String>> englishDefaults = defaults.getOrDefault("en_us", Collections.emptyMap());
                reasons.addAll(englishDefaults.getOrDefault(reason, Collections.singletonList("...")));
                CiviliansMod.LOGGER.warn("Missing JSON section for '{}', using defaults.", reason.getName());
            }

            dialogues.put(reason, reasons);
            CiviliansMod.LOGGER.info("Loaded {} dialogues for {}", reasons.size(), reason.getName());
        }
    }

    public enum ChatReason {
        HURT("hurt"),
        INTERACT("interact");

        final String name;

        ChatReason(String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }

        public static ChatReason fromName(String name) {
            for (ChatReason reason : values()) {
                if (reason.name.equalsIgnoreCase(name) || reason.name().equalsIgnoreCase(name)) {
                    return reason;
                }
            }
            throw new IllegalArgumentException("Unknown ChatReason: " + name);
        }
    }
}
