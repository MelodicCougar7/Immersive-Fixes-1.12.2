package com.melodiccougar7.immersivefixes.mixin;

import com.melodiccougar7.immersivefixes.helper.ChunkDirty;

import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorage;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityBelljar;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityFluidPump;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityFurnaceHeater;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityLightningrod;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityTurret;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = {TileEntityFurnaceHeater.class, TileEntityBelljar.class, TileEntityTurret.class, TileEntityLightningrod.class, TileEntityFluidPump.class}, remap = false)
public abstract class FluxStorageExtractDirtyMixin extends TileEntityIEBase {

    @Redirect(method = "update()V", at = @At(value = "INVOKE", target = "Lblusunrize/immersiveengineering/api/energy/immersiveflux/FluxStorage;extractEnergy(IZ)I", remap = false), remap = true, require = 1)
    private int dirtyOnExtract(FluxStorage storage, int energy, boolean simulate) { return ChunkDirty.extractEnergy(this, storage, energy, simulate); }
}
