package com.melodiccougar7.immersivefixes.mixin;

import com.melodiccougar7.immersivefixes.helper.ChunkDirty;

import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityBelljar;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = TileEntityBelljar.class, remap = false)
public abstract class TileEntityBelljarMixin extends TileEntityIEBase {

    @Shadow private float growth;

    @Redirect(method = "update()V", at = @At(value = "FIELD", target = "Lblusunrize/immersiveengineering/common/blocks/metal/TileEntityBelljar;growth:F", opcode = Opcodes.PUTFIELD, remap = false), remap = true, require = 1)
    private void dirtyOnGrowth(TileEntityBelljar belljar, float value) {
        if(growth!=value) { ChunkDirty.mark(this); }
        growth = value;
    }
}
