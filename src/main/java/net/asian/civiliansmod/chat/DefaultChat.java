package net.asian.civiliansmod.chat;

import java.util.*;

public class DefaultChat {

    /**
     * method to get the default chat based on a language
     *
     * @return a map of chat. First String being the language, second map being the dialogues.
     */
    public static Map<String, Map<NpcChat.ChatReason, List<String>>> getDefaultChat() {
        Map<String, Map<NpcChat.ChatReason, List<String>>> dialogues = new HashMap<>();

        dialogues.put("en_us", getEnglish());
        dialogues.put("fr_fr", getFrench());
        dialogues.put("fr_ca", getCanadianFrench());

        return dialogues;
    }

    private static Map<NpcChat.ChatReason, List<String>> getEnglish() {
        Map<NpcChat.ChatReason, List<String>> englishDialogues = new LinkedHashMap<>();
        List<String> englishHurt = new ArrayList<>(List.of(
                "Ouch! That hurt!",
                "Hey, watch it!",
                "Why would you do that?!",
                "Stop hitting me!",
                "What’s wrong with you?",
                "Please, don’t hurt me!",
                "What have I done to deserve this?!",
                "Fight me fair and square!",
                "Watch it pal, you don't know who you're messing with.",
                "Ow!",
                "GET AWAY FROM ME!",
                "Why must this world cast unfortunate events upon me!",
                "Hey...please stop I have already had a long day.",
                "IF ONLY THERE WAS A HERO WHO COULD SAVE ME!",
                "Lash your anger out on the sheep, not me!",
                "I'm so sorry, I'm so sorry!",
                "The prophecy foretold you would do this.",
                "Friends shouldn't hurt other friends!",
                "@$%#&!!"
        ));
        List<String> englishInteraction = new ArrayList<>(List.of(
                "Hello there, traveler! How can I help you?",
                "I hope you're enjoying the day.",
                "Stay safe—the world is dangerous.",
                "There's treasure hidden nearby... or so I've heard.",
                "Don't forget to stay out of trouble!",
                "I'm here to help you, traveler.",
                "What can I do for you?",
                "I'm so hungry... Got any spare food?",
                "I need to get my eyes checked, everything looks pixelated!",
                "Sometimes it feels like I'm in a dream. I'm not sure what to do.",
                "Hey! Can I help you something traveler?",
                "Some would say the world is flat... can you believe that?",
                "I don't have time to talk right now, I'm sorry!",
                "Wow you look totally awesome, I might copy your look!",
                "I need to find the hidden treasure, rumors have it that it's somewhere around here.",
                "I love this place, it's a lot of fun to be here!",
                "I hope someone got rid of that scary dragon... I'm sure it's not here anymore.",
                "Want to go hunting with me?",
                "Have you seen my friend? He's a little bit of a troublemaker.",
                "Hopefully this place doesn't get too crowded...",
                "I am surprised to see there are not more people here...",
                "I'm so happy to see you, traveler!",
                "When the birds sing, I can't help but sing along too.",
                "I feel this unforgiving anger built up in my body! MUST... MUST... STOP!",
                "Oop! Excuse me, let me just squeeze past ya",
                "Darkness consumes me...",
                "I AM SO HAPPY TO SEE YOU AGAIN! I LOVE YOU!",
                "Hey, you're doing great"
        ));
        englishDialogues.put(NpcChat.ChatReason.HURT, englishHurt);
        englishDialogues.put(NpcChat.ChatReason.INTERACT, englishInteraction);
        return englishDialogues;
    }

    private static Map<NpcChat.ChatReason, List<String>> getFrench() {
        Map<NpcChat.ChatReason, List<String>> frenchDialogues = new LinkedHashMap<>();
        List<String> frenchHurt = new ArrayList<>(List.of(
                "Aïe! Ca fait mal!",
                "Pourquoi tu fais ça?!",
                "Arrête de me taper!",
                "Qu'est ce qui va pas avec toi?",
                "Steuplait, arrête de me taper!",
                "Qu'est ce que j'ai fais pour mériter ça?!",
                "Fais gaffe, tu sais pas à qui tu fais face.",
                "Ow!",
                "VA-T'EN!",
                "Hey...arrête stp, j'ai déjà eu une longue journée.",
                "SI SEULEMENT IL Y AVAIT UN HEROS POUR ME SAUVER!",
                "Les amis ne devraient pas se taper ensemble!",
                "@$%#&!!"
        ));
        List<String> frenchInteraction = new ArrayList<>(List.of(
                "Bonjour, voyageur ! Comment puis-je t'aider ?",
                "J'espère que tu passes une bonne journée.",
                "Reste prudent, le monde est dangereux.",
                "Il y aurait un trésor caché dans le coin... du moins, c'est ce qu'on dit.",
                "N'oublie pas de rester hors des ennuis !",
                "Je suis là pour t'aider, voyageur.",
                "Que puis-je faire pour toi ?",
                "J'ai tellement faim... Tu n'aurais pas un peu de nourriture en trop ?",
                "Je devrais faire vérifier mes yeux, tout a l'air pixelisé !",
                "Parfois, j'ai l'impression d'être dans un rêve. Je ne sais pas quoi faire.",
                "Hé ! Puis-je vous aider, voyageur ?",
                "Certains disent que le monde est plat... peux-tu croire ça ?",
                "Je n'ai pas le temps de parler maintenant, désolé !",
                "Wow, tu as trop la classe, je pourrais copier votre style !",
                "Je dois trouver le trésor caché, la rumeur dit qu'il est quelque part dans les parages.",
                "J'adore cet endroit, c'est vraiment agréable d'être ici !",
                "J'espère que quelqu'un s'est débarrassé de ce dragon effrayant... Je suis sûr qu'il n'est plus là.",
                "Ça vous dit d'aller chasser avec moi ?",
                "As-tu vu mon ami ? Il a tendance à faire des bêtises.",
                "J'espère que cet endroit ne sera pas trop bondé...",
                "Je suis surpris de ne pas voir plus de monde ici...",
                "Je suis tellement heureux de te voir, voyageur !",
                "Quand les oiseaux chantent, je ne peux pas m'empêcher de chanter aussi.",
                "Je ressens une colère incontrôlable monter en moi ! JE DOIS... JE DOIS... M'ARRÊTER !",
                "Oups ! Excuses-moi, laisse moi juste passer.",
                "Les ténèbres me consument...",
                "JE SUIS TELLEMENT HEUREUX DE TE REVOIR ! JE T'AIME !",
                "Hé, tu te débrouilles super bien !"
        ));
        frenchDialogues.put(NpcChat.ChatReason.HURT, frenchHurt);
        frenchDialogues.put(NpcChat.ChatReason.INTERACT, frenchInteraction);
        return frenchDialogues;
    }

    private static Map<NpcChat.ChatReason, List<String>> getCanadianFrench(){
        Map<NpcChat.ChatReason, List<String>> quebec = new LinkedHashMap<>();
        List<String> quebecHurt = new ArrayList<>(List.of(
                "Tabarnak! Ça fait mal!",
                "Pourquoi tu fais ça, câlisse?!",
                "Arrête de me frapper, ostie!",
                "T'es-tu en train de niaiser, toi là?",
                "Steuplait, arrête de me sacrer des coups!",
                "Quessé j’ai fait pour mériter ça, maudit?!",
                "Fais attention, tu sais pas à qui tu t’attaques.",
                "Aye, ostie!",
                "DÉCALISSE!",
                "Hey... arrête stp, j’ai déjà eu une maudite grosse journée.",
                "SI SEULEMENT Y AVAIT UN HÉROS POUR ME SAUVER, TABARNAK!",
                "Les chums sont pas supposés se taper dessus!",
                "@$%#&!!"
        ));
        List<String> quebecInteraction = new ArrayList<>(List.of(
                "Salut là, voyageur ! Quessé j’peux faire pour toi ?",
                "J'espère que t’as une belle journée.",
                "Fais attention, le monde est ben dangereux.",
                "Y'aurait un trésor caché pas loin… du moins, c’est c’qui se dit.",
                "Fais pas trop de conneries, hein !",
                "Chu là pour t’aider, mon chum.",
                "Quessé tu veux que j'fasse pour toi ?",
                "J’ai la dalle... T’as pas un p’tit quelque chose à manger ?",
                "Faudrait que j'aille checker ma vue, tout est full pixelisé !",
                "Des fois, j’ai l’impression d’être dans un rêve. J’sais pas quoi faire.",
                "Hey ! J’peux t’aider avec de quoi, voyageur ?",
                "Y’en a qui disent que la Terre est plate... peux-tu croire ça ?",
                "Pas le temps de jaser là, désolé !",
                "Ouin, t’as du style toi, j’pourrais ben te copier !",
                "Faut que je trouve l’trésor caché, paraîtrait qu’il est dans l’coin.",
                "J’adore icitte, c’est ben l’fun d’être là !",
                "J’espère que quelqu’un a sacré son camp à c’dragon-là… j’suis sûr qu’il est pu là.",
                "Ça te tente-tu d’aller chasser avec moi ?",
                "T’as pas vu mon chum ? Y’est toujours en train de niaiser.",
                "Espérons que ça devienne pas trop plein icitte...",
                "J’suis surpris qu’y ait pas plus de monde dans l’coin...",
                "Ben content de te voir, voyageur !",
                "Quand les oiseaux chantent, j’peux pas m’empêcher de chanter aussi.",
                "J’feel en tabarnak... FAUT... FAUT... QUE J’ME CALME !",
                "Oups ! Pardon, laisse-moi passer là.",
                "L’obscurité m’envahit...",
                "TABARNAK J'SUIS CONTENT DE TE REVOIR ! J'T'AIME !",
                "Hey, tu gères ça comme un chef !"
        ));
        quebec.put(NpcChat.ChatReason.HURT, quebecHurt);
        quebec.put(NpcChat.ChatReason.INTERACT, quebecInteraction);
        return quebec;
    }
}
