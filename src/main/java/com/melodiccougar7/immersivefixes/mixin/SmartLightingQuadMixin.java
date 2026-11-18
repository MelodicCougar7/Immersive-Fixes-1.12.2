package com.melodiccougar7.immersivefixes.mixin;

import blusunrize.immersiveengineering.client.models.SmartLightingQuad;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.model.pipeline.IVertexConsumer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = SmartLightingQuad.class, remap = false)
public abstract class SmartLightingQuadMixin {

    @Unique private static final ThreadLocal<int[]> immersivefixes$lastLight = ThreadLocal.withInitial(() -> new int[5]);

    @Inject(method = "pipe(Lnet/minecraftforge/client/model/pipeline/IVertexConsumer;)V", at = @At("HEAD"), require = 1)
    private void immersivefixes$forgetLastLight(IVertexConsumer consumer, CallbackInfo ci) {
        immersivefixes$lastLight.get()[0] = 0;
    }

    @Redirect(method = "pipe(Lnet/minecraftforge/client/model/pipeline/IVertexConsumer;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/IBlockAccess;getCombinedLight(Lnet/minecraft/util/math/BlockPos;I)I", remap = true), require = 1)
    private int immersivefixes$reuseLastLight(IBlockAccess world, BlockPos pos, int lightValue) {
        int[] last = immersivefixes$lastLight.get();
        if(last[0]==1&&last[1]==pos.getX()&&last[2]==pos.getY()&&last[3]==pos.getZ()) { return last[4]; }
        int light = world.getCombinedLight(pos, lightValue);
        last[0] = 1;
        last[1] = pos.getX();
        last[2] = pos.getY();
        last[3] = pos.getZ();
        last[4] = light;
        return light;
    }
}
