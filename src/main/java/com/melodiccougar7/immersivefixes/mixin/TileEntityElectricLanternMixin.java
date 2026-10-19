package com.melodiccougar7.immersivefixes.mixin;

import com.melodiccougar7.immersivefixes.helper.ChunkDirty;

import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityElectricLantern;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = TileEntityElectricLantern.class, remap = false)
public abstract class TileEntityElectricLanternMixin extends TileEntityIEBase {

    @Inject(method = "update()V", at = @At(value = "FIELD", target = "Lblusunrize/immersiveengineering/common/blocks/metal/TileEntityElectricLantern;energyStorage:I", opcode = Opcodes.PUTFIELD, remap = false), remap = true, require = 1)
    private void dirtyOnDraw(CallbackInfo ci) { ChunkDirty.mark(this); }

    @Inject(method = "outputEnergy(IZI)I", at = @At(value = "FIELD", target = "Lblusunrize/immersiveengineering/common/blocks/metal/TileEntityElectricLantern;energyStorage:I", opcode = Opcodes.PUTFIELD), require = 1)
    private void dirtyOnReceive(CallbackInfoReturnable<Integer> cir) { ChunkDirty.mark(this); }
}
