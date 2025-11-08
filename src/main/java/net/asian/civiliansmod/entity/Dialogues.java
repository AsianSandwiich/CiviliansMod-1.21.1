package net.asian.civiliansmod.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.asian.civiliansmod.chat.NpcChat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Dialogue {

    public static record Dialogues(Map<String, LanguageDialogue> dialogues) {

        public static final Codec<Dialogues> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.unboundedMap(Codec.STRING, LanguageDialogue.CODEC)
                        .fieldOf("dialogues")
                        .forGetter(Dialogues::dialogues)
        ).apply(instance, Dialogues::new));

        public static Dialogues fromMap(Map<String, Map<NpcChat.ChatReason, List<String>>> input) {
            Map<String, LanguageDialogue> langMap = new HashMap<>();
            input.forEach((lang, reasons) -> langMap.put(lang, LanguageDialogue.fromMap(reasons)));
            return new Dialogues(langMap);
        }
    }

    public static record LanguageDialogue(Map<NpcChat.ChatReason, ChatReasonDialogue> languageDialogue) {

        public static final Codec<LanguageDialogue> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.unboundedMap(NpcChat.ChatReason.CODEC, ChatReasonDialogue.CODEC)
                        .fieldOf("language_dialogues")
                        .forGetter(LanguageDialogue::languageDialogue)
        ).apply(instance, LanguageDialogue::new));

        public static LanguageDialogue fromMap(Map<NpcChat.ChatReason, List<String>> input) {
            Map<NpcChat.ChatReason, ChatReasonDialogue> map = new HashMap<>();
            input.forEach((reason, sayings) -> map.put(reason, new ChatReasonDialogue(sayings)));
            return new LanguageDialogue(map);
        }
    }

    public static record ChatReasonDialogue(List<String> sayings) {

        public static final Codec<ChatReasonDialogue> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.list(Codec.STRING)
                        .fieldOf("sayings")
                        .forGetter(ChatReasonDialogue::sayings)
        ).apply(instance, ChatReasonDialogue::new));
    }
}