package net.asian.civiliansmod.util;

import net.minecraft.util.Identifier;

public record SkinIdentifier(Identifier id, boolean slim, boolean custom) {


    @Override
    public int hashCode() {
        int i = id.hashCode();
        i = 31 * i + (slim ? 1 : 0);
        i = 31 * i + (custom ? 1 : 0);
        return i;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof SkinIdentifier(Identifier id1, boolean slim1, boolean custom1))) return false;
        if (!id1.equals(id)) return false;
        if (slim != slim1) return false;
        return custom == custom1;
    }
}