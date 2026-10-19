package com.melodiccougar7.immersivefixes.mixin;

import com.melodiccougar7.immersivefixes.helper.ChunkDirty;

import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityBottlingMachine;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityBottlingMachine.BottlingProcess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = TileEntityBottlingMachine.class, remap = false)
public abstract class TileEntityBottlingMachineMixin extends TileEntityIEBase {

    @Redirect(method = "update()V", at = @At(value = "INVOKE", target = "Lblusunrize/immersiveengineering/common/blocks/metal/TileEntityBottlingMachine$BottlingProcess;processStep(Lblusunrize/immersiveengineering/common/blocks/metal/TileEntityBottlingMachine;)Z", remap = false), remap = true, require = 1)
    private boolean dirtyOnProcessStep(BottlingProcess process, TileEntityBottlingMachine tile) {
        boolean stepped = process.processStep(tile);
        if(stepped) { ChunkDirty.mark(this); }
        return stepped;
    }
}
