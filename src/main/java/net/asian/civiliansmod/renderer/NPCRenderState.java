package net.asian.civiliansmod.renderer;

import net.minecraft.client.render.entity.state.BipedEntityRenderState;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.entity.passive.ParrotEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

public class NPCRenderState extends BipedEntityRenderState {
    public boolean slim;
    public Identifier texture;

    public boolean spectator;
    public boolean hatVisible = true;
    public boolean jacketVisible = true;
    public boolean leftPantsLegVisible = true;
    public boolean rightPantsLegVisible = true;
    public boolean leftSleeveVisible = true;
    public boolean rightSleeveVisible = true;
    public boolean capeVisible = true;
    public float glidingTicks;
    public boolean applyFlyingRotation;
    public float flyingRotation;
    public boolean handSwinging;
    @Nullable
    public Text playerName;
    @Nullable
    public ParrotEntity.Variant leftShoulderParrotVariant;
    @Nullable
    public ParrotEntity.Variant rightShoulderParrotVariant;
    public int id;
    public String name = "Steve";
    public final ItemRenderState spyglassState = new ItemRenderState();

    public NPCRenderState() {
    }

    public float getGlidingProgress() {
        return MathHelper.clamp(this.glidingTicks * this.glidingTicks / 100.0F, 0.0F, 1.0F);
    }
}
