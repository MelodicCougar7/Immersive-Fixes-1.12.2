package com.melodiccougar7.immersivefixes.mixin;

import com.melodiccougar7.immersivefixes.helper.ChunkDirty;

import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorage;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityCapacitorLV;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = TileEntityCapacitorLV.class, remap = false)
public abstract class TileEntityCapacitorLVMixin extends TileEntityIEBase {

    @Shadow FluxStorage energyStorage;

    @Inject(method = "transferEnergy(I)V", at = @At("HEAD"), cancellable = true, require = 1)
    private void skipTransferWhenEmpty(int side, CallbackInfo ci) {
        if(energyStorage.getEnergyStored()==0) { ci.cancel(); }
    }

    @Redirect(method = "transferEnergy(I)V", at = @At(value = "INVOKE", target = "Lblusunrize/immersiveengineering/api/energy/immersiveflux/FluxStorage;modifyEnergyStored(I)V"), require = 1)
    private void dirtyOnOutput(FluxStorage storage, int energy) {
        storage.modifyEnergyStored(energy);
        if(energy!=0) { ChunkDirty.mark(this); }
    }
}
