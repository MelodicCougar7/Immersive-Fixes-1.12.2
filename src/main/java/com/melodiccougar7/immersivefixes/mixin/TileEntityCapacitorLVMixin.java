package com.melodiccougar7.immersivefixes.mixin;

import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorage;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityCapacitorLV;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IIEInternalFluxHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = TileEntityCapacitorLV.class, remap = false)
public abstract class TileEntityCapacitorLVMixin extends TileEntityIEBase implements IIEInternalFluxHandler {

    @Shadow FluxStorage energyStorage;
    @Unique private long immersivefixes$dirtyTick = -1;

    @Inject(method = "transferEnergy(I)V", at = @At("HEAD"), cancellable = true, require = 1)
    private void skipTransferWhenEmpty(int side, CallbackInfo ci) {
        if(energyStorage.getEnergyStored()==0) { ci.cancel(); }
    }

    @Redirect(method = "transferEnergy(I)V", at = @At(value = "INVOKE", target = "Lblusunrize/immersiveengineering/api/energy/immersiveflux/FluxStorage;modifyEnergyStored(I)V"), require = 1)
    private void dirtyOnOutput(FluxStorage storage, int energy) {
        storage.modifyEnergyStored(energy);
        if(energy!=0) { immersivefixes$markChunkDirty(); }
    }

    @Override public void postEnergyTransferUpdate(int energy, boolean simulate) {
        if(energy!=0&&!simulate) { immersivefixes$markChunkDirty(); }
    }

    @Unique private void immersivefixes$markChunkDirty() {
        long tick = world.getTotalWorldTime();
        if(immersivefixes$dirtyTick==tick) { return; }
        immersivefixes$dirtyTick = tick;
        world.markChunkDirty(pos, this);
    }
}
