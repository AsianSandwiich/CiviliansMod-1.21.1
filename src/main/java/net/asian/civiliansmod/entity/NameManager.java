package net.asian.civiliansmod.entity;

import net.minecraft.text.Text;
import net.minecraft.util.math.random.Random;

public class NameManager {
    String[] defaultModelNames = {"Charles", "Cade", "Henry", "Liam", "Rodney", "Nathaniel", "Elliot", "Julian", "Malcolm", "Tobias",
            "Wesley", "Felix", "Desmond", "Simon", "Miles", "Everett", "Dorian", "Quentin", "Cedric", "Adrian", "Roman", "Marcus", "Gideon", "Levi", "Jasper"};
    String[] slimModelNames = {"Evelyn", "Sarah", "Olivia", "Emma", "Alexia", "Amelia", "Celeste", "Lillian", "Joleen", "Rosalie",
            "Clara", "Vivienne", "Elena", "Margot", "Nora", "Daphne", "Fiona", "Genevieve", "Juliette", "Lucille", "Naomi", "Ivy", "Serena", "Vera", "Adelaide"};

    NPCEntity npcEntity;

    public NameManager(NPCEntity npcEntity) {
        this.npcEntity = npcEntity;
    }

    public void setRandomName(boolean slim) {
        if (slim)
            this.npcEntity.setCustomName(Text.literal(slimModelNames[Random.create().nextInt(slimModelNames.length)]));
        else
            this.npcEntity.setCustomName(Text.literal(defaultModelNames[Random.create().nextInt(defaultModelNames.length)]));

    }
}
