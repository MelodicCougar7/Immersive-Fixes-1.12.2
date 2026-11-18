package com.melodiccougar7.immersivefixes.mixin;

import blusunrize.immersiveengineering.api.energy.wires.TileEntityImmersiveConnectable;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityEnergyMeter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = TileEntityEnergyMeter.class, remap = false)
public abstract class TileEntityEnergyMeterMixin extends TileEntityImmersiveConnectable {

    @Redirect(method = "updateComparatorValues()V", at = @At(value = "INVOKE", target = "Ljava/lang/Math;ceil(D)D"), require = 1)
    private double clampComparatorValue(double value) {
        if(Double.isNaN(value)||Double.isInfinite(value)) { return 0; }
        return Math.max(0, Math.min(15, Math.ceil(value)));
    }
}
