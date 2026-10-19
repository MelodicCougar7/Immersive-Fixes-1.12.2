package com.melodiccougar7.immersivefixes.mixin;

import com.melodiccougar7.immersivefixes.helper.ChunkDirty;

import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorage;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityBlastFurnacePreheater;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = TileEntityBlastFurnacePreheater.class, remap = false)
public abstract class TileEntityBlastFurnacePreheaterMixin extends TileEntityIEBase {

    @Redirect(method = "doSpeedup()I", at = @At(value = "INVOKE", target = "Lblusunrize/immersiveengineering/api/energy/immersiveflux/FluxStorage;extractEnergy(IZ)I"), require = 1)
    private int dirtyOnExtract(FluxStorage storage, int energy, boolean simulate) { return ChunkDirty.extractEnergy(this, storage, energy, simulate); }
}
