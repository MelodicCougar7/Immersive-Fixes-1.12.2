package com.melodiccougar7.immersivefixes.helper;

import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorage;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

public final class ChunkDirty {

    private ChunkDirty() {}

    public static void mark(TileEntity tile) {
        World world = tile.getWorld();
        if(world.isRemote) { return; }
        IChunkDirtyTile stamp = (IChunkDirtyTile)tile;
        long tick = world.getTotalWorldTime();
        if(stamp.immersivefixes$getDirtyTick()==tick) { return; }
        stamp.immersivefixes$setDirtyTick(tick);
        world.markChunkDirty(tile.getPos(), tile);
    }

    public static int extractEnergy(TileEntity tile, FluxStorage storage, int energy, boolean simulate) {
        int extracted = storage.extractEnergy(energy, simulate);
        if(!simulate&&extracted > 0) { mark(tile); }
        return extracted;
    }

    public static FluidStack drain(TileEntity tile, FluidTank tank, int maxDrain, boolean doDrain) {
        FluidStack drained = tank.drain(maxDrain, doDrain);
        if(doDrain&&drained!=null&&drained.amount > 0) { mark(tile); }
        return drained;
    }

    public interface IChunkDirtyTile {
        long immersivefixes$getDirtyTick();
        void immersivefixes$setDirtyTick(long tick);
        void postEnergyTransferUpdate(int energy, boolean simulate);
    }
}
