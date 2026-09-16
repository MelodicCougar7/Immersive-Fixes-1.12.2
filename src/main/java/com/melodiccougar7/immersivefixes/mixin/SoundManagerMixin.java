package com.melodiccougar7.immersivefixes.mixin;

import com.melodiccougar7.immersivefixes.helper.EarmuffHandler;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.SoundManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SoundManager.class)
public abstract class SoundManagerMixin {

    @Inject(method = "getClampedVolume", at = @At("RETURN"), cancellable = true)
    private void applyEarmuffVolume(ISound soundIn, CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(EarmuffHandler.apply(cir.getReturnValue(), soundIn));
    }
}
