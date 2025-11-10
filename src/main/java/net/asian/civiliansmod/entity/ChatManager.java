package net.asian.civiliansmod.entity;

import net.asian.civiliansmod.CiviliansMod;
import net.asian.civiliansmod.chat.DefaultChat;
import net.asian.civiliansmod.chat.NpcChat;
import net.asian.civiliansmod.networking.payload.npc.dialogue.DialogueSyncPayload;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.*;
import java.util.function.BiConsumer;

public class ChatManager {

    private final NPCEntity npc;
    private final Map<String, Map<NpcChat.ChatReason, List<String>>> dialogues;
    private final Map<NpcChat.ChatReason, List<String>> customDialogues = new EnumMap<>(NpcChat.ChatReason.class);

    public ChatManager(NPCEntity npc) {
        this.npc = npc;
        this.dialogues = new HashMap<>(DefaultChat.getDefaultChat());
    }

    public String getRandomChat(String language, NpcChat.ChatReason reason) {
        List<String> custom = customDialogues.get(reason);
        if (custom != null && !custom.isEmpty()) {
            return custom.get(Random.create().nextInt(custom.size()));
        }

        Map<NpcChat.ChatReason, List<String>> langDialogues = dialogues.get(language);
        if (langDialogues == null || langDialogues.isEmpty()) {
            CiviliansMod.LOGGER.warn("[CiviliansMod] No dialogues for language {}, falling back to en_us (NPC ID: {})", language, npc.getId());
            langDialogues = DefaultChat.getDefaultChat().get("en_us");
        }

        if (langDialogues == null && !dialogues.isEmpty()) {
            langDialogues = dialogues.values().iterator().next();
            CiviliansMod.LOGGER.warn("[CiviliansMod] No en_us dialogues, using first available language for NPC {}", npc.getId());
        }

        if (langDialogues == null) {
            CiviliansMod.LOGGER.warn("[CiviliansMod] No dialogues available at all, using placeholder for NPC {}", npc.getId());
            langDialogues = new EnumMap<>(NpcChat.ChatReason.class);
        }

        if (dialogues.isEmpty()) {
            CiviliansMod.LOGGER.error("[CiviliansMod] Dialogue map empty, loading default dialogues for NPC {}", npc.getId());
            dialogues.putAll(DefaultChat.getDefaultChat());
        }

        List<String> messages = langDialogues.getOrDefault(reason, Collections.singletonList("..."));
        if (messages.isEmpty()) messages = Collections.singletonList("...");
        return messages.get(Random.create().nextInt(messages.size()));
    }

    public Map<NpcChat.ChatReason, List<String>> getDialoguesForLanguage(String language) {
        Map<NpcChat.ChatReason, List<String>> languageMap = dialogues.get(language);

        if (languageMap == null || languageMap.isEmpty()) {
            CiviliansMod.LOGGER.warn("[CiviliansMod] No dialogues for language {}, falling back to en_us (NPC ID: {})", language, npc.getId());
            languageMap = DefaultChat.getDefaultChat().get("en_us");
        }
        if (languageMap == null && !dialogues.isEmpty()) {
            languageMap = dialogues.values().iterator().next();
            CiviliansMod.LOGGER.warn("[CiviliansMod] No en_us dialogues, using first available language for NPC {}", npc.getId());
        }
        if (languageMap == null) {
            CiviliansMod.LOGGER.warn("[CiviliansMod] No dialogues available at all, creating default placeholder map for NPC {}", npc.getId());
            languageMap = new EnumMap<>(NpcChat.ChatReason.class);
        }

        for (NpcChat.ChatReason reason : NpcChat.ChatReason.values()) {
            languageMap.computeIfAbsent(reason, r -> new ArrayList<>(Collections.singletonList("...")));
        }
        return languageMap;
    }

    public Map<NpcChat.ChatReason, List<String>> getTranslatedDialogues(String language) {
        return getDialoguesForLanguage(language);
    }

    public NbtCompound saveDialogues() {
        NbtCompound main = new NbtCompound();

        NbtCompound langs = new NbtCompound();
        for (Map.Entry<String, Map<NpcChat.ChatReason, List<String>>> langEntry : dialogues.entrySet()) {
            NbtCompound langCompound = new NbtCompound();
            for (Map.Entry<NpcChat.ChatReason, List<String>> reasonEntry : langEntry.getValue().entrySet()) {
                NbtList list = new NbtList();
                for (String msg : reasonEntry.getValue()) {
                    list.add(NbtString.of(msg));
                }
                langCompound.put(reasonEntry.getKey().getName(), list);
            }
            langs.put(langEntry.getKey(), langCompound);
        }
        main.put("Languages", langs);

        NbtCompound customCompound = new NbtCompound();
        for (Map.Entry<NpcChat.ChatReason, List<String>> entry : customDialogues.entrySet()) {
            NbtList list = new NbtList();
            for (String msg : entry.getValue()) {
                list.add(NbtString.of(msg));
            }
            customCompound.put(entry.getKey().getName(), list);
        }
        main.put("CustomDialogues", customCompound);

        return main;
    }

    public void setFromReadView(ReadView readView) {
        Optional<Dialogue.Dialogues> dialoguesOptional = readView.read("dialogues", Dialogue.Dialogues.CODEC);
        if (dialoguesOptional.isEmpty()) {
            if (dialogues.isEmpty()) dialogues.putAll(DefaultChat.getDefaultChat());
            return;
        }

        Dialogue.Dialogues d = dialoguesOptional.get();
        Map<String, Map<NpcChat.ChatReason, List<String>>> chats = new HashMap<>();
        d.dialogues().forEach((lang, languageDialogue) -> {
            Map<NpcChat.ChatReason, List<String>> perReason = new EnumMap<>(NpcChat.ChatReason.class);
            languageDialogue.languageDialogue().forEach((reason, chatReasonDialogue) ->
                    perReason.put(reason, new ArrayList<>(chatReasonDialogue.sayings())));
            chats.put(lang, perReason);
        });

        this.dialogues.clear();
        this.dialogues.putAll(chats);

        if (this.dialogues.isEmpty()) this.dialogues.putAll(DefaultChat.getDefaultChat());
    }

    public Map<String, Map<NpcChat.ChatReason, List<String>>> getDialogues() {
        return dialogues;
    }

    public void setDialogues(Map<String, Map<NpcChat.ChatReason, List<String>>> newDialogues) {
        dialogues.clear();
        dialogues.putAll(newDialogues);
    }

    public Map<NpcChat.ChatReason, List<String>> getCustomDialogues() {
        return customDialogues;
    }

    public void setCustomDialogues(Map<NpcChat.ChatReason, List<String>> map) {
        customDialogues.clear();
        customDialogues.putAll(map);
    }

    public void markDialoguesDirty(UUID avoid) {
        if (!(npc.getWorld() instanceof ServerWorld serverWorld)) return;

        for (ServerPlayerEntity player : serverWorld.getPlayers(p -> avoid == null || !p.getUuid().equals(avoid))) {
            try {
                ServerPlayNetworking.send(player, new DialogueSyncPayload(npc.getId(), dialogues, customDialogues));
            } catch (IOException e) {
                CiviliansMod.LOGGER.error("[CiviliansMod] Failed to sync dialogues to player {}", player.getGameProfile().getName(), e);
            }
        }
    }

    public void updateDialoguesForLanguage(String language, Map<NpcChat.ChatReason, List<String>> newDialogues) {
        this.dialogues.put(language, new HashMap<>(newDialogues));
        markDialoguesDirty(null);
    }

    public void updateDialogueForLanguageAndReason(String language, NpcChat.ChatReason reason, List<String> messages) {
        this.dialogues.computeIfAbsent(language, k -> new EnumMap<>(NpcChat.ChatReason.class))
                .put(reason, new ArrayList<>(messages));
        markDialoguesDirty(null);
    }

    public void addLanguage(String language, Map<NpcChat.ChatReason, List<String>> dialogues) {
        this.dialogues.put(language, new HashMap<>(dialogues));
        markDialoguesDirty(null);
    }

    public void removeLanguage(String language) {
        this.dialogues.remove(language);
        markDialoguesDirty(null);
    }

    public Set<String> getAvailableLanguages() {
        return dialogues.keySet();
    }

    public boolean hasLanguage(String language) {
        return dialogues.containsKey(language) && !dialogues.get(language).isEmpty();
    }

    public void setLanguageMap(Map<String, Map<NpcChat.ChatReason, List<String>>> newLanguageMap) {
        this.dialogues.clear();
        this.dialogues.putAll(newLanguageMap);
        markDialoguesDirty(null);
    }

    public static @NotNull BiConsumer<String, Map<NpcChat.ChatReason, List<String>>> getManageCompoundSave(NbtCompound mainCompound) {
        return (language, reasonToMessagesMap) -> {
            NbtCompound languageCompound = new NbtCompound();
            reasonToMessagesMap.forEach((reason, messages) -> {
                NbtList messageList = new NbtList();
                messages.forEach(message -> messageList.add(NbtString.of(message)));
                languageCompound.put(reason.getName(), messageList);
            });
            mainCompound.put(language, languageCompound);
        };
    }
}
