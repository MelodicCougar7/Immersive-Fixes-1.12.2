package com.melodiccougar7.immersivefixes.mixin;

import com.melodiccougar7.immersivefixes.helper.ChunkDirty;

import blusunrize.immersiveengineering.common.blocks.metal.TileEntityBreakerSwitch;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityRedstoneBreaker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = TileEntityRedstoneBreaker.class, remap = false)
public abstract class TileEntityRedstoneBreakerMixin extends TileEntityBreakerSwitch {

    @Inject(method = "update()V", at = @At(value = "INVOKE", target = "Lblusunrize/immersiveengineering/api/energy/wires/ImmersiveNetHandler;resetCachedIndirectConnections(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)V", shift = At.Shift.AFTER, remap = false), remap = true, require = 1)
    private void dirtyAndSyncAfterFlip(CallbackInfo ci) {
        ChunkDirty.mark(this);
        world.addBlockEvent(getPos(), getBlockType(), active?1: 0, 0);
    }
}
