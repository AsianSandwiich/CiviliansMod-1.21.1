package net.asian.civiliansmod.mixin;

import net.asian.civiliansmod.networking.PlayerLanguagePayload;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.option.LanguageOptionsScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(LanguageOptionsScreen.class)
public class LanguageOptionScreenMixin {

    @Inject(method = "onDone", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;setScreen(Lnet/minecraft/client/gui/screen/Screen;)V"))
    private void onDone(CallbackInfo ci) {
        String lang = MinecraftClient.getInstance().getLanguageManager().getLanguage();
        ClientPlayNetworking.send(new PlayerLanguagePayload(MinecraftClient.getInstance().player.getUuid(), lang));
    }

}
