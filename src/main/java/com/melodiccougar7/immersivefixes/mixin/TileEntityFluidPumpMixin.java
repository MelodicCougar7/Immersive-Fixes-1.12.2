package com.melodiccougar7.immersivefixes.mixin;

import com.melodiccougar7.immersivefixes.helper.ChunkDirty;

import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorage;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityFluidPump;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = TileEntityFluidPump.class, remap = false)
public abstract class TileEntityFluidPumpMixin extends TileEntityIEBase {

    @Redirect(method = {"outputFluid(Lnet/minecraftforge/fluids/FluidStack;Z)I", "canOutputPressurized(Z)Z"}, at = @At(value = "INVOKE", target = "Lblusunrize/immersiveengineering/api/energy/immersiveflux/FluxStorage;extractEnergy(IZ)I"), require = 5)
    private int dirtyOnExtract(FluxStorage storage, int energy, boolean simulate) { return ChunkDirty.extractEnergy(this, storage, energy, simulate); }
}
