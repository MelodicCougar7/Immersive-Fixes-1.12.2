package com.melodiccougar7.immersivefixes.mixin;

import com.melodiccougar7.immersivefixes.helper.ChunkDirty;

import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorageAdvanced;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityChargingStation;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityExcavator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = {TileEntityChargingStation.class, TileEntityExcavator.class}, remap = false)
public abstract class FluxStorageAdvancedExtractDirtyMixin extends TileEntityIEBase {

    @Redirect(method = "update()V", at = @At(value = "INVOKE", target = "Lblusunrize/immersiveengineering/api/energy/immersiveflux/FluxStorageAdvanced;extractEnergy(IZ)I", remap = false), remap = true, require = 1)
    private int dirtyOnExtract(FluxStorageAdvanced storage, int energy, boolean simulate) { return ChunkDirty.extractEnergy(this, storage, energy, simulate); }
}
