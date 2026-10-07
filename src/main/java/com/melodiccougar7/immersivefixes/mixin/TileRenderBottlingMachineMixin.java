package com.melodiccougar7.immersivefixes.mixin;

import blusunrize.immersiveengineering.client.render.TileRenderBottlingMachine;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @since 06.10.2026
 * @author Pabilo8 (pabilo@iiteam.net)
 */
@Mixin(value = TileRenderBottlingMachine.class, remap = false)
public abstract class TileRenderBottlingMachineMixin {

    @Inject(
            method = {"render(Lblusunrize/immersiveengineering/common/blocks/metal/TileEntityBottlingMachine;DDDFIF)V"},
            at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glEnable(I)V", remap = false),
            remap = false,
            require = 1,
            allow = 1
    )
    private void immersivefixes$saveStencilState(CallbackInfo ci) {
        GL11.glPushAttrib(GL11.GL_STENCIL_BUFFER_BIT);
    }

    @Inject(
            method = {"render(Lblusunrize/immersiveengineering/common/blocks/metal/TileEntityBottlingMachine;DDDFIF)V"},
            at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glDisable(I)V",
                    shift = At.Shift.AFTER, remap = false),
            remap = false,
            require = 1,
            allow = 1
    )
    private void immersivefixes$restoreStencilState(CallbackInfo ci) {
        GL11.glPopAttrib();
    }
}
