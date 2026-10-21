package com.melodiccougar7.immersivefixes.mixin;

import com.melodiccougar7.immersivefixes.helper.ChunkDirty;

import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import blusunrize.immersiveengineering.common.blocks.stone.TileEntityAlloySmelter;
import blusunrize.immersiveengineering.common.blocks.stone.TileEntityBlastFurnace;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = {TileEntityBlastFurnace.class, TileEntityAlloySmelter.class}, remap = false)
public abstract class StoneFurnaceDirtyMixin extends TileEntityIEBase {

    @Shadow(remap = false) public int burnTime;
    @Unique private int immersivefixes$burnTimeAtStart;

    @Inject(method = "update()V", at = @At("HEAD"), remap = true, require = 1)
    private void recordBurnTime(CallbackInfo ci) { immersivefixes$burnTimeAtStart = burnTime; }

    @Inject(method = "update()V", at = @At("RETURN"), remap = true, require = 1)
    private void dirtyWhileBurning(CallbackInfo ci) {
        if(immersivefixes$burnTimeAtStart > 0||burnTime!=immersivefixes$burnTimeAtStart) { ChunkDirty.mark(this); }
    }
}
