package net.asian.civiliansmod.entity;

import com.mojang.serialization.Codec;
import net.minecraft.util.dynamic.Codecs;
import net.asian.civiliansmod.CiviliansMod;
import net.asian.civiliansmod.util.NPCUtil;
import net.asian.civiliansmod.util.SkinIdentifier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;

import java.util.Optional;

public class SkinManager {

    private byte[] skinByteArray;
    private SkinIdentifier skinIdentifier;

    private int baseVariant;
    private boolean slim;
    private boolean defaultSkin;

    private final NPCEntity npcEntity;

    public SkinManager(NPCEntity npcEntity) {
        this.npcEntity = npcEntity;
        this.defaultSkin = true;

        // random basevariant
        this.baseVariant = npcEntity.getRandom().nextInt(88);
        this.slim = this.baseVariant > 43;

        // random name
        if (npcEntity.nameManager != null) {
            npcEntity.nameManager.setRandomName(this.slim);
        } else {
            CiviliansMod.LOGGER.warn("[CiviliansMod] nameManager is null in SkinManager constructor");
        }
    }

    public boolean isSlim() {
        return slim;
    }

    public void setSlim(boolean slim) {
        this.slim = slim;
    }

    public void setBaseVariant(int baseVariant) {
        this.baseVariant = baseVariant;
    }

    public void setSkinByteArray(byte[] skinByteArray) {
        this.skinByteArray = skinByteArray;
    }

    @Environment(EnvType.CLIENT)
    public void setIdSkin(SkinIdentifier skin) {
        this.skinIdentifier = skin;
    }

    @Environment(EnvType.CLIENT)
    public SkinIdentifier getIdSkin() {
        if (this.skinIdentifier == null) {
            return NPCUtil.getNPCTexture(baseVariant);
        }
        return this.skinIdentifier;
    }

    void writeView(WriteView writeView) {
        writeView.putInt("basevariat", baseVariant);
        if (skinByteArray != null) {
            writeView.put("skin", Skin.CODEC, new Skin(skinByteArray));
        }
    }

    void readNbt(ReadView readView) {
        this.baseVariant = readView.getInt("basevariat", 0);
        Optional<Skin> skin = readView.read("skin", Skin.CODEC);
        skin.ifPresent(s -> this.skinByteArray = s.skin);
    }

    public static class Skin {
        public static final Codec<Skin> CODEC = Codecs.BASE_64.xmap(Skin::new, s -> s.skin);
        public final byte[] skin;

        public Skin(byte[] skin) {
            this.skin = skin;
        }
    }
    public byte[] getSkinByteArray() {
        return skinByteArray;
    }

    public int getBaseVariant() {
        return baseVariant;
    }

    public boolean isSlimModel() {
        return slim;
    }
    public boolean isDefaultSkin() {
        return defaultSkin;
    }
}
