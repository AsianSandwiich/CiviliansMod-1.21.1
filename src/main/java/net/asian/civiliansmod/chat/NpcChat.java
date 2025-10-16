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
    private static String currentLoadedLanguage = "en_us";

    public static String getRandomChat(ChatReason reason, String requestedLanguage) {
        //is requested language loaded?
        if (!requestedLanguage.equals(currentLoadedLanguage)) {
            CiviliansMod.LOGGER.debug("Language changed from {} to {}, reloading dialogues", currentLoadedLanguage, requestedLanguage);
            loadLanguage(requestedLanguage);
        }

        List<String> languagechat = dialogues.get(reason);

        if (languagechat == null || languagechat.isEmpty()) {
            CiviliansMod.LOGGER.debug("No dialogues loaded for reason: {}, using internal defaults", reason.getName());
            return getInternalFallback(reason, requestedLanguage);
        }

        return languagechat.get(Random.create().nextInt(languagechat.size()));
    }

    private static String getInternalFallback(ChatReason reason, String language) {
        Map<String, Map<ChatReason, List<String>>> defaultChats = DefaultChat.getDefaultChat();
        Map<ChatReason, List<String>> languageChats = defaultChats.get(language);

        if (languageChats == null || languageChats.isEmpty()) {
            CiviliansMod.LOGGER.debug("Language {} not found in internal defaults, falling back to en_us", language);
            languageChats = defaultChats.get("en_us");
        }

        if (languageChats == null || languageChats.isEmpty()) {
            CiviliansMod.LOGGER.debug("en_us not found, using first available language");
            languageChats = defaultChats.values().stream().findFirst().orElse(Collections.emptyMap());
        }

        List<String> chat = languageChats.get(reason);

        if (chat == null || chat.isEmpty()) {
            CiviliansMod.LOGGER.warn("No chat found for reason: {} in language: {}, using default",
                    reason.getName(), language);
            return "...";
        }

        return chat.get(Random.create().nextInt(chat.size()));
    }

    public static void registerChat() {
        CiviliansMod.LOGGER.info("Registering dialogues from internal defaults");
        loadLanguage(getCurrentLanguage());
    }

    public static void refresh(){
        CiviliansMod.LOGGER.info("Refreshing dialogues from internal defaults");
        loadLanguage(getCurrentLanguage());
    }

    private static String getCurrentLanguage() {
        try {
            if (MinecraftClient.getInstance() != null &&
                    MinecraftClient.getInstance().getLanguageManager() != null) {
                return MinecraftClient.getInstance().getLanguageManager().getLanguage();
            }
        } catch (Exception e) {
            CiviliansMod.LOGGER.warn("Could not determine language, using en_us as fallback", e);
        }
        return "en_us";
    }

    private static void loadLanguage(String languageCode) {
        dialogues.clear(); //build allways

        if (languageCode == null || languageCode.isEmpty()) {
            languageCode = getCurrentLanguage();
            CiviliansMod.LOGGER.debug("No language provided, using current client language: {}", languageCode);
        }

        currentLoadedLanguage = languageCode;

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
            CiviliansMod.LOGGER.error("Couldn't read dialogue JSON for language: {}", languageCode, e);
        }

        if (customcontent == null) {
            CiviliansMod.LOGGER.info("Using built-in default chat for language: {}", languageCode);
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
                List<String> fallbackChat = englishDefaults.getOrDefault(reason, Collections.singletonList("..."));
                reasons.addAll(fallbackChat);
                CiviliansMod.LOGGER.warn("Missing JSON section for '{}', using {} default dialogues.", reason.getName(), fallbackChat.size());
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
