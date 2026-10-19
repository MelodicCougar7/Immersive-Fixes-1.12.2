package com.melodiccougar7.immersivefixes.mixin;

import com.melodiccougar7.immersivefixes.helper.ChunkDirty;

import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityBelljar;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityDieselGenerator;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityFluidPlacer;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityFluidPump;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntitySheetmetalTank;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = {TileEntityBelljar.class, TileEntityFluidPump.class, TileEntityDieselGenerator.class, TileEntitySheetmetalTank.class, TileEntityFluidPlacer.class}, remap = false)
public abstract class FluidTankDrainDirtyMixin extends TileEntityIEBase {

    @Redirect(method = "update()V", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/fluids/FluidTank;drain(IZ)Lnet/minecraftforge/fluids/FluidStack;", remap = false), remap = true, require = 1)
    private FluidStack dirtyOnDrain(FluidTank tank, int maxDrain, boolean doDrain) { return ChunkDirty.drain(this, tank, maxDrain, doDrain); }
}
