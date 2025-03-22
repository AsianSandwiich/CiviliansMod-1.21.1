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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NpcChat {
    public static Map<ChatReason, List<String>> dialogues = new HashMap<>();

    public static String getRandomChat(ChatReason reason) {
        List<String> chat = dialogues.get(reason);
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
        Path customDialogue = FolderUtil.DIALOGUES_PATH.resolve(MinecraftClient.getInstance().getLanguageManager().getLanguage() + ".json");
        if(!customDialogue.toFile().exists()){
            customDialogue = FolderUtil.DIALOGUES_PATH.resolve("en_us.json");
            if(!customDialogue.toFile().exists())
                return;
        }

        try {
            String jsonContent = Files.readString(customDialogue);

            JsonObject content = JsonParser.parseString(jsonContent).getAsJsonObject();

            for(NpcChat.ChatReason reasons : NpcChat.ChatReason.values()) {
                List<String> dialogues = new ArrayList<>();
                JsonArray jsonArray = content.get(reasons.getName()).getAsJsonArray();
                for(JsonElement jsonElement : jsonArray) {
                    dialogues.add(jsonElement.getAsString());
                }
                NpcChat.dialogues.put(reasons, dialogues);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public enum ChatReason {
        HURT("hurt"),
        INTERACT("interact");

        String name;

        ChatReason(String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }
    }
}
