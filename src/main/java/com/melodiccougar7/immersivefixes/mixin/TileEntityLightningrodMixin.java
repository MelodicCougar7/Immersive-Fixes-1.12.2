package com.melodiccougar7.immersivefixes.mixin;

import com.melodiccougar7.immersivefixes.helper.ChunkDirty;

import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorage;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityLightningrod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = TileEntityLightningrod.class, remap = false)
public abstract class TileEntityLightningrodMixin extends TileEntityIEBase {

    @Redirect(method = "update()V", at = @At(value = "INVOKE", target = "Lblusunrize/immersiveengineering/api/energy/immersiveflux/FluxStorage;setEnergy(I)V", remap = false), remap = true, require = 1)
    private void dirtyOnStrike(FluxStorage storage, int energy) {
        if(storage.getEnergyStored()!=energy) { ChunkDirty.mark(this); }
        storage.setEnergy(energy);
    }
}
